# Aula 3 · Qua 04/11/2026 · turma 1749 · BE-JV-013 (Fundamentos de segurança para aplicações)

Fontes: transcrição automática (Whisper small) `aula-03-2026-11-04-transcricao.txt`, lida por inteiro (1.387 linhas, fala até [02:07:33]); quadros do vídeo a cada 5 min (`f_001` a `f_030`), dos quais foram lidos `f_004` a `f_020`, `f_024`, `f_026` e `f_030`.
Horário real (BRT) = 15h00 + marca [hh:mm:ss]. O relógio do Windows do instrutor nos quadros mostra a hora de Portugal (+3h): `f_005` (20 min) marca 6:22 PM, ou seja, 15h22 em Brasília. Isso confere com a fala "estamos aqui no horário de Portugal" [00:26:00].
Termos corrigidos: "Kuki/Cook/QQ/QoOx/Cuckoo" = cookie; "Alt2/OWT 2" = OAuth 2; "ZWT/OWT/WTE" = JWT; "Gamec/gamaque/hmec" = HMAC; "HSM 256" = HS256; "Playload" = payload; "clens/clãs" = claims; "Sikiri" = Secure; "street" = Strict; "ss1d/S1ID/ssyniD" = session ID; "base MIA4" = Base64; "www.iou.com.br" = jwt.io; "Xixá/char 256" = SHA-256; "argum2d/becrypt" = Argon2id/BCrypt; "canta TPS" = HTTPS; "gata tpsession" = HttpSession; "Ixua Ed" = issued at (iat); "inspiration" = expiration (exp); "Kings" = salas (breakout rooms), provavelmente.

---

## 1. Linha do tempo

| Horário real (BRT) | Marca | Duração aprox. | O que aconteceu |
|---|---|---|---|
| 15h00–15h04 | 00:00–00:03:57 | 4 min | Espera pela turma. Ruído sem fala útil. "Então podemos começar, acho que a maioria do pessoal já está" [00:03:57]. |
| 15h04–15h07 | 00:04:21–00:06:43 | 3 min | Recapitulação da aula 2: criptografia, hash, chave simétrica e assimétrica, chave pública e privada, TLS/HTTPS, certificado autoassinado. |
| 15h07–15h17 | 00:06:43–00:17:22 | 10 min | Anuncia os dois temas do dia, cookie e JWT. Conceito de cookie em conversa com a turma (Matheus, um aluno não identificado, Diego). Gestão de cookies e LGPD/Marco Civil, anúncios (AdSense), partes do cookie, cookie primário, de sessão e "permanente". |
| 15h17–15h34 | 00:17:22–00:34:05 | 17 min | Demonstração do `CookieController` (endpoints `/boas-vindas`, `/login-teste`, `/login-teste/legivel`), DevTools (Application > Cookies, Console `document.cookie`, cabeçalho `Set-Cookie`). Pergunta do Vinicius sobre HttpOnly e XSS [00:27:15]. |
| 15h34–15h46 | 00:34:05–00:46:20 | 12 min | Pergunta à turma: "sessão expirou / fechei o navegador / o servidor guarda a sessão" são a mesma coisa? Os três tipos de sessão (navegador, servidor, usuário). Demonstração de `loginTestWithSessionID` no `CookieController`. |
| 15h46–15h50 | 00:46:20–00:49:54 | 4 min | Sessão no servidor guarda estado, logo não é RESTful (stateless). Exemplo de formulário em várias etapas guardado na sessão. |
| 15h50 | 00:49:54–00:50:26 | 1 min | Mostra rapidamente o `DesafioController` (desafio com a solução no próprio arquivo, para fazer depois). |
| 15h50–16h01 | 00:50:26–01:01:10 | 11 min | `SessaoController` e `UsuariosDemo` (login com `HttpSession`, hash SHA-256). Aluno aponta que SHA-256 é rápido demais e cita Argon2id/BCrypt [00:52:20]. Tenta debug (falha e depois funciona), recebe 401 no perfil, refaz o login e recebe 200, depois logout com `invalidate()`. Fecha o tema cookie com regras. |
| 16h01–16h34 | 01:01:25–01:34:19 | 33 min | JWT: perguntas provocativas à turma (roubar, forjar, segredo), jwt.io, RFC 7519, estrutura header.payload.signature, claims (`iss`, `sub`, `iat`, `exp`, `role`), Base64, HMAC, demo do `JwtManual` decodificado no jwt.io. Pergunta sobre o segredo diferente no jwt.io [01:22:44]. Intervenções longas do Tiago e de outro aluno. Analogia do crachá. Primeira menção a OAuth 2 e a Client Credentials. |
| 16h34–16h35 | 01:34:19–01:35:47 | 1 min | Libera o intervalo. Matheus avisa que sai por volta das 17h20 (formatura da faculdade) e pergunta sobre a presença. |
| ~16h35–16h56 | 01:35:47–01:56:01 | ~20 min | **Intervalo.** Sem fala; os quadros `f_020` a `f_023` (16h35–16h50) mostram só a grade de participantes com câmera desligada. |
| 16h56–17h00 | 01:56:01–01:59:44 | 4 min | Volta e divide a turma em salas (breakout rooms) para trabalhar no **projeto**. Recado sobre o escopo do projeto. Ajuste de quem ficou sem sala (Tiago, Paulo, João Pedro). |
| 17h00–17h04 | 01:59:44–02:04:11 | ~4 min | Silêncio na sala principal (grupos nas salas). |
| 17h04–17h05 | 02:04:15–02:05:34 | 1 min | Um aluno (não identificado) volta à sala principal: o requisito de Git no projeto é obrigatório? Resposta: não é; o instrutor vai tirar do documento. |
| 17h06–17h07 | 02:06:40–02:07:33 | 1 min | Tiago volta ("voltei do intervalo") e é posto na sala 1. Última fala da transcrição. |
| 17h07–~17h29 | 02:07:33–02:29 | ~22 min | Silêncio na sala principal; o instrutor fica na câmera (`f_026` às 17h05, `f_030` às 17h25). |
| **~17h29** | 02:29 | | **Fim da gravação.** Não há fala de encerramento, chamada ou despedida gravada. Pelo horário oficial a aula vai até 18h; não há evidência do que aconteceu entre 17h29 e 18h nem de reunião geral para fechar. |

