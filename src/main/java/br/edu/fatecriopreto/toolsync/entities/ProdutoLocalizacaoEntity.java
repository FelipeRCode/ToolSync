package br.edu.fatecriopreto.toolsync.entities;

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

/** Tabela associativa do N x N Produtos x Localizacoes. */
@Entity
@Table(name = "ProdutoLocalizacao")
@Getter
@Setter
@NoArgsConstructor
public class ProdutoLocalizacaoEntity {

	@EmbeddedId
	private ProdutoLocalizacaoId id = new ProdutoLocalizacaoId();

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("produtoId")
	@JoinColumn(name = "produto_id")
	private ProdutoEntity produto;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("localizacaoId")
	@JoinColumn(name = "localizacao_id")
	private LocalizacaoEntity localizacao;

	private Integer quantidade = 0;
}
