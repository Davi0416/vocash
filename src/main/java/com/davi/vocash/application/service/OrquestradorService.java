package com.davi.vocash.application.service;

import com.davi.vocash.infrastructure.TranscricaoService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OrquestradorService {

    private final ChatClient chatClient;
    private final GastoTools gastoTools;
    private final TranscricaoService transcricaoService;

    public OrquestradorService(ChatClient.Builder builder, GastoTools gastoTools, TranscricaoService transcricaoService) {
        this.gastoTools = gastoTools;
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

    public String processar(MultipartFile arquivo) throws Exception {
        ByteArrayResource resource = new ByteArrayResource(arquivo.getBytes()) {
            @Override
            public String getFilename() {
                return arquivo.getOriginalFilename();
            }
        };

        String texto = transcricaoService.transcrever(resource);
        return chatClient.prompt()
                .user(texto)
                .call()
                .content();
    }
}