Distribuição aproximada: conteúdo expositivo e demonstração ~1h30 (15h04–16h34); intervalo ~20 min; trabalho no projeto em salas ~33 min gravados (16h56–17h29).

---

## 2. Conteúdo dado

### 2.1 Recapitulação da aula 2 [00:04:21–00:06:43] (explicado, sem código)
- Corrige o que disse na aula anterior: simétrica = uma mesma chave cifra e decifra; assimétrica = par de chaves. A transcrição tem um trecho truncado em [00:04:50] ("chave simétrica é você usa duas chaves, uma mesma chave").
- Exemplo: "se eu quero que só eu leia, entrego minha chave pública; só com a minha privada abro" [00:05:16].
- HTTPS/TLS: chave pública no navegador, privada configurada no servidor [00:05:42].
- Certificado autoassinado: "são seguros", mas a cadeia não é reconhecida (não há *web of trust*), o navegador mostra o alerta e você "avança por sua conta" [00:06:05–00:06:41].

### 2.2 Cookies: conceito [00:06:53–00:17:22] (explicado, em conversa)
- Anúncio: "dois assuntos que a gente deve ver hoje: o cookie e o JWT" [00:06:53]; o cookie "ficou faltando" da aula passada [00:06:43].
- Gestão de consentimento de cookies nos sites; não lembra se veio da LGPD ou do Marco Civil da Internet [00:07:49–00:08:22].
- Definição construída com a turma: pequenos arquivos de texto criados pelo site e guardados no navegador; limite de 4 KB [00:10:23]; servem para preferências do usuário; usados por publicidade (AdSense, YouTube); exemplo da Caixa mostrando anúncio de empréstimo a quem visita sites de empréstimo [00:13:58]. Separa explicitamente o uso em publicidade do foco da aula, que é segurança [00:14:37].
- "O cookie não deve identificar um usuário... olhar o cookie e falar que ele é o Matheus" [00:09:18].
- Cookie para manter autenticação "em aplicações mais antigas / legado / monolíticos, quando a gente mantém estado" [00:11:05–00:11:44]; hoje há JWT e OAuth 2, "mais padrões de mercado do que mais seguros" [00:12:11].
- Estrutura: nome, valor (chave-valor) e atributos, por exemplo a expiração [00:14:55].
- Tipos: primário (*first-party*, "é nosso") [00:15:41]; de sessão (some ao fechar o navegador) [00:16:18]; "permanente, que não expira nunca" [00:16:44] (ver imprecisão na seção 8). Recomenda limpar cookies de tempos em tempos [00:17:03].

### 2.3 Demonstração: `CookieController` [00:17:22–00:34:05] (demonstrado em código e no navegador)
Projeto IntelliJ `api-alunos` (Spring Boot, `ApiAlunosApplication`, porta 8080, HTTP), pacote `com.ada.seguranca`, subpacotes `aluno`, `cookie`, `cripto`, `jwt`, `xss` (`f_004`), arquivo `http/aula03.http` e `http-client.env.json`. O caminho local que aparece em `f_013` é `C:\Users\nicolau.oliveira\ada\BE-JV-013 Fundamentos de segurança para aplicações\aula\aula03\source`. O código é muito comentado em português, com referências cruzadas às aulas 01, 02 e 08 e perguntas para a turma ("PERGUNTE: ...").

O que o código faz (lido nos quadros `f_004`–`f_010`):
- **Campos:** `Map<String,String> sessoes = new ConcurrentHashMap<>()`, a "memória" do servidor (ID da sessão para usuário). O comentário diz que "em um sistema real isto seria a HttpSession do Tomcat (ou um Redis)" e que reiniciar a aplicação apaga tudo. Há também `SecureRandom aleatorio` ("o `java.util.Random` é previsível").
- **`GET /boas-vindas`:** `@CookieValue(name="user-id", defaultValue="default-user-id") String userId` devolve `"Olá, usuário " + userId`. Sem o cookie, responde com o valor padrão [00:18:19].
- **Variante "estrita"** (sem `defaultValue`): sem o cookie, dá erro. O log mostra `MissingRequestCookieException: Required cookie 'user-id'...` (`f_005`). Ele explica que o Spring exige o cookie na requisição [00:19:38–00:20:08].
- **`GET /login-teste`:** simula um login. `ResponseCookie.from("user-id","42")` com `.httpOnly(true)`, `.secure(true)`, `.sameSite("Strict")`, `.path("/")`, `.maxAge(Duration.ofHours(1))`, devolvido no cabeçalho `Set-Cookie: user-id=42; Max-Age=3600; Path=/; Secure; HttpOnly; SameSite=Strict` [00:23:04]. Depois disso, `/boas-vindas` passa a responder "usuário 42" [00:24:31].
  - Comentários do código: HttpOnly bloqueia o roubo por XSS (aula 01); Secure só envia em HTTPS ("em localhost (HTTP) o comportamento varia por navegador: teste antes da aula"); SameSite Strict/Lax/None bloqueia CSRF (aula 01); sem Max-Age seria cookie de sessão do navegador e, com 1 hora, é cookie permanente.
  - Alerta didático: "isso nunca pode acontecer... se eu altero 42 para 55 eu vou ser o usuário 55" [00:23:31–00:24:13].
