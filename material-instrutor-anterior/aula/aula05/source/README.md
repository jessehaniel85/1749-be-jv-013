# Aula 05: código de apoio (Single Sign-On)

Código usado na [Aula 05](../roteiroaula05.md): um **Single Sign-On** completo e local, **sem nenhum serviço externo** (nada de Google, GitHub ou Keycloak). São três processos que rodam na sua máquina:

| Processo | Papel no SSO | Endereço | Perfil |
|---|---|---|---|
| **Login único da escola** | Provedor de identidade (IdP): o único lugar com **senha** | `http://idp.localhost:9000` | `idp` |
| **Notas** | Aplicação (SP): não tem senha, confia no IdP | `http://notas.localhost:8081` | `notas` |
| **Biblioteca** | Aplicação (SP): não tem senha, confia no IdP | `http://biblioteca.localhost:8082` | `biblioteca` |

É o cenário da prática do roteiro: *"a escola tem dois sistemas, Notas e Biblioteca, e quer um login único"*.

## Ponto de partida

**Este é um projeto novo e independente** (`sso-escola`). Ele não continua a API de alunos: as aulas 07 e 08 seguem de `../../aula03/source`. É **um projeto só, rodando três vezes** com perfis diferentes; os pacotes mostram quem é quem:

- `com.ada.sso.idp`: só existe no provedor (login com senha, emissão do código, assinatura da asserção, logout único).
- `com.ada.sso.cliente`: só existe nas aplicações (redirecionar ao provedor, validar a asserção, sessão local).

As três não compartilham código de segurança, banco nem sessão. **Só se falam por HTTP**, como sistemas independentes.

## Como executar

Java 17+ e Maven 3.9+. Abra **três terminais** nesta pasta (`aula05/source`), um para cada processo:

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=idp"
```

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=notas"
```

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=biblioteca"
```

No IntelliJ: crie três configurações de execução da classe `SsoApplication`, cada uma com *Active profiles* `idp`, `notas` ou `biblioteca`.

Depois abra **http://notas.localhost:8081** no navegador.

> **Por que `*.localhost` e não `localhost:8081`?** Cookies são separados por **nome de host**, e **não por porta**. Com tudo em `localhost`, os três sistemas dividiriam os mesmos cookies e se atrapalhariam. Os navegadores (Chrome, Edge, Firefox) resolvem qualquer `algo.localhost` para a sua própria máquina, então cada sistema ganha o seu "site", como na vida real (`notas.escola.com`, `login.escola.com`). O Java e o `curl` no Windows podem **não** resolver esses nomes: por isso as chamadas servidor a servidor usam `localhost` (veja `idp-url-interna`).

### Usuários de demonstração (senha de todos: `Escola@2026`)

| Usuário | Papel | O que faz de especial |
|---|---|---|
| `ana` | ALUNO | Vê as próprias notas e os próprios empréstimos. |
| `carlos` | PROFESSOR | Pode **lançar notas** (nas Notas). Não gerencia o acervo. |
| `marta` | BIBLIOTECARIO | Pode **gerenciar o acervo** (na Biblioteca). Não lança notas. |

## Roteiro de demonstração (6 experimentos)

Use o navegador com o **DevTools aberto na aba Application > Cookies** e acompanhe também o log dos três terminais (cada passo imprime uma linha `[IdP]`, `[notas]`, `[biblioteca]`).

| # | Faça | O que acontece | O que mostrar |
|---|---|---|---|
| 1 | Abra `http://notas.localhost:8081/minhas-notas`. | Você é **redirecionado** ao provedor e vê a tela de login dele. | A barra de endereço: `idp.localhost:9000/sso/autorizar?app=notas&state=...` (passo 2 do fluxo). |
| 2 | Entre como `ana`. | Você **volta às Notas**, já logado, na página que pediu. | A URL passou rapidamente por `.../sso/callback?code=...&state=...` (passo 6). |
| 3 | Veja os **cookies**. | Cada sistema tem o seu: `IDP_SESSAO` em `idp.localhost` e `NOTAS_SESSAO` em `notas.localhost`. | Nenhum cookie guarda a senha ou o nome. São IDs aleatórios. |
| 4 | Abra `http://biblioteca.localhost:8082`. | Redireciona ao provedor e volta **logado, sem pedir senha**. | **O login único.** O provedor já tinha a sessão (`IDP_SESSAO`). |
| 5 | Entre como `carlos` (outra janela anônima) e abra `/lancar-notas` nas Notas, depois `/acervo/gerenciar` na Biblioteca. | Pode lançar notas, mas na Biblioteca recebe "sem permissão". | O provedor diz **quem** é; **cada sistema decide** o que pode. |
| 6 | Clique **"Sair só deste sistema"** nas Notas e reabra as Notas. | Você **entra de novo sozinho**. | A sessão do provedor continua viva. Sair de um sistema não é sair do SSO. |
| 7 | Clique **"Sair de todos (login único)"** e confirme. | A sessão do provedor termina e **as duas aplicações são avisadas**. Reabra as Notas **e** a Biblioteca: as duas pedem login. | Logs: `[IdP] logout único: aplicação 'notas' avisada...` e `[App] ... sessão(ões) encerrada(s)`. |
| 8 | Suba o provedor com um endereço de logout **errado** para a Biblioteca: `mvn spring-boot:run "-Dspring-boot.run.profiles=idp" "-Dspring-boot.run.arguments=--sso.idp.aplicacoes.biblioteca.url-logout=http://localhost:9999/x"`. Entre nas duas aplicações e faça o logout único. | As **Notas** pedem login de novo; a **Biblioteca continua logada**: ela não ouviu o aviso. | O risco do roteiro: *"o logout único é difícil de implementar bem"*. No log: `[IdP] logout único: não foi possível avisar a aplicação 'biblioteca'`. |

