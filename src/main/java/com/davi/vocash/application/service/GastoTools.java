package com.davi.vocash.application.service;

import com.davi.vocash.domain.model.Gasto;
import com.davi.vocash.domain.repository.GastoRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Conjunto de tools de domínio financeiro expostas ao LLM via Spring AI.
 *
 * <p><b>Camada DDD:</b> Application — orquestra o domínio em resposta às
 * intenções detectadas pelo modelo de linguagem, sem conter regras de negócio
 * próprias.
 *
 * <p><b>Papel no pipeline:</b> esta classe é registrada como bean de tools no
 * {@link OrquestradorService}. Após o LLM analisar o texto transcrito pelo
 * Whisper, ele decide qual metodo chamar e com quais parâmetros; o Spring AI
 * executa o metodo correspondente e devolve o resultado ao LLM para compor a
 * resposta final ao usuário.
 *
 * <p>As duas tools disponíveis são:
 * <ul>
 *   <li>{@link #registrarGasto} — persiste um novo gasto no PostgreSQL.</li>
 *   <li>{@link #gerarRelatorio} — consulta e formata os gastos registrados.</li>
 * </ul>
 */
@Service
public class GastoTools {

    private final GastoRepository gastoRepository;

    public GastoTools(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    /**
     * Registra um gasto no banco de dados a partir dos parâmetros extraídos pelo LLM.
     *
     * @param valor       valor monetário em reais informado pelo usuário
     * @param categoria   categoria semântica
     * @param descricao   descrição breve do item ou serviço
     * @param local       estabelecimento ou local onde o gasto ocorreu
     * @param parcelas    número de parcelas (1 = à vista)
     * @return mensagem de confirmação para o LLM incluir na resposta ao usuário
     */
    @Tool(description = """
            Registra um gasto do usuário no banco de dados.
            Use quando o usuário mencionar que gastou, pagou ou comprou algo.
            """)
    public String registrarGasto(
            @ToolParam(description = "Valor total em reais do gasto") BigDecimal valor,
            @ToolParam(description = "Categoria: alimentacao, transporte, lazer, saude, contas, mercado, outros") String categoria,
            @ToolParam(description = "Descrição curta do que foi gasto") String descricao,
            @ToolParam(description = "Local onde o gasto ocorreu") String local,
            @ToolParam(description = "Número de parcelas, padrão 1 (à vista). Use >1 se o usuário mencionar parcelamento.") Integer parcelas
    ) {
        Gasto gasto = new Gasto(valor, categoria, descricao, local, LocalDate.now(), parcelas);
        gastoRepository.salvar(gasto);
        String msg = parcelas != null && parcelas > 1
                ? "Gasto de R$ " + valor + " em " + categoria + " em " + parcelas + "x registrado!"
                : "Gasto de R$ " + valor + " em " + categoria + " registrado com sucesso!";
        return msg;
    }

    /**
     * Consulta os gastos registrados e retorna um relatório formatado em texto.
     *
     * @param categoria categoria para filtrar; se vazia retorna todos
     * @return relatório em texto com cada gasto e o total acumulado
     */
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