- **`GET /login-teste/legivel`:** cria o cookie `user-id-legivel=42` **sem** HttpOnly ("Cookie legível criado"). No DevTools (`f_007`) aparecem os dois cookies: `user-id` com HttpOnly ✓, Secure ✓ e SameSite Strict, e `user-id-legivel` sem flags. Expiração uma hora à frente (18h30 para 19h30, hora de Portugal) [00:25:49–00:26:13].
- **Console:** `document.cookie` mostra só o `user-id-legivel`, e não o HttpOnly [00:31:24–00:32:21]: "na Application eu consigo ver, mas com o JavaScript eu não consigo capturar".
- **Aba Network / cabeçalhos:** mostra o `Set-Cookie` na resposta e a constante `HttpHeaders.SET_COOKIE` do Spring (`f_008`) [00:33:10–00:34:05]. Um trecho em [00:32:40–00:33:03] ficou ininteligível; ele cita "Postman" ali.
- **`loginTestWithSessionID`** [00:39:01–00:46:47] (`f_010`): recebe `@RequestParam(name="usuario", defaultValue="ana")`, valida o valor (400 "usuário inválido"), gera 16 bytes com `SecureRandom` ("128 bits, impossível de adivinhar"), converte com `HexFormat.of().withUpperCase().formatHex(bytes)` (32 caracteres), faz `sessoes.put(sessionId, usuario)` ("a ficha fica no servidor") e devolve só o ID num cookie HttpOnly/Secure/SameSite. Resposta: "Sessão Ana criada" [00:46:40]. Um comentário do código cita `perfilTestWithSessionId` "no fim desta classe", que não foi mostrado.

### 2.4 Os três tipos de sessão [00:34:15–00:46:20] (explicado com código)
- Pergunta à turma; dois alunos respondem (ver seção 6).
- **Sessão do navegador:** período em que janela ou aba fica aberta e os cookies valem. Ele mistura com *Session Storage* e Max-Age (ver seção 8) [00:37:08–00:38:15].
- **Sessão do servidor:** "uma ficha", memória (ou "algum tipo de Redis") no servidor, identificada pela chave enviada ao cliente. Vale até o logout ou o TTL [00:38:17–00:43:55]. Fragilidade: "se a pessoa roubar esse session ID, ela pode se passar por outra pessoa"; atenuação com validade curta (10 a 15 min) e renovação; "trade-off" [00:43:55–00:44:46].
- **Sessão do usuário:** "estado de logado", resultado de um login, controlado pela aplicação e pela regra de negócio [00:44:47–00:45:46].
- Formulário em várias etapas (*form wizard*) guardado na sessão do servidor [00:47:17–00:48:11].
- **REST stateless:** sessão no servidor guarda estado, então "não é uma API RESTful" [00:48:21–00:49:43].

### 2.5 `SessaoController` + `UsuariosDemo` [00:50:26–01:00:59] (demonstrado em código, com debug)
O que o código faz (`f_011`, `f_012`, `f_013`):
- **`UsuariosDemo`:** "base de dados" em memória. Guarda o **hash** SHA-256 da senha (classe `FuncaoHash`, escrita à mão), não a senha em texto claro. `autenticar(username, senha)` protege contra campos nulos, faz `Optional.ofNullable(usuarios.get(username)).filter(u -> u.hashDaSenha().equals(hash.sha256(senha)))` e devolve vazio tanto para "usuário não existe" quanto para "senha errada", para não revelar quais usuários existem. O comentário do código diz: "SHA-256 é rápido demais... em produção, BCrypt (lento de propósito), na aula 08".
- **`POST /sessao/login`** com corpo `{"username":"ana","password":"senha123"}` (`record LoginRequest(String username, String password)`) e `HttpSession sessao`: autentica ou lança 401 "Usuário ou senha inválidos."; faz `sessao.setAttribute(ATRIBUTO, usuario.username())`; devolve `{"mensagem":"Sessão criada para ana"}`. O comentário traz a pergunta "se rodarmos 3 instâncias do servidor atrás de um balanceador, o que acontece?", com a resposta "401... É o problema que o token resolve (ou exige sessão compartilhada, como um Redis)". **Essa pergunta não foi feita em aula**, segundo a transcrição.
- **`GET /sessao/perfil`:** sem nome na requisição, o servidor descobre o usuário pelo cookie `JSESSIONID` que o navegador manda sozinho. Sem sessão, responde 401 "Não autenticado".
- **`POST /sessao/logout`:** `sessao.invalidate()` ("o servidor esquece o usuário"). Comentário: "Vantagem da sessão: o logout é REAL... PERGUNTE: com token isso é tão simples? (Bloco 5: o token vale até expirar)".
- Requisições disparadas pelo `aula03.http` no cliente HTTP do IntelliJ ("1.2 Perfil por sessão", `f_013`).
- **Percalços da demo:** o debug não subiu de primeira [00:55:33–00:55:50] (ele aproveita e diz que "é muito importante vocês aprenderem a fazer debug"). Depois o perfil voltou 401 porque o login não tinha rodado; ele refez o login e recebeu 200 [00:57:41–00:59:20]. No painel de debug (`f_012`) aparece `LoginRequest[username=ana, password=senha123]` e o `hashDaSenha`.
- **Fechamento de cookies** [00:59:59–01:01:16]: nunca colocar a identidade do usuário no cookie; apagar o cookie não apaga a sessão no servidor; "roubar cookie é roubar a sessão". "Isso era tudo que a gente ia falar de cookie... ficou maior do que eu pretendia."

