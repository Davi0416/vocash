package com.davi.vocash.interfaces.web;

import com.davi.vocash.application.service.OrquestradorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/assistente")
public class AssistenteController {

    private final OrquestradorService orquestradorService;

    public AssistenteController(OrquestradorService orquestradorService) {
        this.orquestradorService = orquestradorService;
    }

    @PostMapping("/processar")
    public ResponseEntity<String> processar(@RequestParam("audio") MultipartFile arquivo) throws Exception {
        String resposta = orquestradorService.processar(arquivo);
        return ResponseEntity.ok(resposta);
    }
}
