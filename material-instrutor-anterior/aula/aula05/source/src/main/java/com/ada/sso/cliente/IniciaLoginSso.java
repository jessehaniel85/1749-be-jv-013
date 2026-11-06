package com.ada.sso.cliente;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * PASSO 2 do fluxo: "a aplicação percebe que o usuário não está autenticado e o REDIRECIONA
 * ao provedor de identidade".
 *
 * O Spring Security chama esta classe quando alguém sem sessão local acessa uma rota protegida.
 * Em vez de mostrar um formulário de login (esta aplicação NÃO tem senha), ela manda o navegador
 * para o provedor. Antes disso o Spring já guardou a URL que o usuário queria, para voltar a ela.
 */
@Component
@Profile({"notas", "biblioteca"})
public class IniciaLoginSso implements AuthenticationEntryPoint {

    /** Nome do atributo da sessão local com os "state" ainda pendentes. */
    public static final String ESTADOS_PENDENTES = "sso.estados";
    private static final int MAXIMO_DE_ESTADOS = 5;

    private static final Logger log = LoggerFactory.getLogger(IniciaLoginSso.class);

    private final ClienteProperties props;
    private final SecureRandom aleatorio = new SecureRandom();

    public IniciaLoginSso(ClienteProperties props) {
        this.props = props;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        // O "state": um valor aleatório que esta aplicação gera, guarda na SUA sessão e manda ao
        // provedor. Ele volta intacto na URL de retorno, e só aceitamos o retorno se bater.
        // Isso impede que um atacante faça a vítima "entrar" com um código que ele mesmo obteve
        // (CSRF no login, aula 01 e 04). Guardamos um conjunto pequeno para suportar duas abas abertas.
        byte[] bytes = new byte[24];
        aleatorio.nextBytes(bytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        HttpSession sessao = request.getSession();   // cria a sessão local (e o cookie) se ainda não existir
        Set<String> pendentes = (Set<String>) sessao.getAttribute(ESTADOS_PENDENTES);
        if (pendentes == null) {
            pendentes = new LinkedHashSet<>();
        }
        if (pendentes.size() >= MAXIMO_DE_ESTADOS) {
            pendentes.remove(pendentes.iterator().next());   // descarta o mais antigo
        }
        pendentes.add(state);
        sessao.setAttribute(ESTADOS_PENDENTES, pendentes);

        String destino = UriComponentsBuilder.fromUriString(props.idpUrlPublica() + "/sso/autorizar")
                .queryParam("app", props.id())
                .queryParam("state", state)
                .build().toUriString();
        log.info("[{}] sem sessão local: redirecionando para o provedor de identidade", props.id());
        response.sendRedirect(destino);
    }
}
