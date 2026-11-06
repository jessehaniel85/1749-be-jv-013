# Material do Aluno — Aula 5: Explicar um login único (SSO) e reconhecer um login com OpenID Connect

> **Segunda, 09/11/2026 · Tempo de leitura:** ~15 min. Na aula passada, o OAuth 2.0 resolveu o acesso **sem entregar a senha**, mas deixou uma pergunta sem resposta: **quem é** o usuário? Hoje você vê duas peças que respondem a ela. O **Single Sign-On (SSO)**: entrar uma vez e acessar vários sistemas, rodando na sua máquina com um provedor de identidade e duas aplicações. E o **OpenID Connect (OIDC)**, a camada de autenticação padronizada sobre o OAuth, com o seu **ID Token**. No fim, um quadro que põe JWT, OAuth, SSO e OIDC no lugar de cada um.
>
> **Código:** `aula05` (projeto `sso-escola`), publicado no LMS. Para decodificar tokens sem site externo, o `aula03` continua útil.
>
> As três pausas de sempre: 🎬 **Imagine o cenário**, 💭 **Pare e reflita** e ✍️ **Experimente agora**.

---

## 0. Retomada: qual grant eu uso?

💭 **Pare e reflita.** Para cada cenário, qual tipo de concessão do OAuth você escolheria, e por quê? Se descartar alguma opção, qual risco ela traria?

1. Um site com backend em Java quer ler as fotos do usuário num serviço de fotos de outra empresa.
2. Um aplicativo móvel precisa acessar a API de um banco em nome do cliente.
3. O serviço de relatórios chama, de madrugada, a API de pedidos. Não há usuário.
4. O token de acesso do usuário expira a cada 15 minutos, e ele não deve precisar logar de novo.
5. Um colega propõe: "a SPA recebe o usuário e a senha e manda para a API de autorização, é mais simples".

---

## 1. O que é SSO (e o que ele não é)

> 🎬 **Imagine o cenário**
> Uma empresa tem 20 sistemas internos: e-mail, folha de pagamento, repositório de código, wiki, sistema de chamados. Cada um tem o seu login e a sua senha. Na segunda-feira, metade dos chamados abertos no suporte é "esqueci a senha". Quando alguém é desligado, a área de TI precisa lembrar de bloquear o acesso em 20 lugares.

💭 **Pare e reflita.** Quantas senhas você tem hoje? O que acontece com a segurança quando ninguém consegue lembrar de tantas?

**Single Sign-On (SSO)**, ou **login único**, é uma forma de autenticação que permite ao usuário acessar com segurança vários aplicativos e serviços com **um único conjunto de credenciais**. Você entra na sua conta de um grande provedor e já está autenticado no e-mail, no armazenamento de arquivos e nos vídeos do mesmo provedor.

**SSO não é "usar a mesma senha em todo lugar".** Com senha repetida, você digita a senha em cada sistema e cada um a guarda. No SSO você digita **uma vez**, em **um lugar só**, e os demais sistemas **nunca veem a sua senha**.

### Identidade federada

O SSO se apoia na **identidade federada**: compartilhar atributos de identidade entre sistemas **confiáveis, mas autônomos**. Os sistemas "terceirizam" a pergunta *"quem é você?"* para um provedor em que todos confiam. Cada sistema continua independente, com as suas regras, os seus dados e as suas permissões, mas aceita a resposta desse provedor.

### Os componentes

| Componente | O que é | Exemplo |
|---|---|---|
| **Provedor de identidade** (*Identity Provider*, IdP) | Onde o usuário se autentica de fato; guarda as credenciais e responde "esta pessoa é fulano" | Microsoft Entra ID, Keycloak, Okta |
| **Aplicação** (*Service Provider*, SP, ou *relying party*) | O sistema que o usuário quer acessar e que **confia** no provedor | O e-mail, o sistema de notas |
| **Relação de confiança** | O acordo (chaves, certificados e endereços configurados de antemão) que faz a aplicação aceitar a resposta do provedor | Configurada entre os dois antes do primeiro login |

### O fluxo

```
1. O usuário solicita um recurso da aplicação
2. A aplicação percebe que ele não está autenticado e
   o REDIRECIONA ao provedor de identidade
3. O usuário informa as credenciais (no provedor, não na aplicação)
4. O provedor valida as credenciais
5. O provedor devolve ao usuário uma comprovação de autenticação
   (um token ou uma asserção)
6. A aplicação recebe e VERIFICA a comprovação e o login é concluído
```

