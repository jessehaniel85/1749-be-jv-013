# Aula 1 — turma 1749 (Caixa) · BE-JV-013 Fundamentos de segurança para aplicações · Qua 28/10/2026

Notas para o instrutor que assume a turma em andamento. Fontes: transcrição automática (Whisper small, gerada em
05/11/2026) e quadros do vídeo a cada 5 min (`f_001` = 0 min … `f_033` = 160 min). Instrutor da aula: Hagamenon
Nicolau de Oliveira ("Nicolau", conta Teams "Educacional 07"). Marcas `[hh:mm:ss]` = posição no vídeo; horário real
= 15h15 BRT + marca.

> **Aviso sobre o relógio.** O relógio da barra de tarefas do Nicolau (hora de Lisboa, UTC+0 desde 25/10) marca
> 18h23 no quadro de 5 min (`f_002`) e 20h53 no de 155 min (`f_032`). Convertido para Brasília, isso dá 15h23 e
> 17h53, ou seja, cerca de 3 min a mais que a conta "15h15 + marca". Os horários abaixo seguem a conta pedida
> (15h15 + marca). O início real da gravação pode ter sido por volta de 15h18.

Correções de termos aplicadas no texto: "Agameno" = Hagamenon; "Alt2/Alt-2/Auto e 2" = OAuth 2; "ZWT/JWP/VWT" = JWT;
"ATP" = OTP; "CVF" = CVE; "Carla/Carlos" = Karla; "Iã/Ia/Yann/Guiân" = Ian; "Guijamir/Beija a mim/enjaminho" =
Benjamin; "Levi/Lili" = Levy; "Té, o Dio/Ed Nielson" = Eldio (confirmado pela lista de participantes e pela ordem
alfabética da chamada); "Spring Bet" = Spring Batch; "Celene" = Selenium; "iShark" = Wireshark; "CISU lá no MAC" =
SiSU no MEC (inferência); "COS, caixa object script" = Caché ObjectScript (inferência: linguagem da InterSystems
baseada em MUMPS).

---

## 1. Linha do tempo

| Horário BRT | Marca | Duração | O que aconteceu |
|---|---|---|---|
| 15h00 | — | — | Início previsto. A aula não começou no horário: o Nicolau teve problema de acesso à conta ("problemas com o fuso de horário", "o OTP não estava a funcionar") `[00:01:12]`. |
| 15h15 | 00:00 | — | Início da gravação. |
| 15h16–15h20 | 00:01–00:05 | ~4 min | Saudação, desculpas pelo "super atraso", preparação para compartilhar a tela. |
| 15h20–15h31 | 00:05–00:16 | ~11 min | Apresentação pessoal do Nicolau; pedido de câmeras abertas; e-mail e link (provavelmente LinkedIn) no chat; regra de tratar faltas com ele antes da ouvidoria. |
| 15h31–15h33 | 00:16–00:18 | ~2 min | Combinado do intervalo (16h30–16h50). Tenta abrir salas de grupo (breakout rooms) e não consegue. |
| 15h33–15h43 | 00:18–00:28 | ~10 min | Mostra o "Plano de Ensino.docx" (objetivo, conteúdo, justificativa, aulas, links, bibliografia). Promete PDF no LMS. |
| 15h43–15h47 | 00:28–00:31 | ~3 min | Mostra um arquivo `.md` com a especificação do projeto avaliado (o projeto não aparece nos quadros capturados). |
| 15h47–16h12 | 00:31–00:57 | ~25 min | Rodada de apresentação dos alunos (nome, área, Java, "quando ouço segurança de aplicações penso em…"). |
| 16h12–16h31 | 00:57–01:15 | ~19 min | Debate: que ataques sofreria um sistema de agendamento de câmbio? DDoS, phishing, engenharia social, SQL injection. Levy sai mais cedo (~16h27). |
| 16h31–16h52 | 01:15:52–01:36:36 | ~21 min | **Intervalo** (timer na tela; trecho sem fala). |
| 16h52–16h58 | 01:36–01:43 | ~6 min | Nicolau consegue acesso às salas; explica atividade em grupo e cola o enunciado no chat. |
| 16h58–17h08 | 01:43–01:53 | ~10 min | **Trabalho em grupo em breakout rooms** (4 salas, aleatórias). Gravação na sala principal sem fala. |
| 17h08–17h35 | 01:53–02:20 | ~27 min | Volta das salas (Karla caiu da sala); cada grupo apresenta. Digressão sobre tipos de teste `[02:08–02:13]` e sobre HTTPS/rede pública. |
| 17h35–17h43 | 02:20–02:28 | ~8 min | Tríade CIA (confidencialidade, integridade, disponibilidade); alunos acrescentam irretratabilidade, conformidade, autenticidade; digressão sobre assinatura digital × eletrônica. |
| 17h43–17h45 | 02:28–02:30 | ~2 min | Chamada oral. Vanessa avisa que precisa sair. |
| 17h46–17h56 | 02:31–02:41 | ~10 min | XSS: demo no XSS game do Google (nível 1) e código "vulnerável" mostrado na IDE sem executar. |
| 17h56–17h57 | 02:41–02:42 | ~1 min | Encerramento; promete publicar plano e trabalho no LMS. |
| ~18h00 | 02:45 | — | Fim da gravação (duração informada 2h45). |

