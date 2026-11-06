# Plano de continuidade — turma 1749 · BE-JV-013 (Aulas 4 a 9)

> **Documento de coordenação, para o instrutor que assume a turma. Não vai para os alunos.**
> Elaborado em 05/11/2026. **Status: aprovado pelo PO (Jessé) em 05/11/2026** quanto à ordem das aulas (seção 2) e
> ao material do aluno (seção 5). Decisões registradas na seção 6.

**Fontes**

| Fonte | Onde está | O que traz |
|---|---|---|
| Planejamento oficial do módulo | `Modulos/BE-JV-Backend-Java/BE-JV-013 Fundamentos de segurança para aplicações/Planejamento/planejamento.md` | 9 aulas, temas e projeto (autenticação com JWT, `POST /user` e `POST /authenticate`) |
| Material do instrutor (Hagamenon) | `material-instrutor-anterior/` (conteúdo do `aula.zip`), pasta `aula/` | Roteiro detalhado das 9 aulas (`aulaNN/roteiroaulaNN.md`), código por aula (`aulaNN/source/`) e o mapa `aula/README.md` |
| Projeto final | `aula/aula01/apichamadosespecificacao.md` (o mesmo arquivo publicado no LMS) | API de chamados (*help desk*): UC1 a UC6, contrato, mapa de ameaças, entregáveis e rubrica |
| O que aconteceu nas Aulas 1 a 3 | `passagem-de-turma.md` e `notas-por-aula/` | Onde parou cada tema, turma, grupos e imprecisões a corrigir |

**Dois projetos, papéis distintos**

- **Projeto guiado (em aula): API de alunos** (`api-alunos`, pacote `com.ada.seguranca`). É o código que evolui de uma
  aula para a outra; cada `aulaNN/source` é o estado final daquela aula.
  - A cadeia é 01 → 02 → 03 → 07 → 08.
  - Há dois projetos à parte, de demonstração: `aula04/source` (`auth-jwt`, access e refresh token) e `aula05/source`
    (`sso-escola`, SSO local).
  - Pilha única: **Spring Boot 3.3.5, Java 17**, `java-jwt`, H2.
- **Projeto final (em grupo): API de Chamados (HelpDesk)**. É a avaliação. Entrega e apresentação em **Qua 18/11**:
  6 min de apresentação mais 2 min de perguntas por grupo, e cada integrante sobe o zip no LMS.

---

## 1. Onde estamos

| Roteiro do Hagamenon | Situação em 05/11 |
|---|---|
| Aula 01 (XSS, CSRF, DoS) | Dada entre as Aulas 1 e 2, com cortes. **Sem** as listas de exercícios e sem a prática de CSRF. |
| Aula 02 (criptografia, HTTPS, cookies) | Criptografia e HTTPS dados na Aula 2; cookies e sessão, na Aula 3. **Sem** a lista de HTTPS. |
| Aula 03 (JWT, Blocos 1 a 6) | Blocos 1 a 3 dados em parte (sessão, token, estrutura do JWT, `JwtManual`). **Faltam os Blocos 4, 5 e 6:** forjar o token, token roubado, chave fraca e HS256 × RS256. É o miolo do JWT, e o roteiro diz que são os blocos de que a turma mais lembra. |
| Aulas 04 a 09 | Não iniciadas. |

**Atraso:** cerca de **meia aula**, porque falta o fim da Aula 03 do roteiro. Restam **6 encontros** (06, 09, 11, 13,
16 e 18/11) para 6 roteiros (04 a 09) mais esse resto.

**Projeto final:**

- Grupos formados: 4 salas, duas com 5 pessoas.
- A especificação está no LMS desde 30/10.
- O Git deixou de ser exigido em 04/11.
- O andamento dos grupos é **desconhecido**, porque o trabalho nas salas não foi gravado.

O cronograma da própria especificação (seção 12) espera, até 09/11, os endpoints de chamados já modelados e
funcionando sem segurança.

---

## 2. Proposta de reorganização (Aulas 4 a 9)

**Ideia central:** fechar o JWT, condensar a parte conceitual (OAuth, SSO e OpenID Connect) em dois encontros e
manter a cadeia de código 07 → 08. Isso **libera 16/11 como laboratório do projeto**.

No plano original, a autenticação era construída em 16/11, a um dia útil da entrega. A própria especificação alerta
para esse risco (seção 12, nota final).

