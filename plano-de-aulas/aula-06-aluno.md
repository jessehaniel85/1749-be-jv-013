# Material do Aluno — Aula 6: Escrever consultas seguras e responder erros sem vazar informação

> **Quarta, 11/11/2026 · Tempo de leitura:** ~17 min. Até aqui o módulo tratou de **provar quem é o usuário**. Hoje a pergunta é outra: o que a sua API faz com o que recebe, e o que ela conta quando algo dá errado? Uma autenticação perfeita não adianta se a aplicação monta SQL com o texto do usuário ou responde um erro com a pilha inteira de chamadas. São dois lados do mesmo defeito: **confiar demais** no que entra e **revelar demais** no que sai. Código desta aula: pasta `aula07` (API de alunos com H2).

---

## 1. SQL injection: quando o dado vira comando

🎬 **Imagine o cenário.** Um sistema interno de uma empresa tem uma tela de busca de clientes pelo nome. Um estagiário curioso digita, no campo de busca, `x' OR '1'='1`. A tela, que deveria mostrar zero ou um cliente, lista a base inteira. Ninguém invadiu servidor nenhum: a própria aplicação entregou os dados.

**SQL injection** é o ataque em que alguém **altera a consulta SQL** da aplicação colocando comandos dentro de um dado de entrada, que a aplicação concatena na consulta sem tratar. É o mesmo problema do XSS, em outro lugar: um **dado** do usuário passa a ser interpretado como **código**.

Na API de alunos, o repositório tem as duas versões lado a lado (`aula07/source/src/main/java/com/ada/seguranca/aluno/AlunoRepository.java`):

```java
// VULNERÁVEL a SQL Injection: o texto recebido é concatenado na consulta.
// Ataque: nome = x' OR '1'='1   (devolve todos os alunos)
public List<Aluno> pesquisaVulneravel(String nome) {
    String sql = "SELECT * FROM alunos WHERE nome = '" + nome + "'";
    return jdbc.query(sql, MAPPER);
}

// CORRIGIDO: instrução preparada. O SQL e o dado seguem por caminhos separados.
public List<Aluno> pesquisa(String nome) {
    return jdbc.query("SELECT * FROM alunos WHERE nome = ?", MAPPER, nome);
}
```

Com o valor `x' OR '1'='1`, a versão vulnerável manda ao banco:

```sql
SELECT * FROM alunos WHERE nome = 'x' OR '1'='1'
```

Como `'1'='1'` é sempre verdadeiro, a consulta devolve **todos** os alunos. Na versão corrigida, o `?` é um lugar reservado para um **valor**: o banco recebe o SQL e o dado separados, e `x' OR '1'='1` vira apenas um nome estranho que ninguém tem.

✍️ **Experimente agora.** Suba o projeto (`mvn spring-boot:run` dentro de `aula07/source`) e, no Git Bash, rode os dois comandos do README:

```bash
curl -G --data-urlencode "nome=x' OR '1'='1" http://localhost:8080/alunos/pesquisa/vulneravel
curl -G --data-urlencode "nome=x' OR '1'='1" http://localhost:8080/alunos/pesquisa
```

O primeiro devolve os 4 alunos da tabela; o segundo, uma lista vazia. É exatamente o que o `Aula07Test` confere (`pesquisaVulneravelEnganadaPeloSqlInjectionDevolveTodosOsAlunos` espera 4 itens; `pesquisaComInstrucaoPreparadaTrataOAtaqueComoUmNome` espera 0). Se preferir, monte as mesmas requisições no cliente HTTP do IntelliJ.

E o ataque mais famoso, que encerra a consulta e apaga a tabela (`x'; TRUNCATE TABLE alunos; --`)? Muitos drivers e configurações **não executam vários comandos** numa chamada só, então ele pode não funcionar no seu ambiente. Isso **não torna a aplicação segura**: o `OR '1'='1'` já vazou todos os dados.

> **Ética:** o ataque é feito só neste projeto local, nunca em sistemas de terceiros.

### "Com JPA eu já estou protegido?"

Depende de **como** a consulta é escrita. O que protege não é o framework, é o **parâmetro**.

| Forma de consultar | Situação |
|---|---|
| `JdbcTemplate` com `?` e o valor passado à parte | segura (é o `pesquisa` acima) |
| Spring Data: método derivado do nome, como `findByUsername(String username)` | segura: o Spring Data gera a consulta com parâmetro |
| `@Query` com parâmetro nomeado (`:nome`) ou posicional (`?1`) | segura |
| JPQL ou SQL nativo montado com `+` (`createQuery`, `createNativeQuery`, `@Query` com texto concatenado) | **vulnerável**, mesmo usando JPA |

