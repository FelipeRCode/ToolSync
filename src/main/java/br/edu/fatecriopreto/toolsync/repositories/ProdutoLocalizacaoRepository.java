package br.edu.fatecriopreto.toolsync.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.fatecriopreto.toolsync.entities.*;

@Repository
public interface ProdutoLocalizacaoRepository extends JpaRepository<ProdutoLocalizacaoEntity, ProdutoLocalizacaoId> {

	@Query("select pl from ProdutoLocalizacaoEntity pl join fetch pl.localizacao where pl.produto.id = :produtoId")
	List<ProdutoLocalizacaoEntity> listarPorProduto(@Param("produtoId") Integer produtoId);
}