| Encontro | Data | Conteúdo | Roteiros de origem | Código | Meta do projeto no dia |
|---|---|---|---|---|---|
| **A4** | Sex 06/11 | Fechamento do JWT + OAuth 2.0 | Aula 03, Blocos 4 a 6 + Aula 04 | `aula03/source` e `aula04/source` | Check-in: chamados modelados (UC3 a UC6 sem segurança) |
| **A5** | Seg 09/11 | SSO + OpenID Connect | Aulas 05 e 06, condensadas | `aula05/source` | Endpoints de chamados funcionando; extras escolhidos |
| **A6** | Qua 11/11 | SQL injection + tratamento de exceções | Aula 07 | `aula07/source` | Tratamento global, validação e busca parametrizada |
| **A7** | Sex 13/11 | Spring Security: básica + JWT | Aula 08 | `aula08/source` | Cadastro, autenticação e filtro no projeto |
| **A8** | Seg 16/11 | Laboratório do projeto + ensaio | Aula 09, seções 02 e 03 | Projeto dos grupos | Fluxo completo + mapa de ameaças + README |
| **A9** | Qua 18/11 | Ajustes, apresentações e devolutiva | Aula 09 | Projeto dos grupos | Entrega no LMS e apresentação |

**Alternativa B: manter a ordem do Hagamenon.**

- **Como fica:** A4 OAuth, A5 SSO, A6 OIDC, A7 exceções, A8 Spring Security, A9 projeto, com o resto do JWT
  espremido na abertura da A4.
- **Vantagem:** segue o plano de ensino que a turma recebeu e dá mais profundidade a SSO e OIDC.
- **Custo:** os grupos só veem Spring Security em 16/11 e chegam à entrega sem nenhuma aula de laboratório.

**Decisão (D1): a proposta acima.** Os motivos:

- O projeto pesa mais na avaliação. Os critérios da rubrica (seção 14 da especificação) são quase todos de Spring
  Security, JWT, exceções e autorização.
- SSO e OIDC entram só no critério "Decisões de segurança".

---

## 3. Roteiro de cada encontro

Horários reais da turma: 15h às 18h, com intervalo combinado de **16h30 às 16h50 (20 min)**. Os roteiros do
Hagamenon preveem intervalo de 10 min, por isso os tempos abaixo já estão ajustados.

**Adaptação para o Teams.** Os roteiros foram escritos para sala presencial (cartões de votação, cantos da sala,
lousa, papel). No Teams:

| No roteiro | No Teams |
|---|---|
| Votação com cartões | Enquete do Teams ou reação no chat ("A", "B", "C") |
| Cantos da sala | Enquete com 4 opções |
| Lousa ou papel | Whiteboard do Teams ou um slide em branco compartilhado |
| Duplas | Salas de grupo de 2 ou 3 pessoas, ou os próprios grupos do projeto |

### A4 · Sex 06/11 — Fechamento do JWT + OAuth 2.0

**Antes da aula:**

- Rodar `aula03/source` e `aula04/source`. A Aula 04 exige `JWT_SECRET` com 32 caracteres ou mais (checklist na
  seção 10 do roteiro da Aula 04).
- Abrir o jwt.io. Se a rede não abrir o site, usar o plano B do roteiro da Aula 03: `/jwt/decodificar` e
  `/jwt/validar`.

| Horário | Bloco | Origem |
|---|---|---|
| 15h00–15h10 | Apresentação do novo instrutor; combinados mantidos (projeto, grupos, 18/11); como está cada grupo, em uma frase | — |
| 15h10–15h40 | **Dá para falsificar?** Trocar `USER` por `ADMIN`, assinar com chave errada, as três verificações de `JwtManual.validar`, `alg: none`. Dinâmica Time Vermelho × Time Azul pelo chat (8 min) | Aula 03, Bloco 4 |
| 15h40–16h00 | **Quem guarda a chave?** Desafio da chave fraca (requisições 6.1 a 6.3); **HS256 (segredo compartilhado) × RS256 (par de chaves)** | Aula 03, Bloco 6 |
| 16h00–16h10 | **E se roubarem o token?** Token de 20 s (5.1 e 5.2), expiração curta; enquete "onde guardar o token" | Aula 03, Bloco 5 (condensado) |
| 16h10–16h30 | OAuth 2.0: o problema (app de impressão de fotos) e os papéis | Aula 04, seções 03 e 04 |
| 16h30–16h50 | Intervalo | |
| 16h50–17h20 | Grant types; qual usar hoje (código de autorização com PKCE, credenciais do cliente, refresh token); por que OAuth não autentica | Aula 04, seções 05 e 06 |
| 17h20–17h35 | Demo `auth-jwt`: login, `/me`, 401 × 403, refresh, reuso recusado (requisições 4, 6, 7, 8, 9, 10) | Aula 04, Parte 1 |
| 17h35–17h55 | **Projeto:** cada grupo mostra o modelo de `Chamado` e o que já roda. Esclarecer dúvidas da especificação | Especificação, seção 12 |
| 17h55–18h00 | Chamada; atividade de casa: listas de JWT e OAuth | |

