package br.edu.fatecriopreto.toolsync.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Produto (ferramenta). O saldo de estoque (estoqueAtual / estoqueReservado)
 * so e alterado pelo trigger de MovimentacoesEstoque, por isso o JPA nunca
 * grava essas colunas depois do cadastro (updatable = false).
 */
@Entity
@Table(name = "Produtos")
@Getter
@Setter
@NoArgsConstructor
public class ProdutoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String codigo;
	private String nome;
	private String descricao;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "categoria_id")
	private CategoriaEntity categoria;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "marca_id")
	private MarcaEntity marca;

	private String ean;
	private String sku;
	private String ncm;
	private BigDecimal precoCusto = BigDecimal.ZERO;
	private BigDecimal precoVenda;
	private String unidadeCompra = "UN";
	private String unidadeVenda = "UN";
	private BigDecimal larguraCm;
	private BigDecimal alturaCm;
	private BigDecimal comprimentoCm;
	private String imagemUrl;

	@Column(updatable = false)
	private Integer estoqueAtual = 0;

	@Column(updatable = false)
	private Integer estoqueReservado = 0;

	private Integer estoqueMinimo = 0;

	/** coluna calculada no banco: estoque_atual menos estoque_reservado */
	@Column(insertable = false, updatable = false)
	private Integer estoqueDisponivel;

	private Boolean ativo = true;

	/** N x N com Fornecedores (tabela associativa ProdutoFornecedor) */
	@OneToMany(mappedBy = "produto")
	private List<ProdutoFornecedorEntity> fornecedores = new ArrayList<>();

	/** N x N com Localizacoes (tabela associativa ProdutoLocalizacao) */
	@OneToMany(mappedBy = "produto")
	private List<ProdutoLocalizacaoEntity> localizacoes = new ArrayList<>();
}
