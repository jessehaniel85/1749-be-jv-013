# Aula 03: código de apoio (sessão x token, JWT)

Código usado na [Aula 03](../roteiroaula03.md): a mesma API, agora com **dois modelos de autenticação lado a lado** (sessão e token), um **JWT construído à mão**, ferramentas para **tentar falsificar** tokens e um **desafio de chave fraca**.

**Atenção:** quase tudo aqui é **só para a demonstração**. Endpoints como `/jwt/gerar` (emite token para qualquer um) e `/jwt/validar` não existiriam em um sistema real. O sistema de autenticação de verdade é construído na aula 08, com Spring Security.

## Ponto de partida

**Comece do estado final da Aula 02** (`../../aula02/source`). Este projeto é aquele, mais o pacote `jwt`. O que já veio das aulas anteriores continua igual: `/busca`, `/alunos` com rate limit, `/cripto/...` (incluindo `FuncaoHash`, reaproveitada aqui) e os cookies.

## O que esta aula acrescenta

| Arquivo | O que é |
|---|---|
| `jwt/SessaoController.java` | Autenticação por **sessão**: o servidor guarda o usuário na memória e o cliente recebe só o cookie `JSESSIONID`. |
| `jwt/JwtController.java` | Autenticação por **token**: `/jwt/login`, as rotas protegidas e as ferramentas da demonstração. |
| `jwt/JwtManual.java` | O JWT (HS256) feito **à mão**: gerar, decodificar e validar (algoritmo, assinatura, emissor e expiração). |
| `jwt/JwtBiblioteca.java` | O mesmo JWT com a biblioteca **java-jwt** (a da aula 08), para mostrar que JWT é um padrão. |
| `jwt/UsuariosDemo.java` | Três usuários fixos (`ana`, `bia`, `admin`), com a senha guardada como hash SHA-256 (da aula 02). |
| `jwt/DesafioController.java` | O **desafio da chave fraca**. **Não abra este arquivo antes do desafio.** |
| `http/aula03.http` e `http/http-client.env.json` | As requisições da aula, para o cliente HTTP do IntelliJ. |
| `src/test/.../Aula03Test.java` | Testes de tudo o que está acima. |

**Usuários:** `ana` / `senha123` (`USER`), `bia` / `senha456` (`USER`), `admin` / `admin789` (`ADMIN`).

## Mapa: bloco da aula, requisições e código

As requisições estão em `http/aula03.http`, numeradas por bloco do roteiro.

| Bloco do roteiro | Requisições | Onde está o código |
|---|---|---|
| **1. Sessão** | 1.1 a 1.4 | `SessaoController` |
| **2. Token** | 2.1 a 2.6 | `JwtController` (`login`, `protegido`, `admin`) |
| **3. O que é um JWT** | 3.1 (decodificar), 3.2 (gerar os tokens T1, T2 e T3) | `JwtController.decodificar`, `JwtManual.decodificar` |
| **4. Dá para falsificar?** | 4.1 a 4.6 | `JwtManual.validar`: as 3 verificações; `JwtBiblioteca` |
| **5. E se roubarem o token?** | 5.1 e 5.2 | `JwtController.gerar` (validade escolhida) |
| **6. Quem guarda a chave?** | 6.1 a 6.3 | `DesafioController`; a chave de demonstração está em `application.yml` |

## Até onde a aula vai

**A aula usa todo o código acima, e ele é o estado final.** O que ainda **não** existe, de propósito:

- **Sem Spring Security.** A validação do token é feita **dentro dos controllers**, à mão. O Spring Security entra na aula 08 e faz isso em um filtro, para toda a API.
- **Sem banco de dados.** Os três usuários são fixos. O banco (H2) entra na aula 07.
- **Sem refresh token e sem revogação de tokens.** São conversados no Bloco 5, mas não implementados.
- **Sem RS256.** Só HS256 (chave secreta); o RS256 é comparado em conversa no Bloco 6.
- **Sem BCrypt.** As senhas dos usuários de demonstração usam SHA-256 (da aula 02); o BCrypt entra na aula 08.

