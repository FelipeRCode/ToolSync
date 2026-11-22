package br.edu.fatecriopreto.toolsync.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Cliente da loja (PF ou PJ). Herda de PessoaEntity (tabela Clientes). */
@Entity
@Table(name = "Clientes")
@PrimaryKeyJoinColumn(name = "pessoa_id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ClienteEntity extends PessoaEntity {

	/** PF ou PJ */
	private String tipoPessoa;

	/** CPF (PF) ou CNPJ (PJ), com pontuacao */
	private String documento;
}
