package br.edu.fatecriopreto.toolsync.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.fatecriopreto.toolsync.entities.*;

@Repository
public interface ProdutoFornecedorRepository extends JpaRepository<ProdutoFornecedorEntity, ProdutoFornecedorId> {

	@Query("select pf from ProdutoFornecedorEntity pf join fetch pf.fornecedor where pf.produto.id = :produtoId")
	List<ProdutoFornecedorEntity> listarPorProduto(@Param("produtoId") Integer produtoId);
}
