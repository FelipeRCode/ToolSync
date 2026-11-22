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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.fatecriopreto.toolsync.dto.Dtos.FuncionarioDTO;
import br.edu.fatecriopreto.toolsync.entities.Cargo;
import br.edu.fatecriopreto.toolsync.seguranca.Perfis;
import br.edu.fatecriopreto.toolsync.seguranca.UsuarioSessao;
import br.edu.fatecriopreto.toolsync.services.FuncionarioService;
import lombok.RequiredArgsConstructor;

/** CRUD de funcionarios (Proprietario e Gerente). */
@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
@Perfis({ Cargo.PROPRIETARIO, Cargo.GERENTE })
public class FuncionarioController {

	private final FuncionarioService funcionarioService;

	@GetMapping
	public List<FuncionarioDTO> listar() {
		return funcionarioService.listar();
	}

	@GetMapping("/{id}")
	public FuncionarioDTO buscar(@PathVariable Integer id) {
		return funcionarioService.buscar(id);
	}

	@PostMapping
	public ResponseEntity<FuncionarioDTO> incluir(@RequestBody FuncionarioDTO dados,
			@RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		return ResponseEntity.status(HttpStatus.CREATED).body(funcionarioService.incluir(dados, usuario));
	}

	@PutMapping("/{id}")
	public FuncionarioDTO editar(@PathVariable Integer id, @RequestBody FuncionarioDTO dados,
			@RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		return funcionarioService.editar(id, dados, usuario);
	}

	@DeleteMapping("/{id}")
	public Map<String, String> excluir(@PathVariable Integer id, @RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		return Map.of("mensagem", funcionarioService.excluir(id, usuario));
	}
}
