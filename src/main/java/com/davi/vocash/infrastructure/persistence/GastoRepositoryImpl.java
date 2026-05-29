package com.davi.vocash.infrastructure.persistence;

import com.davi.vocash.domain.model.Gasto;
import com.davi.vocash.domain.repository.GastoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Implementação JPA da porta de saída {@link GastoRepository}.
 *
 * <p><b>Camada DDD:</b> Infrastructure — adapta a interface do domínio para
 * Spring Data JPA + PostgreSQL.
 */
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
    public Optional<Gasto> buscarPorId(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Gasto> buscarPorCategoria(String categoria) {
        return jpaRepository.findByCategoria(categoria);
    }

    @Override
    public List<Gasto> buscarTodos() {
        return jpaRepository.findAll();
    }

    @Override
    public void deletar(Long id) {
        jpaRepository.deleteById(id);
    }
}
