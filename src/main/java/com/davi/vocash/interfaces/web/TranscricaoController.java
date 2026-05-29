package com.davi.vocash.interfaces.web;

import com.davi.vocash.application.service.OrquestradorService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/teste")
public class TranscricaoController {

    private final OrquestradorService orquestradorService;

    public TranscricaoController(OrquestradorService orquestradorService) {
        this.orquestradorService = orquestradorService;
    }

    @PostMapping("/transcrever")
    public String transcrever(@RequestParam("audio") MultipartFile arquivo) throws Exception {
        return orquestradorService.processar(arquivo);
    }
}
