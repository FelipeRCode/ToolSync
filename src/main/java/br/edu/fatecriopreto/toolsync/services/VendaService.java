package br.edu.fatecriopreto.toolsync.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import br.edu.fatecriopreto.toolsync.config.NegocioException;
import br.edu.fatecriopreto.toolsync.dto.Dtos.ItemVendaRequest;
import br.edu.fatecriopreto.toolsync.dto.Dtos.PagamentoRequest;
import br.edu.fatecriopreto.toolsync.dto.Dtos.VendaRequest;
import br.edu.fatecriopreto.toolsync.entities.Cargo;
import br.edu.fatecriopreto.toolsync.seguranca.UsuarioSessao;
import lombok.RequiredArgsConstructor;

/**
 * Vendas. Gravacao pelas procedures (sp_RegistrarVenda, sp_ConcluirReserva,
 * sp_CancelarVenda) e consulta pela view v_VendasDetalhadas.
 */
@Service
@RequiredArgsConstructor
public class VendaService {

	private final BancoService banco;

	/** o vendedor so ve as proprias vendas; proprietario e gerente veem todas */
	public List<Map<String, Object>> listarTodos(UsuarioSessao quem) {
		if (quem.cargo() == Cargo.VENDEDOR) {
			return banco.consultar("select * from v_VendasDetalhadas where vendedor_id = ? order by data_venda desc", quem.funcionarioId());
		}
		return banco.consultar("select * from v_VendasDetalhadas order by data_venda desc");
	}

	public Map<String, Object> detalhe(Integer id, UsuarioSessao quem) {
		Map<String, Object> venda = banco.consultarUm("select * from v_VendasDetalhadas where codigo = ?", id);
		if (venda.isEmpty()) {
			throw new NoSuchElementException("Venda não encontrada.");
		}
		if (quem.cargo() == Cargo.VENDEDOR && !quem.funcionarioId().equals(venda.get("vendedor_id"))) {
			throw new NegocioException("Você só pode ver as suas próprias vendas.");
		}
		venda.put("itens", banco.consultar("""
				select IV.id, P.codigo, P.nome as produto, IV.quantidade, IV.preco_unitario, IV.desconto, IV.subtotal, IV.observacao
				from ItensVenda as IV inner join Produtos as P on P.id = IV.produto_id
				where IV.venda_id = ? order by IV.id
				""", id));
		venda.put("pagamentos", banco.consultar(
				"select forma, valor, valor_recebido, troco, parcelas, data_pagamento from PagamentosVenda where venda_id = ? order by id", id));
		venda.put("motivo_cancelamento", banco.consultarUm("select motivo_cancelamento from Vendas where id = ?", id).get("motivo_cancelamento"));
		return venda;
	}

	/** grava a venda inteira numa transacao (procedure sp_RegistrarVenda) */
	public Map<String, Object> registrar(VendaRequest dados, UsuarioSessao quem) {
		if (dados.clienteId() == null) {
			throw new NegocioException("Escolha o cliente da venda.");
		}
		if (dados.itens() == null || dados.itens().isEmpty()) {
			throw new NegocioException("Adicione pelo menos um produto à venda.");
		}
		boolean reserva = Boolean.TRUE.equals(dados.reserva());
		if (!reserva && (dados.pagamentos() == null || dados.pagamentos().isEmpty())) {
			throw new NegocioException("Informe o pagamento da venda.");
		}
		return banco.consultarUm("""
				exec sp_RegistrarVenda @clienteId = ?, @vendedorId = ?, @itensJson = ?, @pagamentosJson = ?,
				     @reserva = ?, @numeroCupom = ?, @numeroNf = ?
				""", dados.clienteId(), quem.funcionarioId(), itensJson(dados.itens()),
				reserva ? null : pagamentosJson(dados.pagamentos()), reserva,
				Validacao.opcional(dados.numeroCupom()), Validacao.opcional(dados.numeroNf()));
	}

	public Map<String, Object> concluirReserva(Integer vendaId, List<PagamentoRequest> pagamentos, UsuarioSessao quem) {
		if (pagamentos == null || pagamentos.isEmpty()) {
			throw new NegocioException("Informe o pagamento para concluir a reserva.");
		}
		return banco.consultarUm("exec sp_ConcluirReserva @vendaId = ?, @pagamentosJson = ?, @funcionarioId = ?",
				vendaId, pagamentosJson(pagamentos), quem.funcionarioId());
	}

	public Map<String, Object> cancelar(Integer vendaId, String motivo, UsuarioSessao quem) {
		return banco.consultarUm("exec sp_CancelarVenda @vendaId = ?, @funcionarioId = ?, @motivo = ?",
				vendaId, quem.funcionarioId(), motivo);
	}

	private String itensJson(List<ItemVendaRequest> itens) {
		List<Map<String, Object>> lista = new ArrayList<>();
		for (ItemVendaRequest i : itens) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("produtoId", i.produtoId());
			m.put("quantidade", i.quantidade());
			m.put("desconto", i.desconto());
			m.put("observacao", Validacao.opcional(i.observacao()));
			lista.add(m);
		}
		return BancoService.json(lista);
	}

	private String pagamentosJson(List<PagamentoRequest> pagamentos) {
		List<Map<String, Object>> lista = new ArrayList<>();
		for (PagamentoRequest p : pagamentos) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("forma", p.forma());
			m.put("valor", p.valor());
			m.put("valorRecebido", p.valorRecebido());
			m.put("parcelas", p.parcelas());
			lista.add(m);
		}
		return BancoService.json(lista);
	}
}
