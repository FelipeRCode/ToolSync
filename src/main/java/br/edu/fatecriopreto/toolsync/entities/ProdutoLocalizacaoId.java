package br.edu.fatecriopreto.toolsync.entities;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Chave composta de ProdutoLocalizacao (produto_id + localizacao_id). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProdutoLocalizacaoId implements Serializable {

	private Integer produtoId;
	private Integer localizacaoId;
}
