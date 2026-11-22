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
import org.springframework.web.bind.annotation.RestController;

import br.edu.fatecriopreto.toolsync.dto.Dtos.ClienteDTO;
import br.edu.fatecriopreto.toolsync.entities.Cargo;
import br.edu.fatecriopreto.toolsync.seguranca.Perfis;
import br.edu.fatecriopreto.toolsync.services.ClienteService;
import lombok.RequiredArgsConstructor;

/** CRUD de clientes (Proprietario e Vendedor). */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Perfis({ Cargo.PROPRIETARIO, Cargo.VENDEDOR })
public class ClienteController {

	private final ClienteService clienteService;

	@GetMapping
	public ResponseEntity<List<ClienteDTO>> listarTodos() {
		List<ClienteDTO> lista = clienteService.listarTodos();
		return ResponseEntity.ok().body(lista);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ClienteDTO> buscar(@PathVariable Integer id) {
		return ResponseEntity.ok().body(clienteService.buscar(id));
	}

	/** historico de compras (function fc_HistoricoCliente) */
	@GetMapping("/{id}/historico")
	public ResponseEntity<List<Map<String, Object>>> historico(@PathVariable Integer id) {
		return ResponseEntity.ok().body(clienteService.historico(id));
	}

	@PostMapping
	public ResponseEntity<ClienteDTO> incluir(@RequestBody ClienteDTO cliente) {
		ClienteDTO novo = clienteService.incluir(cliente);
		return new ResponseEntity<>(novo, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ClienteDTO> editar(@PathVariable Integer id, @RequestBody ClienteDTO cliente) {
		ClienteDTO atualizado = clienteService.editar(id, cliente);
		return new ResponseEntity<>(atualizado, HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	@Perfis(Cargo.PROPRIETARIO)
	public ResponseEntity<Void> excluir(@PathVariable Integer id) {
		clienteService.excluir(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
