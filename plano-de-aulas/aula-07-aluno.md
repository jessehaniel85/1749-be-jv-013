# Material do Aluno — Aula 7: Proteger uma API com Spring Security e JWT

> **Sexta, 13/11/2026 · Tempo de leitura:** ~22 min. Hoje a API deixa de ser aberta. A cada requisição ela vai perguntar o que toda a segurança do módulo pergunta: **quem é você, e o que você pode fazer?** Faremos em duas etapas, as duas sobre a API de alunos: primeiro a **autenticação básica**, com usuário e senha na configuração, para ver o mecanismo funcionando; depois a **autenticação com JWT**, com cadastro (`POST /user`), autenticação (`POST /authenticate`) e um filtro que valida o token a cada chamada. É a camada de autenticação do projeto final. Código desta aula: pasta `aula08`, com os perfis `basica` e `jwt`.

---

## 1. Como o Spring Security funciona

🎬 **Imagine o cenário.** Uma empresa tem um sistema interno de solicitações. Na primeira versão, qualquer pessoa da rede chama a API e abre pedidos em nome de qualquer outra. A segunda versão pede login, mas guarda a senha em texto puro numa tabela; quando um backup vaza, as senhas de todo mundo vazam junto, e várias pessoas usam a mesma senha no e-mail. A terceira versão precisa resolver as duas coisas.

São duas perguntas diferentes, e convém não misturá-las:

| | Pergunta | Exemplo | Quando falha |
|---|---|---|---|
| **Autenticação** | Quem é você? | usuário e senha, ou um token válido | `401 Unauthorized`: não sei quem você é |
| **Autorização** | O que você pode fazer? | só administradores apagam alunos | `403 Forbidden`: sei quem você é, mas isto não é para você |

O **Spring Security** é o framework de autenticação e controle de acesso do ecossistema Spring. Ele se coloca **na frente** dos controllers como uma **cadeia de filtros**: toda requisição passa por ela antes de chegar ao seu código.

```
Requisição  ->  [ filtro 1 ] -> [ filtro 2 ] -> ... -> [ filtro de autenticação ] -> Controller
                                                          |
                                  Se não autenticado: a requisição é barrada (401/403)
```

As peças que você vai usar hoje:

| Componente | Para que serve |
|---|---|
| `SecurityFilterChain` | as regras: quais rotas são livres, quais exigem autenticação, quais filtros entram na cadeia |
| `UserDetailsService` | sabe **buscar o usuário** (por exemplo, no banco) pelo nome |
| `PasswordEncoder` | faz o **hash** da senha e confere (BCrypt) |
| `AuthenticationManager` | recebe as credenciais e as valida usando as peças acima |
| `SecurityContext` | guarda **quem é o usuário autenticado** na requisição atual |

> **Spring Security 5 × 6** — Por muitos anos, a configuração do Spring Security foi feita numa classe que estendia `WebSecurityConfigurerAdapter` e sobrescrevia `configure(HttpSecurity http)`, e é assim que muitos tutoriais e textos de referência ainda mostram. Essa classe foi depreciada na versão 5.7 e removida na 6.0, que é a que acompanha o Spring Boot 3. Desde então, a configuração é um **bean `SecurityFilterChain`**, declarado num método `@Bean` de uma classe `@Configuration`. Se você encontrar a forma antiga, a ideia é a mesma (rotas livres, rotas protegidas, filtros, CSRF); muda só onde a configuração mora e alguns nomes de método. No projeto da turma (Spring Boot 3.3.5), use o bean. Vale o mesmo para as bibliotecas de JWT: `java-jwt` (a da aula, `com.auth0`) e `jjwt` implementam o mesmo padrão com APIs diferentes, e a própria `jjwt` mudou de API entre versões. Ao copiar um exemplo, confira de qual biblioteca e de qual versão ele é.

Se o tutorial que você encontrou estiver na forma antiga, esta é a correspondência:

