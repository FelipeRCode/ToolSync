package br.edu.fatecriopreto.toolsync.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.fatecriopreto.toolsync.entities.*;

public interface LocalizacaoRepository extends JpaRepository<LocalizacaoEntity, Integer> {

	List<LocalizacaoEntity> findAllByOrderByGalpaoAscCorredorAscPrateleiraAscBoxAsc();
}
