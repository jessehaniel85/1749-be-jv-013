# Aula 02: Noções básicas de HTTPS e Cookies

**Roteiro de Aula · Módulo Fundamentos de segurança para aplicações (BE-JV-013)**

Criptografia, HTTPS e cookies. A turma entende como a comunicação entre navegador e servidor é protegida, como esse HTTPS pode ser atacado e o que são os cookies que apareceram na aula passada (XSS e CSRF), incluindo os atributos que os tornam mais seguros.

**Data:** 30/10/2026 · sexta &nbsp;·&nbsp; **Duração:** 3h &nbsp;·&nbsp; **Formato:** expositiva + estudo de caso + prática guiada

**Código:** [`source/`](source/README.md) · **Ponto de partida:** estado final da Aula 01 (`../aula01/source`) · **Próxima aula com código:** Aula 03, que começa do estado final deste projeto.

> **Problema gerador:** Noções básicas de HTTPS: como garantir que ninguém leia ou altere o que trafega entre o navegador e o servidor?

---

## Sumário

1. [Visão geral](#01--visão-geral)
2. [Retomada da aula 01](#02--retomada-da-aula-01)
3. [Criptografia](#03--criptografia)
4. [HTTPS](#04--https)
5. [Cookies](#05--cookies)
6. [Prática guiada](#06--prática-guiada)
7. [Encerramento](#07--encerramento)
8. [Dicas de condução](#08--dicas-de-condução)

---

## 01 · Visão geral

Tempos relativos ao início da aula, ajuste ao seu horário real. Se a teoria de criptografia render mais que o previsto, corte detalhes do handshake (seção 04), não a prática: o importante é a turma entender *o que* o HTTPS garante, não decorar cada mensagem do protocolo.

| Hora | Duração | Bloco |
|---|---|---|
| 00:00 | 10 min | **Retomada** da aula 01 e da atividade de casa |
| 00:10 | 40 min | **Teoria + código**: criptografia (simétrica, assimétrica e hash) e demonstração em Java |
| 00:50 | 30 min | **Teoria**: HTTPS, certificados, ataques e como torná-lo seguro |
| 01:20 | 10 min | Intervalo |
| 01:30 | 35 min | **Teoria**: cookies, tipos, atributos de segurança e **sessão** (e a diferença para o cookie) |
| 02:05 | 35 min | **Prática guiada**: certificado, DevTools, `@CookieValue` e exercícios |
| 02:40 | 20 min | **Encerramento**: recap, atividade de casa, preview da próxima aula |

## 02 · Retomada da aula 01

*~10 min*

Pergunte, sem consultar nada: "Se eu escrever `<script>` num campo de comentário e o site não tratar, o que acontece?" e "Por que o CSRF funciona mesmo sem o invasor saber a senha?". Quem responder o segundo ponto vai usar a palavra **cookie**: é a ponte para hoje.

Peça 2 ou 3 voluntários para contar um ataque DoS/DDoS que pesquisaram em casa. Não gaste mais de 5 minutos, o objetivo é só reativar o assunto.

**Gancho:** "O CSRF explorava o fato de o navegador anexar cookies sozinho. Hoje vamos entender o que é esse cookie, e antes disso: como a comunicação entre navegador e servidor pode ser protegida."

## 03 · Criptografia

*~40 min: ~30 min de teoria e ~10 min de demonstração em código (ao final do tópico)*

**Pergunta de abertura:** "Quando vocês acessam o site do banco numa rede de café, o que impede quem está na mesma rede de ler a sua senha?" Deixe 2 ou 3 respostas aparecerem antes de seguir.

**Definição simples:** criptografia é transformar uma informação legível em algo ilegível para quem não tem a chave, de modo que só quem tem a chave certa consiga voltar ao original.

### Criptografia simétrica

- **Uma única chave** serve para cifrar e para decifrar.
- É **rápida**, boa para grandes volumes de dados (exemplo de algoritmo: AES).
- Problema: como entregar a chave ao outro lado **sem que ninguém a intercepte**? Se a chave viaja em claro, quem a ler decifra tudo.

### Criptografia assimétrica

- Um **par de chaves**: a **pública**, que pode ser distribuída para qualquer um, e a **privada**, que nunca sai do dono (exemplos: RSA, curvas elípticas).
- O que uma chave cifra, só a outra decifra. Cifrar com a pública do destinatário garante que **só ele** lê. Assinar com a privada permite que **qualquer um** confirme, com a pública, que foi aquele dono quem assinou.
- É **mais lenta**, por isso não se usa para trafegar todos os dados.

> **Ponto de atenção:** guarde a ideia de **assinatura digital** (privada assina, pública verifica). Ela volta nas aulas de JWT, OAuth e OpenID Connect.

### Hash

- Função **de mão única**: transforma qualquer entrada em uma "impressão digital" de tamanho fixo (exemplo: SHA-256). Não dá para voltar do hash ao original.
- Serve para **integridade**: se um único bit mudar, o hash muda completamente. Também é como senhas devem ser guardadas (com algoritmos próprios para isso, como BCrypt, que aparece na aula 08).
- **Hash não é criptografia:** não existe chave, e não existe "decifrar".

### Como o HTTPS junta tudo

| Ferramenta | Papel no HTTPS |
|---|---|
| Assimétrica | Autentica o servidor (certificado) e combina uma chave de sessão sem enviá-la em claro. |
| Simétrica | Cifra o tráfego da sessão de fato, porque é rápida. |
| Hash / assinatura | Garante que nada foi alterado no caminho. |

### Demonstração em código: simétrica x assimétrica (~10 min)

Use o projeto de [`source/`](source/README.md) (pacote `cripto`). Os endpoints existem **só para a demonstração**: em um sistema real, ninguém expõe "cifrar" e "decifrar" para o mundo.

**Simétrica (AES-256-GCM): a mesma chave cifra e decifra**

1. `POST /cripto/simetrica/cifrar` com `{"texto":"Olá, Ada!"}`: devolve um texto ilegível em Base64.
2. **Cifre o mesmo texto de novo:** o resultado **muda**. Pergunte por quê. (O IV é aleatório a cada cifragem; ele não é secreto e viaja junto.)
3. `POST /cripto/simetrica/decifrar` com o texto cifrado: volta o original.
4. **Adultere um caractere** do texto cifrado e tente decifrar: **400**. O GCM também **autentica**, então a alteração é detectada (integridade).
5. Mostre no código que a **chave é gerada ao subir a aplicação** e comente: reiniciar a aplicação torna impossível decifrar o que foi cifrado antes. Em um sistema real, a chave vem de variável de ambiente ou cofre de segredos (aula 03).

**Assimétrica (RSA-2048): a pública cifra, a privada decifra**

1. `GET /cripto/assimetrica/chave-publica`: devolve **só a chave pública**. Pergunte: "posso mostrar isso para qualquer pessoa?" (Sim, é o ponto.) E a privada? (Nunca sai da aplicação, nenhum endpoint a expõe.)
2. `POST /cripto/assimetrica/cifrar` (usa a pública) e depois `/cripto/assimetrica/decifrar` (usa a privada): volta o original.
3. Tente cifrar um texto **maior que 190 bytes**: **400**. O RSA só cifra textos pequenos.
4. **Ligue com o HTTPS:** "se o RSA só cifra pouco e é lento, como o HTTPS cifra uma página inteira?" (O RSA, ou a troca de chaves, protege uma chave **simétrica**, e o AES cifra os dados. É o quadro "Como o HTTPS junta tudo".)

**Assinatura digital (a privada assina, a pública verifica)**

1. `POST /cripto/assimetrica/assinar` com `{"texto":"pagar R$ 100"}`: devolve a assinatura.
2. `POST /cripto/assimetrica/verificar` com o texto original e a assinatura: `valida: true`.
3. Verifique de novo trocando **100 por 900**: `valida: false`. É a ideia que volta no JWT (aula 03).

**Hash (SHA-256): a impressão digital de mão única**

1. `POST /cripto/hash` com `{"texto":"pagar R$ 100"}`: devolve `SHA-256` e um hash de **64 caracteres hexadecimais**.
2. Envie **o mesmo texto de novo**: o hash é **idêntico** (diferente da cifragem simétrica, que muda a cada vez: aqui não há IV, o hash é determinístico).
3. Troque **100 por 900**: o hash muda **por completo**, não só um pedaço. É assim que se detecta alteração.
4. Envie um texto **enorme**: o hash continua com 64 caracteres.
5. Pergunte: "dá para voltar do hash ao texto?" (Não: não existe chave nem "decifrar".) E "posso guardar a senha dos usuários só com SHA-256?" (Não: ele é rápido demais, o que ajuda quem tenta adivinhar. Senhas usam o BCrypt, na aula 08.)

> **Ponto de atenção:** o que o código usa, e que a turma não deve trocar por "versões mais simples": **AES no modo GCM** com IV aleatório (não ECB, que revela padrões) e **RSA com OAEP** (não o preenchimento antigo). Se um aluno pesquisar e encontrar `AES/ECB` ou `RSA/ECB/PKCS1Padding` em tutoriais, explique que são exemplos antigos.

## 04 · HTTPS

*~30 min*

O **`S`** de HTTPS é de *Secure*: é o HTTP trafegando dentro de uma conexão protegida por **TLS** (sucessor do SSL).

### O que o HTTPS garante

- **Confidencialidade:** o conteúdo trafega criptografado, ninguém no caminho consegue ler.
- **Autenticidade:** o usuário está falando com o servidor verdadeiro, e não com um site de *phishing*.
- **Integridade:** os dados não foram alterados no caminho.

Uma requisição HTTP pode ser facilmente monitorada, modificada ou copiada. Mostre isso com uma frase: "HTTP é um cartão-postal, HTTPS é uma carta lacrada".

### Certificado e autoridade certificadora

Para provar que o servidor é quem diz ser, ele apresenta um **certificado digital**. Ele contém, entre outras coisas:

- o **domínio** para o qual foi emitido;
- a **chave pública** do servidor;
- o **emissor** (a Autoridade Certificadora, CA) e o **prazo de validade**;
- a **assinatura** da CA sobre tudo isso.

O navegador e o sistema operacional já vêm com uma lista de CAs em que confiam. Se o certificado foi assinado por uma delas (direta ou por uma cadeia) e está dentro do prazo, e se o domínio confere com o endereço acessado, o navegador confia.

### Handshake, em versão resumida

```
1. Cliente -> Servidor: "olá, suporto estes algoritmos" (Client Hello)
2. Servidor -> Cliente: escolhe os algoritmos e envia o certificado
3. Cliente: valida o certificado (CA confiável? validade? domínio?)
4. Cliente e servidor combinam uma chave de sessão (troca de chaves assimétrica)
5. A partir daqui, todo o tráfego é cifrado com essa chave simétrica
```

> **Dica:** não entre em detalhes de cada mensagem. A pergunta que a turma precisa levar é "como o navegador sabe que o servidor é de verdade?" (certificado assinado por CA confiável) e "como os dois combinam uma chave sem que ninguém a veja?" (criptografia assimétrica na troca).

### Como o HTTPS pode ser atacado

- **Fraquezas do protocolo ou da codificação:** versões antigas de SSL/TLS e algoritmos fracos podem ser explorados. Hoje, o recomendado é TLS 1.2 ou 1.3; SSL e as primeiras versões de TLS são consideradas inseguras.
- **Dispositivo do usuário comprometido por phishing:** o canal pode ser perfeito e ainda assim o invasor estar *dentro* do computador da vítima.
- **Certificado falso:** um certificado forjado ou emitido indevidamente tentando se passar por um verdadeiro, com o invasor no meio da comunicação (ataque *man-in-the-middle*).

### Tornando o HTTPS seguro

- Certificado emitido por uma **autoridade certificadora** reconhecida, **dentro da validade** e para o **domínio correto**.
- Versões atuais de TLS, desabilitando as antigas.
- Redirecionar todo o tráfego HTTP para HTTPS e, como reforço, usar o cabeçalho **HSTS** (`Strict-Transport-Security`), que faz o navegador se recusar a usar HTTP com aquele site.
- **Benefício extra:** sites com HTTPS têm melhor classificação em buscas.

> **Ponto de atenção:** ter HTTPS na URL **não significa que o site é confiável**. O cadeado diz "a conexão com este domínio está protegida", não "este domínio é honesto". Sites de phishing também podem ter certificados válidos. Pergunte à turma: "o cadeado garante que `banc0.com` é o meu banco?" (Não.)

## 05 · Cookies

*~35 min (25 de cookies e 10 de sessão)*

**Gancho:** "Na aula passada o CSRF funcionava porque o navegador anexava o cookie sozinho. Agora vamos ver o que tem dentro dele."

Cookies HTTP são **pequenos arquivos de texto** criados pelos sites e armazenados no navegador. Têm tamanho limitado (cerca de 4 KB), então servem para identificadores e preferências, não para guardar muito dado.

### Composição de um cookie

- **Nome:** como o site identifica o cookie.
- **Valor:** normalmente um identificador exclusivo (por exemplo, o ID da sessão do usuário).
- **Atributos:** data de expiração (`Expires`/`Max-Age`), `Domain`, `Path` e as flags de segurança.

O servidor cria o cookie com um cabeçalho de resposta, e o navegador o devolve em cada requisição seguinte:

```http
HTTP/1.1 200 OK
Set-Cookie: user-id=42; Max-Age=3600; Path=/; Secure; HttpOnly; SameSite=Strict

GET /perfil HTTP/1.1
Cookie: user-id=42
```

### Tipos de cookies

| Tipo | Característica |
|---|---|
| **Primário** (*first-party*) | Instalado pelo site em que o usuário está. Usado, por exemplo, para saber se está logado. |
| **De terceiros** (*third-party*) | Instalado por outro site ou servidor que não é o visitado (propaganda, rastreamento). |
| **De sessão** | Existe só durante uma **sessão de navegação** e some quando o navegador é fechado. Atenção: isso **não** é a "sessão do servidor"; veja a diferença logo abaixo. |
| **Permanente** | Não expira ao fechar o navegador; vale até a data de expiração. |

### Atributos de segurança

| Atributo | O que faz | Liga com |
|---|---|---|
| `Secure` | O cookie só é enviado por HTTPS. | HTTPS (hoje) |
| `HttpOnly` | O JavaScript da página **não consegue ler** o cookie (`document.cookie`). | XSS (aula 01) |
| `SameSite` | O navegador só envia o cookie em requisições originadas do mesmo site. | CSRF (aula 01) |

> **Fechando o ciclo:** um XSS que rouba `document.cookie` não funciona se o cookie é `HttpOnly`; um CSRF não consegue usar um cookie `SameSite=Strict` vindo de outro site; um cookie `Secure` não vaza em HTTP. Cada atributo bloqueia um ataque da aula passada.

### Sessão: o que é e qual a diferença para o cookie (~10 min)

**Pergunte antes de explicar:** "Quando alguém diz *'sua sessão expirou'*, *'fechei o navegador e encerrei a sessão'* ou *'o servidor guarda a sessão'*, é a **mesma** coisa nas três frases?" Deixe 2 palpites. **Não é**: a palavra "sessão" é usada em **três** sentidos, e a confusão entre eles é a causa de muita dúvida (e de muita falha de segurança).

#### Os três sentidos de "sessão"

| Sentido | O que é | Quem controla | Quanto dura |
|---|---|---|---|
| **1. Sessão do navegador** (*browser session*) | O período em que o navegador (ou a janela/aba) fica aberto. Os cookies **sem** `Expires`/`Max-Age` (os "cookies de sessão") e o `sessionStorage` da aba valem só durante esse período. | **O navegador** | Até fechar o navegador (a aba, no caso do `sessionStorage`). |
| **2. Sessão do servidor** (*server-side session*) | Uma **ficha guardada na memória (ou no banco/Redis) do servidor**, com os dados de um usuário (por exemplo, "o ID 9F3A é a Ana, perfil USER"), identificada por um **ID**. Em Java é a `HttpSession`. | **O servidor** | Até o `logout` ou até um tempo sem uso (o Tomcat usa 30 minutos por padrão). |
| **3. Sessão do usuário** (a ideia de "estar logado") | O estado lógico "esta pessoa já provou quem é e continua autenticada". É o **resultado** de um login; pode ser implementada de várias formas (sessão do servidor, token...). | **A aplicação** | Pela regra de negócio: "mantenha-me conectado", expiração por inatividade etc. |

> **Sobre o sentido 1:** "fechar o navegador" nem sempre apaga os cookies de sessão. Se o navegador estiver configurado para *"continuar de onde parei"*, ele **restaura** esses cookies. Por isso não se deve confiar nele para "deslogar".

#### Cookie é uma coisa, sessão é outra

| | **Cookie** | **Sessão** (do servidor) |
|---|---|---|
| **Onde fica** | No **navegador** | No **servidor** |
| **O que é** | Um texto pequeno que o navegador guarda e devolve em toda requisição | A "ficha" com os dados do usuário (quem é, qual o perfil) |
| **Quem pode alterar** | O usuário (basta o DevTools) | Só o servidor |

**Como se juntam:** o cookie leva só o **ID** da sessão (`JSESSIONID=9F3A...`); a sessão guarda os **dados** (`9F3A` = ana, USER). É a chapelaria: a sessão é o **casaco** no servidor, e o cookie é a **ficha numerada** no seu bolso. A ficha não é o casaco, só serve para achá-lo.

```
Login          ──▶  o servidor cria a sessão  { 9F3A → ana, USER }
Resposta       ◀──  Set-Cookie: JSESSIONID=9F3A
Requisições    ──▶  Cookie: JSESSIONID=9F3A   (o servidor acha a ficha: "é a Ana")
```

**O que importa para a segurança:**

- **Nunca coloque a identidade dentro do cookie.** O `user-id=42` desta aula é só para ver o mecanismo: quem troca o `42` por `1` no DevTools vira outro usuário. Na sessão de verdade, o cookie leva um ID aleatório e a identidade fica no servidor (a aula 03 constrói isso).
- **Apagar o cookie não apaga a sessão.** A ficha continua no servidor até expirar. Por isso o *logout* correto invalida a sessão no servidor (`session.invalidate()`).
- **Roubar o cookie é assumir a sessão.** É para isso que existem `HttpOnly`, `Secure` e `SameSite`.

**Cheque (cartões A/B):**

1. "Apaguei o cookie `JSESSIONID`. A sessão no servidor foi apagada? **A** sim · **B** não." → **B**. Só o cliente perdeu a ficha.
2. "Posso guardar o perfil `ADMIN` direto no cookie?" → **Não**: o usuário edita o cookie.

> **Ponte para a aula 03:** "Se a sessão fica na memória do servidor, o que acontece com 3 servidores atrás de um balanceador?" Deixe no ar.

### Lendo um cookie com Spring

A anotação `@CookieValue` mapeia o valor de um cookie para um parâmetro do método do controller:

```java
@GetMapping("/boas-vindas")
public String boasVindas(
        @CookieValue(name = "user-id", defaultValue = "default-user-id") String userId) {
    return "Olá, usuário " + userId;
}
```

- Se o cookie `user-id` **não existir**, é usado o `defaultValue`.
- Sem `defaultValue` e sem o cookie na requisição, o Spring rejeita a requisição. O material cita `IllegalStateException`; nas versões atuais do Spring MVC o erro costuma ser `MissingRequestCookieException` (resposta 400). Confira na versão do seu projeto antes de afirmar em sala.

Para **criar** um cookie com os atributos de segurança:

```java
@GetMapping("/login-teste")
public ResponseEntity<String> loginTeste() {
    ResponseCookie cookie = ResponseCookie.from("user-id", "42")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(Duration.ofHours(1))
            .build();
    return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body("Cookie criado");
}
```

## 06 · Prática guiada

*~35 min*

### Parte 1: Estudo de caso "Entendendo HTTPS" (~7 min)

Em duplas, cada aluno escolhe um site (o do banco, uma loja, o próprio site da Ada) e:

1. Clica no **cadeado** do navegador e abre as informações do certificado.
2. Anota: **para quem** foi emitido, **quem emitiu** (a CA), **validade** e a **cadeia** de certificação.
3. Responde: "este certificado garante que o site é honesto?" (Garante a conexão protegida e o domínio, não a honestidade.)

Compartilhe 2 ou 3 resultados e compare as CAs.

### Parte 2: Cookies no DevTools (~10 min)

1. Abra as **ferramentas do desenvolvedor** (`F12`) em um site que a turma use.
2. Aba **Application** (ou *Armazenamento*), seção **Cookies**: identifique nome, valor, expiração e as colunas `HttpOnly`, `Secure` e `SameSite`.
3. Aba **Network**: recarregue a página, abra uma requisição e encontre o cabeçalho `Set-Cookie` na resposta e `Cookie` na requisição.
4. No **Console**, digite `document.cookie`: quais cookies aparecem? E quais *não* aparecem? (Os `HttpOnly` não aparecem. Faça a ligação com o XSS.)
5. **Ache o cookie da sessão.** Em um site em que a turma esteja logada e possa perder o login sem prejuízo, procure o cookie com **valor longo e aleatório** (nomes comuns: `JSESSIONID`, `PHPSESSID`, `session`, `sid`). Pergunte: "ele contém o **nome** da pessoa?" (Não: é só o ID da ficha.) Depois **apague só esse cookie** e recarregue a página: a pessoa é deslogada. É a diferença entre cookie e sessão na prática: o cookie era a "ficha"; sem ela o servidor não acha o "casaco".

### Parte 3: `@CookieValue` com Spring, ao vivo (~13 min)

Use o projeto da aula anterior, no estado final (`../aula01/source`); o código desta aula está em [`source/`](source/README.md), que é o mesmo projeto mais os pacotes `cripto` (demonstração da seção 03) e `cookie`.

1. Crie o endpoint `/boas-vindas` com `@CookieValue` e `defaultValue`. Chame sem cookie e veja o valor padrão.
2. Crie o endpoint `/login-teste` que devolve o cookie com `HttpOnly`, `SameSite` etc.
3. Chame `/login-teste` pelo navegador, confira o cookie no DevTools e chame `/boas-vindas` de novo: agora o valor vem do cookie.
4. Remova o `defaultValue` e chame sem cookie: o que acontece? (Erro 400.) Como isso deveria ser tratado? Deixe a pergunta no ar, ela é retomada na aula 07, de tratamento de exceções.
5. No console do navegador, rode `document.cookie`: o cookie `HttpOnly` não aparece.
6. **(Se der tempo) O cookie com ID de sessão.** Crie `/login-teste/session-id`, que gera um ID aleatório, guarda `ID → usuário` no servidor e devolve **só o ID** no cookie `session-id`; e `/perfil-teste/session-id`, que acha o usuário pelo ID. Chame o login, olhe o cookie no DevTools (valor longo, **sem o nome** do usuário) e chame o perfil: "Olá, ana!". Troque um caractere do ID e chame de novo: **401**. Pergunte: "qual a diferença para o `user-id=42`?" (Naquele, o cookie **era** a identidade e qualquer um editava; aqui o cookie só aponta para a ficha que o servidor guarda.)

### Parte 4: Lista de exercícios (~5 min)

Lista de exercícios de múltipla escolha sobre **HTTPS**. Dê o tempo para a turma começar e deixe o que sobrar como atividade de casa.

## 07 · Encerramento

*~20 min*

Recap de 3 minutos, **feito pela turma**: o que é criptografia simétrica e assimétrica e para que cada uma serve; o que o HTTPS garante; como ele pode ser atacado; o que é um cookie e quais atributos protegem contra XSS e CSRF; **a diferença entre cookie e sessão** (um fica no navegador, o outro no servidor; o cookie leva só o ID).

Feche amarrando com o módulo: "Cookies guardam o identificador da sessão do usuário. Na próxima aula vamos comparar autenticação **por sessão (cookie)** com autenticação **por token**, e conhecer o JWT".

**Atividade para casa:**
- **Leitura:** "Proteja seu site com HTTPS" (Google Search Central): https://developers.google.com/search/docs/advanced/security/https?hl=pt-br
- **Pesquisa individual:** a diferença entre SSL e TLS e quais versões são consideradas seguras atualmente.
- **Pesquisa individual:** a diferença entre criptografia simétrica e assimétrica, com um exemplo de uso de cada uma.
- **Exercícios de fixação:** o que sobrou da lista sobre HTTPS.

**Preview da Aula 03:** Json Web Token (JWT). Sessão x token, a estrutura de um JWT e o que acontece quando um token é roubado. Vocês vão decodificar um token de verdade.

## 08 · Dicas de condução

- **Criptografia pode virar um buraco:** a turma tem níveis diferentes. Evite fórmulas e números primos. Use a analogia do cadeado (chave pública = cadeado aberto que qualquer um pode fechar; chave privada = a chave que só o dono tem).
- **Teste a Parte 2 antes:** alguns sites bloqueiam o DevTools ou mostram avisos de cookies. Tenha um site de reserva escolhido.
- **Cookie `Secure` em `localhost`:** na demo do `/login-teste`, o comportamento de cookies `Secure` sobre HTTP puro varia por navegador. Teste antes; se o cookie não for gravado, mostre isso à turma, é uma boa ilustração do atributo.
- **Não prometa mais do que o HTTPS dá:** reforce que ele protege o **canal**, não o conteúdo da aplicação. XSS e CSRF continuam possíveis em um site HTTPS.
- **Itens do planejamento cobertos:** o que é criptografia, o que é HTTPS e como funciona, como HTTPS pode ser atacado, tornando HTTPS seguro, o que são cookies, estudo de caso "Entendendo HTTPS" e a lista de múltipla escolha sobre HTTPS.

---

*Roteiro de aula: módulo de Fundamentos de segurança para aplicações. Aula 02 de 09.*
