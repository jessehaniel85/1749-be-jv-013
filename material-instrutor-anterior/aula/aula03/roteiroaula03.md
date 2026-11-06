# Aula 03: Json Web Token (JWT)

**Roteiro de Aula · Módulo Fundamentos de segurança para aplicações (BE-JV-013)**

Uma aula conduzida por **perguntas**, com **mão na massa** em cada parte. Você fala pouco: pergunta, espera, mostra no jwt.io e na API rodando, e a turma explica de volta. Ao final, todos sabem o que é um JWT, por que ele não pode ser falsificado sem a chave e o que acontece quando ele é roubado ou a chave é fraca.

**Data:** 04/11/2026 · quarta &nbsp;·&nbsp; **Duração:** 3h &nbsp;·&nbsp; **Formato:** perguntas + prática guiada + dinâmicas + desafio

**Código:** [`source/`](source/README.md) · **Ponto de partida:** estado final da Aula 02 (`../aula02/source`) · **Próxima aula com código:** Aula 07, que começa do estado final deste projeto (as aulas 04 a 06 não têm código).

> **Problema gerador:** Json Web Token (JWT): como autenticar usuários em uma API sem guardar sessão no servidor?

---

## Sumário

1. [Como usar este roteiro](#01--como-usar-este-roteiro)
2. [Antes da aula](#02--antes-da-aula)
3. [Visão geral](#03--visão-geral)
4. [Aquecimento](#04--aquecimento)
5. [Bloco 1: como o servidor sabe quem sou eu?](#05--bloco-1-como-o-servidor-sabe-quem-sou-eu)
6. [Bloco 2: e se o servidor não guardasse nada?](#06--bloco-2-e-se-o-servidor-não-guardasse-nada)
7. [Bloco 3: o que é um JWT?](#07--bloco-3-o-que-é-um-jwt)
8. [Bloco 4: dá para falsificar?](#08--bloco-4-dá-para-falsificar)
9. [Bloco 5: e se roubarem o token?](#09--bloco-5-e-se-roubarem-o-token)
10. [Bloco 6: quem guarda a chave?](#10--bloco-6-quem-guarda-a-chave)
11. [Encerramento](#11--encerramento)
12. [Dicas de condução](#12--dicas-de-condução)

---

## 01 · Como usar este roteiro

Todo bloco segue a mesma ordem. Mantenha-a: é ela que faz a aula ser da turma, e não sua.

| Etapa | O que é | Regra |
|---|---|---|
| **Pergunte** | Uma pergunta aberta **antes** de qualquer explicação. | Depois de perguntar, **conte até 5 em silêncio**. Quem responde primeiro costuma ser sempre o mesmo: peça a resposta de uma dupla diferente. |
| **Mão na massa** | Um guia passo a passo do que você abre, clica ou digita, **com a turma acompanhando**. | Cada passo termina com uma pergunta: "o que vocês esperam que aconteça?" **antes** de apertar o botão. Prever e depois ver é o que fixa. |
| **Cheque** | Duas ou três perguntas para ver se a turma entendeu, com a resposta esperada. | Peça que **um aluno explique para o colega**. Se ele explicar, entendeu. |
| **Dinâmica** | Uma atividade em grupo, de 5 a 8 minutos. | A turma se levanta, vota ou disputa. |

**Cartões de votação:** entregue a cada aluno três papéis, **A**, **B** e **C** (ou peça que levantem 1, 2 ou 3 dedos). Em toda votação, peça que **escolham sem olhar o colega** e depois mostrem ao mesmo tempo.

**As ferramentas da aula:**

- **jwt.io**, no navegador: é onde se "abre" um JWT.
- A **API da aula** rodando na sua máquina: [`source/`](source/README.md).
- O **arquivo de requisições** [`source/http/aula03.http`](source/http/aula03.http), executado no IntelliJ (cada requisição citada abaixo tem um número, como "2.1"). Se preferir, os comandos `curl` equivalentes estão no README de `source/`.
- O **console do navegador** (`F12`) e um **terminal**.

## 02 · Antes da aula

Faça com 30 minutos de antecedência. O que mais atrasa uma aula como esta é a preparação feita na hora.

- [ ] Suba a API (`mvn spring-boot:run`, em `source/`) e confirme que abre `http://localhost:8080/jwt/desafio/token`.
- [ ] Abra o `aula03.http` no IntelliJ, com o ambiente **dev** selecionado, e rode a requisição **2.1**. Se funcionar, está tudo certo.
- [ ] Deixe abertas, em abas: **jwt.io**, a pasta do projeto e o terminal.
- [ ] **Imprima** (ou projete) os **três tokens da caça aos claims** (Bloco 3), gerados no dia da aula com a requisição 3.2:
  - **T1:** usuário `ana`, perfil `USER`, validade de 24 h (`expiraEmSegundos: 86400`).
  - **T2:** usuário `admin`, perfil `ADMIN`, validade de 24 h.
  - **T3:** usuário `bia`, perfil `USER`, já **expirado** (`expiraEmSegundos: -10`).
  
  Um token é só uma linha de texto: prefira projetar em tela e mandar por chat/QR code para a turma, em vez de ditar.
- [ ] Deixe à mão o **JWT de exemplo** do Bloco 3 (seção "Mão na massa 3b"): ele é fixo, está escrito no próprio roteiro e não depende da API rodando. Teste uma vez o `openssl` e o `base64` no seu terminal.
- [ ] **Não abra** o `DesafioController.java` na tela: a chave do desafio está nele.
- [ ] Separe papel e caneta para a dinâmica dos "servidores" (Bloco 1) e os cartões A/B/C.
- [ ] Teste o **plano B**: se o jwt.io não abrir, a requisição **3.1** e o comando `base64 -d` fazem o mesmo trabalho.

## 03 · Visão geral

Tempos relativos ao início da aula, ajuste ao seu horário real. Se atrasar, corte a dinâmica do Bloco 5 e encurte o Bloco 1; **não corte** o Bloco 4 nem o desafio do Bloco 6, são os que a turma mais lembra.

| Hora | Duração | Bloco |
|---|---|---|
| 00:00 | 10 min | **Aquecimento**: quiz-relâmpago da aula 02, com votação |
| 00:10 | 20 min | **Bloco 1:** como o servidor sabe quem sou eu? (sessão) |
| 00:30 | 20 min | **Bloco 2:** e se o servidor não guardasse nada? (token) |
| 00:50 | 30 min | **Bloco 3:** o que é um JWT? (explicação, jwt.io, Base64 à mão e caça aos claims) |
| 01:20 | 10 min | Intervalo |
| 01:30 | 30 min | **Bloco 4:** dá para falsificar? (assinatura, Time Vermelho x Time Azul) |
| 02:00 | 15 min | **Bloco 5:** e se roubarem o token? |
| 02:15 | 25 min | **Bloco 6:** quem guarda a chave? (desafio da chave fraca) |
| 02:40 | 20 min | **Encerramento:** exercícios, roda de explicação, atividade de casa |

## 04 · Aquecimento

*~10 min*

**Pergunte (quiz-relâmpago, com cartões A/B/C):**

1. "Qual atributo de cookie impede que um **script** leia o cookie? **A** `Secure` · **B** `HttpOnly` · **C** `SameSite`." → **B** (e liga com o XSS da aula 01).
2. "O cadeado do navegador garante que o site é **honesto**? **A** sim · **B** não." → **B** (garante a conexão e o domínio, não a honestidade).
3. "Quem **decifra** o que foi cifrado com a chave **pública**? **A** a própria pública · **B** a privada · **C** qualquer um." → **B**.
4. **A pergunta-ponte:** "Aquele cookie `user-id` da aula passada guardava o quê? Um ID. Mas e o **JSESSIONID** que todo site Java usa: o que será que o servidor faz com ele?" Deixe sem resposta. É o gancho do próximo bloco.

**Cheque:** em cada pergunta errada, peça a **quem acertou** que explique a quem errou, em uma frase. Não explique você.

## 05 · Bloco 1: como o servidor sabe quem sou eu?

*~20 min*

### Pergunte

> "Vocês acabaram de fazer login em um site. Na **próxima** requisição, como o servidor se lembra de que são vocês? O HTTP **não tem memória**: cada requisição chega como se fosse a primeira."

Em **duplas**, 2 minutos. Anote as ideias no quadro. O esperado: "um cookie", "um ID", "o servidor guarda". Se ninguém falar em **o servidor guardar**, pergunte: "e onde fica a informação de que a Ana está logada?"

### Mão na massa 1: a sessão

Objetivo: a turma **vê** que o servidor guarda quem está logado, e o que acontece quando ele perde essa informação.

1. **Preveja:** "Vou fazer login por **sessão**. O que vocês esperam encontrar na resposta?" Rode a requisição **1.1** (`POST /sessao/login`).
2. **Olhe a resposta com a turma:** o cabeçalho **`Set-Cookie: JSESSIONID=...`** (você viu `Set-Cookie` na aula 02). Pergunte: "esse cookie contém a Ana? Contém o nome dela?" → **Não, só um número.**
3. Rode a **1.2** (`GET /sessao/perfil`): vem `ana`. Pergunte: "quem lembrou que eu sou a Ana: o **cookie** ou o **servidor**?" → o servidor. O cookie só trouxe o ID para ele achar a ficha.
4. **No navegador, para ver o cookie de verdade.** Abra qualquer página de `http://localhost:8080` (por exemplo `/busca?q=x`), abra o console (`F12`) e cole:

   ```js
   fetch('/sessao/login', {method: 'POST', headers: {'Content-Type': 'application/json'},
     body: JSON.stringify({username: 'ana', password: 'senha123'})}).then(r => r.json()).then(console.log)
   ```

   Abra `http://localhost:8080/sessao/perfil` em outra aba: aparece `ana`. Vá em **Application > Cookies**: o `JSESSIONID` está lá, **`HttpOnly`** marcado (aula 02). Pergunte: "e se eu **apagar** esse cookie?" Apague e recarregue o `/sessao/perfil`: **401**.
5. **O golpe final:** reinicie a API (pare e rode de novo no IntelliJ). Recarregue o perfil, com o cookie ainda no navegador: **401**. Pergunte: "o cookie estava lá. Por que não funcionou?" → **a sessão estava na memória do servidor, e ele esqueceu.**

### Dinâmica: "os servidores" (~5 min)

Escolha **3 alunos** para serem os **servidores A, B e C**, cada um com uma folha (a "memória" do servidor). Uma aluna, **Lia**, é o cliente. Você é o **balanceador de carga**, que decide para qual servidor cada requisição vai.

1. Lia "faz login" no **servidor A**. O A anota na sua folha: *"ID 481 = Lia, logada"* e entrega a ela um papel com **481**.
2. Lia pede "meu perfil" e você a manda para o **A**. O A olha a folha, acha o 481: "Olá, Lia." ✔
3. Agora você manda a próxima requisição da Lia para o **servidor B**. O B procura o 481 na folha dele: **não existe**. "Quem é você? Faça login." ✘
4. **Pergunte à turma:** "Vocês têm **3 servidores** atrás de um balanceador. O que fazer?" Deixe propor. O esperado:
   - **Grudar** o usuário em um servidor (*sticky session*): se aquele cair, perde tudo.
   - **Compartilhar** as sessões em um banco ou em um serviço de cache: funciona, mas **cada requisição** vira uma consulta a mais, e é mais uma peça para manter.
   - **Não guardar nada:** é a pergunta do próximo bloco.

### Cheque

- "Cite **dois** problemas de guardar a sessão no servidor." (Vários servidores; consulta a cada requisição; perde tudo se reiniciar.)
- "O cookie `JSESSIONID` é **seguro**? Alguém roubar o cookie, o que consegue?" (Se roubar o ID, **vira** o usuário. Liga com XSS, CSRF e `HttpOnly`.)

## 06 · Bloco 2: e se o servidor não guardasse nada?

*~20 min*

### Pergunte

> "Imaginem que o servidor **não** guardasse **nenhuma** ficha. Então, **em toda requisição**, o cliente teria que trazer, dentro dela, a prova de quem é. **O que ele traria?**"

Em duplas, 1 minuto. Respostas típicas: "o nome", "o ID do usuário", "a senha". Conduza:

- "Mandar a **senha** a cada requisição?" → inseguro e caro (o servidor teria que conferir a senha toda hora).
- "Mandar o **nome** e o **perfil**: `usuario=ana, perfil=USER`?" Faça a pergunta que abre o próximo bloco: **"E se eu escrever `perfil=ADMIN` à mão?"** Deixe a turma perceber o problema: *quem garante que não foi alterado?*

> **Resposta que vocês vão construir nos próximos blocos:** um "crachá" com os dados da pessoa e uma **assinatura** que só o servidor sabe fazer. Isso é o **token**.

### Mão na massa 2: o token

1. **Preveja:** "Vou fazer login por **token**. O que muda na resposta em relação à sessão?" Rode a **2.1** (`POST /jwt/login`).
2. **Mostre:** não veio cookie nenhum. Veio um **texto grande** em `token`. Pergunte: "isso é o quê?" (Deixe para o Bloco 3.)
3. Rode a **2.2** (`GET /jwt/protegido`, com `Authorization: Bearer <token>`): `Olá, ana! Você está autenticada por token.`
4. Rode a **2.3**: o mesmo, com o cabeçalho `access_token` (é o cabeçalho que o enunciado do **projeto** pede).
5. Rode a **2.4**, **sem** token: **401**. Olhe a resposta: ela **não diz o motivo**. Pergunte: "por que o servidor não explica por que recusou?" (Para não ajudar quem está atacando.)
6. **A prova do "stateless":** reinicie a API de novo. Rode a **2.2** **com o mesmo token de antes**. **Continua funcionando.** Pergunte: "na sessão, reiniciar derrubou tudo. Aqui não derrubou. **O que o servidor guardou?**" → **Nada.** Tudo estava no token.

### Dinâmica: "o crachá" (~3 min)

Volte aos três servidores. Dessa vez a Lia faz login no A e recebe um **crachá** (papel com "Lia, USER, até 15h" e um **carimbo** que só o A sabe fazer, mas que **todos conseguem conferir**). Você manda a requisição ao **B** e ao **C**: ambos olham o crachá, conferem o carimbo e aceitam. **Sem folha, sem consulta.**

### Cheque: construam a tabela no quadro

Peça que a turma preencha, e **você só escreve o que eles disserem**:

| | **Sessão** | **Token** |
|---|---|---|
| Onde fica a informação de quem está logado? | no servidor | dentro do token, com o cliente |
| Vários servidores? | difícil (compartilhar sessões) | fácil (qualquer um confere) |
| Reiniciar o servidor? | todos deslogados | nada muda |
| Quero **deslogar um usuário agora**? | fácil (apago a sessão) | **difícil** (o token vale até vencer) |

> **A resposta da última linha é o "preço" do token** e volta no Bloco 5. Se a turma não a encontrar sozinha, pergunte: "como eu faço para o servidor **esquecer** um crachá que ele não guardou?"

## 07 · Bloco 3: o que é um JWT?

*~30 min (2 pergunte · 4 explique · 8 jwt.io · 6 Base64 à mão · 6 caça aos claims · 4 cheque)*

### Pergunte

Projete o token da requisição 2.1 e **não explique nada**.

> "Olhem este texto. **Sem eu dizer nada:** o que vocês acham que tem dentro? E uma votação: ele está **criptografado**? **A** sim · **B** não."

Anote quantos votaram em cada. Guarde o placar: você vai **voltar a ele** no fim do bloco.

### Explique: o que é um JWT (~4 min)

Só **depois** dos palpites. Desenhe no quadro o token em três pedaços:

```
eyJhbGciOiJIUzI1NiIs...  .  eyJpc3MiOiJhZGEtc2Vn...  .  anwUMCgZBQ1tjo8c...
        HEADER                      PAYLOAD                    SIGNATURE
   (como foi assinado)        (os dados: as "claims")        (o carimbo)
```

**Em uma frase:** JWT (*JSON Web Token*, pronuncia-se "jót") é um **padrão aberto** (RFC 7519) para um texto compacto que carrega **dados** (as *claims*) e uma **assinatura** que prova que esses dados não foram alterados. É o "crachá com carimbo" da dinâmica do Bloco 2.

| Parte | O que guarda | Exemplo (decodificado) |
|---|---|---|
| **Header** | O **algoritmo** da assinatura (`alg`) e o tipo do token (`typ`). | `{"alg":"HS256","typ":"JWT"}` |
| **Payload** | As **claims**: os dados sobre o usuário e sobre o próprio token. | `{"sub":"ana","role":"USER","exp":2099952000}` |
| **Signature** | O **carimbo**: um HMAC do `header.payload`, calculado com a **chave** do servidor. | 32 bytes, escritos em Base64URL |

**As claims que vamos ver** (as quatro primeiras são padronizadas; `role` é nossa):

| Claim | Significa | Em português |
|---|---|---|
| `iss` | *issuer* | quem **emitiu** o token |
| `sub` | *subject* | **de quem** é o token (o usuário) |
| `iat` | *issued at* | **quando nasceu** (segundos desde 01/01/1970) |
| `exp` | *expiration* | **quando vence** (mesmo formato) |
| `role` | (nossa) | o **perfil**: `USER`, `ADMIN` |

**Como cada parte é escrita:** cada pedaço é convertido para **Base64URL** e os três são separados por ponto. Base64 é só uma forma de escrever bytes usando 64 letras e símbolos, para o texto viajar em cabeçalhos HTTP sem quebrar. A versão "URL" troca `+` e `/` por `-` e `_` e tira o `=` do final. **Não existe segredo nisso**, qualquer um desfaz: é o que vamos provar agora.

**Como a assinatura funciona:** o servidor pega `header.payload`, calcula um **HMAC-SHA256** com a chave e escreve o resultado no fim. Para conferir, ele repete a conta com a chave dele e compara. Mudou **uma letra** do header ou do payload? A conta dá outro resultado e o token é recusado. Sem a chave, ninguém consegue produzir a assinatura certa.

**Para que serve:** autenticar numa API **sem guardar sessão** (o que estamos construindo) e **passar informação entre sistemas**. Nas próximas aulas ele reaparece: no **OAuth** o *access token* costuma ser um JWT, e no **OpenID Connect** o *ID token* **é** um JWT.

**O que o JWT *não* é:**

- **Não é criptografia.** O conteúdo é visível (o próximo passo prova isso).
- **Não é uma sessão.** O servidor não guarda nada; quem guarda o token é o cliente.
- **Não é uma "senha".** Quem tiver o token **age como o usuário** até ele vencer (Bloco 5).

**Três frases para o quadro** (a turma copia):

1. JWT é um **crachá com carimbo**: dados + assinatura.
2. **Assinado, não escondido.**
3. Qualquer servidor que tenha a chave **confere o carimbo sem consultar ninguém**.

> Existem dois tipos: o **JWS** (assinado, o que usamos) e o **JWE** (criptografado, raro). Quando alguém disser só "JWT", quase sempre é o JWS.

### Mão na massa 3: abrindo um JWT no jwt.io

1. Abra **jwt.io**, cole o token no campo da esquerda (*Encoded*). Projete.
2. **Antes de olhar a direita**, pergunte: "o texto tem **três** pedaços separados por ponto, e o site pintou cada um de uma cor. **O que vocês acham que cada parte guarda?**" Deixe 3 palpites.
3. **Revele a direita** (*Decoded*) e confirme com a turma:
   - **Header** (vermelho): `alg` e `typ`. O **algoritmo** que assina.
   - **Payload** (roxo): os dados, as **claims**: `sub` (quem), `role` (perfil), `iss` (quem emitiu), `iat` e `exp` (quando nasceu e quando vence).
   - **Signature** (azul): a assinatura.
4. **Peça a um voluntário** que leia em voz alta: "quem é o usuário? qual o perfil? quando expira?" O `exp` vem como um número (segundos desde 1970). Converta no console do navegador: `new Date(EXP * 1000)`.
5. **A prova de que não é criptografia.** Em um terminal, **sem nenhuma chave**:

   ```bash
   echo 'COLE_AQUI_A_SEGUNDA_PARTE_DO_TOKEN' | base64 -d
   ```

   (Se o terminal reclamar de preenchimento, acrescente `==` ao final da parte colada.) Aparece o JSON. Mostre também a requisição **3.1** (`POST /jwt/decodificar`), que faz o mesmo no servidor. Volte ao placar: **"quem votou *sim*, criptografado?"** Deixe a turma se corrigir.

> **A frase da aula:** o JWT é **assinado, não escondido**. Qualquer pessoa com o token lê o conteúdo. Quem garante a segurança é a **assinatura**, que vem no próximo bloco.

### Mão na massa 3b: um JWT de exemplo, no jwt.io e no Base64 (~6 min)

Este é um token **fixo**, para você usar sempre igual (com a API desligada, sem internet além do jwt.io, e para a turma repetir em casa). Ele foi assinado com a chave **`segredo-da-aula`**. É só um exemplo de estudo: **a API da aula usa outra chave**, então a API não o aceita.

**O token** (uma linha só):

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJhZGEtc2VndXJhbmNhIiwic3ViIjoiYW5hIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3OTM3OTM2MDAsImV4cCI6MjA5OTk1MjAwMH0.anwUMCgZBQ1tjo8cNjRTAkW1_D6Ah6QahU5GA9pCSKs
```

**As três partes, separadas:**

| Parte | Base64URL | Decodificada |
|---|---|---|
| Header | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9` | `{"alg":"HS256","typ":"JWT"}` |
| Payload | `eyJpc3MiOiJhZGEtc2VndXJhbmNhIiwic3ViIjoiYW5hIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3OTM3OTM2MDAsImV4cCI6MjA5OTk1MjAwMH0` | `{"iss":"ada-seguranca","sub":"ana","role":"USER","iat":1793793600,"exp":2099952000}` |
| Signature | `anwUMCgZBQ1tjo8cNjRTAkW1_D6Ah6QahU5GA9pCSKs` | 32 bytes binários (não é texto) |

Datas: `iat` = 04/11/2026 12:00 (UTC) e `exp` = 18/07/2036. Confirme no console do navegador com `new Date(2099952000 * 1000)`.

**Passo 1. No jwt.io.** Cole o token no campo *Encoded*. Aparecem header e payload, e a assinatura aparece como **não verificada**, porque o site ainda não tem a chave. Pergunte: "por que não pôde verificar?" → falta a chave. Digite `segredo-da-aula` no campo da chave (*Verify Signature*): a tela passa a dizer **Signature Verified**. *(Confira antes da aula como o site mostra isso hoje; a tela muda de tempos em tempos.)*

**Passo 2. Desfaça o Base64, sem chave.** No terminal (Git Bash, Linux ou macOS), um pedaço de cada vez:

```bash
echo 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9' | base64 -d
echo 'eyJpc3MiOiJhZGEtc2VndXJhbmNhIiwic3ViIjoiYW5hIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3OTM3OTM2MDAsImV4cCI6MjA5OTk1MjAwMH0=' | base64 -d
```

Repare no `=` que **acrescentamos** ao fim do payload: o Base64URL do JWT não tem o preenchimento, e o `base64 -d` exige. Sem ele o JSON aparece e logo depois o terminal reclama de "invalid input". No PowerShell: `[Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('...='))`.

Agora tente a **terceira** parte do mesmo jeito: dá erro ou lixo. Pergunte: "por quê?" → não é texto, é uma assinatura binária.

**Passo 3. Faça o caminho de volta (o servidor faz isto).** Escreva o JSON, converta para Base64URL e assine. Cole tudo de uma vez no Git Bash:

```bash
SEGREDO='segredo-da-aula'
b64url() { base64 -w0 | tr '+/' '-_' | tr -d '='; }

HEADER=$(printf '%s' '{"alg":"HS256","typ":"JWT"}' | b64url)
PAYLOAD=$(printf '%s' '{"iss":"ada-seguranca","sub":"ana","role":"USER","iat":1793793600,"exp":2099952000}' | b64url)
ASSINATURA=$(printf '%s' "$HEADER.$PAYLOAD" | openssl dgst -sha256 -hmac "$SEGREDO" -binary | b64url)

echo "$HEADER.$PAYLOAD.$ASSINATURA"
```

O resultado é **exatamente** o token de cima. Mostre o que cada linha faz: `b64url` troca `+/` por `-_` e remove o `=`; a assinatura é o HMAC-SHA256 de `header.payload`. É o mesmo que o `JwtManual.gerar` faz em Java. *(No macOS, o `base64` não tem `-w0`: use `base64 | tr -d '\n' | tr '+/' '-_' | tr -d '='`.)*

**Passo 4. Quebre de propósito.** Preveja primeiro: "se eu trocar `USER` por `ADMIN` no JSON do payload e rodar de novo, **o que muda** no token?" Troque, rode e compare:

- o **header** continua igual;
- o **payload** muda;
- a **assinatura muda por inteiro**, mesmo para uma mudança pequena.

Pergunta final: "e se eu levar o payload novo **com a assinatura antiga**?" → o servidor refaz a conta, o resultado não bate e o token é recusado. É o Bloco 4.

### Dinâmica: caça aos claims (~6 min)

Em **duplas**, cada uma recebe **um** dos três tokens da preparação (T1, T2 ou T3) e abre no jwt.io. Elas têm 3 minutos para descobrir:

1. **Quem** é o usuário?
2. **Qual** o perfil?
3. O token **ainda vale**? (comparar `exp` com a data de hoje)
4. Esse token consegue entrar em `GET /jwt/admin`?

Depois, **cada dupla apresenta** o seu em 20 segundos e você confirma com a requisição **2.5/2.6** ou com a **4.1**. Respostas: **T1** `ana`, `USER`, vale, **não** entra no admin (403). **T2** `admin`, `ADMIN`, vale, entra. **T3** `bia`, `USER`, **expirado**, não entra em nada (401).

### Cheque

- "Posso colocar a **senha** do usuário no payload?" (**Não**: qualquer um lê.)
- "Se o conteúdo não é secreto, **o que impede** alguém de criar um token dizendo que é admin?" (Deixe sem resposta: é o gancho do Bloco 4.)
- "Existe JWT **criptografado**?" (Sim, o **JWE**, mas o mais usado é o assinado, o **JWS**, que é o que vamos usar.)

## 08 · Bloco 4: dá para falsificar?

*~30 min*

### Pergunte

Com o token da Ana na tela do jwt.io:

> "Vou trocar `USER` por `ADMIN` **no payload**. Eu viro admin? **A** sim · **B** não · **C** depende." Votação com cartões. Anote o placar.

### Mão na massa 4: tentar forjar um token

**Passo 1. O token verdadeiro é aceito.** Rode a **4.1** (`POST /jwt/validar`): `valido: true`, `motivo: "token válido"`.

**Passo 2. Forjar sem a chave.**
1. No jwt.io, com o token da Ana, vá ao campo da chave (*Verify Signature*, embaixo) e digite uma chave **errada**, por exemplo `123`.
2. No *Decoded*, mude `"role": "USER"` para `"ADMIN"`. Repare: o texto da **esquerda** mudou, **inclusive a assinatura** (o site refez a assinatura com a chave `123`).
3. Copie o **novo** token. Rode a **4.4** (cole o token) ou a **2.5** com ele: o servidor responde **assinatura inválida**. Veja o **log do servidor** (console do IntelliJ): `Token recusado: assinatura inválida...`
4. **Volte ao placar e à pergunta:** "eu virei admin?" Deixe a turma explicar **por quê**: o servidor refaz a assinatura **com a chave dele** e compara; a minha foi feita com `123`.

**Passo 3. E se eu tivesse a chave?** Diga a chave de demonstração (ela está no `application.yml`) e digite-a no jwt.io. Agora o token com `ADMIN` é **aceito** em `/jwt/admin`. Pergunte: **"então o que protege o sistema?"** → **a chave.** Quem tem a chave **emite** qualquer token. Guarde essa frase: é o Bloco 6.

**Passo 4. Como o servidor confere.** Abra `JwtManual.java` (método `validar`) e mostre as **três** verificações, em ordem:
1. **O algoritmo é o que o servidor espera?** (nunca o que o token diz)
2. **A assinatura bate?** (recalcula `HMAC-SHA256(header.payload, chave)` e compara)
3. **As claims estão certas?** (emissor e expiração)

   Para fechar, mostre que o **JWT é um padrão**, e não uma invenção da nossa API: rode a **4.5** (gera um token com a **biblioteca** java-jwt, a que usaremos na aula 08) e a **4.6** (a validação feita **à mão** o aceita). Pergunte: "por que um token de uma biblioteca funciona em outro código?" (Porque o formato e a assinatura são os mesmos, definidos pelo padrão.)

### Dinâmica: Time Vermelho x Time Azul (~12 min)

Divida a turma em dois times.

- **Time Vermelho (atacantes):** tem 8 minutos para conseguir acesso a `GET /jwt/admin` **sendo a Ana**. Podem usar o jwt.io e o `aula03.http`. Ideias para dar se travarem: mudar o `role`; mudar o `exp` para daqui a um ano; usar o token **sem assinatura** (a requisição **4.2**, que troca o `alg` por `none`); assinar com outra chave.
- **Time Azul (defensores):** para **cada tentativa** do Vermelho, aponta **qual linha** de `JwtManual.validar` a barrou e explica com as próprias palavras.

Pontuação: 1 ponto para o Vermelho por tentativa **diferente** (mesmo que falhe); 1 ponto para o Azul por explicação **correta**. Ao final, **ninguém vence o servidor**, e é essa a conclusão.

### Cheque

- "Por que o servidor **ignora** o `alg` que vem no token?" (Se confiasse, o atacante diria `none` e o token passaria sem assinatura.)
- "Por que a rota `/jwt/protegido` **não explica** o motivo da recusa, e a `/jwt/validar` explica?" (Explicar ajuda o atacante; a `/validar` existe só para a aula.)
- "Posso **mudar o `exp`** para mais longe?" (Sim, mas a assinatura deixa de bater: qualquer alteração a invalida.)

## 09 · Bloco 5: e se roubarem o token?

*~15 min*

### Pergunte

> "Alguém **copiou** o token da Ana (viu na tela, achou em um log, um script malicioso roubou). **O que essa pessoa consegue fazer? E por quanto tempo?**"

Debate de 2 minutos. O esperado: "tudo que a Ana faz", "até o token vencer".

### Mão na massa 5: usar o token de outra pessoa

1. Copie o token da Ana e, em um **terminal** (outra janela, simulando "outra máquina"), rode:

   ```bash
   curl -H "Authorization: Bearer COLE_O_TOKEN" http://localhost:8080/jwt/protegido
   ```

   **Funciona.** Pergunte: "o servidor sabe que não sou a Ana?" → **Não.** Quem tem o token **é** a Ana.
2. **O token curto.** Rode a **5.1** (gera um token que dura **20 segundos**) e **preveja**: "quem acha que ele vai funcionar **daqui a 30 segundos**? **A** sim · **B** não". Rode a **5.2** agora (200). Espere 30 s, rode de novo: **401**. Olhe o **log do servidor**: `Token recusado: token expirado`.
3. Pergunte: "e se o token durasse **um ano**?" (O estrago de um roubo duraria um ano.)

### Dinâmica: "onde guardar o token no navegador?" (~6 min)

Marque **quatro cantos** da sala: **(1) `localStorage`**, **(2) cookie `HttpOnly`**, **(3) memória do JavaScript**, **(4) cookie comum**. Cada aluno vai para o canto que **escolheria** para guardar o token. Em cada canto, um aluno **defende** a escolha. Depois você revela o preço de cada um (liga com as aulas 01 e 02):

| Onde | Ponto fraco |
|---|---|
| `localStorage` | Um **XSS** lê o token (o script malicioso acessa o `localStorage`). |
| Cookie comum | JavaScript lê (XSS) **e** o navegador envia sozinho (CSRF). |
| Cookie `HttpOnly` | JavaScript não lê, mas volta a exigir proteção contra **CSRF** (`SameSite`). |
| Memória do JavaScript | Some ao recarregar a página; ainda exposta a XSS enquanto a página vive. |

> **Não existe opção sem custo.** O ponto da dinâmica é a turma **raciocinar com o que já aprendeu**, e não decorar a tabela.

### Cheque: como limitar o estrago?

Peça que a turma **liste**, e você completa: **expiração curta** · **HTTPS sempre** (aula 02) · **refresh token** para renovar sem novo login (o refresh pode ser revogado no servidor) · **lista de revogação** para casos urgentes (e aí o servidor volta a guardar um pouco de estado: relacione com a última linha da tabela do Bloco 2).

## 10 · Bloco 6: quem guarda a chave?

*~25 min*

### Pergunte

> "Tudo o que vimos hoje depende de **uma** coisa: a chave que assina. O que acontece se essa chave for uma palavra fácil, como `ada123`?"

Deixe a turma responder. Depois: **"vamos descobrir."**

### Mão na massa 6: o desafio da chave fraca (~15 min)

Esta é a atividade que a turma mais lembra. Diga que a missão é **tornar-se ADMIN em uma API que usa uma chave fraca**. **Ninguém abre o código.**

1. Peça que todos rodem a **6.1** (`GET /jwt/desafio/token`) e copiem o `token`. Depois a **6.2** (`/jwt/desafio/admin`) com esse token: **403** (é USER).
2. **Passo 1: descobrir a chave.** No jwt.io, cole o token. No campo da chave (*Verify Signature*), **tentem palavras fáceis** até aparecer **Signature Verified**. Dê 3 minutos. Se a turma travar, escreva no quadro um **ranking de chaves comuns**: `secret`, `123456`, `password`, `admin`, `ada`, `ada123`...
3. **Passo 2: forjar.** Com a chave certa digitada, mude `"role"` para `"ADMIN"`. O jwt.io refaz a assinatura com a chave correta. Copie o token.
4. **Passo 3: entrar.** Rode a **6.3** com o token forjado: **"Parabéns... a chave era fraca."**
5. **O contraste:** "agora tentem o mesmo com o token do **login** (requisição 2.1)". **Ninguém** consegue adivinhar a chave dele, que é longa e aleatória... mas ela está no código? Abra o `application.yml` e pergunte: **"o que está errado neste arquivo?"** → **a chave está dentro do repositório.** Vale como exemplo do que **não** fazer: em sistema real, a chave vem de **variável de ambiente** ou de um **cofre de segredos**, nunca do código. Na aula 08, a chave será lida de `${JWT_SECRET}`, **sem valor padrão**.

> **Para conversar depois:** quem ataca de verdade não digita palavras uma a uma: usa programas que testam **milhões** de combinações por segundo. Por isso a chave precisa ser **longa e aleatória** (para HS256, no mínimo 256 bits), e não uma palavra.

### Pergunte: HS256 ou RS256? (~5 min)

> "Imaginem **10 microsserviços** que precisam **validar** tokens emitidos por um serviço de login. Com o que vimos (HS256, **a mesma chave** assina e valida), **todos** teriam que conhecer a chave secreta. **Isso é bom?**"

Deixe a turma perceber o risco (10 lugares com o segredo). Apresente a saída, em duas linhas no quadro:

| | **HS256** (chave secreta) | **RS256** (par de chaves) |
|---|---|---|
| Quem assina? | quem tem a chave | quem tem a **privada** |
| Quem valida? | quem tem a **mesma** chave | **qualquer um**, com a **pública** |
| Ideal para | uma aplicação só | vários serviços validando o token de um emissor |

(Liga com a **assinatura digital** da aula 02: a privada assina, a pública verifica.)

### Mão na massa 7 (opcional, 3 min): trocar a chave

Reinicie a API com outra chave (`JWT_DEMO_SECRET=outra-chave...`). Rode a **2.2** com o token **antigo**: **401**. Pergunte: "trocar a chave **invalida todos os tokens**. Como uma empresa troca a chave sem derrubar todo mundo?" (**Rotação de chaves**: o header traz um `kid`, que indica qual chave foi usada, e o servidor aceita as duas por um tempo.)

## 11 · Encerramento

*~20 min*

### Lista de exercícios (~10 min)

Lista de exercícios de múltipla escolha sobre **JWT**. Dê o tempo para a turma começar e deixe o que sobrar como atividade de casa.

### Roda de explicação (~10 min)

Em **duplas**, cada aluno explica ao colega, em **30 segundos**, **sem olhar nada**: "o que é um JWT, por que ele não pode ser falsificado sem a chave e o que acontece se for roubado". Sorteie 3 duplas para repetir para a turma. Use as perguntas abaixo para **fechar lacunas**:

- Sessão x token: onde fica a informação?
- As **três partes** do JWT e o que cada uma guarda.
- Por que o payload **não** guarda segredo.
- O que a **assinatura** garante.
- Token roubado: o que o atacante consegue e como limitar.
- Onde a chave **não** pode ficar.

**Amarre com o projeto:** "O JWT é o formato do token que vocês vão emitir no `POST /authenticate`. Na aula 08, a validação que vimos hoje à mão passa a ser feita pelo Spring Security, mas é **exatamente** esta".

**Atividade para casa:**
- **Leitura:** a introdução ao JWT: https://jwt.io/introduction
- **Leitura:** o artigo da Okta "What happens if your JWT is stolen?": https://developer.okta.com/blog/2018/06/20/what-happens-if-your-jwt-is-stolen
- **Pesquisa individual:** o que são os claims registrados de um JWT (`iss`, `sub`, `aud`, `exp`, `iat`) e para que serve cada um.
- **Desafio opcional:** ler `JwtManual.java` e descobrir **onde** o código poderia ser atacado se faltasse cada uma das três verificações do método `validar`.
- **Exercícios de fixação:** o que sobrou da lista sobre JWT.

**Preview da Aula 04:** OAuth 2.0: como um aplicativo acessa seus dados em outro serviço **sem** que você entregue a sua senha a ele. Na prática, vamos ver um projeto Spring Security com **access token e refresh token**.

## 12 · Dicas de condução

- **Fale menos do que parece necessário.** Se você explicou por mais de 2 minutos sem fazer uma pergunta ou abrir uma ferramenta, pare e pergunte algo.
- **Silêncio é ferramenta.** Depois de perguntar, conte até 5. Se ninguém responde, peça que **conversem em duplas** e só então chame alguém.
- **Preveja antes de executar.** O maior ganho de cada demonstração é a pergunta "o que vocês esperam que aconteça?". Não pule.
- **Erro é bem-vindo.** Quando um aluno chutar errado, pergunte "por que você acha isso?" antes de corrigir. O raciocínio errado costuma ser o que a turma inteira tem.
- **Plano B do jwt.io:** se o site não abrir, use a **3.1** (`/jwt/decodificar`), o `base64 -d` e a `4.1` (`/jwt/validar`). O conteúdo da aula é o mesmo.
- **Se o IntelliJ não rodar o `.http`:** os comandos `curl` equivalentes estão no README de `source/`.
- **Atenção ao tempo do Bloco 4:** ele é o mais rico. Se atrasar, reduza a dinâmica do Time Vermelho x Time Azul para 8 minutos, mas mantenha o **Passo 2** (forjar sem a chave).
- **Não use um JWT real de nenhum sistema.** Em sala só circulam tokens gerados para a aula. Diga isso à turma: **um JWT é uma credencial**, e colar um token real em um site é como colar uma senha.
- **A chave de demonstração:** ela está no `application.yml` **de propósito**, para a aula rodar sem configuração. Use isso como exemplo do que não fazer (Bloco 6).
- **Não adianta o código do projeto.** Se a turma pedir "como faço isso no Spring?", segure para a aula 08.
- **Itens do planejamento cobertos:** autenticação baseada em sessão, autenticação baseada em token, o que é JWT, validação JWT, JWT Stolen, gerenciamento de chave de criptografia, estudo de caso "Estrutura JWT", fluxo básico de autenticação baseada em token, tipos de tokens (comentados no Bloco 5) e a lista de múltipla escolha sobre JWT.

---

*Roteiro de aula: módulo de Fundamentos de segurança para aplicações. Aula 03 de 09.*