## O fluxo, passo a passo (os 6 passos do roteiro)

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

**No segundo sistema** (Biblioteca), os passos 3 e 4 **não acontecem**: o IdP encontra a sessão `IDP_SESSAO` e emite o código na hora. O usuário vê só os redirecionamentos.

### O que é o quê (tradução entre o roteiro e o código)

| Conceito do roteiro | No código |
|---|---|
| Provedor de identidade (IdP) | Perfil `idp`: `IdpSecurityConfig`, `AutorizarController`, `TokenController` |
| Aplicação / Service Provider | Perfis `notas` e `biblioteca`: `ClienteSecurityConfig`, `CallbackController` |
| **Relação de confiança** | O cadastro das aplicações no IdP (`application-idp.yml`: retorno, segredo) **e** o endereço do IdP + a chave pública na aplicação (`application-notas.yml`, `VerificadorDeAsercao`) |
| Comprovação de autenticação (asserção) | Um JWT assinado com RSA: `AsercaoService.gerarAsercao` |
| Sessão do provedor | Cookie `IDP_SESSAO` |
| Sessão local | Cookies `NOTAS_SESSAO` e `BIBLIOTECA_SESSAO` |
| Single logout | `NotificadorDeLogout` (IdP) + `LogoutBackchannelController` (aplicação) |

### Preparando a aula 06 (os nomes do OpenID Connect)

O que está aqui é a **mesma estrutura** do OIDC, com nomes próprios. Na aula 06, mostre a tabela e diga: *"vocês já construíram isto, agora com os nomes padronizados"*.

| Neste projeto | No OpenID Connect |
|---|---|
| `/sso/autorizar?app=...&state=...` | `/authorize?client_id=...&state=...&scope=openid` |
| `code` na URL de retorno | `code` (fluxo de código de autorização) |
| `POST /sso/token` com `id:segredo` | `/token` com `client_id` e `client_secret` |
| `assercao` (JWT RS256 com `sub`, `nome`, `email`, `aud`, `iss`) | **ID Token** |
| `GET /sso/chave-publica` | `jwks_uri` (JWKS) |
| token de logout (`uso=logout`) | Back-Channel Logout (`logout_token`) |

Ficaram de fora, de propósito: `nonce`, `scope`, descoberta automática (`.well-known/openid-configuration`), PKCE e `userinfo`.