Tempo líquido de conteúdo técnico expositivo: cerca de 30 min (CIA + XSS + digressões); o restante foi
apresentação, debate e dinâmica de grupo.

---

## 2. Conteúdo dado

| Tema | Marca | Profundidade | Detalhes |
|---|---|---|---|
| "Segurança não é só código" / elo mais fraco | `[00:11:00]`, `[01:13:19]` | Explicado (repetido várias vezes) | "Uma corrente é tão forte quanto seu elo mais fraco": não adianta criptografia, JWT, OAuth 2, Spring Security se a senha está anotada num post-it no monitor. Cita política de senhas (troca a cada 30 dias, 20 caracteres, não repetir as últimas N). |
| Normas ISO 27001/27002 | `[00:12:17]`, `[01:08:04]` | Só citado | Diz ter a certificação 27001/27002. Banco como setor regulado, dados sensíveis. |
| Casos reais: falha recente em PHP, Log4Shell | `[00:22:00]–[00:23:53]` | Contado como história | PHP: "uma, duas semanas atrás", governos (Austrália, Portugal, França) tiraram sites do ar. Log4j: execução remota de comandos, CVE, "correria de madrugada" na empresa de certificação digital onde trabalhava. Ver imprecisões na seção 8. |
| DoS/DDoS | `[00:59:40]–[01:01:35]` | Explicado por aluno (Paulo) e complementado | Paulo contou ter visto um DDoS ao vivo na "sala de guerra" do SiSU/MEC, com mitigação via Akamai. Nicolau complementa com "máquinas zumbis"; Benjamin com vírus/cavalo de Troia que transforma a máquina em zumbi. |
| Engenharia social e phishing | `[01:01:36]–[01:12:00]` | Explicado com exemplos | Henrique levanta engenharia social; Vinícius conta o teste de phishing interno da Caixa (e-mail de promoção de Natal de suporte para notebook); outro aluno diz que ignorar ou apagar sem denunciar também penaliza; Matheus cita SMS falso "Nubank, compra de R$ 3 mil". Nicolau: história do auditor de certificação que testava bancos com crachá falso; golpe do falso atendente pedindo o OTP; troca de aparelho sem resistência num banco digital (experiência própria em Portugal). |
| SQL injection | `[01:05:50]` | Só citado (por Matheus) | Não aprofundado. |
| CSRF (sem nome) | `[01:14:58]` | Só antecipado | "Consigo meio que forjar a autenticação… você fez uma operação indesejável e não percebeu" — prometido para a próxima aula. Havia uma aba "What is CSRF (Cross-site request…)" aberta no navegador (`f_032`), não apresentada. |
| Autenticação em dois fatores | `[01:57:59]–[01:58:57]` | Explicado | Algo que você é (biometria), algo que você tem (token, celular/OTP), algo que você sabe (senha). |
| HTTPS/SSL e captura de tráfego | `[02:00:50]–[02:04:03]`, `[02:07:47]` | Explicado, sem demo | Exemplo: compra na Amazon pelo Wi-Fi do café; sem HTTPS alguém lê os pacotes (Wireshark). Recomenda VPN e SSL. Disse que HTTPS seria visto "hoje" — não foi. |
| Pirâmide de testes e requisito não funcional de segurança | `[02:08:33]–[02:13:18]` | Explicado (digressão, "não é nosso foco") | Teste unitário testa qualidade de código e não valida segurança por si só; segurança tem de estar especificada nos requisitos não funcionais para ser testada; tipos: unitário, integração, ponta a ponta, interface (Selenium), carga, segurança/pentest. |
| Lojas de aplicativos e permissões | `[02:17:18]–[02:19:20]` | Comentário | Google/Apple exigem justificativa para localização e câmera; Apple faz revisão humana. Localização em tempo real como dado sensível. Caso Sony/PlayStation (vazamento de cartões). |
| Tríade CIA | `[02:21:05]–[02:24:24]` | Explicado com exemplos | Confidencialidade (só quem é autorizado), integridade (senha e saldo de R$ 10 não alterados sem autorização), disponibilidade (banco offline na hora da transferência). Alunos acrescentam irretratabilidade (não repúdio), conformidade (LGPD) e autenticidade `[02:24:33]–[02:26:18]`. |
| Assinatura digital × eletrônica; inversão do ônus da prova | `[02:26:36]–[02:28:35]` | Explicado | Digital: certificado e par de chaves, cadeia ICP-Brasil; eletrônica: IP, CPF, e-mail, menos rigor. |
| XSS (cross-site scripting) | `[02:31:23]–[02:41:23]` | **Demonstrado** (site) + código mostrado sem executar | Analogia: comentários com HTML/`<script>` em blogs. Conceito: a aplicação usa a entrada do usuário "sem tratar". Demo no **XSS game do Google** (`xss-game.appspot.com`, "Level 1: Hello, world of XSS", app "FourOrFour"): digitou `<script>alert('XSS')</script>` na busca e o alerta abriu. Depois abriu na IDE um código próprio com um endpoint "vulnerável" que concatena a string recebida no HTML/JavaScript; explicou que a correção é fazer escape de HTML/JavaScript para tratar a entrada como texto. **Não executou** o código; os quadros não capturaram a tela da IDE, então nome do projeto, classes e endpoints não são conhecidos. |

