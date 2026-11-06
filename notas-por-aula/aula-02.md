# Aula 2: turma 1749 (Caixa), BE-JV-013 "Fundamentos de segurança para aplicações", sexta, 30/10/2026

Fontes:

- a transcrição automática `../transcricoes/aula-02-2026-10-30-transcricao.txt`, gerada pelo Whisper small. A gravação tem 2h56 e começa às 15h00, horário de Brasília (BRT);
- os quadros do vídeo, um a cada 5 minutos (`f_001` a `f_035`).

Os horários reais estão em BRT e foram calculados como 15h00 mais a marca do vídeo. Nos quadros, o relógio da máquina do instrutor (fuso de Portugal, 3 h à frente) confirma a conta: o quadro `f_003` mostra 18h13 locais, ou seja, 15h13 em BRT.

Quando um trecho é inferência, ele vem marcado como **(inferência)**. Quando um nome ou termo é incerto, vem marcado como **(incerto)**.

Instrutor: Hagamenon Nicolau de Oliveira, que usa a conta do Teams "Educacional 07". No LMS ele aparece como "Hagamenon Nicolau De Oliveira – Instrutor".

---

## 1. Linha do tempo

| Horário (BRT) | Duração | O que aconteceu |
|---|---|---|
| 15h00–15h04 | ~4 min | Espera pela turma ("vamos aguardar o resto do pessoal chegar"). |
| 15h04–15h06 | ~2 min | Abertura. Pede câmeras abertas e avisa que o plano de ensino (PDF) e a especificação do projeto (`.md`) estão na **Pasta Geral** do LMS. Anuncia que, "do meio da aula para frente", vai dividir a turma em salas para montar os grupos do projeto. |
| 15h06–15h21 | ~15 min | **Retomada da Aula 1 (28/10), parte 1.** Mostra o XSS (*cross-site scripting*, injeção de script na página) com demonstração no código, `/busca` × `/busca/segura`. Em resposta a uma pergunta, faz uma demonstração **improvisada** de SQL injection (injeção de comando SQL), escrevendo uma *query* concatenada no IntelliJ. |
| 15h21–15h31 | ~10 min | **Retomada, parte 2: CSRF** (*cross-site request forgery*, requisição forjada em nome do usuário logado). Mostra a página da PortSwigger Web Security Academy, explica o token CSRF e abre a aba *Application* do DevTools (cookies e cabeçalhos de segurança). |
| 15h31–15h45 | ~14 min | **Retomada, parte 3: DoS/DDoS** (negação de serviço). Conta a curiosidade histórica de 1974, demonstra o *rate limiter* do Resilience4j no endpoint `/alunos` e pergunta à turma se isso basta (Diego responde). Um aluno sugere o Cloudflare. Mostra os slides de camadas (Cloudflare → Nginx/HAProxy → aplicação; *load balancer* com Proxy A/B). |
| 15h45–15h47 | ~1,5 min | Transição, com cerca de 1 min sem fala (marca 00:45:42 a 00:46:39). |
| 15h47–16h05 | ~18 min | **Hash × criptografia** (início do conteúdo previsto para a Aula 2). Pergunta à turma (Tiago responde) e usa a analogia da carta com chave combinada. Responde dúvidas de Vinícius e Benjamin sobre colisão de hash e sobre por que o hash não volta. Mostra o código `FuncaoHash` (SHA-256) e `CriptografiaSimetrica` (AES-GCM). Gil (incerto) pergunta se um zip com senha é criptografia. Explica chave assimétrica (com uma autocorreção) e assinatura de documento por hash. |
| 16h05–16h16 | ~10 min | Demonstração com testes automatizados (`Aula02CriptoTest`): hash de "ABC", cifrar e decifrar, chave pública RSA-2048. **Falha de áudio** perto de 16h11–16h12 (um aluno avisa: "ficou mudo"). |
| 16h16–16h26 | ~10 min | **HTTPS/TLS.** Abre o certificado de `*.ada.tech` no navegador e explica autoridade certificadora, cadeia, confidencialidade, autenticidade e integridade, validade do certificado, *handshake* e certificado autoassinado (pergunta à turma; um aluno responde). Faz um fecho rápido sobre HTTP. |
| 16h26–16h27 | ~1 min | Anuncia o intervalo e pede que **não saiam depois do intervalo**, porque na aula anterior "muita gente foi embora". |
| **16h27–16h48** | **~21 min** | **Intervalo.** |
| 16h48–17h01 | ~13 min | Formação dos grupos: a turma escolhe manter os grupos do módulo anterior e manda a composição pelo chat. Ele cria 4 salas e passa as regras de entrega e de apresentação (seção 4). |
| 17h01–17h54 | ~52 min | **Trabalho em grupo nas salas do Teams.** Não há fala na gravação, que ficou na sala principal: nos quadros `f_026` a `f_035` aparecem só Caio Cesar e "Educacional 07" (câmera desligada); Henrique entra por volta de 17h45. O que se discutiu nas salas **não está gravado**. |
| 17h54–17h56 | ~2 min | Volta à sala principal, chamada (cortada na transcrição depois de "Paulo") e despedida: "voltamos na quarta-feira", por causa do feriado de segunda, 02/11. |
| **17h56** | — | **Fim real**, cerca de 4 min antes das 18h previstas. |

