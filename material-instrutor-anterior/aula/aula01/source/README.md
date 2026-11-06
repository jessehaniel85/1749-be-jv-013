# Aula 01: código de apoio (XSS e rate limit)

Código usado nas demonstrações da [Aula 01](../roteiroaula01.md): Reflected XSS (prática da seção 05 do roteiro) e rate limit contra DoS (seção 07 do roteiro).

## Ponto de partida

**Projeto novo.** Esta é a primeira aula com código e não depende de nenhuma outra. Para dar a aula ao vivo, crie o projeto vazio (Spring Boot 3, Java 17, Maven) e vá digitando os passos abaixo, ou abra este projeto e use-o como gabarito.

Requisitos: Java 17 ou superior e Maven 3.9 ou superior.

## O que a aula faz, na ordem em que acontece

No roteiro, cada ataque tem a teoria e a prática no mesmo tópico. O XSS vem **primeiro** (prática a partir de ~01:30) e o rate limit **depois** (tópico de DoS, ~02:25). Siga essa ordem.

### Parte A: Reflected XSS (prática da seção 05 do roteiro, ~01:30)

| Passo | O que fazer | Onde está |
|---|---|---|
| A1 | Criar o endpoint vulnerável `GET /busca?q=` e testar com `?q=java`. | `xss/BuscaController.java`, método `busca` |
| A2 | Executar o ataque: `/busca?q=<script>alert('XSS')</script>`. | |
| A3 | Corrigir com `HtmlUtils.htmlEscape` e repetir o ataque. | método `buscaSegura` (`/busca/segura`) |
| A4 | Mostrar o código-fonte da página (`Ctrl+U`): aparece `&lt;script&gt;`. | |

> Neste projeto os dois endpoints existem lado a lado (`/busca` vulnerável e `/busca/segura` corrigido) para facilitar a comparação. Em sala, o ideal é digitar o vulnerável e **editar o mesmo método** para virar o corrigido.

### Parte B: rate limit contra DoS (seção 07 do roteiro, ~02:25)

| Passo | O que fazer | Onde está |
|---|---|---|
| B1 | Mostrar `GET /alunos` respondendo sempre, sem limite. (Comente a anotação `@RateLimiter` para ver.) | `aluno/AlunoController.java` |
| B2 | Adicionar as dependências `spring-boot-starter-aop` e `resilience4j-spring-boot3`. | `pom.xml` |
| B3 | Configurar o limite: 1 chamada a cada 10 s, `timeoutDuration: 0s`. | `application.yml` |
| B4 | Anotar o método com `@RateLimiter(name = "alunoService")`. | `AlunoController.java` |
| B5 | Chamar `GET /alunos` duas vezes seguidas: a segunda falha. | |

## Até onde a aula vai

**Esta aula vai até o passo B5 e o código acima é o estado final.** O que ainda **não** existe de propósito:

- **Sem Spring Security.** Ele só entra na aula 08. Por enquanto qualquer um acessa qualquer endpoint.
- **Sem tratamento de exceções.** A segunda chamada a `/alunos` devolve um **500** feio. Isso é intencional: a aula 07 troca esse erro por um `429 Too Many Requests` limpo.
- **Sem banco de dados.** Os alunos são uma lista fixa em memória; o banco (H2) entra na aula 07.
- **Sem código para o CSRF.** O tópico de CSRF é teoria e estudo de caso (discussão), sem implementação.

## Próxima aula com código

**Aula 02** (HTTPS e cookies). **Comece deste projeto, no estado final** (`aula01/source`): copie esta pasta, ou continue neste mesmo projeto. Veja `../../aula02/source/README.md`.

As aulas 03 a 06 **não têm código**.

## Como executar

```bash
mvn spring-boot:run
```

Teste:

- XSS vulnerável: http://localhost:8080/busca?q=java e depois `http://localhost:8080/busca?q=<script>alert('XSS')</script>`
- XSS corrigido: `http://localhost:8080/busca/segura?q=<script>alert('XSS')</script>`
- Rate limit (rode duas vezes em menos de 10 s): `curl -i http://localhost:8080/alunos`

## Testes automatizados

```bash
mvn test
```

Os testes (`Aula01Test`) confirmam que o `/busca` devolve o script sem tratar, que o `/busca/segura` o escapa e que a segunda chamada a `/alunos` dentro da janela é bloqueada.

> **Atenção:** se o seu `~/.m2/settings.xml` define `maven.test.skip=true`, o `mvn test` pula os testes. Use `mvn -Dmaven.test.skip=false test`.

> **Ética:** o ataque é só contra este projeto, na sua máquina. Nunca teste XSS em sites de terceiros.
