package br.edu.fatecriopreto.toolsync.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Objetos de entrada e saida da API (JSON). Ficam separados das entidades para
 * nao expor relacionamentos LAZY nem a senha.
 */
public final class Dtos {

	private Dtos() {
	}

	// ------------------------------------------------------------------ login
	public record LoginRequest(String login, String senha) {
	}

	public record UsuarioLogadoDTO(Integer id, String nome, String login, String cargo, String cargoDescricao, String telaInicial) {
	}

	// ------------------------------------------------------------------ clientes
	public record ClienteDTO(
			Integer id, String codigo, String nome, String tipoPessoa, String documento,
			String email, String telefone, String rua, String numero, String bairro, String cep,
			String cidade, String uf, String dataCadastro, String ultimaCompra) {
	}

	// ------------------------------------------------------------------ funcionarios
	public record FuncionarioDTO(
			Integer id, String codigo, String nome, String matricula, String rg, String cpf,
			LocalDate dataNascimento, String cargo, String cargoDescricao, String email, String celular,
			String rua, String numero, String bairro, String cep, String cidade, String uf,
			Boolean ativo, String login,
			/** so na entrada: senha inicial (cadastro) ou nova senha (edicao) */
			String senha) {
	}

	// ------------------------------------------------------------------ fornecedores
	public record FornecedorDTO(
			Integer id, String codigo, String razaoSocial, String cnpj, String tipoProduto,
			String regimeTributario, String representante, String telefone, String email,
			String dadosBancarios, String rua, String numero, String bairro, String cep,
			String cidade, String uf, Boolean ativo) {
	}

	// ------------------------------------------------------------------ produtos
	public record ProdutoDTO(
			Integer id, String codigo, String nome, String descricao,
			Integer categoriaId, String categoria, Integer marcaId, String marca,
			String ean, String sku, String ncm, BigDecimal precoCusto, BigDecimal precoVenda,
			String unidadeCompra, String unidadeVenda, BigDecimal larguraCm, BigDecimal alturaCm,
			BigDecimal comprimentoCm, String imagemUrl, Integer estoqueAtual, Integer estoqueReservado,
			Integer estoqueDisponivel, Integer estoqueMinimo, Boolean ativo,
			List<String> fornecedores, List<LocalEstoqueDTO> localizacoes,
			/** so no cadastro: entrada inicial de estoque (feita pela sp_EntradaEstoque) */
			Integer estoqueInicial, Integer fornecedorId, Integer localizacaoId) {
	}

	public record LocalEstoqueDTO(Integer localizacaoId, String galpao, String corredor, String prateleira, String box, Integer quantidade) {
	}

	public record ItemSimplesDTO(Integer id, String nome) {
	}

	public record EntradaEstoqueRequest(Integer produtoId, Integer quantidade, BigDecimal precoCusto,
			Integer fornecedorId, Integer localizacaoId, String documento) {
	}

	// ------------------------------------------------------------------ vendas
	public record ItemVendaRequest(Integer produtoId, Integer quantidade, BigDecimal desconto, String observacao) {
	}

	public record PagamentoRequest(String forma, BigDecimal valor, BigDecimal valorRecebido, Integer parcelas) {
	}

	public record VendaRequest(Integer clienteId, List<ItemVendaRequest> itens, List<PagamentoRequest> pagamentos,
			Boolean reserva, String numeroCupom, String numeroNf) {
	}

	public record ConcluirReservaRequest(List<PagamentoRequest> pagamentos) {
	}

	public record CancelarVendaRequest(String motivo) {
	}

	// ------------------------------------------------------------------ sistema
	public record RestoreRequest(String arquivo) {
	}
}
