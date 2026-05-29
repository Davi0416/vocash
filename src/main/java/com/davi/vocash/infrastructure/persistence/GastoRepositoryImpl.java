package com.davi.vocash.infrastructure.persistence;

import com.davi.vocash.domain.model.Gasto;
import com.davi.vocash.domain.repository.GastoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Implementação JPA da porta de saída {@link GastoRepository} definida no domínio.
 *
 * <p><b>Camada DDD:</b> Infrastructure — adapta a interface de repositório do
 * domínio para a tecnologia de persistência concreta (Spring Data JPA + PostgreSQL),
 * seguindo o padrão <em>Adapter</em> da arquitetura hexagonal.
 *
 * <p><b>Papel no pipeline:</b> é o único ponto da aplicação que efetivamente
 * acessa o banco de dados. Recebe chamadas das tools
 * ({@link com.davi.vocash.application.service.GastoTools}) e delega as operações
 * ao {@link GastoJpaRepository}, que é gerenciado pelo Spring Data.
 */
@Repository
public class GastoRepositoryImpl implements GastoRepository {

    private final GastoJpaRepository jpaRepository;

    /**
     * @param jpaRepository repositório Spring Data JPA injetado pelo contêiner
     */
    public GastoRepositoryImpl(GastoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * {@inheritDoc}
     * Delega para {@link GastoJpaRepository#save}, que realiza INSERT ou UPDATE.
     */
    @Override
    public Gasto salvar(Gasto gasto) {
        return jpaRepository.save(gasto);
    }

    /**
     * {@inheritDoc}
     * Delega para o método derivado {@link GastoJpaRepository#findByCategoria}.
     */
    @Override
    public List<Gasto> buscarPorCategoria(String categoria) {
        return jpaRepository.findByCategoria(categoria);
    }

    /**
     * {@inheritDoc}
     * Delega para {@link GastoJpaRepository#findAll}.
     */
    @Override
    public List<Gasto> buscarTodos() {
        return jpaRepository.findAll();
    }
}
