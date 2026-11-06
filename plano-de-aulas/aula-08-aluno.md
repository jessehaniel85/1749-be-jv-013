# Material do Aluno — Aula 8: Fechar a API de Chamados com segurança verificável

> **Segunda, 16/11/2026 · Tempo de leitura:** ~12 min. Hoje não há conteúdo novo: é laboratório do projeto final. A
> meta do dia é sair com o fluxo completo rodando (cadastrar, autenticar, 401 sem token, 200 com token, 404 no chamado
> de outra pessoa), o mapa de ameaças começado no README e um ensaio de 2 minutos feito. **As regras valem como estão
> na especificação do projeto** (`apichamadosespecificacao.md`, no LMS). Este texto só organiza o trabalho; se algo
> aqui parecer diferente da especificação, vale a especificação.

---

## 1. O dia em quatro blocos

| Horário | O que acontece |
|---|---|
| 15h00–15h15 | Checklist de aceite (seção 2) e mapa de ameaças (seção 3) explicados para a turma |
| 15h15–16h30 | Grupos nas salas; o instrutor passa em cada sala a cada ~20 min |
| 16h30–16h50 | Intervalo |
| 16h50–17h40 | Grupos nas salas |
| 17h40–17h55 | **Ensaio relâmpago:** cada grupo mostra o fluxo em 2 min (seção 6) |
| 17h55–18h00 | Chamada e lembretes para 18/11 |

Lembrete do que foi combinado e não muda: os quatro grupos seguem como estão; o Git não é requisito (cada grupo
compartilha o código como preferir); a entrega é um zip sem a pasta `target`, enviado no LMS por **cada** integrante;
e a presença de todos em 18/11 é obrigatória: quem faltar fica sem nota.

---

## 2. Checklist de aceite

Cada item vem da especificação (a seção entre parênteses). Confira com o projeto rodando, não de memória.

**Autenticação e segredos**

- [ ] `POST /user` cadastra com `nome`, `username` e `password` e responde **201** com `id`, `nome` e `username`,
      **sem** senha nem hash (03, 07, UC1).
- [ ] `username` repetido responde **409** "Nome de usuário já está em uso." (09, UC1).
- [ ] A senha vai para o banco como **hash BCrypt**; nem a senha nem o hash aparecem em resposta ou em log (04).
- [ ] `POST /authenticate` devolve `{"token": "..."}` com credenciais certas; com usuário inexistente **ou** senha
      errada, **401** com a **mesma** mensagem: "Usuário ou senha inválidos." (UC2, 09).
- [ ] O token tem **expiração** (sugestão da especificação: até 1 hora) e **nenhum dado sensível** no payload (04).
- [ ] A chave do JWT está **fora do código**, em variável de ambiente ou configuração externa (04, 11).
- [ ] Só `POST /user` e `POST /authenticate` são abertos; **todo o resto** exige token (04).
- [ ] Token ausente, adulterado ou expirado responde **401** "Não autenticado." (04, 09).
- [ ] O README diz qual cabeçalho a API aceita: `access_token`, `Authorization: Bearer <token>` ou os dois (07).

**Chamados e propriedade**

- [ ] UC3: `POST /chamados` cria com status inicial `ABERTO` e responde **201**; o dono vem **do token**, nunca de um
      campo do corpo (UC3, 11).
- [ ] UC4: `GET /chamados` lista **só os meus**, com filtros `status` e `titulo`; `status` fora dos valores
      permitidos responde **400** (UC4).
- [ ] UC5: `GET /chamados/{id}` de outra pessoa responde **404**, igual a um id que não existe (UC5, 03).
- [ ] UC6: `PATCH /chamados/{id}/status` respeita a transição (`FECHADO` não volta a `ABERTO`, → **400**) e também
      responde **404** para chamado de outra pessoa (UC6).

**Entrada e erros**

- [ ] Campos validados conforme a seção 08 (tamanhos, formato do `username`, enums), com **400** que diz **qual
      campo** está inválido (04, 08, 09).