---

## 3. Comparação com o plano dele

Plano de Ensino (quadros `f_002`, `f_005`, `f_006`): "Segunda | Quarta | Sexta: 15:00–18:00, período 28/10/2026 a
18/11/2026". Objetivo: credenciais, autenticação e autorização (OAuth, SSO, OpenID Connect), JWT, tratamento e
isolamento de exceções, proteção de entrypoints, sanitização, defesa contra injection, Spring Security; ao final,
implementar camada de autenticação numa API REST com Spring Security (básica ou JWT).

**Aula 01 – 28/10 – Introdução à segurança de aplicações: XSS, CSRF e DoS.** Tópicos visíveis: apresentação do
instrutor, apresentação dos alunos, mais dois itens cortados no quadro, e "Ataques: Cross-site Scripting (XSS),
Cross-site Request Forgery (CSRF) e Denial-of-Service (DoS)". O plano também tem uma "prática para trabalhar hoje"
`[00:25:10]`, que não foi identificada na aula (a dinâmica em grupo pode ter sido essa prática; inferência).

| Previsto | Situação |
|---|---|
| Apresentações | Feito. |
| XSS | Introduzido e demonstrado só no nível 1 do XSS game. **Pendente:** rodar o código próprio ("a primeira coisa que a gente vai fazer na próxima aula é correr esse código" `[02:41:12]`) e os tipos de XSS ("vou deixar de falar dos tipos na próxima aula" `[02:35:54]`). |
| CSRF | **Não dado**, só antecipado `[01:14:58]`. |
| DoS | Só conceitual, puxado por aluno. Sem demo. |
| HTTPS | Disse "hoje a gente vai falar de HTTPS, de SSL" `[02:02:28]`; ficou só no comentário. É tema da Aula 02. |

Cronograma que ele narrou `[00:25:55]–[00:27:01]`: Aula 02 sexta 30/10 (criptografia, HTTPS, cookies); segunda 02/11
é feriado; quarta (04/11) JWT; sexta (06/11) OAuth 2 e tipos de fluxo; "sexta da próxima semana" SSO; "segunda dia 11"
OpenID Connect; dia 13 vulnerabilidades reais e tratamento de exceções; "dia 11 de 11" Spring Security (autenticação
básica e JWT); dia 18 encerramento. **A fala tem datas trocadas.** Pela grade Seg/Qua/Sex sem o feriado, a sequência
seria: 30/10 aula 2, 04/11 aula 3, 06/11 aula 4, 09/11 aula 5 (SSO), 11/11 aula 6 (OpenID Connect), 13/11 aula 7,
16/11 aula 8 (Spring Security), 18/11 aula 9 (inferência a partir do plano; conferir com a coordenação).

