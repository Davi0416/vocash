package com.davi.vocash.application.service;

import com.davi.vocash.application.exception.AudioInvalidoException;
import com.davi.vocash.application.exception.ServicoIndisponivelException;
import com.davi.vocash.application.exception.TranscricaoVaziaException;
import com.davi.vocash.infrastructure.TranscricaoService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * Serviço responsável por orquestrar o pipeline de processamento do Vocash.
 *
 * <p><b>Camada DDD:</b> Application.
 *
 * <p>Valida a entrada antes de chamar os serviços externos e converte falhas
 * de infraestrutura em exceções de domínio ({@link ServicoIndisponivelException}).
 */
@Service
public class OrquestradorService {

    /** Tamanho mínimo de áudio considerado válido (3 KB). */
    private static final long MIN_AUDIO_BYTES = 3_072;

    /**
     * Textos que o Whisper retorna quando o áudio não tem fala útil.
     * Comparados em lowercase após trim.
     */
    private static final Set<String> WHISPER_NOISE_TOKENS = Set.of(
            "", "[music]", "[applause]", "[noise]", "[silence]",
            "(music)", "(applause)", "(noise)", "...", "."
    );

    private final ChatClient chatClient;
    private final TranscricaoService transcricaoService;

    public OrquestradorService(ChatClient.Builder builder, GastoTools gastoTools,
                               TranscricaoService transcricaoService) {
        this.transcricaoService = transcricaoService;
        this.chatClient = builder
                .defaultSystem("""
                        Você é um assistente financeiro pessoal em português brasileiro.
                        Quando o usuário mencionar um gasto, registre-o usando a tool disponível.
                        Quando pedir relatório ou resumo, gere usando a tool disponível.
                        Sempre responda de forma curta e amigável em português.
                        """)
                .defaultTools(gastoTools)
                .build();
    }

    /**
     * Pipeline completo: áudio → validação → Whisper → LLM → tool → resposta.
     *
     * @throws AudioInvalidoException       se o arquivo estiver vazio ou muito curto
     * @throws TranscricaoVaziaException    se o Whisper não extrair texto útil
     * @throws ServicoIndisponivelException se a API da Groq falhar
     */
    public String processar(MultipartFile arquivo) {
        validarAudio(arquivo);

        ByteArrayResource resource;
        try {
            resource = new ByteArrayResource(arquivo.getBytes()) {
                @Override public String getFilename() { return arquivo.getOriginalFilename(); }
            };
        } catch (Exception ex) {
            throw new AudioInvalidoException("Não foi possível ler o arquivo de áudio.");
        }

        String texto;
        try {
            texto = transcricaoService.transcrever(resource);
        } catch (Exception ex) {
            throw new ServicoIndisponivelException(
                    "O serviço de transcrição está temporariamente indisponível. Tente novamente em instantes.");
        }

        validarTranscricao(texto);

        try {
            return chatClient.prompt().user(texto).call().content();
        } catch (Exception ex) {
            throw new ServicoIndisponivelException(
                    "O assistente de IA está temporariamente indisponível. Tente novamente em instantes.");
        }
    }

    /**
     * Pipeline sem transcrição: texto → validação → LLM → tool → resposta.
     *
     * @throws AudioInvalidoException       se o texto estiver vazio
     * @throws ServicoIndisponivelException se a API da Groq falhar
     */
    public String processarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new AudioInvalidoException("O texto enviado está vazio.");
        }
        try {
            return chatClient.prompt().user(texto).call().content();
        } catch (Exception ex) {
            throw new ServicoIndisponivelException(
                    "O assistente de IA está temporariamente indisponível. Tente novamente em instantes.");
        }
    }

    // ── validações privadas ───────────────────────────────────────────────────

    private void validarAudio(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new AudioInvalidoException("Nenhum arquivo de áudio foi recebido.");
        }
        if (arquivo.getSize() < MIN_AUDIO_BYTES) {
            throw new AudioInvalidoException(
                    "O áudio é muito curto. Fale seu gasto e aguarde o envio automático.");
        }
    }

    private void validarTranscricao(String texto) {
        if (texto == null) {
            throw new TranscricaoVaziaException(
                    "Não foi possível transcrever o áudio. Tente falar mais próximo do microfone.");
        }
        String normalizado = texto.trim().toLowerCase();
        if (normalizado.length() < 3 || WHISPER_NOISE_TOKENS.contains(normalizado)) {
            throw new TranscricaoVaziaException(
                    "Não entendi o áudio. Fale claramente o seu gasto (ex.: \"gastei 50 reais no mercado\").");
        }
    }
}
