package com.ada.sso;

import com.ada.sso.cliente.IdpClient;
import com.ada.sso.cliente.SsoException;
import com.ada.sso.idp.AsercaoService;
import com.ada.sso.idp.ChavesIdp;
import com.ada.sso.idp.Codigo;
import com.ada.sso.idp.CodigoService;
import com.ada.sso.idp.IdpProperties;
import com.ada.sso.idp.UsuarioIdp;

import java.util.Map;

/**
 * Um provedor de identidade "de bolso" para testar as APLICAÇÕES sem rede: usa as classes REAIS
 * do provedor (chaves RSA, emissão do código, assinatura da asserção) chamadas direto na memória,
 * no lugar da chamada HTTP. Assim o teste verifica o contrato de verdade (nomes dos claims,
 * assinatura, destinatário), sem abrir porta nenhuma.
 */
public class IdpEmProcesso implements IdpClient {

    public static final String EMISSOR = "http://idp.localhost:9000";

    public final ChavesIdp chaves = new ChavesIdp();
    public final CodigoService codigos = new CodigoService();
    public final AsercaoService asercoes = new AsercaoService(new IdpProperties(EMISSOR, Map.of()), chaves);

    private final String appId;

    /** Se não for nulo, é devolvido no lugar da asserção real (para testar asserções forjadas). */
    public volatile String asercaoForcada;

    public IdpEmProcesso(String appId) {
        this.appId = appId;
    }

    /** Equivale ao navegador voltar do provedor com um código para esta aplicação. */
    public String emitirCodigoPara(String username, String nome, String papel) {
        UsuarioIdp usuario = new UsuarioIdp(username, "hash-nao-usado", nome, username + "@escola.test", papel);
        return codigos.emitir(usuario, appId);
    }

    @Override
    public String trocarCodigo(String codigo) {
        // O mesmo que o POST /sso/token faz: resgata o código (uso único, da aplicação certa)
        Codigo resgatado = codigos.resgatar(codigo, appId)
                .orElseThrow(() -> new SsoException("O provedor recusou o código."));
        return asercaoForcada != null ? asercaoForcada : asercoes.gerarAsercao(resgatado);
    }

    @Override
    public String buscarChavePublica() {
        return chaves.publicaEmBase64();
    }
}
