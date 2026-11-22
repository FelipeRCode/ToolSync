package br.edu.fatecriopreto.toolsync.entities;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Chave composta de ProdutoFornecedor (produto_id + fornecedor_id). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProdutoFornecedorId implements Serializable {

	private Integer produtoId;
	private Integer fornecedorId;
}
