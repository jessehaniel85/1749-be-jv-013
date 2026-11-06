# Turma 1749 · BE-JV-013 Fundamentos de segurança para aplicações — passagem de turma

> **Documento de coordenação, para o instrutor que assume a turma em andamento. Não vai para os alunos.**
> Elaborado em 05/11/2026 a partir das gravações das Aulas 1 a 3 (28/10, 30/10 e 04/11/2026).

| Item | Valor |
|---|---|
| Turma | 1749 · Escalação Tech 02 · Dev. Back-End Java Intermediário (Nível II) · Caixa |
| Módulo | BE-JV-013 Fundamentos de segurança para aplicações (2º módulo da turma; o 1º foi o BE-JV-009, Arquitetura de Software e Ágil I) |
| Horário | Seg/Qua/Sex, 15h às 18h (Brasília). Intervalo combinado: 16h30 às 16h50 |
| Período | 28/10 a 18/11/2026 · 9 aulas (Seg 02/11 foi feriado) |
| Instrutor até agora | Hagamenon Nicolau de Oliveira ("Nicolau"), conta Teams "Educacional 07"; mora em Portugal |
| Aulas dadas | 3 de 9 (28/10, 30/10, 04/11). **Próxima: Sex 06/11 (Aula 4)** |
| Avaliação | Projeto em grupo, apresentado e entregue em **Qua 18/11** (Aula 9) |

**Conteúdo desta pasta**

| Arquivo | O que é |
|---|---|
| `README.md` | Índice curto da pasta. |
| `passagem-de-turma.md` | Este resumo geral das Aulas 1 a 3. |
| `plano-de-continuidade.md` | Proposta para as Aulas 4 a 9. |
| `transcricoes/aula-0N-AAAA-MM-DD-transcricao.txt` | Transcrição automática integral de cada aula, com marcas `[hh:mm:ss]` da posição no vídeo. |
| `notas-por-aula/aula-0N.md` | Notas detalhadas de cada aula: linha do tempo, conteúdo com marcas de tempo, código mostrado, turma, dúvidas, imprecisões, citações. |

**Limites da evidência.** A transcrição é do Whisper (modelo `small`) e erra nomes e termos técnicos; as notas
corrigem os casos óbvios. Os quadros foram capturados a cada 5 min. As gravações mostram só a sala principal, então o
trabalho dos grupos nas salas do Teams (cerca de 1h por aula nas Aulas 2 e 3) **não foi registrado**, e o chat também
não. A gravação da Aula 3 termina às ~17h29, sem chamada nem encerramento.

> **Atualização de 05/11, à noite.** Chegou o material do Hagamenon (`material-instrutor-anterior/` (conteúdo do `aula.zip`)): roteiro e código das 9
> aulas, a especificação do projeto (`apichamadosespecificacao.md`, com rubrica na seção 14) e o plano de ensino. O
> projeto `api-alunos` deixou de ser pendência. O que muda está consolidado em `plano-de-continuidade.md`; as
> seções abaixo descrevem o que se via **nas gravações**.

---

## 1. Resumo em 10 pontos

1. **O cronograma está cerca de uma aula atrasado.** A Aula 1 não terminou XSS/CSRF/DoS, e a retomada consumiu ~40 min
   da Aula 2. A Aula 2 não deu Cookies, que tomaram ~55 min da Aula 3. Da Aula 3, o JWT ficou só na parte conceitual
   e na geração manual do token. A **validação do token no servidor e o JWT com biblioteca ou Spring não foram feitos**.
2. **Anunciado para a Aula 4 (06/11):** "OAuth 2 já com o JWT" e "implementar o JWT de forma mais elegante, com as
   funções do Spring". As classes `JwtBiblioteca` e `JwtController` do projeto de demonstração parecem ser esse
   material, mas nunca foram abertas em aula.
3. **Ensino com código.** Todas as demos estão num projeto Spring Boot `api-alunos` (pacote `com.ada.seguranca`), que
   estava **na máquina do Nicolau**. Ele prometeu publicar o código para os alunos, mas até 30/10 não havia
   publicado. O código já chegou no `aula.zip`; falta publicá-lo para a turma (ver `plano-de-continuidade.md`).
