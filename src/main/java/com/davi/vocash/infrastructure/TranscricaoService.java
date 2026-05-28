package com.davi.vocash.infrastructure;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class TranscricaoService {

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    public TranscricaoService(OpenAiAudioTranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    public String transcrever(Resource audio) {
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audio);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);
        return response.getResult().getOutput();
    }
}
