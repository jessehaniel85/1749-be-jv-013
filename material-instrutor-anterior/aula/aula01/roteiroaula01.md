# Aula 01: Introdução à segurança de aplicações (XSS, CSRF e DoS)

**Roteiro de Aula · Módulo Fundamentos de segurança para aplicações (BE-JV-013)**

Primeiro dia de módulo. Por que proteger uma aplicação, e os três ataques mais comuns a aplicações web: Cross-site Scripting (XSS), Cross-site Request Forgery (CSRF) e Denial-of-Service (DoS). A teoria é acompanhada de prática em ambiente controlado.

**Data:** 28/10/2026 · quarta &nbsp;·&nbsp; **Duração:** 3h &nbsp;·&nbsp; **Formato:** expositiva + dinâmica em sala + prática guiada

**Código:** [`source/`](source/README.md) · **Ponto de partida:** projeto novo (primeira aula com código) · **Próxima aula com código:** Aula 02, que começa do estado final deste projeto.

> **Problema gerador:** Introdução à segurança da informação: o que acontece quando uma aplicação não é protegida?

---

## Sumário

1. [Visão geral](#01--visão-geral)
2. [Abertura](#02--abertura)
3. [Rodada com a turma](#03--rodada-com-a-turma)
4. [Por que proteger aplicativos](#04--por-que-proteger-aplicativos)
5. [Cross-site Scripting (XSS)](#05--cross-site-scripting-xss)
6. [Cross-site Request Forgery (CSRF)](#06--cross-site-request-forgery-csrf)
7. [Denial-of-Service (DoS)](#07--denial-of-service-dos)
8. [Encerramento](#08--encerramento)
9. [Dicas de condução](#09--dicas-de-condução)

---

## 01 · Visão geral

Tempos relativos ao início da aula, ajuste ao seu horário real. **Cada ataque (seções 05, 06 e 07) tem a teoria e a prática no mesmo tópico**: primeiro a turma entende, logo em seguida pratica. Os blocos têm folga embutida; se a rodada de apresentação render mais que o previsto, corte tempo da teoria de DoS (seção 07) e não da prática, é a prática que fixa o conteúdo.

| Hora | Duração | Bloco |
|---|---|---|
| 00:00 | 15 min | **Abertura**: quem sou eu, como funciona o módulo e o projeto |
| 00:15 | 20 min | **Rodada com a turma**: cada aluno se apresenta |
| 00:35 | 20 min | **Por que proteger aplicativos**: dinâmica "O valor da segurança" + conceitos |
| 00:55 | 25 min | **XSS, teoria**: Cross-site Scripting |
| 01:20 | 10 min | Intervalo |
| 01:30 | 30 min | **XSS, prática**: XSS Game, XSS em uma API Spring e lista de exercícios |
| 02:00 | 25 min | **CSRF**: teoria (~15 min) + estudo de caso e lista de exercícios (~10 min) |
| 02:25 | 15 min | **DoS**: teoria e demonstração do rate limit (~10 min) + lista de exercícios (~5 min) |
| 02:40 | 20 min | **Encerramento**: recap, atividade de casa, preview da próxima aula |

## 02 · Abertura

*~15 min*

Não é um texto pra decorar: é um roteiro de pontos pra você preencher com a sua trajetória real. Numa turma que acabou de chegar, o que constrói confiança não é o currículo, é entender por que *você* está falando sobre isso.

1. **Quem você é**: nome, formação, quanto tempo trabalhando com desenvolvimento de software.
2. **Onde você usou isso de verdade**: um caso real em que autenticação, tratamento de entrada de dados, tokens ou um incidente de segurança apareceram no seu dia a dia. Concreto vale mais que abstrato.
3. **Por que esse módulo importa pro mercado**: toda aplicação que guarda dado de usuário precisa de autenticação, e quase toda vaga de back-end Java pede Spring Security ou JWT no requisito.
4. **Como as 9 aulas vão funcionar**: cada aula parte de um problema gerador, tem teoria curta e prática logo em seguida; o módulo fecha com um projeto avaliado. Apresente o calendário:

| Aula | Data | Tema |
|---|---|---|
| 01 | 28/10 | Introdução à segurança: XSS, CSRF e DoS |
| 02 | 30/10 | HTTPS e cookies |
| 03 | 04/11 | JWT |
| 04 | 06/11 | OAuth 2.0 |
| 05 | 09/11 | Single Sign-On |
| 06 | 11/11 | OpenID Connect |
| 07 | 13/11 | Situações reais de vulnerabilidade e tratamento de exceções |
| 08 | 16/11 | Spring Security: autenticação básica e JWT |
| 09 | 18/11 | Desenvolvimento do projeto e encerramento |

5. **O projeto do módulo**: implementar uma camada de autenticação em uma aplicação REST (nova ou já existente) com Spring Security. No caso de JWT: `POST /user` cria o usuário e grava no banco; `POST /authenticate` recebe os dados do usuário e devolve um token JWT; com o token, o aluno acessa os demais endpoints. Diga logo que tudo o que a turma vê nas próximas aulas é peça desse projeto.
6. **Combinado de ética**: tudo o que for mostrado de ataque hoje é para **entender a defesa**. Só se pratica em ambientes preparados para isso (XSS Game e a aplicação local da aula), nunca em sites de terceiros.
7. **O que você espera da turma**: participação nas rodadas, dúvidas em voz alta e Java + IDE funcionando no computador para as próximas aulas.

## 03 · Rodada com a turma

*~20 min*

Cada aluno responde, um a um. O objetivo não é só quebrar o gelo, é você sair dessa rodada sabendo o nível real da turma antes de decidir o ritmo da teoria que vem a seguir.

> **Pergunta para a turma:** "Diga seu nome, o que você faz hoje (ou pretende fazer) na área de tecnologia, e complete a frase: *'quando eu escuto segurança de aplicações, penso em ___'*: vale 'nunca pensei nisso', vale um golpe ou vazamento que você já viu acontecer."
>
> ~1 min por aluno. Ajuste esse bloco se a turma for grande: em duplas, ou reduza pra "nome + a frase".

Essa única pergunta entrega três sinais ao mesmo tempo, escute por eles:

| Sinal | O que fazer com isso |
|---|---|
| Nível técnico | Já programa em Java/Spring? Está começando agora? Ajusta o quanto você aprofunda em código (controller, requisição HTTP) durante a prática. |
| Exposição prévia a segurança | "Nunca pensei nisso" pede mais tempo nos exemplos do dia a dia (banco, e-commerce). "Já mexi com isso" é quem você chama pra complementar uma explicação em voz alta. |
| Sentimento | Respostas como "golpe no WhatsApp" ou "vazamento de senha" são ganchos prontos: hoje vocês vão ver como ataques desse tipo funcionam do lado da aplicação. |

**Dica:** Anote os nomes de quem citou uma experiência concreta (golpe, vazamento, invasão). Ao longo da teoria, puxe essas pessoas pelo nome ("Lembra que o João falou do golpe do boleto? É exatamente esse tipo de ataque."): a rodada só vale a pena se ela voltar a aparecer depois.

## 04 · Por que proteger aplicativos

*~20 min*

### Dinâmica "O valor da segurança" (~10 min)

Antes de qualquer definição, a turma descobre sozinha o que está em jogo. Divida em grupos de 3 a 4 pessoas e passe a tarefa:

> "Escolham uma aplicação do dia a dia (banco, delivery, e-commerce, sistema de escola). Listem: **(1)** o que ela guarda que tem valor, **(2)** o que acontece com a empresa e com os usuários se ela for invadida, **(3)** o que seria preciso fazer para evitar."

- 6 min de discussão nos grupos, 4 min de compartilhamento (um grupo de cada vez, sem repetir o que já foi dito).
- Vá anotando no quadro em três colunas: **o que tem valor** (dados pessoais, cartão, senhas, dinheiro) / **o que se perde** (dinheiro, reputação, clientes, disponibilidade) / **como evitar**.
- Fecha dizendo: a coluna da direita é o assunto do módulo inteiro.

### O que é segurança de aplicativos (~10 min)

> Segurança de aplicativos é o processo de desenvolver, incluir e testar recursos de segurança nos aplicativos para evitar vulnerabilidades diante de ameaças como acesso e modificação não autorizados.

Pontos para explicar com as palavras da turma (que já apareceram na dinâmica):

- Segurança não é só de rede ou de servidor: ela envolve **decisões tomadas durante o desenvolvimento** (como a aplicação trata entradas, senhas, erros) e continua depois do deploy.
- A segurança web cobre sites, aplicativos e APIs.
- **Casos recentes** (2021), para dar peso ao tema:
  - **Poly Network**: hackers roubaram cerca de US$ 600 milhões em criptomoedas explorando vulnerabilidades do sistema.
  - **Liquid**: bolsa japonesa de criptomoedas invadida, com cerca de US$ 90 milhões roubados.
  - **Renner**: ataque de ransomware que deixou o site indisponível e impactou o valor da empresa na bolsa.
- **Vantagens de proteger**: confiança do usuário, continuidade do negócio e menos custo do que corrigir depois do incidente.

**Transição:** "Hoje vamos conhecer três dos ataques mais comuns a aplicações web: XSS, CSRF e DoS. Para cada um: o que é, como funciona e como se defende."

## 05 · Cross-site Scripting (XSS)

*~55 min: ~25 min de teoria, intervalo de 10 min e ~30 min de prática*

### Teoria (~25 min)

**O que é:** um ataque em que um script malicioso é adicionado a um site. Quando outro usuário acessa esse site, o script roda **no navegador dele**, e o invasor passa a controlar o que aquele navegador faz naquele site (roubar sessão/token, alterar a página, redirecionar).

**A causa raiz, em uma frase:** a aplicação pega o que o usuário digitou e devolve para a página **sem tratar**. O navegador não sabe diferenciar "texto" de "código".

#### Tipos de ataque

| Tipo | Como funciona | Exemplo |
|---|---|---|
| **Stored XSS** | O invasor grava o script em algum lugar que a aplicação guarda (comentário, perfil, avaliação). Quando a informação é lida do banco e exibida, o script executa para **todo mundo** que abrir a página. | Script gravado numa caixa de comentários de um blog. |
| **Reflected XSS** | O script vai **na própria requisição** (por exemplo na URL) e a aplicação o devolve na resposta. A vítima precisa ser induzida a clicar no link. | E-mail de "promoção" com um link para o site real, contendo o script no parâmetro de busca. |

> **Nota:** existe ainda o **DOM-based XSS**, em que o próprio JavaScript da página insere dados da URL no DOM sem tratar. Só mencione se a turma perguntar; o foco hoje são os dois tipos acima.

#### Exemplo de script

O exemplo clássico, que prova que o código do invasor está executando:

```html
<script>alert('XSS')</script>
```

Deixe claro que o `alert` é só a prova de conceito. Em um ataque real, no lugar dele estaria um script que lê o cookie ou o token do usuário e o envia para o servidor do invasor, o que liga este assunto à aula de JWT ("JWT roubado").

#### Como prevenir

1. **Tratar toda entrada como maliciosa** até passar pela validação, antes de guardar e antes de exibir.
2. **Limpar/escapar a saída**: transformar `<` em `&lt;` e assim por diante, de modo que o navegador exiba o texto em vez de executá-lo. Em Spring: `HtmlUtils.htmlEscape(...)`.
3. **Content Security Policy (CSP)**: cabeçalho de resposta que diz ao navegador de quais fontes ele pode carregar scripts, estilos, imagens etc. (`script-src`, `default-src`, `img-src`...). O Spring Security permite configurar a CSP; isso será retomado na aula de Spring Security.
4. **Cabeçalhos de segurança do Spring Security**: o Spring Security adiciona vários por padrão.

> **Atenção (material desatualizado neste ponto):** o material de apoio cita o cabeçalho `X-XSS-Protection` como proteção. Ele pertence a um filtro de navegadores que foi descontinuado, e as versões atuais do Spring Security o enviam desabilitado. As defesas que valem hoje são **escapar a saída** e **CSP**. Se um aluno trouxer o `X-XSS-Protection` do material, explique isso.

**Pergunta para a turma (2 min):** "Em um formulário de comentários, onde o script poderia ser gravado e quem seria afetado?" (Resposta esperada: no banco, e *todos* que abrirem a página, não só quem comentou.)

> **Intervalo de 10 minutos aqui (01:20).** Depois dele, a turma volta e já executa o ataque que acabou de entender.

### Prática (~30 min)

Os alunos executam o ataque (em ambiente controlado) e depois aplicam a defesa, nessa ordem.

#### Parte 1: Reflected XSS no Google XSS Game (~10 min)

1. Em duplas, abrir **https://xss-game.appspot.com/** (programa de treinamento criado pelo Google em 2014).
2. **Nível 1:** o desafio é injetar um script que exiba um `alert()` na página. Dica para quem travar: pensar em *onde* o texto digitado é devolvido para a página, e em como o navegador interpretaria uma tag `<script>`.
3. Quem terminar, segue para o nível 2 e anota o que mudou na forma de injetar.
4. Fechamento (2 min): "O que o desenvolvedor do site deveria ter feito para esse ataque não funcionar?" (Escapar a saída.)

#### Parte 2: XSS em uma API Spring, ao vivo (~15 min)

Use o projeto de [`source/`](source/README.md) (Spring Boot 3, Java 17, Maven), criado antes da aula. Ele é **sem Spring Security** (que só entra na aula 08) e já tem as dependências do rate limit da seção 07. O README de `source/` traz o passo a passo na ordem da aula.

1. **Crie o endpoint vulnerável** e pergunte à turma o que está errado antes de rodar:

```java
@RestController
public class BuscaController {

    @GetMapping(value = "/busca", produces = MediaType.TEXT_HTML_VALUE)
    public String busca(@RequestParam String q) {
        return "<h1>Resultados para: " + q + "</h1>";
    }
}
```

2. **Teste o uso normal:** `http://localhost:8080/busca?q=java`
3. **Execute o ataque:** `http://localhost:8080/busca?q=<script>alert('XSS')</script>`. O alerta aparece. Peça a um voluntário que repita no próprio computador.
4. **Aplique a defesa:** escape a saída.

```java
@GetMapping(value = "/busca", produces = MediaType.TEXT_HTML_VALUE)
public String busca(@RequestParam String q) {
    return "<h1>Resultados para: " + HtmlUtils.htmlEscape(q) + "</h1>";
}
```

5. **Repita o ataque:** agora a página mostra o texto `<script>alert('XSS')</script>` na tela, sem executar. Mostre o código-fonte da página (`Ctrl+U`) para a turma ver o `&lt;script&gt;`.

> **Ponto de atenção:** pergunte "isso foi um Reflected ou um Stored XSS?" (Reflected: o script vai na URL e volta na resposta.) Depois pergunte o que mudaria para virar Stored (gravar o `q` num banco e exibir depois).

#### Parte 3: Lista de exercícios de XSS (~5 min)

Lista de exercícios de múltipla escolha sobre **XSS**. Dê o tempo para a turma começar e deixe o que sobrar como atividade de casa. Circule pela sala: é onde aparecem as dúvidas que não vieram durante a teoria.

## 06 · Cross-site Request Forgery (CSRF)

*~25 min: ~15 min de teoria e ~10 min de prática*

### Teoria (~15 min)

**O que é:** um ataque que engana um usuário **já autenticado** a executar uma ação indesejada em uma aplicação, sem que ele perceba. O invasor não precisa roubar a senha: ele usa a sessão que o navegador já tem aberta.

**Diferença-chave para o XSS:** no XSS o invasor executa código dentro do site; no CSRF ele faz o **navegador da vítima enviar uma requisição** ao site. Vale dizer isso explicitamente, é a confusão mais comum entre os dois.

#### Como funciona (duas condições)

1. **Sessão baseada em cookies:** o usuário fez login e o site identifica quem ele é pelo cookie. O navegador **anexa o cookie automaticamente** em toda requisição para aquele domínio, inclusive as disparadas por outro site.
2. **Parâmetros de requisição previsíveis:** a ação não exige nada que o invasor não consiga adivinhar (como uma senha ou um token).

Desenhe o fluxo no quadro:

```
1. Superman faz login no banco        -> navegador guarda o cookie de sessão
2. Superman abre o e-mail do invasor  -> clica no link / abre a página maliciosa
3. A página dispara uma requisição ao banco
4. O navegador anexa o cookie de sessão sozinho
5. O banco vê um usuário logado e executa a transferência
```

#### Exemplo com GET

Cenário do material: o Superman é cliente do banco fictício "Let's Code Bank" e está logado (sessão nos cookies). A requisição de PIX só tem os parâmetros `cc` (conta que recebe) e `valor`:

```http
GET http://letscodebank.com/pix.do?cc=Invasor&valor=R$100 HTTP/1.1
```

O invasor só precisa fazer o navegador do Superman enviar essa requisição (um link, ou uma imagem escondida na página dele apontando para essa URL). **Lição:** requisição `GET` nunca deve alterar estado.

#### Exemplo com POST

Trocar para POST não resolve sozinho. O invasor cria uma página com um formulário escondido que se envia sozinho, e manda o link por e-mail (phishing):

```html
<body onload="document.csrf.submit()">
  <form action="http://letscodebank.com/pix" method="POST" name="csrf">
    <input type="hidden" name="valor" value="100">
    <input type="hidden" name="conta" value="invasor">
  </form>
</body>
```

#### Como prevenir

- **Token CSRF:** uma string aleatória conhecida apenas pelo navegador do usuário e pela aplicação, enviada em toda requisição que altera estado. O site do invasor não consegue adivinhar esse token, então a requisição forjada é recusada.
- **Cookies `SameSite`:** com o atributo `SameSite`, o navegador só envia o cookie em requisições originadas do mesmo site. Navegadores atuais já tratam cookies sem o atributo como `Lax`, o que reduz bastante o problema, mas não é motivo para abandonar o token.
- **Spring Security:** a proteção CSRF vem **ligada por padrão**. Para uma aplicação ficar vulnerável, alguém precisa desabilitar explicitamente. Isso volta na aula 08 (e por que, em APIs stateless com token no header, é aceitável desabilitar).

**Pergunta para a turma (2 min):** "Se o banco exigisse a senha a cada transferência, o ataque funcionaria?" (Não: os parâmetros deixariam de ser previsíveis. Conecte com a segunda condição.)

### Prática (~10 min)

#### Estudo de caso: "Como prevenir CSRF?" (~5 min)

Retome o banco "Let's Code Bank": o Superman está logado (sessão em cookie) e a transferência é um `POST /pix` com `conta` e `valor`. Em duplas, a turma decide **para cada defesa abaixo se ela barra o ataque e por quê**, antes de você revelar a resposta:

| Defesa proposta | Barra o CSRF? | Por quê |
|---|---|---|
| Trocar o `GET` por `POST` | **Não** | O formulário escondido da página do invasor envia o `POST` sozinho. |
| Token CSRF em toda requisição que altera estado | **Sim** | O site do invasor não consegue adivinhar o token. |
| Cookie com `SameSite` | **Sim**, na maioria dos casos | O navegador não envia o cookie em requisições vindas de outro site. |
| Pedir a senha a cada transferência | **Sim** | O parâmetro deixa de ser previsível. |
| Usar HTTPS | **Não** | HTTPS protege o canal (aula 02), mas o navegador da vítima continua enviando a requisição legítima, com o cookie. |

Feche com a pergunta: "qual dessas o Spring Security já faz por padrão?" (O token CSRF, que será visto na aula 08.)

#### Lista de exercícios de CSRF (~5 min)

Lista de exercícios de múltipla escolha sobre **CSRF**. Dê o tempo para a turma começar e deixe o que sobrar como atividade de casa.

## 07 · Denial-of-Service (DoS)

*~15 min: ~10 min de teoria e demonstração e ~5 min de prática*

### Teoria e demonstração (~10 min)

**O que é:** um ataque de **negação de serviço**: o invasor tenta derrubar ou sobrecarregar a aplicação para que os demais usuários não consigam usá-la. O alvo é o dono da aplicação (concorrente, extorsão, ativismo).

> **Curiosidade (do material):** o primeiro ataque DoS registrado foi feito em 1974 por David Dennis, então com 13 anos, que escreveu um programa com o comando `ext` e forçou computadores de um laboratório universitário a desligar.

#### Tipos de ataque DoS

- **Flood Attack (inundação):** o invasor sobrecarrega a aplicação com muito mais requisições do que ela consegue atender, deixando o servidor lento ou fazendo-o cair.
- **Crash Attack:** o invasor envia uma entrada que explora um bug ou vulnerabilidade e derruba o servidor.

#### DoS x DDoS

No **DDoS (distribuído)**, vários sistemas atacam o mesmo alvo ao mesmo tempo, de locais diferentes. Isso torna o ataque mais forte e muito mais difícil de bloquear, porque não existe um único endereço de origem para barrar.

#### Como parar um ataque DoS

- **Black Hole Routing:** ao perceber o ataque, o dono roteia todo o tráfego para um "buraco negro". A aplicação cai de qualquer forma, mas ele ganha tempo para investigar a origem.
- **Rate Limit (limite de taxa):** limita quantas requisições o servidor aceita por janela de tempo. O sistema não é sobrecarregado.

#### Estudo de caso: "Entendendo um DoS simples"

Use a API de alunos como cenário. O endpoint `GET /alunos` só aguenta **1 requisição a cada 10 segundos**. O que acontece se 1.000 requisições chegarem por segundo? Mostre o rate limiter com a biblioteca `resilience4j` (dependência `resilience4j-spring-boot3`, mais `spring-boot-starter-aop` para a anotação funcionar):

```yaml
resilience4j:
  ratelimiter:
    instances:
      alunoService:
        limitForPeriod: 1
        limitRefreshPeriod: 10s
        timeoutDuration: 0s
```

```java
@RestController
@RequestMapping("/alunos")
public class AlunoController {

    @GetMapping
    @RateLimiter(name = "alunoService")
    @ResponseStatus(HttpStatus.OK)
    public List<Aluno> consultaTodos() {
        return alunoService.buscaTodos();
    }
}
```

Quando o usuário passa de 1 chamada dentro do intervalo, a biblioteca lança `RequestNotPermitted`.

> **Dica de demo:** o material usa `timeoutDuration: 5s`, o que faz a segunda chamada ficar **esperando até 5 segundos** por uma vaga antes de falhar. Para o efeito ser imediato na sala, use `0s`. Se der tempo, rode `curl` duas vezes seguidas e mostre o erro. A resposta "feia" que aparece será tratada na aula 07 (tratamento de exceções), quando a turma aprender a devolver `429 Too Many Requests`.

### Prática (~5 min)

#### Lista de exercícios de DoS (~5 min)

Lista de exercícios de múltipla escolha sobre **DoS**. Dê o tempo para a turma começar e deixe o que sobrar como atividade de casa. A demonstração do rate limit acima é a parte prática principal deste tópico; se der tempo, peça a um voluntário que rode o `curl` duas vezes no próprio computador.

## 08 · Encerramento

*~20 min*

Recap de 3 minutos, **feito pela turma** (pergunte e vá anotando): o que é segurança de aplicativos e por que importa; como funciona o XSS e a defesa; como funciona o CSRF e a defesa; o que é DoS e como o rate limit ajuda.

Feche amarrando com o módulo: "XSS e CSRF dependem de **cookies, sessão e entrada de dados**. É por isso que as próximas aulas falam de HTTPS, cookies e tokens: são as peças que vão compor a autenticação do projeto".

**Atividade para casa:**
- **Leitura:** segurança em aplicações web (MDN Web Docs, *Website Security*) e a página da OWASP sobre XSS: https://owasp.org/www-community/attacks/xss/
- **Pesquisa individual:** como um script malicioso pode atacar o usuário.
- **Pesquisa individual:** grandes ataques DoS/DDoS que já aconteceram.
- **Exercícios de fixação:** o que sobrou das listas sobre XSS, CSRF e DoS.

**Preview da Aula 02:** Noções básicas de HTTPS e cookies. Vocês vão ver por que o HTTP puro é inseguro, como a criptografia protege a comunicação e o que são os cookies que apareceram hoje no CSRF.

## 09 · Dicas de condução

- **Turma grande (20+)?** Reduza a rodada individual para "nome + a frase", ou faça em duplas que se apresentam uma à outra antes de compartilhar com a turma toda.
- **Setup do projeto:** deixe o projeto de `source/` aberto e rodando antes da aula, para não gastar a prática com configuração (ou crie um projeto vazio e digite a partir dele, seguindo o README). Teste o endpoint `/busca` antes, no navegador que será usado na sala.
- **Plano B para o XSS Game:** se o site estiver fora do ar ou bloqueado na rede da escola, a Parte 2 da prática de XSS (endpoint `/busca`) cobre o mesmo objetivo; estenda-a em dupla, com cada aluno rodando o ataque no próprio projeto.
- **Navegador:** tente o ataque do `/busca` no navegador antes da aula. Se algum navegador da turma bloquear o alerta, mostre em outro e explique que isso é uma proteção do navegador, não da aplicação.
- **Ética:** repita o combinado da abertura se alguém sugerir testar em sites reais. Ataque só no que é seu ou no que foi feito para isso.
- **Itens do planejamento cobertos:** XSS (o que é, tipos, prevenção), CSRF (o que é, como funciona, GET/POST, prevenção), DoS (o que é, tipos, DoS x DDoS, como parar), dinâmica "O valor da segurança", executar Reflected XSS no Google XSS Game e as três listas de múltipla escolha.

---

*Roteiro de aula: módulo de Fundamentos de segurança para aplicações. Aula 01 de 09.*