4. **Projeto avaliado:** uma API de chamados (*help desk*) com camada de segurança em Spring. A especificação está
   no LMS. A turma está em 4 grupos (seção 5). Os pontos combinados com a turma são os da tabela abaixo. O próprio
   Nicolau chamou o projeto de "muito grande, muito audacioso" e disse que não precisa estar 100% pronto.

   | Ponto | Combinado |
   |---|---|
   | Entrega | Zip sem a pasta `target`, enviado no LMS no último dia (18/11) |
   | Apresentação | Em 18/11, com presença obrigatória de todos: quem faltar fica sem nota |
   | Exigência dita em aula | Tratar SQL injection (injeção de SQL) e XSS (injeção de script na página) |
   | Git | Deixou de ser requisito em 04/11; o Nicolau prometeu tirá-lo do documento |
5. **Imprecisões técnicas repetidas.** São 15 ou mais por aula, e algumas afetam o projeto diretamente:
   - SHA-256 apresentado como bom para guardar senha;
   - MD5 chamado de criptografia;
   - CSRF descrito como roubo de cookie;
   - Client Credentials confundido com Basic Auth;
   - assinatura HS256 ligada a chave assimétrica.

   A seção 6 lista o que vale corrigir primeiro.
6. **A turma** tem ~18 empregados da Caixa, muito heterogêneos. Há quem não programe (Scrum Master) e quem venha de
   mainframe, MUMPS ou PHP, até quem já trabalhe com Java/Spring/Quarkus. Quem participa responde bem a perguntas, e
   cerca de 5 a 7 pessoas falam com frequência.
7. **Parte da turma bate ponto** e às vezes sai cedo ou volta atrasada do intervalo. Na Aula 1, muitos foram embora
   depois do intervalo, e o Nicolau reclamou disso na Aula 2.
8. **Restrições de rede da Caixa:** GitHub público bloqueado; o acesso a sites externos (PortSwigger, XSS game) não
   foi confirmado.
9. **Operação no Teams:** as salas de grupo funcionam, mas houve erros para entrar (alunos caindo ou sem sala), e o
   chat das salas some quando elas fecham. A turma criou um espaço próprio no Teams ou no WhatsApp.
10. **Clima:** cordial, com câmeras abertas a partir da segunda metade da Aula 1. Os alunos trazem casos reais do
    banco (phishing interno, DDoS no SiSU, assinatura digital de juiz) e às vezes corrigem o instrutor, como na fala
    sobre Argon2id e BCrypt. Um instrutor com respostas firmes em hash, senha e tokens tende a ser bem recebido
    (inferência).

---

## 2. Cronograma: plano do Nicolau × o que aconteceu

Plano de ensino que ele mostrou na Aula 1. O PDF com a mesma grade está no LMS, na "Pasta Geral" da turma.

| Aula | Data | Tema no plano dele | O que aconteceu / situação |
|---|---|---|---|
| 1 | Qua 28/10 | Introdução: XSS, CSRF, DoS | Começou ~15h20, com atraso por problema de acesso à conta. Teve apresentações dos alunos, debate de ameaças, dinâmica em grupo, tríade confidencialidade-integridade-disponibilidade e XSS só nos últimos 10 min (XSS game do Google, nível 1). CSRF e DoS ficaram para a aula seguinte. |
| 2 | Sex 30/10 | HTTPS e Cookies | Retomada de XSS, SQL injection improvisado, CSRF e DoS/DDoS (~40 min). Depois: hash × criptografia, AES-GCM, RSA, assinatura e HTTPS com o certificado do LMS. **Cookies não dados.** Formação dos grupos e ~50 min de projeto em salas. |
| 3 | Qua 04/11 | JWT | Cookies e sessão (~55 min, com demo), JWT conceitual e geração manual (~33 min), ~33 min de projeto em salas. **Validação de JWT e uso de biblioteca não feitos.** |
| 4 | Sex 06/11 | OAuth 2.0 | Anunciado: OAuth 2 "já com o JWT" e JWT com Spring. |
| 5 | Seg 09/11 | Single Sign-On (SSO) | — |
| 6 | Qua 11/11 | OpenID Connect | — |
| 7 | Sex 13/11 | Vulnerabilidades reais + tratamento de exceções | — |
| 8 | Seg 16/11 | Spring Security: autenticação básica e JWT | O comentário no código manda o BCrypt para esta aula. |
| 9 | Qua 18/11 | Projeto e encerramento | Apresentação e entrega do projeto. |

