package br.edu.fatecriopreto.toolsync.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.fatecriopreto.toolsync.dto.Dtos.CancelarVendaRequest;
import br.edu.fatecriopreto.toolsync.dto.Dtos.ConcluirReservaRequest;
import br.edu.fatecriopreto.toolsync.dto.Dtos.VendaRequest;
import br.edu.fatecriopreto.toolsync.entities.Cargo;
import br.edu.fatecriopreto.toolsync.seguranca.Perfis;
import br.edu.fatecriopreto.toolsync.seguranca.UsuarioSessao;
import br.edu.fatecriopreto.toolsync.services.VendaService;
import lombok.RequiredArgsConstructor;

/**
 * Vendas. Gravacao pelas procedures (sp_RegistrarVenda, sp_ConcluirReserva,
 * sp_CancelarVenda); consulta pela view v_VendasDetalhadas.
 */
@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
@Perfis({ Cargo.PROPRIETARIO, Cargo.VENDEDOR })
public class VendaController {

	private final VendaService vendaService;

	@GetMapping
	@Perfis({ Cargo.PROPRIETARIO, Cargo.GERENTE, Cargo.VENDEDOR })
	public ResponseEntity<List<Map<String, Object>>> listarTodos(@RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		return ResponseEntity.ok().body(vendaService.listarTodos(usuario));
	}

	@GetMapping("/{id}")
	@Perfis({ Cargo.PROPRIETARIO, Cargo.GERENTE, Cargo.VENDEDOR })
	public ResponseEntity<Map<String, Object>> detalhe(@PathVariable Integer id, @RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		return ResponseEntity.ok().body(vendaService.detalhe(id, usuario));
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> incluir(@RequestBody VendaRequest venda, @RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		return new ResponseEntity<>(vendaService.registrar(venda, usuario), HttpStatus.CREATED);
	}

	@PostMapping("/{id}/concluir")
	public ResponseEntity<Map<String, Object>> concluirReserva(@PathVariable Integer id, @RequestBody ConcluirReservaRequest dados,
			@RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		return ResponseEntity.ok().body(vendaService.concluirReserva(id, dados.pagamentos(), usuario));
	}

	@PostMapping("/{id}/cancelar")
	@Perfis({ Cargo.PROPRIETARIO, Cargo.GERENTE })
	public ResponseEntity<Map<String, Object>> cancelar(@PathVariable Integer id, @RequestBody CancelarVendaRequest dados,
			@RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		return ResponseEntity.ok().body(vendaService.cancelar(id, dados.motivo(), usuario));
	}
}
