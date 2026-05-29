package com.davi.vocash.interfaces.web;

import com.davi.vocash.application.service.OrquestradorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller REST da interface de voz do assistente financeiro.
 *
 * <p><b>Camada DDD:</b> Interfaces / Web.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /api/v1/assistente/processar} — áudio multipart → Whisper → LLM</li>
 *   <li>{@code POST /api/v1/assistente/processar-texto} — texto → LLM (sem Whisper)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/assistente")
public class AssistenteController {

    private final OrquestradorService orquestradorService;

    public AssistenteController(OrquestradorService orquestradorService) {
        this.orquestradorService = orquestradorService;
    }

    /**
     * Processa áudio via Whisper + LLM.
     *
     * @param arquivo arquivo de áudio (campo {@code audio}, até 25 MB)
     * @return resposta textual do assistente
     */
    @PostMapping("/processar")
    public ResponseEntity<String> processar(@RequestParam("audio") MultipartFile arquivo) throws Exception {
        return ResponseEntity.ok(orquestradorService.processar(arquivo));
    }

    /**
     * Processa texto diretamente via LLM, sem transcrição.
     * Usado pelo frontend no modo de entrada manual / chips.
     *
     * @param texto frase do usuário
     * @return resposta textual do assistente
     */
    @PostMapping("/processar-texto")
    public ResponseEntity<String> processarTexto(@RequestParam("texto") String texto) {
        return ResponseEntity.ok(orquestradorService.processarTexto(texto));
    }
}