> **A chave de demonstração:** `app.demo.jwt-secret`, no `application.yml`, tem um **valor padrão** para a aula rodar sem configuração. Isso é **o que não se deve fazer** em um sistema real: a chave nunca fica no repositório (o roteiro usa isso como pergunta no Bloco 6). Para trocá-la, defina a variável de ambiente `JWT_DEMO_SECRET` antes de subir a API; todos os tokens anteriores passam a ser recusados.

## Próxima aula com código

A **Aula 04** tem um projeto à parte, **independente** deste (`../../aula04/source`: Spring Security com access e refresh token, H2 e Flyway). A **Aula 05** também tem um projeto à parte (`../../aula05/source`: Single Sign-On). A **aula 06 não tem código** (OpenID Connect). A próxima aula que **continua este projeto** é a **Aula 07**. **Comece deste projeto, no estado final da Aula 03** (`aula03/source`). Veja `../../aula07/source/README.md`.

## Como executar

Java 17 ou superior e Maven 3.9 ou superior.

```bash
mvn spring-boot:run
```

Teste no navegador: http://localhost:8080/jwt/desafio/token (o token do desafio).

As demais requisições são `POST` e exigem cabeçalhos: use o **`http/aula03.http`** no IntelliJ (selecione o ambiente **dev**) ou o `curl` abaixo.

### Os mesmos passos com `curl` (Git Bash/Linux/macOS; no PowerShell/CMD prefira o IntelliJ ou o Postman)

```bash
# Bloco 1: sessão (o -c/-b guardam e reenviam o cookie JSESSIONID)
curl -i -c cookies.txt -X POST localhost:8080/sessao/login -H "Content-Type: application/json" \
     -d '{"username":"ana","password":"senha123"}'
curl -i -b cookies.txt localhost:8080/sessao/perfil

# Bloco 2: token
TOKEN=$(curl -s -X POST localhost:8080/jwt/login -H "Content-Type: application/json" \
     -d '{"username":"ana","password":"senha123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
curl -i localhost:8080/jwt/protegido -H "Authorization: Bearer $TOKEN"
curl -i localhost:8080/jwt/protegido -H "access_token: $TOKEN"
curl -i localhost:8080/jwt/protegido                         # 401

# Bloco 3: ler o payload SEM chave
echo "$TOKEN" | cut -d. -f2 | base64 -d

# Bloco 4: validar
curl -s -X POST localhost:8080/jwt/validar -H "Content-Type: application/json" -d "{\"token\":\"$TOKEN\"}"

# Bloco 5: token de 20 segundos
curl -s -X POST localhost:8080/jwt/gerar -H "Content-Type: application/json" \
     -d '{"usuario":"ana","expiraEmSegundos":20}'

# Bloco 6: desafio
curl -s localhost:8080/jwt/desafio/token
```

## Testes automatizados

```bash
mvn test
```

O `Aula03Test` cobre: sessão (com e sem cookie), login por token, rota protegida com os dois cabeçalhos, payload legível sem chave, 403 do usuário comum na área admin, **payload alterado**, token **sem assinatura (`alg: none`)**, token assinado com **outra chave**, token **expirado**, token malformado, token da biblioteca aceito pela validação manual (e o contrário) e o **desafio da chave fraca** (forjar com `ada123` funciona; forjar com a chave forte não).

> **Atenção:** se o seu `~/.m2/settings.xml` define `maven.test.skip=true`, o `mvn test` pula os testes. Use `mvn -Dmaven.test.skip=false test`.

> **Ética:** o desafio e as tentativas de falsificação são só contra esta API, na sua máquina. Nunca teste em sistemas de terceiros e nunca cole em sites online um JWT real de um sistema da empresa.
