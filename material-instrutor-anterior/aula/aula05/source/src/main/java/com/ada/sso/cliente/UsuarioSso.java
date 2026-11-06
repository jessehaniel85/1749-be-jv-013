package com.ada.sso.cliente;

/**
 * Quem é o usuário logado, segundo o provedor de identidade. Estes dados vêm da ASERÇÃO;
 * a aplicação não tem tabela de usuários nem senha nenhuma.
 *
 * @param sub   identificador do usuário (o login no provedor).
 * @param papel o papel dele (ALUNO, PROFESSOR...). O que cada papel PODE FAZER é decidido por esta
 *              aplicação: o provedor diz QUEM é, a aplicação decide o que ele pode.
 */
public record UsuarioSso(String sub, String nome, String email, String papel) implements java.io.Serializable {
}