Na Aula 1, o Nicolau narrou as datas com trocas ("dia 11" aparece duas vezes). As datas das Aulas 5 a 8 acima
seguem a grade Seg/Qua/Sex sem o feriado de 02/11 e coincidem com o período "28/10 a 18/11" do plano. **Convém
confirmar com a coordenação.**

Ele mesmo disse que o plano é "o norte, não uma caixa": o que não for visto passa para a aula seguinte.

**Comparação com o planejamento oficial do módulo**
(`Modulos/BE-JV-Backend-Java/BE-JV-013 Fundamentos de segurança para aplicações/Planejamento/planejamento.md`):

- A sequência de temas é a mesma.
- O projeto oficial é outro: autenticação com Spring Security, `POST /user` e `POST /authenticate` devolvendo um
  JWT, enviado depois no cabeçalho `access_token`.
- O Nicolau trocou esse projeto pela API de chamados com segurança. O núcleo de autenticação e autorização do
  projeto oficial cabe dentro dela.

---

## 3. O que cada aula cobriu (detalhe nas `notas-por-aula/`)

### Aula 1 · Qua 28/10 — Abertura

- **Apresentação do Nicolau:**
  - Java desde 2010 (Struts, JSF, Spring);
  - trabalhou numa empresa de certificação digital;
  - diz ter as certificações ISO 27001/27002.
- **Combinados:** câmeras abertas, interromper sem levantar a mão, intervalo 16h30–16h50, e faltas tratadas
  primeiro com o instrutor antes de ir à ouvidoria.
- **Rodada de apresentação dos alunos**, com a pergunta "quando ouço segurança de aplicações penso em…" (perfis na
  seção 5).
- **Debate:** que ataques sofreria um sistema de agendamento de câmbio (DDoS, phishing, engenharia social, SQL
  injection). O Paulo contou um DDoS real no SiSU/MEC; o Vinícius, o teste de phishing interno da Caixa.
- **Dinâmica em 4 salas (10 min):** escolher um sistema do dia a dia (escola, e-commerce, iFood) e listar o que ele
  guarda de valor, o impacto de uma invasão e a prevenção. Os grupos apresentaram bem.
- **Conceitos:** autenticação em dois fatores, tríade confidencialidade-integridade-disponibilidade (os alunos
  acrescentaram autenticidade, não repúdio e conformidade com a LGPD) e assinatura digital × eletrônica.
- **XSS:** demo no XSS game (`<script>alert('XSS')</script>`) e código "vulnerável" mostrado na IDE, mas não
  executado.
- **Projeto:** apresentado por alto, com especificação em `.md`, casos de uso, arquitetura sugerida, grupos de 2 a 3
  (depois virou 3 a 5) e apresentação em 18/11 com presença obrigatória.

### Aula 2 · Sex 30/10 — Retomada + criptografia + HTTPS

- **XSS:** `BuscaController`, com `GET /busca?q=` concatenando no HTML (vulnerável) × `GET /busca/segura` com
  `HtmlUtils.htmlEscape` (corrigido).
- **SQL injection:** improvisado a partir da pergunta de um aluno. Query concatenada e `' or 1=1`; a solução
  indicada foi `PreparedStatement` ou JPA. Ele mesmo disse "não vim preparado".
- **CSRF:** página da PortSwigger Web Security Academy e explicação do token CSRF, sem código.
- **DoS/DDoS:**
  - demo do Resilience4j RateLimiter em `/alunos`, com 1 chamada a cada 10 s, configurado no `application.yml`;
  - pergunta à turma se isso basta (o Diego respondeu certo que não);
  - slides de camadas: Cloudflare → Nginx/HAProxy → aplicação, e load balancer.
- **Hash × criptografia:**
  - classe `FuncaoHash` (SHA-256) e `CriptografiaSimetrica` (AES/GCM, IV aleatório, saída em Base64);
  - RSA-2048 e assinatura de documento pelo hash;
  - testes em `Aula02CriptoTest`.
- **HTTPS/TLS:** certificado `*.ada.tech` (Google Trust Services, válido de 26/10/2026 a 25/01/2027), cadeia de
  certificação e certificado autoassinado.
- **Grupos** formados pela turma (mantiveram os do módulo anterior) e ~50 min de trabalho em 4 salas, que não ficou
  gravado.

### Aula 3 · Qua 04/11 — Cookies, sessão e JWT