### 2.6 JWT [01:01:25–01:34:19] (explicado + demonstrado no jwt.io e em código manual)
- Abre com perguntas à turma: o que é? (token em formato JSON; serve para passar autenticação entre aplicações); "se eu roubo o JWT de alguém, consigo me passar pela pessoa?" (sim, enquanto for válido; uso do Postman para pegar o token no login e reutilizá-lo) [01:02:36–01:03:31]; "posso forjar um JWT?" (só com o *secret*) [01:03:36–01:04:18].
- "JWT é a forma de autenticação do momento" [01:04:25]; o cliente guarda o token; ele pode ser capturado nas requisições; por isso precisa ter **validade curta** [01:05:41].
- **jwt.io** [01:06:07]: token de exemplo padrão do site (`sub` 1234567890, `name` John Doe, `admin` true, `iat` 1516239022; `f_014`–`f_016`). Header, payload e assinatura, cada um com sua cor.
- "Não existe criptografia aqui; existe carimbo e assinatura" [01:07:26–01:07:35].
- **RFC 7519**, padrão aberto, compacto [01:07:49–01:08:29]; claims ou atributos e assinatura [01:09:00–01:09:11].
- **Assinatura:** prova que os dados não foram alterados e que o token vem daquele servidor; com o segredo, qualquer um forja: "se o seu segredo está público, a sua aplicação está toda exposta" [01:09:40–01:11:14].
- **Estrutura:** header (algoritmo HS256, tipo JWT) [01:11:58]; payload (claims) [01:12:16]; assinatura [01:12:55]. Se alguém troca "João" por outro valor, a assinatura não bate e o servidor nega (401) [01:13:01–01:13:36].
- **Claims registradas** (abre a RFC): `iss` (emissor), `sub` (de quem é o token), `iat` (data de emissão), expiração, e "role" [01:13:40–01:14:41].
- **Base64** (ver imprecisão na seção 8): "Base64 não é criptografia, guardem isso" [01:14:53–01:16:15].
- **HMAC:** o servidor recalcula a assinatura sobre header + payload com o segredo a cada requisição; se mudar algo, não bate [01:16:20–01:17:36].
- **Demo `JwtManual`** [01:17:38–01:22:34]: segredo fixo no código ("demonstração da aula 3, não usa em produção"; sem variável de ambiente; "quanto maior, mais protegido"). Método `gerar(chave, usuario, perfil, segundosValidade)` monta o header `{"alg":"HS256","typ":"JWT"}` e o payload com `iss`, `sub`, `role`, `iat` e `exp`, codifica em Base64URL, concatena com ponto e calcula o HMAC-SHA256 "na mão" (função própria). Ele repete que no Spring não se faz assim e que "na próxima aula vocês vão perceber como isso deve ser melhor feito". O token gerado, decodificado no jwt.io (`f_017`–`f_019`), traz `"iss":"ada-seguranca"`, `"sub":"ana"`, `"role":"USER"`, `"iat":1793820123`, `"exp":1793820423`. **Inferência:** a diferença é de 300 s, logo validade de 5 minutos. Não ficou claro na fala se o token veio do `JwtController` ou de uma requisição do `aula03.http`. O campo de segredo do jwt.io mostrava o valor padrão do site (`a-string-secret-at-least-256-bits-long`), não o segredo da aplicação.
- Cuidado com dados sensíveis no payload (e-mail, nome: "não é problema, mas isso é público") [01:22:23–01:22:29; 01:24:01–01:24:20]; exemplo do cibercafé com o navegador aberto [01:24:22].
- **Analogia do crachá** [01:28:19–01:29:14; 01:32:52]: "o JWT é um crachá; crachá não garante que a pessoa é a pessoa"; exemplo de entrar no prédio da Caixa (biometria na entrada e crachá dentro, em vez de digital em cada porta) [01:31:53–01:32:52].
- **Gancho para OAuth 2** [01:30:25–01:31:50]: "quando a gente for ver o OAuth 2 na próxima aula... vários tipos de autenticação, uma delas é Client Credentials", que ele confunde com Basic Auth (ver seção 8); Basic Auth aceitável entre aplicações próximas em ambiente seguro, não para API REST com clientes externos.
- **Não mostrado:** `JwtBiblioteca` e `JwtController` aparecem na árvore do projeto, mas não foram abertos (na transcrição só há a demo do `JwtManual`). **Inferência:** `JwtBiblioteca` é o material da "forma mais elegante" prometida para a próxima aula.

---

## 3. Comparação com o plano

