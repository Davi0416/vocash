package com.davi.vocash.application.exception;

/**
 * Lançada quando o arquivo de áudio está vazio, muito curto ou ilegível.
 */
public class AudioInvalidoException extends VocashException {
    public AudioInvalidoException(String mensagem) {
        super("AUDIO_INVALIDO", mensagem);
    }
}
