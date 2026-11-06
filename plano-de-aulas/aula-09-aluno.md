# Material do Aluno — Aula 9: Entregar e defender uma API protegida

> **Quarta, 18/11/2026 · Tempo de leitura:** ~10 min. Última aula do módulo, sem conteúdo novo. Cada grupo faz os
> últimos ajustes, entrega o projeto no LMS, apresenta e responde às perguntas; no fim, cada pessoa faz a
> autoavaliação e recebe a devolutiva pela rubrica. **As regras valem como estão na especificação do projeto**
> (`apichamadosespecificacao.md`, seções 13 e 14). Este texto só ajuda você a se preparar; se algo aqui parecer
> diferente da especificação, vale a especificação.

---

## 1. A agenda do dia

| Horário | O que acontece |
|---|---|
| 15h00–16h20 | Últimos ajustes nos grupos, com apoio do instrutor |
| até 16h30 | **Upload do zip no LMS, por cada integrante** |
| 16h30–16h50 | Intervalo |
| 16h50–17h25 | Apresentações: 4 grupos, 8 min cada (6 de apresentação + 2 de perguntas) |
| 17h25–17h55 | Autoavaliação, devolutiva da rubrica e encerramento do módulo |
| 17h55–18h00 | Chamada |

**Presença obrigatória de todos os integrantes.** Foi o combinado com a turma: quem faltar à apresentação fica sem
nota. A ordem dos grupos é informada na aula.

---

## 2. O que entregar e como

**O que vai no zip** (especificação, seção 13):

- [ ] O projeto inteiro: `pom.xml`, `src/` e o que mais o projeto precisar para rodar.
- [ ] **Sem a pasta `target`.** Ela é gerada pelo Maven a cada build; o `mvn clean` a apaga, ou apague a pasta à mão
      antes de compactar.
- [ ] O **README**, com:
  - os integrantes e a **divisão de frentes** (quem fez o quê);
  - como rodar, incluindo a variável de ambiente da chave do JWT;
  - como testar: passo a passo, coleção do Postman ou arquivo de requisições;
  - o **mapa de ameaças** preenchido, com a defesa de cada linha;
  - qual cabeçalho de token a API aceita;
  - as decisões de segurança e o porquê: expiração do token, CSRF, HTTPS em produção;
  - os extras escolhidos e como foram implementados.
- [ ] As **evidências**: a coleção ou o roteiro de requisições com o fluxo cadastrar → autenticar → sem token (401) →
      com token (200) → chamado de outro usuário (404).
- [ ] **Nenhuma chave ou senha real escrita** em código, configuração ou README (seção 11). No README, a chave aparece
      só como exemplo ("troque por uma chave longa e aleatória").

**Como entregar:**

1. Um integrante gera o zip final, e o grupo confere: abre o arquivo, vê `pom.xml`, `src/` e README, e nenhum
   `target`.
2. O mesmo zip vai para **todos** os integrantes, pelo meio que o grupo já usa para compartilhar o código.
3. **Cada integrante faz o próprio upload no LMS.** O projeto é em equipe, mas o upload é individual (seção 13). Quem
   não subir não tem entrega registrada.
4. Sugestão: depois de subir, baixe o arquivo do LMS e confira que ele abre. Leva um minuto e evita surpresa.

💭 **Pare e reflita.** Se outra pessoa baixar o zip de vocês e seguir só o README, ela consegue rodar e testar a API?
Qual passo vocês fazem "de cabeça" e não escreveram?

---

## 3. Roteiro da apresentação

São **6 minutos de apresentação e 2 de perguntas**, no roteiro fixo da seção 13 da especificação. Todos os
integrantes precisam estar presentes, e **todos** podem ser chamados a responder; não é preciso que todos falem na
parte expositiva.

| Parte | Tempo | O que mostrar |
|---|---|---|
| **1. A aplicação e a divisão do trabalho** | 1 min | Em uma frase, o que a API faz; quem cuidou de qual frente |
| **2. Demonstração do fluxo** | 3 min | Cadastrar → autenticar → sem token (401) → com token (200) → chamado de outro usuário (404) |
| **3. Uma decisão de segurança e os extras** | 1 min | **Uma** decisão, com o porquê; o(s) extra(s) implementado(s) |
| **4. Uma dificuldade** | 1 min | O que foi mais difícil e como o grupo resolveu |

**Dicas para cada parte** (sugestões):