**Corrigir aqui** (detalhe na seção 6 de `passagem-de-turma.md`):

- HS256 é simétrico.
- Client Credentials é o fluxo do OAuth 2 entre sistemas, não "login e senha em toda requisição" (isso é Basic Auth).
- No CSRF o navegador envia o cookie sozinho; quem lê o cookie é o XSS.

**Pode cortar:** a dinâmica "Qual grant eu uso?" vai para a abertura da A5.

### A5 · Seg 09/11 — SSO + OpenID Connect

| Horário | Bloco | Origem |
|---|---|---|
| 15h00–15h15 | Retomada: dinâmica "Qual grant eu uso?" (5 cenários, em enquete) | Aula 04, Parte 2 |
| 15h15–15h45 | O que é SSO, identidade federada, componentes e fluxo | Aula 05, seções 03 e 04 |
| 15h45–16h05 | Demo `sso-escola`: um provedor de identidade e duas aplicações (Notas e Biblioteca), login único e logout único | Aula 05, demonstração ao vivo |
| 16h05–16h30 | OpenID Connect: o que acrescenta ao OAuth, participantes, **ID Token** (decodificar um) | Aula 06, seções 03 e 04 + Parte 2 |
| 16h30–16h50 | Intervalo | |
| 16h50–17h10 | Fluxo de código de autorização (o implícito como legado); **quadro comparativo JWT × OAuth × SSO × OIDC** | Aula 06, seções 05 e 06 + Parte 3 |
| 17h10–17h55 | **Projeto em salas:** endpoints de chamados funcionando; escolha dos extras (seção 15 da especificação) | Especificação, seção 12 |
| 17h55–18h00 | Chamada; casa: listas de SSO e OIDC | |

**Pode cortar:** detalhes de SAML 2.0, o estudo de caso do e-commerce (Aula 06, Parte 1) e o desenho de fluxo em
grupo (Aula 05, Parte 2). O quadro comparativo **não** se corta: o roteiro diz que é ele que dá o mapa do bloco
conceitual.

### A6 · Qua 11/11 — SQL injection + tratamento de exceções

O código parte de `aula03/source` e termina em `aula07/source` (H2, `AlunoRepository`, `GlobalExceptionHandler`).

| Horário | Bloco | Origem |
|---|---|---|
| 15h00–15h10 | Retomada | |
| 15h10–15h40 | SQL injection: demo vulnerável × corrigida; **JPA só protege com consulta parametrizada** | Aula 07, seção 03 |
| 15h40–15h55 | Dependências vulneráveis e OWASP Top 10 (condensado) | Aula 07, seção 04 |
| 15h55–16h30 | Exceções: tratamento seguro, o que a resposta de erro revela, exceções próprias, `@RestControllerAdvice` | Aula 07, seções 05 e 06 |
| 16h30–16h50 | Intervalo | |
| 16h50–17h55 | **Prática no projeto:** tratamento global no formato da seção 09 da especificação, `@Valid` com 400 e busca por título parametrizada | Aula 07, Parte 1, aplicada ao projeto |
| 17h55–18h00 | Chamada; casa: listas de exceções | |

### A7 · Sex 13/11 — Spring Security: autenticação básica e JWT

O código parte de `aula07/source` e termina em `aula08/source`, com os perfis `basica` e `jwt`. É a aula mais densa.
O roteiro pede **três versões prontas** do projeto: o ponto de partida, um ponto intermediário e o final. Se a
codificação ao vivo atrasar, pula-se para a versão seguinte.

| Horário | Bloco | Origem |
|---|---|---|
| 15h00–15h10 | Retomada | |
| 15h10–15h25 | O que é o Spring Security; autenticação × autorização; a cadeia de filtros | Aula 08, seção 03 |
| 15h25–15h50 | Autenticação básica, condensada (passos 1 a 5, incluindo o 403 no POST por causa do CSRF) | Aula 08, seção 04 |
| 15h50–16h30 | Desenho do JWT e decisões com a turma (cabeçalho `access_token` × `Authorization: Bearer`, BCrypt, chave em variável de ambiente, *stateless*); passos 1 e 2 (entidade, `PasswordEncoder`, `UserDetailsService`) | Aula 08, seções 05 e 06 |
| 16h30–16h50 | Intervalo | |
| 16h50–17h40 | Passos 3 a 6: `TokenService`, `POST /user` e `POST /authenticate`, filtro, `SecurityFilterChain`. Teste completo: cadastrar → autenticar → sem token 401 → com token 200 | Aula 08, seção 06 |
| 17h40–17h55 | Além da autenticação: por que desligar o CSRF aqui é aceitável, cabeçalhos de segurança, **regra de propriedade (404 para chamado de outro usuário)** | Aula 08, seção 07 + especificação, seção 06 |
| 17h55–18h00 | Chamada; casa: cada grupo leva o fluxo para o projeto | |