- [ ] A busca por título usa **parâmetro**, nunca concatenação de texto (04).
- [ ] Todas as respostas de erro têm o mesmo formato (`status`, `erro`, `mensagem`, `timestamp`), sem stack trace,
      nome de classe ou SQL; o detalhe técnico fica no log (09).
- [ ] Erro inesperado responde **500** genérico, e as exceções de autenticação **não** viram 500 (09).

**README e extras**

- [ ] O README tem tudo o que a seção 13 lista: integrantes e divisão de frentes, como rodar (com a variável da
      chave), como testar, mapa de ameaças, cabeçalho do token, decisões de segurança e extras.
- [ ] Extras: **pelo menos 1** da seção 15 (**2** para equipes de 4 pessoas), funcionando e explicado no README. A
      especificação não fala de equipes de 5: se for o caso do seu grupo, confirme com o instrutor hoje.

💭 **Pare e reflita.** Qual item desta lista o seu grupo marcaria como "feito" sem nunca ter testado? Esse é o
primeiro a conferir.

---

## 3. Como preencher o mapa de ameaças no README

A seção 10 da especificação traz 12 ameaças. **Cada linha precisa de uma defesa implementada ou de uma decisão
justificada**, e o grupo defende essa tabela na apresentação. Duas linhas são extras (DoS/força bruta e dependências
vulneráveis): se o grupo não implementou, a linha continua lá, descrevendo o risco.

Uma linha bem preenchida responde a três perguntas:

1. **O que o atacante tenta?** Em uma frase, no contexto da API de chamados, não a definição do livro.
2. **O que o nosso projeto faz?** A defesa concreta, com o lugar no código (classe ou configuração) onde ela está.
3. **O que sobra de risco?** O limite da defesa, dito com honestidade.

Uma sugestão de colunas para o README: `Ameaça | O que o atacante tenta | Defesa no nosso projeto (onde está) |
Risco que sobra`. O formato é escolha do grupo; o conteúdo das três perguntas é o que conta.

**Exemplo de uma linha (Token roubado).** Adaptem ao que o projeto de vocês realmente faz:

| Ameaça | O que o atacante tenta | Defesa no nosso projeto (onde está) | Risco que sobra |
|---|---|---|---|
| Token roubado | Copiar o token de alguém (de um log, de um print, de uma rede sem HTTPS) e chamar a API como se fosse essa pessoa | O token expira em 1 hora (`TokenService`, na geração). O payload leva só o `username` (no `sub`), o emissor e as datas: nada de senha, CPF ou e-mail. A assinatura é validada em toda requisição, então alterar o payload dá 401 | Durante essa 1 hora, quem tem o token lê e abre chamados como a vítima, e não há como revogá-lo antes do prazo. Por isso o token nunca vai para log nem para print da apresentação, e em produção a API só roda com HTTPS |

Compare com uma versão fraca da mesma linha: "Token roubado: usamos JWT com expiração." Ela não diz quanto tempo, não
diz o que está no payload e não diz o que o atacante consegue fazer. É a diferença entre **descrever** e **justificar**,
e a rubrica (critério "Decisões de segurança") olha a justificativa.

**A coluna "Aula" da especificação.** Ela segue a numeração original do módulo. Na nossa turma, os temas estão nestes
encontros:

| Tema | Onde vimos |
|---|---|
| XSS, CSRF, DoS | Aulas 1 e 2 |
| Criptografia, hash e HTTPS | Aula 2 |
| Cookies e sessão; estrutura do JWT | Aula 3 |
| Falsificação, roubo e chave do JWT; OAuth 2.0 | Aula 4 |
| SSO e OpenID Connect | Aula 5 |
| SQL injection, dependências vulneráveis e exceções | Aula 6 |
| Spring Security e regra de propriedade | Aula 7 |

---

## 4. Roteiro de evidências

A seção 13 pede que a coleção ou o roteiro de requisições mostre, nesta ordem: **cadastrar → autenticar → sem token
(401) → com token (200) → chamado de outro usuário (404)**. Para o último passo, vocês precisam de **dois** usuários.
Pode ser coleção do Postman, arquivo de requisições ou passo a passo no README; o formato é escolha do grupo.