| Aula | Plano | O que houve |
|---|---|---|
| 02 (02/11) | HTTPS e Cookies | Pela fala, a aula 2 cobriu cripto, hash, chaves e TLS, mas **cookies "ficou faltando"** [00:06:43]. Foram dados nesta aula 3 (~55 min, 15h07–16h01, "ficou maior do que eu pretendia" [01:01:10]). |
| **03 (04/11)** | **JWT** | JWT dado **em parte** (~33 min): conceito, estrutura, claims, assinatura HMAC, Base64 e JWT manual. Não foram feitos validação de token no servidor, uso de biblioteca, JWT com Spring, refresh token nem revogação. "Acho que a gente não vai conseguir ver tudo de JWT hoje" [01:06:36]. |
| 04 (06/11) | OAuth 2.0 | **Anunciado:** "A gente entra na próxima aula já falando de OAuth 2 com o JWT" [01:06:39]; "na próxima aula, for implementar mesmo um JWT... de uma forma muito mais elegante, usar as funções do próprio Spring" [00:54:03–00:54:12]; "Na próxima aula vocês vão perceber como isso deve ser melhor feito" [01:20:52]; Client Credentials no OAuth 2 [01:30:25]. |
| 08 | Spring Security básica e JWT | O comentário do `UsuariosDemo` remete o BCrypt à aula 08. Há sobreposição com a promessa de "JWT com Spring" na aula 04: decidir onde fica. |

Retomado de aulas anteriores: XSS e CSRF da aula 01 (para explicar HttpOnly e SameSite) [00:20:53; 00:21:50; 00:27:36–00:28:36]; chaves e hash da aula 02 [00:04:30; 00:51:24; 00:59:56].
Pendente e sem data na fala: a pergunta do balanceador (3 instâncias) e a do logout com token ("Bloco 5"), que estão nos comentários do código e não foram feitas; `perfilTestWithSessionId`; `JwtBiblioteca` e `JwtController`.

---

## 4. Projeto avaliado, atividades, desafios e lição de casa

- **`DesafioController`** [00:49:54–00:50:23]: "um probleminha que vocês podem resolver". **A solução está no próprio arquivo**, "bem comentada". Sugestão: ler o desafio e tentar resolver sem olhar a solução, depois trazer dúvidas. "Eu vou subir lá." O conteúdo do desafio não foi lido em voz alta nem aparece nos quadros lidos.
- **Código no repositório:** "Vou subir esse código para vocês, vou deixar disponível o código da aula 1" [00:17:34]. Não está claro se ele promete também o código da aula 3; o contexto sugere que sim.
- **Projeto em grupos** [01:56:31–01:57:16]: trabalho nas salas na segunda parte da aula (já anunciado em [01:06:46]: "na segunda parte da aula eu queria que a gente trabalhe nos projetos"). Recado: "cumprir aquele roteiro, tentar entregar o máximo possível... O projeto é muito grande, muito audacioso... não deixem de apresentar... apresentem o que conseguirem... Não precisa estar 100% para apresentar. Caprichem."
- **Tema do projeto (inferência):** o Tiago fala em "acessar as listas do teu projeto... dos teus chamados... a API de chamados" [01:26:51–01:27:44], o que indica que o projeto é uma API de **chamados** (tickets) com autenticação. Confirmar no documento do projeto.
- **Git:** um aluno pergunta se o requisito de usar Git/GitHub vale, porque "o grupo" entendeu que não [02:04:15–02:05:34]. Resposta: "não é preciso... foi por um vacilo [ter deixado no documento]"; o documento dizia algo como "repositório Git compartilhado logo no início, quem revisa o quê"; "**Eu vou tirar tudo que é Git e vou atualizar o documento**". O aluno diz que o instrutor já tinha falado isso em aula antes.
- **Andamento dos grupos:** não há registro, porque o instrutor ficou na sala principal e a gravação não pega as salas. Não se ouve ele visitando as salas.
- **Data da entrega/apresentação (18/11):** **não foi mencionada nesta aula.**
- Lição de casa formal: nenhuma além do `DesafioController` e de "ler o código com mais calma" (`UsuariosDemo`/hash) [00:53:26].

---

## 5. Combinados e regras da turma

- **Participação:** "Me interrompam... abra o microfone e fala 'professor, Nicolau'", porque muitas vezes ele está olhando para a IDE e não para o Teams e não vê a mão levantada [00:26:56–00:27:07]; "pode errar... eu quero que vocês participem" [00:09:54]; "não deixem eu falar sozinho" [00:12:23]; "não deixem passar com dúvidas" [00:39:21].
- **Presença de quem sai cedo:** Matheus sai às ~17h20 (formatura). Combinado: um colega lembra o instrutor na hora da chamada para ele dar presença ao Matheus [01:35:04–01:35:43]. **A transcrição não registra chamada nenhuma nesta aula.**
- **Formação dos grupos:** o instrutor distribui as salas no Teams; "quem ficar aqui a gente reorganiza" [01:57:16–01:57:31].
- Tratamento: o instrutor pede para ser chamado de "professor, Nicolau" [00:27:02]; os alunos o chamam de "professor".

---

## 6. A turma

Nomes completos vêm da grade do Teams (`f_020`, `f_024`, `f_026` e outros); a associação fala-pessoa vem da transcrição. Nomes com (?) são incertos.

