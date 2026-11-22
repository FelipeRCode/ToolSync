package br.edu.fatecriopreto.toolsync.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.fatecriopreto.toolsync.entities.*;

public interface ItemVendaRepository extends JpaRepository<ItemVendaEntity, Integer> {

	@Query("select i from ItemVendaEntity i join fetch i.produto where i.venda.id = :vendaId order by i.id")
	List<ItemVendaEntity> listarPorVenda(@Param("vendaId") Integer vendaId);

	boolean existsByProdutoId(Integer produtoId);
}
