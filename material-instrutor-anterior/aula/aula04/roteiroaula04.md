# Aula 04: OAuth 2.0

**Roteiro de Aula · Módulo Fundamentos de segurança para aplicações (BE-JV-013)**

OAuth 2.0 como protocolo de **autorização**. A turma aprende os papéis envolvidos, as terminologias, os cinco tipos de concessão (*grant types*), quais deles ainda se recomenda usar e por que OAuth, sozinho, não serve para autenticar.

**Data:** 06/11/2026 · sexta &nbsp;·&nbsp; **Duração:** 3h &nbsp;·&nbsp; **Formato:** expositiva + dinâmica de cenários + prática guiada

**Código:** [`source/`](source/README.md), um projeto **novo e independente** (`auth-jwt`): Spring Security com JWT, **access token + refresh token**, H2 e Flyway. Ele não substitui o projeto da API de alunos: as aulas 07 e 08 continuam de `../aula03/source`.

> **Problema gerador:** OAuth: como permitir que um aplicativo acesse seus dados em outro serviço sem que você entregue sua senha a ele?

---

## Sumário

1. [Visão geral](#01--visão-geral)
2. [Retomada da aula 03](#02--retomada-da-aula-03)
3. [O problema e o OAuth 2.0](#03--o-problema-e-o-oauth-20)
4. [Terminologias](#04--terminologias)
5. [Grant types](#05--grant-types)
6. [Qual grant usar hoje e OAuth x autenticação](#06--qual-grant-usar-hoje-e-oauth-x-autenticação)
7. [Prática guiada](#07--prática-guiada)
8. [Encerramento](#08--encerramento)
9. [Dicas de condução](#09--dicas-de-condução)
10. [Código de apoio e preparação da demonstração](#10--código-de-apoio-e-preparação-da-demonstração)

---

## 01 · Visão geral

Tempos relativos ao início da aula, ajuste ao seu horário real. É uma aula conceitual e cheia de siglas: se a turma se perder, volte sempre ao exemplo do app de impressão de fotos e peça que ela aponte *quem é quem* nele. Proteja o bloco de dinâmica (seção 07), é onde o conteúdo se fixa.

**Antes de começar:** a Parte 1 da prática (02:00) usa a aplicação rodando. Faça a **preparação da seção 10** (~10 min) antes de a turma chegar.

| Hora | Duração | Bloco |
|---|---|---|
| 00:00 | 10 min | **Retomada** da aula 03 e da atividade de casa |
| 00:10 | 30 min | **Teoria**: o problema, OAuth 2.0 e terminologias |
| 00:40 | 45 min | **Teoria**: grant types (os cinco fluxos) |
| 01:25 | 10 min | Intervalo |
| 01:35 | 25 min | **Teoria**: qual grant usar hoje e por que OAuth não autentica |
| 02:00 | 40 min | **Prática guiada**: access e refresh token no código, dinâmica de cenários e exercícios |
| 02:40 | 20 min | **Encerramento**: recap, atividade de casa, preview da próxima aula |

## 02 · Retomada da aula 03

*~10 min*

- "O que garante que um JWT não foi alterado?" (A assinatura.)
- "Posso colocar a senha do usuário no payload de um JWT?" (Não, o payload é legível por quem tem o token.)
- "O que eu preciso ter para forjar um token válido?" (A chave de assinatura.)

Peça a 2 alunos que contem o que acharam sobre os claims (`iss`, `sub`, `aud`, `exp`, `iat`). Cinco minutos no máximo.

**Gancho:** "Até agora a própria aplicação emitia e validava o token. E quando quem emite o token é **outro** sistema, e a sua aplicação só quer acessar os dados de um usuário lá? É aí que entra o OAuth."

## 03 · O problema e o OAuth 2.0

*~15 min*

### O problema

Pergunte à turma: "Um app de impressão de fotos quer buscar as suas fotos no Google Fotos. Como ele consegue?"

Anote as ideias. A pior delas (e a que existia antes do OAuth) é: **o usuário entrega seu usuário e senha do Google ao app de impressão.** Peça que a turma liste o que há de errado:

- O app passa a ter **acesso total** à conta, não só às fotos.
- O usuário não consegue **revogar** o acesso de um app sem trocar a senha (e quebrar todos os outros).
- Se o app for invadido, a **senha do Google** vaza.

### A solução: OAuth 2.0

> O OAuth 2 é um protocolo de **autorização** que permite que aplicativos obtenham **acesso limitado** a contas de usuários em um serviço HTTP **sem precisar do usuário e da senha** dele. O usuário *delega* ao aplicativo o acesso a seus dados.

Ideia central: em vez de dar a senha, o usuário autoriza o app e o serviço entrega ao app um **token de acesso** com permissões limitadas e prazo de validade.

**Exemplo cotidiano:** o *login social* (*social sign-in*), aquele botão "Entrar com..." de muitos sites, é um uso de OAuth em que o usuário dá permissão para outro site ver certos dados da sua conta. É só um exemplo para a turma reconhecer: **ninguém precisa acessar nenhum desses sites** nesta aula.

> **Ponto de atenção:** OAuth 2.0 é o nome do protocolo (a versão 2 é a usada hoje). Não é compatível com a versão 1.0. Quando alguém diz "OAuth", quase sempre quer dizer OAuth 2.0.

## 04 · Terminologias

*~15 min*

Use o exemplo do app de impressão de fotos e do Google Fotos para dar nome a cada papel:

| Termo | O que é | No exemplo |
|---|---|---|
| **Proprietário do recurso** (*resource owner*) | Quem é dono dos dados e pode autorizar o acesso. | O usuário. |
| **Cliente** (*client*) | O aplicativo que quer acessar os dados em nome do proprietário. | O app de impressão. |
| **Servidor de recursos** (*resource server*) | Onde os dados protegidos ficam; aceita requisições com token de acesso. | A API do Google Fotos. |
| **Servidor de autorização** (*authorization server*) | Autentica o usuário, colhe o consentimento e **emite os tokens**. | O servidor de login/consentimento do Google. |
| **Concessão de autorização** (*grant*) | A credencial que representa a autorização dada pelo proprietário. | A prova de que o usuário autorizou. |
| **Código de autorização** | Um código temporário, de uso único, que o cliente troca por um token. | Devolvido pelo redirecionamento. |
| **Token de acesso** (*access token*) | A credencial usada para acessar o recurso protegido. | O que o app envia à API de fotos. |
| **Escopo** (*scope*) | O que o token permite fazer. | "ler fotos", mas não "apagar" nem "ler e-mails". |

> **Dica:** peça que a turma **aponte quem é quem** no exemplo das fotos (quem é o proprietário, o cliente, o servidor de recursos, o servidor de autorização) antes de seguir. Se errar o papel do servidor de recursos x servidor de autorização, volte à tabela.

## 05 · Grant types

*~45 min*

*Grant type* (tipo de concessão) é **o caminho pelo qual o cliente obtém o token**. A escolha depende do tipo de aplicação e de se existe um usuário envolvido.

### 1. Código de autorização (*Authorization Code*)

O fluxo mais importante, o usado no login social e em apps de terceiros. O cliente **nunca vê a senha** do usuário.

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

- O token **não passa pelo navegador**; só o código, que é de uso único e curto.
- O parâmetro **`state`** é um valor aleatório que o cliente confere na volta. Ele protege contra **CSRF** no fluxo de login (liga com a aula 01).

### 2. Implícito (*Implicit*)

Parecido com o anterior, mas **sem a etapa do código**: o token é devolvido **direto no redirecionamento**, na URL.

- Foi criado para aplicações só de navegador (SPAs), que não tinham como guardar segredo.
- **Problema:** o token fica exposto na URL, no histórico do navegador e em logs, e o cliente não se autentica. Hoje é **considerado legado e desaconselhado**.

### 3. Credenciais do cliente (*Client Credentials*)

**Não há usuário.** É uma aplicação falando com outra (máquina a máquina): o cliente se autentica com o seu próprio `client_id` e `client_secret` e recebe um token.

- Exemplos: um serviço de relatórios chamando outro serviço, um job agendado.
- Só deve ser usado por clientes que conseguem **guardar um segredo** (um servidor, não um app de navegador).

### 4. Credenciais do proprietário do recurso (*Resource Owner Password Credentials*)

O usuário digita **usuário e senha direto no cliente**, que os repassa ao servidor de autorização em troca do token.

- Anula a ideia do OAuth: o cliente **vê a senha**.
- Só se justificaria para clientes extremamente confiáveis (do mesmo dono do servidor). Hoje é **legado e desaconselhado**.

### 5. Atualização de token (*Refresh Token*)

O token de acesso tem vida curta. Em vez de pedir ao usuário que faça login de novo, o cliente apresenta o **refresh token** e recebe um novo token de acesso.

- Mantém o usuário "logado" sem expor um token de acesso de vida longa.
- O refresh token precisa ser **guardado com mais cuidado** que o de acesso.

### Resumo

| Grant type | Há usuário? | Quando se usa | Situação hoje |
|---|---|---|---|
| Código de autorização | Sim | Apps web com backend, apps móveis, SPAs (com PKCE) | **Recomendado** |
| Implícito | Sim | SPAs antigas | Legado, evitar |
| Credenciais do cliente | Não | Máquina a máquina | Recomendado para esse caso |
| Credenciais do proprietário | Sim | Clientes 100% confiáveis | Legado, evitar |
| Refresh token | Sim | Renovar o acesso | Recomendado, junto com outro grant |

## 06 · Qual grant usar hoje e OAuth x autenticação

*~25 min*

### A recomendação atual

Quem não tem como guardar segredo (app móvel, SPA) não pode usar o `client_secret` do fluxo de código. A solução moderna é o **código de autorização com PKCE**:

- Antes de começar, o cliente gera um valor secreto aleatório (*code verifier*) e envia ao servidor apenas um **hash** dele (*code challenge*).
- Na troca do código por token, envia o valor original; o servidor confere o hash.
- Quem interceptar o código no caminho **não consegue trocá-lo**, porque não tem o verificador.

> **Nota:** o material de apoio descreve os cinco grants clássicos. Apresente-os todos, como no planejamento, mas deixe claro que as versões mais novas das recomendações de segurança do OAuth **abandonaram o implícito e o de credenciais do proprietário** e fixaram **código de autorização com PKCE** como padrão.

### Por que não usar OAuth para autenticação

Essa é a frase do planejamento e a confusão mais comum do assunto.

- **OAuth responde:** "este portador **pode acessar** tal recurso, com tais escopos?" (autorização).
- **OAuth não responde:** "**quem é** o usuário e ele está presente agora?" (autenticação).
- O token de acesso é feito para o **servidor de recursos**, não para o cliente interpretar. Cada provedor acabou inventando um jeito próprio de entregar os dados do perfil, e isso gerou implementações inseguras de "login com OAuth".
- Para **autenticar**, existe uma camada padronizada **por cima** do OAuth: o **OpenID Connect**, tema da aula 06.

**Analogia:** o token de acesso é como a **chave de um quarto de hotel**. Abre a porta (autoriza), mas não diz quem é o hóspede.

## 07 · Prática guiada

*~40 min*

### Parte 1: Access token e refresh token no código (~15 min)

Tudo roda na máquina local, **sem nenhum site externo**. O projeto está pronto em [`source/`](source/README.md) (Spring Security + JWT + H2 + Flyway). Suba a aplicação antes (preparação na seção 10) e abra o `http/auth.http` no IntelliJ, com o ambiente `dev` selecionado em *Run with*.

**Mão na massa**

1. Rode a requisição **4 (login)** com `ana` / `Senha@12345`. A resposta traz **dois** tokens: `accessToken` e `refreshToken`.
2. Cole os dois no **jwt.io** (aula 03) e compare os claims:

   | | Access token | Refresh token |
   |---|---|---|
   | `exp` | 15 minutos | 7 dias |
   | `role` | tem | **não tem** |
   | `jti` | não tem | tem (o id que fica no banco) |
   | `token_use` | `access` | `refresh` |

3. Rode **6** (`/me`) com o access token: 200. Rode **7** (sem token): 401. Rode **8** (`/admin/painel` com a `ana`): **403**.
4. Rode **9 (refresh)**: volta um par novo. Rode **10 (reuso)**, que reapresenta o refresh token que o 9 acabou de gastar: **401**. Pergunte: "por que o servidor recusou um token que ainda não expirou?" (Cada refresh token vale **uma vez só**: é a *rotação*. Reusar um token já usado é sinal de roubo, e o servidor revoga todos os tokens do usuário.) Rode o **9** mais uma vez: agora o refresh token **novo** também deu 401. Para seguir, faça o login de novo (passo 1).

**Pergunte (aponte no código, sem digitar nada):**
- "Quem é o **servidor de autorização** aqui? E o **servidor de recursos**?" (`/auth/login` e `/auth/refresh` emitem tokens; `/me` e `/admin/painel` os aceitam. Aqui os dois estão **na mesma aplicação**; no OAuth de verdade costumam ser sistemas separados.)
- "O `/auth/login` recebe usuário e senha direto no cliente. Qual *grant* da seção 05 isso lembra?" (Credenciais do proprietário. É aceitável aqui porque o cliente e o servidor são do **mesmo dono**; para apps de terceiros, é o caso em que se usa código de autorização.)
- "Qual grant é o `/auth/refresh`?" (O de **refresh token**, igual ao da seção 05.)
- "Por que o access token vive só 15 minutos e o refresh token 7 dias?" (Se o access for roubado, vale pouco. O refresh é usado raramente e **pode ser revogado**, porque está registrado no banco.)

**Cheque:** peça que as duplas desenhem no papel o fluxo *login → usa o access → access expira (401) → refresh → novo par → logout*, apontando em cada passo qual token trafega.

> **Ponto de atenção:** este projeto **não é** um servidor OAuth completo (não há tela de consentimento, nem código de autorização, nem escopos). Ele mostra o **par access + refresh** e a rotação, que é o que um servidor de autorização real também faz. O código de leitura é o `AuthService.renovar`.

Compare 2 ou 3 desenhos na lousa e corrija as confusões de papéis.

### Parte 2: Dinâmica "Qual grant eu uso?" (~15 min)

Apresente cada cenário, deixe as duplas decidirem em 1 minuto e peça que **justifiquem**:

| # | Cenário | Resposta esperada |
|---|---|---|
| 1 | Um site com backend em Java quer ler as fotos do usuário em um serviço de fotos de outra empresa. | Código de autorização. |
| 2 | Um aplicativo móvel precisa acessar a API do banco em nome do cliente. | Código de autorização com PKCE. |
| 3 | O serviço de relatórios chama, de madrugada, a API de pedidos. Não há usuário. | Credenciais do cliente. |
| 4 | O token de acesso do usuário expira a cada 15 minutos e ele não deve logar de novo. | Refresh token. |
| 5 | Um colega propõe: "a SPA recebe o usuário e a senha e manda para a API de autorização, é mais simples". | Rejeitar (credenciais do proprietário): o cliente não deve ver a senha. Usar código com PKCE. |

Para cada resposta errada, pergunte "qual risco isso traz?" em vez de só corrigir.

### Parte 3: Lista de exercícios (~10 min)

Lista de exercícios de múltipla escolha sobre **OAuth**. Dê o tempo para a turma começar e deixe o que sobrar como atividade de casa.

## 08 · Encerramento

*~20 min*

Recap de 3 minutos, **feito pela turma**: o problema que o OAuth resolve; os quatro papéis; o fluxo de código de autorização em 6 passos; um uso de cada grant type; o que é escopo; por que OAuth autoriza, mas não autentica.

Feche amarrando com o módulo: "Hoje, o servidor de autorização emitiu o token. Na próxima aula vamos ver o **Single Sign-On**: como o usuário entra **uma vez** e acessa vários sistemas, e qual o papel do OAuth nisso".

**Atividade para casa:**
- **Leitura:** introdução ao OAuth 2.0: https://oauth.net/2/
- **Pesquisa individual:** o que é PKCE e qual problema ele resolve.
- **Pesquisa individual:** escolher um sistema da empresa (ou um app que você usa) que acessa dados de outro serviço em seu nome e listar quais permissões (escopos) ele precisaria ter.
- **Laboratório (opcional):** reconstruir o projeto `auth-jwt` do zero, seguindo o passo a passo do [README](source/README.md).
- **Exercícios de fixação:** o que sobrou da lista sobre OAuth.

**Preview da Aula 05:** Single Sign-On (SSO). Identidade federada, arquitetura, fluxo e protocolos como SAML 2.0 e OpenID Connect.

## 09 · Dicas de condução

- **Muitas siglas, poucas telas:** use sempre o mesmo exemplo (fotos e Google) em todos os blocos. Trocar de exemplo a cada conceito é o que perde a turma.
- **Leve para a sala uma URL de autorização de exemplo,** colada no quadro, com `client_id`, `redirect_uri`, `scope` e `state` destacados em cores (pode ser inventada, no formato `https://auth.exemplo.com/authorize?response_type=code&client_id=...`). Ajuda mais que qualquer diagrama e não depende de acesso à internet.
- **Sem acesso externo:** a prática não usa nenhum login social nem site de fora. Tudo roda na máquina local, com o projeto `auth-jwt`.
- **Não entre em detalhes de cada endpoint ou campo do `/token`.** O objetivo é entender o conceito e escolher o grant, não implementar um servidor OAuth.
- **Onde aparece o que já foi visto:** o `state` previne CSRF (aula 01); o token de acesso pode ser um JWT (aula 03); o `redirect_uri` precisa ser HTTPS (aula 02).
- **Itens do planejamento cobertos:** o que é OAuth e OAuth 2.0, terminologias, grant types (código de autorização, implícito, credenciais do cliente, credenciais do proprietário, refresh token), por que não usar OAuth para autenticação e a lista de múltipla escolha sobre OAuth.

## 10 · Código de apoio e preparação da demonstração

O projeto `auth-jwt` está em [`source/`](source/README.md). Esta seção diz **o que ele tem**, **o que fazer antes da aula** e **o que mostrar** na Parte 1 da prática.

### O que o código tem

| Recurso | Onde está | Visto em aula? |
|---|---|---|
| Cadastro e login com usuário e senha | `AuthController`, `AuthService` | **Esta aula** (Parte 1) |
| **Access token** (JWT, 15 min, com o papel do usuário) | `TokenService.gerarAccessToken` | **Esta aula** |
| **Refresh token** (JWT, 7 dias, registrado no banco), com **rotação** | `TokenService.gerarRefreshToken`, `AuthService.renovar` | **Esta aula** |
| **Detecção de reuso** do refresh token (revoga todos do usuário) | `AuthService.renovar` | **Esta aula**, como pergunta |
| Logout (revoga o refresh token) | `AuthService.logout` | **Esta aula** |
| Filtro que lê o `Authorization: Bearer` e as regras de acesso (401, 403, papel `ADMIN`) | `JwtAuthenticationFilter`, `SecurityConfig` | Mostrar na leitura; o Spring Security em detalhe é a aula 08 |
| Validação de entrada (`@Valid`) e validação do usuário (conta desativada, mesma resposta para usuário inexistente e senha errada) | `RegisterRequest`, `UsuarioDetailsService` | Aulas 01 e 07 |
| Senha com **BCrypt** | `SecurityConfig` | Aula 02 (hash) e aula 08 |
| **H2** e migrations do **Flyway** (`V1`, `V2`, `V100`) | `db/migration`, `db/seed` | Só neste projeto; fica como material de estudo |
| Erros em formato único, sem vazar detalhes | `GlobalExceptionHandler` | Aula 07 |
| 30 testes automatizados | `AuthJwtTest` | Material de estudo |

**O que o código não tem (diga à turma):** tela de consentimento, código de autorização, escopos e PKCE (ou seja, **não é um servidor OAuth completo**); bloqueio de tentativas de login; limpeza dos refresh tokens expirados. Está na lista de limites do README.

### Antes da aula (checklist, ~10 min)

1. **Java 17+ e Maven 3.9+:** `java -version` e `mvn -v`.
2. **Defina a chave** (mínimo de 32 caracteres; com menos, a aplicação não sobe) e **suba** a aplicação, na pasta `aula04/source`:

   ```powershell
   $env:JWT_SECRET = "troque-por-uma-chave-longa-com-32-ou-mais-caracteres"
   mvn spring-boot:run
   ```

   No Git Bash: `export JWT_SECRET="..."` e depois `mvn spring-boot:run`. No IntelliJ: *Run > Edit Configurations > Environment variables* da classe `AuthApplication`.
3. **Confira o log:** as linhas `Migrating schema "PUBLIC" to version "1"`, `"2"` e `"100"` do Flyway, e `Started AuthApplication`. A aplicação responde em `http://localhost:8080`.
4. **Abra o `http/auth.http` no IntelliJ**, escolha o ambiente **`dev`** em *Run with* e rode a requisição **1** (`/publico/ping`): deve devolver 200. O ambiente `dev` é só o conjunto de variáveis do arquivo `http-client.env.json` (aqui, `host = http://localhost:8080`).
5. **Ensaie o roteiro da Parte 1** uma vez, na ordem **4, 6, 7, 8, 9, 10**. Depois reinicie a aplicação (o banco é em memória e volta ao estado inicial).
6. **Tenha o jwt.io aberto** em uma aba, para colar os tokens.
7. **Usuários de demonstração:** `ana` / `Senha@12345` (USER) e `admin` / `Admin@12345` (ADMIN).

> **Nota:** a aplicação roda em HTTP e na sua máquina. Nenhum passo depende de internet, de Google, de GitHub ou de qualquer serviço externo.

### O que mostrar (Parte 1 da prática, ~15 min)

A sequência já está na Parte 1 da seção 07. Em resumo, no `auth.http`:

| Requisição | O que mostrar | Resposta |
|---|---|---|
| **4** login | Os **dois** tokens; cole no jwt.io e compare `exp`, `role`, `jti`, `token_use` | 200 |
| **6** `/me` com o access token | O access token abre a porta | 200 |
| **7** `/me` sem token | Sem token não entra | 401 |
| **8** `/admin/painel` com a `ana` | Autenticado, mas sem o papel: **401 x 403** | 403 |
| **9** refresh | Volta um **par novo** | 200 |
| **10** reuso do refresh gasto | O token ainda não expirou, mas já foi usado | 401 |
| **9** de novo | Até o refresh **novo** foi revogado | 401 |

**Extras, se sobrar tempo** (cada um leva ~2 minutos):

- **Cadastro inválido** (requisição **3**): 400 com a lista de campos que falharam.
- **Senha errada** (requisição **5**): 401 com a **mesma** mensagem de "usuário inexistente". Pergunte: "por que a resposta é igual?"
- **Adulterar o token:** troque o último caractere do access token no `/me` e veja o 401.
- **Usar o refresh token como access token** no `/me`: 401. Pergunte: "por que existe o claim `token_use`?"
- **Logout** (requisição **11**) e depois o **9**: 401.

### Se algo der errado

| Sintoma | Causa provável | O que fazer |
|---|---|---|
| A aplicação não sobe: `JWT_SECRET` | A variável não foi definida, ou tem menos de 32 caracteres | Defina de novo no **mesmo** terminal em que roda o `mvn`. |
| `Port 8080 was already in use` | Outra aplicação usa a porta | Feche-a, ou suba com `--server.port=8081` e ajuste o `host` no `http-client.env.json`. |
| `mvn test` não roda nenhum teste | O `~/.m2/settings.xml` define `maven.test.skip=true` | Use `mvn "-Dmaven.test.skip=false" test` (com aspas no PowerShell). |
| `FlywayException: Validate failed` | Uma migration já aplicada foi editada | Reinicie a aplicação (o banco é em memória) ou crie uma migration nova. |
| O refresh (**9**) dá 401 depois de reiniciar a aplicação | O banco é em memória: os refresh tokens de antes sumiram (o access token antigo ainda vale até expirar, se a chave for a mesma) | Rode o login (**4**) de novo. |
| `{{host}}` aparece sem valor | O ambiente `dev` não está selecionado | Escolha **dev** em *Run with*. |

---

*Roteiro de aula: módulo de Fundamentos de segurança para aplicações. Aula 04 de 09.*