Tempo de conteúdo expositivo: cerca de 1h20 (15h06–16h26). Desse total, cerca de 40 min foram retomada da Aula 1. O tempo restante de aula (cerca de 1h05) foi de grupos e de projeto.

---

## 2. Conteúdo dado

### 2.1 Ambiente da demonstração (quadros)

- Projeto Spring Boot `api-alunos`, aberto no IntelliJ. Caminho local: `C:\Users\nicolau.oliveira\ada\BE-JV-013 Fundamentos de segurança...`
- Pacote `com.ada.seguranca`, com estas subpastas:
  - `aluno`: `Aluno`, `AlunoController`;
  - `cookie`: `CookieController`;
  - `cripto`: `CriptografiaAssimetrica`, `CriptografiaController`, `CriptografiaSimetrica`, `FuncaoHash`, `HashController`, `TextoCifradoInvalidoException`;
  - `xss`: `BuscaController`.
- Classe principal `ApiAlunosApplication`, servida em Tomcat 10.1.31 na porta 8080. Há também `README.md` e testes em `src/test/.../Aula02CriptoTest`.
- Abas abertas no navegador: "What is CSRF" (PortSwigger), `localhost:8080/alunos` (e `/busca/segura`), **"XSS game: Level 1"** (pelo título, o XSS Game do Google, **inferência**) e LMS | Ada.
- Slides: arquivo "Slides Aula 02.pptx". Os primeiros são "DDOS" (3 slides), depois "HTTPS e Cookies" e "O que vimos na aula 01" (miniaturas com XSS, CSRF e DoS). Pela transcrição, só os de DDoS e o de capa "HTTPS e Cookies" foram usados de fato.

### 2.2 Temas

