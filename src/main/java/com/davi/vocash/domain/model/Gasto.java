package com.davi.vocash.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    public Gasto(BigDecimal valor, String categoria, String descricao, String local, LocalDate data) {
        this.valor = valor;
        this.categoria = categoria;
        this.descricao = descricao;
        this.local = local;
        this.data = data;
    }
}
