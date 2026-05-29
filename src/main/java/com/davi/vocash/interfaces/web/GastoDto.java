package com.davi.vocash.interfaces.web;

import com.davi.vocash.domain.model.Gasto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de resposta para a entidade {@link Gasto}, exposto na API REST.
 */
public record GastoDto(
        Long id,
        BigDecimal valor,
        String categoria,
        String descricao,
        String local,
        LocalDate data,
        Integer parcelas
) {
    public static GastoDto from(Gasto g) {
        return new GastoDto(
                g.getId(),
                g.getValor(),
                g.getCategoria(),
                g.getDescricao(),
                g.getLocal(),
                g.getData(),
                g.getParcelas() != null ? g.getParcelas() : 1
        );
    }
}
