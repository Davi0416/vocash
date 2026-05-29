package com.davi.vocash.interfaces.web;

import com.davi.vocash.application.service.OrquestradorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller REST que expõe o endpoint de entrada do assistente financeiro por voz.
 *
 * <p><b>Camada DDD:</b> Interfaces / Web — é a fronteira entre o mundo externo
 * (cliente HTTP) e a camada de aplicação. Não contém lógica de negócio; apenas
 * recebe a requisição, delega ao {@link OrquestradorService} e devolve a resposta.
 *
 * <p><b>Papel no pipeline:</b> ponto de entrada do fluxo. Recebe o arquivo de
 * áudio via {@code multipart/form-data}, inicia o pipeline de transcrição +
 * interpretação + tool calling e retorna a resposta textual gerada pelo
 * assistente.
 *
 * <p><b>Base URL:</b> {@code /api/v1/assistente}
 */
@RestController
@RequestMapping("/api/v1/assistente")
public class AssistenteController {

    private final OrquestradorService orquestradorService;

    /**
     * @param orquestradorService serviço de orquestração do pipeline de IA
     */
    public AssistenteController(OrquestradorService orquestradorService) {
        this.orquestradorService = orquestradorService;
    }

    /**
     * Processa um áudio enviado pelo usuário e retorna a resposta do assistente.
     *
     * <p>O arquivo é transcrito pelo Whisper, o texto é interpretado pelo LLM
     * e a tool adequada ({@code registrarGasto} ou {@code gerarRelatorio}) é
     * executada automaticamente pelo Spring AI.
     *
     * @param arquivo arquivo de áudio enviado como {@code multipart/form-data}
     *                com o campo {@code audio} (máximo 25 MB, configurável em
     *                {@code application.yml})
     * @return {@code 200 OK} com a resposta textual do assistente no corpo
     * @throws Exception se ocorrer erro na leitura do arquivo ou nas chamadas à API da Groq
     */
    @PostMapping("/processar")
    public ResponseEntity<String> processar(@RequestParam("audio") MultipartFile arquivo) throws Exception {
        String resposta = orquestradorService.processar(arquivo);
        return ResponseEntity.ok(resposta);
    }
}