| Tema | Profundidade | Detalhes, exemplos e marcas |
|---|---|---|
| **XSS** (retomada) | Demonstrado em código | [00:08:02–00:12:20] Define XSS como texto não tratado que é concatenado no HTML e executado no navegador. Exemplo do comentário de blog. Usa o `BuscaController`: `GET /busca?q=` devolve `"<h1>Resultados para: " + q + "</h1>"` (vulnerável; o comentário no código traz o ataque `<script>alert('XSS')</script>`). `GET /busca/segura` usa `HtmlUtils.htmlEscape(q)` (corrigido). Executa o ataque e depois a versão segura ("fazer um escape"). Cita o roubo de token ou cookie e o uso de XSS em vários blogs para gerar tráfego contra outro site ("um exemplo de ataque de DDoS"). Menciona a "plataforma de game" de ataques [00:06:20], que seria o XSS Game (**inferência** pela aba aberta). |
| **SQL injection** | Improvisado no código, sem executar | [00:12:27–00:20:46] Começa por iniciativa própria e aprofunda depois da pergunta de um aluno ("Brigê", incerto) [00:14:16]. Diz que "não vim preparado" [00:14:45]. Escreve no `BuscaController` um método `teste(String username, String pass)` com `"select a.username from schme.table where a.username ='" + username + "' and a.pass = '"+pass+"'"` (quadro `f_004`) e explica o `' or 1=1`. Solução apresentada: `PreparedStatement` ou JPA/Spring Data. Afirma que "hoje é muito difícil fazer SQL injection" (ver a seção 8). |
| **Exigência do projeto** | Citada | [00:20:40] "o seu projeto tem que ter pelo menos ali tratamento para SQL Injection, tratamento para XSS". |
| **CSRF** (retomada) | Explicado, sem código; usou site | [00:20:53–00:31:04] "Esse não tem um código porque é um pouquinho mais complexo." Mostra a página CSRF da PortSwigger Web Security Academy (`portswigger.net/web-security/csrf`), com o exemplo `vulnerable-website.com/email/change?email=pwned@evil-user.net`, e manda o link no chat, com a dúvida se o acesso funciona "aí na Caixa". Analogia: o usuário logado no banco abre um e-mail com link malicioso, que dispara um PIX ou a troca de e-mail usando a sessão ativa (exemplo "Lex Code bank", incerto). Explica o token CSRF ("string aleatória conhecida só pelo navegador e pela aplicação, enviada em toda requisição que altera estado"). Diz que no Spring a proteção "vem *enabled* por padrão" e que é preciso desativá-la deliberadamente. Abre o DevTools (aba *Application*/cookies) e comenta cabeçalhos de segurança [00:29:26–00:31:04]. Promete talvez trazer depois um exemplo de token CSRF no site da Ada [00:27:36]. |
| **DoS/DDoS** (retomada) | Demonstrado em código, mais slides | [00:31:15–00:45:33] Liga ao pilar da **disponibilidade**, visto na Aula 1. Curiosidade: o "primeiro ataque" em 1974, feito por um garoto de 13 anos que desligou terminais ligados a uma central. Exemplo de capacidade: 100 mil requisições simultâneas contra 200 mil. Divide em ataque de inundação (*flood*) e *crash attack*. **Demonstração:** Resilience4j RateLimiter (dependência `resilience4j-spring6` 2.2.0, pelo *stack trace* do quadro `f_008`) no `application.yml`, instância `alunoService` com `limitForPeriod: 1`, `limitRefreshPeriod: 10s` e `timeoutDuration: 0s` (comentário no arquivo: "0s: a segunda chamada falha na hora"). Chama `/alunos` duas vezes e a segunda dá erro [00:35:17–00:36:42]. Pergunta socrática: "só isso é suficiente para evitar DDoS?" [00:37:09]. Diego responde que não, porque as requisições continuam chegando. Outro aluno sugere o Cloudflare. Slides de camadas: Internet → Cloudflare (1º bloqueio) → Nginx/HAProxy (proxy reverso, bloqueio por IP, ex.: "a partir de 200 acessos daquele IP") → aplicação Spring Boot. Slide "Load Balancer → Proxy A (ativo) / Proxy B (standby) → Backend Spring Boot": diz que o *load balancer* serve para escalar, não protege contra DDoS, e que é preciso "ter todos eles implementados". |
| **Hash × criptografia** | Explicado e mostrado em código | [00:47:29–01:00:08] Começa com a pergunta "hash e criptografia são a mesma coisa?" e o requisito "senha não pode estar em texto claro". Tiago responde, misturando os dois. Analogia: carta para o "Hélio" com cifra combinada (A=1, B=2…), que serve para explicar a **chave simétrica** (a mesma chave cifra e decifra). Hash é "caminho único": o cadastro gera o hash, o login gera de novo e compara hash com hash. Colisão é "possível, mas muito difícil". Código `FuncaoHash` (`@Service`, `ALGORITMO = "SHA-256"`, `MessageDigest.getInstance`, `HexFormat.of().formatHex`); o comentário do código diz "MD5 e SHA-1 estão quebrados: não use". Diz que o MD5 "já fizeram a engenharia reversa" e que hoje "conseguem adivinhar o hash". |
| **Criptografia simétrica** | Demonstrado em código e teste | [00:56:00–00:57:12, 01:07:45–01:10:44] Classe `CriptografiaSimetrica`, com AES/GCM/NoPadding, IV de 12 bytes aleatório a cada cifragem, `GCMParameterSpec(TAG_BITS, iv)`, IV concatenado ao texto cifrado e saída em Base64. `decifrar` captura `AEADBadTagException`/`GeneralSecurityException` e lança `TextoCifradoInvalidoException` ("chave errada, texto adulterado ou Base64 inválido: não revelar o motivo"). Mostra que o texto cifrado muda a cada execução e que o hash, ao contrário, é sempre igual [01:06:45]. |
| **Criptografia assimétrica e assinatura** | Explicado, mais saída de teste | [01:01:41–01:05:33, 01:12:06–01:14:58] Par de chaves pública e privada. **Primeiro diz que cifra com a privada para o outro abrir com a pública e depois se corrige** [01:03:47]: para cifrar, usa-se a pública do destinatário; para assinar, a própria privada, e qualquer um verifica com a pública. Assinatura de documento: gera o hash do documento e assina o hash; mudar uma vírgula muda o hash. Teste `assimetricaExpoeSoAChavePublica` devolve JSON `{"algoritmo":"RSA-2048","formato":"X.509","chavePublica":"MIIBIjAN..."}`. Usa as classes do próprio Java (`Cipher`). "Curva elíptica... não é importante; importante é entender o conceito." |
| **HTTPS/TLS e certificados** | Explicado, mais demonstração no navegador | [01:15:46–01:25:55] Abre o certificado do LMS (quadro `f_016`): `*.ada.tech`, emitido por **WR3 / Google Trust Services**, válido de 26/10/2026 a 25/01/2027. Fala de cadeia de certificação até a "autoridade máxima" ("WebTrust"). Usa o exemplo do Wi-Fi do café. TLS é o sucessor do SSL. HTTPS garante confidencialidade, autenticidade e integridade; HTTP é texto claro. Diz que certificados "valem um a dois anos no máximo". Explica o *handshake* e erros de certificado vencido. **Certificado autoassinado:** pergunta à turma e um aluno ("Corriscain", incerto) responde corretamente que serve para teste e não tem validade comercial; o instrutor completa (gerado com ferramenta do Java, provavelmente `keytool`, **inferência**; o navegador alerta; usado em desenvolvimento e homologação porque certificado é pago). |
| **Cookies** | **Só citado** | O slide de capa se chama "HTTPS e Cookies", mas não houve explicação de atributos de cookie (`HttpOnly`, `Secure`, `SameSite`) nem demonstração do `CookieController`. O único contato foi a aba de cookies do DevTools no bloco de CSRF [00:29:43–00:31:04]. |
| **HTTP** | Só citado | [01:25:58] "Pressupus que esse conhecimento vocês já deviam ter." |