- **Cookies:**
  - conceito, consentimento, tipos e limite de 4 KB;
  - `CookieController` com `GET /boas-vindas` (lê o cookie `user-id`);
  - `GET /login-teste` grava o cookie com `HttpOnly`, `Secure`, `SameSite=Strict` e `Max-Age` de 1 h;
  - `GET /login-teste/legivel` grava um cookie sem `HttpOnly`;
  - DevTools mostrando que `document.cookie` não enxerga o cookie `HttpOnly`;
  - alerta: "se eu troco 42 por 55, viro o usuário 55".
- **Sessão:**
  - três tipos de sessão (navegador, servidor, usuário);
  - session ID gerado com `SecureRandom`;
  - `SessaoController` + `UsuariosDemo`: `POST /sessao/login`, `GET /sessao/perfil` (`JSESSIONID`) e
    `POST /sessao/logout` com `invalidate()`;
  - "sessão no servidor guarda estado, logo não é RESTful".
- **Senha:** um aluno (provavelmente o Benjamin) apontou que SHA-256 é rápido demais para senha e citou
  Argon2id/BCrypt. O Nicolau concordou e remeteu para "a próxima aula, com o Spring".
- **JWT:**
  - perguntas provocativas: "dá para roubar?", "dá para forjar?";
  - jwt.io, RFC 7519, header.payload.signature, *claims* (`iss`, `sub`, `iat`, `exp`, `role`);
  - "Base64 não é criptografia";
  - assinatura HMAC;
  - `JwtManual` monta o token à mão (`iss=ada-seguranca`, `sub=ana`, `role=USER`, validade de 5 min);
  - analogia: "JWT é um crachá".
- **Gancho para OAuth 2**, confundindo Client Credentials com Basic Auth (seção 6).
- **`DesafioController`:** só apresentado ("um probleminha para vocês resolverem; a solução está no arquivo").
- **Projeto:** ~33 min em salas. O Git deixou de ser requisito. A gravação acaba às ~17h29, sem chamada.

**Onde a aula parou:** cookies e sessão estão fechados, e o JWT foi visto do lado de quem emite, montado à mão.
Falta o lado de quem recebe: validar a assinatura e a expiração, usar biblioteca ou Spring, renovação e revogação.

---

## 4. Ponto de partida técnico (o que o novo instrutor herda)

**Projeto de demonstração `api-alunos`** (Spring Boot, `ApiAlunosApplication`, porta 8080, HTTP). Caminho na máquina
do Nicolau: `C:\Users\nicolau.oliveira\ada\BE-JV-013 Fundamentos de segurança para aplicações\aula\aula0N\source`.
Pacotes de `com.ada.seguranca`:

| Pacote | Classes | Usado em aula? |
|---|---|---|
| `aluno` | `Aluno`, `AlunoController` (`/alunos`, com rate limiter) | Sim (Aula 2) |
| `xss` | `BuscaController` (`/busca`, `/busca/segura`) | Sim (Aula 2) |
| `cripto` | `FuncaoHash`, `HashController`, `CriptografiaSimetrica`, `CriptografiaAssimetrica`, `CriptografiaController`, `TextoCifradoInvalidoException` | Sim (Aula 2) |
| `cookie` | `CookieController` | Sim (Aula 3) |
| `jwt` | `SessaoController`, `UsuariosDemo`, `JwtManual` | Sim (Aula 3) |
| `jwt` | `JwtBiblioteca`, `JwtController` | **Não abertos** |
| `jwt` | `DesafioController` (desafio com solução no próprio arquivo) | Só apresentado |

Outros arquivos: `http/aula03.http` (requisições do cliente HTTP do IntelliJ), `http-client.env.json`, testes
`Aula02CriptoTest`, `README.md`, dependência `resilience4j-spring6` 2.2.0.

O código é muito comentado em português e traz referências às aulas e perguntas do tipo "PERGUNTE: …" para a turma.
Duas dessas perguntas ainda não foram feitas:

- "3 instâncias atrás de um balanceador: o que acontece com a sessão?";
- "com token, o logout é tão simples?".

As duas são boas pontes para OAuth 2 e para a revogação de JWT.

**Para pegar com a coordenação ou com o Nicolau antes de 06/11:**

