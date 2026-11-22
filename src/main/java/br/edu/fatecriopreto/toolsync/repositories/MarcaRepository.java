package br.edu.fatecriopreto.toolsync.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.fatecriopreto.toolsync.entities.*;

public interface MarcaRepository extends JpaRepository<MarcaEntity, Integer> {

	List<MarcaEntity> findAllByOrderByNomeAsc();

	Optional<MarcaEntity> findByNome(String nome);
}