- **Parte 1.** Não leia a especificação para a turma: todos a conhecem. Diga o que é **do grupo** (as decisões, os
  extras, a divisão).
- **Parte 2.** Deixe a aplicação no ar e as requisições salvas na ordem antes de chegar a sua vez. Comece pelo
  cadastro de dois usuários. No passo do 404, mostre também um id que não existe: as duas respostas iguais são o
  ponto da regra de propriedade. Se der tempo, mostre um token **alterado** (um caractere trocado) dando 401.
- **Parte 3.** Escolha uma decisão e justifique de verdade. "Desabilitamos o CSRF" é descrição; "desabilitamos o CSRF
  porque o token vai num cabeçalho que o código do cliente envia, e não num cookie que o navegador envia sozinho" é
  justificativa. Para o extra, mostre-o funcionando, não só o código.
- **Parte 4.** Uma dificuldade real, com a causa e a solução, vale mais do que "foi tudo tranquilo".

**Cuidado com a tela.** Ao mostrar configuração ou terminal, confira que nenhuma chave real aparece. Se a
demonstração ao vivo falhar, mostre as evidências guardadas (coleção com as respostas salvas ou prints) e siga. Quem
não conseguiu concluir tudo apresenta o que tem.

---

## 4. As perguntas: para qualquer integrante

As perguntas da apresentação são parte da avaliação e são **dirigidas a cada integrante, inclusive sobre partes que
ele não escreveu** (seção 14). Por isso, treine com o grupo cada pessoa explicando a parte que **não** fez.

O tipo de pergunta que pode vir, com o tema e a aula em que ele foi visto:

| Tema | Exemplos de pergunta |
|---|---|
| Token (Aulas 3 e 4) | O que está no payload do token de vocês? Tem algum dado sensível? Por que este prazo de expiração? |
| Token roubado (Aula 4) | Se alguém roubar o token, o que consegue fazer, e por quanto tempo? O que vocês fizeram para limitar o estrago? |
| Chave (Aula 4) | Onde está a chave do JWT? O que acontece se ela vazar? |
| Senha (Aulas 2 e 7) | Por que a senha vai como hash, e não criptografada? Por que BCrypt, e não SHA-256? |
| Autenticação (Aula 7) | Quais rotas são abertas e por quê? Qual a diferença entre 401 e 403? |
| Propriedade (Aula 7) | O que acontece se o Bruno pedir o chamado da Ana? Por que 404, e não 403? Onde está esse filtro no código? |
| CSRF e XSS (Aulas 1, 2 e 7) | Por que desabilitar o CSRF é aceitável nesta API? E se o token fosse num cookie? Quem escapa o texto do chamado antes de exibir? |
| Erros e consultas (Aula 6) | O que a resposta de erro de vocês revela? Como a busca por título se protege de SQL injection? |
| HTTPS (Aula 2) | Se a API fosse acessada por HTTP puro, o que mudaria para o token? |
| OAuth e OIDC (Aulas 4 e 5) | Qual a diferença entre o que vocês implementaram e usar OAuth 2.0 ou OpenID Connect? |

A especificação avisa: quem apenas copiou do roteiro sem entender costuma travar em perguntas como "o que está no
payload do seu token?" ou "o que acontece se o chamado for de outro usuário?". Se você não souber, diga o que sabe e
como descobriria; inventar costuma pesar mais do que admitir o limite.

**Sugestão para quem assiste:** anote, para cada grupo, um acerto e uma sugestão. Ajuda a prestar atenção e serve de
material para a conversa da devolutiva.

---

## 5. Como você é avaliado

O projeto é a principal evidência da **avaliação por rubrica** do módulo. A rubrica é **por pessoa**, mesmo com o
projeto em equipe: pessoas do mesmo grupo podem ficar em níveis diferentes.

**As quatro habilidades e os quatro níveis.** Cada habilidade recebe um nível:

1. **Não conhece** a habilidade.
2. **Conhece**, mas não é capaz de aplicar.
3. **É capaz de aplicar** em escopo **fechado**: reproduz o que foi construído em aula, aplicado à API proposta.
4. **É capaz de aplicar** em escopo **aberto**: resolve algo novo ou decide por conta própria e **justifica** (uma
   aplicação própria, os extras ou uma decisão de segurança além do exigido).

As habilidades são: fundamentos de segurança para aplicações; criptografia; JWT, OAuth, SSO e OpenID Connect; e
Spring Security.

**Os critérios** (especificação, seção 14):

