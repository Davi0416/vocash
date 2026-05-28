package com.davi.vocash.infrastructure.persistence;

import com.davi.vocash.domain.model.Gasto;
import com.davi.vocash.domain.repository.GastoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class GastoRepositoryImpl implements GastoRepository {

    private final GastoJpaRepository jpaRepository;

    public GastoRepositoryImpl(GastoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Gasto salvar(Gasto gasto) {
        return jpaRepository.save(gasto);
    }

    @Override
    public List<Gasto> buscarPorCategoria(String categoria) {
        return jpaRepository.findByCategoria(categoria);
    }

    @Override
    public List<Gasto> buscarTodos() {
        return jpaRepository.findAll();
    }
}
