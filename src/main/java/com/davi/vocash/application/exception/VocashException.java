package com.davi.vocash.application.exception;

/**
 * Exceção base da aplicação. Carrega um código legível por máquina
 * para que o frontend possa exibir mensagens específicas.
 */
public class VocashException extends RuntimeException {

    private final String codigo;

    public VocashException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() { return codigo; }
}
