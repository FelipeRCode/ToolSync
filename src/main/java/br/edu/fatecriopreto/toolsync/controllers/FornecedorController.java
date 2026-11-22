package br.edu.fatecriopreto.toolsync.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.fatecriopreto.toolsync.dto.Dtos.FornecedorDTO;
import br.edu.fatecriopreto.toolsync.entities.Cargo;
import br.edu.fatecriopreto.toolsync.seguranca.Perfis;
import br.edu.fatecriopreto.toolsync.services.FornecedorService;
import lombok.RequiredArgsConstructor;

/** CRUD de fornecedores (Proprietario e Gerente; o Estoquista so consulta). */
@RestController
@RequestMapping("/api/fornecedores")
@RequiredArgsConstructor
@Perfis({ Cargo.PROPRIETARIO, Cargo.GERENTE })
public class FornecedorController {

	private final FornecedorService fornecedorService;

	@GetMapping
	@Perfis({ Cargo.PROPRIETARIO, Cargo.GERENTE, Cargo.ESTOQUISTA })
	public List<FornecedorDTO> listar(@RequestParam(defaultValue = "false") boolean ativos) {
		return fornecedorService.listar(ativos);
	}

	@GetMapping("/{id}")
	public FornecedorDTO buscar(@PathVariable Integer id) {
		return fornecedorService.buscar(id);
	}

	@PostMapping
	public ResponseEntity<FornecedorDTO> incluir(@RequestBody FornecedorDTO dados) {
		return ResponseEntity.status(HttpStatus.CREATED).body(fornecedorService.salvar(null, dados));
	}

	@PutMapping("/{id}")
	public FornecedorDTO editar(@PathVariable Integer id, @RequestBody FornecedorDTO dados) {
		return fornecedorService.salvar(id, dados);
	}

	@DeleteMapping("/{id}")
	public Map<String, String> excluir(@PathVariable Integer id) {
		return Map.of("mensagem", fornecedorService.excluir(id));
	}
}