Ou seja: JPA e Spring Data protegem **quando a consulta é parametrizada**, que é o padrão quando você usa os métodos derivados e o `@Query` com parâmetros. Basta uma concatenação num ponto qualquer para o problema voltar.

Um caso que confunde: a busca por **parte** de um texto (`LIKE`). Os `%` podem ser colados ao **valor** antes de passá-lo como parâmetro (o dado continua indo por fora do SQL); o que não pode é colar o valor ao **texto da consulta**.

**Outras defesas, em camadas:** validar o formato da entrada (tamanho, tipo, caracteres esperados); conectar ao banco com um usuário de **privilégios mínimos** (a aplicação não precisa de `DROP TABLE`); e não devolver ao usuário a mensagem de erro do banco, que é o assunto da seção 3.

💭 **Pare e reflita.** No sistema em que você trabalha hoje, existe algum ponto em que uma consulta é montada juntando textos, nem que seja um filtro dinâmico de relatório? Como você descobriria isso sem ler o código inteiro?

---

## 2. Dependências vulneráveis e o OWASP Top 10

Quantas bibliotecas que você não escreveu existem na última aplicação em que trabalhou? Quase todo o código. Uma falha numa dependência passa a ser uma falha na **sua** aplicação.

O caso mais conhecido é o **Log4Shell** (CVE-2021-44228), divulgado em **dezembro de 2021**: na biblioteca de logs Apache Log4j 2, escrever certos textos no log (um valor que o usuário controla, como um cabeçalho) levava a aplicação a **baixar e executar código** de um servidor do atacante. Foi grave justamente por estar em toda parte; empresas como Adobe, Cisco e Fortinet publicaram correções.

Uma nuance: o Spring Boot usa por padrão o **Logback**, não o Log4j 2. Uma aplicação Spring Boot só era afetada se tivesse trocado o log para Log4j 2 ou se outra dependência o trouxesse. A lição é a mesma: **você precisa saber o que tem na sua aplicação**.

Como se proteger:

- **Enxergar a árvore** de dependências: `mvn dependency:tree`.
- **Cruzar com bases públicas de vulnerabilidades** (CVE), com o OWASP Dependency-Check: `mvn org.owasp:dependency-check-maven:check`. A primeira execução baixa uma base grande e demora.
- **Receber alertas e atualizações**: num repositório do GitHub, o Dependabot faz isso com um arquivo pequeno (`aula07/source/dependabot.yml.exemplo`, que vai em `.github/dependabot.yml`):

```yaml
version: 2
updates:
  - package-ecosystem: "maven"
    directory: "/"
    schedule:
      interval: "weekly"
```

A **OWASP** publica de tempos em tempos a lista dos 10 maiores riscos de aplicações web. Na edição de 2021 (confira no site se já há uma mais recente), várias categorias já passaram por este módulo:

| Categoria (2021) | Onde aparece no módulo |
|---|---|
| Broken Access Control (a primeira da lista) | regra de propriedade do projeto: cada um só vê os próprios chamados |
| Cryptographic Failures | HTTPS, hash de senha, assinatura do JWT |
| Injection | SQL injection (hoje) e XSS (refletido, armazenado e baseado em DOM) |
| Vulnerable and Outdated Components | dependências (esta seção) |
| Identification and Authentication Failures | autenticação com JWT (próxima aula) |

💭 **Pare e reflita.** Quem, na sua equipe de trabalho, é responsável por atualizar uma biblioteca quando sai um alerta de segurança? Existe esse dono?

---

## 3. Exceções: o que a sua API revela quando algo dá errado?

Uma **exceção** é um evento inesperado durante a execução, resultado de uma condição de erro ou de uma entrada inesperada; em Java, exceções são objetos. Se ninguém a captura, a execução é interrompida (numa API, a requisição falha). A questão é **como** ela é tratada. São três critérios para um tratamento seguro:

1. **Todas as exceções são detectadas e tratadas.**
2. O sistema **não fica num estado inseguro**.
3. Nenhuma resposta associada ao erro **divulga informação sigilosa** ou detalhe interno.

O critério 2 tem nome: **falhar para o lado seguro** (*fail secure*). Pense num login que consulta o banco para conferir a senha, e o banco está fora do ar. Se o `catch` devolver "pode entrar" porque "não deu para checar", o erro virou uma porta aberta. Em caso de falha, **nega** o acesso e **registra** o problema no log.

E o critério 3: uma resposta com *stack trace* mostra nomes de classes, versões, caminhos de pacote e, às vezes, o próprio SQL. É um **mapa para o atacante**. A regra:

| Para quem | O que recebe |
|---|---|
| Quem chamou a API | mensagem **genérica**, com o status HTTP correto |
| O log do servidor | o **detalhe técnico completo**, que só a equipe vê |

