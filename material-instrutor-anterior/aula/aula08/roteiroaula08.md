# Aula 08: Spring Security (autenticação básica e JWT)

**Roteiro de Aula · Módulo Fundamentos de segurança para aplicações (BE-JV-013)**

A aula que junta tudo. A turma conhece o Spring Security, protege uma API com autenticação básica e depois constrói, passo a passo, a **autenticação com JWT**: cadastro de usuários (`POST /user`), autenticação (`POST /authenticate`) e proteção dos demais endpoints. É a base do projeto do módulo.

**Data:** 16/11/2026 · segunda &nbsp;·&nbsp; **Duração:** 3h &nbsp;·&nbsp; **Formato:** expositiva + codificação ao vivo + prática guiada

**Código:** [`source/`](source/README.md) · **Ponto de partida:** estado final da Aula 07 (`../aula07/source`) · **Próxima aula:** Aula 09, sem código novo; usa este projeto como referência.

> **Problema gerador:** Spring Security: como implementar autenticação com JWT na API?

---

## Sumário

1. [Visão geral](#01--visão-geral)
2. [Retomada da aula 07](#02--retomada-da-aula-07)
3. [Introdução ao Spring Security](#03--introdução-ao-spring-security)
4. [Autenticação básica](#04--autenticação-básica)
5. [O desenho da autenticação com JWT](#05--o-desenho-da-autenticação-com-jwt)
6. [Autenticação com JWT: codificação ao vivo](#06--autenticação-com-jwt-codificação-ao-vivo)
7. [Spring Security além da autenticação](#07--spring-security-além-da-autenticação)
8. [Encerramento](#08--encerramento)
9. [Dicas de condução](#09--dicas-de-condução)

---

## 01 · Visão geral

Tempos relativos ao início da aula, ajuste ao seu horário real. É a aula mais densa do módulo, com muito código. **Prepare o projeto final e uma versão intermediária antes** (veja a seção 09): se a codificação ao vivo atrasar, você retoma de um ponto conhecido em vez de deixar a turma perdida.

| Hora | Duração | Bloco |
|---|---|---|
| 00:00 | 10 min | **Retomada** da aula 07 e da atividade de casa |
| 00:10 | 20 min | **Teoria**: o que é o Spring Security e como ele funciona |
| 00:30 | 35 min | **Codificação ao vivo**: autenticação básica |
| 01:05 | 10 min | Intervalo |
| 01:15 | 20 min | **Teoria**: o desenho da autenticação com JWT |
| 01:35 | 50 min | **Codificação ao vivo**: autenticação com JWT |
| 02:25 | 15 min | **Teoria**: Spring Security além da autenticação |
| 02:40 | 20 min | **Encerramento**: exercícios, recap, atividade de casa |

## 02 · Retomada da aula 07

*~10 min*

- "Como evitar o SQL Injection?" (Consulta parametrizada.)
- "O que **nunca** deve aparecer na resposta de erro?" (Stack trace, SQL, detalhes internos.)
- "Qual a vantagem do tratamento global de exceções?" (Um lugar só, resposta padronizada.)

Peça a 2 duplas que mostrem o handler global que fizeram em casa. Cinco minutos no máximo.

**Gancho:** "Hoje a API deixa de ser aberta. A pergunta que ela vai fazer a cada requisição é a de toda a segurança do módulo: **quem é você e o que você pode fazer?**"

## 03 · Introdução ao Spring Security

*~20 min*

> O **Spring Security** é uma estrutura poderosa e altamente personalizável de **autenticação** e **controle de acesso**. É o padrão de fato para proteger aplicações baseadas em Spring.

### Autenticação x autorização

| | Pergunta | Exemplo |
|---|---|---|
| **Autenticação** | Quem é você? | Login e senha, ou um token válido. |
| **Autorização** | O que você pode fazer? | Só administradores apagam alunos. |

### Características

- Suporte abrangente e extensível para **autenticação e autorização**.
- Proteção contra ataques como **fixação de sessão, *clickjacking* e CSRF**.
- Integração com a API de servlet e, opcionalmente, com o Spring Web MVC.
- Muito mais.

### Como funciona, em uma ideia

O Spring Security se coloca **na frente** dos controllers, como uma **cadeia de filtros**. Toda requisição passa pela cadeia antes de chegar ao seu código:

```
Requisição  ->  [ filtro 1 ] -> [ filtro 2 ] -> ... -> [ filtro de autenticação ] -> Controller
                                                          |
                                  Se não autenticado: a requisição é barrada (401/403)
```

Os componentes que a turma precisa conhecer hoje:

| Componente | Para que serve |
|---|---|
| **`SecurityFilterChain`** | Define as regras: quais rotas são livres, quais exigem autenticação, quais filtros entram na cadeia. |
| **`UserDetailsService`** | Sabe **buscar o usuário** (por exemplo, no banco) pelo nome. |
| **`PasswordEncoder`** | Faz o **hash** das senhas e confere (BCrypt). |
| **`AuthenticationManager`** | Recebe as credenciais e as valida usando os componentes acima. |
| **`SecurityContext`** | Guarda **quem é o usuário autenticado** na requisição atual. |

> **Ponto de atenção:** o material de apoio usa a classe `WebSecurityConfigurerAdapter` para configurar o Spring Security. Ela foi **removida no Spring Security 6** (Spring Boot 3). Nesta aula, a configuração é feita por um **bean `SecurityFilterChain`**. Se um aluno trouxer um tutorial com `extends WebSecurityConfigurerAdapter`, é por isso que não compila.

## 04 · Autenticação básica

*~35 min*

Use o projeto da **API de alunos** (a mesma das aulas 01 e 07).

### Passo 1: adicionar a dependência e rodar

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Rode a aplicação e peça que a turma **leia o log**: aparece uma linha parecida com `Using generated security password: ...`.

**Sem escrever nenhuma linha de código**, a API já mudou: **todos os endpoints estão protegidos**. Acesse `http://localhost:8080/alunos` no navegador: aparece um **formulário de login**. O usuário padrão é `user` e a senha é a gerada no log (muda a cada execução).

### Passo 2: definir o usuário e a senha

```yaml
# application.yml
spring:
  security:
    user:
      name: aluno
      password: ada123
```

Reinicie: agora a senha **não é mais aleatória** e o login funciona com as credenciais definidas.

### Passo 3: autenticação básica (HTTP Basic) na linha de comando

Em um cliente de API (Postman) ou no `curl`:

```bash
curl -i -u aluno:ada123 http://localhost:8080/alunos
```

Mostre o que o `-u` faz: envia o cabeçalho `Authorization: Basic YWx1bm86YWRhMTIz`. Decodifique o texto na frente da turma:

```bash
echo 'YWx1bm86YWRhMTIz' | base64 -d     # aluno:ada123
```

> **Ponto de atenção:** a "autenticação básica" é só `usuário:senha` em **Base64**, que é codificação, não criptografia (você viu isso na aula 03). Por isso ela **só pode ser usada com HTTPS** (aula 02), senão a senha viaja em claro.

### Passo 4: escolher as rotas protegidas e as livres

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

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

Crie um `GET /publico/ola` simples e teste: responde **sem** credenciais, enquanto `/alunos` continua protegido.

### Passo 5: o primeiro "susto" com POST

Faça um `POST /alunos` com as credenciais corretas. A resposta é **403 Forbidden**, mesmo com a senha certa.

**Pergunte à turma por quê.** A resposta é a **proteção CSRF**, que o Spring Security deixa **ligada por padrão** (aula 01): requisições que alteram estado precisam de um token CSRF. Deixe a explicação para a seção 07, mas registre o ponto: é o primeiro erro que todo mundo encontra.

## 05 · O desenho da autenticação com JWT

*~20 min*

Antes de escrever o código, desenhe o **fluxo** no quadro. Este é o fluxo do projeto do módulo:

```
1. POST /user           -> cadastra o usuário (a senha é gravada com HASH, BCrypt)
2. POST /authenticate   -> recebe login e senha, valida e DEVOLVE o token JWT
3. GET /alunos (com o token no cabeçalho)
                        -> o filtro valida o token, descobre o usuário e libera
4. GET /alunos (sem token ou com token inválido/expirado)
                        -> 401, a API barra a requisição
```

### As peças que vamos construir

| Peça | Responsabilidade |
|---|---|
| **Entidade `Usuario`** e **`UsuarioRepository`** | Guardar usuário e senha **(hash)** no banco. |
| **`PasswordEncoder` (BCrypt)** | Fazer o hash da senha ao cadastrar e conferir no login. |
| **`UsuarioDetailsService`** | Ensinar o Spring a buscar o usuário no banco. |
| **`TokenService`** | **Gerar** e **validar** o JWT, com a chave lida de variável de ambiente. |
| **`AuthController`** | Os endpoints `POST /user` e `POST /authenticate`. |
| **`JwtAuthenticationFilter`** | A cada requisição, lê o token, valida e diz ao Spring quem é o usuário. |
| **`SecurityConfig`** | Monta as regras: quais rotas são livres, sem sessão, com o filtro na cadeia. |

### Decisões a tomar com a turma

- **Cabeçalho do token:** o enunciado do projeto pede o token no cabeçalho **`access_token`**; o padrão de mercado é **`Authorization: Bearer <token>`**. Mostre o padrão de mercado e implemente o filtro de modo a **aceitar os dois**, como no código abaixo. Combine com a turma qual será usado na entrega.
- **Senha:** **nunca** em texto puro. Só o **hash BCrypt** vai para o banco. (Pesquisa de casa: por que BCrypt e não SHA-256 simples?)
- **Chave do JWT:** vem de **variável de ambiente**, nunca do código (aula 03).
- **Sem sessão:** a API é *stateless*: cada requisição traz o token.

## 06 · Autenticação com JWT: codificação ao vivo

*~50 min*

Projeto Spring Boot 3 (Java 17 ou superior) com `starter-web`, `starter-data-jpa`, `starter-security`, H2 e a biblioteca de JWT **`com.auth0:java-jwt`** (versão 4.x). Tudo isso já está em [`source/`](source/README.md), que parte do estado final da aula 07; rode com o perfil `jwt` e a variável `JWT_SECRET`. Use um nome de classe de entidade que **não conflite** com o `User` do Spring Security: neste roteiro, `Usuario`.

### Passo 1: entidade, repositório e hash da senha (~8 min)

```java
@Entity
@Table(name = "users")   // "user" é palavra reservada em vários bancos
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;   // guarda o HASH, nunca a senha pura

    protected Usuario() {}
    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
    }
    // getters
}
```

```java
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}
```

```java
@Configuration
public class SecurityBeans {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

> **Ponto de atenção:** BCrypt gera um hash **diferente a cada chamada** (tem um *salt* embutido). Mostre isso no console: `encode("123")` duas vezes dá textos diferentes, e `matches` confere os dois. É por isso que não se compara senha com `equals`.

### Passo 2: ensinar o Spring a buscar o usuário (~5 min)

```java
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repository;

    public UsuarioDetailsService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario usuario = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return org.springframework.security.core.userdetails.User
                .withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .roles("USER")
                .build();
    }
}
```

### Passo 3: gerar e validar o token (~10 min)

```yaml
# application.yml
app:
  jwt:
    secret: ${JWT_SECRET}
```

Defina a variável de ambiente na execução (na IDE: *Run Configuration > Environment variables*). Use uma chave longa e aleatória.

```java
@Service
public class TokenService {

    private static final String EMISSOR = "api-seguranca";
    private final Algorithm algorithm;

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

    /** Devolve o usuário (subject) se o token é válido; lança JWTVerificationException se não for. */
    public String validar(String token) {
        return JWT.require(algorithm)
                .withIssuer(EMISSOR)
                .build()
                .verify(token)
                .getSubject();
    }
}
```

Relacione com a aula 03: `sign` = a **assinatura**; `verify` confere **assinatura, emissor e expiração**; o algoritmo é o **esperado** (HMAC256), não o do header do token.

### Passo 4: os endpoints `POST /user` e `POST /authenticate` (~10 min)

```java
public record UsuarioRequest(String username, String password) {}
public record TokenResponse(String token) {}
```

```java
@RestController
public class AuthController {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    // construtor com os quatro campos

    @PostMapping("/user")
    public ResponseEntity<Void> criar(@RequestBody UsuarioRequest request) {
        if (repository.findByUsername(request.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já existe");
        }
        repository.save(new Usuario(request.username(),
                passwordEncoder.encode(request.password())));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<TokenResponse> autenticar(@RequestBody UsuarioRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return ResponseEntity.ok(new TokenResponse(tokenService.gerar(authentication.getName())));
    }
}
```

> **Ligação com a aula 07:** se a senha estiver errada, o `AuthenticationManager` lança `BadCredentialsException`. Com o handler global da aula 07, isso vira **500**. Acrescente um `@ExceptionHandler(AuthenticationException.class)` que devolve **401** (com a mesma mensagem para usuário inexistente e senha errada). O mesmo vale para a `ResponseStatusException` do `POST /user` (409): sem um handler próprio, o `Exception.class` genérico a transformaria em 500. Os dois handlers já estão no `GlobalExceptionHandler` de [`source/`](source/README.md).

### Passo 5: o filtro que valida o token a cada requisição (~10 min)

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioDetailsService userDetailsService;

    // construtor com os dois campos

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
                // token inválido ou expirado: segue sem autenticar; a rota protegida responderá 401
            }
        }
        chain.doFilter(request, response);
    }

    private String extrairToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return request.getHeader("access_token");   // cabeçalho pedido no projeto
    }
}
```

### Passo 6: juntar tudo na configuração (~7 min)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/user", "/authenticate").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

O que cada bloco faz:

- **`csrf.disable()`:** explicado na seção 07 (API stateless com token no cabeçalho).
- **`STATELESS`:** sem sessão; cada requisição traz o token.
- **`permitAll` só em `POST /user` e `POST /authenticate`:** o resto exige autenticação.
- **`HttpStatusEntryPoint(UNAUTHORIZED)`:** devolve **401** quando não há autenticação (sem isso, o Spring Security 6 devolve 403).
- **`addFilterBefore`:** o filtro do JWT entra **antes** do filtro padrão de login.

### O teste completo, ao vivo

No Postman (ou `curl` em um terminal Unix/Git Bash):

```bash
# 1. cadastrar
curl -i -X POST localhost:8080/user -H "Content-Type: application/json" \
     -d '{"username":"ana","password":"senha123"}'                       # 201

# 2. autenticar e copiar o token
curl -i -X POST localhost:8080/authenticate -H "Content-Type: application/json" \
     -d '{"username":"ana","password":"senha123"}'                       # 200 + {"token":"..."}

# 3. acessar sem token
curl -i localhost:8080/alunos                                            # 401

# 4. acessar com token
curl -i localhost:8080/alunos -H "Authorization: Bearer <token>"          # 200
curl -i localhost:8080/alunos -H "access_token: <token>"                  # 200
```

Em seguida, **quebre de propósito**, para a turma ver cada defesa funcionando:

1. Altere **um caractere** do token → **401** (assinatura inválida, aula 03).
2. Mande uma **senha errada** no `/authenticate` → **401**.
3. Olhe a tabela no banco: a coluna `password` guarda um **hash**, não a senha.
4. Cole o token no **jwt.io**: o payload é legível, e a **senha não está lá**.

## 07 · Spring Security além da autenticação

*~15 min*

O Spring Security faz mais do que autenticar: ele também lida com os assuntos das primeiras aulas.

### CSRF

A proteção CSRF vem **ligada por padrão**. Para uma aplicação ficar vulnerável, alguém precisa **desabilitá-la explicitamente**, como o material mostra.

**Por que podemos desabilitar neste projeto?** O CSRF explora o fato de o navegador anexar **cookies** automaticamente. Em uma API *stateless* em que o token vai em um **cabeçalho enviado pelo próprio código do cliente** (`Authorization` ou `access_token`), o navegador **não anexa nada sozinho**, e o ataque deixa de existir. **Se a sua aplicação usar sessão com cookie, não desabilite.**

### XSS e cabeçalhos de segurança

O Spring Security adiciona **cabeçalhos de segurança** por padrão, e permite configurar a **Content Security Policy** (aula 01):

```java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")));
```

### Cookies

O `@CookieValue` (aula 02) continua funcionando normalmente.

### Autorização: o próximo passo

Autenticar responde "quem é você". Para controlar **o que cada um pode fazer**, o Spring Security usa **perfis** e anotações nos métodos:

```java
@EnableMethodSecurity   // na configuração

@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public void apaga(@PathVariable Long id) { ... }
```

Fica como **extensão do projeto**, para quem quiser ir além.

> **Ponto de atenção:** `@PreAuthorize` lança `AccessDeniedException`. Se o handler global tiver um `Exception.class`, ele vai **capturar essa exceção** e devolver 500 em vez de 403 (aula 07). Quando for usar autorização por método, trate-a explicitamente.

## 08 · Encerramento

*~20 min*

**Exercícios de fixação (JWT):** use os primeiros 5 minutos para a turma começar a lista; o que sobrar é atividade de casa.

Recap de 3 minutos, **feito pela turma**: a diferença entre autenticação e autorização; o que a dependência faz sozinha; o que é a `SecurityFilterChain`; o caminho de uma requisição com JWT (cadastro, autenticação, token, filtro); por que a senha vai como hash; por que desabilitamos o CSRF aqui.

Feche amarrando com o módulo: "Vocês construíram **a camada de autenticação do projeto**. Na próxima aula é dia de **desenvolver o projeto** e apresentar. O que vocês viram do HTTPS ao JWT está todo aqui".

**Atividade para casa:**
- **Projeto:** concluir em casa o que não foi finalizado em aula e **começar o projeto do módulo**: escolher a API (nova ou existente), modelar a entidade de usuário e implementar o `POST /user`.
- **Leitura:** a documentação do Spring Security: https://docs.spring.io/spring-security/reference/getting-spring-security.html
- **Pesquisa individual:** por que as senhas devem ser guardadas com **hash e salt** (BCrypt) e nunca em texto puro.
- **Exercícios de fixação:** o que sobrou da lista (JWT).

**Preview da Aula 09:** desenvolvimento do projeto, apresentação e devolutiva da avaliação por rubrica.

## 09 · Dicas de condução

- **Prepare três versões do projeto:** (1) o ponto de partida, com a API de alunos sem segurança; (2) o ponto intermediário, com entidade, repositório e `PasswordEncoder` prontos; (3) o resultado final, que funciona de ponta a ponta. Se o ao vivo atrasar, **pule para a versão seguinte** em vez de deixar a turma perdida. Compartilhe as três com os alunos.
- **Não codifique linha a linha todo o boilerplate:** construtores, getters e records podem estar prontos. O que precisa ser feito **na frente da turma** é o `TokenService`, o filtro e a `SecurityConfig`, porque são onde a segurança acontece.
- **Variável de ambiente `JWT_SECRET`:** configure na IDE de todos antes da aula; é a causa mais comum de a aplicação não subir ("Could not resolve placeholder").
- **Comandos `curl`:** em Windows, as aspas e as quebras de linha mudam entre CMD, PowerShell e Git Bash. Prefira o **Postman** ou uma coleção pronta, e use o `curl` só se o terminal for Git Bash.
- **Erros comuns para antecipar:** 403 no POST (CSRF); 403 em vez de 401 (falta o `HttpStatusEntryPoint`); "There is no PasswordEncoder mapped" (senha salva sem hash); tabela `user` (palavra reservada); filtro executando e usuário não encontrado.
- **O material de apoio está defasado** neste ponto (`WebSecurityConfigurerAdapter`). Diga isso explicitamente e use o bean `SecurityFilterChain`.
- **Itens do planejamento cobertos:** introdução ao Spring Security, implementação de autenticação básica, implementação de autenticação com JWT, estudo de caso "Como implementar autenticação com JWT na API?" e os exercícios de fixação (JWT).

---

*Roteiro de aula: módulo de Fundamentos de segurança para aplicações. Aula 08 de 09.*
