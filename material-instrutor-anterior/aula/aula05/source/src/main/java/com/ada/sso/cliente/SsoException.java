package com.ada.sso.cliente;

/** Falha no fluxo de SSO (provedor fora do ar, código recusado, asserção inválida...). */
public class SsoException extends RuntimeException {

    public SsoException(String mensagem) {
        super(mensagem);
    }

    public SsoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
