# API de Chamados de Suporte (HelpDesk)

**Especificação de Projeto: Fundamentos de segurança para aplicações**
Projeto final do módulo: aplica, em uma única API REST, a camada de autenticação e as defesas vistas nas Aulas 01 a 08.

**Equipe:** 3–4 pessoas · **Entrega:** Aula 09 · 18/11/2026 · **Apresentação:** 6 min + 2 min de perguntas

---

## Sumário

1. [Visão geral](#01--visão-geral)
2. [Formação das equipes e aplicação própria](#02--formação-das-equipes-e-aplicação-própria)
3. [Escopo funcional obrigatório](#03--escopo-funcional-obrigatório)
4. [Requisitos técnicos obrigatórios](#04--requisitos-técnicos-obrigatórios)
5. [Casos de uso](#05--casos-de-uso)
6. [Arquitetura sugerida](#06--arquitetura-sugerida)
7. [Contrato de API](#07--contrato-de-api)
8. [Modelo de dados](#08--modelo-de-dados)
9. [Tratamento de erros](#09--tratamento-de-erros)
10. [Mapa de ameaças](#10--mapa-de-ameaças)
11. [Autenticação](#11--autenticação)
12. [Cronograma sugerido](#12--cronograma-sugerido)
13. [Entregáveis](#13--entregáveis)
14. [Critérios de avaliação](#14--critérios-de-avaliação)
15. [Extras (escopo aberto)](#15--extras-escopo-aberto)
16. [Referências](#16--referências)

---

## 01 · Visão geral

A TechNova, empresa fictícia, quer substituir as planilhas e os e-mails com que o time de TI recebe pedidos de suporte (impressora que não imprime, acesso a sistema, computador lento) por uma API de **chamados**. Cada colaborador faz seu cadastro, se autentica e abre chamados; **cada pessoa só enxerga os chamados que ela mesma abriu**.

O sistema guarda e responde dados de pessoas e de problemas internos da empresa. Por isso, o que é avaliado aqui **não é a quantidade de funcionalidades**, e sim se a API é **segura**: quem entra, quem vê o quê, o que acontece quando algo dá errado e quais decisões de segurança foram tomadas e justificadas.

Como vai funcionar a avaliação?
- O funcionamento da API (o fluxo completo de cadastro, autenticação e acesso protegido).
- A **justificativa das decisões de segurança**: por que o token expira nesse prazo, por que o CSRF foi desabilitado, por que a senha vai como hash, onde está a chave.

## 02 · Formação das equipes e aplicação própria

Equipes de **3 a 4 pessoas**, formadas até a **Aula 03**. Como o foco do módulo é segurança, em vez de papéis de gestão a equipe divide **frentes de trabalho**. Uma divisão sugerida (a equipe decide, e pode alternar):

O importante é que:

- **todos entendem o projeto inteiro**, porque na apresentação **todos os membros da equipe deve estar presente na apresentação**.

## 03 · Escopo funcional obrigatório

Três capacidades, todas em uma única aplicação Spring Boot.

### Cadastro de usuário

Uma pessoa se cadastra informando `nome`, `username` e `password`. O sistema grava o usuário no banco (com a senha **protegida por hash**) e responde sem devolver a senha, nem o hash. É uma das duas únicas rotas abertas da API.

### Autenticação

O usuário envia `username` e `password`. Se as credenciais conferem, o sistema devolve um **token JWT** com tempo de expiração. Credenciais erradas resultam em erro de autenticação. É a outra rota aberta da API.

### Chamados

O usuário autenticado:

- **abre um chamado** informando `titulo`, `descricao` e `prioridade`;
- **lista os seus chamados**, podendo filtrar por `status` e buscar por parte do `titulo`;
- **consulta um chamado pelo id**.

**Regra de ouro (propriedade):** um usuário **nunca** vê nem altera um chamado de outra pessoa. Tentar acessar o chamado de outro usuário resulta em **404**, exatamente como se o chamado não existisse (a API não deve confirmar que ele existe).

## 04 · Requisitos técnicos obrigatórios

Cada linha remete a uma aula do módulo. A apresentação deve conseguir apontar, no código, onde cada item foi resolvido.

| Requisito | O que precisa existir | Aula de referência |
|---|---|---|
| Autenticação | Spring Security com **JWT** (ver seção 11 para o caminho alternativo). Só `POST /user` e `POST /authenticate` são abertos; **todo o resto exige token**. | Aulas 03 e 08 |
| Senhas | Gravadas como **hash BCrypt**. A senha e o hash **nunca** aparecem em resposta, nem em log. | Aulas 02 e 08 |
| Token | **Expiração** definida (sugestão: até 1 hora). **Nenhum dado sensível** no payload. Assinatura validada em toda requisição. | Aula 03 |
| Chave do JWT | **Fora do código** (variável de ambiente ou configuração externa). | Aula 03 |
| Sem token ou token inválido | Resposta **401** (token ausente, adulterado ou expirado). | Aula 08 |
| Propriedade dos dados | Usuário só acessa os **próprios** chamados (404 para os de outros). | Aulas 07 e 08 |
| Tratamento de exceções | Tratamento **global** com resposta de erro padronizada, **sem stack trace**, e o detalhe técnico só no log. | Aula 07 |
| Consultas seguras | A busca por título e qualquer consulta usam **parâmetros** (Spring Data/JPQL parametrizado ou instruções preparadas), **nunca** concatenação de texto. | Aula 07 |
| Validação de entrada | Campos validados (tamanhos, formatos, valores permitidos), com resposta **400** clara quando inválidos. | Aulas 01 e 07 |
| CSRF e XSS | Decisão **documentada** no README: por que desabilitar o CSRF é aceitável nesta API e o que a API faz (ou não faz) sobre texto livre que será exibido por um front-end. | Aulas 01 e 08 |
| HTTPS | Não é exigido localmente, mas o README explica **por que é obrigatório em produção** e o que muda sem ele para este projeto. | Aula 02 |
| Documentação | README com o passo a passo para rodar e testar (seção 13). | Aula 09 |

## 05 · Casos de uso

O enunciado exige os **seis** casos de uso abaixo (UC1 a UC6), pois o projeto é feito em equipe. O UC7 é sugestão; a equipe é livre para explorar outros.

### UC1 · Cadastrar usuário

- **Ator:** Qualquer pessoa (rota aberta)
- **Pré-condição:** `username` ainda não existe
- **Fluxo principal:**
  1. Pessoa envia nome, `username` e senha
  2. Sistema valida os campos (ver seção 08)
  3. Sistema gera o **hash** da senha e persiste o usuário
  4. Sistema responde 201 com `id`, `nome` e `username`
- **Exceção:** `username` já existe → 409 · Campos inválidos → 400

### UC2 · Autenticar

- **Ator:** Usuário cadastrado (rota aberta)
- **Fluxo principal:**
  1. Usuário envia `username` e senha
  2. Sistema confere as credenciais
  3. Sistema gera um token JWT com expiração
  4. Sistema responde 200 com o token
- **Exceção:** Credenciais inválidas → 401 com **a mesma mensagem** para "usuário não existe" e "senha errada" (para não revelar quais usuários existem)

### UC3 · Abrir chamado

- **Ator:** Usuário autenticado
- **Fluxo principal:**
  1. Usuário envia título, descrição e prioridade
  2. Sistema identifica o usuário **pelo token** (nunca por um campo do corpo da requisição)
  3. Sistema valida os campos, define o status inicial como `ABERTO` e persiste o chamado associado ao usuário
  4. Sistema responde 201 com o chamado criado
- **Exceção:** Sem token válido → 401 · Campos inválidos → 400

### UC4 · Listar os meus chamados

- **Ator:** Usuário autenticado
- **Fluxo principal:**
  1. Usuário chama a listagem, opcionalmente informando `status` e/ou parte do `titulo`
  2. Sistema devolve **somente os chamados do próprio usuário** que atendem aos filtros
  3. Sistema responde 200 com a lista (vazia, se não houver nenhum)
- **Exceção:** Sem token válido → 401 · `status` fora dos valores permitidos → 400

### UC5 · Consultar um chamado pelo id

- **Ator:** Usuário autenticado
- **Fluxo principal:**
  1. Usuário informa o id do chamado
  2. Sistema busca o chamado **apenas entre os do próprio usuário**
  3. Sistema responde 200 com o chamado
- **Exceção:** Chamado inexistente **ou de outro usuário** → 404 (mesma resposta nos dois casos) · Sem token válido → 401

### UC6 · Atualizar o status de um chamado

- **Ator:** Usuário autenticado, dono do chamado
- **Fluxo principal:**
  1. Usuário informa o id do chamado e o novo status (`ABERTO`, `EM_ANDAMENTO` ou `FECHADO`)
  2. Sistema busca o chamado **apenas entre os do próprio usuário**
  3. Sistema aplica a regra de transição (um chamado `FECHADO` não volta a `ABERTO`)
  4. Sistema persiste e responde 200 com o chamado atualizado
- **Exceção:** Chamado inexistente ou de outro usuário → 404 · Status inválido ou transição não permitida → 400 · Sem token válido → 401

### UC7 · Perfil administrador (sugestão, avançado)

- **Ator:** Usuário com o perfil `ADMIN`
- **Fluxo principal:** vê **todos** os chamados e altera o status de qualquer um. Os demais usuários continuam restritos aos próprios. Exige perfis (`roles`) no usuário e autorização por perfil (seção 15).

## 06 · Arquitetura sugerida

Uma única aplicação **Spring Boot**, organizada em camadas, com pacotes separados por responsabilidade (a organização exata fica a critério da equipe, desde que a segurança fique **isolada** e fácil de apontar na apresentação):

| Pacote/camada | Responsabilidade |
|---|---|
| **segurança** | Configuração do Spring Security, geração e validação do token, filtro que lê o token a cada requisição. |
| **usuário** | Cadastro, busca por `username` e o `UserDetailsService`. |
| **chamado** | Regras de negócio e persistência dos chamados, **sempre** filtrando pelo usuário autenticado. |
| **exceção** | Exceções próprias e o tratamento global. |

**Fluxo de `POST /chamados`, passo a passo:**

1. O cliente envia `POST /chamados` com o token no cabeçalho e o corpo (título, descrição, prioridade)
2. O filtro de segurança lê o token e **valida** assinatura, emissor e expiração
3. Se válido, o filtro informa ao Spring Security **quem é o usuário** da requisição
4. O controller recebe a requisição já autenticada e a valida
5. O serviço de chamados obtém o usuário do **contexto de segurança** (e não do corpo da requisição)
6. O serviço cria o chamado vinculado a esse usuário e o persiste
7. A API responde 201 Created com o chamado criado

**Fluxo de `GET /chamados/{id}` (a regra de propriedade):**

1. O filtro valida o token e identifica o usuário
2. O serviço busca o chamado pelo **id e pelo usuário** (nunca só pelo id)
3. Se não encontra, lança a exceção de chamado inexistente → o tratamento global responde 404
4. Se encontra, devolve 200

> **Nota:** o ponto em que mais se erra é o passo 2: buscar só pelo id e comparar o dono depois (ou esquecer de comparar). Buscar **já filtrando pelo dono** faz o erro ser difícil de cometer.

## 07 · Contrato de API

Endpoints mínimos. Nomes de path são sugestão, mantenham consistência dentro do próprio projeto.

| Método | Endpoint | Descrição | Acesso | Resposta |
|---|---|---|---|---|
| `POST` | `/user` | Cadastra usuário (UC1) | Aberto | 201 / 400 / 409 |
| `POST` | `/authenticate` | Autentica e devolve o token (UC2) | Aberto | 200 / 400 / 401 |
| `POST` | `/chamados` | Abre um chamado (UC3) | Token | 201 / 400 / 401 |
| `GET` | `/chamados` | Lista os meus chamados, com filtros `status` e `titulo` (UC4) | Token | 200 / 400 / 401 |
| `GET` | `/chamados/{id}` | Consulta um chamado meu (UC5) | Token | 200 / 401 / 404 |
| `PATCH` | `/chamados/{id}/status` | Atualiza o status (UC6) | Token | 200 / 400 / 401 / 404 |

**Cabeçalho do token (nas rotas protegidas):** o enunciado do módulo pede o cabeçalho `access_token`; o formato `Authorization: Bearer <token>` também é aceito. A API deve aceitar **ao menos um dos dois**, e a equipe declara no README qual usa.

**Exemplo: resposta do cadastro** (`POST /user`, 201). Observe que **não há senha nem hash**:

```
{
    "id": 1,
    "nome": "Ana Souza",
    "username": "ana.souza"
}
```

**Exemplo: resposta da autenticação** (`POST /authenticate`, 200):

```
{
    "token": "<jwt>"
}
```

**Exemplo: resposta ao abrir um chamado** (`POST /chamados`, 201):

```
{
    "id": 10,
    "titulo": "Impressora do 2º andar não imprime",
    "descricao": "Aparece a mensagem de erro de papel, mas há papel na bandeja.",
    "prioridade": "ALTA",
    "status": "ABERTO",
    "criadoEm": "2026-11-17T10:42:11"
}
```

## 08 · Modelo de dados

Banco em memória (H2) ou SGBD à escolha da equipe. Duas entidades cobrem o escopo obrigatório.

**Usuario**

| Campo | Tipo | Regras |
|---|---|---|
| `id` | Long, gerado | |
| `nome` | String | obrigatório, 2 a 100 caracteres |
| `username` | String, **único** | obrigatório, 3 a 30 caracteres, apenas letras, números, ponto e sublinhado |
| `password` | String | **hash BCrypt**; a senha recebida tem no mínimo 8 caracteres |
| `criadoEm` | LocalDateTime | |

**Chamado**

| Campo | Tipo | Regras |
|---|---|---|
| `id` | Long, gerado | |
| `titulo` | String | obrigatório, 5 a 100 caracteres |
| `descricao` | String | obrigatória, até 2.000 caracteres |
| `prioridade` | enum | `BAIXA`, `MEDIA`, `ALTA` |
| `status` | enum | `ABERTO`, `EM_ANDAMENTO`, `FECHADO` (inicial: `ABERTO`) |
| `criadoEm` | LocalDateTime | |
| `usuario` | referência ao Usuario | dono do chamado; vem do **token**, nunca do corpo |

> **Atenção:** `user` é palavra reservada em vários bancos. Nomeie a tabela de usuários de outra forma (por exemplo, `users` ou `usuarios`).

## 09 · Tratamento de erros

Todas as respostas de erro seguem **o mesmo formato**, com `status`, `erro`, `mensagem` e `timestamp`, e **nada de stack trace**, nome de classe ou SQL na resposta. O detalhe técnico vai para o log do servidor.

```
{
    "status": 404,
    "erro": "Not Found",
    "mensagem": "Chamado 10 não encontrado.",
    "timestamp": "2026-11-17T10:45:02.331"
}
```

| Situação | Status | Mensagem esperada |
|---|---|---|
| Campos inválidos (tamanho, formato, enum) | 400 | Indica **qual campo** está inválido, sem detalhes internos. |
| Credenciais inválidas (usuário inexistente **ou** senha errada) | 401 | "Usuário ou senha inválidos." |
| Token ausente, adulterado ou expirado | 401 | "Não autenticado." |
| Chamado inexistente **ou de outro usuário** | 404 | "Chamado {id} não encontrado." |
| `username` já cadastrado | 409 | "Nome de usuário já está em uso." |
| Muitas requisições (se implementar rate limit) | 429 | "Muitas requisições. Tente novamente em instantes." |
| Erro inesperado | 500 | "Erro interno. Tente novamente mais tarde." (detalhe **só no log**) |

> **Ponto de atenção:** o tratamento global genérico não pode "engolir" as exceções de autenticação e transformá-las em 500. Cada situação da tabela deve devolver o status correto.

## 10 · Mapa de ameaças

Esta é a parte que conecta o projeto ao conteúdo do módulo. **Cada linha precisa de uma defesa implementada ou de uma decisão justificada**, e a equipe preenche essa tabela no README (e a defende na apresentação).

| Ameaça | Aula | Defesa esperada neste projeto |
|---|---|---|
| **SQL Injection** | 07 | A busca por título usa consulta parametrizada. Nenhuma consulta é montada por concatenação de texto. |
| **XSS armazenado** | 01 | Título e descrição são **texto livre** que será guardado e devolvido. O README explica quem escapa a saída (o cliente/front-end que exibe) e o que a API faz (por exemplo, limite de tamanho e validação). |
| **CSRF** | 01 e 08 | A API é *stateless*, com o token em cabeçalho enviado pelo código do cliente (não por cookie): desabilitar o CSRF é aceitável. O README **justifica** isso. |
| **DoS / força bruta** | 01 | (Extra) Rate limit no `/authenticate`. Sem ele, o README descreve o risco. |
| **Token roubado** | 03 | Expiração curta e nenhum dado sensível no payload. O README diz o que um atacante com o token consegue fazer e por quanto tempo. |
| **Chave do JWT exposta** | 03 | Chave fora do código, em variável de ambiente. |
| **Senha vazada** | 02 e 08 | Hash BCrypt no banco. Senha e hash nunca saem em respostas nem em logs. |
| **Vazamento por mensagem de erro** | 07 | Tratamento global sem stack trace, com mensagens genéricas. |
| **Acesso a dado de outro usuário** | 07 e 08 | Toda consulta de chamado filtra pelo dono. Resposta 404 nos dois casos (inexistente/de outro). |
| **Enumeração de usuários** | 08 | Mesma mensagem de erro para usuário inexistente e senha errada. |
| **Tráfego em claro** | 02 | README explica que, em produção, a API só é servida por HTTPS (e que o token, sem HTTPS, seria capturado). |
| **Dependências vulneráveis** | 07 | (Extra) Verificação de dependências com uma ferramenta, com o resultado registrado. |

## 11 · Autenticação

O planejamento do módulo aceita dois caminhos. A avaliação assume o primeiro.

- **Caminho A, JWT (esperado):** `POST /user` cadastra, `POST /authenticate` devolve o token e todas as demais rotas exigem o token no cabeçalho. É o fluxo da Aula 08 e cobre todos os requisitos da seção 04.
- **Caminho B, Spring Security simples (autenticação básica):** usuários gravados no banco com senha em hash, rotas protegidas por HTTP Basic. Não há `/authenticate` nem token, então os requisitos de token e de chave do JWT **não se aplicam**; os demais continuam valendo. Combine com o instrutor antes de escolher este caminho.

> **Nota:** a autenticação básica envia `usuário:senha` em Base64 a cada requisição, o que **só é aceitável com HTTPS**. Quem escolher o caminho B deve explicar essa limitação no README.

**Regras comuns aos dois caminhos:**

- Nenhuma credencial real ou chave escrita no código. Usem variáveis de ambiente (ou uma configuração externa ao projeto) e expliquem a escolha na apresentação.
- A API **nunca** confia em um identificador de usuário enviado no corpo da requisição: a identidade vem sempre da autenticação.

## 12 · Cronograma sugerido

O projeto roda em paralelo ao módulo. O tempo é curto (a autenticação só é construída na Aula 08), então o ideal é **liberar esta especificação na Aula 01** e deixar as equipes modelarem e começarem o que não depende de segurança. Um trabalho em equipe também pede **combinados de colaboração** logo no início (divisão de frentes, como compartilhar o código entre os integrantes, quem revisa o quê).

| Quando | Meta |
|---|---|
| **Aula 01 (28/10)** | Apresentação do projeto. A turma lê a especificação. |
| **Até a Aula 03 (04/11)** | Equipes formadas e frentes de trabalho divididas e registradas. |
| **Até a Aula 05 (09/11)** | Escolhe o formato (padrão ou aplicação própria) e combina com o instrutor. Modela as entidades e cria os endpoints de chamados **abertos** (sem segurança ainda). Define quais extras (seção 15) fará. |
| **Aula 07 (13/11)** | Incorpora o tratamento global de exceções e a validação de entrada. Revisa a busca por título (consulta parametrizada). |
| **Aula 08 (16/11)** | Constrói em aula o cadastro, a autenticação e o filtro (a equipe pode alternar quem digita). Passa a proteger as rotas e aplica a regra de propriedade. |
| **Entre as Aulas 08 e 09** | Fecha os UC1 a UC6, implementa o(s) extra(s), preenche o mapa de ameaças e escreve o README. |
| **Aula 09 (18/11)** | Últimos ajustes com apoio do instrutor, **ensaio da apresentação**, apresentação e devolutiva da rubrica. |

> **Atenção:** entre a Aula 08 e a Aula 09 há **apenas um dia útil** de intervalo. Por isso o plano pressupõe que a equipe chegue na Aula 08 com os chamados já modelados.

## 13 · Entregáveis

- **Código:** upload do arquivo zip com todo o projeto no LMS. *Importante:* o projeto é em equipe, mas o upload é **individual**: todos os integrantes fazem o upload via LMS.
- **README** com:
  - os **integrantes** e a **divisão de frentes** de trabalho;
  - como rodar a aplicação (incluindo a variável de ambiente da chave do JWT);
  - como testar (passo a passo ou coleção do Postman/arquivo de requisições);
  - o **mapa de ameaças** (seção 10) preenchido, com a defesa de cada linha;
  - qual cabeçalho de token a API aceita;
  - as decisões de segurança e o porquê (expiração do token, CSRF, HTTPS em produção);
  - os **extras** (seção 15) escolhidos e como foram implementados.
- **Evidências:** a coleção/roteiro de requisições deve mostrar o fluxo: cadastrar → autenticar → acessar **sem** token (401) → acessar **com** token (200) → acessar o chamado de **outro** usuário (404).
- **Apresentação da equipe** (6 minutos + 2 de perguntas), no roteiro da Aula 09. *Importante:* todos os integrantes devem estar presentes e **todos** podem ser chamados a responder as perguntas; não é necessário que todos falem na parte expositiva.
  1. a aplicação e a divisão do trabalho (1 min);
  2. a demonstração do fluxo acima (3 min);
  3. uma decisão de segurança e o porquê, e o(s) extra(s) implementado(s) (1 min);
  4. uma dificuldade da equipe (1 min).

## 14 · Critérios de avaliação

O projeto é a principal evidência para a **avaliação por rubrica** do módulo, cujas habilidades são: fundamentos de segurança, criptografia, JWT/OAuth/SSO/OpenID Connect e Spring Security. Os níveis (de "não conhece" a "aplica em escopo aberto") seguem o padrão da escola.

| Critério | O que é observado | Habilidade da rubrica |
|---|---|---|
| Funcionalidade | UC1 a UC6 funcionando de ponta a ponta, com os status da seção 09. | Spring Security |
| Autenticação | Só `POST /user` e `POST /authenticate` abertos; token validado; 401 correto. | Spring Security / JWT |
| Segredos e criptografia | Hash BCrypt; chave fora do código; senha e hash nunca expostos. | Criptografia |
| Autorização | Regra de propriedade respeitada (404 para chamados de outros). | Fundamentos de segurança |
| Entrada e erros | Validação, consulta parametrizada, tratamento global sem vazamento. | Fundamentos de segurança |
| Decisões de segurança | Mapa de ameaças preenchido e **justificado**. | Fundamentos / JWT, OAuth, SSO e OIDC |
| Extras | Pelo menos 1 item da seção 15 (2 itens, para equipes de 4 pessoas), funcionando e explicado. | Conforme o item |
| Apresentação | Clareza ao demonstrar e defender as decisões, e ao responder perguntas. | Todas |

**Escopo fechado x aberto:**

- **Fechado:** a equipe **reproduz** o que foi construído em aula (a mesma estrutura de autenticação) aplicado à API proposta, atendendo a seção 04.
- **Aberto:** a equipe resolve algo **novo** ou decide por conta própria e **justifica**: uma aplicação própria, itens da seção 15, ou uma decisão de segurança que vai além do exigido.

**Avaliação individual dentro da equipe:** a rubrica é **por pessoa**, mesmo com o projeto em equipe. As evidências individuais são:

- as **perguntas da apresentação**, dirigidas a cada integrante (inclusive sobre partes que ele não escreveu);
- a **divisão de frentes registrada no README** (quem fez o quê);
- as listas de exercícios e a participação ao longo do módulo.

Pessoas da mesma equipe podem, portanto, ficar em níveis diferentes na rubrica.

> **Nota:** as perguntas da apresentação são parte da avaliação. Quem apenas "copiou do roteiro" sem entender costuma travar em perguntas como "o que está no payload do seu token?" ou "o que acontece se o chamado for de outro usuário?".

## 15 · Extras (escopo aberto)

Por ser um trabalho em equipe, o escopo inclui **pelo menos 1 extra** (2, para equipes de 4 pessoas), escolhido da lista abaixo e combinado com o instrutor até a Aula 05. Os extras demonstram escopo aberto. Escolham poucos e façam-nos bem:

- **Perfis e autorização por perfil:** perfil `ADMIN` (UC7) com autorização por perfil.
- **Refresh token:** renovar o acesso sem novo login, com tratamento da revogação.
- **Rate limit no `/authenticate`** e/ou bloqueio temporário após várias falhas de login.
- **Cabeçalhos de segurança:** configurar uma Content Security Policy.
- **Testes automatizados** do fluxo completo (cadastrar, autenticar, acessar com e sem token, acessar chamado de outro usuário).
- **Verificação de dependências** com uma ferramenta, registrando o resultado.
- **Troca de senha** com confirmação da senha atual.
- **Registro de auditoria** (quem fez o quê e quando) **sem** dados sensíveis (sem senha, sem token).
- **HTTPS local** com certificado de teste.
- **Comparação documentada** entre a solução própria e o uso de um servidor de identidade externo (OAuth 2.0/OpenID Connect): quando valeria a pena trocar.

## 16 · Referências

- [Spring Security (documentação)](https://docs.spring.io/spring-security/reference/getting-spring-security.html)
- [Spring Security (projeto)](https://spring.io/projects/spring-security)
- [Introdução ao JWT](https://jwt.io/introduction)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP: Cross-Site Request Forgery](https://owasp.org/www-community/attacks/csrf)
- [OWASP: Cross-Site Scripting](https://owasp.org/www-community/attacks/xss/)
- [Error Handling for REST with Spring (Baeldung)](https://www.baeldung.com/exception-handling-for-rest-with-spring)
- [OAuth 2.0](https://oauth.net/2/)
- [OpenID Connect](https://openid.net/connect/)

*Especificação do projeto final, módulo de Fundamentos de segurança para aplicações.*
