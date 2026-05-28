package com.davi.vocash.interfaces.web;

import com.davi.vocash.infrastructure.TranscricaoService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/teste")
public class TranscricaoController {

    private final TranscricaoService transcricaoService;

    public TranscricaoController(TranscricaoService transcricaoService) {
        this.transcricaoService = transcricaoService;
    }

    @PostMapping("/transcrever")
    public String transcrever(@RequestParam("audio") MultipartFile arquivo) throws Exception {
        ByteArrayResource resource = new ByteArrayResource(arquivo.getBytes()) {
            @Override
            public String getFilename() {
                return arquivo.getOriginalFilename();
            }
        };

        return transcricaoService.transcrever(resource);
    }
}
