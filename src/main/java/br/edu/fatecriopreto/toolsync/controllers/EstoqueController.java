package br.edu.fatecriopreto.toolsync.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.fatecriopreto.toolsync.dto.Dtos.EntradaEstoqueRequest;
import br.edu.fatecriopreto.toolsync.dto.Dtos.LocalEstoqueDTO;
import br.edu.fatecriopreto.toolsync.entities.Cargo;
import br.edu.fatecriopreto.toolsync.seguranca.Perfis;
import br.edu.fatecriopreto.toolsync.seguranca.UsuarioSessao;
import br.edu.fatecriopreto.toolsync.services.EstoqueService;
import lombok.RequiredArgsConstructor;

/** Estoque: entradas (procedure), movimentacoes e estoque baixo (views). */
@RestController
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
@Perfis({ Cargo.ESTOQUISTA, Cargo.GERENTE, Cargo.PROPRIETARIO })
public class EstoqueController {

	private final EstoqueService estoqueService;

	@PostMapping("/entradas")
	public ResponseEntity<Map<String, Object>> entrada(@RequestBody EntradaEstoqueRequest dados, @RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		return ResponseEntity.ok().body(estoqueService.entrada(dados.produtoId(), dados.quantidade(), dados.precoCusto(), dados.fornecedorId(),
				dados.localizacaoId(), dados.documento(), usuario));
	}

	@GetMapping("/movimentacoes")
	public ResponseEntity<List<Map<String, Object>>> movimentacoes(@RequestParam(required = false) String codigo) {
		return ResponseEntity.ok().body(estoqueService.movimentacoes(codigo));
	}

	/** usado nos dashboards de todos os perfis */
	@GetMapping("/baixo")
	@Perfis({ Cargo.ESTOQUISTA, Cargo.GERENTE, Cargo.PROPRIETARIO, Cargo.VENDEDOR })
	public ResponseEntity<List<Map<String, Object>>> estoqueBaixo() {
		return ResponseEntity.ok().body(estoqueService.estoqueBaixo());
	}

	@GetMapping("/saidas")
	public ResponseEntity<List<Map<String, Object>>> ultimasSaidas(@RequestParam(defaultValue = "5") int quantidade) {
		return ResponseEntity.ok().body(estoqueService.ultimasSaidas(Math.min(Math.max(quantidade, 1), 50)));
	}

	@GetMapping("/localizacoes")
	public ResponseEntity<List<LocalEstoqueDTO>> localizacoes() {
		return ResponseEntity.ok().body(estoqueService.localizacoes());
	}
}
