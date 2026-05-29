package com.davi.vocash.interfaces.web;

import com.davi.vocash.application.exception.AudioInvalidoException;
import com.davi.vocash.application.exception.ServicoIndisponivelException;
import com.davi.vocash.application.exception.TranscricaoVaziaException;
import com.davi.vocash.application.exception.VocashException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Tratamento centralizado de exceções para todos os controllers.
 * Converte exceções em respostas JSON padronizadas ({@link ErrorResponse}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Áudio vazio, muito curto ou ilegível → 400. */
    @ExceptionHandler(AudioInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleAudioInvalido(AudioInvalidoException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    /** Whisper não extraiu texto útil → 422. */
    @ExceptionHandler(TranscricaoVaziaException.class)
    public ResponseEntity<ErrorResponse> handleTranscricaoVazia(TranscricaoVaziaException ex) {
        return ResponseEntity.unprocessableEntity()
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    /** API Groq indisponível → 502. */
    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<ErrorResponse> handleServicoIndisponivel(ServicoIndisponivelException ex) {
        log.error("Serviço externo indisponível: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    /** Arquivo maior que o limite configurado (25 MB) → 413. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleArquivoGrande(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse("ARQUIVO_GRANDE", "O áudio enviado é muito grande. Limite: 25 MB."));
    }

    /** Qualquer outra VocashException não mapeada → 400. */
    @ExceptionHandler(VocashException.class)
    public ResponseEntity<ErrorResponse> handleVocash(VocashException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    /** Erros inesperados → 500 (sem expor detalhes internos). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Erro inesperado", ex);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("ERRO_INTERNO", "Ocorreu um erro inesperado. Tente novamente em instantes."));
    }
}
