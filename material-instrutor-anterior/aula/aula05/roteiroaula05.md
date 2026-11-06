# Aula 05: Single Sign-On (SSO)

**Roteiro de Aula · Módulo Fundamentos de segurança para aplicações (BE-JV-013)**

Single Sign-On, o "login único". A turma aprende o que é SSO, o conceito de identidade federada, a arquitetura e o fluxo de uma solução de SSO e os protocolos que a viabilizam (SAML 2.0, OAuth e OpenID Connect), com seus benefícios e riscos.

**Data:** 09/11/2026 · segunda &nbsp;·&nbsp; **Duração:** 3h &nbsp;·&nbsp; **Formato:** expositiva + debate + prática guiada

**Código:** [`source/`](source/README.md), um projeto **novo e independente** (`sso-escola`) com um **provedor de identidade** e **duas aplicações** (Notas e Biblioteca) rodando na sua máquina, sem serviço externo. Ele não substitui a API de alunos: as aulas 07 e 08 continuam de `../aula03/source`.

> **Problema gerador:** Single Sign-On: como entrar uma única vez e acessar vários sistemas?

---

## Sumário

1. [Visão geral](#01--visão-geral)
2. [Retomada da aula 04](#02--retomada-da-aula-04)
3. [O que é SSO e identidade federada](#03--o-que-é-sso-e-identidade-federada)
4. [Arquitetura e fluxo](#04--arquitetura-e-fluxo)
5. [Protocolos e integrações](#05--protocolos-e-integrações)
6. [Vantagens e riscos](#06--vantagens-e-riscos)
7. [Prática guiada](#07--prática-guiada)
8. [Encerramento](#08--encerramento)
9. [Dicas de condução](#09--dicas-de-condução)

---

## 01 · Visão geral

Tempos relativos ao início da aula, ajuste ao seu horário real. É uma aula mais conceitual e de menor volume de conteúdo que as anteriores: use a folga para o debate e para o desenho em grupo, que são os blocos em que a turma realmente aprende.

| Hora | Duração | Bloco |
|---|---|---|
| 00:00 | 10 min | **Retomada** da aula 04 e da atividade de casa |
| 00:10 | 30 min | **Teoria**: o que é SSO e identidade federada |
| 00:40 | 20 min | **Teoria**: arquitetura e fluxo de trabalho |
| 01:00 | 20 min | **Demonstração ao vivo**: o SSO funcionando (projeto `source/`) |
| 01:20 | 10 min | Intervalo |
| 01:30 | 30 min | **Teoria**: protocolos, integrações, vantagens e riscos |
| 02:00 | 40 min | **Prática guiada**: debate, desenho em grupo e exercícios |
| 02:40 | 20 min | **Encerramento**: recap, atividade de casa, preview da próxima aula |

## 02 · Retomada da aula 04

*~10 min*

- "Quais são os quatro papéis do OAuth?" (Proprietário do recurso, cliente, servidor de recursos, servidor de autorização.)
- "Qual grant type se usa em 'Entrar com Google' e qual parâmetro protege esse fluxo contra CSRF?" (Código de autorização; `state`.)
- "Por que o OAuth sozinho não serve para autenticar?" (Ele autoriza acesso, não diz quem é o usuário.)

Peça a 2 alunos que mostrem os **escopos** que o aplicativo que escolheram solicita. Cinco minutos no máximo.

**Gancho:** "Imaginem uma empresa com 20 sistemas internos. Cada um com seu login e senha. O que dá errado?"

## 03 · O que é SSO e identidade federada

*~30 min*

### O problema, para a turma resolver

Pergunte: "Quantas senhas vocês têm hoje? O que acontece com a segurança quando ninguém consegue lembrar de tantas?" Deixe aparecerem as respostas clássicas: senha repetida, senha fraca, papel colado no monitor, pedido constante de redefinição.

### O que é

> **Single Sign-On (SSO)**, ou **login único**, é uma ferramenta de autenticação que permite ao usuário acessar com segurança vários aplicativos e serviços usando **um único conjunto de credenciais**.

**Exemplo:** você faz login na sua conta Google e, automaticamente, está autenticado no Gmail, no YouTube e em outros serviços do Google.

> **Ponto de atenção, a confusão mais comum:** SSO **não é** "usar a mesma senha em todo lugar". Com senha repetida, você digita a senha em cada sistema e cada um a guarda. No SSO você digita **uma vez**, em **um lugar só**, e os demais sistemas **nunca veem a sua senha**. Pergunte à turma qual é a diferença antes de explicar.

### Identidade federada

O SSO se baseia na ideia de **identidade federada**:

> Compartilhar atributos de identidade entre sistemas **confiáveis, mas autônomos**. Quando um usuário é confiável para um sistema, ele recebe automaticamente acesso a todos os outros que estabeleceram uma **relação de confiança** com ele.

Em outras palavras: os sistemas "terceirizam" a pergunta *"quem é você?"* a um provedor em que todos confiam. Cada sistema continua independente (tem suas regras, seus dados e suas permissões), mas confia na resposta desse provedor.

## 04 · Arquitetura e fluxo

*~20 min de teoria + ~20 min de demonstração*

### Os componentes

| Componente | O que é | Exemplo |
|---|---|---|
| **Provedor de identidade** (*Identity Provider*, IdP) | Onde o usuário se autentica de fato; guarda as credenciais e responde "esta pessoa é fulano". | Servidor de login do Google, Microsoft Entra ID, Keycloak, Okta. |
| **Aplicação** (*Service Provider*, SP, ou *relying party*) | O sistema que o usuário quer acessar e que **confia** no provedor. | Gmail, YouTube, o sistema interno de notas. |
| **Relação de confiança** | O acordo (certificados/chaves e URLs configurados) que faz a aplicação aceitar a resposta do provedor. | Configurada previamente entre os dois. |

### Fluxo de trabalho

Desenhe no quadro, com a turma, o fluxo do material:

```
1. O usuário solicita um recurso da aplicação
2. A aplicação percebe que ele não está autenticado e
   o REDIRECIONA ao provedor de identidade
3. O usuário informa as credenciais (no provedor, não na aplicação)
4. O provedor valida as credenciais
5. O provedor devolve ao usuário uma comprovação de autenticação
   (um token ou uma asserção)
6. A aplicação recebe e VERIFICA a comprovação e o login é concluído
```

**O ganho no segundo acesso:** quando o usuário abre outra aplicação, ela também o redireciona ao provedor. Como o provedor **já tem uma sessão ativa** para esse usuário, ele **não pede as credenciais de novo**: devolve a comprovação direto. É isso que dá a sensação de "login único".

```
Usuário -> App A -> (redireciona) -> IdP: pede login   -> volta ao App A, logado
Usuário -> App B -> (redireciona) -> IdP: já tem sessão -> volta ao App B, logado (sem digitar nada)
```

### Conexão com o que já foi visto

Este fluxo é **muito parecido com o fluxo de código de autorização** da aula 04. Chame a atenção: o OAuth entrega *tokens de acesso*; o SSO precisa de uma comprovação de **quem** o usuário é. É por isso que a próxima aula mostra o OpenID Connect.

### Demonstração ao vivo: o SSO funcionando (~20 min)

Tudo roda na sua máquina, **sem nenhum serviço externo**. Suba os três processos **antes da aula** (um provedor e as duas aplicações do cenário "Notas e Biblioteca"): o passo a passo está no [README do código](source/README.md). Deixe o **DevTools aberto na aba Application > Cookies** e os três terminais visíveis (cada passo imprime uma linha de log).

Antes de clicar, **pergunte** (1 minuto, em duplas): "vou abrir as Notas e depois a Biblioteca. Em qual delas vocês acham que a senha será pedida? Por quê?"

| # | Faça | Pergunte / mostre |
|---|---|---|
| 1 | Abra `http://notas.localhost:8081/minhas-notas`. | "Cadê o login das Notas?" (Não existe: a aplicação **redirecionou** ao provedor. Aponte a barra de endereço: `idp.localhost:9000/sso/autorizar?app=notas&state=...`.) |
| 2 | Entre como `ana` (senha `Escola@2026`). | "Onde a senha foi digitada?" (No **provedor**. As Notas nunca a viram.) Aponte a volta com `?code=...&state=...`. |
| 3 | Mostre os **cookies**. | "Quantos cookies de sessão existem e de quem são?" (`IDP_SESSAO` do provedor e `NOTAS_SESSAO` das Notas. São **duas sessões diferentes**.) |
| 4 | Abra `http://biblioteca.localhost:8082`. | "Pediu a senha?" **Não.** Os redirecionamentos acontecem, mas o provedor **já tinha a sessão**. É o login único. |
| 5 | Entre como `carlos` (janela anônima) e abra `/lancar-notas` nas Notas e `/acervo/gerenciar` na Biblioteca. | "O provedor diz quem é o Carlos. Quem decide o que ele pode fazer?" (**Cada aplicação.** Identidade é federada; permissão é local.) |
| 6 | Clique em **"Sair só deste sistema"** nas Notas e abra as Notas de novo. | "Por que entrou sozinho?" (A sessão do **provedor** continua viva.) |
| 7 | Clique em **"Sair de todos"**, confirme, e abra Notas e Biblioteca. | "O que o provedor fez por trás?" (Avisou cada aplicação, de servidor a servidor: veja o log.) Ligue ao risco da seção 06: *single logout* é difícil. |

**Cheque (30 segundos):** "Se o provedor cair agora, quem consegue entrar? E quem já está logado continua?" (Ninguém **novo** entra: ponto único de falha. Quem já tem sessão local continua até ela expirar.)

> **Ponto de atenção:** o código usa as mesmas peças que a turma já viu: o **código de autorização** e o `state` (aula 04), o **JWT assinado com RSA** (aulas 02 e 03) e o **cookie de sessão** (aula 02). A asserção do provedor é o que o SAML chama de asserção e o OIDC chama de **ID Token** (aula 06).

**Se algo der errado ao vivo:** o erro mais comum é abrir `localhost:8081` em vez de `notas.localhost:8081` (os cookies se misturam). O segundo é o provedor não estar de pé: as aplicações mostram uma página de erro 502.

## 05 · Protocolos e integrações

*~20 min*

### Protocolos de SSO

- **SAML 2.0** (*Security Assertion Markup Language*): troca as informações de autenticação em **asserções XML**, assinadas, entre o provedor de identidade e a aplicação. É muito usado em **ambientes corporativos** e em sistemas mais antigos.
- **OAuth 2.0:** visto na aula 04. Autoriza acesso, **não** autentica. Costuma aparecer como base dos outros.
- **OpenID Connect (OIDC):** extensão do OAuth, uma **camada fina que adiciona a autenticação**. É o protocolo moderno, baseado em JSON e JWT, usado em aplicações web e móveis. É o assunto da aula 06.

| | SAML 2.0 | OpenID Connect |
|---|---|---|
| Formato das mensagens | XML | JSON / JWT |
| Cenário típico | Sistemas corporativos, integrações legadas | Aplicações web, SPAs e apps móveis |
| Base | Próprio | Camada sobre OAuth 2.0 |

### Integrações SSO

Na prática, ninguém constrói um provedor de identidade do zero. As aplicações **se integram** a um existente:

- **Google, Microsoft Entra ID e Okta:** provedores de identidade comerciais, usados em empresas e em "Entrar com...".
- **Keycloak:** provedor de identidade de código aberto, que muitas empresas hospedam por conta própria.
- **Spring Security:** tem suporte para atuar como **cliente** desses provedores (SAML e OIDC), o que será comentado na aula 08.

> **Opcional, se houver tempo:** projete a tela de administração de um provedor (por exemplo, o Keycloak em uma conta de teste) só para a turma ver onde se cadastram **usuários** e **aplicações**. Não instale nada em sala sem testar antes.

## 06 · Vantagens e riscos

*~10 min (cabe no bloco de protocolos)*

Faça em formato de duas colunas no quadro, com a turma propondo os itens antes de você completar:

| Vantagens | Riscos |
|---|---|
| Menos senhas para o usuário lembrar e menos senhas fracas ou repetidas | O provedor de identidade é um **ponto único de falha**: se cair, ninguém entra em nenhum sistema |
| Experiência melhor: um login só | O provedor é um **alvo muito valioso**: invadir a conta do usuário abre todos os sistemas |
| Gestão **centralizada**: criar, bloquear e remover um usuário em um só lugar | Sair de todos os sistemas (*single logout*) é difícil de implementar bem |
| Aplicações **não guardam senhas**, o que reduz o estrago de um vazamento | Configuração incorreta da confiança entre provedor e aplicação abre brechas |
| Facilita políticas de segurança, como autenticação em dois fatores, em um ponto só | |

**Fechamento do tema:** como o SSO concentra o risco no provedor, é nele que vale investir em proteção extra: **autenticação multifator**, bloqueio de tentativas e monitoramento.

## 07 · Prática guiada

*~40 min*

### Parte 1: Debate de casos de uso (~10 min)

Divida a turma em 3 ou 4 grupos e dê a cada um um caso. Eles têm 5 minutos para discutir e depois apresentam em 1 minuto:

1. **Conta Google** (Gmail, YouTube, Drive...): quem é o IdP, quem são as aplicações, o que acontece se a conta for invadida?
2. **Sistemas corporativos** (e-mail, folha de pagamento, repositório de código, wiki): por que uma empresa adotaria SSO? O que muda quando um funcionário é desligado?
3. **Serviços de governo** (um único login para vários serviços públicos): vantagens e riscos para o cidadão.
4. **"Entrar com Google" em lojas e apps de terceiros:** é SSO? Quem sabe o quê sobre o usuário?

Guie com as perguntas: "quem é o provedor de identidade?", "o que o usuário digita e onde?", "o que acontece se o provedor cair?".

### Parte 2: Desenho do fluxo em grupo (~20 min)

Cenário para o desenho (em papel ou lousa):

> "A escola tem dois sistemas: **Notas** e **Biblioteca**. Hoje cada um tem seu login. A escola quer um login único, com um **provedor de identidade**."

Cada grupo desenha:

1. Os **três componentes** (Notas, Biblioteca, provedor de identidade) e as **setas** de cada passo.
2. O fluxo de quando o aluno abre **primeiro o Notas** e **depois a Biblioteca**.
3. Onde fica a **sessão** e por que a Biblioteca não pede senha de novo.
4. Um **risco** do desenho e uma **medida** para reduzi-lo.

Compare 2 desenhos com a turma. O ponto que mais precisa aparecer: no segundo acesso, o redirecionamento **acontece**, mas o usuário **não digita nada**, porque o provedor já tem a sessão.

### Parte 3: Lista de exercícios (~10 min)

Lista de exercícios de múltipla escolha sobre **Single Sign-On**. Dê o tempo para a turma começar e deixe o que sobrar como atividade de casa.

## 08 · Encerramento

*~20 min*

Recap de 3 minutos, **feito pela turma**: o que é SSO e o que ele **não é**; o que é identidade federada; os três componentes e a relação de confiança; os 6 passos do fluxo; um protocolo e uma vantagem; um risco e uma medida contra ele.

Feche amarrando com o módulo: "Vimos o SSO como **objetivo**. Na próxima aula vamos ver o **OpenID Connect**, o protocolo moderno que entrega a **identidade** do usuário e usa o OAuth e o JWT que já conhecemos".

**Atividade para casa:**
- **Leitura:** "O que é Single Sign-On" (GeeksForGeeks): https://www.geeksforgeeks.org/introduction-of-single-sign-on-sso/
- **Pesquisa individual:** a diferença entre SAML 2.0 e OpenID Connect (formato das mensagens e cenários de uso).
- **Pesquisa individual:** escolher um sistema que você usa com SSO e descrever quem é o provedor de identidade e quem são as aplicações.
- **Exercícios de fixação:** o que sobrou da lista sobre Single Sign-On.
- **Laboratório (opcional):** reconstruir o projeto `sso-escola` seguindo o passo a passo do [README](source/README.md), ou resolver os exercícios do fim dele (por exemplo, cadastrar uma terceira aplicação, a "Secretaria").

**Preview da Aula 06:** OpenID Connect. Participantes, ID Token, fluxo de código de autorização e fluxo implícito.

## 09 · Dicas de condução

- **Cuidado com a palavra "SSO":** a turma mistura o objetivo (login único), os protocolos (SAML, OIDC) e os produtos (Okta, Keycloak). Quando alguém misturar, pergunte: "isso é o *objetivo*, o *protocolo* ou o *produto*?".
- **Se a turma já tiver experiência corporativa**, use os alunos como fonte: peça que contem como é o SSO na empresa onde trabalham (sem expor dados internos).
- **Ensaie a demonstração uma vez antes da aula** (os três processos de pé, `*.localhost` abrindo no seu navegador). Se a rede da sala não permitir, rode tudo na sua máquina e projete: nenhum passo depende de internet.
- **Não entre em detalhes de XML de SAML.** Basta saber que a asserção é um documento assinado que prova a autenticação.
- **Aula com menos conteúdo:** se sobrar tempo, antecipe a pergunta da aula 06 ("como a aplicação recebe o nome e o e-mail do usuário?") e deixe como gancho.
- **Itens do planejamento cobertos:** o que é Single Sign-On, integrações SSO, identidade federada, fluxo de trabalho, arquitetura, tipos de protocolo (SAML 2.0 e OpenID Connect) e a lista de múltipla escolha sobre Single Sign-On.

---

*Roteiro de aula: módulo de Fundamentos de segurança para aplicações. Aula 05 de 09.*