## Passo a passo para implementar com a turma

Cada passo termina com algo que dá para **ver funcionando**. Os arquivos estão comentados para servir de guia.

| Passo | O que fazer | Onde está | O que ver |
|---|---|---|---|
| 1 | Projeto com web, security, thymeleaf e java-jwt; os três perfis e as portas. | `pom.xml`, `application*.yml` | Os três sobem. |
| 2 | **Provedor:** usuários com BCrypt e tela de login (formLogin). | `UsuarioIdpService`, `IdpSecurityConfig`, `login.html` | Entrar em `http://idp.localhost:9000` e ver a conta. |
| 3 | **Provedor:** cadastrar as aplicações (a relação de confiança) e emitir o **código** de uso único. | `IdpProperties`, `CodigoService`, `AutorizarController` | `/sso/autorizar?app=notas&state=abcdefgh1234` logado: redireciona com `code`. |
| 4 | **Provedor:** o par de chaves RSA, a asserção assinada e o `/sso/token`. | `ChavesIdp`, `AsercaoService`, `TokenController`, `ChavePublicaController` | Trocar o código e colar a asserção no jwt.io. |
| 5 | **Aplicação:** mandar quem não tem sessão para o provedor, com o `state`. | `IniciaLoginSso`, `ClienteSecurityConfig` | Abrir as Notas e ser redirecionado. |
| 6 | **Aplicação:** o callback: conferir o state, trocar o código, **verificar a asserção**, criar a sessão local. | `CallbackController`, `HttpIdpClient`, `VerificadorDeAsercao` | Entrar e voltar logado. |
| 7 | **Segundo sistema:** subir a Biblioteca (mesmo código, outro perfil) e ver o login único. | `application-biblioteca.yml` | Entrar sem digitar a senha. |
| 8 | **Permissões locais** por papel. | `@PreAuthorize` em `NotasController` e `BibliotecaController` | `carlos` lança notas; `ana` recebe "sem permissão". |
| 9 | **Logout:** local, depois o único (aviso entre servidores). | `NotificadorDeLogout`, `RegistroDeSessoes`, `LogoutBackchannelController` | O experimento 7 do roteiro de demonstração. |

## Decisões de segurança (o "porquê" para discutir)

| Decisão | Por quê |
|---|---|
| **Só o provedor tem senha** (BCrypt). As aplicações nunca a veem. | É a diferença entre SSO e "mesma senha em todo lugar". Um vazamento de uma aplicação não vaza senhas. |
| Volta com um **código** (60 s, uso único), e não com a asserção na URL. | A URL fica em histórico, logs e `Referer`. A asserção real viaja só no canal servidor a servidor. |
| A aplicação **prova quem é** ao trocar o código (`id:segredo`, HTTP Basic). | Quem interceptar o código sozinho não consegue trocá-lo. |
| O endereço de retorno vem do **cadastro**, não da requisição. | Evita *open redirect*: o atacante não manda o código para o site dele. |
| **`state`** aleatório conferido na volta (uso único). | Barra o CSRF no login (aula 01 e 04): ninguém "entra" com um código alheio. |
| Asserção **assinada com RSA**; a aplicação só tem a chave **pública**. | Só o provedor assina; quem valida não consegue forjar (aula 02). Por isso RS256 e não HS256. |
| Confere **`iss`**, **`aud`**, `exp` e `uso`; o algoritmo é fixado. | `aud` impede usar a asserção da Biblioteca nas Notas; `uso` impede confundir login com logout; fixar o algoritmo barra `alg=none` (aula 03). |
| `changeSessionId()` ao criar a sessão local. | Evita *session fixation*. |
| Cookie de sessão `HttpOnly` e **`SameSite=Lax`**. | `Strict` quebraria o fluxo (o cookie não acompanharia a volta do provedor). Veja aula 02. |
| Logout por **POST com CSRF**, com tela de confirmação. | Se bastasse abrir uma URL, qualquer site deslogaria o usuário com um `<img>`. |
| O aviso de logout é um **token assinado**. | Sem isso, qualquer um derrubaria a sessão de qualquer usuário. |
| Segredo e chaves **não vão no repositório**: os valores padrão são só da demonstração. | Em produção, `SSO_SEGREDO_NOTAS` e `SSO_SEGREDO_BIBLIOTECA` vêm do ambiente. |

