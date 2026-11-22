package br.edu.fatecriopreto.toolsync.entities;

import java.math.BigDecimal;
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

/** Historico de precos, gravado pelo trigger tg_Produtos_HistoricoPreco. */
@Entity
@Table(name = "HistoricoPrecos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoPrecoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "produto_id")
	private ProdutoEntity produto;

	private BigDecimal precoCustoAnterior;
	private BigDecimal precoCustoNovo;
	private BigDecimal precoVendaAnterior;
	private BigDecimal precoVendaNovo;
	private LocalDateTime dataAlteracao;
	private String usuarioBanco;
}
