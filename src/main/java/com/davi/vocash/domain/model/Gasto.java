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
 *
 * <p><b>Papel no pipeline:</b> instâncias de {@code Gasto} são criadas pela tool
 * {@link com.davi.vocash.application.service.GastoTools#registrarGasto} e persistidas via
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private String categoria;

    private String descricao;
    private String local;

    @Column(nullable = false)
    private LocalDate data;

    /** Número de parcelas. Padrão 1 (à vista). Nullable no banco para compatibilidade com linhas existentes. */
    @Column
    private Integer parcelas = 1;

    /** Construtor de negócio sem {@code id} (gerado pelo banco). */
    public Gasto(BigDecimal valor, String categoria, String descricao, String local, LocalDate data, Integer parcelas) {
        this.valor = valor;
        this.categoria = categoria;
        this.descricao = descricao;
        this.local = local;
        this.data = data;
        this.parcelas = parcelas != null && parcelas >= 1 ? parcelas : 1;
    }
}