**O ganho vem no segundo sistema.** A outra aplicação também redireciona ao provedor, mas o provedor **já tem uma sessão ativa** para aquele usuário: devolve a comprovação sem pedir a senha. Os redirecionamentos acontecem; o usuário só não digita nada.

```
Usuário -> App A -> (redireciona) -> IdP: pede login   -> volta ao App A, logado
Usuário -> App B -> (redireciona) -> IdP: já tem sessão -> volta ao App B, logado (sem digitar nada)
```

Repare como esse fluxo lembra o **código de autorização** da aula passada. A diferença: o OAuth entrega um **token de acesso**; o SSO precisa de uma comprovação de **quem** é o usuário.

---

## 2. O SSO rodando na sua máquina: `sso-escola`

O `aula05` é **um projeto só, rodando três vezes**, com perfis diferentes. Nada de serviço externo:

| Processo | Papel no SSO | Endereço | Perfil |
|---|---|---|---|
| **Login único da escola** | Provedor de identidade: o único lugar com **senha** | `http://idp.localhost:9000` | `idp` |
| **Notas** | Aplicação: não tem senha, confia no provedor | `http://notas.localhost:8081` | `notas` |
| **Biblioteca** | Aplicação: não tem senha, confia no provedor | `http://biblioteca.localhost:8082` | `biblioteca` |

Abra **três terminais** na pasta do projeto, um para cada processo (comandos do README):

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=idp"
```

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=notas"
```

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=biblioteca"
```

No IntelliJ: três configurações de execução da classe `SsoApplication`, com *Active profiles* `idp`, `notas` e `biblioteca`. Usuários (senha de todos: `Escola@2026`): `ana` (ALUNO), `carlos` (PROFESSOR, lança notas) e `marta` (BIBLIOTECARIO, gerencia o acervo).

> **Por que `notas.localhost` e não `localhost:8081`?** Cookies são separados por **nome de host**, não por porta. Com tudo em `localhost`, os três sistemas dividiriam os mesmos cookies. Os navegadores resolvem qualquer `algo.localhost` para a sua própria máquina, então cada sistema ganha o seu "site". Abrir `localhost:8081` é o erro mais comum.

O fluxo de verdade, como está no README do projeto:

```
Navegador              Notas (SP)                    Login único (IdP)
    |  1. GET /minhas-notas  |                              |
    |----------------------->|                              |
    |  2. 302 -> IdP/sso/autorizar?app=notas&state=XYZ      |   (a aplicação guarda o state na sessão)
    |<-----------------------|                              |
    |  GET /sso/autorizar?app=notas&state=XYZ               |
    |------------------------------------------------------>|
    |  3. sem sessão no IdP: tela de login. O usuário digita a senha AQUI
    |  4. o IdP valida a senha (BCrypt) e cria a sessão IDP_SESSAO
    |  5. 302 -> notas/sso/callback?code=ABC&state=XYZ      |   (código de uso único, 60 s)
    |<------------------------------------------------------|
    |  GET /sso/callback?code=ABC&state=XYZ                 |
    |----------------------->|                              |
    |                        |  6a. confere o state         |
    |                        |  POST /sso/token (code + id:segredo)   <- servidor a servidor
    |                        |----------------------------->|
    |                        |<-----------------------------|   {"assercao": "<JWT RS256>"}
    |                        |  6b. verifica assinatura, iss, aud, exp
    |                        |  6c. cria a SESSÃO LOCAL (NOTAS_SESSAO)
    |  302 -> /minhas-notas  |                              |
    |<-----------------------|                              |
```

✍️ **Experimente agora.** Deixe o DevTools do navegador aberto em *Application > Cookies* e os três terminais à vista (cada passo imprime uma linha `[IdP]`, `[notas]` ou `[biblioteca]`). Antes de começar, aposte: em qual das duas aplicações a senha será pedida?

| # | Faça | Observe |
|---|---|---|
| 1 | Abra `http://notas.localhost:8081/minhas-notas` | A barra de endereço. Onde está o login das Notas? |
| 2 | Entre como `ana` | Onde a senha foi digitada? Por qual URL você voltou? |
| 3 | Veja os cookies | Quantos cookies de sessão existem e de quem é cada um? |
| 4 | Abra `http://biblioteca.localhost:8082` | A senha foi pedida? |
| 5 | Numa janela anônima, entre como `carlos`; abra `/lancar-notas` nas Notas e `/acervo/gerenciar` na Biblioteca | O que cada aplicação deixou o Carlos fazer? |
| 6 | Clique em **"Sair só deste sistema"** nas Notas e abra as Notas de novo | Por que aconteceu o que aconteceu? |
| 7 | Clique em **"Sair de todos (login único)"**, confirme e abra Notas e Biblioteca | O que os logs mostram? |

