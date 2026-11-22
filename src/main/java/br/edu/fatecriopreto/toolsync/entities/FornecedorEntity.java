package br.edu.fatecriopreto.toolsync.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Fornecedor (nome herdado = razao social). Herda de PessoaEntity (tabela Fornecedores). */
@Entity
@Table(name = "Fornecedores")
@PrimaryKeyJoinColumn(name = "pessoa_id")
@Getter
@Setter
@NoArgsConstructor
public class FornecedorEntity extends PessoaEntity {

	private String cnpj;

	/** tipo de produto fornecido */
	private String ramo;

	/** Simples Nacional, Lucro Presumido ou Lucro Real */
	private String regimeTributario;

	private String representante;
	private String dadosBancarios;
	private Boolean ativo = true;
}
