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

/** CRUD de funcionarios (Proprietario e Gerente). O cadastro usa a procedure sp_CadastrarFuncionario. */
@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
@Perfis({ Cargo.PROPRIETARIO, Cargo.GERENTE })
public class FuncionarioController {

	private final FuncionarioService funcionarioService;

	@GetMapping
	public ResponseEntity<List<FuncionarioDTO>> listarTodos() {
		List<FuncionarioDTO> lista = funcionarioService.listarTodos();
		return ResponseEntity.ok().body(lista);
	}

	@GetMapping("/{id}")
	public ResponseEntity<FuncionarioDTO> buscar(@PathVariable Integer id) {
		return ResponseEntity.ok().body(funcionarioService.buscar(id));
	}

	@PostMapping
	public ResponseEntity<FuncionarioDTO> incluir(@RequestBody FuncionarioDTO funcionario,
			@RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		FuncionarioDTO novo = funcionarioService.incluir(funcionario, usuario);
		return new ResponseEntity<>(novo, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<FuncionarioDTO> editar(@PathVariable Integer id, @RequestBody FuncionarioDTO funcionario,
			@RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		FuncionarioDTO atualizado = funcionarioService.editar(id, funcionario, usuario);
		return new ResponseEntity<>(atualizado, HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, String>> excluir(@PathVariable Integer id,
			@RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		String mensagem = funcionarioService.excluir(id, usuario);
		return ResponseEntity.ok().body(Map.of("mensagem", mensagem));
	}
}
