# Aula 07: código de apoio (SQL Injection e tratamento de exceções)

Código usado na [Aula 07](../roteiroaula07.md): SQL Injection (seção 03 do roteiro), dependências vulneráveis (seção 04) e tratamento de exceções no Spring (seções 05 e 06, prática na seção 07).

## Ponto de partida

**Comece do estado final da Aula 03** (`../../aula03/source`). As aulas 04 a 06 não tiveram código, então o projeto é o mesmo.

Mudanças em relação à aula 02:

- **Banco H2 em memória** com a tabela `alunos` (`schema.sql` e `data.sql`), via `spring-boot-starter-jdbc` e `h2`.
- **`/alunos` deixa de usar a lista fixa** e passa a ler do banco (repositório e serviço novos).
- O **rate limiter** da aula 01 continua, mas agora a segunda chamada devolve **429** em vez de 500.
- O teste do rate limiter saiu do `Aula01Test` e foi para o `Aula07Test`.
- Os pacotes `cripto` e `cookie` (aula 02) e `jwt` (aula 03: sessão, JWT à mão e desafio da chave fraca) **continuam aqui, sem alteração**. A única mudança por causa deles é a dependência `java-jwt` no `pom.xml` e a chave de demonstração em `application.yml`.

## O que esta aula acrescenta

| Arquivo | O que é |
|---|---|
| `aluno/AlunoRepository.java` | Consultas com `JdbcTemplate`: uma **vulnerável** e uma **corrigida**. |
| `aluno/AlunoService.java` | Regra de negócio; lança a exceção própria. |
| `aluno/AlunoController.java` | `GET /alunos`, `/alunos/{id}`, `/alunos/pesquisa` e `/alunos/pesquisa/vulneravel`. |
| `exception/AlunoNaoEncontradoException.java` | Exceção própria. |
| `exception/ErroResponse.java` | Corpo padronizado das respostas de erro. |
| `exception/GlobalExceptionHandler.java` | Tratamento global. |
| `exception/ErroTesteController.java` | `GET /erro-teste`: força um erro inesperado **só para a demonstração**. |
| `dependabot.yml.exemplo` | Exemplo de configuração do Dependabot (seção 04 do roteiro). |

## Passo a passo da aula, na ordem em que acontece

### Parte A: SQL Injection (seção 03 do roteiro)

| Passo | O que fazer | Onde está |
|---|---|---|
| A1 | Escrever a consulta vulnerável (concatenando o `nome`) e perguntar à turma o que está errado. | `AlunoRepository.pesquisaVulneravel` |
| A2 | Uso normal: `/alunos/pesquisa/vulneravel?nome=Batman` devolve 1 aluno. | |
| A3 | Ataque: `/alunos/pesquisa/vulneravel?nome=x' OR '1'='1` devolve **todos** os alunos. | |
| A4 | (Opcional) tentar o ataque do material: `nome=x'; TRUNCATE TABLE alunos; --`. O resultado depende do banco e do driver; teste antes. | |
| A5 | Corrigir com instrução preparada (`?`) e repetir o ataque: não encontra nada. | `AlunoRepository.pesquisa` (`/alunos/pesquisa`) |

### Parte B: dependências (seção 04 do roteiro)

| Passo | O que fazer |
|---|---|
| B1 | Mostrar `dependabot.yml.exemplo` e explicar onde ele vai (`.github/dependabot.yml`). |
| B2 | Mostrar `mvn dependency:tree` e, se der tempo, `mvn org.owasp:dependency-check-maven:check` (rode **antes** da aula: a primeira execução baixa uma base grande). |

### Parte C: tratamento de exceções (seções 05, 06 e 07 do roteiro)

| Passo | O que fazer | Onde está |
|---|---|---|
| C1 | **Reproduzir o problema:** no serviço, comece com `repository.findById(id).get()` e chame `/alunos/999`: erro 500. | `AlunoService.buscaPorId` |
| C2 | Criar a **exceção própria** e lançá-la com `orElseThrow`. | `AlunoNaoEncontradoException` |
| C3 | Tratar **no controller** com `@ExceptionHandler` e ver o 404. (Bloco comentado no controller: descomente.) | `AlunoController`, final do arquivo |
| C4 | Mover para o **tratamento global** (`@RestControllerAdvice`) com o `ErroResponse` padronizado. Comente o bloco do passo C3. | `GlobalExceptionHandler` |
| C5 | Forçar um erro inesperado (`/erro-teste`): resposta genérica e detalhe **só no log**. | `ErroTesteController` |
| C6 | Tratar o `RequestNotPermitted` do rate limiter: chamar `/alunos` duas vezes e ver o **429**. | `GlobalExceptionHandler.muitasRequisicoes` |
| C7 | **A armadilha do handler genérico:** chamar `/boas-vindas/estrito` (o 400 da aula 02). Com um handler de `Exception` "puro", vira **500**. A solução é estender `ResponseEntityExceptionHandler`, como no código final. | `GlobalExceptionHandler` |

> **Sobre o passo C7:** no código final, o `GlobalExceptionHandler` já estende `ResponseEntityExceptionHandler`. Para mostrar o problema ao vivo, comece com um handler que **não** estende a classe-base, chame `/boas-vindas/estrito` (500), e só então estenda a classe e veja o 400 voltar. Responde à pergunta que ficou aberta na aula 02.

## Até onde a aula vai

**Esta aula vai até o passo C7 e o código acima é o estado final.** O que ainda **não** existe de propósito:

- **Sem Spring Security.** Ele só entra na aula 08. Portanto, **nenhum endpoint exige autenticação**.
- **Sem tratamento de exceções de autenticação** (401) nem de acesso negado (403): isso vem na aula 08, junto com o Spring Security.
- **Sem validação de entrada com `@Valid`.** Fica como desafio para quem terminar cedo (`MethodArgumentNotValidException`, 400).
- **Sem JPA.** A tabela `alunos` usa `JdbcTemplate` porque o SQL Injection precisa de uma consulta escrita à mão. O JPA entra na aula 08, para a entidade de usuário.

## Próxima aula com código

**Aula 08** (Spring Security). **Comece deste projeto, no estado final da Aula 07** (`aula07/source`). Veja `../../aula08/source/README.md`.

## Como executar

```bash
mvn spring-boot:run
```

Teste:

- Lista (rate limit: duas chamadas em menos de 10 s dão 429): `curl -i http://localhost:8080/alunos`
- Aluno inexistente: `curl -i http://localhost:8080/alunos/999`
- SQL Injection: `curl -G --data-urlencode "nome=x' OR '1'='1" http://localhost:8080/alunos/pesquisa/vulneravel`
- Corrigido: `curl -G --data-urlencode "nome=x' OR '1'='1" http://localhost:8080/alunos/pesquisa`
- Erro inesperado: `curl -i http://localhost:8080/erro-teste`

## Testes automatizados

```bash
mvn test
```

O `Aula07Test` cobre o SQL Injection (vulnerável x corrigido), o 404 padronizado, o 500 genérico sem vazamento, o 400 do Spring MVC e o 429 do rate limiter.

> **Ética:** o SQL Injection é demonstrado só neste projeto local, nunca em sistemas de terceiros.
