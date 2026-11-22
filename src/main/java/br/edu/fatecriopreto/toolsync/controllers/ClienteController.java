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
	public List<ClienteDTO> listar() {
		return clienteService.listar();
	}

	@GetMapping("/{id}")
	public ClienteDTO buscar(@PathVariable Integer id) {
		return clienteService.buscar(id);
	}

	@GetMapping("/{id}/historico")
	public List<Map<String, Object>> historico(@PathVariable Integer id) {
		return clienteService.historico(id);
	}

	@PostMapping
	public ResponseEntity<ClienteDTO> incluir(@RequestBody ClienteDTO dados) {
		return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.salvar(null, dados));
	}

	@PutMapping("/{id}")
	public ClienteDTO editar(@PathVariable Integer id, @RequestBody ClienteDTO dados) {
		return clienteService.salvar(id, dados);
	}

	@DeleteMapping("/{id}")
	@Perfis(Cargo.PROPRIETARIO)
	public ResponseEntity<Void> excluir(@PathVariable Integer id) {
		clienteService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
