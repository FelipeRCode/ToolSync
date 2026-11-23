package br.edu.fatecriopreto.toolsync.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Fornecedor (nome herdado = razao social). Herda de PessoaEntity (tabela Fornecedores). */
@Entity
@Table(name = "Fornecedores")
@PrimaryKeyJoinColumn(name = "pessoa_id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FornecedorEntity extends PessoaEntity {

	private String cnpj;

	/** tipo de produto fornecido */
	private String ramo;

	/** Simples Nacional, Lucro Presumido, Lucro Real ou MEI */
	private String regimeTributario;

	private String representante;
	private String dadosBancarios;
	private Boolean ativo = true;
}