---

## 4. Projeto avaliado, atividades e lição de casa

**Projeto avaliado** `[00:28:16]–[00:31:37]`, `[01:40:38]`, `[02:41:43]`:

- Especificação num arquivo `.md` mostrado na tela; ele disse que o subiria no LMS junto com o plano. Conteúdo
  citado: o que se espera, **casos de uso** e **arquitetura sugerida** ("espero que vocês sigam essa arquitetura").
- Tema: "desenvolver uma API e uma camada de segurança com Spring"; projeto "básico", "mão na massa". Em
  `[00:57:56]` ele diz que o projeto pedido "é um help desk" (Whisper: "hop desk").
- **Grupos de 2 ou 3 pessoas.** Grupos ainda não formados; ficou para sexta 30/10. Os grupos da dinâmica de hoje
  **não** são os do trabalho `[01:40:38]`.
- Trabalho feito **em sala**: a partir de sexta 30/10, ele separaria em todas as aulas um tempo com breakout rooms
  para os grupos `[00:29:45]`.
- **Apresentação em 18/11**, compartilhando a tela, rodando a aplicação e discutindo o código. É a avaliação do
  módulo. **Presença obrigatória nesse dia** ("podem até faltar em outras datas, mas não faltem essa data")
  `[00:30:13]–[00:30:53]`.
- Critérios de nota: não foram ditos além de "entender como vocês evoluíram" e presença na apresentação.

**Dinâmica da aula** `[01:37:07]–[01:43:00]` (enunciado colado no chat): escolher uma aplicação do dia a dia (banco,
delivery, e-commerce, sistema de escola, internet banking da Caixa) e listar (1) o que ela guarda de valor, (2) o que
acontece com a empresa e os usuários se for invadida, (3) o que fazer para evitar. 10 min em 4 salas aleatórias (uma
com 4 pessoas, três com 3). Resultados na seção 6.

**Debate do câmbio** `[00:58:23]`: cenário de discussão (não é o projeto) — sistema de agendamento de câmbio em que o
cliente agenda a compra de dólar ou euro para sacar na agência. Nota de contexto (não vem da transcrição): segundo a
memória do projeto, o projeto guiado do módulo anterior desta turma (BE-JV-009) foi uma API de Câmbio, o que pode
explicar a familiaridade.

**Lição de casa / leitura** `[00:25:17]–[00:27:50]`, `[02:41:49]`: "atividade extra", sem entrega — ler as questões
sugeridas no plano, pesquisar sobre ataques, visitar os links e vir com eles lidos. Links visíveis no plano (`f_006`):
spring.io/projects/spring-security; docs do Spring Security (getting started); jwt.io/introduction; oauth.net/2;
openid.net/connect; OWASP XSS; OWASP CSRF; OWASP Top Ten; xss-game.appspot.com; MDN "Website security" e "HTTP
Cookies"; Google "HTTPS"; Resilience4j RateLimiter; Baeldung "exception handling for REST with Spring";
GeeksforGeeks "session vs token based authentication". Bibliografia: *Spring Security in Action* (Laurențiu Spilcă),
*OAuth 2 in Action* (Justin Richer e Antonio Sanso) e outros itens cortados no quadro.

---

## 5. Combinados e regras da turma

- **Horário:** 15h00 às 18h00; ele costuma começar às 15h05 para esperar quem chega `[00:16:16]`.
- **Intervalo:** 16h30 às 16h50 (20 min), aceito pela turma no chat `[00:17:04]–[00:17:27]`. Ele cronometra com um
  timer na tela.
- **Câmera:** pedido insistente e repetido para abrir (`[00:06:45]`, `[00:15:04]`, `[00:33:56]`, `[00:58:11]`). Na
  segunda metade a maioria estava com câmera aberta, e ele agradeceu `[02:20:44]`.
- **Interrupções:** "não precisa levantar a mão, abram o microfone e me interrompam"; "não tem pergunta boba"
  `[02:13:22]`.
- **Contato:** e-mail e link (provavelmente LinkedIn) no chat `[00:13:41]`. Faltas e problemas (ex.: saiu mais cedo
  e levou falta) devem ser tratados primeiro com ele; só se não resolver, escalar à ouvidoria/Ada `[00:14:04]–[00:14:46]`.
