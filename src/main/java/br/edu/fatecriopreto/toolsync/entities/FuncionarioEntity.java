package br.edu.fatecriopreto.toolsync.entities;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Funcionario da loja. Herda de PessoaEntity (tabela Funcionarios).
 * O cadastro e feito pela procedure sp_CadastrarFuncionario (Pessoas +
 * Funcionarios + Usuarios na mesma transacao); a edicao usa o JPA.
 */
@Entity
@Table(name = "Funcionarios")
@PrimaryKeyJoinColumn(name = "pessoa_id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FuncionarioEntity extends PessoaEntity {

	private String matricula;
	private String rg;
	private String cpf;
	private LocalDate dataNascimento;

	@Enumerated(EnumType.STRING)
	private Cargo cargo;

	private LocalDate dataAdmissao;

	/** false = desligado. O trigger tg_Funcionarios_SincronizarUsuario desativa o login junto. */
	private Boolean ativo = true;

	@PrePersist
	void antesDeGravarFuncionario() {
		if (dataAdmissao == null) {
			dataAdmissao = LocalDate.now();
		}
	}
}