✍️ **Experimente agora.** O roteiro abaixo segue o formato do `README.md` do código da Aula 7 (`aula08/source`), com os
caminhos e os campos da especificação. No PowerShell ou CMD as aspas mudam: prefira o Postman.

```bash
# 1. cadastrar duas pessoas (201, sem senha nem hash na resposta)
curl -i -X POST localhost:8080/user -H "Content-Type: application/json" \
     -d '{"nome":"Ana Souza","username":"ana.souza","password":"senha123"}'
curl -i -X POST localhost:8080/user -H "Content-Type: application/json" \
     -d '{"nome":"Bruno Lima","username":"bruno.lima","password":"senha456"}'

# 2. autenticar cada uma (200 + {"token":"..."})
curl -i -X POST localhost:8080/authenticate -H "Content-Type: application/json" \
     -d '{"username":"ana.souza","password":"senha123"}'
# (repita com "bruno.lima" / "senha456" para obter o token do Bruno)

# 3. sem token (401)
curl -i localhost:8080/chamados

# 4. com o token da Ana: abrir um chamado (201) e listar (200)
curl -i -X POST localhost:8080/chamados -H "Authorization: Bearer <token-da-ana>" \
     -H "Content-Type: application/json" \
     -d '{"titulo":"Impressora do 2º andar não imprime","descricao":"Erro de papel, mas há papel na bandeja.","prioridade":"ALTA"}'
curl -i localhost:8080/chamados -H "Authorization: Bearer <token-da-ana>"

# 5. com o token do Bruno, o chamado da Ana (404)
curl -i localhost:8080/chamados/10 -H "Authorization: Bearer <token-do-bruno>"
```

Troque `Authorization: Bearer` por `access_token: <token>` se for esse o cabeçalho do grupo, e `10` pelo id que a
API devolveu no passo 4. No passo 5, a resposta esperada é a da seção 09:

```
{
    "status": 404,
    "erro": "Not Found",
    "mensagem": "Chamado 10 não encontrado.",
    "timestamp": "2026-11-17T10:45:02.331"
}
```

Peça agora ao Bruno o chamado 9999, que não existe. A resposta tem de ser **igual** (fora o id e o horário). Se as
duas forem diferentes, a API está confirmando para o Bruno que o chamado 10 existe.

**Sugestão para a apresentação:** guardem as evidências (a coleção salva com as respostas, ou prints) para ter um
plano B se a demonstração ao vivo falhar. Antes de guardar um print, confiram que nenhuma chave ou senha real aparece
nele.

---

## 5. Problemas mais comuns e como diagnosticar

Antes de mudar código, leia duas coisas: **o status da resposta** e **o log da aplicação**. Quase todos os casos
abaixo se resolvem com elas.

| Sintoma | Causa provável | Pergunta para o grupo se fazer |
|---|---|---|
| A aplicação não sobe: `Could not resolve placeholder 'JWT_SECRET'` (ou `app.jwt.secret`) | A variável de ambiente da chave não está definida no terminal ou na *Run Configuration* da IDE | Onde a aplicação lê a chave? No terminal em que rodei, ela existe? |
| `POST` devolve **403** com tudo certo | A proteção CSRF está ligada (vem assim por padrão) | Por que podemos desabilitar o CSRF nesta API, e isso está justificado no README? |
| Sem token, a rota devolve **403** em vez de **401** | Falta o *entry point* que responde 401 quando não há autenticação | Que status o Spring Security devolve por padrão quando ninguém está autenticado? |
| Token válido, mas a rota continua bloqueada | O filtro não está na cadeia, está fora de ordem ou não preenche o contexto de segurança | O filtro está registrado **antes** do filtro de login? O que ele faz quando o token é válido? |
| `/authenticate` com senha errada devolve **500** | O tratamento global genérico captura a exceção de autenticação | Que exceção é lançada? Existe um handler específico para ela, antes do genérico? |
| O token sempre parece expirado | Unidade errada na expiração (segundos × milissegundos) | Em que unidade a biblioteca espera a data de expiração? |
| `There is no PasswordEncoder mapped...` | Senha gravada sem hash, ou falta o bean de `PasswordEncoder` | Como a senha foi gravada? Existe o bean do BCrypt? |
| Erro ao criar a tabela de usuários | `user` é palavra reservada em vários bancos | Qual o nome da tabela na entidade? (A especificação sugere `users` ou `usuarios`.) |
| `POST /user` devolve 201 **sem corpo**, ou devolve o hash | O controller devolve nada, ou a própria entidade | A resposta é um objeto só com `id`, `nome` e `username`? |
| O 400 diz só "Requisição inválida." | A validação existe, mas a mensagem não diz o campo | A resposta diz **qual campo** está inválido, como pede a seção 09? |
| O 401 sai **sem corpo** | Quem responde é o Spring Security, antes do tratamento global | O 401 de token ausente segue o formato da seção 09, com "Não autenticado."? |
| O Bruno recebe **200** ou **403** no chamado da Ana | A busca é só pelo id, e o dono é comparado depois (ou nunca) | A consulta já filtra pelo **id e pelo dono**? (Especificação, seção 06, nota.) |
| O chamado fica com o dono errado | O dono vem de um campo do corpo da requisição | De onde vem o usuário: do contexto de segurança ou do JSON? |