- **Chamada:** oral, perto do fim da aula `[02:29:06]`.
- **Materiais:** plano em PDF e `.md` do projeto no LMS, "na primeira pasta", "ao final do dia" `[00:28:00]–[00:28:13]`.
  Havia uma aba "LMS | Ada" aberta no navegador (`f_032`).
- **Plano como "norte":** pode atrasar; o que não for visto passa para a aula seguinte `[00:18:43]`, `[00:24:49]`.

---

## 6. A turma

Nomes completos tirados da lista de participantes do Teams (`f_008`, `f_009`, `f_010`, `f_020`, `f_024`). Sem dado de
nome nos quadros: Roberto, Tiago e João Pedro Moreta (só na chamada).

| Aluno | O que se sabe | Participação |
|---|---|---|
| **Matheus Fonseca Sousa** | Ambiente de produção do sistema lotérico, testes de qualidade de software. Terminando a graduação; TCC sobre testes de segurança estáticos e dinâmicos (SAST/DAST). Segurança = prejuízo financeiro, perda de confiança na marca `[00:34:19]`. Fundo "Loterias Caixa". | Alta. Primeiro a se apresentar e a abrir a câmera. Exemplo do SMS falso "Nubank"; citou SQL injection `[01:05:04]`. Provável relator da sala 3 (código seguro desde os requisitos, SAST/DAST no pipeline `[02:06:22]`; inferência pelo tema do TCC). |
| **Benjamin da Nobrega Bezerra** | Java no trabalho "pela primeira vez de verdade" há ~1 ano (antes só na faculdade). Responsável por parte do sistema que administra programas sociais. Segurança = perfis; "chato ter de logar toda hora" `[00:35:04]`. | Média. Complementou o DDoS (cavalo de Troia, máquina zumbi) `[01:09:06]`. Sala 1. Saiu brevemente perto do XSS (`[02:31:43]`, inferência). |
| **Ian Pereira da Silva Fernandes** | Entrou como desenvolvedor numa equipe que já usa Java; está em treinamento porque vão refatorar o sistema para Java 21; "maratona de estudos de Java". Segurança = proteção de dados, camadas, criptografia `[00:36:14]`. | Média-alta. Brincou com a câmera. Sala 4. Provavelmente respondeu à pergunta do XSS (validação, bloquear chamadas ao banco), elogiado por "ir no caminho" `[02:36:28]–[02:41:08]` (inferência: aparece em destaque no quadro `f_033`). |
| **Karla Danielle Santos da Silva** | Sistema de tratamento de correições/processos disciplinares da Caixa ("correções disciplinares", termo incerto). Segurança = perfis diferenciados, dados sigilosos `[00:37:27]`. | Baixa-média. Chamada de "Carlos" pelo Nicolau. Caiu da sala de grupo `[01:53:21]`. Sala 2. |
| **Paulo Ueiner Moreira de Assis** | Na Caixa desde 2021, no mercado desde 2006 (PHP); administração de dados no MEC; BI na Caixa (sumarização do pagamento do auxílio emergencial); comunidade de pagamento de programas sociais; hoje no sistema de treinamento das prefeituras para o sistema do Bolsa Família (Whisper: "C-BEC"; possivelmente SIBEC, incerto). Nunca tinha programado Java; montou a arquitetura do projeto em Quarkus, usada pelas fábricas; também Flutter, JS, Node. Quer o histórico da linguagem. Segurança = prevenção de fraudes, perdas financeiras; "a Caixa está toda hora sob ataque" `[00:38:16]`. | Alta. Experiente. Contou o DDoS no SiSU/MEC e explicou o ataque `[00:59:40]`. Sala 2. |
| **Caio Cesar da Matta Souza** | "Bom tempo de Caixa", ~2 anos em TI; atua como **Scrum Master**, **não codifica**; estudando para migrar para desenvolvimento. Segurança = confiabilidade, proteção de dados `[00:40:57]`. | Baixa. Sala 1. Ponto de atenção para o projeto em grupo (sem prática de código). |
| **Vanessa Soares Borges da Silva** | Nova na Caixa e em TI. Equipe da tesouraria; sistema legado em MUMPS e Caché ObjectScript; componentes Java para modernização. Segurança = proteção de dados, criptografia, ataque hacker `[00:42:02]`. | Média. Sala 4. Saiu às ~17h45 por compromisso de trabalho `[02:30:33]`. |
| **Henrique Campanharo Seabra** | ~1 ano de Caixa; mainframe (COBOL, JCL, batch); base forte em C; pouco Java (curso na faculdade). Segurança = mainframe como ambiente seguro `[00:43:43]` — o Nicolau rebateu: infraestrutura e aplicação "andam juntas". | Média. Levantou engenharia social e phishing `[01:01:36]`. Sala 3. |
| **Helio Serrano Brandao Junior** | Na Caixa desde 2003; em desenvolvimento desde a pandemia (disse "2019"); passou por FGTS e habitação; PHP/Laravel; há ~3 anos na TI, **área de segurança, squad de Biometria**; aplicações Java 8 + JBoss migrando para Spring; trabalhou com Spring Batch. Segurança = vazamentos de dados, autenticação `[00:45:34]`. | Média. Sala 2. |
| **Levy Washin…** (sobrenome cortado) | Comunidade de loterias; desenvolvimento e manutenção de legados; Java no back-end, TypeScript no front. Segurança = validar quem o usuário diz ser e controlar o que acessa (autenticação/autorização) `[00:47:52]`. | Média. **Saiu ~16h28** para não estourar o ponto `[01:12:52]`. |
| **Diego Marques Soares** | Sistema de mensuração de ponto de função; Java e Angular. Segurança deveria ser essencial; "com o IA first o pessoal deixa a segurança de lado" `[00:48:42]`. | Média. Sala 1. Alguém (talvez ele) explicou que ponto de função é a métrica aceita por auditorias em órgãos de governo `[00:49:42]`. |
| **Eldio Teixeira Junior** | Na Caixa; em tecnologia desde 2000 (suporte e redes até 2010); desenvolvimento desde 2010 (JSF, JBoss), hoje Quarkus e Spring (mais Spring Batch); comunidade de "estruturas" (termo incerto), sistemas de relatórios da intranet; módulos batch e online; só desenvolve quando aperta. Segurança = matriz de acesso, autenticação/autorização, trilha de auditoria; apontamentos de pentest costumam ser de infraestrutura `[00:50:54]`. | Média. Na chamada o Nicolau não lembrava de ter ouvido ele `[02:29:35]`. Sala não identificada (talvez o 4º membro da sala 1). |
| **Vinicius Silva de Oliveira** | Back-end na comunidade de depósitos judiciais; 2 anos de Caixa; aprendeu a programar na Caixa. Trabalha no "levantamento" (alvará assinado por juiz): valida a assinatura digital via API interna (Whisper: "FWC", incerto) e confere no CNJ se o CPF é de magistrado `[00:52:59]`. | Média-alta. Contou o teste de phishing interno da Caixa `[01:03:05]`. Sala 4. Fundo "Assunto sigiloso". |
| **Edgar Guimaraes Damazio Junior** | ~1 ano de Caixa; gestão de acesso, abertura e fechamento de agência (sigla incerta: "CCIA"); código legado sendo reescrito; gestão e front-end, ainda sem back-end. Segurança = acesso, proteção `[00:56:16]`. | Baixa-média. Sala 3. |
| **Roberto** | Só na chamada; não houve resposta audível. | — |
| **Tiago** | Só na chamada; não houve resposta audível. | — |
| **João Pedro Moreta** | Só na chamada; "acho que ele está de férias" (dito por um colega) `[02:29:56]`. | Ausente. |

