package br.edu.fatecriopreto.toolsync.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.edu.fatecriopreto.toolsync.entities.Cargo;
import br.edu.fatecriopreto.toolsync.seguranca.UsuarioSessao;
import lombok.RequiredArgsConstructor;

/** Dashboards e relatorios: views e functions do banco. */
@Service
@RequiredArgsConstructor
public class RelatorioService {

	private static final String[] MESES = { "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez" };

	private final BancoService banco;
	private final EstoqueService estoqueService;

	/** relatorio de vendas por item (view v_RelatorioVendas) com filtros */
	public List<Map<String, Object>> vendas(String vendedor, String peca, String cliente) {
		return banco.consultar("""
				declare @vendedor nvarchar(150) = ?, @peca nvarchar(150) = ?, @cliente nvarchar(150) = ?;
				select * from v_RelatorioVendas
				where (@vendedor is null or vendedor like '%' + @vendedor + '%')
				  and (@peca is null or produto like '%' + @peca + '%' or produto_codigo like '%' + @peca + '%')
				  and (@cliente is null or cliente like '%' + @cliente + '%')
				order by data_venda desc, venda_id desc
				""", nvl(vendedor), nvl(peca), nvl(cliente));
	}

	/** desempenho por vendedor no periodo (function de tabela fc_VendasPorVendedor) */
	public List<Map<String, Object>> vendedores(LocalDate inicio, LocalDate fim) {
		return banco.consultar("select * from dbo.fc_VendasPorVendedor(?, ?) order by total_vendido desc",
				java.sql.Timestamp.valueOf(inicio.atStartOfDay()), java.sql.Timestamp.valueOf(fim.plusDays(1).atStartOfDay()));
	}

	/**
	 * Numeros dos dashboards: cards (view v_DashboardVendas), vendas por mes do
	 * ano (function fc_TotalVendasPeriodo), vendas dos ultimos 7 dias por
	 * vendedor (function fc_VendasPorVendedor) e estoque baixo (view v_EstoqueBaixo).
	 */
	public Map<String, Object> dashboard(UsuarioSessao quem) {
		Map<String, Object> d = new LinkedHashMap<>();
		d.put("cards", banco.consultarUm("select * from v_DashboardVendas"));

		int ano = LocalDate.now().getYear();
		List<Map<String, Object>> meses = new ArrayList<>();
		for (int m = 1; m <= 12; m++) {
			LocalDateTime inicio = LocalDate.of(ano, m, 1).atStartOfDay();
			LocalDateTime fim = inicio.plusMonths(1);
			Map<String, Object> linha = banco.consultarUm("select dbo.fc_TotalVendasPeriodo(?, ?) as total",
					java.sql.Timestamp.valueOf(inicio), java.sql.Timestamp.valueOf(fim));
			Map<String, Object> mes = new LinkedHashMap<>();
			mes.put("mes", MESES[m - 1]);
			mes.put("total", linha.get("total"));
			meses.add(mes);
		}
		d.put("vendasPorMes", meses);

		LocalDate hoje = LocalDate.now();
		d.put("vendasSemanaPorVendedor", vendedores(hoje.minusDays(6), hoje));
		d.put("estoqueBaixo", estoqueService.estoqueBaixo());
		if (quem.cargo() == Cargo.ESTOQUISTA || quem.cargo() == Cargo.GERENTE || quem.cargo() == Cargo.PROPRIETARIO) {
			d.put("ultimasSaidas", estoqueService.ultimasSaidas(5));
		}
		if (quem.cargo() == Cargo.VENDEDOR) {
			d.put("minhasVendasMes", banco.consultarUm("""
					select count(*) as qtd, isnull(sum(valor_total), 0) as total from Vendas
					where vendedor_id = ? and status = 'FINALIZADA'
					  and data_venda >= datefromparts(year(getdate()), month(getdate()), 1)
					""", quem.funcionarioId()));
		}
		return d;
	}

	/** texto vazio vira string vazia tratada como "sem filtro" pelo SQL (evita parametro nulo sem tipo) */
	private static String nvl(String s) {
		return s == null ? "" : s.trim();
	}
}
