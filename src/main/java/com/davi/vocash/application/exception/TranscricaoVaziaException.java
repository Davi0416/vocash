package com.davi.vocash.application.exception;

/**
 * Lançada quando o Whisper não consegue extrair texto útil do áudio
 * (silêncio, ruído, fala ininteligível, etc.).
 */
public class TranscricaoVaziaException extends VocashException {
    public TranscricaoVaziaException(String mensagem) {
        super("TRANSCRICAO_VAZIA", mensagem);
    }
}