### A comprovação: um JWT assinado com RSA

A "asserção" que o provedor entrega é um JWT. O provedor assina com a chave **privada** (`idp/AsercaoService.java`):

```java
public String gerarAsercao(Codigo codigo) {
    Instant agora = Instant.now();
    return JWT.create()
            .withIssuer(props.emissor())                     // iss: quem emitiu (o provedor)
            .withSubject(codigo.username())                  // sub: quem é o usuário
            .withAudience(codigo.appId())                    // aud: para QUAL aplicação (só ela aceita)
            .withJWTId(UUID.randomUUID().toString())         // jti: id único desta asserção
            .withClaim("uso", "login")
            .withClaim("nome", codigo.nome())                // atributos de identidade
            .withClaim("email", codigo.email())
            .withClaim("papel", codigo.papel())
            .withIssuedAt(agora)
            .withExpiresAt(agora.plusSeconds(VALIDADE_ASERCAO_SEGUNDOS))
            .sign(algoritmo);
}
```

E cada aplicação confere, só com a chave **pública** do provedor (`cliente/VerificadorDeAsercao.java`):

```java
private JWTVerifier montar(RSAPublicKey chavePublica, String uso) {
    // O algoritmo é FIXADO aqui (RS256). Um token que declare "alg: none" ou HS256 no header
    // é rejeitado (os ataques da aula 03).
    return JWT.require(Algorithm.RSA256(chavePublica, null))
            .withIssuer(props.idpUrlPublica())
            .withAudience(props.id())
            .withClaim("uso", uso)
            .build();
}
```

É o **RS256** da aula passada em ação: só o provedor assina; as aplicações validam e **não conseguem forjar**. O `aud` impede que uma asserção emitida para a Biblioteca seja aceita pelas Notas. E o resto do desenho usa peças que você já conhece: o **código** de uso único e o `state` (OAuth), o cookie de sessão `HttpOnly` com `SameSite=Lax` e o algoritmo fixado no servidor.

💭 **Pare e reflita.**
- O provedor diz **quem** é o Carlos. Quem decide o que ele **pode** fazer?
- Por que o provedor devolve um **código** na URL, e não a asserção direto?
- Se o provedor cair agora, quem consegue entrar? E quem já está logado continua?

**O logout único é difícil.** No experimento 8 do README, o provedor sobe com um endereço de logout **errado** para a Biblioteca. Faça o logout único: as Notas pedem login de novo, mas a Biblioteca **continua logada**, porque não ouviu o aviso (`[IdP] logout único: não foi possível avisar a aplicação 'biblioteca'`).

💭 **Pare e reflita.** Liste as vantagens do SSO para o usuário e para a empresa. Agora liste os riscos: o que acontece quando **um** lugar concentra a autenticação de todos os sistemas? Onde vale investir proteção extra?

---

## 3. Os protocolos: SAML 2.0, OAuth e OpenID Connect

SSO é o **objetivo** (a experiência de login único). Para chegar nele, existem **protocolos**:

- **SAML 2.0** (*Security Assertion Markup Language*): troca as informações de autenticação em **asserções XML** assinadas. Muito usado em ambientes corporativos e em sistemas mais antigos.
- **OAuth 2.0:** autoriza acesso, **não** autentica. Aparece como base do próximo.
- **OpenID Connect (OIDC):** uma **camada fina sobre o OAuth 2.0** que acrescenta a autenticação. Baseado em JSON e JWT, usado em aplicações web e móveis.

| | SAML 2.0 | OpenID Connect |
|---|---|---|
| Formato das mensagens | XML | JSON / JWT |
| Cenário típico | Sistemas corporativos, integrações legadas | Aplicações web, SPAs e apps móveis |
| Base | Próprio | Camada sobre OAuth 2.0 |

E há os **produtos**, que implementam esses protocolos: Microsoft Entra ID, Okta, Keycloak (código aberto). Na prática, ninguém constrói um provedor de identidade do zero: as aplicações se integram a um existente, e o Spring Security sabe atuar como cliente deles. Quando a conversa misturar os três, pergunte: isso é o **objetivo**, o **protocolo** ou o **produto**?

---

## 4. OpenID Connect: quem é o usuário

O **OpenID Connect** permite que os clientes solicitem e recebam informações sobre as **sessões autenticadas** e os **usuários finais**, num formato padronizado. Antes dele existiram o OpenID 1.0 e o 2.0; o usado hoje é o OpenID Connect.

