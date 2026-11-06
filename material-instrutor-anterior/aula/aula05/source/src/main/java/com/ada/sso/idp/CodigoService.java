package com.ada.sso.idp;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Emite e resgata os CÓDIGOS de autorização (a mesma ideia do fluxo da aula 04).
 *
 * Por que um código e não a asserção direto na URL? Tudo que passa pela barra de endereço fica
 * no histórico do navegador, em logs e no cabeçalho Referer. O código é inútil sozinho: vale
 * 60 segundos, UMA vez, e só para a aplicação certa, que ainda precisa provar quem é (segredo)
 * em uma chamada direta de servidor a servidor. A asserção de verdade nunca passa pelo navegador.
 */
@Service
@Profile("idp")
public class CodigoService {

    private static final Duration VALIDADE = Duration.ofSeconds(60);

    private final SecureRandom aleatorio = new SecureRandom();
    // código -> dados de quem o pediu. Na memória: em vários servidores seria um Redis.
    private final Map<String, Codigo> codigos = new ConcurrentHashMap<>();

    /** Emite um código de uso único para este usuário, destinado a esta aplicação. */
    public String emitir(UsuarioIdp usuario, String appId) {
        removerExpirados();
        byte[] bytes = new byte[32];            // 256 bits aleatórios: impossível de adivinhar
        aleatorio.nextBytes(bytes);
        String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        codigos.put(codigo, new Codigo(usuario.getUsername(), usuario.getNome(), usuario.getEmail(),
                usuario.getPapel(), appId, Instant.now().plus(VALIDADE)));
        return codigo;
    }

    /**
     * Resgata o código. remove() é atômico: se duas requisições tentarem usar o MESMO código ao
     * mesmo tempo, só uma recebe o resultado. Por isso o código vale uma vez só.
     * Devolve vazio se o código não existe, já foi usado, expirou ou é de outra aplicação.
     */
    public Optional<Codigo> resgatar(String codigo, String appId) {
        if (codigo == null) {
            return Optional.empty();
        }
        Codigo encontrado = codigos.remove(codigo);
        if (encontrado == null || encontrado.expirou() || !encontrado.appId().equals(appId)) {
            return Optional.empty();
        }
        return Optional.of(encontrado);
    }

    private void removerExpirados() {
        codigos.values().removeIf(Codigo::expirou);
    }
}
