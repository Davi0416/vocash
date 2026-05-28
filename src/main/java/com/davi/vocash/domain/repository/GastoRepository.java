package com.davi.vocash.domain.repository;

import com.davi.vocash.domain.model.Gasto;
import java.util.List;

public interface GastoRepository {
    Gasto salvar(Gasto gasto);
    List<Gasto> buscarPorCategoria(String categoria);
    List<Gasto> buscarTodos();
}
