package com.ada.sso.cliente;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recebe o aviso de LOGOUT ÚNICO do provedor (servidor a servidor, sem navegador no meio).
 *
 * Esta rota é livre de sessão e de CSRF (veja ClienteSecurityConfig), mas NÃO é livre para
 * qualquer um: só vale com um token de logout assinado pelo provedor (e destinado a esta
 * aplicação). Sem essa verificação, qualquer pessoa derrubaria a sessão de qualquer usuário.
 */
@RestController
@Profile({"notas", "biblioteca"})
public class LogoutBackchannelController {

    private final VerificadorDeAsercao verificador;
    private final RegistroDeSessoes registro;

    public LogoutBackchannelController(VerificadorDeAsercao verificador, RegistroDeSessoes registro) {
        this.verificador = verificador;
        this.registro = registro;
    }

    @PostMapping(path = "/sso/logout-backchannel", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> logout(@RequestParam("logout_token") String tokenDeLogout) {
        String usuario;
        try {
            usuario = verificador.verificarLogout(tokenDeLogout);
        } catch (SsoException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();   // não é do provedor: ignorado
        }
        registro.encerrarSessoesDe(usuario);
        return ResponseEntity.noContent().build();
    }
}