Se o grupo travar, compare com o código da Aula 7 (`aula08/source`), rodando com o perfil `jwt`: "o nosso faz o que o
dele faz?". Lembre que ele cobre só a autenticação: a regra de propriedade, a validação de entrada e a resposta do
cadastro com `id`, `nome` e `username` ficaram para o projeto de vocês.

---

## 6. Como dividir o tempo até 18/11

Entre hoje e a entrega há **um dia útil**. A divisão abaixo é **sugestão**; o grupo decide.

| Quando | Foco sugerido |
|---|---|
| Hoje, até o intervalo | Fluxo de evidências (seção 4) rodando de ponta a ponta. Se algum item da autenticação falha, é a prioridade |
| Hoje, depois do intervalo | UC6 (transição de status), validação com 400 por campo, formato único de erro. Começar o mapa de ameaças |
| Ter 17/11 | Extra(s) funcionando; mapa de ameaças completo; README com tudo da seção 13; cada integrante roda o projeto do zero seguindo só o README |
| Qua 18/11, até 16h20 | Só ajustes finos. Nada de funcionalidade nova no dia da entrega |

Duas sugestões de divisão de frentes que costumam funcionar: uma pessoa no README e no mapa de ameaças enquanto as
outras fecham o código; e, no fim, cada integrante explicando aos outros a parte que **não** escreveu. A rubrica é
individual e as perguntas de 18/11 podem cair em qualquer parte do projeto, para qualquer integrante.

Registrem no README quem fez o quê (a divisão de frentes é uma das evidências individuais da rubrica, seção 14).

---

## 7. Ensaio relâmpago (17h40)

Cada grupo tem **2 minutos** para mostrar o fluxo da seção 4: cadastrar → autenticar → 401 → 200 → 404. Sem slides e
sem explicar código: é só a demonstração, cronometrada. Serve para descobrir hoje, e não em 18/11, o que falha ao vivo.

Antes do ensaio, combinem no grupo:

- [ ] Quem compartilha a tela e quem fica de reserva com o projeto rodando na própria máquina (sugestão).
- [ ] A aplicação já está no ar, com a variável da chave definida, antes de chegar a vez do grupo.
- [ ] As requisições estão salvas na ordem certa, prontas para disparar.
- [ ] Se o banco é H2 em memória, ele começa vazio a cada reinício: o cadastro dos dois usuários faz parte do roteiro.

Depois do ensaio, anote o que travou. É a lista de tarefas para 17/11.

💭 **Pare e reflita.** Se alguém da turma perguntasse agora "o que está no payload do token de vocês?", quem do grupo
responderia sem abrir o código?

---

**Antes de sair hoje**

- O que falta no checklist da seção 2 está anotado, com nome de quem faz.
- A dúvida sobre a quantidade de extras (se o grupo tem 5 pessoas) foi confirmada com o instrutor.
- Em 18/11, todos presentes, e cada integrante faz o próprio upload do zip no LMS.