Para o `try-catch`, dois hábitos: **capture a exceção específica** que você sabe tratar (não um `Exception` genérico em todo lugar) e use o `finally` (ou o *try-with-resources*) para liberar recursos, porque ele roda com ou sem erro.

### Exceção própria: dar nome ao erro

Na API de alunos, `GET /alunos/999` começa devolvendo 500: o serviço fazia `repository.findById(id).get()`, e o `Optional` vazio lança `NoSuchElementException`. Um aluno que não existe não é um erro do servidor; é um **404**. A correção é dar nome ao erro (`exception/AlunoNaoEncontradoException.java`):

```java
public class AlunoNaoEncontradoException extends RuntimeException {

    public AlunoNaoEncontradoException(Long id) {
        super("Aluno " + id + " não encontrado");
    }
}
```

E lançá-lo no serviço (`aluno/AlunoService.java`):

```java
public Aluno buscaPorId(Long id) {
    return repository.findById(id)
            .orElseThrow(() -> new AlunoNaoEncontradoException(id));
}
```

💭 **Pare e reflita.** Qual é o status HTTP adequado para um aluno que não existe? E para quem errou a senha? E para uma falha inesperada do servidor? E se o aluno existe, mas pertence a outra pessoa?

---

## 4. Tratamento de exceções no Spring

O Spring permite tirar o tratamento de dentro da regra de negócio. Há dois níveis.

**No controller**, com `@ExceptionHandler`: vale só para aquele controller. O bloco está comentado no fim de `aluno/AlunoController.java`, para você descomentar e ver o 404:

```java
// @ExceptionHandler(AlunoNaoEncontradoException.class)
// public ResponseEntity<String> naoEncontrado(AlunoNaoEncontradoException ex) {
//     return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
// }
```

Para casos simples, sem criar classe, existe também a `ResponseStatusException`, que já carrega o status.

**Global**, com `@RestControllerAdvice`: um único lugar trata as exceções de **todos** os controllers, e a resposta de erro fica sempre no mesmo formato. O corpo padronizado é um `record` (`exception/ErroResponse.java`):

```java
public record ErroResponse(int status, String erro, String mensagem, Instant timestamp) {
}
```

E o tratamento global, em trechos (`exception/GlobalExceptionHandler.java`):

```java
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AlunoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(AlunoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Rede de segurança: o detalhe técnico vai só para o log, o usuário vê uma frase genérica.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno. Tente novamente mais tarde.");
    }

    // Formato único também para os erros do Spring MVC tratados pela classe-base.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String erro = status != null ? status.getReasonPhrase() : "Erro";
        ErroResponse corpo = new ErroResponse(
                statusCode.value(), erro, mensagemGenerica(statusCode), Instant.now());
        return ResponseEntity.status(statusCode).headers(headers).body(corpo);
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem) {
        ErroResponse corpo = new ErroResponse(
                status.value(), status.getReasonPhrase(), mensagem, Instant.now());
        return ResponseEntity.status(status).body(corpo);
    }
}
```

Três coisas para reparar:

- **A rede de segurança.** O handler de `Exception` é o último recurso: o erro completo vai para o **log** e a resposta tem só uma frase. Teste com `curl -i http://localhost:8080/erro-teste`: o endpoint lança de propósito uma exceção cuja mensagem cita `jdbc:h2:mem:testdb` (`ErroTesteController`), e a resposta não traz nada disso. O teste `erroInesperadoDevolveMensagemGenericaSemVazarDetalhes` confere que o corpo não contém `jdbc` nem `IllegalStateException`. Olhe o console da aplicação: o detalhe está lá.
- **A armadilha do handler genérico.** Um handler de `Exception` pega **tudo**, inclusive exceções do próprio Spring MVC que já têm o status certo. O exemplo é `/boas-vindas/estrito`, da aula de cookies: sem o cookie, ele deve responder **400**. Com um `@RestControllerAdvice` que não estende nada, vira **500**. Estender `ResponseEntityExceptionHandler` devolve a essas exceções o status correto (400, 404, 405, 415...), e o `handleExceptionInternal` sobrescrito põe todas no mesmo formato. O teste `erroDoSpringMvcMantemOStatus400EmVezDeVirar500` prova isso.
- **O 429 do rate limiter.** O limitador de `/alunos` lança `RequestNotPermitted`, que agora tem handler próprio e vira **429 Too Many Requests**. Chame `curl -i http://localhost:8080/alunos` duas vezes em menos de 10 segundos. Repare na configuração (`application.yml`): `limitForPeriod: 1` e `limitRefreshPeriod: 10s` numa única instância, `alunoService`. É um limite **global**, compartilhado por todos os clientes, e não por pessoa. Limitar por cliente exige separar as contagens por alguma chave (IP, usuário).

Os status deste projeto, conferidos pelo `Aula07Test`:

| Chamada | Status | `mensagem` |
|---|---|---|
| `GET /alunos/999` | 404 | `Aluno 999 não encontrado` |
| `GET /erro-teste` | 500 | `Erro interno. Tente novamente mais tarde.` |
| `GET /boas-vindas/estrito` sem cookie | 400 | `Requisição inválida.` |
| segunda `GET /alunos` em menos de 10 s | 429 | `Muitas requisições. Tente novamente em instantes.` |

💭 **Pare e reflita.** Na próxima aula entra o Spring Security, que lança as próprias exceções para "não autenticado" e "acesso negado". O que acontece com elas se o único handler for o de `Exception`?

---

## 5. Para o projeto

Hoje é dia de levar três coisas para a **API de Chamados**. Elas correspondem à linha "Aula 07" do cronograma (seção 12 da especificação) e ao critério "Entrada e erros" da rubrica (seção 14).

**1. Tratamento global no formato da seção 09.** O `ErroResponse` da aula tem exatamente os campos que a especificação pede (`status`, `erro`, `mensagem`, `timestamp`). Esta é a resposta de exemplo da seção 09:

```
{
    "status": 404,
    "erro": "Not Found",
    "mensagem": "Chamado 10 não encontrado.",
    "timestamp": "2026-11-17T10:45:02.331"
}
```

- Crie a exceção própria do chamado inexistente e use as **mensagens da tabela da seção 09** (por exemplo, `Chamado {id} não encontrado.` e `Erro interno. Tente novamente mais tarde.`).
- Mantenha a rede de segurança (`Exception` → 500 genérico, detalhe no log) e estenda `ResponseEntityExceptionHandler`.
- Atenção ao "ponto de atenção" da seção 09: o handler genérico **não pode engolir** as exceções de autenticação e transformá-las em 500. Na próxima aula vocês acrescentam o handler do 401.

**2. Validação de entrada com `@Valid` e resposta 400.** A seção 08 dá as regras: `titulo` de 5 a 100 caracteres, `descricao` até 2.000, `prioridade` e `status` só com os valores do enum; `nome`, `username` e senha do usuário também têm regras. Anote o DTO de entrada com as restrições do Bean Validation (`@NotBlank`, `@Size`, `@Pattern`...) e o parâmetro do controller com `@Valid`. A dependência é a `spring-boot-starter-validation` (o projeto `aula07` não a tem). Quando a validação falha, o Spring lança `MethodArgumentNotValidException`, que o `ResponseEntityExceptionHandler` já transforma em 400. A seção 09 pede mais: a mensagem deve dizer **qual campo** está inválido, sem detalhe interno. Para isso, sobrescreva o tratamento dessa exceção na classe-base e monte a mensagem com os campos que falharam. Teste também um `status` fora do enum (no filtro e no `PATCH`): tem que dar 400, nunca 500.

**3. Busca por título parametrizada.** O UC4 (listar os meus chamados) aceita "parte do `titulo`". Use um método derivado do Spring Data ou um `@Query` com parâmetro; se escrever SQL, use `?`. Nenhuma concatenação no texto da consulta. E já deixe a busca **filtrando pelo dono** do chamado, que vem do token a partir da próxima aula (seção 06, "Fluxo de `GET /chamados/{id}`").

**No mapa de ameaças (seção 10)**, preencham desde já três linhas:

| Linha do mapa | O que escrever no README |
|---|---|
| SQL Injection | onde está a busca por título e por que ela é parametrizada |
| Vazamento por mensagem de erro | como o tratamento global evita *stack trace* e onde fica o detalhe (log) |
| Dependências vulneráveis (extra) | se a equipe escolher este extra (seção 15): a ferramenta usada e o resultado registrado |

💭 **Pare e reflita.** Na apresentação, qualquer integrante pode ouvir: "o que a sua API devolve se eu mandar um título com 3 letras?" ou "onde está a consulta da busca?". Todos do grupo sabem responder?

---

## Para praticar

- **Listas de exercícios no LMS:** tratamento de exceção com bloco `try-catch` e tratamento de exceção global.
- **Prática em casa:** na API de Chamados, uma exceção própria e um tratamento global que devolva o JSON padronizado, sem *stack trace*.
- **Leitura:** o OWASP Top 10 (https://owasp.org/www-project-top-ten/). Escolha uma categoria e explique-a em 3 linhas.
- **Leitura:** "Error Handling for REST with Spring" (Baeldung): https://www.baeldung.com/exception-handling-for-rest-with-spring
- **Preparação para a próxima aula:** Java e IDE funcionando e o projeto `aula07` rodando. Vamos acrescentar o Spring Security a ele.

> **Na próxima aula:** a API deixa de ser aberta. A cada requisição ela vai perguntar "quem é você?", com autenticação básica primeiro e JWT depois, com `POST /user` e `POST /authenticate`.
