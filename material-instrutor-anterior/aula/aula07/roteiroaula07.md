# Aula 07: Situações reais de vulnerabilidade e Tratamento de exceções

**Roteiro de Aula · Módulo Fundamentos de segurança para aplicações (BE-JV-013)**

A aula em que a segurança vira **código**. A turma vê duas vulnerabilidades reais (SQL Injection e dependências vulneráveis), aprende a tratar exceções de forma segura (sem vazar informações e sem abrir acesso por engano) e implementa exceções próprias e tratamento global em uma API Spring.

**Data:** 13/11/2026 · sexta &nbsp;·&nbsp; **Duração:** 3h &nbsp;·&nbsp; **Formato:** expositiva + demonstração ao vivo + prática guiada

**Código:** [`source/`](source/README.md) · **Ponto de partida:** estado final da Aula 03 (`../aula03/source`), pois as aulas 04 a 06 não têm código · **Próxima aula com código:** Aula 08, que começa do estado final deste projeto.

> **Problema gerador:** Tratamento de exceções: o que a sua API revela quando algo dá errado?

---

## Sumário

1. [Visão geral](#01--visão-geral)
2. [Retomada da aula 06](#02--retomada-da-aula-06)
3. [SQL Injection](#03--sql-injection)
4. [Dependências vulneráveis e OWASP Top 10](#04--dependências-vulneráveis-e-owasp-top-10)
5. [Exceções e tratamento seguro](#05--exceções-e-tratamento-seguro)
6. [Exceções no Spring](#06--exceções-no-spring)
7. [Prática guiada](#07--prática-guiada)
8. [Encerramento](#08--encerramento)
9. [Dicas de condução](#09--dicas-de-condução)

---

## 01 · Visão geral

Tempos relativos ao início da aula, ajuste ao seu horário real. A aula tem dois assuntos que parecem separados. Costure-os com a pergunta do problema gerador: tanto o SQL Injection quanto uma exceção mal tratada acontecem quando a aplicação **confia demais** no que recebe ou **revela demais** no que responde. Se faltar tempo, encurte a parte de dependências (seção 04) e proteja a prática.

| Hora | Duração | Bloco |
|---|---|---|
| 00:00 | 10 min | **Retomada** da aula 06 e da atividade de casa |
| 00:10 | 30 min | **Teoria + demo**: SQL Injection |
| 00:40 | 25 min | **Teoria**: dependências vulneráveis e OWASP Top 10 |
| 01:05 | 10 min | Intervalo |
| 01:15 | 30 min | **Teoria**: exceções, try-catch-finally, exceções próprias e tratamento seguro |
| 01:45 | 20 min | **Teoria + código**: tratamento de exceções no Spring |
| 02:05 | 35 min | **Prática guiada**: implementar na API de alunos e exercícios |
| 02:40 | 20 min | **Encerramento**: recap, atividade de casa, preview da próxima aula |

## 02 · Retomada da aula 06

*~10 min*

- "O que o OpenID Connect acrescenta ao OAuth?" (A autenticação: o ID Token.)
- "Qual a frase que resume OAuth, OIDC, JWT e SSO?" (OAuth autoriza, OIDC autentica sobre ele, JWT é o formato, SSO é a experiência.)
- "O que o cliente valida no ID Token?" (Assinatura, `iss`, `aud`, `exp`, `nonce`.)

Peça a 2 alunos que leiam o texto de 10 linhas que escreveram sobre a relação entre JWT, OAuth, SSO e OIDC. Cinco minutos no máximo.

**Gancho:** "Até aqui, vimos **como provar quem o usuário é**. Mas de nada adianta uma autenticação perfeita se a aplicação aceita qualquer entrada sem tratar e responde com a pilha de erros inteira. Hoje, o código."

## 03 · SQL Injection

*~30 min*

### Por que importa

Em 2017, o SQL Injection era a **vulnerabilidade n.º 1** entre os 10 principais riscos de segurança de aplicações web da OWASP. Em 2021, apesar de não ser mais a primeira, continuava entre as três principais (agrupada na categoria **Injection**).

### O que é

> O invasor **altera a consulta SQL** da aplicação, colocando comandos SQL dentro de um dado de entrada que a aplicação concatena na consulta sem tratar.

É o mesmo problema do XSS da aula 01, em outro lugar: a aplicação deixa que um **dado** do usuário seja interpretado como **código**.

### Demonstração ao vivo (~20 min)

Parta do estado final da aula 03 (`../aula03/source`) e acrescente `spring-boot-starter-jdbc` e o banco H2 em memória, com uma tabela `alunos (id, nome, email)` e 4 registros. Tudo isso já está pronto em [`source/`](source/README.md), com o passo a passo na ordem da aula; deixe-o rodando antes da aula.

**1. Escreva o código vulnerável** e peça que a turma diga o que há de errado antes de executar:

```java
@GetMapping("/alunos/pesquisa")
public List<Map<String, Object>> pesquisa(@RequestParam String nome) {
    String sql = "SELECT * FROM alunos WHERE nome = '" + nome + "'";
    return jdbcTemplate.queryForList(sql);
}
```

**2. Uso normal:** `/alunos/pesquisa?nome=Batman` devolve o aluno esperado.

**3. O ataque:** `/alunos/pesquisa?nome=x' OR '1'='1`. A consulta que chega ao banco passa a ser:

```sql
SELECT * FROM alunos WHERE nome = 'x' OR '1'='1'
```

Como `'1'='1'` é sempre verdadeiro, a consulta devolve **todos os alunos**. Pergunte: "e se a tabela fosse a de usuários com senha?"

**4. O ataque do material:** o material de apoio mostra um valor que vai além, encerrando a consulta e apagando a tabela:

```
nome = batman"; TRUNCATE TABLE alunos; --
```

> **Ponto de atenção:** muitos drivers e configurações **não executam vários comandos** em uma única chamada, então esse segundo ataque pode não funcionar no seu ambiente. Teste antes e, se não funcionar, diga isso à turma: **isso não torna a aplicação segura**. O `OR '1'='1'` já vaza todos os dados.

**5. A correção: instruções preparadas (consultas parametrizadas).** O SQL e os dados seguem por **caminhos separados**; o banco nunca interpreta o dado como comando:

```java
String sql = "SELECT * FROM alunos WHERE nome = ?";
return jdbcTemplate.queryForList(sql, nome);
```

**6. Repita o ataque.** O valor `x' OR '1'='1` agora é tratado como um **nome** qualquer, e a busca não encontra nada.

### Com JPA e Spring Data

- **Seguro por padrão:** métodos derivados (`findByNome(String nome)`) e `@Query` com parâmetros nomeados.
- **Onde o problema volta:** quando alguém monta uma consulta nativa **concatenando texto**.

```java
// Seguro
@Query("SELECT a FROM Aluno a WHERE a.nome = :nome")
List<Aluno> buscar(@Param("nome") String nome);

// Vulnerável: concatenação em consulta nativa
entityManager.createNativeQuery("SELECT * FROM alunos WHERE nome = '" + nome + "'");
```

### Outras defesas (em camadas)

- **Validar** o formato da entrada (tamanho, tipo, caracteres esperados).
- Conectar-se ao banco com um usuário de **privilégios mínimos** (a aplicação não precisa de `DROP TABLE`).
- Não devolver ao usuário a mensagem de erro do banco (liga com o tratamento de exceções, logo abaixo).

## 04 · Dependências vulneráveis e OWASP Top 10

*~25 min*

### Dependências com vulnerabilidades

Pergunte: "quantas bibliotecas que vocês não escreveram existem na última aplicação em que trabalharam?" A resposta é: **quase todo o código**. Uma falha em uma dependência passa a ser uma falha na sua aplicação.

**O caso Log4j (Log4Shell), dezembro de 2021:**

- A biblioteca de logs **Apache Log4j 2**, usada em milhões de aplicações Java, tinha uma falha (**CVE-2021-44228**) em que **escrever certos textos no log** (por exemplo, um valor que o usuário controla, como um cabeçalho ou um campo de formulário) levava a aplicação a **baixar e executar código** de um servidor do invasor.
- Foi considerada uma das vulnerabilidades mais graves dos últimos anos justamente por estar em toda parte.
- O material cita empresas afetadas, como Adobe, Cisco e Fortinet, que precisaram publicar correções.

> **Nuance para quem usa Spring Boot:** o Spring Boot usa por padrão o **Logback**, não o Log4j 2. Aplicações só eram afetadas se tivessem trocado o log para Log4j 2, ou se alguma outra dependência o trouxesse. Isso reforça a lição: **você precisa saber o que tem na sua aplicação**.

### Como se proteger

- **Manter as dependências atualizadas** e acompanhar os avisos de segurança.
- **Ferramentas que identificam pacotes vulneráveis:**
  - o **Dependabot**, do GitHub, que alerta e abre *pull requests* de atualização;
  - o **OWASP Dependency-Check**, que cruza as dependências com bases públicas de vulnerabilidades (CVE).
- **Enxergar a árvore de dependências** do projeto (`mvn dependency:tree`).

Mostre, se houver tempo, o arquivo mínimo que ativa o Dependabot em um repositório Maven:

```yaml
# .github/dependabot.yml
version: 2
updates:
  - package-ecosystem: "maven"
    directory: "/"
    schedule:
      interval: "weekly"
```

E o comando que verifica vulnerabilidades pelo Maven:

```bash
mvn org.owasp:dependency-check-maven:check
```

> **Dica:** a primeira execução baixa uma grande base de dados e pode demorar. Rode antes da aula, não ao vivo.

### OWASP Top 10

A **OWASP** publica uma lista, atualizada de tempos em tempos, com os 10 maiores riscos de segurança em aplicações web. A edição de 2021 (a citada no material) inclui, entre outros:

- **Broken Access Control** (controle de acesso quebrado), a primeira da lista;
- **Cryptographic Failures** (falhas criptográficas), assunto das aulas de HTTPS e JWT;
- **Injection**, que reúne o SQL Injection e o XSS;
- **Vulnerable and Outdated Components** (componentes vulneráveis e desatualizados), as dependências;
- **Identification and Authentication Failures** (falhas de identificação e autenticação), o tema do projeto.

> Confira em https://owasp.org/www-project-top-ten/ se há uma edição mais recente antes de apresentar os números.

**Pergunta para a turma (2 min):** "Quais categorias da lista a gente já viu em aula nesta semana?" (Várias: o módulo inteiro passa por ela.)

## 05 · Exceções e tratamento seguro

*~30 min*

### Exceções

> Exceções são **eventos inesperados** que acontecem durante a execução de um programa, resultado de uma condição de erro ou de uma entrada inesperada. Em Java, **exceções são objetos**.

Ponto-chave: se uma exceção **não for capturada**, o programa **encerra** (em uma API, a requisição falha). Isso é bom ou ruim? Depende de **como** ela é tratada.

### O que é tratar uma exceção com segurança

Três critérios (do material):

1. **Todas as exceções devem ser detectadas e tratadas.**
2. O sistema deve ser projetado para **não deixar um estado inseguro.**
3. Nenhuma comunicação associada à exceção deve **divulgar informações sigilosas** (ou detalhes de transações internas).

**Exemplo para a turma pensar:** o sistema se conecta ao banco para verificar as credenciais do usuário, e o banco está fora do ar.

```java
// ERRADO: em caso de erro, libera o acesso
boolean autentica(String usuario, String senha) {
    try {
        return repositorio.confere(usuario, senha);
    } catch (Exception e) {
        return true;   // "não deu para checar, então deixa entrar"
    }
}

// CERTO: em caso de erro, nega o acesso e registra o problema
boolean autentica(String usuario, String senha) {
    try {
        return repositorio.confere(usuario, senha);
    } catch (DataAccessException e) {
        log.error("Falha ao consultar credenciais", e);
        return false;
    }
}
```

Princípio: **em caso de falha, a aplicação deve falhar para o lado seguro** (*fail secure*).

### O que a resposta de erro revela

Mostre (ou descreva) uma resposta de erro com **stack trace**: nomes de classes, versões, caminhos de pacote e, às vezes, o texto da consulta SQL. É um **mapa para o invasor**. A regra:

- **Para o usuário:** uma mensagem **genérica e amigável**, com o status HTTP correto.
- **Para o log do servidor:** o **detalhe técnico completo**, que só a equipe vê.

### try-catch-finally

```java
try {
    // código que pode lançar uma exceção
    int idade = Integer.parseInt(entrada);
} catch (NumberFormatException e) {
    // o que fazer se ocorrer o erro
    System.out.println("Informe um número válido");
} finally {
    // sempre executa, com ou sem exceção (ex.: liberar recursos)
    System.out.println("Fim da leitura");
}
```

- `try`: o trecho que pode lançar a exceção.
- `catch`: o que fazer se o erro acontecer. **Capture a exceção específica**, e não um `Exception` genérico em tudo.
- `finally`: opcional; roda **sempre**, tratada ou não.

### Exceções próprias

Criar exceções próprias dá **significado** ao erro e permite devolver uma mensagem mais amigável ao usuário:

```java
public class AlunoNaoEncontradoException extends RuntimeException {
    public AlunoNaoEncontradoException(Long id) {
        super("Aluno " + id + " não encontrado");
    }
}
```

```java
public Aluno buscaPorId(Long id) {
    return repository.findById(id)
            .orElseThrow(() -> new AlunoNaoEncontradoException(id));
}
```

**Pergunta para a turma (2 min):** "Qual é o status HTTP adequado para um aluno que não existe? E para o usuário que errou a senha? E para uma falha inesperada do servidor?" (404, 401, 500.)

## 06 · Exceções no Spring

*~20 min*

O Spring permite tratar exceções além do `try-catch`, de forma que o tratamento saia do corpo da regra de negócio.

### Tratamento no Controller: `@ExceptionHandler` e `ResponseStatusException`

```java
@RestController
@RequestMapping("/alunos")
public class AlunoController {

    @GetMapping("/{id}")
    public Aluno buscaPorId(@PathVariable Long id) {
        return service.buscaPorId(id);
    }

    // trata só as exceções deste controller
    @ExceptionHandler(AlunoNaoEncontradoException.class)
    public ResponseEntity<String> naoEncontrado(AlunoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
```

Para casos simples, sem criar uma classe de exceção, `ResponseStatusException` lança a exceção já com o status:

```java
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado");
```

### Tratamento global: `@RestControllerAdvice`

Um único lugar trata as exceções de **todos** os controllers, e a resposta de erro fica **padronizada**:

```java
public record ErroResponse(int status, String erro, String mensagem, Instant timestamp) {}

@RestControllerAdvice
public class GlobalExceptionHandler /* depois: extends ResponseEntityExceptionHandler (veja a nota abaixo) */ {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AlunoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(AlunoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // o rate limiter da aula 01 lança esta exceção
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErroResponse> muitasRequisicoes(RequestNotPermitted ex) {
        return resposta(HttpStatus.TOO_MANY_REQUESTS,
                "Muitas requisições. Tente novamente em instantes.");
    }

    // rede de segurança: detalhe só no log, mensagem genérica na resposta
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno. Tente novamente mais tarde.");
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem) {
        ErroResponse corpo = new ErroResponse(
                status.value(), status.getReasonPhrase(), mensagem, Instant.now());
        return ResponseEntity.status(status).body(corpo);
    }
}
```

**Pontos para destacar:**

- A resposta tem **sempre o mesmo formato**, o que facilita a vida de quem consome a API.
- O handler `Exception.class` é a **rede de segurança**: o erro completo vai para o **log**, e o usuário vê só uma frase genérica, **sem stack trace**.
- Fecha a promessa da aula 01: o `RequestNotPermitted` do rate limiter agora vira um **429 Too Many Requests** limpo.

> **Ponto de atenção (e a armadilha da aula 02):** um handler para `Exception` pega **tudo**, inclusive as exceções do próprio Spring MVC, que já têm um status certo. O exemplo é o `400` do cookie ausente de `/boas-vindas/estrito` (aula 02): com o handler acima **ele vira 500**. Mostre isso ao vivo: chame o endpoint, veja o 500 e só então corrija.
>
> **A correção:** fazer a classe **estender `ResponseEntityExceptionHandler`**, que já trata as exceções do Spring MVC com o status correto (400, 404, 405...), e sobrescrever `handleExceptionInternal` para que elas também saiam no formato do `ErroResponse`. É o que está no código de [`source/`](source/README.md). Na aula 08 vale a mesma atenção: acesso negado (403) lançado por `@PreAuthorize` também seria engolido por um handler genérico de `Exception`.

## 07 · Prática guiada

*~35 min*

Projeto-base: a **API de alunos** com H2 de [`source/`](source/README.md) (a mesma da demonstração de SQL Injection), compartilhada com a turma antes da aula. Os passos C1 a C7 do README correspondem às etapas abaixo.

### Parte 1: Implementar o tratamento de exceções (~20 min)

Em duplas, sobre a API de alunos:

1. **Reproduza o problema:** chame `GET /alunos/999`, um aluno que não existe. O que a API devolve hoje? (Provavelmente um 500 ou um erro genérico do Spring.)
2. **Crie a exceção própria** `AlunoNaoEncontradoException` e lance-a no serviço.
3. **Primeiro, trate no controller** com `@ExceptionHandler` e confira o 404.
4. **Depois, mova para o tratamento global** com `@RestControllerAdvice` e o `ErroResponse` padronizado.
5. **Rede de segurança:** force um erro inesperado (por exemplo, um `NullPointerException` proposital) e confirme que a resposta é genérica e que o detalhe aparece **só no log**.
6. **Rate limiter:** acrescente um handler para `RequestNotPermitted` (429): a API da aula 01 já tem o rate limiter. Chame `/alunos` duas vezes seguidas.
7. **A armadilha:** chame `/boas-vindas/estrito` (o 400 da aula 02). Com o handler genérico de `Exception`, ele vira 500. Corrija estendendo `ResponseEntityExceptionHandler`.

**Perguntas de verificação:** "a resposta de erro revela o nome de alguma classe? O caminho de algum arquivo? O SQL?" Se sim, algo está vazando.

### Parte 2: Listas de exercícios (~15 min)

- Lista de exercícios para implementação de tratamento de exceção com bloco **try-catch**.
- Lista de exercícios para implementação de tratamento de exceção **global**.

Dê o tempo para a turma começar e deixe o que sobrar como atividade de casa.

## 08 · Encerramento

*~20 min*

Recap de 3 minutos, **feito pela turma**: como o SQL Injection funciona e como as consultas parametrizadas o impedem; por que as dependências são um risco e o que o Log4Shell ensinou; os três critérios do tratamento seguro de exceções; a diferença entre tratar no controller e globalmente; o que **nunca** deve aparecer na resposta de erro.

Feche amarrando com o módulo: "Depois de hoje, a API sabe se defender de entradas maliciosas e responder erros sem vazar nada. Falta o essencial: **saber quem está chamando**. Na próxima aula, o **Spring Security** e a autenticação com **JWT**, que é o projeto do módulo".

**Atividade para casa:**
- **Prática:** na API de alunos (ou na API que será usada no projeto), criar uma exceção própria e um tratamento global que devolva um **JSON padronizado, sem stack trace**.
- **Leitura:** o OWASP Top 10 (https://owasp.org/www-project-top-ten/): escolher uma categoria e explicá-la em 3 linhas.
- **Leitura:** "Error Handling for REST with Spring" (Baeldung): https://www.baeldung.com/exception-handling-for-rest-with-spring
- **Exercícios de fixação:** o que sobrou das listas.
- **Preparação para a aula 08:** deixar o ambiente com Java e uma IDE funcionando e a API de alunos rodando; vamos adicionar o Spring Security a ela.

**Preview da Aula 08:** Spring Security. Autenticação básica e, em seguida, autenticação com JWT, com `POST /user` e `POST /authenticate`.

## 09 · Dicas de condução

- **Sempre ataque o código que você acabou de mostrar:** o SQL Injection só "pega" quando a turma vê o erro acontecer na própria tela e depois vê a correção funcionar. Evite só mostrar slides.
- **Deixe a demonstração pronta e testada,** inclusive o ataque que falha (`TRUNCATE`), para você não ser surpreendido ao vivo.
- **Ética:** reforce, como na aula 01, que o ataque é feito em um projeto **local**, nunca em sistemas de terceiros.
- **Se a turma estiver cansada do volume de código,** priorize: SQL Injection + exceção própria + handler global. As dependências podem ser tratadas só conceitualmente.
- **Alunos mais rápidos:** podem implementar `MethodArgumentNotValidException` (400, com `@Valid`) ou usar o `ProblemDetail` do Spring 6 como formato de erro.
- **Itens do planejamento cobertos:** situações reais de vulnerabilidade (SQL Injection, dependências vulneráveis), entendendo exceções baseadas em Controller, manipulação global de exceções, implementação de tratamento com try-catch, exceções próprias, tratamento global e as duas listas de exercícios.

---

*Roteiro de aula: módulo de Fundamentos de segurança para aplicações. Aula 07 de 09.*