## Limites conhecidos (de propósito)

- **Tudo em HTTP e na memória.** Em produção: **HTTPS** nos três (aula 02), cookies `Secure`, e sessões/códigos em um armazenamento compartilhado (Redis) se houver mais de um servidor.
- **A chave RSA é gerada a cada subida do provedor.** Se ele reinicia, as aplicações percebem a assinatura inválida e buscam a chave nova (no máximo a cada 10 s). Em produção a chave fica em um cofre e é rotacionada com um período de transição.
- **A chave pública é confiada pelo endereço.** Quem controla `idp-url-interna` controla em quem a aplicação confia; por isso, em produção, esse endereço é HTTPS.
- **O logout único não tenta de novo.** Se a aplicação está fora do ar, perde o aviso e a sessão local sobrevive até expirar (30 min). É o risco do experimento 8.
- **Sem bloqueio de tentativas de login.** Como o provedor concentra o risco, é nele que entram o rate limit (aula 01), a **autenticação multifator** e o monitoramento.
- **O provedor é um ponto único de falha.** Com ele fora do ar, ninguém **novo** entra; quem já tem sessão local continua (experimente parar o provedor).
- **Não é OpenID Connect nem SAML.** É a mesma ideia com formato próprio; a aula 06 mostra o padrão.

## Exercícios para ir além

1. **Terceira aplicação:** cadastre a "Secretaria" (porta 8083) no `application-idp.yml` e crie o `application-secretaria.yml`. Quantas linhas de código precisou mudar?
2. **Desativar um usuário** no provedor: o que acontece com quem já tem sessão local? E o que mudaria se a asserção tivesse validade curta e a sessão local fosse renovada contra o provedor?
3. **Atacante no meio:** troque o `state` na URL de retorno, ou use um código já usado, e veja a página de erro e o log.
4. **`aud` na prática:** altere o `id` da Biblioteca para `notas` no `application-biblioteca.yml` (e o segredo igual) e veja o que o provedor e a aplicação respondem.
5. **Multifator:** acrescente no provedor uma segunda etapa (um código fixo, por exemplo) depois da senha. As aplicações precisam mudar? (Não: esse é o ganho de concentrar a autenticação.)
6. **Reinício do provedor:** reinicie só o `idp` e tente entrar. Por que funciona sem reiniciar as aplicações?

## Testes automatizados

```bash
mvn test
```

> **Atenção:** se o seu `~/.m2/settings.xml` define `maven.test.skip=true`, o `mvn test` pula os testes. Use `mvn "-Dmaven.test.skip=false" test` (com as aspas no PowerShell).

São **52 testes**, todos sem abrir porta (`MockMvc` e um servidor HTTP de mentira):

- `IdpTest` (20): login e senha errada, CSRF, sessão única com **duas aplicações**, o código (uso único, aplicação certa, segredo), open redirect, state inválido e logout.
- `NotasTest` (21) e `BibliotecaTest` (5): redirecionamento ao provedor, callback e state, asserção forjada (outra chave, `alg=none`, outra aplicação, token de logout), permissões por papel e logout (local e único). Usam o `IdpEmProcesso`, que chama as classes **reais** do provedor na memória, então o contrato entre os dois lados (nomes dos claims, assinatura, `aud`) é verificado de verdade.
- `ChamadasHttpTest` (6): o que cada lado envia nas chamadas servidor a servidor (Basic, corpo, endereços), incluindo o aviso de logout e a aplicação fora do ar.

> **O que os testes não cobrem:** a execução dos três processos juntos, em um navegador de verdade, e a resolução de `*.localhost`. Rode o roteiro de demonstração uma vez antes da aula.

## Próxima aula

A **Aula 06** (OpenID Connect) não tem código: use a tabela "Preparando a aula 06" acima. A **Aula 07** continua do projeto da API de alunos (`../../aula03/source`).
