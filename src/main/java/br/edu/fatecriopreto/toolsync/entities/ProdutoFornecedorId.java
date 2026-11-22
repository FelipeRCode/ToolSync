package br.edu.fatecriopreto.toolsync.entities;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Chave composta de ProdutoFornecedor (produto_id + fornecedor_id). */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoFornecedorId implements Serializable {

	private Integer produtoId;
	private Integer fornecedorId;
}
