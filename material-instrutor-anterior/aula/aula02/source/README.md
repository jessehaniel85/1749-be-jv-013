# Aula 02: código de apoio (criptografia e cookies)

Código usado na [Aula 02](../roteiroaula02.md): criptografia simétrica e assimétrica em Java (seção 03 do roteiro) e leitura e criação de cookies com Spring (parte 3 da prática guiada, seção 06 do roteiro).

## Ponto de partida

**Comece do estado final da Aula 01** (`../../aula01/source`). Este projeto é exatamente aquele, mais os pacotes `cripto` e `cookie`. Se você continuou o projeto da aula 01, só acrescente o que está abaixo.

O que já veio da aula 01 e **continua igual**: `/busca`, `/busca/segura`, `/alunos` com rate limit. Nenhuma dependência nova: a criptografia usa só a biblioteca padrão do Java (`javax.crypto` e `java.security`).

## O que esta aula acrescenta

| Arquivo | O que é |
|---|---|
| `cripto/CriptografiaSimetrica.java` | AES-256 no modo GCM: a **mesma chave** cifra e decifra. |
| `cripto/CriptografiaAssimetrica.java` | RSA-2048: a **pública cifra**, a **privada decifra**; e a privada **assina** e a pública **verifica**. |
| `cripto/FuncaoHash.java` | SHA-256: transforma qualquer texto em um hash de 64 caracteres hexadecimais. |
| `cripto/HashController.java` | `POST /cripto/hash`: recebe um texto e devolve o hash dele. |
| `cripto/CriptografiaController.java` | Os endpoints `/cripto/...` de cifrar, decifrar e assinar. |
| `cripto/TextoCifradoInvalidoException.java` | Erro para texto cifrado inválido ou adulterado. |
| `cookie/CookieController.java` | Os endpoints de cookies da aula. |
| `src/test/.../Aula02CriptoTest.java` e `Aula02Test.java` | Testes da criptografia e dos cookies. |

## Passo a passo da aula, na ordem em que acontece

### Parte A: criptografia em código (seção 03 do roteiro, ~00:40)

| Passo | O que fazer | Onde está |
|---|---|---|
| A1 | Cifrar um texto com `POST /cripto/simetrica/cifrar`. Cifrar **de novo**: o resultado muda (IV aleatório). | `CriptografiaSimetrica.cifrar` |
| A2 | Decifrar com `POST /cripto/simetrica/decifrar`: volta o original. | `decifrar` |
| A3 | Alterar um caractere do texto cifrado e decifrar: **400** (o GCM detecta a alteração). | |
| A4 | Mostrar que a chave é gerada ao subir a aplicação e conversar sobre onde ela ficaria em um sistema real. | construtor |
| A5 | `GET /cripto/assimetrica/chave-publica`: só a pública é exposta. | `CriptografiaController.chavePublica` |
| A6 | Cifrar com a pública (`/cripto/assimetrica/cifrar`) e decifrar com a privada (`/cripto/assimetrica/decifrar`). | `CriptografiaAssimetrica` |
| A7 | Tentar cifrar mais de 190 bytes: **400**. Ligar com o HTTPS: RSA protege uma chave simétrica, o AES cifra os dados. | `TAMANHO_MAXIMO_BYTES` |
| A8 | Assinar (`/cripto/assimetrica/assinar`) e verificar (`/cripto/assimetrica/verificar`); trocar `100` por `900` e ver `valida: false`. | `assinar`, `verificar` |

| A9 | Calcular o hash com `POST /cripto/hash`. Enviar o mesmo texto de novo (hash **igual**) e trocar `100` por `900` (hash **totalmente diferente**). | `HashController`, `FuncaoHash.sha256` |

Exemplos (Git Bash/Linux/macOS; no PowerShell/CMD as aspas mudam, prefira o Postman):

```bash
# simétrica
curl -s -X POST localhost:8080/cripto/simetrica/cifrar -H "Content-Type: application/json" \
     -d '{"texto":"Olá, Ada!"}'
curl -s -X POST localhost:8080/cripto/simetrica/decifrar -H "Content-Type: application/json" \
     -d '{"cifrado":"<cole o texto cifrado>"}'

# assimétrica
curl -s localhost:8080/cripto/assimetrica/chave-publica
curl -s -X POST localhost:8080/cripto/assimetrica/cifrar -H "Content-Type: application/json" \
     -d '{"texto":"senha: ada123"}'

# assinatura
curl -s -X POST localhost:8080/cripto/assimetrica/assinar -H "Content-Type: application/json" \
     -d '{"texto":"pagar R$ 100"}'

# hash (SHA-256)
curl -s -X POST localhost:8080/cripto/hash -H "Content-Type: application/json" \
     -d '{"texto":"pagar R$ 100"}'
```

