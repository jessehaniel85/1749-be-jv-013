# Aula 08: código de apoio (Spring Security: autenticação básica e JWT)

Código usado na [Aula 08](../roteiroaula08.md): primeiro a autenticação básica (seção 04 do roteiro) e depois a autenticação com JWT (seção 06 do roteiro).

## Ponto de partida

**Comece do estado final da Aula 07** (`../../aula07/source`). É a mesma API de alunos, com H2, tratamento global de exceções e rate limit.

Mudanças em relação à aula 07:

- **Dependências novas:** `spring-boot-starter-security`, `spring-boot-starter-data-jpa` (para a entidade de usuário) e `com.auth0:java-jwt`.
- **`POST /alunos`** novo, só para mostrar a proteção CSRF (passo 5 da autenticação básica).
- **`GlobalExceptionHandler`** ganhou dois handlers: `AuthenticationException` (401) e `ResponseStatusException` (usa o status e o motivo dela, por exemplo 409).
- Os pacotes `cripto`, `cookie` (aula 02) e `jwt` (aula 03) continuam aqui, mas **agora também exigem autenticação** (como todo o resto). O `jwt` da aula 03 era a versão **à mão** e só para demonstração; o JWT do projeto é o do pacote `security`.
- **Os testes das aulas 01, 02 e 07 foram removidos**, porque agora os endpoints exigem autenticação. Os novos testes são `Aula08BasicaTest` e `Aula08JwtTest`.

## Três estados do projeto, por perfil

O código final é o JWT, mas a aula passa por três estados. Eles são separados por **perfil do Spring**, para você poder mostrar cada um sem apagar código:

| Perfil | Estado | Passos da aula |
|---|---|---|
| *nenhum* | Só a dependência `starter-security`, **sem nenhuma configuração**. Senha gerada no log e formulário de login. | Básica 1 |
| `basica` | Usuário e senha em `application-basica.yml` e rotas livres/protegidas em `SecurityBasicConfig`. | Básica 2 a 5 |
| `jwt` | **Estado final:** cadastro, `/authenticate`, filtro do JWT. | JWT 1 a 6 |

## Passo a passo da aula, na ordem em que acontece

### Parte A: autenticação básica (seção 04 do roteiro)

| Passo | O que fazer | Onde está |
|---|---|---|
| Básica 1 | Adicionar `spring-boot-starter-security` e rodar **sem perfil**. Ler no log `Using generated security password`. Abrir `/alunos/1`: formulário de login (usuário `user`). | `pom.xml` |
| Básica 2 | Definir o usuário e a senha em `application.yml` (aqui, no perfil `basica`) e rodar de novo. A senha não é mais aleatória. | `application-basica.yml` |
| Básica 3 | Acessar com HTTP Basic: `curl -i -u aluno:ada123 http://localhost:8080/alunos/1`. Mostrar que o cabeçalho é `Basic YWx1bm86YWRhMTIz` e decodificá-lo (`echo 'YWx1bm86YWRhMTIz' \| base64 -d`). | |
| Básica 4 | Escolher as rotas: `/publico/**` livre, o resto protegido, com `SecurityFilterChain`. | `security/SecurityBasicConfig.java`, `publico/PublicoController.java` |
| Básica 5 | `POST /alunos` com as credenciais certas: **403**. A causa é o CSRF, ligado por padrão. | `AlunoController.cadastra` |

> **Intervalo** (roteiro: 01:05). Daqui em diante, troque para o perfil `jwt`.

### Parte B: autenticação com JWT (seção 06 do roteiro)

| Passo | O que fazer | Onde está |
|---|---|---|
| JWT 1 | Entidade `Usuario` (tabela `users`), `UsuarioRepository` e os beans `PasswordEncoder` (BCrypt) e `AuthenticationManager`. Mostrar no console que o BCrypt gera hashes diferentes para a mesma senha. | `usuario/Usuario.java`, `usuario/UsuarioRepository.java`, `security/SecurityBeans.java` |
| JWT 2 | Ensinar o Spring a buscar o usuário no banco. | `usuario/UsuarioDetailsService.java` |
| JWT 3 | Gerar e validar o token, com a chave vinda de variável de ambiente. | `security/TokenService.java`, `application-jwt.yml` |
| JWT 4 | `POST /user` e `POST /authenticate`. | `auth/AuthController.java` |
| JWT 5 | O filtro que lê o token a cada requisição (aceita `Authorization: Bearer` e `access_token`). | `security/JwtAuthenticationFilter.java` |
| JWT 6 | Juntar tudo: CSRF desabilitado (e por quê), sem sessão, rotas livres, 401, filtro na cadeia. | `security/SecurityConfig.java` |
| Teste | Cadastrar, autenticar, acessar sem token (401) e com token (200). Depois **quebrar de propósito**: alterar um caractere do token, errar a senha, olhar o hash na tabela, colar o token no jwt.io. | |

