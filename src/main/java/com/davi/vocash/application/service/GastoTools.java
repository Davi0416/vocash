package com.davi.vocash.application.service;

import com.davi.vocash.domain.model.Gasto;
import com.davi.vocash.domain.repository.GastoRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class GastoTools {

    private final GastoRepository gastoRepository;

    public GastoTools(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    @Tool(description = """
            Registra um gasto do usuário no banco de dados.
            Use quando o usuário mencionar que gastou, pagou ou comprou algo.
            """)
    public String registrarGasto(
            @ToolParam(description = "Valor em reais do gasto") BigDecimal valor,
            @ToolParam(description = "Categoria do gasto: alimentacao, transporte, lazer, saude, outros") String categoria,
            @ToolParam(description = "Descrição curta do que foi gasto") String descricao,
            @ToolParam(description = "Local onde o gasto ocorreu") String local
    ) {
        Gasto gasto = new Gasto(valor, categoria, descricao, local, LocalDate.now());
        gastoRepository.salvar(gasto);
        return "Gasto de R$ " + valor + " em " + categoria + " registrado com sucesso!";
    }

    @Tool(description = """
            Gera um relatório dos gastos do usuário.
            Use quando o usuário pedir resumo, relatório ou quiser saber quanto gastou.
            """)
    public String gerarRelatorio(
            @ToolParam(description = "Categoria para filtrar. Se vazio, retorna todos os gastos") String categoria
    ) {
        List<Gasto> gastos = categoria == null || categoria.isBlank()
                ? gastoRepository.buscarTodos()
                : gastoRepository.buscarPorCategoria(categoria);

        if (gastos.isEmpty()) return "Nenhum gasto encontrado.";

        StringBuilder sb = new StringBuilder("Relatório de gastos:\n");
        gastos.forEach(g -> sb.append(String.format("- R$ %.2f em %s (%s) em %s%n",
                g.getValor(), g.getCategoria(), g.getDescricao(), g.getData())));

        BigDecimal total = gastos.stream()
                .map(Gasto::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sb.append("Total: R$ ").append(total);
        return sb.toString();
    }
}
