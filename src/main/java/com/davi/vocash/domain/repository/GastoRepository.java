package com.davi.vocash.domain.repository;

import com.davi.vocash.domain.model.Gasto;
import java.util.List;
import java.util.Optional;

/**
 * Porta de saída do domínio para persistência de {@link Gasto}.
 *
 * <p><b>Camada DDD:</b> Domain — define o contrato de acesso a dados sem
 * conhecer detalhes de infraestrutura.
 */
public interface GastoRepository {

    Gasto salvar(Gasto gasto);

    Optional<Gasto> buscarPorId(Long id);

    List<Gasto> buscarPorCategoria(String categoria);

    List<Gasto> buscarTodos();

    void deletar(Long id);
}
