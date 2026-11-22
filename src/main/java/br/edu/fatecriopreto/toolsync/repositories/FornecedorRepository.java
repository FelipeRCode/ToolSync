package br.edu.fatecriopreto.toolsync.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.fatecriopreto.toolsync.entities.*;

public interface FornecedorRepository extends JpaRepository<FornecedorEntity, Integer> {

	List<FornecedorEntity> findAllByOrderByNomeAsc();

	List<FornecedorEntity> findByAtivoTrueOrderByNomeAsc();

	boolean existsByCnpj(String cnpj);
}
