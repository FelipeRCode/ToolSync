package br.edu.fatecriopreto.toolsync.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tabela associativa do N x N Produtos x Fornecedores. */
@Entity
@Table(name = "ProdutoFornecedor")
@Getter
@Setter
@NoArgsConstructor
public class ProdutoFornecedorEntity {

	@EmbeddedId
	private ProdutoFornecedorId id = new ProdutoFornecedorId();

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("produtoId")
	@JoinColumn(name = "produto_id")
	private ProdutoEntity produto;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("fornecedorId")
	@JoinColumn(name = "fornecedor_id")
	private FornecedorEntity fornecedor;

	private String codigoNoFornecedor;
	private BigDecimal ultimoPrecoCompra;
	private LocalDateTime dataUltimaCompra;
}
