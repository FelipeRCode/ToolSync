package br.edu.fatecriopreto.toolsync.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Movimentacao de estoque (ENTRADA, SAIDA, ESTORNO, RESERVA, LIBERACAO).
 * E gravada pelas procedures e pelo trigger de ItensVenda; o trigger
 * tg_MovimentacoesEstoque_AtualizarSaldo atualiza o saldo do produto.
 */
@Entity
@Table(name = "MovimentacoesEstoque")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoEstoqueEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "produto_id")
	private ProdutoEntity produto;

	private String tipo;
	private Integer quantidade;
	private LocalDateTime dataMovimentacao;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "funcionario_id")
	private FuncionarioEntity funcionario;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "venda_id")
	private VendaEntity venda;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fornecedor_id")
	private FornecedorEntity fornecedor;

	private String documento;
}