| OAuth 2.0 | OpenID Connect |
|---|---|
| Entrega o **token de acesso** | Entrega o token de acesso **e** o **ID Token** |
| "Pode acessar tal recurso?" | "**Quem** é o usuário, e ele se autenticou?" |
| Escopos como `read:fotos` | Escopo especial **`openid`** (e `profile`, `email`) |
| Cada provedor entrega o perfil do seu jeito | Formato **padronizado** das informações do usuário |

**Participantes**, os mesmos papéis do OAuth com nomes voltados à identidade: o **usuário final** (a pessoa cuja identidade é pedida), o **cliente** ou *relying party* (o app que precisa saber quem ele é), o **servidor de autorização** ou *OpenID Provider* (autentica e fornece a identidade: é o provedor de identidade da seção 1) e o **provedor de recursos** (disponibiliza as informações do usuário, dependendo do servidor de autorização).

**Os termos que você vai encontrar:**

- **ID Token:** um **JWT** assinado pelo provedor que **prova que o usuário foi autenticado** e traz dados de identidade. É a grande novidade do OIDC.
- **Escopo `openid`:** obrigatório; é ele que faz o fluxo ser OIDC. `profile` e `email` pedem nome, e-mail etc.
- **Endpoint UserInfo:** devolve os dados do usuário quando chamado com o token de acesso.
- **Descoberta (*discovery*):** o provedor publica as suas configurações (endereços, chaves públicas) em `/.well-known/openid-configuration`, para o cliente se configurar sozinho.

### Dentro de um ID Token

Payload decodificado de um ID Token de exemplo:

```json
{
  "iss": "https://accounts.exemplo.com",
  "sub": "248289761001",
  "aud": "meu-client-id",
  "exp": 1762876800,
  "iat": 1762873200,
  "nonce": "n-0S6_WzA2Mj",
  "name": "Ana Souza",
  "email": "ana@exemplo.com"
}
```

- `iss`: quem emitiu. `sub`: identificador **único e estável** do usuário naquele provedor. `aud`: para quem o token foi emitido (o `client_id`). `exp` e `iat`: validade.
- `nonce`: valor aleatório que o cliente envia no pedido e que volta no token; protege contra reaproveitamento (*replay*).
- `name`, `email`: aparecem conforme os escopos pedidos.

Antes de confiar, o cliente **valida** o ID Token: assinatura (com a chave pública que o provedor publica), `iss`, `aud` igual ao seu `client_id`, `exp` e `nonce`. É a validação de JWT que você fez à mão no `JwtManual`. E uma divisão que evita confusão: o **ID Token é para o cliente ler**; o **token de acesso é para a API**, não para o cliente interpretar.

✍️ **Experimente agora.** Em aula, você recebe um ID Token de exemplo, gerado para a aula (nunca use um de produção). Decodifique-o e identifique o emissor, o destinatário, o `sub`, a expiração (`new Date(exp * 1000)` no console do navegador), o `nonce` e os dados pessoais. Sem acesso ao jwt.io: suba o `aula03` e use a requisição **3.1** (`POST /jwt/decodificar`), que abre qualquer JWT sem conferir a assinatura, ou o `base64 -d` na segunda parte do token.

💭 **Pare e reflita.** Compare com o JWT do `aula03`. O que é **igual** e o que é **novo**? Na tela de login de uma loja virtual com "Entrar com rede social", quem é cada participante? O que a loja recebe do provedor, e o que ela **não** deveria receber?

### Os fluxos

**Código de autorização**, o fluxo da aula passada, agora com o ID Token na resposta:

```
1. Usuário clica em "Entrar" no aplicativo
2. O aplicativo REDIRECIONA ao provedor de identidade
   (pedindo scope=openid profile email)
3. O provedor mostra o formulário de login; o usuário informa as credenciais
4. Após validar, o provedor REDIRECIONA de volta ao aplicativo
   com um CÓDIGO DE AUTORIZAÇÃO de uso único
5. O aplicativo troca o código, em uma chamada direta servidor-a-servidor,
   por um ID TOKEN e um TOKEN DE ACESSO (o código se invalida)
6. O aplicativo valida o ID Token e sabe quem é o usuário
   (e usa o token de acesso para pedir recursos, se precisar)
```

Os tokens não passam pelo navegador, só o código. É o fluxo **recomendado**, com **PKCE** para apps móveis e SPAs.

