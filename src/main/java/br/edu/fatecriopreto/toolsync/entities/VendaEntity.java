package br.edu.fatecriopreto.toolsync.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Cabecalho da venda. A venda e gravada pela procedure sp_RegistrarVenda
 * (itens e pagamentos na mesma transacao); o JPA e usado para consultar.
 */
@Entity
@Table(name = "Vendas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private LocalDateTime dataVenda;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cliente_id")
	private ClienteEntity cliente;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vendedor_id")
	private FuncionarioEntity vendedor;

	private String numeroCupom;
	private String numeroNf;
	private BigDecimal valorTotal;

	/** ABERTA, RESERVADA, FINALIZADA ou CANCELADA */
	private String status;

	private LocalDateTime dataCancelamento;
	private String motivoCancelamento;

	/** N x N Vendas x Produtos (tabela associativa ItensVenda) */
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "venda")
	private List<ItemVendaEntity> itens = new ArrayList<>();

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "venda")
	private List<PagamentoVendaEntity> pagamentos = new ArrayList<>();
}
