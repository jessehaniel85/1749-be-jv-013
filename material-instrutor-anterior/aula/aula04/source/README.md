# Aula 04: código de apoio (Spring Security com JWT: access token e refresh token)

Código usado na [Aula 04](../roteiroaula04.md): um serviço de autenticação com **usuário e senha**, **validação** de entrada e de usuário, banco **H2**, migrations do **Flyway** e **dois tokens JWT** (access e refresh).

## Ponto de partida

**Este é um projeto novo e independente** (`auth-jwt`). Ele **não** continua a API de alunos: as aulas 07 e 08 seguem de `../../aula03/source`. A ideia é ter um serviço de autenticação completo e pequeno, que serve de referência para o projeto das equipes (Aula 09).

## O que o projeto faz

| Endpoint | Acesso | O que faz |
|---|---|---|
| `POST /auth/register` | livre | Cadastra um usuário (valida os campos; a senha vira hash BCrypt). |
| `POST /auth/login` | livre | Confere usuário e senha e devolve `accessToken` + `refreshToken`. |
| `POST /auth/refresh` | livre (exige um refresh token válido) | Troca o refresh token por um **novo par**; o antigo é revogado. |
| `POST /auth/logout` | livre | Revoga o refresh token. |
| `GET /me` | access token | Mostra quem é o usuário do token. |
| `GET /admin/painel` | access token com papel `ADMIN` | Prova o controle por papel (`USER` recebe 403). |
| `GET /publico/ping` | livre | Rota pública. |

Usuários de demonstração (criados pela migration `V100`): `admin` / `Admin@12345` (papel ADMIN) e `ana` / `Senha@12345` (papel USER).

## O fluxo com dois tokens

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
| Se for roubado | vale poucos minutos | permite renovar: por isso a **rotação** e a **detecção de reuso** |

## Como executar

Java 17+ e Maven 3.9+. A chave de assinatura **nunca** fica no código: defina a variável de ambiente `JWT_SECRET` (**mínimo de 32 caracteres**; com menos, a aplicação nem sobe).

PowerShell:

```powershell
$env:JWT_SECRET = "troque-por-uma-chave-longa-com-32-ou-mais-caracteres"
mvn spring-boot:run
```

Git Bash / Linux / macOS:

```bash
export JWT_SECRET="troque-por-uma-chave-longa-com-32-ou-mais-caracteres"
mvn spring-boot:run
```

No IntelliJ: *Run > Edit Configurations > Environment variables* da configuração `AuthApplication`: `JWT_SECRET=...`.

Depois, abra [`http/auth.http`](http/auth.http) no IntelliJ, escolha o ambiente **dev** em *Run with* e rode as requisições na ordem (cada uma tem um título explicando o que esperar). O login guarda os tokens nas variáveis `{{access}}` e `{{refresh}}` sozinho.

## Testes automatizados

```bash
mvn test
```

> **Atenção:** se o seu `~/.m2/settings.xml` define `maven.test.skip=true`, o `mvn test` pula os testes. Use `mvn "-Dmaven.test.skip=false" test` (com as aspas no PowerShell).

O `AuthJwtTest` (30 testes, com `MockMvc`) cobre: cadastro válido e inválido, senha guardada como BCrypt, conflito de usuário, tentativa de "mass assignment" do papel, login com a mesma resposta para usuário inexistente e senha errada, conta desativada, token adulterado, expirado, de outra chave e `alg=none`, refresh token usado como access (e o contrário), 403 por papel, rotação, **detecção de reuso**, logout e o histórico do Flyway.

## Passo a passo para implementar com a turma

Cada passo termina com algo que dá para **ver funcionando**. Os arquivos estão comentados para servir de guia.

| Passo | O que fazer | Onde está | O que ver |
|---|---|---|---|
| 1 | Criar o projeto com as dependências (web, security, validation, data-jpa, h2, flyway, java-jwt) e configurar o H2 em memória. | `pom.xml`, `application.yml` | A aplicação sobe. |
| 2 | Escrever as migrations. `ddl-auto: validate` faz o Hibernate **conferir** o que o Flyway criou. | `db/migration/V1__...sql`, `V2__...sql`, `db/seed/V100__...sql` | No log: `Migrating schema "PUBLIC" to version "1"`, `"2"`, `"100"`. |
| 3 | Mapear `Usuario` e `RefreshToken` e criar os repositórios. | `usuario/`, `token/` | Sobe sem erro de validação do schema. |
| 4 | Ligar o Spring Security aos usuários do banco: `PasswordEncoder` (BCrypt) e `UsuarioDetailsService`. | `SecurityConfig`, `UsuarioDetailsService` | Toda rota passa a responder 401. |
| 5 | Cadastro com **validação** (`@Valid`) e senha em hash. | `RegisterRequest`, `AuthService.registrar` | `auth.http` 2 (201) e 3 (400 com a lista de campos). |
| 6 | O access token: `TokenService.gerarAccessToken` e o login. | `TokenService`, `AuthService.login` | `auth.http` 4: copie o token para o jwt.io. |
| 7 | O filtro que lê o `Bearer` e a regra "todo o resto exige autenticação". | `JwtAuthenticationFilter`, `SecurityConfig` | `/me` com token = 200; sem token = 401. |
| 8 | Papéis: `/admin/**` só para `ADMIN`. | `SecurityConfig`, `RestAccessDeniedHandler` | `ana` recebe **403**; `admin` recebe 200. |
| 9 | O refresh token com `jti` registrado no banco, e o `/auth/refresh` com **rotação**. | `TokenService.gerarRefreshToken`, `AuthService.renovar` | `auth.http` 9 (refresh) e depois 10 (reuso do token gasto): 401. |
| 10 | **Detecção de reuso**: um token já usado revoga todos os do usuário. | `AuthService.renovar`, `RefreshTokenRepository` | Depois do 10, rodar o 9 outra vez: o refresh token novo também deixou de funcionar. |
| 11 | Logout e tratamento global de erros (formato único, sem vazar detalhes). | `AuthService.logout`, `GlobalExceptionHandler` | `auth.http` 11, depois 9: 401. |

