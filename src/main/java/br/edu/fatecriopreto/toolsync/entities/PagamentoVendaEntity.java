package br.edu.fatecriopreto.toolsync.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Pagamento de uma venda (uma venda pode ter varias formas de pagamento). */
@Entity
@Table(name = "PagamentosVenda")
@Getter
@Setter
@NoArgsConstructor
public class PagamentoVendaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "venda_id")
	private VendaEntity venda;

	/** DINHEIRO, PIX, CARTAO_DEBITO ou CARTAO_CREDITO */
	private String forma;

	private BigDecimal valor;
	private BigDecimal valorRecebido;

	/** coluna calculada: valor_recebido menos valor */
	@Column(insertable = false, updatable = false)
	private BigDecimal troco;

	private Integer parcelas;
	private LocalDateTime dataPagamento;
}