**Atenção:** a turma precisa saber que o texto do material do aluno oficial sobre Spring Security está defasado
(seção 5). O roteiro da Aula 08 já avisa disso.

### A8 · Seg 16/11 — Laboratório do projeto

| Horário | Bloco | Origem |
|---|---|---|
| 15h00–15h15 | Checklist de aceite projetado na tela e o mapa de ameaças explicado (o que cada linha pede no README) | Aula 09, seção 02 + especificação, seção 10 |
| 15h15–16h30 | Grupos em salas; **check-in em cada sala a cada ~20 min** (problemas mais comuns na seção 03 do roteiro da Aula 09) | Aula 09, seção 03 |
| 16h30–16h50 | Intervalo | |
| 16h50–17h40 | Salas | |
| 17h40–17h55 | **Ensaio relâmpago:** cada grupo mostra em 2 min o fluxo cadastrar → autenticar → 401 → 200 → 404 | Especificação, seção 13 |
| 17h55–18h00 | Chamada; lembrete: presença obrigatória em 18/11 e upload individual | |

### A9 · Qua 18/11 — Apresentações e encerramento

Segue a Aula 09 do roteiro, que tem abertura, desenvolvimento com suporte, apresentações e devolutiva da rubrica.
Encaixe sugerido: ajustes até 16h20, upload no LMS até 16h30, intervalo, apresentações de 16h50 a 17h25 (4 grupos ×
8 min) e devolutiva com autoavaliação até 17h55.

---

## 4. Projeto final: o que acompanhar

| Data | Checar com cada grupo |
|---|---|
| 06/11 | Como o grupo compartilha o código sem Git; divisão de frentes; entidades `Usuario` e `Chamado` |
| 09/11 | UC3 a UC6 rodando sem segurança; extras escolhidos (1 extra; 2 para grupos de 4 ou mais) |
| 11/11 | Tratamento global de erros e validação; busca por título parametrizada |
| 13 e 16/11 | Cadastro e autenticação; filtro; regra de propriedade; mapa de ameaças e README |
| 18/11 | Entrega (zip sem `target`, enviado por **cada** integrante) e apresentação com todos presentes |

**Grupos com risco:**

- O Grupo 4 tem 3 pessoas, uma delas com pouco Java.
- Integrantes sem prática de back-end: Caio (Grupo 1), Henrique e Edgar (Grupo 2), Vanessa (Grupo 4). A rubrica é
  individual e pergunta sobre partes que a pessoa não escreveu (especificação, seção 14).

---

## 5. Material do aluno: precisamos?

**Como fizemos nas outras turmas da Escalação Tech (Caixa):**

- Cada aula teve um `aula-0X-aluno.md` (BE-JV-009 na 1749, BE-JV-010 na 1751, PE-JV-001 na 1751).
- Esse texto era curto, alinhado ao código da aula, com a pilha fixada e o código verificado.
- Foi publicado aula a aula, sem o conteúdo do instrutor.

**O que existe para esta turma:**

| Peça | Situação |
|---|---|
| Roteiros do Hagamenon | Para o **instrutor**: cheios de "pergunte", "cheque" e respostas esperadas. Não servem como texto do aluno sem adaptação. |
| Código por aula (`source/` + `README.md`) | Bom para o aluno. **Não foi publicado** (promessa de 30/10 e 04/11 não cumprida). Como o GitHub é bloqueado na rede da Caixa, o caminho é zip no LMS. |
| Especificação do projeto | Pronta e já no LMS. |
| Material do aluno oficial (14 textos, ~1.500 linhas) | Curto e genérico na maior parte. O `14_Spring-Security.md` está **defasado**: usa `WebSecurityConfigurerAdapter`, removido no Spring Security 6 / Boot 3, e a biblioteca `jjwt` antiga, enquanto a aula usa `java-jwt` com Boot 3.3.5. Quem copiar dele para o projeto **não compila**. |
| Listas de exercícios | 40 questões no módulo (`Exercícios/BE_JV_013_*.json`), citadas em todos os roteiros. **Não se sabe** se estão liberadas no LMS da turma. |

