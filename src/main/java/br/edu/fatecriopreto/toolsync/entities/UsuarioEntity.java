package br.edu.fatecriopreto.toolsync.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Login do sistema. Relacao 1 x 1 com FuncionarioEntity (mesma chave). */
@Entity
@Table(name = "Usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEntity {

	@Id
	private Integer funcionarioId;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "funcionario_id")
	private FuncionarioEntity funcionario;

	private String login;

	/** hash BCrypt (60 caracteres); a senha pura nunca e gravada */
	private String senhaHash;

	private Boolean ativo = true;
	private LocalDateTime ultimoAcesso;
}