---

## 3. Comparação com o plano

| Item | Situação |
|---|---|
| Plano da Aula 2 (30/10): "HTTPS e Cookies" | **HTTPS: dado** (conceitos, certificado real, autoassinado, *handshake*). **Cookies: não dado** (só a aba do DevTools); o slide de capa existe, mas não teve conteúdo explicado. |
| Conteúdo extra (não está no título do plano) | Hash × criptografia, chave simétrica (AES-GCM) e assimétrica (RSA), assinatura digital. Isso ocupou cerca de 30 min e é base para a Aula 3 (JWT). |
| **Retomado da Aula 1** (28/10, "Intro XSS/CSRF/DoS") | Ele mesmo diz "nós ficamos no caminho" [00:05:57]. Nesta aula: XSS com demonstração, CSRF com PortSwigger, DoS/DDoS com *rate limiter* e camadas. Somou-se SQL injection, improvisado. São cerca de 40 min de retomada. |
| Ficou para depois / pendente | Cookies (atributos de segurança). Exemplo prático de token CSRF (prometido "talvez depois"). Código do `api-alunos` a ser publicado (prometido). O slide "O que vimos na aula 01" não foi apresentado. |
| Próxima aula | Na quarta, 04/11 (segunda, 02/11, é feriado). Pelo plano dele, a Aula 3 é **JWT**. |

---

## 4. Projeto avaliado, atividades e lição de casa

O que foi dito (literal ou parafraseado) e o que se vê nos quadros:

- **Material no LMS:** na "Pasta Geral" da turma "Escalação Tech - 02 | Dev. Back-End Java Intermediário | #1749" estão dois arquivos, ambos "Enviado 30 de out.":
  - `apichamadosespecificacao.md`;
  - `plano-ensino.pdf`.

  O instrutor: "Tem lá o `.md`, então, API, para vocês, com a especificação, para vocês desenvolverem. E é um PDF, que é o plano de ensino, tudo aquilo que a gente falou na nossa primeira aula" [00:05:38]. Pelo nome do arquivo, o projeto é uma **API de chamados** (**inferência**; o conteúdo da especificação **não aparece** em nenhum quadro nem foi lido em voz alta).
- **Requisito mínimo dito em aula:** "tratamento para SQL Injection, tratamento para XSS" [00:20:46].
- **Grupos:** ele propôs sorteio pelo Teams (3 ou 4 salas, grupos de 3 a 4 pessoas) ou grupos montados pela turma. Um aluno (não identificado) disse: "A gente já tem grupo do outro módulo... se o pessoal quiser seguir nessa" [01:50:35], e a turma montou os grupos pelo chat. Contagem dita: "São 18 pessoas" [01:52:19], "grupos de quatro e alguns ficaram com cinco".
- **Composição final** [01:56:44]:
  - **Sala 1:** Caio, Hélio (*), Ian, Paulo, Tiago;
  - **Sala 2:** Edgar, Karla, Hélio (*), Henrique;
  - **Sala 3:** Matheus, Levi, Diego, Edinilson (confirmado em [01:54:39]: "Edinilson está no grupo do Mateus e do Levi");
  - **Sala 4:** Benjamin, Vinícius, Vanessa.

  (*) Há dois "Hélio" na transcrição: Helio Serrano Brandão Junior e Eldio Teixeira Junior, que o Whisper transcreve igual. **Não dá para saber qual está na Sala 1 e qual na Sala 2.** Um aluno disse que há mais uma pessoa ("tem um a mais também, mas ele está de férias"), e o instrutor respondeu: "quando ele voltar a gente organiza" [01:54:48]. Nome não dito (pode ser o João Pedro, que não respondeu à chamada, **inferência**).
