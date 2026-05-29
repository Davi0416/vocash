package com.davi.vocash.infrastructure;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Serviço de infraestrutura responsável pela transcrição de áudio para texto.
 *
 * <p><b>Camada DDD:</b> Infrastructure — encapsula a comunicação com a API de
 * transcrição da Groq (compatível com a API OpenAI), isolando o restante da
 * aplicação de detalhes de integração externa.
 *
 * <p><b>Papel no pipeline:</b> recebe um {@link Resource} com o áudio enviado
 * pelo usuário e retorna a transcrição em texto puro, que é então passada ao
 * LLM pelo {@link com.davi.vocash.application.service.OrquestradorService}.
 *
 * <p>O modelo utilizado é configurado em {@code application.yml}:
 * {@code spring.ai.openai.audio.transcription.options.model = whisper-large-v3}.
 */
@Service
public class TranscricaoService {

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    /**
     * @param transcriptionModel modelo de transcrição configurado pelo Spring AI
     *                           com as propriedades da Groq definidas em {@code application.yml}
     */
    public TranscricaoService(OpenAiAudioTranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    /**
     * Transcreve um arquivo de áudio para texto utilizando o Whisper via Groq.
     *
     * @param audio recurso de áudio a ser transcrito (qualquer implementação de {@link Resource})
     * @return texto transcrito pelo modelo Whisper
     */
    public String transcrever(Resource audio) {
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audio);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);
        return response.getResult().getOutput();
    }
}