**Decisão (D2): opção (c).** A ordem de produção segue abaixo.

1. **Publicar o código** de `aula01` a `aula03` já (zip por aula no LMS) e o de cada aula seguinte depois dela.
   **Verificado em 05/11/2026** com JDK 21 e Maven Central: todos os projetos passam no `mvn test` (aula01: 3 testes;
   aula02: 26; aula03: 43; aula04: 30; aula05: 52; aula07: 49; aula08: 17; nenhuma falha). Ainda falta conferir com
   o Nexus da rede da Caixa e decidir se o `DesafioController` vai com a solução, como o próprio arquivo prevê.
   **Atenção à ordem de publicação:** a chave fraca do desafio da A4 (Bloco 6 da Aula 03) está escrita no
   `DesafioController`, nos testes e nos README de `aula03` e `aula04`. Também aparece nos projetos seguintes, porque
   `ada123` é a senha do perfil `basica` da `aula08`. Publicar o código de `aula03` **depois** da A4 (06/11)
   preserva o desafio sem alterar o material do Hagamenon.
   **O que vai no zip de cada aula:** só a pasta `aulaNN/source/`, sem `target/` e sem os `roteiroaulaNN.md`, que são
   do instrutor. Os README de `aula03` e `aula04` têm um link para o roteiro, que ficará quebrado no zip; o efeito é
   pequeno e não exige alterar o arquivo.
2. **Um texto do aluno por encontro, de A4 a A9**, em `plano-de-aulas/aula-0N-aluno.md`, no formato das outras
   turmas: leitura curta, cenário, perguntas de reflexão, o que rodar no código da aula e o que levar para o projeto.
   Prioridade de produção: A4 (06/11) primeiro, depois A7 (Spring Security), que é o que o projeto mais exige.
3. **Versões, com sutileza.** O texto da A7 traz uma nota de versão, no mesmo formato usado nas turmas de Quarkus. O
   Spring Security evoluiu: por muitos anos a configuração foi feita estendendo `WebSecurityConfigurerAdapter`, e é
   assim que muitos tutoriais e textos de referência ainda mostram. A classe foi depreciada na versão 5.7 e removida
   na 6.0, que é a do Spring Boot 3; desde então se usa um bean `SecurityFilterChain`. A nota não cita nem corrige o material oficial: só explica por que o aluno vai
   encontrar as duas formas e qual usar no projeto.

---

## 6. Decisões

**Tomadas pelo PO (Jessé) em 05/11/2026:**

| # | Decisão |
|---|---|
| D1 | **Ordem reorganizada** das Aulas 4 a 9, como na seção 2. |
| D2 | **Material do aluno completo (opção c):** código publicado e um texto do aluno por encontro, de A4 a A9. A diferença entre versões do Spring Security é tratada **de forma sutil**, como evolução do framework, sem desqualificar o material oficial (seção 5). |
| D3 | **O que o Hagamenon decidiu e combinou com a turma fica mantido.** Isso inclui os 4 grupos como estão (incluindo os de 5 pessoas), o Git fora dos requisitos, a entrega em zip no LMS, a presença obrigatória em 18/11, o intervalo de 20 min e a especificação do projeto, inclusive o cabeçalho do token (`access_token` ou `Authorization: Bearer`, ao menos um). O novo instrutor não reabre esses pontos. |

**A esclarecer com a turma na A4, sem mudar o combinado:**

- Como cada grupo está compartilhando o código, já que o Git saiu dos requisitos.
- Quantos extras cada grupo vai fazer. A especificação pede 1, ou 2 para equipes de 4 pessoas, e não fala de grupos de 5.

**Lacuna na especificação:** a seção 02 fala em "aplicação própria" e anuncia "uma divisão sugerida" de frentes, mas
a lista não está no arquivo. O cronograma (seção 12) também cita a escolha entre "padrão ou aplicação própria". Vale
esclarecer com a turma na A4.

---

## 7. Preparação antes de 06/11

1. Combinar com a coordenação o acesso ao LMS da turma (Pasta Geral, listas de exercícios, presenças das Aulas 1 a 3).
2. Rodar `aula03/source` e `aula04/source` na máquina do novo instrutor e ensaiar as requisições da A4.
3. Avisar a turma, na abertura da A4, da reorganização das aulas (D1). O projeto, os grupos e os combinados não mudam (D3).
4. Publicar o código das aulas 1 a 3, depois da verificação de build. Publicar é ação do instrutor ou da coordenação.
