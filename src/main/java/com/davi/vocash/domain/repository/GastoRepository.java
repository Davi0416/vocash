package com.davi.vocash.domain.repository;

import com.davi.vocash.domain.model.Gasto;
import java.util.List;

/**
 * Porta de saída do domínio para persistência de {@link Gasto}.
 *
 * <p><b>Camada DDD:</b> Domain — define o contrato de acesso a dados sem
 * conhecer nenhum detalhe de infraestrutura (JPA, SQL, etc.). Segue o padrão
 * <em>Repository</em> do DDD, mantendo o domínio isolado de tecnologias externas.
 *
 * <p>A implementação concreta reside na camada de infraestrutura:
 * {@link com.davi.vocash.infrastructure.persistence.GastoRepositoryImpl}.
 */
public interface GastoRepository {

    /**
     * Persiste um novo gasto ou atualiza um existente.
     *
     * @param gasto entidade a ser salva
     * @return entidade salva, incluindo o {@code id} gerado pelo banco
     */
    Gasto salvar(Gasto gasto);

    /**
     * Retorna todos os gastos de uma categoria específica.
     *
     * @param categoria nome da categoria (ex.: {@code alimentacao})
     * @return lista de gastos; vazia se nenhum for encontrado
     */
    List<Gasto> buscarPorCategoria(String categoria);

    /**
     * Retorna todos os gastos registrados, sem filtro.
     *
     * @return lista completa de gastos; vazia se não houver nenhum
     */
    List<Gasto> buscarTodos();
}