## Decisões de segurança (o "porquê" para discutir)

| Decisão | Por quê |
|---|---|
| Senha só como hash **BCrypt** (com salt e custo) | O SHA-256 é rápido demais para senhas (aula 02). Nem quem lê o banco vê a senha. |
| Mesma resposta (401) para "usuário não existe", "senha errada" e "conta desativada" | Não ajuda o atacante a descobrir quais usuários existem. |
| Claim `token_use` (`access` x `refresh`) conferido pelo verificador | Sem ele, um refresh token de 7 dias serviria como access token em qualquer rota. |
| Algoritmo **fixado** no servidor (`JWT.require(HMAC256)`) | Barra o `alg=none` e a troca de algoritmo (aula 03). |
| Chave de no mínimo 32 caracteres, vinda do ambiente, sem valor padrão | Chave fraca é forjável (desafio `ada123` da aula 03); sem a variável a aplicação não sobe. |
| O cliente **não** envia o papel no cadastro | Evita o *mass assignment*: ninguém se cadastra como `ADMIN`. |
| `username` único também na **constraint do banco** | Protege contra duas requisições simultâneas criando o mesmo usuário (resposta 409). |
| Rotação do refresh token + revogação de todos ao detectar reuso | Limita o estrago de um refresh token roubado. É a recomendação atual de segurança do OAuth. |
| `noRollbackFor` em `AuthService.renovar` | A revogação por reuso precisa ser **gravada**, mesmo com a exceção lançada logo depois. |
| CSRF desligado, sessão `STATELESS` | O token vai em cabeçalho montado pelo cliente, não em cookie automático (aula 01). |
| Erros sempre no mesmo formato, sem stack trace | Aula 07. |

## Limites conhecidos (de propósito)

- **O logout não derruba o access token já emitido:** ele vale até expirar (no máximo 15 min). Derrubar na hora exigiria uma lista de bloqueio no servidor, o que tira do JWT a vantagem de ser *stateless*. Esse é o motivo da vida curta.
- **O papel e o estado da conta ficam "congelados" no access token** até ele expirar. A renovação, sim, consulta o banco (conta desativada não renova).
- **Onde o cliente guarda os tokens?** Aqui o refresh token trafega no corpo da resposta (simples para estudar). Em um navegador, o mais seguro é entregá-lo em um **cookie `HttpOnly; Secure; SameSite=Strict`** restrito ao caminho `/auth/refresh` (aula 02), e manter o access token só em memória. `localStorage` é legível por qualquer XSS (aula 01).
- **Sem bloqueio de tentativas de login.** Em produção, combine com o *rate limit* da aula 01 (por IP e por usuário) para frear força bruta.
- **Os refresh tokens expirados não são apagados do banco.** Um job agendado (`@Scheduled`) com um `DELETE ... WHERE expira_em < now()` resolveria.
- **Os usuários de demonstração (`db/seed`) não pertencem a produção.** Em um ambiente real, a pasta `db/seed` sai de `spring.flyway.locations`.
- **Chave simétrica (HS256):** quem valida também consegue assinar. Com vários serviços validando tokens, o ideal é **RS256** (chave privada assina, pública valida; aula 02).
- **H2 em memória:** os dados somem ao parar a aplicação. Trocar por um banco real é mudar a `datasource` e usar o mesmo Flyway.

## Exercícios para ir além

1. **Nova migration:** crie `V3__adicionar_ultimo_login.sql` acrescentando a coluna `ultimo_login` em `usuarios` e preencha no login. Depois **edite a V1** e veja o Flyway recusar a subida (checksum).
2. **Logout de todos os dispositivos:** um endpoint que chama `revogarTodosDoUsuario` para o usuário autenticado.
3. **Escopos:** acrescente o claim `scope` no access token e proteja uma rota por escopo em vez de papel (liga com o OAuth da aula).
4. **Limpeza:** um job agendado que apaga os refresh tokens expirados.
5. **Cookie HttpOnly:** devolva o refresh token em um cookie `HttpOnly` e leia-o com `@CookieValue` no `/auth/refresh`.

## Próxima aula com código

A **Aula 05** também tem um projeto à parte (`../../aula05/source`: Single Sign-On). A **aula 06 não tem código**. A **Aula 07** continua do projeto da API de alunos (`../../aula03/source`). Na **Aula 08**, o Spring Security entra naquele projeto com um JWT mais simples (um token só); este projeto da aula 04 é a versão completa, com refresh token e Flyway.
