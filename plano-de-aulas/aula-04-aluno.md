# Material do Aluno — Aula 4: Validar um JWT, proteger a chave e escolher o fluxo certo do OAuth 2.0

> **Sexta, 06/11/2026 · Tempo de leitura:** ~16 min. Você já sabe abrir um JWT e ler o que tem dentro. Hoje vem a pergunta que importa: **dá para falsificar?** Vamos tentar de vários jeitos, descobrir que tudo depende de uma chave e ver o que acontece quando um token é roubado. Na segunda metade, o OAuth 2.0: como um aplicativo acessa os seus dados em outro serviço **sem que você entregue a sua senha a ele**. Para fechar, o par **access token + refresh token** rodando num projeto Spring Security.
>
> **Código:** `aula03` e `aula04`, publicados no LMS. O `aula03` é a API de alunos (JWT feito à mão); o `aula04` é um projeto à parte, o `auth-jwt`.
>
> As três pausas de sempre: 🎬 **Imagine o cenário**, 💭 **Pare e reflita** e ✍️ **Experimente agora**.

---

## 1. Dá para falsificar um JWT?

> 🎬 **Imagine o cenário**
> Uma equipe de um banco está construindo um portal interno de aprovação de crédito. Um analista curioso copia o token que o portal guarda no navegador, cola num decodificador e lê: `"role":"USER"`. Ele pensa: "se eu trocar por `ADMIN`, aprovo o meu próprio limite". O payload é só Base64, qualquer um edita. Ele vira administrador?

Relembrando: um JWT tem três partes separadas por ponto, `header.payload.assinatura`. As duas primeiras são JSON em Base64URL, que **qualquer pessoa lê sem chave**. A terceira é um **HMAC-SHA256** de `header.payload`, calculado com a chave do servidor. O JWT é **assinado, não escondido**.

Editar o payload é fácil. O que decide se a edição **funciona** é a validação do servidor. No `aula03`, ela está em `JwtManual.validar` (`jwt/JwtManual.java`). Trecho, com os comentários resumidos:

```java
// ---- 2) ALGORITMO -------------------------------------------------------
// O servidor usa o que ELE espera, nunca o que o token diz.
// Tokens com "alg": "none" (sem assinatura) são recusados aqui.
Map<String, Object> header = doJson(partes[0]);
if (!ALGORITMO.equals(header.get("alg"))) {
    return recusado("algoritmo não permitido: " + header.get("alg"));
}

// ---- 3) ASSINATURA ------------------------------------------------------
// Recalcula o HMAC de "header.payload" com a NOSSA chave e compara com o que veio.
byte[] esperada = hmac(chave, partes[0] + "." + partes[1]);
byte[] recebida = Base64.getUrlDecoder().decode(partes[2]);
if (!MessageDigest.isEqual(esperada, recebida)) {
    return recusado("assinatura inválida: o token foi alterado ou assinado com outra chave");
}

// ---- 4) EMISSOR ---------------------------------------------------------
// Só agora é seguro confiar no payload: a assinatura provou que fomos nós que o criamos.
Map<String, Object> payload = doJson(partes[1]);
if (!EMISSOR.equals(payload.get("iss"))) {
    return recusado("emissor inválido");
}

// ---- 5) EXPIRAÇÃO -------------------------------------------------------
Object exp = payload.get("exp");
if (!(exp instanceof Number) || Instant.now().getEpochSecond() >= ((Number) exp).longValue()) {
    return recusado("token expirado");
}
```

Antes disso, o método confere o **formato** (três partes). Depois, são três verificações, nesta ordem:

1. **O algoritmo é o que o servidor espera?** O `alg` que vem no header é só uma declaração de quem montou o token. O servidor compara com o **seu** (`HS256`) e não aceita outro.
2. **A assinatura bate?** O servidor refaz o HMAC com a chave dele e compara com o que veio. Mudou uma letra do header ou do payload, o resultado muda por inteiro. O `MessageDigest.isEqual` compara em **tempo constante**: um `equals` comum para no primeiro byte diferente, e a diferença de tempo daria pistas a um atacante.
3. **As claims estão certas?** Emissor (`iss`) e expiração (`exp`). Repare: o conteúdo só é lido **depois** de provar que a assinatura é autêntica.

