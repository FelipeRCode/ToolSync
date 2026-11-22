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
	public ResponseEntity<List<FornecedorDTO>> listarTodos(@RequestParam(defaultValue = "false") boolean ativos) {
		List<FornecedorDTO> lista = fornecedorService.listarTodos(ativos);
		return ResponseEntity.ok().body(lista);
	}

	@GetMapping("/{id}")
	public ResponseEntity<FornecedorDTO> buscar(@PathVariable Integer id) {
		return ResponseEntity.ok().body(fornecedorService.buscar(id));
	}

	@PostMapping
	public ResponseEntity<FornecedorDTO> incluir(@RequestBody FornecedorDTO fornecedor) {
		FornecedorDTO novo = fornecedorService.incluir(fornecedor);
		return new ResponseEntity<>(novo, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<FornecedorDTO> editar(@PathVariable Integer id, @RequestBody FornecedorDTO fornecedor) {
		FornecedorDTO atualizado = fornecedorService.editar(id, fornecedor);
		return new ResponseEntity<>(atualizado, HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, String>> excluir(@PathVariable Integer id) {
		String mensagem = fornecedorService.excluir(id);
		return ResponseEntity.ok().body(Map.of("mensagem", mensagem));
	}
}
