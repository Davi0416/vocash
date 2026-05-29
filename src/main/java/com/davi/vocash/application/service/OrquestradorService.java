package com.davi.vocash.application.service;

import com.davi.vocash.infrastructure.TranscricaoService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Serviço responsável por orquestrar o pipeline de processamento de áudio do Vocash.
 *
 * <p><b>Camada DDD:</b> Application — coordena os serviços de infraestrutura
 * (transcrição) e as tools de domínio ({@link GastoTools}), sem conter regras
 * de negócio próprias.
 *
 * <p><b>Papel no pipeline:</b>
 * <ol>
 *   <li>Recebe o arquivo de áudio enviado pelo controller.</li>
 *   <li>Delega a transcrição para {@link TranscricaoService} (Whisper via Groq).</li>
 *   <li>Envia o texto transcrito ao {@link ChatClient} configurado com o LLM
 *       {@code llama-3.3-70b-versatile} e as tools {@link GastoTools}.</li>
 *   <li>O LLM decide qual tool invocar; o Spring AI executa o método Java
 *       correspondente e devolve o resultado ao modelo.</li>
 *   <li>Retorna a resposta final em texto ao controller.</li>
 * </ol>
 *
 * <p>O {@link ChatClient} é configurado no construtor com um system prompt em
 * português e as tools registradas via {@code defaultTools(gastoTools)}.
 */
@Service
public class OrquestradorService {

    private final ChatClient chatClient;
    private final GastoTools gastoTools;
    private final TranscricaoService transcricaoService;

    /**
     * Constrói o serviço e configura o {@link ChatClient} com o system prompt
     * e as tools de gastos.
     *
     * @param builder            builder do ChatClient injetado pelo Spring AI
     * @param gastoTools         bean com as tools {@code registrarGasto} e {@code gerarRelatorio}
     * @param transcricaoService serviço de transcrição de áudio via Whisper
     */
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

    /**
     * Executa o pipeline completo: transcrição do áudio → interpretação pelo LLM
     * → execução da tool correspondente → resposta em texto.
     *
     * @param arquivo arquivo de áudio enviado pelo usuário (multipart/form-data)
     * @return resposta textual gerada pelo assistente após executar a ação solicitada
     * @throws Exception se a leitura do arquivo ou a chamada à API da Groq falhar
     */
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