| Aluno | O que se viu nesta aula |
|---|---|
| **Matheus Fonseca Sousa** | Primeiro a responder sobre cookies ("dados auxiliares que ajudam a identificar o usuário... vender dados") [00:08:46–00:09:12]. Sai às ~17h20 para a formatura da faculdade e pediu a presença [01:34:55]. Faz parte do grupo do João Pedro e do Levy [01:58:35]. Câmera ligada a maior parte da aula. |
| **Diego Marques Soares** | Complementa: cookies guardam preferências e padrões de navegação [00:10:38–00:10:58]. |
| **Vinicius Silva de Oliveira** | Pergunta [00:27:15]: no XSS com HttpOnly, o que o atacante tentaria, modificar o próprio cookie? Câmera ligada, fundo "Assunto sigiloso" da Caixa. |
| **Tiago da Silva Valeriano** (Teams "Tiago da S..."/"TO") | Instrutor pergunta "Tiago ficou claro?" [01:26:06]; ele faz intervenção longa sobre o token liberar só os "chamados" do usuário e sobre o risco de copiar o token de outra pessoa [01:26:13–01:27:51]; corrige a si mesmo de "descriptografar" para "decodificar" [01:28:03–01:28:10]. **Possível autor** da pergunta [01:22:44] sobre o jwt.io ler o payload com outro segredo (inferência pela sequência). Saiu (bateu ponto?) e voltou às 17h06 "voltei do intervalo"; posto na sala 1 [02:06:40–02:07:30]. Mostra raciocínio de quem já integra APIs. |
| **Paulo Ueiner Moreira de Assis** | Na hora das salas: "Paulo falando", está na sala com "Elvio" (= Eldio), Thiago e Ian [01:58:07]. Fundo da Caixa ("CAIXA CAIXA..."). |
| **João Pedro Moreto Lourenção** | Ficou sem sala: "é pra eu estar no grupo do Levy e do Matheus. E de News(?)" [01:58:35–01:58:47]; o Teams deu erro ao adicioná-lo. **Incerto** se é ele ou o Tiago quem diz "tive que bater meu ponto aqui... vou esperar uns 5 minutos" [01:58:27; 01:59:12]. |
| **Eldio Teixeira Junior** | Câmera ligada quase a aula toda; citado como "Elvio" no grupo do Paulo [01:58:07]. Não falou na gravação. |
| **Benjamin da Nóbrega Bezerra** | Câmera ligada. **Possível** autor da observação técnica [00:52:20] de que SHA-256 é rápido e que o ideal são Argon2id/BCrypt: o Whisper transcreveu o chamado do instrutor como "Fala lá, veja a mim" (incerto). |
| **Caio César da Matta Sou...** (sobrenome cortado) | Câmera ligada em vários quadros. Sem fala identificada. |
| **Hélio Serrano Brandão Júnior** | Câmera ligada em parte. Sem fala identificada. |
| **Levy Washington Rocha Moreira** | Citado como do grupo do João Pedro e do Matheus. Sem fala identificada. |
| **Ian Pereira da Silva Fernandes** | Citado no grupo do Paulo [01:58:07]. Sem fala identificada. |
| **Karla Danielle Santos da Silva** | Só na lista de participantes. |
| **Henrique Campanharo Seabra** | Só na lista. |
| **Edinils... (ED)** (nome cortado; "Edinilson"?) | Só na lista (`f_024`). |
| **Edgar J...** (nome cortado) | Só na lista (`f_024`). |

Falas sem autor identificável:
- Aluno que "arrisca junto com o Matheus": cookie é "pequeno arquivo salvo no navegador, pode armazenar qualquer informação" [00:09:57].
- Resposta sobre sessão [00:35:00] (tempo de vida, guardada em memória) e outra, mais elaborada, sobre renovação a cada requisição, com o exemplo do aplicativo da Caixa (Previdência?) que avisa "sua sessão vai expirar em 30/20 minutos" [00:35:38–00:36:24]. Pode ser a mesma pessoa.
- Respostas sobre JWT [01:01:46; 01:02:06; 01:02:43; 01:03:44] e quem acerta "o secret" [01:04:11].
- Aluno que explica o token como forma de não reenviar login e senha a cada requisição [01:29:18–01:30:24] (o instrutor diz "Sim, pode curir(?)... vai lá, força").
- Aluno que volta das salas para perguntar sobre o Git [02:04:15].

Perfil geral (inferência a partir das falas): servidores ou empregados da Caixa (fundos de tela "CAIXA" e "Assunto sigiloso"; "bater ponto"; exemplos do app da Caixa); boa base de desenvolvimento web (Postman, tokens, hash lento); participação ativa de 5 a 7 pessoas.

---

## 7. Dúvidas e dificuldades dos alunos

| Marca | Aluno | Dúvida | Resposta dada |
|---|---|---|---|
| [00:27:15] | Vinicius | Com HttpOnly bloqueando XSS, o atacante tentaria modificar o próprio cookie? | Relembra o XSS (comentário de blog executado como JS); o script, quando o admin logado abre a página, capturaria o cookie e mandaria para outro servidor; HttpOnly impede essa leitura. Mistura com um exemplo de "injetar cookie de imobiliária" para anúncios [00:27:36–00:30:46]. Vinicius: "Sim, sim". |
| [00:52:20] | Benjamin (?) | SHA-256 é rápido demais para senha; o ideal são Argon2id/BCrypt. | Concorda: "não é o ideal, aqui é só exemplo"; BCrypt é o mais visto; não vamos implementar nosso próprio SHA nem BCrypt; na próxima aula será "mais elegante" com o Spring [00:52:42–00:54:28]. |
| [01:22:44] | Tiago (?) | Por que o jwt.io conseguiu ler as informações do JWT gerado no código se o secret é diferente? | "Excelente pergunta": duas das três partes são abertas (Base64), qualquer um decodifica; o segredo só garante a integridade via assinatura; se trocar "Ana" por "Nicolau", o HMAC não bate [01:23:05–01:26:01]. |
| [01:26:13] | Tiago | Se eu pegar o token de outra pessoa, acesso os chamados dela enquanto não expirar? | Sim; "o JWT é um crachá" [01:27:51–01:29:14]. |
| [01:29:18] | não identificado | O token serve para não ter de mandar login e senha em toda requisição? | Sim; Basic Auth / "Client Credentials" mandam credenciais a cada requisição; aceitável entre sistemas internos próximos, não para APIs REST com clientes; OAuth 2 dá flexibilidade (próxima aula) [01:30:25–01:34:13]. |
| [01:34:55] | Matheus | Presença saindo às 17h20. | Colegas lembram o instrutor na chamada. |
| [01:58:23] | João Pedro / Tiago (?) | Sem sala, ou erro do Teams ao entrar na sala. | Instrutor reposiciona; Tiago na sala 1 [02:07:22]. |
| [02:04:15] | não identificado | Git é requisito do projeto? | Não; vai tirar do documento e atualizar. |