1. ~~O projeto `api-alunos` completo e os slides~~ — recebidos no `aula.zip` em 05/11.
2. ~~O `plano-ensino.pdf` e o `apichamadosespecificacao.md`~~ — recebidos no `aula.zip` em 05/11.
3. Se o código já foi publicado para os alunos, e onde.
4. Se o documento do projeto já foi atualizado sem o requisito de Git (a cópia do `aula.zip` não fala em Git
   obrigatório, mas cita "como compartilhar o código entre os integrantes").
5. As presenças das Aulas 1 a 3, sobretudo a do Matheus em 04/11 (saiu às 17h20 com combinado de presença) e a da
   Aula 3, cuja chamada não ficou gravada.
6. ~~A rubrica~~ — está na seção 14 da especificação (critérios por habilidade; nota individual dentro do grupo).

---

## 5. A turma

Os nomes vêm da lista do Teams. O perfil vem das apresentações da Aula 1, salvo indicação.

| Aluno | Grupo | Perfil | Participação |
|---|---|---|---|
| Matheus Fonseca Sousa | 3 | Testes de qualidade no sistema lotérico; TCC sobre testes de segurança estáticos e dinâmicos (SAST/DAST) | Alta; primeiro a falar. Saiu às 17h20 em 04/11 (formatura) |
| Paulo Ueiner Moreira de Assis | 1 | Na Caixa desde 2021, no mercado desde 2006 (PHP, BI, dados no MEC); montou a arquitetura Quarkus de um sistema de treinamento ligado ao Bolsa Família; Java recente | Alta; experiente; contou o DDoS no SiSU |
| Vinicius Silva de Oliveira | 4 | Back-end em depósitos judiciais (valida assinatura digital de juiz); aprendeu a programar na Caixa | Média-alta; boas perguntas (hash, HttpOnly) |
| Ian Pereira da Silva Fernandes | 1 | Dev em equipe que vai refatorar para Java 21; "maratona de estudos" | Média-alta |
| Diego Marques Soares | 3 | Sistema de mensuração de pontos de função; Java + Angular | Média; respostas certeiras (rate limiter) |
| Tiago da Silva Valeriano de Oliveira | 1 | Não se apresentou na Aula 1; raciocínio de quem integra APIs | Média na Aula 3 (perguntas boas sobre JWT); voltou atrasado do intervalo |
| Benjamin da Nobrega Bezerra | 4 | Java no trabalho há ~1 ano; sistema de programas sociais | Média; provável autor do comentário sobre Argon2id/BCrypt |
| Helio Serrano Brandao Junior | 2 | Na Caixa desde 2003; dev desde ~2020 (PHP/Laravel); **área de segurança, squad de Biometria**; Java 8/JBoss migrando para Spring | Média |
| Eldio Teixeira Junior | 1 | Em TI desde 2000, dev desde 2010 (JSF, JBoss, Quarkus, Spring Batch); relatórios da intranet | Baixa na fala, câmera sempre aberta |
| Henrique Campanharo Seabra | 2 | Mainframe (COBOL, JCL, batch); base em C; **pouco Java** | Média na Aula 1 |
| Edgar Guimaraes Damazio Junior | 2 | ~1 ano de Caixa; gestão de acesso a agências; mais front-end, **ainda sem back-end** | Baixa-média |
| Karla Danielle Santos da Silva | 2 | Sistema de processos disciplinares (termo incerto) | Baixa |
| Vanessa Soares Borges da Silva | 4 | Nova na Caixa e em TI; legado da tesouraria em MUMPS/Caché ObjectScript; **pouco Java** | Média; saiu cedo na Aula 1 |
| Levy Washington Rocha Moreira | 3 | Legados de loterias; Java no back-end, TypeScript no front | Média; saiu às ~16h28 na Aula 1 (ponto) |
| Caio Cesar da Matta Souza | 1 | ~2 anos em TI; **Scrum Master, não codifica**; estudando para migrar | Baixa; ficou na sala principal durante todo o trabalho em grupo da Aula 2 |
| Edinilson Dafonte | 3 | Não se apresentou | Só respondeu à chamada |
| João Pedro Moreto Lourenção | 3 | Estava de férias na Aula 1; entrou na Aula 3 | Grupo definido em 04/11 |
| Roberto | ? | Citado só na chamada da Aula 1 | Sem resposta; não reaparece |

**Grupos do projeto.** Foram formados em 30/10, mantendo os do módulo anterior, e conferidos com as falas de 04/11:

