# Aula 06: OpenID Connect

**Roteiro de Aula · Módulo Fundamentos de segurança para aplicações (BE-JV-013)**

OpenID Connect (OIDC): a camada de **autenticação** construída sobre o OAuth 2.0. A turma aprende os participantes, o ID Token, os fluxos de código de autorização e implícito e consolida a relação entre JWT, OAuth, SSO e OIDC.

**Data:** 11/11/2026 · quarta &nbsp;·&nbsp; **Duração:** 3h &nbsp;·&nbsp; **Formato:** expositiva + estudo de caso + prática guiada

**Código:** não há código nesta aula. O projeto segue de `../aula03/source` e a **próxima aula, a 07, começa dele** (veja `../aula07/source/README.md`).

> **Problema gerador:** OpenID Connect: como um aplicativo descobre quem é o usuário sem receber a senha dele?

---

## Sumário

1. [Visão geral](#01--visão-geral)
2. [Retomada da aula 05](#02--retomada-da-aula-05)
3. [O que é OpenID Connect](#03--o-que-é-openid-connect)
4. [Participantes e terminologias](#04--participantes-e-terminologias)
5. [Fluxos de autenticação](#05--fluxos-de-autenticação)
6. [Como tudo se conecta](#06--como-tudo-se-conecta)
7. [Prática guiada](#07--prática-guiada)
8. [Encerramento](#08--encerramento)
9. [Dicas de condução](#09--dicas-de-condução)

---

## 01 · Visão geral

Tempos relativos ao início da aula, ajuste ao seu horário real. Esta aula **fecha o bloco de conceitos** (aulas 03 a 06): proteja o bloco de quadro comparativo, porque é ele que dá à turma o mapa de tudo o que foi visto.

| Hora | Duração | Bloco |
|---|---|---|
| 00:00 | 10 min | **Retomada** da aula 05 e da atividade de casa |
| 00:10 | 30 min | **Teoria**: o que é OIDC, participantes e terminologias |
| 00:40 | 35 min | **Teoria**: fluxo de código de autorização x fluxo implícito |
| 01:15 | 10 min | Intervalo |
| 01:25 | 25 min | **Teoria**: como JWT, OAuth, SSO e OIDC se conectam |
| 01:50 | 50 min | **Prática guiada**: caso do e-commerce, ID Token, quadro comparativo e exercícios |
| 02:40 | 20 min | **Encerramento**: recap, atividade de casa, preview da próxima aula |

## 02 · Retomada da aula 05

*~10 min*

- "Qual a diferença entre SSO e usar a mesma senha em todos os sistemas?" (No SSO, a senha é digitada em um lugar só e os sistemas nunca a veem.)
- "Quem é o IdP e quem é a aplicação no 'Entrar com Google'?"
- "Cite um risco do SSO." (O provedor é ponto único de falha e alvo valioso.)

Peça a 2 alunos que digam o que acharam sobre SAML x OIDC. Cinco minutos no máximo.

**Gancho:** "No SSO a aplicação recebe uma comprovação de que o usuário se autenticou. Mas qual **formato** tem essa comprovação, e como o app descobre o nome e o e-mail? É isso que o OpenID Connect padroniza."

## 03 · O que é OpenID Connect

*~15 min*

Lembre o que ficou da aula 04: **OAuth autoriza, mas não autentica**. Sobrou um problema: como entregar a identidade do usuário de forma padronizada?

> **OpenID Connect** é uma **camada fina acima do OAuth 2.0** que adiciona o suporte à **autenticação**. Ele permite que os clientes solicitem e recebam informações sobre as **sessões autenticadas** e os **usuários finais**. É o padrão que define como um **cliente** e um **provedor de identidade** devem interagir.

**Exemplo cotidiano:** um site de e-commerce que permite entrar com uma conta de rede social, em vez de pedir e guardar uma senha própria.

### Versões

Antes do OpenID Connect existiram o **OpenID 1.0** e o **OpenID 2.0**. A versão usada hoje é o OpenID Connect; as anteriores ficaram para trás.

### O que o OIDC acrescenta ao OAuth

| OAuth 2.0 | OpenID Connect |
|---|---|
| Entrega o **token de acesso** | Entrega o token de acesso **e** o **ID Token** |
| "Pode acessar tal recurso?" | "**Quem** é o usuário e ele se autenticou?" |
| Escopos como `read:fotos` | Escopo especial **`openid`** (e `profile`, `email`) |
| Cada provedor entrega o perfil do seu jeito | Formato **padronizado** das informações do usuário |

## 04 · Participantes e terminologias

*~15 min*

### Participantes

São os mesmos papéis do OAuth, com nomes voltados à identidade:

| Participante | O que é |
|---|---|
| **Usuário final** (*end user*) | A pessoa cuja identidade é solicitada. Somos nós. |
| **Cliente** (*relying party*) | O aplicativo ou site com o qual o usuário interage e que precisa saber quem ele é. |
| **Servidor de autorização** (*OpenID Provider*) | Autentica o usuário e fornece as informações de identidade. É o provedor de identidade da aula 05. |
| **Provedor de recursos** | Disponibiliza as informações do usuário (por exemplo, o endpoint de dados do usuário), dependendo do servidor de autorização. |

### Terminologias

- **ID Token:** um **JWT** (aula 03) assinado pelo provedor, que **prova que o usuário foi autenticado** e traz os dados de identidade. É a grande novidade do OIDC.
- **Token de acesso:** o mesmo do OAuth, usado para chamar o endpoint de informações do usuário e outras APIs.
- **Escopos:** o escopo **`openid`** é obrigatório e é o que faz o fluxo ser OIDC. `profile` e `email` pedem nome, e-mail etc.
- **Endpoint UserInfo:** endpoint que devolve os dados do usuário quando chamado com o token de acesso.
- **Descoberta (*discovery*):** o provedor publica suas configurações (endereços, chaves públicas) em um endereço padrão, `/.well-known/openid-configuration`, para o cliente se configurar sozinho.

### Dentro do ID Token

Mostre o payload decodificado de um ID Token de exemplo:

```json
{
  "iss": "https://accounts.exemplo.com",
  "sub": "248289761001",
  "aud": "meu-client-id",
  "exp": 1762876800,
  "iat": 1762873200,
  "nonce": "n-0S6_WzA2Mj",
  "name": "Ana Souza",
  "email": "ana@exemplo.com"
}
```

- `iss`: quem emitiu. `sub`: identificador **único e estável** do usuário naquele provedor. `aud`: para quem o token foi emitido (o `client_id`). `exp` e `iat`: validade.
- `nonce`: valor aleatório enviado pelo cliente na requisição, devolvido no token; protege contra reaproveitamento (*replay*).
- `name`, `email`: aparecem conforme os escopos pedidos.

> **Ponto de atenção:** o cliente **valida** o ID Token antes de confiar: confere a **assinatura** (com a chave pública que o provedor publica), o emissor (`iss`), o destinatário (`aud` igual ao seu `client_id`), a expiração e o `nonce`. Ligue com a aula 03, "validação do JWT". O ID Token é para o **cliente** ler; o token de acesso é para a **API**, não para o cliente interpretar.

## 05 · Fluxos de autenticação

*~35 min*

### Fluxo de código de autorização

É o fluxo da aula 04, com o ID Token na resposta. Desenhe no quadro:

```
1. Usuário clica em "Entrar" no aplicativo
2. O aplicativo REDIRECIONA ao provedor de identidade
   (pedindo scope=openid profile email)
3. O provedor mostra o formulário de login; o usuário informa as credenciais
4. Após validar, o provedor REDIRECIONA de volta ao aplicativo
   com um CÓDIGO DE AUTORIZAÇÃO de uso único
5. O aplicativo troca o código, em uma chamada direta servidor-a-servidor,
   por um ID TOKEN e um TOKEN DE ACESSO (o código se invalida)
6. O aplicativo valida o ID Token e sabe quem é o usuário
   (e usa o token de acesso para pedir recursos, se precisar)
```

- Os **tokens não passam pelo navegador**: só o código.
- É o fluxo **recomendado**, com PKCE para apps móveis e SPAs.

### Fluxo implícito

> Tem muitas etapas em comum com o fluxo de código de autorização, mas **perde aspectos importantes para torná-lo seguro**. A principal diferença é que o **token é enviado de volta ao aplicativo diretamente no redirecionamento**.

- Sem etapa de troca do código: o token chega pela **URL**, o que o expõe ao histórico do navegador, a logs e a scripts.
- O cliente **não se autentica** ao receber o token.
- Era usado por SPAs antes do PKCE existir. Hoje é **desaconselhado**, o mesmo que vimos no OAuth.

| | Código de autorização | Implícito |
|---|---|---|
| Como o token chega | Troca do código, servidor-a-servidor | Direto na URL do redirecionamento |
| Exposição do token | Não passa pelo navegador | Passa pela barra de endereço |
| Situação hoje | **Recomendado** | Legado, evitar |

**Pergunta para a turma (2 min):** "Se o token chega na URL, quem mais além do aplicativo pode vê-lo?" (Histórico, extensões, servidores intermediários, logs.)

## 06 · Como tudo se conecta

*~25 min*

Este é o momento de montar o mapa do bloco de conceitos. Construa o quadro **com a turma**, uma linha por vez:

| Conceito | O que é | Responde a | Aula |
|---|---|---|---|
| **JWT** | Um **formato** de token (assinado, com claims). | "Em que formato viaja a informação?" | 03 |
| **OAuth 2.0** | Protocolo de **autorização** delegada. | "Este portador pode acessar o quê?" | 04 |
| **SSO** | O **objetivo/experiência** de entrar uma vez e acessar vários sistemas. | "Como o usuário evita logins repetidos?" | 05 |
| **SAML 2.0** | Protocolo de SSO com asserções **XML**. | "Como provar a autenticação (mundo corporativo)?" | 05 |
| **OpenID Connect** | Camada de **autenticação** sobre o OAuth 2.0, com ID Token em **JWT**. | "**Quem** é o usuário?" | 06 |

**Frase-resumo para fixar:**

> O **OAuth** autoriza, o **OpenID Connect** autentica sobre ele, o **JWT** é o formato dos tokens e o **SSO** é o que o usuário sente quando tudo funciona.

Ligações com tudo o que foi visto: o fluxo de código (aula 04) é a base do OIDC; o ID Token é um JWT (aula 03) cuja chave pública o cliente busca no provedor; o redirecionamento exige HTTPS (aula 02); o `state` protege contra CSRF (aula 01).

## 07 · Prática guiada

*~50 min*

### Parte 1: Estudo de caso, login por rede social de um e-commerce (~15 min)

Em duplas, a turma analisa a tela de login de um e-commerce (o material de apoio usa um exemplo de loja, com opções de entrar por conta de rede social):

1. Identifique os quatro **participantes** do OpenID Connect nesse login.
2. Qual **escopo** o site provavelmente pede? Por que ele precisa do `openid`?
3. O que o site **recebe** do provedor? E o que ele **não** deveria receber? (A senha.)
4. Qual a vantagem **para o site** de usar esse login? E **para o usuário**? Qual o risco?
5. Se o usuário perder o acesso ao provedor, o que acontece com a conta dele no site?

### Parte 2: Decodificar um ID Token (~10 min)

1. Abra o **jwt.io** e cole o ID Token de exemplo da seção 04 (ou um gerado em sala, **nunca** um de produção).
2. Identifique: o **emissor**, o **destinatário**, o **identificador do usuário** (`sub`), a **expiração**, o `nonce` e os dados pessoais.
3. Responda: o que o aplicativo precisa conferir antes de confiar nesse token? (Assinatura, `iss`, `aud`, `exp`, `nonce`.)
4. Compare com o JWT da aula 03: o que há de **igual** (estrutura) e de **novo** (claims de identidade)?

### Parte 3: Quadro comparativo (~15 min)

Em grupos, preencham uma tabela com as colunas **OAuth 2.0**, **OpenID Connect**, **SSO** e **SAML 2.0**, e as linhas:

- Para que serve.
- Que tipo de token ou documento usa.
- Responde "quem é" ou "pode acessar"?
- Um exemplo do dia a dia.

Cada grupo apresenta **uma linha** e a turma corrige. Compare com o quadro da seção 06.

### Parte 4: Lista de exercícios (~10 min)

Lista de exercícios de múltipla escolha sobre **OpenID Connect**. Dê o tempo para a turma começar e deixe o que sobrar como atividade de casa.

## 08 · Encerramento

*~20 min*

Recap de 3 minutos, **feito pela turma**: o que o OIDC acrescenta ao OAuth; os participantes; o que é o ID Token e o que o cliente valida; a diferença entre fluxo de código e fluxo implícito; a frase-resumo do quadro.

Feche amarrando com o módulo: "Acabaram os conceitos de **autenticação e autorização em arquiteturas maiores**. Nas próximas aulas vamos para o **código**: como a aplicação se defende de injeção e de erros mal tratados, e depois o **Spring Security** com JWT, que é o projeto do módulo".

**Atividade para casa:**
- **Leitura:** a página do OpenID Connect: https://openid.net/connect/
- **Pesquisa individual:** a diferença entre ID Token e token de acesso.
- **Revisão:** escrever, com suas palavras, em até 10 linhas, a relação entre JWT, OAuth, SSO e OpenID Connect.
- **Exercícios de fixação:** o que sobrou da lista sobre OpenID Connect.

**Preview da Aula 07:** Situações reais de vulnerabilidade (SQL Injection e dependências vulneráveis) e tratamento de exceções, em código Spring.

## 09 · Dicas de condução

- **Esta é a aula mais abstrata do módulo:** mantenha o exemplo concreto ("Entrar com Google" em um e-commerce) em todos os blocos e use as perguntas da seção 06 para a turma montar o quadro, e não recebê-lo pronto.
- **Cuidado com o nome:** "OpenID" (antigo) x "OpenID Connect" (atual) x "OAuth". Se um aluno trouxer material antigo, explique as versões da seção 03.
- **Não implemente OIDC em sala.** O projeto usa JWT com Spring Security simples; o objetivo desta aula é a **compreensão**, que vale para a vida profissional mesmo sem o aluno implementar o fluxo.
- **Fechamento do bloco:** vale tirar uma foto do quadro comparativo e disponibilizar à turma.
- **Itens do planejamento cobertos:** introdução ao OpenID Connect, terminologias, fluxo de autenticação, fluxo de código implícito de autenticação, participantes do OpenID Connect, "O que é OpenID Connect?" e a lista de múltipla escolha sobre OpenID Connect.

---

*Roteiro de aula: módulo de Fundamentos de segurança para aplicações. Aula 06 de 09.*
