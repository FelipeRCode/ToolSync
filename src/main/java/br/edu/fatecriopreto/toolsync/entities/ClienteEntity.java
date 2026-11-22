package br.edu.fatecriopreto.toolsync.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cliente da loja (PF ou PJ). Herda de PessoaEntity (tabela Clientes). */
@Entity
@Table(name = "Clientes")
@PrimaryKeyJoinColumn(name = "pessoa_id")
@Getter
@Setter
@NoArgsConstructor
public class ClienteEntity extends PessoaEntity {

	/** PF ou PJ */
	private String tipoPessoa;

	/** CPF (PF) ou CNPJ (PJ), com pontuacao */
	private String documento;
}