| Grupo | Integrantes |
|---|---|
| Sala 1 | Caio, Eldio, Ian, Paulo, Tiago |
| Sala 2 | Edgar, Hélio, Henrique, Karla |
| Sala 3 | Diego, Edinilson, João Pedro, Levy, Matheus |
| Sala 4 | Benjamin, Vanessa, Vinícius |

O Whisper transcreve "Eldio" e "Hélio" do mesmo jeito. A distribuição acima usa a fala do Paulo em 04/11 ("estou
com o Eldio, o Tiago e o Ian"); **confirme na primeira aula.**

**Atenção pedagógica (inferência a partir dos perfis):**

- Caio, Henrique, Vanessa e Edgar têm pouca ou nenhuma prática de back-end Java. O Grupo 4 tem só 3 pessoas, e uma
  delas é a Vanessa.
- Vale acompanhar se essas pessoas estão programando no projeto ou só assistindo.

---

## 6. Imprecisões ditas em aula que vale corrigir (por prioridade)

Lista completa, com marcas de tempo, nas `notas-por-aula/` (seção 8 de cada uma). Prioridade = impacto no projeto
e nas próximas aulas.

1. **Senha com SHA-256** (Aulas 2 e 3). Foi apresentado como "um dos melhores", sem sal nem algoritmo lento. Para
   senha, o correto é BCrypt, Argon2 ou PBKDF2; no Spring, `PasswordEncoder`/`BCryptPasswordEncoder`. Os grupos
   vão guardar senha no projeto.
2. **Client Credentials = "login e senha em toda requisição"** (Aula 3). Isso é Basic Auth. Client Credentials é o
   fluxo do OAuth 2 entre máquinas: `client_id`/`client_secret` trocados por um *access token*. Como a próxima aula
   é OAuth 2, é o ponto mais urgente.
3. **CSRF descrito como roubo ou leitura do cookie** (Aulas 2 e 3). No CSRF o navegador envia o cookie sozinho, e
   quem lê o cookie é o XSS. Falta a defesa `SameSite`. Também falta explicar quando é aceitável desligar o CSRF no
   Spring Security (API stateless com token), senão a turma vai achar que desligar é falha.
4. **HS256 ligado a chave assimétrica** (Aula 3). HS256 usa segredo compartilhado (simétrico); RS256 e ES256 usam
   par de chaves. Essa distinção é a base para OAuth 2, OpenID Connect e JWKS.
5. **"Com JPA já está tratado" / "hoje é muito difícil fazer SQL injection"** (Aula 2). Só vale com consulta
   parametrizada. JPQL ou SQL nativo concatenado continua vulnerável, e o projeto exige tratar SQL injection.
6. **MD5 chamado de criptografia; "curva elíptica" na explicação do hash** (Aula 2). Hash é função de mão única,
   e MD5 é hash. Um aluno ficou sem resposta ("não vou saber te explicar").
7. **Cookies:**
   - sessão × `Max-Age` × *Session Storage* foram misturados (sem `Max-Age`, o cookie é de sessão);
   - "cookie permanente não expira nunca" está errado;
   - "`Secure` não funciona em HTTP" foi contradito pela própria demo em `localhost` (Aula 3).
8. **Base64 "deixa mais compacto"** (Aula 3): Base64 aumenta o tamanho em ~33%.
9. **Rate limiter "por pessoa"** (Aula 2): a configuração mostrada é um limite global, não por cliente.
10. **Fatos citados sem base:**
    - Log4Shell datado em 2019/2020 (foi em dezembro de 2021);
    - "certificados valem 1 a 2 anos" (o que ele mostrou vale ~3 meses);
    - WebTrust como "autoridade máxima" (é um programa de auditoria de autoridades certificadoras);
    - "dois tipos de XSS" (são três: refletido, armazenado, DOM);
    - "falha grave recente no PHP" derrubando sites de governos: **não verificado**.
11. **Tom:** críticas ao Nubank e a bancos digitais diante de uma turma de empregados da Caixa (Aula 1).

---

## 7. Recomendações para a continuidade

As recomendações da primeira versão deste documento foram substituídas por **`plano-de-continuidade.md`**, escrito
depois da leitura do material completo do Hagamenon (roteiros e código das 9 aulas). Lá estão a proposta para as
Aulas 4 a 9, o acompanhamento do projeto, o material do aluno e as decisões pendentes.
