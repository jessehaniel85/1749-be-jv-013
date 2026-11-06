package com.ada.seguranca.cripto; // pacote onde ficam as classes de criptografia desta aula

import org.springframework.stereotype.Service; // marca a classe como um bean gerenciado pelo Spring

import java.nio.charset.StandardCharsets;      // UTF-8, para converter String -> bytes
import java.security.MessageDigest;            // calcula o hash (resumo) de um conjunto de bytes
import java.security.NoSuchAlgorithmException; // lançada se o algoritmo não existir no Java
import java.util.HexFormat;                    // converte bytes em texto hexadecimal (Java 17+)

/**
 * HASH: uma função de MÃO ÚNICA que transforma qualquer texto em uma "impressão digital"
 * de tamanho fixo (SHA-256 gera sempre 256 bits, ou 64 caracteres em hexadecimal).
 *
 * - O mesmo texto gera sempre o mesmo hash.
 * - Mudar um único caractere muda o hash por completo.
 * - Não há como voltar do hash ao texto original, e não existe chave.
 *
 * Serve para conferir INTEGRIDADE. Para guardar SENHAS não basta o SHA-256: usa-se um
 * algoritmo próprio para isso, como o BCrypt, que aparece na aula 08.
 */
@Service // o Spring cria uma única instância desta classe e a injeta onde for pedida
public class FuncaoHash {

    // nome do algoritmo; SHA-256 é seguro e muito usado (MD5 e SHA-1 estão quebrados: não use)
    public static final String ALGORITMO = "SHA-256";

    /** Devolve o SHA-256 do texto, em hexadecimal (64 caracteres). */
    public String sha256(String texto) {
        try {
            // obtém o calculador de hash configurado para SHA-256
            // (cada chamada cria um novo, porque o MessageDigest não é seguro entre threads)
            MessageDigest resumo = MessageDigest.getInstance(ALGORITMO);
            // calcula o hash dos bytes do texto (UTF-8) e devolve os 32 bytes do resultado
            byte[] hash = resumo.digest(texto.getBytes(StandardCharsets.UTF_8));
            // converte os 32 bytes em 64 caracteres hexadecimais, em minúsculas
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 existe em todo Java; chegar aqui seria um erro da aplicação
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
