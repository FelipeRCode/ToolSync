package br.edu.fatecriopreto.toolsync.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela mae da heranca (Pessoas). Clientes, Funcionarios e Fornecedores
 * herdam nome, contato e endereco. Estrategia JOINED: uma tabela por classe,
 * ligadas pela chave pessoa_id.
 */
@Entity
@Table(name = "Pessoas")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public class PessoaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String nome;
	private String email;
	private String telefone;
	private String rua;
	private String numero;
	private String bairro;
	private String cep;
	private String cidade;
	private String uf;
	private LocalDateTime dataCadastro;

	@PrePersist
	void antesDeGravar() {
		if (dataCadastro == null) {
			dataCadastro = LocalDateTime.now();
		}
	}
}
