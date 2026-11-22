package br.edu.fatecriopreto.toolsync.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Endereco fisico do estoque. */
@Entity
@Table(name = "Localizacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalizacaoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String galpao;
	private String corredor;
	private String prateleira;
	private String box;
}