✍️ **Experimente agora.** Suba o `aula03` (`mvn spring-boot:run`, na pasta do projeto) e abra o `http/aula03.http` no IntelliJ, com o ambiente **dev** selecionado. Usuários: `ana` / `senha123` (USER) e `admin` / `admin789` (ADMIN). Rode a **2.1** (login da Ana) e depois:

- **4.1** (`POST /jwt/validar`) com o token verdadeiro: `valido: true`, `motivo: "token válido"`.
- **4.2**, um token montado à mão com `"alg":"none"` e sem assinatura:

```http
### 4.2 Token SEM assinatura (alg none): o servidor recusa
POST {{host}}/jwt/validar
Content-Type: application/json

{"token": "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJpc3MiOiJhZGEtc2VndXJhbmNhIiwic3ViIjoiYW5hIiwicm9sZSI6IkFETUlOIiwiZXhwIjo0MTAyNDQ0ODAwfQ."}
```

- **4.4**: altere o token da Ana (troque `USER` por `ADMIN`, assine com uma chave qualquer, como `123`) e cole-o na requisição. Leia o `motivo`.
- **4.5** e **4.6**: um token gerado pela **biblioteca** java-jwt é aceito pela validação **feita à mão**. JWT é um padrão, não uma invenção da nossa API.

Agora monte o seu ataque: mude o `role`, estique o `exp`, use o `alg: none`, assine com outra chave. Para cada tentativa, aponte **qual bloco** do trecho acima a barra.