| Forma antiga | Forma usada na aula |
|---|---|
| `class X extends WebSecurityConfigurerAdapter` + `configure(HttpSecurity http)` | `@Bean SecurityFilterChain filterChain(HttpSecurity http)` que termina em `return http.build();` |
| `.authorizeRequests().antMatchers("/x").permitAll()` | `.authorizeHttpRequests(auth -> auth.requestMatchers("/x").permitAll())` |
| encadear blocos com `.and()` | um bloco por método, com *lambdas* |
| `.csrf().disable()` | `.csrf(csrf -> csrf.disable())` |

---

## 2. Autenticação básica (perfil `basica`)

O projeto `aula08` separa os estados da aula por **perfil do Spring**: sem perfil (só a dependência), `basica` e `jwt` (estado final). Assim você vê cada etapa sem apagar código.

**Passo 1: só a dependência.** Ela está no `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Rode **sem perfil** (`mvn spring-boot:run`) e leia o log: aparece `Using generated security password: ...`. Sem nenhuma linha de código, **todos os endpoints passaram a exigir login**. Abra `http://localhost:8080/alunos/1` no navegador: aparece um formulário de login (usuário `user`, senha do log, que muda a cada execução). Diferente de outros frameworks, no Spring Security o padrão é **fechado**: você escolhe o que abrir.

**Passo 2: usuário e senha definidos.** No perfil `basica` (`src/main/resources/application-basica.yml`):

