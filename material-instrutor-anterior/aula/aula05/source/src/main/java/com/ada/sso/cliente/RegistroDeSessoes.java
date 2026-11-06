package com.ada.sso.cliente;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lembra QUAIS sessões locais pertencem a cada usuário, para o logout único conseguir
 * encerrá-las quando o provedor avisar "o usuário ana saiu".
 *
 * O servidor de aplicação guarda as sessões pelo ID do cookie, mas não sabe procurar "todas as
 * sessões da ana". Este registro faz esse índice (usuário -> sessões). Está na memória: com
 * vários servidores da mesma aplicação seria preciso um armazenamento compartilhado (Redis).
 */
@Component
@Profile({"notas", "biblioteca"})
public class RegistroDeSessoes implements HttpSessionListener {

    private static final Logger log = LoggerFactory.getLogger(RegistroDeSessoes.class);

    private final Map<String, Set<HttpSession>> sessoesPorUsuario = new ConcurrentHashMap<>();

    public void registrar(String usuario, HttpSession sessao) {
        sessoesPorUsuario.computeIfAbsent(usuario, u -> ConcurrentHashMap.newKeySet()).add(sessao);
    }

    /** Encerra todas as sessões do usuário. Devolve quantas foram encerradas. */
    public int encerrarSessoesDe(String usuario) {
        Set<HttpSession> sessoes = sessoesPorUsuario.remove(usuario);
        if (sessoes == null) {
            return 0;
        }
        int encerradas = 0;
        for (HttpSession sessao : List.copyOf(sessoes)) {
            try {
                sessao.invalidate();
                encerradas++;
            } catch (IllegalStateException jaInvalidada) {
                // a sessão já tinha expirado ou sido encerrada: nada a fazer
            }
        }
        log.info("[App] logout único: {} sessão(ões) de '{}' encerrada(s)", encerradas, usuario);
        return encerradas;
    }

    // Quando uma sessão termina por qualquer motivo (logout local, expiração), sai do índice.
    // Sem isso o mapa cresceria para sempre.
    @Override
    public void sessionDestroyed(HttpSessionEvent evento) {
        sessoesPorUsuario.values().forEach(conjunto -> conjunto.remove(evento.getSession()));
        sessoesPorUsuario.values().removeIf(Set::isEmpty);
    }
}
