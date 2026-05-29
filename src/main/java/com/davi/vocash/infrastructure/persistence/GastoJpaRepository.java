package com.davi.vocash.infrastructure.persistence;

import com.davi.vocash.domain.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório Spring Data JPA para a entidade {@link Gasto}.
 *
 * <p><b>Camada DDD:</b> Infrastructure — detalhe de implementação interno ao
 * pacote de persistência. Visibilidade package-private intencional: o domínio
 * e a camada de aplicação só conhecem a interface
 * {@link com.davi.vocash.domain.repository.GastoRepository}; este repositório
 * JPA é utilizado exclusivamente por {@link GastoRepositoryImpl}.
 *
 * <p>O método derivado {@link #findByCategoria} é gerado automaticamente pelo
 * Spring Data a partir do nome da propriedade {@code categoria} da entidade.
 */
interface GastoJpaRepository extends JpaRepository<Gasto, Long> {

    /**
     * Busca todos os gastos de uma categoria específica.
     *
     * @param categoria nome da categoria a filtrar
     * @return lista de gastos da categoria; vazia se nenhum for encontrado
     */
    java.util.List<Gasto> findByCategoria(String categoria);
}