> **Por que esses algoritmos:** AES no modo **GCM** com IV aleatório (não o ECB, que revela padrões) e RSA com **OAEP** (não o preenchimento PKCS#1 antigo). Tutoriais antigos ainda mostram `AES/ECB` e `RSA/ECB/PKCS1Padding`; não os use.

> **Só para a demonstração:** em um sistema real ninguém expõe endpoints de "cifrar" e "decifrar". As chaves (AES e o par RSA) são geradas **a cada subida** da aplicação; reiniciar torna ilegível o que foi cifrado antes.

### Parte B: cookies (parte 3 da prática, seção 06 do roteiro)

| Passo | O que fazer | Onde está |
|---|---|---|
| B1 | Criar `GET /boas-vindas` com `@CookieValue` e `defaultValue`. Chamar sem cookie e ver o valor padrão. | `cookie/CookieController.java`, `boasVindas` |
| B2 | Criar `GET /login-teste`, que devolve o cookie com `HttpOnly`, `Secure` e `SameSite=Strict`. | `loginTeste` |
| B3 | Chamar `/login-teste` pelo navegador, conferir o cookie no DevTools (aba *Application*) e chamar `/boas-vindas` de novo: agora o valor vem do cookie. | |
| B4 | Remover o `defaultValue` e chamar sem cookie: erro **400**. Deixar a pergunta "como tratar isso?" para a aula 07. | `boasVindasEstrito` (`/boas-vindas/estrito`) |
| B5 | No console do navegador, rodar `document.cookie`: o cookie `HttpOnly` **não** aparece. Para comparar, chamar `/login-teste/legivel` e rodar `document.cookie` de novo: o cookie sem `HttpOnly` aparece. | `loginTesteLegivel` |
| B6 | **Cookie com ID de sessão.** Chamar `/login-teste/session-id?usuario=ana` e ver no DevTools o cookie `session-id` com um valor longo e aleatório (não tem o nome `ana`). Chamar `/perfil-teste/session-id`: o servidor responde "Olá, ana!". Trocar um caractere do ID no DevTools: **401**. Comparar com o `user-id=42` do B2, que qualquer um edita. | `loginTestWithSessionID`, `perfilTestWithSessionID` |

## Até onde a aula vai

**Esta aula vai até o passo B5 e o código acima é o estado final.** O que ainda **não** existe de propósito:

- **O erro 400 do passo B4 e os 400 da criptografia ficam como estão**, sem formato padronizado. A aula 07 trata esse tipo de erro com um tratamento global.
- **Sem BCrypt.** O hash desta aula é o SHA-256, bom para conferir integridade, mas **não para guardar senhas** (é rápido demais). O BCrypt entra no código na aula 08.
- **Sem criptografia híbrida** (RSA protegendo uma chave AES): é só comentada no passo A7. Fica como desafio para quem terminar cedo.
- **Sem Spring Security e sem banco.** Eles entram nas aulas 08 e 07.

> **Cookie `Secure` em `localhost`:** o comportamento de um cookie `Secure` sobre HTTP puro varia por navegador. Teste antes da aula. Se o `/login-teste` não gravar o cookie, mostre isso à turma: é uma boa ilustração do atributo `Secure`.

## Próxima aula com código

A próxima aula com código é a **Aula 03** (sessão x token e JWT). **Comece deste projeto, no estado final da Aula 02** (`aula02/source`). Veja `../../aula03/source/README.md`.

## Como executar

```bash
mvn spring-boot:run
```

Teste no navegador:

- http://localhost:8080/cripto/assimetrica/chave-publica (a chave pública)
- (`POST /cripto/hash` precisa do Postman ou do `curl`)
- http://localhost:8080/boas-vindas (sem cookie: valor padrão)
- http://localhost:8080/login-teste (cria o cookie; depois abra `/boas-vindas` de novo)
- http://localhost:8080/boas-vindas/estrito (sem cookie: 400)
- http://localhost:8080/login-teste/legivel (cookie sem `HttpOnly`)
- http://localhost:8080/login-teste/session-id?usuario=ana (cookie `session-id` com um ID aleatório; depois abra `/perfil-teste/session-id`)

Os endpoints `POST` da criptografia precisam do Postman ou do `curl`.

## Testes automatizados

```bash
mvn test
```

Rodam o `Aula01Test` (herdado), o `Aula02Test` (cookies), o `Aula02CriptoTest` (cifra e decifra nos dois modos, IV diferente a cada cifragem, texto adulterado rejeitado, limite de 190 bytes do RSA, assinatura válida e inválida) e o `Aula02HashTest` (vetor de teste oficial do SHA-256, mesmo texto gera o mesmo hash, um caractere muda tudo, tamanho fixo de 64 caracteres).

> **Atenção:** se o seu `~/.m2/settings.xml` define `maven.test.skip=true`, o `mvn test` pula os testes. Use `mvn -Dmaven.test.skip=false test`.
