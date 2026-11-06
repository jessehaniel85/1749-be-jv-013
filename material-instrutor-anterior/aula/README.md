# Código por aula

Mapa do código de apoio do módulo BE-JV-013. Cada aula que precisa de código tem uma pasta `source/` com um projeto Maven completo e um `README.md` que diz **de onde partir**, **até onde a aula vai** e **o que fica para depois**.

O projeto é **um só, que evolui**: a API de alunos. Cada `source/` é o **estado final** daquela aula, então o ponto de partida da aula seguinte é sempre o `source/` anterior. **Exceção:** o `aula04/source` (`auth-jwt`, autenticação com access e refresh token) e o `aula05/source` (`sso-escola`, Single Sign-On) são projetos **independentes** e não entram nessa cadeia: a aula 07 continua de `aula03/source`.

| Aula | Data | Tem código? | Começa de | Acrescenta |
|---|---|---|---|---|
| [01](aula01/roteiroaula01.md) | 28/10 | Sim: [`source/`](aula01/source/README.md) | Projeto novo | `/busca` com XSS (vulnerável e corrigido) e rate limit em `/alunos` |
| [02](aula02/roteiroaula02.md) | 30/10 | Sim: [`source/`](aula02/source/README.md) | `aula01/source` | Criptografia simétrica (AES-GCM) e assimétrica (RSA, com assinatura) e cookies (`@CookieValue`, `HttpOnly`, `SameSite`) |
| [03](aula03/roteiroaula03.md) | 04/11 | Sim: [`source/`](aula03/source/README.md) | `aula02/source` | Sessão x token, JWT feito à mão (e com biblioteca), tentativas de falsificação e desafio da chave fraca |
| [04](aula04/roteiroaula04.md) | 06/11 | Sim: [`source/`](aula04/source/README.md) | **Projeto à parte** (`auth-jwt`, não continua a API de alunos) | Spring Security com JWT: usuário e senha, **access token + refresh token** (com rotação e revogação), H2 e Flyway |
| [05](aula05/roteiroaula05.md) | 09/11 | Sim: [`source/`](aula05/source/README.md) | **Projeto à parte** (`sso-escola`, não continua a API de alunos) | Single Sign-On local: um provedor de identidade e duas aplicações (Notas e Biblioteca), com código, `state`, asserção JWT assinada com RSA e logout único |
| 06 | 11/11 | Não | (continua em `aula03/source`) | Conceitos de OpenID Connect |
| [07](aula07/roteiroaula07.md) | 13/11 | Sim: [`source/`](aula07/source/README.md) | `aula03/source` | H2, SQL Injection (vulnerável e corrigido) e tratamento global de exceções |
| [08](aula08/roteiroaula08.md) | 16/11 | Sim: [`source/`](aula08/source/README.md) | `aula07/source` | Spring Security: autenticação básica e JWT |
| [09](aula09/roteiroaula09.md) | 18/11 | Não | (projeto das equipes) | Projeto-referência: `aula08/source` |

## Requisitos

Java 17 ou superior e Maven 3.9 ou superior. Em cada `source/`:

```bash
mvn test
mvn spring-boot:run
```

A aula 04 e a aula 08 (perfil `jwt`) exigem a variável de ambiente `JWT_SECRET`; veja o README de cada uma.
