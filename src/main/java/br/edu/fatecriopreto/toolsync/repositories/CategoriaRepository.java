package br.edu.fatecriopreto.toolsync.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.fatecriopreto.toolsync.entities.*;

public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Integer> {

	List<CategoriaEntity> findAllByOrderByNomeAsc();

	Optional<CategoriaEntity> findByNome(String nome);
}