> **Sem acesso ao jwt.io?** A rede da empresa pode bloquear o site. Tudo o que importa tem versão local: a **3.1** (`POST /jwt/decodificar`) mostra header e payload sem chave, e a **4.1**/**4.4** (`POST /jwt/validar`) explicam o motivo de cada recusa. No Git Bash, `echo "$TOKEN" | cut -d. -f2 | base64 -d` mostra o payload (README do `aula03`). Para reassinar um token alterado, o instrutor mostra na tela como fazer no terminal.

Duas observações sobre o código da aula. Os endpoints `/jwt/gerar`, `/jwt/decodificar` e `/jwt/validar` **só existem para estudo**: num sistema real, só o login emite token e ninguém expõe "validar" para o mundo. E a rota de verdade, `/jwt/protegido`, **não diz** por que recusou: responde `401` genérico, e o motivo vai só para o log do servidor (`Token recusado: ...`).

💭 **Pare e reflita.** Por que o `/jwt/validar` explica o motivo da recusa e o `/jwt/protegido` não? A quem ajudaria saber que foi "assinatura inválida" e não "token expirado"?

---

## 2. Quem guarda a chave?

Se tudo depende da assinatura, tudo depende da **chave**. Quem tem a chave emite o token que quiser, com o perfil que quiser.

✍️ **Experimente agora: o desafio da chave fraca.** Sua missão é tornar-se ADMIN numa rota que usa uma chave fraca. **Não abra o `DesafioController.java`**: a graça está em descobrir.

```http
### 6.1 Pegue o token de USER do desafio
GET {{host}}/jwt/desafio/token

> {% client.global.set("tokenDesafio", response.body.token); %}

### 6.2 Tente entrar como ADMIN com o token de USER: 403
GET {{host}}/jwt/desafio/admin
Authorization: Bearer {{tokenDesafio}}

### 6.3 Depois de descobrir a chave e forjar o token ADMIN no jwt.io, cole-o aqui
GET {{host}}/jwt/desafio/admin
Authorization: Bearer COLE_AQUI_O_TOKEN_FORJADO
```

A resposta da 6.1 traz a missão e uma dica: a chave é uma palavra fácil. Cole o token no jwt.io, tente palavras no campo da chave até a assinatura ser verificada, troque o `role` e rode a 6.3. Depois, tente o mesmo com o token do login (2.1).

O ponto do desafio: **o algoritmo é seguro; a chave fraca não é.** E quem ataca de verdade não digita palavras uma a uma. Com o token em mãos, ele testa milhões de chaves por segundo **no próprio computador**, sem que o servidor perceba uma única tentativa. Por isso a chave precisa ser **longa e aleatória**: para HS256, no mínimo 256 bits (32 bytes).

Agora olhe o `application.yml` do `aula03`:

```yaml
app:
  demo:
    jwt-secret: ${JWT_DEMO_SECRET:chave-de-demonstracao-da-aula-03-nao-use-em-producao}
```

💭 **Pare e reflita.** A chave é longa. Mesmo assim, o que está errado neste arquivo? Quem consegue ler essa chave hoje?

> **Ética:** o desafio e as tentativas de falsificação são só contra a API da aula, na sua máquina. Nunca teste em sistemas de terceiros e nunca cole num site um JWT real de um sistema da empresa: **um JWT é uma credencial**, e colar um token real num site é como colar uma senha.

### HS256 × RS256: segredo compartilhado ou par de chaves

O `HS256` que usamos é **HMAC com SHA-256**. É **simétrico**: a **mesma** chave assina e confere (o comentário de `JwtManual` diz isso com todas as letras). O `RS256` (RSA com SHA-256) e o `ES256` (curva elíptica) são **assimétricos**: usam um **par de chaves**, como a assinatura digital que vimos com RSA.

| | **HS256** (segredo compartilhado) | **RS256** (par de chaves) |
|---|---|---|
| Quem assina? | quem tem a chave | quem tem a chave **privada** |
| Quem valida? | quem tem a **mesma** chave | qualquer um, com a chave **pública** |
| Consequência | quem valida também **consegue assinar** | quem valida **não consegue** forjar |
| Ideal para | uma aplicação só, que emite e valida | vários serviços validando o token de um emissor |

💭 **Pare e reflita.** Um serviço de login emite tokens e 10 microsserviços precisam validá-los. Com HS256, onde a chave teria de estar? E se um desses 10 fosse invadido? E mais uma: trocar a chave invalida **todos** os tokens emitidos. Como uma empresa troca a chave sem derrubar todo mundo?

---

## 3. E se roubarem o token?

Alguém copiou o token da Ana: viu na tela, achou num log, um script malicioso levou. O servidor não sabe que não é a Ana: **quem tem o token age como o dono até ele vencer**.

✍️ **Experimente agora.** Gere um token de 20 segundos e use-o na hora e depois de 30 segundos:

```http
### 5.1 Token que dura só 20 segundos
POST {{host}}/jwt/gerar
Content-Type: application/json

{"usuario": "ana", "perfil": "USER", "expiraEmSegundos": 20}

> {% client.global.set("tokenCurto", response.body.token); %}

### 5.2 Use agora (200) e de novo depois de 30 segundos (401). O motivo aparece no log do servidor
GET {{host}}/jwt/protegido
Authorization: Bearer {{tokenCurto}}
```

No log: `Token recusado: token expirado`. O token do login da aula dura 5 minutos (`VALIDADE_PADRAO_SEGUNDOS = 300`, no `JwtController`).

**Onde guardar o token no navegador?** As opções de sempre são `localStorage`, cookie comum, cookie `HttpOnly` e memória do JavaScript. Para pesar cada uma, use os dois ataques que você já conhece:

- **XSS:** um script injetado roda na página e lê **tudo o que o JavaScript da página consegue ler**. Quem lê o `localStorage` ou um cookie comum (sem `HttpOnly`) é o XSS.
- **CSRF:** um site malicioso faz o navegador da vítima enviar uma requisição ao seu sistema. O atacante **não lê** o cookie: é o **navegador que envia o cookie sozinho**, em toda requisição para aquele site. A defesa vem do atributo `SameSite` do cookie (e de um token anti-CSRF).

💭 **Pare e reflita.** Para cada uma das quatro opções: um XSS consegue ler o token? O navegador o envia sozinho? O que some ao recarregar a página? Existe opção sem custo?

Não dá para impedir todo roubo, mas dá para **limitar o estrago**: expiração curta, HTTPS sempre, um **refresh token** que o servidor pode revogar e, em caso urgente, uma lista de revogação (aí o servidor volta a guardar um pouco de estado). O refresh token é a segunda metade da aula.

---

## 4. OAuth 2.0: acesso sem entregar a senha

> 🎬 **Imagine o cenário**
> Um aplicativo de impressão de fotos quer buscar as suas fotos num serviço de fotos na nuvem. O jeito mais antigo (e o pior): você digita, no app de impressão, o usuário e a senha da sua conta no serviço de fotos.

💭 **Pare e reflita.** O que dá errado nesse jeito? Pense no que o app passa a poder fazer, no que acontece se você quiser tirar o acesso dele e no que acontece se o app for invadido.

O **OAuth 2.0** é um protocolo de **autorização**: permite que um aplicativo obtenha **acesso limitado** à conta de um usuário num serviço HTTP **sem receber o usuário e a senha** dele. Em vez da senha, o usuário autoriza o app, e o serviço entrega ao app um **token de acesso**, com permissões limitadas e prazo de validade. O botão "Entrar com..." de muitos sites (o *login social*) é um uso que você já viu. O OAuth 2.0 **não é compatível** com o 1.0; quando alguém diz "OAuth", quase sempre é o 2.0.

Os papéis, com o mesmo exemplo das fotos:

| Termo | O que é | No exemplo |
|---|---|---|
| **Proprietário do recurso** (*resource owner*) | Quem é dono dos dados e pode autorizar o acesso | Você |
| **Cliente** (*client*) | O aplicativo que quer acessar os dados em nome do proprietário | O app de impressão |
| **Servidor de recursos** (*resource server*) | Onde os dados protegidos ficam; aceita requisições com token de acesso | A API de fotos |
| **Servidor de autorização** (*authorization server*) | Autentica o usuário, colhe o consentimento e **emite os tokens** | O login e a tela de consentimento do serviço de fotos |
| **Concessão de autorização** (*grant*) | A credencial que representa a autorização dada pelo proprietário | A prova de que você autorizou |
| **Token de acesso** (*access token*) | A credencial usada para acessar o recurso protegido | O que o app envia à API de fotos |
| **Escopo** (*scope*) | O que o token permite fazer | "ler fotos", mas não "apagar" |

O token de acesso **costuma ser um JWT**: tudo o que vimos na primeira metade vale para ele.

---

## 5. Os tipos de concessão (*grant types*) e qual usar hoje

*Grant type* é **o caminho pelo qual o cliente obtém o token**. A escolha depende do tipo de aplicação e de haver ou não um usuário.

**1. Código de autorização (*Authorization Code*).** O mais importante. O cliente **nunca vê a senha**:

```
1. Cliente redireciona o usuário ao Servidor de Autorização:
   GET /authorize?response_type=code&client_id=...&redirect_uri=...
                  &scope=...&state=...
2. O usuário faz login (no servidor de autorização, não no cliente)
   e consente com os escopos pedidos
3. Servidor de Autorização redireciona de volta ao cliente com um CÓDIGO
   (e o mesmo "state")
4. Cliente troca o código por tokens, em uma chamada direta
   servidor-a-servidor ao endpoint /token
5. Servidor de Autorização devolve o token de acesso (e, se houver, o refresh token)
6. Cliente chama o Servidor de Recursos:  Authorization: Bearer <token de acesso>
```

O token não passa pelo navegador: só o código, que tem vida curta e é de uso único. O `state` é um valor aleatório que o cliente confere na volta e que protege o fluxo contra **CSRF**.

**Com PKCE**, para quem não guarda segredo (app móvel, SPA). Antes de começar, o cliente gera um valor aleatório (*code verifier*) e envia ao servidor só o **hash** dele (*code challenge*). Na troca do código, envia o valor original e o servidor confere. Quem interceptar o código no caminho não consegue trocá-lo. Para clientes sem segredo o PKCE é obrigatório, e as recomendações atuais o pedem também para os demais.

**2. Credenciais do cliente (*Client Credentials*).** **Não há usuário**: é um sistema falando com outro. O cliente se autentica no servidor de autorização com o **seu próprio** `client_id` e `client_secret` e recebe um **token de acesso**, que passa a enviar ao servidor de recursos. Exemplos: um serviço de relatórios chamando outro serviço, um job agendado. Só serve para quem consegue **guardar um segredo** (um servidor, nunca um app de navegador).

> **Não confunda com autenticação básica (HTTP Basic).** Na autenticação básica, o cliente manda `usuário:senha` em Base64 **em toda requisição** (cabeçalho `Authorization: Basic ...`), o que só é aceitável com HTTPS. No *Client Credentials*, a credencial do cliente vai **uma vez** ao servidor de autorização, que devolve um token com prazo e escopo; as requisições seguintes levam o token, não a credencial.

**3. Atualização de token (*Refresh Token*).** O token de acesso vive pouco. Em vez de pedir login de novo, o cliente apresenta o **refresh token** e recebe um token de acesso novo. Ele é usado junto com outro grant e precisa ser **guardado com mais cuidado** que o de acesso.

**4. Implícito (*Implicit*)** e **5. Credenciais do proprietário (*Resource Owner Password Credentials*)** são **legado**. No implícito, o token volta **direto na URL** do redirecionamento (fica no histórico, em logs) e o cliente não se autentica. No de credenciais do proprietário, o usuário digita a senha **no próprio cliente**, o que anula a ideia do OAuth. As recomendações de segurança mais recentes do OAuth abandonaram os dois e fixaram o **código de autorização com PKCE** como padrão.

| Grant type | Há usuário? | Quando se usa | Situação hoje |
|---|---|---|---|
| Código de autorização | Sim | Apps web com backend, apps móveis, SPAs (com PKCE) | **Recomendado** |
| Credenciais do cliente | Não | Sistema a sistema | Recomendado para esse caso |
| Refresh token | Sim | Renovar o acesso | Recomendado, junto com outro grant |
| Implícito | Sim | SPAs antigas | Legado, evitar |
| Credenciais do proprietário | Sim | Clientes 100% confiáveis | Legado, evitar |

### Por que OAuth não autentica

- **O OAuth responde:** "este portador **pode acessar** tal recurso, com tais escopos?" (autorização).
- **O OAuth não responde:** "**quem é** o usuário, e ele está presente agora?" (autenticação).
- O token de acesso é feito para o **servidor de recursos**, não para o cliente interpretar. Cada provedor acabou inventando um jeito próprio de entregar os dados do perfil, e isso gerou implementações inseguras de "login com OAuth".

O token de acesso é como a **chave de um quarto de hotel**: abre a porta, mas não diz quem é o hóspede. Para autenticar existe uma camada padronizada por cima do OAuth, o **OpenID Connect**, assunto da próxima aula.

---

## 6. Access token e refresh token no `auth-jwt`

O `aula04` é um serviço de autenticação pequeno e completo: Spring Security, usuário e senha (BCrypt), H2, Flyway e **dois tokens JWT**. A chave **não** está no código: vem da variável `JWT_SECRET`, com **no mínimo 32 caracteres** (com menos, a aplicação nem sobe). Na pasta do projeto:

PowerShell:

```powershell
$env:JWT_SECRET = "troque-por-uma-chave-longa-com-32-ou-mais-caracteres"
mvn spring-boot:run
```

Git Bash:

```bash
export JWT_SECRET="troque-por-uma-chave-longa-com-32-ou-mais-caracteres"
mvn spring-boot:run
```

No IntelliJ: *Run > Edit Configurations > Environment variables* da configuração `AuthApplication`. Usuários de demonstração: `ana` / `Senha@12345` (USER) e `admin` / `Admin@12345` (ADMIN). Depois, abra o `http/auth.http` com o ambiente **dev**.

O fluxo, no README do projeto:

```
1. POST /auth/login            ->  { accessToken (15 min), refreshToken (7 dias) }
2. GET  /me   + Authorization: Bearer <accessToken>        ->  200
   ... 15 minutos depois ...
3. GET  /me   + accessToken expirado                        ->  401
4. POST /auth/refresh {refreshToken}  ->  NOVO { accessToken, refreshToken }   (o refresh antigo morre)
5. POST /auth/logout  {refreshToken}  ->  204  (não dá mais para renovar)
```

| | Access token | Refresh token |
|---|---|---|
| Vai em | **toda** requisição (`Authorization: Bearer`) | só em `/auth/refresh` e `/auth/logout` |
| Vida | 15 minutos | 7 dias |
| Claims | `sub`, `role`, `token_use=access`, `iss`, `iat`, `exp` | `sub`, `jti`, `token_use=refresh`, `iss`, `iat`, `exp` |
| No banco? | não | sim (só o `jti` e o estado), por isso pode ser **revogado** |

O `token_use` existe para um não se passar pelo outro: o verificador de cada tipo exige o valor certo (`TokenService.java`):

```java
private JWTVerifier verificador(String tipo) {
    // JWT.require(algoritmo): o algoritmo esperado é FIXADO aqui, no servidor. Um token que
    // declare "alg":"none" ou outro algoritmo no header é rejeitado (ataque da aula 03).
    return JWT.require(algoritmo)
            .withIssuer(props.issuer())          // confere o "iss"
            .withClaim(CLAIM_TIPO, tipo)         // confere se é access OU refresh, conforme o caso
            .build();                            // a expiração ("exp") é conferida automaticamente
}
```

É a mesma ordem do `JwtManual`, agora feita pela biblioteca: algoritmo fixado, assinatura, emissor, expiração.

Cada refresh token vale **uma vez só** (*rotação*): ao renovar, o usado é revogado e nasce um par novo. E se alguém apresentar um refresh token **já usado**? Trecho de `AuthService.renovar` (`auth/AuthService.java`):

```java
// 3) REUSO: o token já foi usado ou revogado, mas alguém o apresentou de novo.
if (registro.isRevogado()) {
    int revogados = refreshTokens.revogarTodosDoUsuario(registro.getUsuarioId());
    log.warn("Reuso de refresh token detectado (usuário id={}). {} token(s) revogado(s).",
            registro.getUsuarioId(), revogados);
    throw new TokenInvalidoException("Refresh token já utilizado");
}
```

✍️ **Experimente agora.** No `auth.http`, nesta ordem:

| Requisição | O que fazer | Resposta |
|---|---|---|
| **4** login da `ana` | Compare os dois tokens: `exp`, `role`, `jti`, `token_use` | 200 |
| **6** `/me` com o access token | O access token abre a porta | 200 |
| **7** `/me` sem token | Sem token não entra | 401 |
| **8** `/admin/painel` com a `ana` | Autenticada, mas sem o papel | 403 |
| **9** refresh | Volta um **par novo** | 200 |
| **10** reuso do refresh gasto | O token ainda não expirou, mas já foi usado | 401 |
| **9** de novo | Até o refresh **novo** deixou de valer | 401 |

Para recomeçar, faça o login (4) de novo. Se o jwt.io estiver bloqueado, o `/me` mostra o usuário e as autoridades do token, e o `POST /jwt/decodificar` do `aula03` abre qualquer JWT, inclusive estes, porque não confere a assinatura (os dois projetos usam a porta 8080: suba um de cada vez).

Dois limites, de propósito: **o logout não derruba o access token já emitido**, que vale até expirar (no máximo 15 min); e este projeto **não é um servidor OAuth completo** (não tem consentimento, código de autorização, escopos nem PKCE). Ele mostra o par access + refresh e a rotação, que é o que um servidor de autorização real também faz.

💭 **Pare e reflita.**
- Quem faz o papel de **servidor de autorização** aqui? E de **servidor de recursos**?
- O `/auth/login` recebe usuário e senha direto do cliente. Que grant da seção 5 isso lembra? Em que condição isso é aceitável?
- Por que o access token vive 15 minutos e o refresh token, 7 dias?
- Por que o servidor recusou, na requisição 10, um token que ainda não tinha expirado?

---

## Para o projeto

Hoje é dia de **check-in**: cada grupo mostra o modelo de `Chamado` e o que já roda (UC3 a UC6 ainda **sem** segurança, como prevê o cronograma da especificação, seção 12). O que a aula de hoje já resolve no projeto API de Chamados:

- **Token com expiração** (seção 04 da especificação, sugestão de até 1 hora). Lembre do token de 20 segundos: a validade é o que limita o estrago de um roubo.
- **Chave fora do código** (seções 04 e 11): variável de ambiente ou configuração externa, como o `JWT_SECRET` do `auth-jwt`. Boa prática, como no `auth-jwt`: sem valor padrão no repositório e, para HS256, com 32 caracteres ou mais.
- **Nenhum dado sensível no payload** (seção 04): qualquer um lê o payload. Nada de senha, CPF ou dado de cliente ali.
- **Cabeçalho do token** (seção 07): `access_token` ou `Authorization: Bearer`, ao menos um, declarado no README. O `JwtController.extrairToken` do `aula03` aceita os dois.
- **Mapa de ameaças** (seção 10): as linhas **Token roubado** ("o que um atacante com o token consegue fazer e por quanto tempo") e **Chave do JWT exposta** já podem ser escritas com o que você viu hoje.
- **Extra "Refresh token"** (seção 15): o `auth-jwt` é a referência, com rotação e detecção de reuso.

## Para praticar

- **Listas de exercícios de JWT e de OAuth**, no LMS.
- **Leitura:** o README do `auth-jwt`, seções "Decisões de segurança" e "Limites conhecidos".
- **Desafio opcional:** em `JwtManual.java`, descubra o que um atacante conseguiria se faltasse cada uma das três verificações do `validar`.
- **Pesquisa:** o que é PKCE e qual problema ele resolve. E escolha um app que você usa e que acessa dados de outro serviço em seu nome: quais escopos ele precisaria ter?

> **Na próxima aula:** como você entra uma única vez e acessa vários sistemas sem digitar a senha de novo? E como uma aplicação descobre, de forma padronizada, **quem** é o usuário? SSO e OpenID Connect.