**Implícito.** Tem muitas etapas em comum com o anterior, mas o token volta **direto no redirecionamento**, na URL, e o cliente não se autentica ao recebê-lo. Era usado por SPAs antes do PKCE. Hoje é **legado**, como no OAuth.

| | Código de autorização | Implícito |
|---|---|---|
| Como o token chega | Troca do código, servidor a servidor | Direto na URL do redirecionamento |
| Exposição do token | Não passa pelo navegador | Passa pela barra de endereço |
| Situação hoje | **Recomendado** | Legado, evitar |

💭 **Pare e reflita.** Se o token chega na URL, quem mais, além do aplicativo, pode vê-lo?

### Você já viu um OIDC por dentro

O `sso-escola` tem a **mesma estrutura** do OpenID Connect, com nomes próprios (tabela do README):

| Neste projeto | No OpenID Connect |
|---|---|
| `/sso/autorizar?app=...&state=...` | `/authorize?client_id=...&state=...&scope=openid` |
| `code` na URL de retorno | `code` (fluxo de código de autorização) |
| `POST /sso/token` com `id:segredo` | `/token` com `client_id` e `client_secret` |
| `assercao` (JWT RS256 com `sub`, `nome`, `email`, `aud`, `iss`) | **ID Token** |
| `GET /sso/chave-publica` | `jwks_uri` (JWKS) |
| token de logout (`uso=logout`) | Back-Channel Logout (`logout_token`) |

Ficaram de fora, de propósito: `nonce`, `scope`, descoberta automática, PKCE e `userinfo`.

---

## 5. Como tudo se conecta

| Conceito | O que é | Responde a |
|---|---|---|
| **JWT** | Um **formato** de token (assinado, com claims) | "Em que formato viaja a informação?" |
| **OAuth 2.0** | Protocolo de **autorização** delegada | "Este portador pode acessar o quê?" |
| **SSO** | O **objetivo** de entrar uma vez e acessar vários sistemas | "Como o usuário evita logins repetidos?" |
| **SAML 2.0** | Protocolo de SSO com asserções **XML** | "Como provar a autenticação (mundo corporativo)?" |
| **OpenID Connect** | Camada de **autenticação** sobre o OAuth 2.0, com ID Token em **JWT** | "**Quem** é o usuário?" |

> O **OAuth** autoriza, o **OpenID Connect** autentica sobre ele, o **JWT** é o formato dos tokens e o **SSO** é o que o usuário sente quando tudo funciona.

---

## Para o projeto

Meta de hoje, pelo cronograma da especificação (seção 12): **endpoints de chamados funcionando** (ainda sem segurança) e **extras escolhidos** (seção 15), combinados com o instrutor. O que a aula de hoje leva para a API de Chamados:

- **A identidade vem da autenticação, nunca do corpo** (seção 11). Assim como as Notas não perguntam ao usuário quem ele é, a sua API não aceita um `usuarioId` enviado no JSON: o dono do chamado sai do token. É a base da regra de propriedade (404 para chamado de outro usuário).
- **Token com expiração, chave fora do código e nenhum dado sensível no payload** (seção 04) continuam valendo: o ID Token mostra o que um payload pode carregar (identificadores, nome) e o que ele nunca carrega (senha).
- **Mapa de ameaças** (seção 10): o critério "Decisões de segurança" da rubrica (seção 14) cobre JWT, OAuth, SSO e OIDC. Ao justificar a linha **Token roubado**, use o vocabulário de hoje: quem emite, quem valida, para quem o token foi emitido.
- **Extra "Comparação documentada"** (seção 15): comparar a solução própria com um servidor de identidade externo (OAuth 2.0/OpenID Connect) e dizer quando valeria a pena trocar. O quadro da seção 5 e a tabela do `sso-escola` são o ponto de partida.

## Para praticar

- **Listas de exercícios de Single Sign-On e de OpenID Connect**, no LMS.
- **Leitura:** o README do `sso-escola`, seções "Decisões de segurança" e "Limites conhecidos".
- **Pesquisa:** a diferença entre SAML 2.0 e OpenID Connect (formato das mensagens e cenários de uso). E escolha um sistema que você usa com SSO: quem é o provedor de identidade e quem são as aplicações?
- **Laboratório opcional:** os exercícios do fim do README do `sso-escola`, como cadastrar uma terceira aplicação, a "Secretaria", ou acrescentar uma segunda etapa de autenticação no provedor e ver se as aplicações precisam mudar.

> **Na próxima aula:** a sua busca por título está segura só porque você usa JPA? E o que a resposta de erro da sua API conta a quem a chama? SQL injection e tratamento de exceções.
