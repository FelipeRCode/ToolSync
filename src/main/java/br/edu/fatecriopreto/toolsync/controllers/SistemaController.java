package br.edu.fatecriopreto.toolsync.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.fatecriopreto.toolsync.dto.Dtos.RestoreRequest;
import br.edu.fatecriopreto.toolsync.entities.Cargo;
import br.edu.fatecriopreto.toolsync.seguranca.Perfis;
import br.edu.fatecriopreto.toolsync.services.SistemaService;
import lombok.RequiredArgsConstructor;

/** Backup e restore: somente o Proprietario (tela Configuracoes). */
@RestController
@RequestMapping("/api/sistema")
@RequiredArgsConstructor
@Perfis(Cargo.PROPRIETARIO)
public class SistemaController {

	private final SistemaService sistemaService;

	@PostMapping("/backup")
	public Map<String, Object> backup() {
		return sistemaService.backup();
	}

	@GetMapping("/backups")
	public List<Map<String, Object>> backups() {
		return sistemaService.backups();
	}

	@PostMapping("/restore")
	public Map<String, Object> restore(@RequestBody RestoreRequest dados) {
		return sistemaService.restore(dados.arquivo());
	}
}
