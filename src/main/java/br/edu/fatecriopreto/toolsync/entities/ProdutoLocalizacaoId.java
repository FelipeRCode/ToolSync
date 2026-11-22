package br.edu.fatecriopreto.toolsync.entities;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Chave composta de ProdutoLocalizacao (produto_id + localizacao_id). */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoLocalizacaoId implements Serializable {

	private Integer produtoId;
	private Integer localizacaoId;
}
