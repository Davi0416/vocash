package com.davi.vocash.interfaces.web;

import com.davi.vocash.domain.model.Gasto;
import com.davi.vocash.domain.repository.GastoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operações CRUD de gastos.
 *
 * <p><b>Camada DDD:</b> Interfaces / Web.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET    /api/v1/gastos}      — lista todos</li>
 *   <li>{@code POST   /api/v1/gastos}      — cria manualmente</li>
 *   <li>{@code PUT    /api/v1/gastos/{id}} — atualiza</li>
 *   <li>{@code DELETE /api/v1/gastos/{id}} — remove</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/gastos")
public class GastoController {

    private final GastoRepository gastoRepository;

    public GastoController(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    @GetMapping
    public List<GastoDto> listar() {
        return gastoRepository.buscarTodos().stream().map(GastoDto::from).toList();
    }

    @PostMapping
    public ResponseEntity<GastoDto> criar(@RequestBody GastoRequest req) {
        Gasto gasto = new Gasto(req.valor(), req.categoria(), req.descricao(), req.local(), req.data(), req.parcelas());
        return ResponseEntity.ok(GastoDto.from(gastoRepository.salvar(gasto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GastoDto> atualizar(@PathVariable Long id, @RequestBody GastoRequest req) {
        return gastoRepository.buscarPorId(id)
                .map(existing -> {
                    Gasto updated = new Gasto(
                            existing.getId(), req.valor(), req.categoria(),
                            req.descricao(), req.local(), req.data(), req.parcelas());
                    return ResponseEntity.ok(GastoDto.from(gastoRepository.salvar(updated)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (gastoRepository.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        gastoRepository.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
