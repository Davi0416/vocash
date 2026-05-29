package com.davi.vocash.interfaces.web;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de entrada para criação e atualização manual de gastos via API REST.
 */
public record GastoRequest(
        BigDecimal valor,
        String categoria,
        String descricao,
        String local,
        LocalDate data,
        Integer parcelas
) {}