- **Entrega:** "a entrega é um zip... sem a pasta target... só o código e os arquivos de configuração... upload no LMS, **no último dia de aula**" [01:59:01]. O último dia, pelo plano, é 18/11.
- **Apresentação:** "no dia da apresentação todos devem estar presentes; mesmo que você tenha participado, se não estiver presente no dia da apresentação, vai ficar **sem nota**". Basta uma pessoa apresentar, mas todos devem estar presentes [02:00:28].
- **Organização sugerida:** quadro Kanban com *backlog* e divisão de tarefas; a comunicação fica a critério do grupo (chat, WhatsApp, grupo no Teams) [01:58:29].
- **Versionamento:** "vocês não podem usar o GitHub público, porque não têm acesso aí de dentro"; colegas de outro curso teriam usado "GitHub ou GitLab interno da Caixa por VPN"; "senão, vão ter que versionar o código na mão"; "eu não consigo ajudar muito nisso" [01:59:24–02:00:25].
- **Trabalho nas salas** (17h01–17h54): ler a especificação e levantar dúvidas ("Não gostei disso, isso aqui não está claro"). Ele disse que visitaria cada sala; a visita **não está gravada**.
- **Lição de casa:** nenhuma tarefa formal além de avançar no projeto. Sugestão de estudo: o site da PortSwigger (CSRF) e o "game" de XSS.
- Critérios de avaliação (rubrica, pesos): **não foram ditos nesta aula**. Talvez estejam no `plano-ensino.pdf` (não visto).

---

## 5. Combinados e regras da turma

- Câmeras abertas "para quem pode" [00:04:41].
- Ficar depois do intervalo: "eu senti muita falta, muita gente foi embora logo depois do intervalo na aula anterior" [01:26:32].
- Presença obrigatória na apresentação do projeto, sob pena de ficar sem nota [02:00:28].
- Entrega em zip, sem `target`, no LMS, no último dia.
- O chat das salas do Teams deixa de funcionar quando a sala fecha. Vinícius levantou o ponto, e a turma resolveu criando um espaço próprio no Teams [01:55:15–01:58:27].
- Chamada no fim da aula [02:54:27].
- Nesta turma, os grupos foram mantidos do módulo anterior.

---

## 6. A turma

Lista montada pelos nomes nos quadros (galeria e lista do Teams) e pela chamada. Os quadros mostram 16 participantes além do instrutor. A área ou o cargo de cada aluno **não foi mencionado** nesta aula; vários usam fundo da Caixa ("Assunto sigiloso", Loterias Caixa).

| Nome (Teams) | Grupo | Participação nesta aula |
|---|---|---|
| Benjamin da Nobrega Bezerra | Sala 4 | Provavelmente o "Beijamim" de [00:55:15] (**incerto**): pergunta "se o hash tem uma fórmula, por que não conseguimos fazer o caminho de volta?". Câmera ligada. |
| Caio Cesar da Matta Souza | Sala 1 | Citado como dono de um grupo ("Grupo do Caio e o grupo do Edgar") [01:52:47]. Responde à chamada. Aparece **na sala principal durante todo o trabalho em grupo** (quadros `f_026` a `f_035`); a causa não se sabe. |
| Diego Marques Soares | Sala 3 | Responde corretamente que o *rate limiter* não basta contra DDoS, porque as requisições continuam chegando [00:37:37–00:37:50]. Câmera ligada. |
| Edgar (sobrenome truncado; iniciais "EJ") | Sala 2 | Citado como dono de um grupo. Responde à chamada ("Presidente", provavelmente "Presente"). |
| Edinilson Dafonte | Sala 3 | Só responde à chamada. Câmera desligada. |
| Eldio Teixeira Junior | Sala 1 ou 2 (incerto) | É um dos dois "Hélio" da chamada (**incerto**). Pode ser o "Gil" que perguntou se um arquivo compactado com senha é criptografia [01:00:13] (**inferência fraca**). Câmera ligada. |
| Helio Serrano Brandão Junior | Sala 1 ou 2 (incerto) | Responde à chamada. Câmera ligada. |
| Henrique Campanharo Seabra | Sala 2 | Responde à chamada. Volta à sala principal por volta de 17h45. |
| Ian Pereira da Silva Fernandes | Sala 1 | "Iã" na transcrição. Só responde à chamada. |
| João Pedro | ? | Chamado em [02:55:05], **sem resposta** registrada. Não aparece nos quadros. |
| Karla Danielle Santos da Silva | Sala 2 | "Carla" na transcrição. Só responde à chamada. Câmera ligada. |
| Levy Washington Rocha Moreira | Sala 3 | "Levi" na transcrição. Responde à chamada ("Aqui, professor"). Câmera ligada. |
| Matheus (sobrenome truncado; iniciais "MS") | Sala 3 | "Mateus" na transcrição. Só responde à chamada. |
| Paulo Ueiner Moreira de Assis | Sala 1 | Chamado; a transcrição corta logo depois. Câmera ligada. |
| Tiago da Silva Valeriano de Oliveira | Sala 1 | Responde à pergunta "hash e criptografia" [00:48:38], misturando os conceitos ("hash como forma de criptografia... chave para descriptografar"). O instrutor diz que ele "foi no rumo" e corrige. |
| Vanessa (sobrenome truncado; iniciais "VS") | Sala 4 | Sem fala identificada. |
| Vinícius Silva de Oliveira | Sala 4 | Pergunta sobre hash: "tem fórmula? palavras diferentes podem virar o mesmo hash?" [00:54:15]. Levanta a questão do chat das salas que fica inutilizável [01:55:15]. Câmera ligada. |