A lista do Teams mostrava ainda "+5", "+3" e "+2" participantes não identificáveis nos quadros (pode incluir
Roberto e Tiago; inferência). A ordem da chamada é alfabética, o que ajuda a casar os nomes.

**Grupos da dinâmica (não são os grupos do projeto):**

| Sala | Membros | Aplicação | Pontos levantados |
|---|---|---|---|
| 1 | Benjamin, Diego, Caio (+1, possivelmente Eldio) | Sistema escolar | Dados pessoais, diplomas, notas, frequência (frequência usada como álibi em processo); fraude de notas e diplomas; perda de credibilidade; dados usados em phishing. Prevenção: treinamento, auditoria de acessos, software/bibliotecas/SO atualizados, 2FA. |
| 2 | Karla, Paulo, Hélio | E-commerce | Dados pessoais e financeiros, avaliações, perfil de compras; dano à imagem, roubo de identidade. Prevenção: criptografia de dados sensíveis, autenticação, análise de segurança no desenvolvimento (SQL injection), certificado digital (Nicolau traduziu como HTTPS). |
| 3 | Henrique, Matheus, Edgar | Delivery (iFood) | Endereço, dados pessoais, cartão, localização em tempo real, volume de transações; processos judiciais, reputação, perda financeira. Prevenção: treinamento contra engenharia social, política de rede, criptografia, código seguro desde a concepção (SAST/DAST no pipeline); um membro destacou que o fator humano é o mais perigoso. |
| 4 | Vinícius, Ian, Vanessa | Delivery (iFood) | Pesquisaram o cadastro do iFood (documento, endereço completo); cartão e vale-refeição salvos validados só pelos 3 dígitos de trás. Prevenção: criptografia, treinamento da equipe, autenticação multifator (código ao entregador). |

