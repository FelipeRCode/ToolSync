package br.edu.fatecriopreto.toolsync.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Acesso direto ao SQL Server para o que e programado no banco: procedures,
 * views e functions. Os resultados voltam como mapas (coluna -> valor) e as
 * datas ja convertidas para texto ISO, prontos para virar JSON.
 */
@Service
@RequiredArgsConstructor
public class BancoService {

	private final JdbcTemplate jdbc;

	/** executa uma consulta (view, function ou procedure) e devolve as linhas */
	public List<Map<String, Object>> consultar(String sql, Object... parametros) {
		List<Map<String, Object>> linhas = jdbc.queryForList(sql, parametros);
		List<Map<String, Object>> resultado = new ArrayList<>(linhas.size());
		for (Map<String, Object> linha : linhas) {
			Map<String, Object> convertida = new LinkedHashMap<>();
			linha.forEach((coluna, valor) -> convertida.put(coluna, converter(valor)));
			resultado.add(convertida);
		}
		return resultado;
	}

	/** executa e devolve so a primeira linha (ou um mapa vazio) */
	public Map<String, Object> consultarUm(String sql, Object... parametros) {
		List<Map<String, Object>> linhas = consultar(sql, parametros);
		return linhas.isEmpty() ? new LinkedHashMap<>() : linhas.get(0);
	}

	/** executa um comando que nao devolve linhas */
	public void executar(String sql, Object... parametros) {
		jdbc.update(sql, parametros);
	}

	private Object converter(Object valor) {
		if (valor instanceof Timestamp t) {
			return t.toLocalDateTime().withNano(0).toString();
		}
		if (valor instanceof java.sql.Date d) {
			return d.toLocalDate().toString();
		}
		return valor;
	}

	// ------------------------------------------------------------------
	// JSON simples para as procedures que recebem listas (itens e pagamentos)
	// ------------------------------------------------------------------

	/** monta um array JSON a partir de uma lista de mapas (texto, numero ou nulo) */
	public static String json(Collection<? extends Map<String, ?>> itens) {
		StringBuilder sb = new StringBuilder("[");
		boolean primeiroItem = true;
		for (Map<String, ?> item : itens) {
			if (!primeiroItem) {
				sb.append(',');
			}
			primeiroItem = false;
			sb.append('{');
			boolean primeiroCampo = true;
			for (Map.Entry<String, ?> campo : item.entrySet()) {
				if (campo.getValue() == null) {
					continue;
				}
				if (!primeiroCampo) {
					sb.append(',');
				}
				primeiroCampo = false;
				sb.append(texto(campo.getKey())).append(':');
				Object v = campo.getValue();
				if (v instanceof BigDecimal b) {
					sb.append(b.toPlainString());
				} else if (v instanceof Number) {
					sb.append(v);
				} else {
					sb.append(texto(v.toString()));
				}
			}
			sb.append('}');
		}
		return sb.append(']').toString();
	}

	private static String texto(String s) {
		StringBuilder sb = new StringBuilder("\"");
		for (char c : s.toCharArray()) {
			switch (c) {
				case '"' -> sb.append("\\\"");
				case '\\' -> sb.append("\\\\");
				case '\n' -> sb.append("\\n");
				case '\r' -> sb.append("\\r");
				case '\t' -> sb.append("\\t");
				default -> {
					if (c < 0x20) {
						sb.append(String.format("\\u%04x", (int) c));
					} else {
						sb.append(c);
					}
				}
			}
		}
		return sb.append('"').toString();
	}
}