Dificuldades observadas: nenhuma dificuldade de compreensão explícita. As dificuldades foram operacionais (salas do Teams, debug que não subiu, 401 na demo).

---

## 8. Pontos de atenção para o novo instrutor

### 8.1 Imprecisões técnicas ditas em aula (vale corrigir de leve na abertura da aula 04)
1. **Secure em localhost** [00:21:37–00:21:42]: "ele só envia dentro HTTPS, como a gente não está usando HTTPS, ele acaba não trabalhando". O próprio demo contradiz isso: o cookie `user-id` com Secure ✓ foi gravado e `/boas-vindas` leu "42" em `http://localhost:8080` (`f_007`, [00:24:31]). O Chrome trata `localhost` como contexto seguro. O comentário do código já avisa: "em localhost (HTTP) o comportamento varia por navegador".
2. **CSRF descrito como roubo de cookie** [00:21:50–00:22:49]: "você injeta ali um cookie e manda para o e-mail... como eu capturei o cookie... eu consigo me passar pela pessoa". No CSRF o atacante **não** captura o cookie; ele induz o navegador da vítima a enviar a requisição, e o navegador anexa o cookie sozinho. E "eu só aceito os cookies vindos do próprio site" inverte o papel: quem decide é o navegador, que **não envia** o cookie em requisição *cross-site*. O comentário do código está correto.
3. **Cookie de sessão × Max-Age × Session Storage** [00:37:17–00:38:12]: "vamos setar ali o max-age... aí são os cookies da sessão... aqui no Session Storage". Está invertido e misturado: cookie **sem** Max-Age/Expires é de sessão, e com Max-Age é persistente (o comentário do código diz isso). *Session Storage* é a Web Storage API, outra coisa.
4. **"Cookie permanente que não expira nunca"** [00:16:44]: cookie persistente tem expiração definida, e navegadores como o Chrome limitam a validade máxima (hoje 400 dias).
5. **Exemplo de "injetar cookie de imobiliária"** para explicar HttpOnly [00:29:47–00:30:41]: HttpOnly protege contra **leitura** pelo JS (`document.cookie`); o exemplo de injetar cookies de anúncio confunde o objetivo da flag.
6. **"Cookie com nome fere diretamente a LGPD"** [01:00:10–01:00:17]: é um exagero. O problema demonstrado é confiar num valor controlado pelo cliente e sem assinatura (trocar 42 por 55); a LGPD não proíbe dado pessoal em cookie por si só.
7. **"Senha sempre encriptada"** [00:56:43]: o código usa hash, que não é cifra. O instrutor diferenciou os dois na aula 2; aqui escorregou.
8. **Assinatura do JWT ligada à chave assimétrica** [01:09:50–01:09:56]: o demo usa **HS256 (HMAC, segredo compartilhado, simétrico)**. Convém separar HS256 (segredo) de RS256/ES256 (par de chaves), sobretudo antes de OAuth 2/OIDC, em que o JWKS é assimétrico.
9. **Base64 "64 caracteres... deixa mais compacto e consegue escrever mais coisas"** [01:14:59–01:15:36]: o contrário. Base64 **aumenta** o tamanho em ~33%. O motivo é transportar bytes como texto seguro em URL e cabeçalho (Base64**URL** no JWT), e essa parte ele disse certo em [01:15:38]. Também disse certo que Base64 não é criptografia.
10. **"HMAC gera essa chave"** [01:16:28–01:16:47]: o HMAC gera a **assinatura** (MAC), não uma chave.
11. **`role` como claim da RFC** [01:14:35–01:14:41]: `iss`, `sub`, `aud`, `exp`, `nbf`, `iat` e `jti` são as registradas na RFC 7519; `role` é *claim* privada da aplicação. Ele disse "Xur/emitido" para `iss` (issuer, emissor), o que está correto.
12. **"Client Credentials: login e senha em cada request"** [01:30:35–01:31:26]: ele se corrigiu para Basic Auth no meio da frase, mas a associação ficou. **Client Credentials** é o *grant* do OAuth 2 para máquina a máquina: `client_id`/`client_secret` trocados por um *access token*. Ponto crítico porque a aula 04 é OAuth 2.
13. **"JWT é forma de autenticação... não é de autenticação, mas é de autenticação"** [01:04:25–01:04:39]: ficou confuso. JWT é formato de token; o protocolo de autenticação é outro (OAuth 2/OIDC, aulas 04–06).
14. **TLS** [00:05:42–00:06:01]: "chave pública no navegador... somente aquele navegador consegue ler". É simplificação: o par de chaves autentica o servidor e protege a troca de chaves; os dados trafegam com chave simétrica de sessão. "Certificados autoassinados são seguros" [00:06:05] precisa da ressalva de que cifram, mas não autenticam quem está do outro lado (risco de MITM).
15. Trecho final confuso sobre session ID dentro de JWT e "meu JWT se torna inválido, exijo novo login" [01:33:49–01:34:13]: a ideia de revogação/expiração ficou embolada.