**Perfil geral:** turma heterogênea, quase toda de empregados da Caixa: de quem não codifica (Caio) e de quem vem de
mainframe, MUMPS ou PHP (Henrique, Vanessa, Paulo, Hélio) até quem já trabalha com Java e Spring (Levy, Diego, Eldio,
Vinícius, Ian). Vários veem segurança como perfis, autenticação/autorização e proteção de dados; dois trabalham
diretamente em áreas de segurança (Hélio, biometria; Edgar, gestão de acesso).

---

## 7. Dúvidas e dificuldades levantadas pelos alunos

| Marca | Dúvida ou dificuldade | Resposta |
|---|---|---|
| `[01:40:23]` | "Como ficou a divisão de grupos?" | Os grupos da dinâmica não são os do trabalho; a definição fica para sexta 30/10. |
| `[01:53:21]` | Karla caiu da sala de grupo e voltou para a principal. | Só confirmou a sala (R2) e seguiu. |
| `[02:02:15]`, `[02:00:50]` | Sala 2 propôs "certificação digital" para e-commerce. | Nicolau: "seria o HTTPS"; exigir token do cliente não faria sentido. |
| `[02:06:02]` | Nicolau pediu para a sala 3 explicar "código seguro desde a concepção". | Aluno explicou (requisitos, SAST/DAST); Nicolau fez a digressão sobre a pirâmide de testes. |
| `[02:24:33]` | Aluno: "há autores com quinto e sexto pilares". | Nicolau não lembrava; o aluno citou irretratabilidade, conformidade (LGPD) e autenticidade; Nicolau aceitou e disse que pesquisaria. |
| `[02:36:28]` | Pergunta do Nicolau: "como tratariam esse ataque (XSS)?" Resposta de aluno: validação; impedir que o campo faça chamadas ao banco. | Nicolau: no front-end não há banco; a correção é escape da string de HTML/JavaScript. |

Não houve dúvida de conteúdo técnico vinda espontaneamente dos alunos; a interação foi puxada pelo instrutor.

---

## 8. Pontos de atenção para o novo instrutor

### Imprecisões técnicas ditas em aula

1. **Log4Shell** `[00:22:40]–[00:23:09]`: disse "há uns cinco, seis anos", "2019, 2020" e "vulnerabilidade de 13 ou
   10 anos atrás". A divulgação foi em dezembro de 2021 (CVE-2021-44228), há ~5 anos; o defeito existia desde o
   Log4j 2.0-beta9 (2013), ou seja, ficou ~8 anos sem ser notado. O resto (execução remota de comando) está certo.
2. **"Falha muito grave no PHP"** `[00:22:06]`: governos da Austrália, Portugal e França teriam tirado sites do ar "há
   uma, duas semanas". **Não consegui verificar** (o evento seria de outubro de 2026, depois do meu corte de
   conhecimento). Convém conferir antes de repetir o caso.
3. **"Não é impossível quebrar o SSL"** `[02:03:40]`: frase confusa ("difícil, mas não é possível"). O ponto
   correto: TLS atual, bem configurado, não é quebrável na prática; os riscos reais são man-in-the-middle com
   certificado aceito indevidamente, SSL stripping, versões ou cifras antigas. Também vale dizer TLS, não SSL
   (obsoleto), e que HTTPS garante confidencialidade **e** integridade (ele falou só de "não houve alteração").
4. **"Dois tipos de XSS"** `[02:35:54]`: a classificação usual da OWASP tem três (refletido, armazenado, baseado em
   DOM). A demo do XSS game nível 1 é XSS **refletido**; a analogia do comentário de blog é XSS **armazenado** — vale
   separar os dois na retomada.
5. **Quarto pilar "segurança"** `[02:24:12]`: lapso; ele quis dizer autenticidade (confirmado depois). A tríade CIA
   vem da ISO/IEC 27000; autenticidade, não repúdio e confiabilidade aparecem na própria família ISO 27000 e na
   literatura brasileira — não são "atualização" recente, como ele sugeriu.