```yaml
spring:
  security:
    user:
      name: aluno
      password: ada123
```

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=basica
```

No IntelliJ, o mesmo é feito na *Run Configuration*, campo **Active profiles**.

**Passo 3: HTTP Basic.** No Git Bash:

```bash
curl -i -u aluno:ada123 http://localhost:8080/alunos/1
```

O `-u` monta o cabeçalho `Authorization: Basic YWx1bm86YWRhMTIz`. Decodifique:

```bash
echo 'YWx1bm86YWRhMTIz' | base64 -d     # aluno:ada123
```

**Base64 é codificação, não criptografia:** qualquer um que veja o tráfego lê a senha. E também não compacta nada: o texto codificado fica cerca de um terço maior que o original (`aluno:ada123` tem 12 caracteres; o Base64, 16). Por isso a autenticação básica **só é aceitável com HTTPS**.

**Passo 4: rotas livres e protegidas.** É a primeira `SecurityFilterChain` (`security/SecurityBasicConfig.java`):

```java
@Configuration
@EnableWebSecurity
@Profile("basica")
public class SecurityBasicConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/publico/**").permitAll()
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
```

`GET /publico/ola` (`publico/PublicoController.java`) responde sem credenciais; `/alunos/1` continua protegido.

**Passo 5: o primeiro susto.** Um `POST /alunos` **com as credenciais certas** devolve **403 Forbidden**. Não é a senha: é a **proteção contra CSRF**, que o Spring Security deixa ligada por padrão. Requisições que alteram estado precisam de um token CSRF. O teste `postComCredenciaisCorretasAindaDevolve403PorCausaDoCsrf` (`Aula08BasicaTest`) registra esse comportamento. A seção 6 explica quando é aceitável desligar essa proteção.

💭 **Pare e reflita.** O Spring Security fecha tudo ao entrar no projeto e liga a proteção contra CSRF sem ninguém pedir. Que tipo de erro esse padrão evita? E que tipo de trabalho ele cria para quem está começando?

---

## 3. O desenho da autenticação com JWT

Antes do código, o fluxo. É o mesmo do projeto final:

```
1. POST /user           -> cadastra o usuário (a senha é gravada com HASH, BCrypt)
2. POST /authenticate   -> recebe login e senha, valida e DEVOLVE o token JWT
3. GET /alunos (com o token no cabeçalho)
                        -> o filtro valida o token, descobre o usuário e libera
4. GET /alunos (sem token ou com token inválido/expirado)
                        -> 401, a API barra a requisição
```

Quatro decisões de desenho, que vocês vão defender na apresentação:

- **Onde vai o token.** O padrão de mercado é `Authorization: Bearer <token>`; o enunciado do projeto pede o cabeçalho `access_token`. O filtro da aula **aceita os dois**. No projeto, a API precisa aceitar ao menos um, e o README diz qual.
- **A senha nunca vai em texto puro.** Só o **hash BCrypt** vai para o banco. Um *hash* é uma função de mão única: não dá para "desfazer" e obter a senha de volta, diferente da criptografia, que se desfaz com a chave. MD5 e SHA-256 também são funções de hash, mas são **rápidas**, e rapidez ajuda quem tenta adivinhar senhas por força bruta. O BCrypt é **lento de propósito** e gera um **sal** (*salt*) diferente a cada senha: duas pessoas com a mesma senha têm hashes diferentes. Argon2 e PBKDF2 são alternativas do mesmo tipo. Por isso a conferência é sempre com `matches()`, nunca com `equals()`.
- **A chave do JWT vem de variável de ambiente**, nunca do código nem do repositório.
- **Sem sessão (*stateless*).** O servidor não guarda nada entre uma chamada e outra; cada requisição traz o próprio token.

As peças que vamos montar, uma por passo:

| Passo | Peça | Responsabilidade |
|---|---|---|
| 1 | `Usuario`, `UsuarioRepository`, `SecurityBeans` | guardar usuário e hash; `PasswordEncoder` (BCrypt) e `AuthenticationManager` |
| 2 | `UsuarioDetailsService` | ensinar o Spring a buscar o usuário no banco |
| 3 | `TokenService` | gerar e validar o JWT, com a chave da variável de ambiente |
| 4 | `AuthController` | `POST /user` e `POST /authenticate` |
| 5 | `JwtAuthenticationFilter` | a cada requisição, ler o token, validar e dizer ao Spring quem é o usuário |
| 6 | `SecurityConfig` | juntar tudo: rotas livres, sem sessão, 401, filtro na cadeia |

💭 **Pare e reflita.** Se alguém copiar o banco de usuários inteiro da API, o que essa pessoa consegue fazer com hashes BCrypt? E se, em vez disso, ela copiar a variável `JWT_SECRET`?

---

## 4. Autenticação com JWT, passo a passo (perfil `jwt`)

O projeto ganha `spring-boot-starter-data-jpa` (para a entidade de usuário) e a biblioteca `com.auth0:java-jwt` (versão 4.4.0 no `pom.xml`). Todas as classes desta seção têm `@Profile("jwt")`.

### Passo 1: entidade, repositório e hash da senha

O nome da classe é `Usuario` para não conflitar com o `User` do Spring Security (`usuario/Usuario.java`, sem os *getters*):

```java
@Entity
@Table(name = "users")   // "user" é palavra reservada em vários bancos
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;   // guarda o HASH (BCrypt), nunca a senha pura

    protected Usuario() {
    }

    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
    }
```

O repositório usa um método derivado, que o Spring Data transforma numa consulta **parametrizada** (a lição da aula passada continua valendo) (`usuario/UsuarioRepository.java`):

```java
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);
}
```

E os dois beans de apoio (`security/SecurityBeans.java`):

```java
// BCrypt gera um hash diferente a cada chamada (tem um salt embutido); por isso a senha
// é conferida com matches(), nunca com equals().
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

@Bean
AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}
```

### Passo 2: ensinar o Spring a buscar o usuário

`usuario/UsuarioDetailsService.java`:

```java
@Override
public UserDetails loadUserByUsername(String username) {
    Usuario usuario = repository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    return User.withUsername(usuario.getUsername())
            .password(usuario.getPassword())
            .roles("USER")
            .build();
}
```

O `AuthenticationManager` usa este serviço e o `PasswordEncoder` para conferir a senha: você não escreve a comparação.

### Passo 3: gerar e validar o token

A chave fica fora do código (`src/main/resources/application-jwt.yml`):

```yaml
app:
  jwt:
    secret: ${JWT_SECRET}
```

E o serviço do token (`security/TokenService.java`):

```java
private static final String EMISSOR = "api-seguranca";

private final Algorithm algorithm;

// A chave vem de configuração externa (variável de ambiente JWT_SECRET), nunca do código.
public TokenService(@Value("${app.jwt.secret}") String secret) {
    this.algorithm = Algorithm.HMAC256(secret);
}

public String gerar(String username) {
    return JWT.create()
            .withIssuer(EMISSOR)
            .withSubject(username)
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plus(Duration.ofHours(1)))
            .sign(algorithm);
}

public String validar(String token) {
    return JWT.require(algorithm)
            .withIssuer(EMISSOR)
            .build()
            .verify(token)
            .getSubject();
}
```

Ligando com o que vimos de JWT:

- `HMAC256` é o **HS256**: um algoritmo **simétrico**, em que a **mesma chave** assina e confere. Por isso ela é um segredo do servidor. Com RS256 haveria um par de chaves: a privada assina, a pública confere.
- `verify` confere **assinatura, emissor e expiração**; se qualquer uma falhar, lança `JWTVerificationException`.
- O algoritmo usado na verificação é o **esperado** pelo servidor (HMAC256), e não o que o cabeçalho do token declara.
- O *payload* leva só o `sub` (o username), o emissor e as datas. Nenhuma senha, nenhum dado sensível: qualquer pessoa com o token consegue ler o payload.

### Passo 4: `POST /user` e `POST /authenticate`

`auth/AuthController.java` (os campos e o construtor ficaram de fora do trecho):

```java
@PostMapping("/user")
public ResponseEntity<Void> criar(@RequestBody UsuarioRequest request) {
    if (repository.findByUsername(request.username()).isPresent()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já existe");
    }
    // só o HASH da senha vai para o banco
    repository.save(new Usuario(request.username(), passwordEncoder.encode(request.password())));
    return ResponseEntity.status(HttpStatus.CREATED).build();
}

@PostMapping("/authenticate")
public ResponseEntity<TokenResponse> autenticar(@RequestBody UsuarioRequest request) {
    // senha errada ou usuário inexistente: BadCredentialsException (tratada como 401 no handler global)
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));
    return ResponseEntity.ok(new TokenResponse(tokenService.gerar(authentication.getName())));
}
```

`UsuarioRequest` e `TokenResponse` são dois `record`s (`auth/`): `(String username, String password)` e `(String token)`.

**A ligação com a aula passada.** Senha errada faz o `AuthenticationManager` lançar uma `AuthenticationException`. Com o handler genérico de `Exception`, isso viraria **500**. O mesmo valeria para o 409 do usuário duplicado. Por isso o `GlobalExceptionHandler` ganhou dois handlers (`exception/GlobalExceptionHandler.java`, trecho):

```java
// Aula 08: senha errada ou usuário inexistente. A mesma mensagem nos dois casos, para não
// revelar quais usuários existem.
@ExceptionHandler(AuthenticationException.class)
public ResponseEntity<ErroResponse> naoAutenticado(AuthenticationException ex) {
    return resposta(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos.");
}

// Aula 08: ResponseStatusException já traz o status e o motivo (ex.: 409 usuário já existe).
@ExceptionHandler(ResponseStatusException.class)
public ResponseEntity<ErroResponse> comStatus(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
    String mensagem = ex.getReason() != null ? ex.getReason() : "Não foi possível processar a requisição.";
    return ResponseEntity.status(ex.getStatusCode()).body(new ErroResponse(
            ex.getStatusCode().value(),
            status != null ? status.getReasonPhrase() : "Erro",
            mensagem,
            Instant.now()));
}
```

A **mesma mensagem** para "usuário não existe" e "senha errada" é de propósito: com mensagens diferentes, a API ensinaria a um atacante quais usernames existem.

### Passo 5: o filtro que valida o token a cada requisição

`security/JwtAuthenticationFilter.java` (sem os campos e o construtor):

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain chain) throws ServletException, IOException {
    String token = extrairToken(request);
    if (token != null) {
        try {
            String username = tokenService.validar(token);
            UserDetails usuario = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            usuario, null, usuario.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JWTVerificationException | UsernameNotFoundException e) {
            // token inválido, adulterado ou expirado: segue sem autenticar;
            // a rota protegida responderá 401
        }
    }
    chain.doFilter(request, response);
}

private String extrairToken(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Bearer ")) {
        return authorization.substring(7);
    }
    return request.getHeader("access_token");   // cabeçalho pedido no enunciado do projeto
}
```

O filtro **não barra** nada sozinho: ele só preenche o `SecurityContext` quando o token é válido. Quem decide se a rota exige autenticação é a configuração do passo 6. Repare também que o `Bearer ` tem um espaço, e que o `substring(7)` corta exatamente esses 7 caracteres.

### Passo 6: juntar tudo na `SecurityFilterChain`

`security/SecurityConfig.java`:

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // CSRF explora cookies enviados sozinhos pelo navegador. Aqui o token vai em um
        // cabeçalho enviado pelo código do cliente, então desabilitar é aceitável.
        // Se a aplicação usasse sessão com cookie, NÃO desabilite.
        .csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/user", "/authenticate").permitAll()
            .anyRequest().authenticated())
        // sem isso, o Spring Security 6 devolve 403 quando não há autenticação
        .exceptionHandling(e -> e
            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    // Content Security Policy (aula 01), opcional:
    // http.headers(headers -> headers
    //     .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")));

    return http.build();
}
```

| Bloco | O que faz |
|---|---|
| `csrf.disable()` | desliga a proteção contra CSRF (o porquê está na seção 6) |
| `STATELESS` | sem sessão: cada requisição traz o token |
| `permitAll` só em `POST /user` e `POST /authenticate` | todo o resto exige autenticação |
| `HttpStatusEntryPoint(UNAUTHORIZED)` | devolve **401** quando a requisição chega sem autenticação |
| `addFilterBefore` | o filtro do JWT entra na cadeia **antes** do filtro padrão de login |

💭 **Pare e reflita.** O filtro roda **antes** de qualquer controller. Uma exceção lançada ali chega ao seu `@RestControllerAdvice`? O que isso diz sobre onde montar a resposta do 401?

---

## 5. Rodar e testar o fluxo completo

O perfil `jwt` exige a variável `JWT_SECRET` definida **antes** de subir a aplicação, no mesmo terminal. Use uma chave longa e aleatória (o README sugere mais de 32 caracteres).

No PowerShell:

```powershell
$env:JWT_SECRET = "troque-por-uma-chave-longa-e-aleatoria-com-mais-de-32-caracteres"
mvn spring-boot:run "-Dspring-boot.run.profiles=jwt"
```

No Git Bash:

```bash
export JWT_SECRET="troque-por-uma-chave-longa-e-aleatoria-com-mais-de-32-caracteres"
mvn spring-boot:run -Dspring-boot.run.profiles=jwt
```

No IntelliJ: *Run Configuration* com **Active profiles** `jwt` e a variável de ambiente `JWT_SECRET`.

✍️ **Experimente agora.** No Git Bash (no PowerShell e no CMD as aspas mudam; lá, prefira o cliente HTTP do IntelliJ com o mesmo método, URL, cabeçalhos e corpo):

```bash
# 1. cadastrar (201)
curl -i -X POST localhost:8080/user -H "Content-Type: application/json" \
     -d '{"username":"ana","password":"senha123"}'

# 2. autenticar (200 + {"token":"..."})
curl -i -X POST localhost:8080/authenticate -H "Content-Type: application/json" \
     -d '{"username":"ana","password":"senha123"}'

# 3. sem token (401)
curl -i localhost:8080/alunos/1

# 4. com token (200), nos dois formatos de cabeçalho
curl -i localhost:8080/alunos/1 -H "Authorization: Bearer <token>"
curl -i localhost:8080/alunos/1 -H "access_token: <token>"
```

Use `/alunos/1`, e não `/alunos`: o *rate limiter* da lista continua ativo, e a segunda chamada a `GET /alunos` em menos de 10 segundos devolve 429.

Depois, **quebre de propósito**, para ver cada defesa funcionando:

1. Altere **um caractere** da assinatura do token → **401**.
2. Mande uma **senha errada** no `/authenticate` → **401** com `Usuário ou senha inválidos.`
3. Cole o token no jwt.io (ou decodifique o payload em Base64): ele é legível, e a **senha não está lá**.

Os testes do projeto (`mvn test`, 17 testes) cobrem esses casos e mais alguns. Do `Aula08JwtTest`:

| Teste | O que prova |
|---|---|
| `cadastroGravaASenhaComoHashBCrypt` | a coluna `password` guarda algo que começa com `$2`, não a senha |
| `usernameDuplicadoDevolve409` | cadastro repetido → 409 com `Usuário já existe` |
| `senhaErradaEUsuarioInexistenteDevolvemO401ComAMesmaMensagem` | os dois casos respondem `Usuário ou senha inválidos.` |
| `semTokenDevolve401` | rota protegida sem token → 401 |
| `comTokenNoCabecalhoAuthorizationBearerDevolve200` e `comTokenNoCabecalhoAccessTokenDevolve200` | os dois cabeçalhos funcionam |
| `tokenAdulteradoDevolve401`, `tokenExpiradoDevolve401`, `tokenAssinadoComOutraChaveDevolve401` | assinatura, expiração e chave são conferidas |
| `postComTokenFuncionaPoisOCsrfFoiDesabilitado` | o `POST /alunos` que dava 403 no perfil `basica` agora dá 201 |

---

## 6. Além da autenticação: CSRF e cabeçalhos

### Por que desabilitar o CSRF aqui é aceitável

No **CSRF** (*Cross-Site Request Forgery*), uma página maliciosa faz o **navegador da vítima** enviar um pedido ao site atacado, e o navegador **anexa sozinho** o cookie de sessão que já guarda para aquele site. O atacante **não lê** o cookie: ele não precisa, porque o navegador o envia por conta própria. Ler o cookie é outro ataque, o XSS (e é contra ele que serve o atributo `HttpOnly`). Contra o CSRF, além do token CSRF, existe o atributo `SameSite` do cookie, que restringe o envio dele em pedidos vindos de outro site (com `Strict`, nunca envia; com `Lax`, só em navegação comum, como clicar num link).

Na nossa API não há cookie de sessão. O token vai num **cabeçalho montado pelo código do cliente** a cada chamada (`Authorization` ou `access_token`); o navegador não tem nada para anexar sozinho a um pedido forjado por outra página. Sem essa credencial automática, o ataque deixa de existir, e desligar a proteção é uma **decisão justificada**, não um descuido.

A regra vale nos dois sentidos: **se a aplicação usar sessão com cookie (ou guardar o token num cookie), não desabilite.** Desligar o CSRF é aceitável por causa do **desenho** (*stateless*, token em cabeçalho), e é essa justificativa que o projeto pede no README.

### Cabeçalhos de segurança

O Spring Security já acrescenta cabeçalhos de segurança às respostas por padrão. A **Content Security Policy**, que reduz o estrago de um XSS no navegador, pode ser ligada na mesma `SecurityFilterChain`; o bloco está comentado no `SecurityConfig` e é um dos extras do projeto (seção 15 da especificação).

### Autorização: o próximo degrau

Hoje todo usuário autenticado tem o papel `USER` e pode tudo. Controlar **o que cada um pode fazer** por papel usa `@EnableMethodSecurity` na configuração e `@PreAuthorize("hasRole('ADMIN')")` nos métodos: é o UC7 do projeto, opcional. Um cuidado: `@PreAuthorize` lança `AccessDeniedException`, e um handler genérico de `Exception` a transformaria em 500 em vez de 403. Quem fizer esse extra trata essa exceção explicitamente.

💭 **Pare e reflita.** Se amanhã o front-end da API de Chamados decidir guardar o token num cookie, para não lidar com ele no JavaScript, o que muda na decisão sobre o CSRF?

---

## 7. A regra de propriedade: 404 para o chamado de outra pessoa

A autenticação responde "quem é você". O projeto pede mais: **cada pessoa só enxerga os chamados que ela mesma abriu** (seção 03 da especificação). Isso é autorização baseada no **dado**, e não no papel: todos os usuários têm o mesmo papel, então nenhuma regra na `SecurityFilterChain` resolve.

Três regras, todas da especificação:

- **A identidade vem do token, nunca do corpo** (seções 06 e 11). Depois do filtro, o usuário da requisição está no `SecurityContext`; no serviço, `SecurityContextHolder.getContext().getAuthentication().getName()` devolve o username. Um campo `usuarioId` no JSON de entrada é um convite para abrir chamado em nome de outra pessoa.
- **Buscar já filtrando pelo dono** (seção 06): o chamado é buscado pelo **id e pelo usuário**, nunca só pelo id. Buscar só pelo id e comparar o dono depois é o ponto em que mais se erra (e esquecer a comparação é fácil).
- **404, e não 403**, para o chamado de outra pessoa (UC5 e UC6): a resposta é a mesma de um chamado que não existe. Um 403 confirmaria que aquele id existe e pertence a alguém.

💭 **Pare e reflita.** Um atacante autenticado testa `GET /chamados/1`, `/2`, `/3`... Com 403 para os chamados alheios, o que ele aprende? E com 404?

---

## 8. Erros comuns (e o que significam)

| Sintoma | Causa provável | O que fazer |
|---|---|---|
| `403` num `POST` com a senha certa (perfil `basica`) | proteção contra CSRF ligada (padrão) | é o comportamento esperado; numa API *stateless* com token em cabeçalho, desabilite e justifique |
| `403` em vez de `401` quando falta o token | falta o *entry point* de autenticação | `exceptionHandling(...)` com `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` |
| A aplicação não sobe: `Could not resolve placeholder 'JWT_SECRET'` | variável não definida no terminal ou na *Run Configuration* que roda a aplicação | defina `JWT_SECRET` antes do `mvn spring-boot:run`; no PowerShell, ela vale só para aquele terminal |
| Erro de SQL ao criar a tabela de usuários | tabela chamada `user`, palavra reservada em vários bancos (inclusive no H2) | `@Table(name = "users")` ou `usuarios` |
| `There is no PasswordEncoder mapped for the id "null"` | não há bean `PasswordEncoder`; o Spring usa um codificador que espera um prefixo como `{bcrypt}` antes do hash e encontra uma senha gravada sem ele (ou em texto puro) | declare o `BCryptPasswordEncoder` como bean e grave a senha com `encode()` |
| `500` ao errar a senha no `/authenticate` | o handler genérico de `Exception` capturou a `AuthenticationException` | handler próprio para `AuthenticationException` → 401 |
| Token correto, mas `401` | cabeçalho mal formado (`Bearer` sem o espaço, nome errado) ou token expirado | confira o cabeçalho; gere um token novo |
| `429` em `GET /alunos` | o *rate limiter* da lista | teste com `/alunos/1` |

---

## 9. Para o projeto

Esta aula é a linha "Aula 08" do cronograma (seção 12 da especificação): **cadastro, autenticação e filtro**, depois a proteção das rotas e a regra de propriedade. É o caminho A da seção 11, o esperado. A próxima aula é laboratório do projeto, então cheguem a ela com o fluxo pelo menos começando a funcionar.

O que cada grupo leva do `aula08` para a API de Chamados, apontando a seção da especificação:

| No projeto | Seção | Atenção ao que muda em relação à aula |
|---|---|---|
| Pacote de segurança: `SecurityFilterChain`, `TokenService`, filtro | 04 e 06 | só `POST /user` e `POST /authenticate` abertos; todo o resto exige token |
| `POST /user` (UC1) | 05, 07 e 08 | a entidade tem `nome` e `criadoEm`; a resposta é 201 **com** `id`, `nome` e `username` (sem senha nem hash), e não um corpo vazio; o 409 usa a mensagem `Nome de usuário já está em uso.` |
| `POST /authenticate` (UC2) | 05 e 09 | 401 com `Usuário ou senha inválidos.` nos dois casos |
| 401 sem token ou com token inválido | 09 | o `HttpStatusEntryPoint` só define o status, com corpo vazio; a seção 09 pede o JSON padronizado com `Não autenticado.` Como o filtro roda antes dos controllers, essa resposta se monta num `AuthenticationEntryPoint` próprio, e não no `@RestControllerAdvice` |
| Validação do cadastro | 08 | `username` de 3 a 30 caracteres (letras, números, ponto e sublinhado); senha com no mínimo 8 |
| Regra de propriedade (UC3 a UC6) | 03, 05 e 06 | dono vem do token; busca por id **e** usuário; 404 para o chamado alheio |
| Cabeçalho do token | 07 | aceitar ao menos um de `access_token` e `Authorization: Bearer`, e declarar no README |

No **mapa de ameaças** (seção 10), estas linhas passam a ter defesa no código. Cada uma precisa de uma frase no README:

| Linha do mapa | O que a equipe aponta |
|---|---|
| CSRF | por que desabilitar é aceitável **nesta** API (token em cabeçalho, sem cookie de sessão) |
| Token roubado | o prazo de expiração escolhido, o que há no payload e o que um atacante faz com o token, e por quanto tempo |
| Chave do JWT exposta | onde está a chave (variável de ambiente) e como rodar o projeto com ela |
| Senha vazada | hash BCrypt; senha e hash nunca em resposta nem em log |
| Acesso a dado de outro usuário | a consulta que filtra pelo dono e o 404 nos dois casos |
| Enumeração de usuários | a mesma mensagem para usuário inexistente e senha errada |

E as **evidências** (seção 13): o roteiro de requisições do grupo mostra cadastrar → autenticar → sem token (401) → com token (200) → chamado de **outro** usuário (404). Para o último passo, cadastre dois usuários e tente abrir, com o token do segundo, um chamado do primeiro.

💭 **Pare e reflita.** Na apresentação, qualquer integrante pode ouvir "o que está no payload do seu token?" ou "o que acontece se o chamado for de outro usuário?" (seção 14). Quem do grupo ainda não leu o filtro e a `SecurityFilterChain` do projeto?

---

## Para praticar

- **Lista de exercícios de JWT no LMS** (exercícios de fixação desta aula).
- **Projeto:** levar o fluxo da seção 9 para a API de Chamados (cadastro, autenticação, filtro, rotas protegidas e regra de propriedade).
- **Pesquisa individual:** por que senhas devem ser guardadas com hash e sal (BCrypt) e nunca em texto puro ou com um hash rápido como SHA-256.
- **Leitura:** documentação do Spring Security, "Getting Spring Security": https://docs.spring.io/spring-security/reference/getting-spring-security.html

> **Na próxima aula:** laboratório do projeto. Tragam a API de Chamados com o fluxo cadastrar → autenticar → 401 → 200 → 404 o mais adiantado possível, e o mapa de ameaças começado no README.
