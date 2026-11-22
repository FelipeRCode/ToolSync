package br.edu.fatecriopreto.toolsync.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.fatecriopreto.toolsync.entities.*;

public interface ProdutoRepository extends JpaRepository<ProdutoEntity, Integer> {

	Optional<ProdutoEntity> findByCodigo(String codigo);

	boolean existsByCodigo(String codigo);

	@Query("select p from ProdutoEntity p join fetch p.categoria left join fetch p.marca order by p.codigo")
	List<ProdutoEntity> listarComCategoriaEMarca();
}