### 8.2 Problemas operacionais
- **Gravação termina às ~17h29**, sem fechamento, chamada ou combinados finais gravados. Não se sabe se a turma voltou à sala principal antes das 18h. Vale perguntar à turma ou à coordenação.
- **Chamada:** não há chamada gravada. O combinado da presença do Matheus (saída às 17h20, formatura) depende de alguém lembrar. Verificar se a presença dele foi lançada.
- **Salas do Teams:** erros ao entrar (João Pedro), aluno que precisou "bater ponto" no meio da aula, Tiago voltando atrasado do intervalo às 17h06. **Inferência:** parte da turma tem obrigação de ponto na Caixa durante o horário de aula.
- **Intervalo de ~20 min** (16h35–16h56).
- **Demo com percalços:** o debug não subiu e houve 401 por ordem das requisições. Antes de usar o projeto `api-alunos`, rodar a sequência do `aula03.http` (login → perfil → logout).
- O segredo do JWT está fixo no código (proposital, para a demo). Não reaproveitar em exemplos "de verdade".

### 8.3 Promessas feitas aos alunos (a cumprir ou a verificar)
1. **Subir o código** no repositório da turma: o da aula 1 e, pelo contexto, o da aula 3 [00:17:34], incluindo o `DesafioController` com solução [00:50:04]. Verificar se subiu.
2. **Tirar o requisito de Git do documento do projeto e atualizar o documento** [02:05:30].
3. **Próxima aula:** OAuth 2 "já com o JWT" [01:06:39] e implementação de JWT "mais elegante, com as funções do Spring" [00:54:03; 01:20:52]. O `JwtBiblioteca`/`JwtController` do projeto parecem ser esse material (inferência).
4. **Presença do Matheus** na chamada [01:35:04].
5. BCrypt "na aula 08" (comentário do código, não fala).

### 8.4 Clima
- Turma participativa e respeitosa; vários alunos com câmera ligada (Eldio, Vinicius, Benjamin, Matheus, Paulo, Caio, Hélio). Alunos trazem conhecimento prévio (Argon2id/BCrypt, Postman, APIs de chamados) e às vezes corrigem ou complementam o instrutor, que acolhe ("boa pedida", "excelente pergunta").
- O instrutor pede interação o tempo todo e usa analogias com a Caixa (anúncio de empréstimo, app com sessão de 30 min, crachá no prédio).
- Ele próprio sinaliza que o projeto é "muito grande, muito audacioso", o que sugere preocupação com o tamanho do escopo; os grupos tinham dúvidas sobre requisitos (Git).

### 8.5 Onde a aula parou exatamente
- **Conteúdo:** fechou cookies e os três tipos de sessão (com demo completa de `CookieController` e `SessaoController`) e deu a **parte conceitual do JWT** mais a geração manual (`JwtManual`: header e payload em Base64URL e HMAC-SHA256 à mão; decodificação no jwt.io com `iss=ada-seguranca`, `sub=ana`, `role=USER`, `exp` = `iat` + 300 s). Última fala de conteúdo: crachá e Basic Auth × OAuth 2 [01:32:52–01:34:13].
- **Não foi feito:** **validação** do JWT no servidor (o lado que recebe o token), `JwtController`, `JwtBiblioteca`, uso de biblioteca/Spring, expiração na prática, refresh e revogação, e o `DesafioController` (só apresentado).
- **Projeto:** grupos trabalhando nas salas desde 16h56; Git deixou de ser requisito.
- **Próximo passo combinado:** aula 04 (Sex 06/11) = OAuth 2.0 entrando pelo JWT com biblioteca/Spring.

---

## 9. Citações-chave

1. "Ficou faltando uma coisa importante... são dois assuntos que a gente deve ver hoje, que é o cookie e o JWT." [00:06:43–00:06:55]
2. "Se eu altero aqui 42 para 55, eu vou ser agora o usuário 55... isso não pode acontecer." [00:23:43–00:23:58]
3. "Me interrompam, abra o microfone e fala 'professor, Nicolau'... muitas vezes eu estou olhando aqui para a tela e não estou olhando para a tela do Teams." [00:26:56–00:27:07]
4. "Isso aqui não é uma API RESTful... nós temos estado." [00:49:04–00:49:35]
5. "Apagar cookie não apaga a sessão... Nesse contexto, roubar cookie é roubar a sessão." [01:00:15; 01:00:58]
6. "Aqui não existe criptografia. Aqui existe o carimbo e uma assinatura." [01:07:30–01:07:35]
7. "Se o seu segredo está público, a sua aplicação está toda exposta." [01:10:55–01:10:58]
8. "Base64 não é criptografia, guardem isso." [01:16:15]
9. "O JWT é um crachá. Crachá não garante que a pessoa é a pessoa." [01:28:19–01:28:20]
10. "O projeto é muito grande, muito audacioso... Não precisa estar 100% para apresentar." [01:56:48–01:57:10]