Falas sem dono identificado:

- "Brigê" (**incerto**), dúvida sobre SQL injection [00:14:16];
- quem sugeriu o Cloudflare [00:38:42];
- "Corriscain" (**incerto**), resposta sobre certificado autoassinado [01:22:46];
- quem propôs manter os grupos do módulo anterior [01:50:35];
- quem perguntou "o projeto já está no LMS?" [01:51:26];
- quem esclareceu "é da gente usar esse chat fora da aula" [01:58:04];
- "Priscil" [01:54:23], provavelmente erro do Whisper;
- "Alexandre" [00:06:42], ruído.

---

## 7. Dúvidas e dificuldades dos alunos

| Marca (BRT) | Quem | Dúvida | Resposta dada |
|---|---|---|---|
| 00:14:16 (15h14) | "Brigê" (incerto) | Como o sistema entende que o texto de um *input* é uma *query*? | Escreveu na hora um `SELECT` concatenado e mostrou o `' or 1=1`; a saída é `PreparedStatement`/JPA. Ele mesmo admitiu: "não vim preparado". A resposta foi aceita ("respondeu"). |
| 00:22:33 (15h22) | Instrutor | Dúvida se o link da PortSwigger abre na rede da Caixa. | "Tentem." Não houve retorno registrado. |
| 00:54:15 (15h54) | Vinícius | O hash tem fórmula? Textos diferentes podem dar o mesmo hash? | Tem fórmula, mas não tem volta. Colisão é "possível, mas muito difícil, na casa de um em um trilhão". |
| 00:55:15 (15h55) | Benjamin (provável) | Se tem fórmula, por que não dá para voltar? | "Gera um hexadecimal em cima de... curva elíptica... tem tantos conceitos que **eu não vou saber te explicar**." O propósito é ser via única. Resposta fraca (ver a seção 8). |
| 01:00:13 (16h00) | "Gil" (incerto) | Um zip ou rar com senha é criptografia? | "Não tenho certeza se o WinRAR criptografa ou só protege; tenho a impressão que criptografa." Citou o BitLocker ("becrypt", **incerto**). Resposta hesitante. |
| 01:12:52 (16h12) | Aluno | Avisa que o áudio caiu ("depois da chave pública ficou mudo"). | O instrutor perdeu o fio ("esqueci agora o contexto") e seguiu. |
| 01:51:26 (16h51) | Aluno | O projeto já está no LMS? | Sim, o enunciado está na Pasta Geral. |
| 01:55:15 (16h55) | Vinícius e outro aluno | O chat da sala some depois que a sala fecha; querem usá-lo fora da aula. | A turma cria o próprio espaço no Teams ou no WhatsApp; ele aprovou. |
| 01:59:24 (16h59) | Instrutor | Como versionar sem GitHub público na Caixa? | Não tem solução; sugere perguntar aos colegas sobre um Git interno via VPN ou versionar "na mão". |

---

## 8. Pontos de atenção para o novo instrutor

### 8.1 Imprecisões técnicas ditas em aula

Recomendo esclarecer estes pontos com a turma, principalmente os que afetam o projeto:

1. **"MD5... AES... qualquer um desses algoritmos de encriptação"** [00:51:43–00:51:54]. MD5 é **função de hash**, não algoritmo de criptografia. O próprio código dele está certo ("MD5 e SHA-1 estão quebrados"), mas a fala confunde.
2. **SHA-256 apresentado como "um dos melhores" para senha** [00:58:40], sem falar de **sal (*salt*)** nem de algoritmos lentos. Para armazenar senha, o recomendado é bcrypt, Argon2 ou PBKDF2 (no Spring Security, `BCryptPasswordEncoder`/`PasswordEncoder`); SHA-256 puro é rápido demais e fica exposto a força bruta e a *rainbow tables*. O projeto vai guardar senha (**inferência**: é uma API com autenticação), então **corrija antes de os grupos implementarem**. Ele também disse que no MD5 "fizeram a engenharia reversa... conseguem adivinhar o hash" [00:59:07]. O mais exato: o MD5 tem colisões práticas e é rápido o bastante para força bruta e tabelas pré-computadas; não existe "inversão" matemática.
3. **"Curva elíptica" na explicação do hash** [00:55:49]. SHA-256 não usa curva elíptica, que aparece em criptografia assimétrica (ECDSA/ECDH). Ele admitiu não saber explicar. Vale retomar a ideia de função de mão única com efeito avalanche.
4. **"Chave pequena de 32 bits... 32, 54 bits"** [00:57:37]. Chaves AES têm 128, 192 ou 256 bits. Provavelmente ele quis dizer 32 **bytes** (256 bits) (**inferência**).
5. **Assimétrica: primeiro errou e depois corrigiu** [01:03:16–01:04:17]. A correção ficou confusa na fala ("assino com a minha chave e com a minha chave entregue"). O correto: cifrar usa a chave **pública do destinatário**; assinar usa a **privada do autor**, e a verificação usa a pública do autor. O comentário no código dele está correto.
6. **CSRF descrito como se o atacante "recuperasse" o cookie ou o *authorization code*** [00:24:27–00:24:42]. No CSRF o atacante **não lê** o cookie: o navegador o envia sozinho na requisição forjada (o que lê o cookie é o XSS). Ele também não citou `SameSite`, a defesa moderna mais relevante e que se liga ao tema "Cookies", que não foi dado.
7. **"O Spring vem com CSRF enabled por padrão"** [00:21:32]. Vale para o **Spring Security** quando está no projeto, não para o Spring MVC puro. Em API REST *stateless* com JWT (Aulas 3 e 8), é comum desabilitar o CSRF, e a turma precisa entender quando isso é aceitável, senão "desligar CSRF" vai parecer falha.
8. **"Com JPA isso já está tratado"; "hoje é muito difícil fazer SQL injection"** [00:13:08, 00:19:10]. JPA protege **se a *query* for parametrizada**. JPQL ou *native query* montada por concatenação continua vulnerável. Injeção segue no OWASP Top 10.
9. **Rate limiter "bloqueia aquela pessoa que pede muita requisição"** [00:37:42]. A configuração mostrada (`alunoService`, 1 chamada a cada 10 s) é um **limite global da instância**, não por cliente ou IP. Para limitar por cliente seria preciso outra abordagem (gateway/proxy, Bucket4j por chave etc.). O ponto principal (proteger antes de chegar na aplicação) foi bem conduzido.
10. **"O primeiro ataque de DDoS que se tem conhecimento foi em 1974"** [00:32:20–00:33:05]. O caso de 1974 (garoto de 13 anos, terminais ligados a uma central) costuma ser citado como o primeiro **DoS**, não DDoS (não era distribuído). Ele usa DoS e DDoS como sinônimos ao longo da aula. Ele também diz "Deny of Service" em vez de *Denial of Service*.
11. **"Certificados valem um a dois anos no máximo"** [01:20:45]. O próprio certificado que ele mostrou (`*.ada.tech`, Google Trust Services) vale cerca de 3 meses (26/10/2026 → 25/01/2027). O teto para certificados TLS públicos já é menor que um ano e vem caindo por decisão do CA/Browser Forum. **Conferir o teto vigente antes de citar número.**
12. **"WebTrust" como autoridade máxima que assina certificados** [01:17:46, 01:23:49]. WebTrust é um **programa de auditoria** de autoridades certificadoras, não uma CA raiz. As raízes são CAs, por exemplo a raiz do Google Trust Services, que aparece na cadeia do LMS.
13. **"Nginx, nosso servidor de aplicação"** [00:40:20]. Ele corrigiu logo em seguida para "servidor web". Na mesma fala diz que é importante ter proxy, *load balancer*, Nginx e Cloudflare "configurados na sua máquina" [00:44:50]: generalização, sem distinguir o que é escopo do dev e o que é da infraestrutura.
14. **Colisão "um em um trilhão"** [00:55:01]: número ilustrativo, sem fonte. Para SHA-256 a probabilidade é muitíssimo menor.
15. Fala truncada na transcrição: "vamos gerar o hash do nosso password e vamos deixar ela no contexto claro da base de dados" [01:16:15–01:16:24]. Pelo contexto, ele quis dizer "**não** em texto claro" (**inferência**). Vale reforçar.