### Parte C: além da autenticação (seção 07 do roteiro)

Só **mostrar** (nada para digitar): CSRF e por que o desabilitamos (comentários em `SecurityConfig`), o bloco comentado da Content Security Policy no mesmo arquivo e a menção a `@PreAuthorize`.

## Até onde a aula vai

**A aula vai até o passo "Teste" da Parte B (e a Parte C só em conversa). O código acima é o estado final.** Fica de fora, de propósito, para o **projeto**:

- **Regra de propriedade** (cada usuário ver só os próprios dados): aqui os alunos são de todos.
- **Validação de entrada** (`@Valid`): o `POST /user` aceita qualquer texto.
- **Perfis e autorização por perfil** (`@EnableMethodSecurity` e `@PreAuthorize`): todos os usuários têm o papel `USER`.
- **Refresh token**, **rate limit no login** e **testes do fluxo** em um projeto de equipe.
- Persistência definitiva: o H2 é em memória, então usuários e tokens somem ao reiniciar.

## Próxima aula

**Aula 09** (projeto). **Não tem código novo.** As equipes continuam o **projeto delas**; este projeto (`aula08/source`, estado final) é o **projeto-referência** que o instrutor mantém rodando para comparar quando uma equipe travar.

## Como executar

Java 17 ou superior e Maven 3.9 ou superior.

**Sem perfil** (só a dependência; senha no log):

```bash
mvn spring-boot:run
```

**Perfil `basica`** (usuário `aluno`, senha `ada123`):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=basica
```

**Perfil `jwt`** (estado final). Defina a chave **antes** de subir:

```powershell
$env:JWT_SECRET = "troque-por-uma-chave-longa-e-aleatoria-com-mais-de-32-caracteres"
mvn spring-boot:run "-Dspring-boot.run.profiles=jwt"
```

```bash
export JWT_SECRET="troque-por-uma-chave-longa-e-aleatoria-com-mais-de-32-caracteres"
mvn spring-boot:run -Dspring-boot.run.profiles=jwt
```

Na IDE: *Run Configuration* com **Active profiles** `jwt` e a variável de ambiente `JWT_SECRET`. Sem ela, a aplicação não sobe (`Could not resolve placeholder 'JWT_SECRET'`): é a causa mais comum de erro na aula.

## Testando o fluxo com JWT

No Postman, ou no `curl` (Git Bash/Linux/macOS; no PowerShell/CMD as aspas mudam, prefira o Postman):

```bash
# 1. cadastrar (201)
curl -i -X POST localhost:8080/user -H "Content-Type: application/json" \
     -d '{"username":"ana","password":"senha123"}'

# 2. autenticar (200 + {"token":"..."})
curl -i -X POST localhost:8080/authenticate -H "Content-Type: application/json" \
     -d '{"username":"ana","password":"senha123"}'

# 3. sem token (401)
curl -i localhost:8080/alunos/1

# 4. com token (200), nos dois formatos de cabeçalho
curl -i localhost:8080/alunos/1 -H "Authorization: Bearer <token>"
curl -i localhost:8080/alunos/1 -H "access_token: <token>"
```

> O rate limiter de `/alunos` (aula 01) continua ativo: a segunda chamada a `GET /alunos` em menos de 10 segundos devolve 429. Para testar com calma, use `/alunos/1`.

## Testes automatizados

```bash
mvn test
```

- `Aula08BasicaTest` (perfil `basica`): rota pública, 401 sem credenciais, 200 com credenciais, e o **403 do CSRF** no POST.
- `Aula08JwtTest` (perfil `jwt`): hash BCrypt no banco, 409 de usuário duplicado, token com 3 partes, 401 para senha errada e usuário inexistente (mesma mensagem), 401 sem token, 200 com os dois cabeçalhos, 401 para token **adulterado**, **expirado** e **assinado com outra chave**, e POST funcionando com o CSRF desabilitado.