6. **Sony/PlayStation** `[02:20:09]`: "vários cartões de crédito foram publicados". No vazamento da PSN (abril de
   2011, ~77 milhões de contas) a Sony admitiu que dados de cartão **podiam** ter sido expostos; publicação em massa
   dos cartões não foi confirmada. Também foi "há ~15 anos", não 10.
7. **Inversão do ônus da prova na assinatura digital** `[02:27:00]`: a ideia (presunção de veracidade da assinatura
   ICP-Brasil, MP 2.200-2/2001) está no caminho, mas é tema jurídico; tratar com cautela.
8. **Datas do cronograma** trocadas na fala (ver seção 3): "dia 11" aparece duas vezes.
9. **Comentários sobre bancos digitais/Nubank** `[01:06:13]`, `[01:10:58]` ("não são exemplo de segurança",
   "pouca segurança") e o caso de família com PIX de R$ 200 mil: opinião pessoal sobre marca concorrente, numa turma
   de empregados da Caixa. Não é imprecisão técnica, mas é tom a evitar.

### Problemas operacionais

- **Atraso no início** (gravação só às 15h15, conteúdo a partir de ~15h20) por problema de acesso à conta e OTP.
- **Breakout rooms:** não funcionaram na primeira tentativa porque ele estava logado pelo navegador `[00:17:46]`;
  funcionaram depois do intervalo. Karla caiu da sala.
- **Saídas antecipadas por trabalho/ponto:** Levy (~16h28) e Vanessa (~17h45). Indica que parte da turma tem
  limite de jornada; vale perguntar no início de cada aula.
- **Tempo:** o XSS ficou espremido nos últimos 10 min; o código preparado não foi executado.

### Promessas feitas aos alunos (verificar se foram cumpridas)

- Subir no LMS, "ao final do dia" 28/10, o **PDF do Plano de Ensino** e o **`.md` do projeto** (`[00:28:06]`,
  `[00:31:24]`, `[02:41:43]`).
- Na próxima aula: **rodar o código de XSS**, falar dos **tipos de XSS**, explicar o ataque de **CSRF**, **definir os
  grupos** do projeto.
- A partir de sexta 30/10: tempo de breakout em **toda aula** para os grupos trabalharem no projeto.
- Avaliação = apresentação do projeto em **18/11**, com presença obrigatória.

### Clima da turma

Começou frio (câmeras fechadas, ninguém falava; ele insistiu várias vezes). Melhorou na rodada de apresentações e na
dinâmica em grupo: no fim a maioria estava com câmera aberta e os grupos apresentaram bem. Turma madura,
experiente em contexto bancário, que trouxe casos reais (SiSU, phishing interno, assinatura de juiz). O Nicolau
valoriza troca de experiência e fala muito de sua carreira (Java desde 2010, empresa de certificação digital,
ISO 27001, professor universitário). Ninguém reclamou do atraso.

---

## 9. Citações-chave

1. "A segurança não é só código. Não é só encriptar a senha na base de dados, não é só implementar um JWT, não é só
   você ter um OAuth 2 implementado na sua aplicação." `[00:11:00]`
2. "Uma corrente é tão forte quanto seu elo mais fraco." `[00:11:32]`
3. "Vocês podem até faltar em outras datas, mas não faltem essa data, que vai ser a avaliação de vocês." `[00:30:23]`
4. "Não adianta nada você ter 20 firewalls… se a sua aplicação tacar pedra na segurança. … As duas coisas andam
   muito juntas." `[00:44:52]` (resposta ao Henrique sobre mainframe)
5. "Engenharia social é você se passar pela pessoa; você não tem ataque direto ao sistema." `[01:07:29]`
6. "O teste unitário, por si só, não valida a segurança. Se não estiver especificado nos requisitos não funcionais,
   não tem como testar." `[02:11:18]`
7. "Confidencialidade, integridade e disponibilidade — o nosso tripé." `[02:23:29]`
8. "A aplicação pega o que o usuário digitou e não trata. Lembrem-se disso, essa é a palavra." `[02:33:47]`
9. "Ela tem que ser tratada como texto, não pode ser tratada como JavaScript, como HTML." `[02:40:59]`
10. "A primeira coisa que a gente vai fazer na próxima aula é correr esse código." `[02:41:12]`
