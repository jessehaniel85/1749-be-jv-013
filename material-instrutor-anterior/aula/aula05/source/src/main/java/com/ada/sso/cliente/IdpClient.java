package com.ada.sso.cliente;

/**
 * As duas chamadas SERVIDOR A SERVIDOR que a aplicação faz ao provedor. (Interface para que os
 * testes possam trocar a chamada HTTP por um provedor de mentira.)
 */
public interface IdpClient {

    /** Troca o código recebido na URL pela asserção assinada (POST /sso/token). */
    String trocarCodigo(String codigo);

    /** Busca a chave pública do provedor, em Base64 X.509 (GET /sso/chave-publica). */
    String buscarChavePublica();
}
