package br.edu.fatecriopreto.toolsync.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.edu.fatecriopreto.toolsync.config.NegocioException;
import br.edu.fatecriopreto.toolsync.dto.Dtos.LocalEstoqueDTO;
import br.edu.fatecriopreto.toolsync.repositories.LocalizacaoRepository;
import br.edu.fatecriopreto.toolsync.seguranca.UsuarioSessao;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstoqueService {

	private final BancoService banco;
	private final LocalizacaoRepository localizacaoRepository;

	/**
	 * Entrada de mercadoria pela procedure sp_EntradaEstoque (transacao): gera
	 * a movimentacao (trigger soma no estoque), atualiza o custo (trigger grava o
	 * historico de preco), o fornecedor e o local de armazenagem.
	 */
	public Map<String, Object> entrada(Integer produtoId, Integer quantidade, BigDecimal precoCusto, Integer fornecedorId,
			Integer localizacaoId, String documento, UsuarioSessao quem) {
		if (produtoId == null) {
			throw new NegocioException("Escolha o produto.");
		}
		return banco.consultarUm("""
				exec sp_EntradaEstoque @produtoId = ?, @quantidade = ?, @precoCusto = ?, @fornecedorId = ?,
				     @localizacaoId = ?, @funcionarioId = ?, @documento = ?
				""", produtoId, quantidade, precoCusto, fornecedorId, localizacaoId, quem.funcionarioId(),
				Validacao.opcional(documento));
	}

	/** historico de movimentacoes (view v_HistoricoMovimentacoes) */
	public List<Map<String, Object>> movimentacoes(String codigoProduto) {
		if (codigoProduto == null || codigoProduto.isBlank()) {
			return banco.consultar("select top 100 * from v_HistoricoMovimentacoes order by data_movimentacao desc, id desc");
		}
		return banco.consultar("select * from v_HistoricoMovimentacoes where produto_codigo = ? order by data_movimentacao desc, id desc",
				codigoProduto.trim());
	}

	/** produtos com estoque baixo (view v_EstoqueBaixo) */
	public List<Map<String, Object>> estoqueBaixo() {
		return banco.consultar("select * from v_EstoqueBaixo order by estoque_disponivel - estoque_minimo, nome");
	}

	public List<Map<String, Object>> ultimasSaidas(int quantidade) {
		return banco.consultar("select top (?) * from v_HistoricoMovimentacoes where tipo = 'SAIDA' order by data_movimentacao desc, id desc",
				quantidade);
	}

	public List<LocalEstoqueDTO> localizacoes() {
		return localizacaoRepository.findAllByOrderByGalpaoAscCorredorAscPrateleiraAscBoxAsc().stream()
				.map(l -> new LocalEstoqueDTO(l.getId(), l.getGalpao(), l.getCorredor(), l.getPrateleira(), l.getBox(), null))
				.toList();
	}
}
