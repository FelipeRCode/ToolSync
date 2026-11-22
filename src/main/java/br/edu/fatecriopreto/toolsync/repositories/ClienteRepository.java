package br.edu.fatecriopreto.toolsync.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.fatecriopreto.toolsync.entities.*;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Integer> {

	boolean existsByDocumento(String documento);

	List<ClienteEntity> findAllByOrderByNomeAsc();
}