| Critério | O que é observado | Habilidade |
|---|---|---|
| Funcionalidade | UC1 a UC6 funcionando de ponta a ponta, com os status da seção 09 | Spring Security |
| Autenticação | Só `POST /user` e `POST /authenticate` abertos; token validado; 401 correto | Spring Security / JWT |
| Segredos e criptografia | Hash BCrypt; chave fora do código; senha e hash nunca expostos | Criptografia |
| Autorização | Regra de propriedade respeitada (404 para chamados de outros) | Fundamentos de segurança |
| Entrada e erros | Validação, consulta parametrizada, tratamento global sem vazamento | Fundamentos de segurança |
| Decisões de segurança | Mapa de ameaças preenchido e **justificado** | Fundamentos / JWT, OAuth, SSO e OIDC |
| Extras | Pelo menos 1 item da seção 15 (2 para equipes de 4; para equipes de 5, vale o que foi confirmado com o instrutor), funcionando e explicado | Conforme o item |
| Apresentação | Clareza ao demonstrar, defender as decisões e responder às perguntas | Todas |

**As evidências individuais** são três: as suas respostas às perguntas da apresentação; a divisão de frentes
registrada no README; e as listas de exercícios e a sua participação ao longo do módulo.

---

## 6. Autoavaliação

Preencha **sozinho e antes** de receber a devolutiva. A conversa mais útil da devolutiva é justamente a diferença
entre o nível que você se deu e o que o instrutor observou.

| Habilidade | Meu nível (1 a 4) | Evidência que justifica |
|---|---|---|
| Fundamentos de segurança para aplicações | | |
| Criptografia | | |
| JWT, OAuth, SSO e OpenID Connect | | |
| Spring Security (básica e JWT) | | |

Na coluna de evidência, aponte algo concreto: "escrevi o filtro do JWT e expliquei por que ele vem antes do filtro de
login", e não "entendi bem".

**Perguntas para refletir:**

- Em qual habilidade eu mais cresci neste módulo?
- O que eu consegui fazer sozinho que não conseguia antes?
- Se eu tivesse mais uma semana, o que melhoraria no projeto?

**Próximo passo:** anote **uma** coisa para aprofundar depois do módulo (por exemplo, OAuth 2.0 na prática,
autorização por perfis, testes de segurança).

**Mini-retrospectiva.** Para fechar, uma resposta de uma frase para cada pergunta: "o que funcionou neste módulo?" e
"o que você mudaria?". As respostas ajudam a melhorar o módulo para as próximas turmas.

---

## 7. O que você passou a saber fazer

Você chegou ao módulo com APIs que funcionavam. Sai com uma API que, além de funcionar, **resiste e explica por que
resiste**:

- **Reconhecer as ameaças** de uma aplicação web (XSS, CSRF, DoS, SQL injection, vazamento por mensagem de erro) e
  escolher a defesa de cada uma no back-end.
- **Guardar senha do jeito certo**, com hash lento e com sal (BCrypt), e saber por que hash não é criptografia.
- **Explicar o HTTPS** e o que acontece com o token sem ele.
- **Emitir e validar um JWT**: o que vai no payload, o que a assinatura garante, por que a chave fica fora do código e
  por que o token expira.
- **Situar OAuth 2.0, SSO e OpenID Connect**: quando a sua própria autenticação basta e quando vale um servidor de
  identidade.
- **Proteger uma API com Spring Security**: rotas abertas e fechadas, filtro do token, 401 correto, CSRF desabilitado
  com justificativa.
- **Garantir que cada pessoa veja só o que é dela**, com a consulta já filtrada pelo dono.
- **Responder a erros sem vazar nada**, com tratamento global e o detalhe só no log.
- **Defender decisões de segurança** por escrito (o mapa de ameaças) e em voz alta (a apresentação).

Segurança **não é uma etapa no fim do projeto**: é um conjunto de decisões em cada linha de código, na entrada de
dados, nas senhas, nos tokens, nos erros e nas dependências. O que vimos aqui é o começo; a lista do
[OWASP Top 10](https://owasp.org/www-project-top-ten/) é o mapa para continuar.

---

**Antes de começar a apresentação**

- O zip está no LMS, enviado por **você** (não só por um colega do grupo).
- A aplicação está no ar, com a variável da chave definida, e as requisições estão salvas na ordem do fluxo.
- Você sabe explicar uma parte do projeto que não escreveu.
