package br.edu.fatecriopreto.toolsync.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
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

/** Item de venda: tabela associativa do N x N Vendas x Produtos. */
@Entity
@Table(name = "ItensVenda")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemVendaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "venda_id")
	private VendaEntity venda;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "produto_id")
	private ProdutoEntity produto;

	private Integer quantidade;
	private BigDecimal precoUnitario;
	private BigDecimal desconto;
	private String observacao;

	/** coluna calculada: quantidade x preco_unitario menos desconto */
	@Column(insertable = false, updatable = false)
	private BigDecimal subtotal;
}