### 8.2 Problemas operacionais

- **A Aula 1 não terminou** e consumiu cerca de 40 min da Aula 2. O tema "Cookies" do plano não foi dado. Com isso, o cronograma já está atrasado (JWT ficou para 04/11).
- A **demonstração de SQL injection foi improvisada**, com um erro de compilação corrigido pelos alunos ("é o return na linha", [00:17:35–00:17:47]). Muitas trocas de janela e procura de arquivos ("cadê minha key", [00:56:25]).
- **Queda de áudio** por volta de 16h11–16h12, com trecho ininteligível na transcrição [01:11:02–01:11:50].
- **Acesso a sites externos na rede da Caixa** não confirmado (PortSwigger). **GitHub público bloqueado**: os grupos não têm repositório de código compartilhado garantido, risco real para um projeto em grupo de 3 semanas.
- O trabalho nas salas (cerca de 52 min) **não ficou gravado**. Não se sabe que dúvidas sobre a especificação surgiram nem se ele visitou as salas.
- Caio ficou na sala principal durante todo o trabalho em grupo (quadros). Pode ter havido falha ao mover para a sala; vale perguntar a ele (**inferência**).
- Fim às 17h56, quase no horário.

### 8.3 Promessas feitas aos alunos (verificar se foram cumpridas)

- "**Vou publicar esse código lá para vocês**" (o projeto `api-alunos` com hash, criptografia e testes) [01:09:44]. No quadro de 16h20 (`f_017`), a Pasta Geral tinha só o `.md` e o PDF.
- "Talvez eu possa trazer depois" um exemplo do token CSRF no site da Ada [00:27:36].
- "Vou visitar as salas e discutir com vocês" as dúvidas da especificação [01:51:14].
- "Quando ele voltar a gente organiza": o aluno de férias ainda sem grupo [01:54:53].

### 8.4 Clima

- O tom é cordial e informal, com português de Portugal ("pronto", "desculpe lá", "a correr", "câmaras"). Ele tenta envolver a turma com perguntas (DDoS, hash, autoassinado), mas reclama da pouca participação: "vocês têm que falar quase nada" [01:20:38]. Ao longo de 1h20 de exposição, só cerca de 6 alunos falaram.
- **Evasão depois do intervalo na Aula 1** foi registrada por ele [01:26:32].
- A turma é organizada: já tinha grupos do módulo anterior, resolveu a comunicação por conta própria e respondeu bem à chamada.
- Várias respostas do instrutor foram hesitantes ("não vou saber te explicar", "não tenho certeza"). Um novo instrutor com respostas firmes nos pontos de hash e senha deve ser bem recebido (**inferência**).

---

## 9. Citações-chave

1. [00:20:46] "O seu projeto tem que ter pelo menos ali tratamento para SQL Injection, tratamento para XSS."
2. [00:21:32] "O Spring vem por default... *enabled* por default. Para você sofrer um ataque de CSRF, você tem que lá deliberadamente colocar *false*."
3. [00:39:56] "O que é importante... é que essa camada seja feita antes de chegar na nossa aplicação. Porque se chegar na nossa aplicação você vai ter a negação de serviço na mesma."
4. [00:51:43] "Se eu usar o MD5, se eu usar o AES... qualquer um desses algoritmos de encriptação... no final das pontas eu consigo decriptar." (fala com erro; ver 8.1-1)
5. [00:55:54] "Tem tantos conceitos aqui que eu não vou saber te explicar. O propósito do hash é ter uma via única, sempre."
6. [01:15:16] "O que é importante é você entender o conceito de par de chaves, o que é uma chave simétrica... e entender o que é um hash, porque esse contexto é que vai ser aplicado nos projetos."
7. [01:20:45] "Por isso que certificados... valem ali por um a dois anos no máximo." (ver 8.1-11)
8. [01:26:32] "Eu senti muita falta, muita gente foi embora logo depois do intervalo na aula anterior."
9. [01:59:01] "A entrega é um zip... sem a pasta target... vão fazer o upload dele no LMS, no último dia de aula."
10. [02:00:28] "No dia da apresentação todos devem estar presentes; mesmo que você tenha participado, se não está presente no dia da apresentação, vai ficar sem nota."
