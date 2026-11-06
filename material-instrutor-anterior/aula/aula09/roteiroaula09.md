# Aula 09: Desenvolvimento do Projeto e Encerramento

**Roteiro de Aula · Módulo Fundamentos de segurança para aplicações (BE-JV-013)**

Última aula do módulo. A turma desenvolve e conclui o projeto (a camada de autenticação de uma API REST com Spring Security), apresenta o resultado e recebe a devolutiva da avaliação por rubrica, junto com uma autoavaliação.

**Data:** 18/11/2026 · quarta &nbsp;·&nbsp; **Duração:** 3h &nbsp;·&nbsp; **Formato:** desenvolvimento com suporte + apresentações + devolutiva

**Código:** não há código novo. As equipes continuam o **projeto delas**; o projeto-referência do instrutor é o estado final da Aula 08 (`../aula08/source`, rodando com o perfil `jwt`).

> **Problema gerador:** Desenvolvimento do Projeto: implementar uma camada de autenticação em uma aplicação REST.

---

## Sumário

1. [Visão geral](#01--visão-geral)
2. [Abertura e checklist do projeto](#02--abertura-e-checklist-do-projeto)
3. [Desenvolvimento com suporte](#03--desenvolvimento-com-suporte)
4. [Apresentações](#04--apresentações)
5. [Devolutiva da rubrica e autoavaliação](#05--devolutiva-da-rubrica-e-autoavaliação)
6. [Encerramento do módulo](#06--encerramento-do-módulo)
7. [Dicas de condução](#07--dicas-de-condução)

---

## 01 · Visão geral

Tempos relativos ao início da aula, ajuste ao seu horário real. Esta aula **não tem teoria nova**: o seu papel é de apoio e de avaliação. Proteja o tempo das apresentações e da devolutiva: o projeto é o que alimenta a última categoria da avaliação por rubrica (aplicação em escopo aberto).

| Hora | Duração | Bloco |
|---|---|---|
| 00:00 | 15 min | **Abertura**: checklist do projeto e combinados do dia |
| 00:15 | 65 min | **Desenvolvimento com suporte** (com check-ins) |
| 01:20 | 10 min | Intervalo |
| 01:30 | 60 min | **Apresentações** dos projetos |
| 02:30 | 30 min | **Devolutiva** da avaliação por rubrica, autoavaliação e encerramento do módulo |

## 02 · Abertura e checklist do projeto

*~15 min*

Retome em 2 minutos o que foi o módulo: dos ataques (XSS, CSRF, DoS) ao HTTPS e aos cookies, dos tokens (JWT, OAuth, OIDC e SSO) até o código (SQL Injection, exceções e Spring Security). Hoje tudo isso vira **uma API protegida**.

### O projeto

> Implementar uma **camada de autenticação** em uma aplicação REST, **nova ou já existente**, utilizando **Spring Security simples ou com JWT**.

No caso da implementação com **JWT**:

- **`POST /user`:** cria o usuário e **persiste os dados** em um banco de dados de preferência do aluno.
- **`POST /authenticate`:** recebe os dados do usuário e **retorna um token JWT**.
- Com o token em mãos, o aluno acessa os **demais endpoints** da aplicação, enviando o token no cabeçalho (o enunciado pede `access_token`; o padrão `Authorization: Bearer` também é aceito, conforme combinado na aula 08).

### Checklist de aceite (projete na tela)

Cada aluno confere o próprio projeto:

| # | Item | Feito? |
|---|---|---|
| 1 | A aplicação sobe e tem ao menos um endpoint de negócio além da autenticação. | |
| 2 | `POST /user` cria o usuário e grava no banco. | |
| 3 | A senha é gravada como **hash (BCrypt)**, nunca em texto puro. | |
| 4 | `POST /authenticate` devolve um JWT com credenciais corretas e **401** com credenciais erradas. | |
| 5 | Os demais endpoints **rejeitam** requisições sem token ou com token inválido/expirado (**401**). | |
| 6 | Os demais endpoints **aceitam** requisições com o token válido no cabeçalho. | |
| 7 | A **chave** do JWT vem de **variável de ambiente** ou configuração externa, não está fixa no código. | |
| 8 | O token tem **expiração** definida. | |
| 9 | Há um **tratamento global de exceções**, sem stack trace nas respostas (aula 07). | |
| 10 | Há um **README** curto com como rodar e como testar (passo a passo ou coleção do Postman). | |

> **Caminho com Spring Security simples (autenticação básica):** se o aluno optou por não usar JWT, os itens 2, 4, 7 e 8 não se aplicam; vale o conjunto equivalente (usuário configurado, rotas protegidas, 401 sem credenciais). Combine isso individualmente.

**Combinados do dia:** quem terminar cedo ajuda um colega (explicando, sem digitar por ele) ou parte para os itens extras da seção 03.

## 03 · Desenvolvimento com suporte

*~65 min*

Aqui o aluno trabalha e você circula. A regra é **perguntar mais do que responder**: em vez de apontar o erro, pergunte "o que a resposta HTTP está dizendo?" ou "o que aparece no log?".

### Check-ins (a cada ~20 min)

Pare a turma por 1 minuto e pergunte: "levante a mão quem já tem o item 2, o 4, o 6". Isso mostra quem está atrás sem constranger e deixa você redistribuir a atenção.

### Problemas mais comuns e como orientar

| Sintoma | Causa provável | Pista a dar (sem entregar a solução) |
|---|---|---|
| `POST` retorna **403** mesmo sem ter erro | Proteção CSRF ligada. | "O que o Spring Security faz por padrão com requisições que alteram estado? Por que podemos desabilitar nesta API?" (aulas 01 e 08) |
| Rota protegida devolve **403** em vez de **401** | Falta o *entry point* de autenticação. | "Qual status o Spring devolve quando não há autenticação configurada?" |
| `There is no PasswordEncoder mapped...` | Senha salva sem hash, ou `PasswordEncoder` ausente. | "Como a senha foi gravada? Existe um bean de `PasswordEncoder`?" |
| Erro ao criar a tabela `user` | Palavra reservada no banco. | "Como mudar o nome da tabela na entidade?" |
| `Could not resolve placeholder 'app.jwt.secret'` | Variável de ambiente não definida. | "Onde a aplicação lê a chave? Onde ela deveria estar configurada na IDE?" |
| Token válido, mas a rota continua bloqueada | Filtro fora da cadeia, ou fora de ordem, ou o `SecurityContext` não foi preenchido. | "O filtro está registrado **antes** do filtro de login? O que acontece dentro dele se o token for válido?" |
| `/authenticate` devolve **500** com senha errada | O tratamento global está capturando a exceção de autenticação. | "Qual exceção o `AuthenticationManager` lança? O que o handler global faz com ela?" (aula 07) |
| O token sempre é considerado expirado | Relógio/`exp` com unidade errada (segundos x milissegundos). | "Em que unidade a biblioteca espera a expiração?" |

### Itens extras, para quem terminar cedo

Sugestões que **não fazem parte do mínimo**, mas ampliam o aprendizado (e podem contar como escopo aberto na rubrica):

- Acrescentar **perfis** (`USER` e `ADMIN`) e proteger um endpoint só para `ADMIN` (`@PreAuthorize`).
- Implementar um **refresh token** simples.
- Limitar as tentativas de login com **rate limit** (aula 01).
- Validar a entrada do cadastro (tamanho mínimo da senha, formato do usuário) para evitar entradas inválidas.
- Escrever **testes automatizados** do fluxo (cadastrar, autenticar, acessar com e sem token).

**Dica:** peça aos alunos que **guardem as evidências** (prints ou a coleção do Postman rodando), porque elas serão úteis na apresentação e na devolutiva.

## 04 · Apresentações

*~60 min*

Cada aluno (ou grupo, se o projeto foi em grupo) apresenta em **4 minutos**, mais **1 minuto** de perguntas, em um roteiro fixo para a comparação ser justa.

### Roteiro da apresentação

1. **A aplicação (30 s):** de que se trata a API e quais são os endpoints de negócio.
2. **Demonstração do fluxo (2 min):** executar, ao vivo ou pelo Postman, a sequência:
   1. criar um usuário (`POST /user`);
   2. autenticar e receber o token (`POST /authenticate`);
   3. acessar um endpoint **sem** o token (deve ser barrado);
   4. acessar um endpoint **com** o token (deve funcionar);
   5. (bônus) acessar com um token **alterado** ou **expirado**.
3. **Uma decisão de segurança (1 min):** o que foi feito para proteger a aplicação e **por quê** (ex.: hash da senha, chave fora do código, expiração do token, CSRF desabilitado e a justificativa).
4. **Uma dificuldade (30 s):** o que foi mais difícil e como foi resolvido.

### Conduzindo as perguntas

Cada pergunta da turma ou sua é uma chance de avaliar a compreensão. Sugestões:

- "O que está dentro do payload do seu token? Tem algum dado sensível?" (aula 03)
- "O que aconteceria se alguém roubasse esse token? O que você fez para limitar o estrago?" (aulas 03 e 07)
- "Por que a senha vai como hash e não criptografada?" (aulas 02 e 08)
- "Se a API fosse acessada por HTTP puro, o que mudaria?" (aula 02)
- "Qual a diferença entre o que você implementou e o OAuth/OpenID Connect?" (aulas 04 e 06)

> **Dica:** peça que a turma tome **uma nota de feedback por apresentação** (um acerto e uma sugestão). Isso mantém todos atentos e gera material para a devolutiva entre colegas.

### Se a turma for grande

- **Mais de ~12 apresentações:** reduza para 3 minutos + 1 de perguntas, ou divida a turma em **dois grupos**, em duas salas ou duas rodadas, com você alternando. Outra opção é pedir um **vídeo curto** gravado em casa para quem não couber.
- **Quem não conseguiu concluir:** apresenta o que tem, e a devolutiva usa o que foi entregue. Não interrompa nem comente o atraso na frente da turma.

## 05 · Devolutiva da rubrica e autoavaliação

*~30 min*

A avaliação por rubrica tem quatro níveis em cada habilidade:

1. **Não conhece** a competência/habilidade.
2. **Conhece**, mas **não é capaz de aplicar**.
3. **É capaz de aplicar** em escopo **fechado**.
4. **É capaz de aplicar** em escopo **aberto**.

### As habilidades do módulo e onde buscar evidências

| Habilidade | Onde aparece a evidência |
|---|---|
| **Entender fundamentos da segurança para aplicações** | Respostas na apresentação sobre ataques (XSS, CSRF, DoS) e defesas; tratamento de exceções e SQL Injection; listas de exercícios. |
| **Entender sobre criptografia** | Hash de senha (BCrypt) no projeto; explicação de HTTPS e de assinatura; listas de exercícios. |
| **Entender sobre JWT, OAuth, SSO e OpenID Connect** | Explicação do token do projeto; respostas sobre a diferença entre os conceitos; listas de exercícios. |
| **Entender e implementar com Spring Security** (básica e JWT) | O **projeto** em si: aplicado em escopo fechado (seguiu o roteiro da aula 08) ou aberto (adaptou, ampliou ou decidiu por conta própria). |

> **Como distinguir fechado e aberto:** *fechado* é reproduzir o que foi construído em aula (a mesma estrutura, na mesma API). *Aberto* é resolver algo **novo**: uma API própria, outro banco, perfis, refresh token, testes, ou uma decisão de segurança que o aluno **justifica**.

### Passo a passo da devolutiva (~30 min)

1. **Autoavaliação individual (~10 min):** cada aluno preenche a tabela abaixo, sozinho, **antes** de saber a sua avaliação.
2. **Devolutiva (~15 min):** você apresenta, a cada aluno ou à turma (conforme a política da escola), a sua avaliação por habilidade e **compara com a autoavaliação**. As diferenças são o assunto mais útil da conversa. Se a devolutiva for individual, use o tempo da turma para os alunos trabalharem nos itens extras ou nos pontos fracos, enquanto você os chama um a um.
3. **Plano de próximos passos (~5 min):** cada aluno anota **uma** coisa para aprofundar (por exemplo, OAuth na prática, autorização por perfis, testes de segurança).

### Modelo de autoavaliação

| Habilidade | Meu nível (1 a 4) | Evidência que justifica |
|---|---|---|
| Fundamentos de segurança para aplicações | | |
| Criptografia | | |
| JWT, OAuth, SSO e OpenID Connect | | |
| Spring Security (básica e JWT) | | |

**Perguntas para refletir:**

- Em qual habilidade eu mais cresci neste módulo?
- O que eu consegui fazer sozinho que não conseguia antes?
- Se eu tivesse mais uma semana, o que melhoraria no projeto?

## 06 · Encerramento do módulo

*~5 min (final da seção anterior)*

Feche com uma **mini-retrospectiva** de 5 minutos, com duas perguntas, uma resposta por aluno em uma frase:

- "O que funcionou neste módulo?"
- "O que você mudaria?"

Registre as respostas: elas são insumo para o módulo da próxima turma.

**Mensagem final sugerida:** segurança **não é uma etapa no fim do projeto**: é um conjunto de decisões em cada linha de código (entrada de dados, senhas, tokens, erros, dependências). O que a turma viu aqui é o começo, e a lista do OWASP Top 10 é o mapa para continuar.

## 07 · Dicas de condução

- **Combine antes, na aula 08, o formato do projeto** (individual ou em grupo, e o cabeçalho do token). Evita surpresa hoje.
- **Dê os critérios antes de cobrar:** o checklist da seção 02 é o mesmo usado na avaliação. Projete-o, não o guarde para você.
- **Tenha o seu próprio projeto-referência rodando,** para comparar quando um aluno estiver travado ("ele faz o que o seu faz?").
- **Cuide do tempo das apresentações:** use um cronômetro visível e avise "30 segundos".
- **Seja específico no feedback:** "o item 5 está parcial: o token inválido devolve 500" ajuda mais do que "falta ajustar".
- **Tenha cuidado com o que aparece na tela:** se o aluno mostrar o arquivo de configuração com a chave do JWT, é uma oportunidade de reforçar (sem expor o aluno) por que a chave não vai para o repositório.
- **Itens do planejamento cobertos:** desenvolvimento do projeto, apresentação (forma de avaliação do planejamento) e devolutiva da avaliação por rubrica/autoavaliação.

---

*Roteiro de aula: módulo de Fundamentos de segurança para aplicações. Aula 09 de 09.*
