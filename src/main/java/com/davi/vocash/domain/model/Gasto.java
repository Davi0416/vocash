package com.davi.vocash.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidade de domínio que representa um gasto financeiro do usuário.
 *
 * <p><b>Camada DDD:</b> Domain — é o núcleo do modelo de negócio da aplicação.
 * Não possui dependências de framework além das anotações JPA necessárias para
 * o mapeamento objeto-relacional.
 *
 * <p><b>Papel no pipeline:</b> instâncias de {@code Gasto} são criadas pela tool
 * {@link com.davi.vocash.application.service.GastoTools#registrarGasto} a partir
 * dos parâmetros extraídos pelo LLM do texto transcrito, e persistidas via
 * {@link com.davi.vocash.domain.repository.GastoRepository}.
 *
 * <p>A tabela correspondente no banco é {@code gastos}.
 */
@Entity
@Table(name = "gastos")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Gasto {

    /** Identificador gerado automaticamente pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Valor monetário do gasto em reais. */
    @Column(nullable = false)
    private BigDecimal valor;

    /**
     * Categoria semântica do gasto.
     * Valores esperados: {@code alimentacao}, {@code transporte},
     * {@code lazer}, {@code saude}, {@code outros}.
     */
    @Column(nullable = false)
    private String categoria;

    /** Descrição breve do item adquirido ou serviço utilizado. */
    private String descricao;

    /** Local onde o gasto ocorreu (loja, estabelecimento, etc.). */
    private String local;

    /** Data em que o gasto foi registrado. Preenchida automaticamente com {@link LocalDate#now()}. */
    @Column(nullable = false)
    private LocalDate data;

    /**
     * Construtor de negócio utilizado para criar um novo gasto antes de persistir.
     * O campo {@code id} é omitido pois é gerado pelo banco de dados.
     *
     * @param valor     valor monetário do gasto
     * @param categoria categoria semântica
     * @param descricao descrição breve
     * @param local     local do gasto
     * @param data      data de ocorrência
     */
    public Gasto(BigDecimal valor, String categoria, String descricao, String local, LocalDate data) {
        this.valor = valor;
        this.categoria = categoria;
        this.descricao = descricao;
        this.local = local;
        this.data = data;
    }
}
