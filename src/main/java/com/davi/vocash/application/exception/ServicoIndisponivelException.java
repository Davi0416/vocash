package com.davi.vocash.application.exception;

/**
 * Lançada quando a API da Groq (Whisper ou LLM) retorna erro ou está inacessível.
 */
public class ServicoIndisponivelException extends VocashException {
    public ServicoIndisponivelException(String mensagem) {
        super("SERVICO_INDISPONIVEL", mensagem);
    }
}
