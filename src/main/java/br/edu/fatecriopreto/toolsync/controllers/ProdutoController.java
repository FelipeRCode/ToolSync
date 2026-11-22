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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.fatecriopreto.toolsync.dto.Dtos.ItemSimplesDTO;
import br.edu.fatecriopreto.toolsync.dto.Dtos.ProdutoDTO;
import br.edu.fatecriopreto.toolsync.entities.Cargo;
import br.edu.fatecriopreto.toolsync.seguranca.Perfis;
import br.edu.fatecriopreto.toolsync.seguranca.UsuarioSessao;
import br.edu.fatecriopreto.toolsync.services.ProdutoService;
import lombok.RequiredArgsConstructor;

/**
 * Produtos, categorias e marcas. Todos consultam (o vendedor precisa para
 * vender); cadastrar e alterar: Estoquista, Gerente e Proprietario.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProdutoController {

	private final ProdutoService produtoService;

	@GetMapping("/produtos")
	public ResponseEntity<List<ProdutoDTO>> listarTodos(@RequestParam(required = false) String categoria,
			@RequestParam(required = false) String nome, @RequestParam(required = false) String codigo,
			@RequestParam(defaultValue = "false") boolean ativos) {
		List<ProdutoDTO> lista = produtoService.listarTodos(categoria, nome, codigo, ativos);
		return ResponseEntity.ok().body(lista);
	}

	@GetMapping("/produtos/{id}")
	public ResponseEntity<ProdutoDTO> buscar(@PathVariable Integer id) {
		return ResponseEntity.ok().body(produtoService.buscar(id));
	}

	@GetMapping("/produtos/codigo/{codigo}")
	public ResponseEntity<ProdutoDTO> buscarPorCodigo(@PathVariable String codigo) {
		return ResponseEntity.ok().body(produtoService.buscarPorCodigo(codigo));
	}

	@PostMapping("/produtos")
	@Perfis({ Cargo.ESTOQUISTA, Cargo.GERENTE, Cargo.PROPRIETARIO })
	public ResponseEntity<ProdutoDTO> incluir(@RequestBody ProdutoDTO produto, @RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		ProdutoDTO novo = produtoService.incluir(produto, usuario);
		return new ResponseEntity<>(novo, HttpStatus.CREATED);
	}

	@PutMapping("/produtos/{id}")
	@Perfis({ Cargo.ESTOQUISTA, Cargo.GERENTE, Cargo.PROPRIETARIO })
	public ResponseEntity<ProdutoDTO> editar(@PathVariable Integer id, @RequestBody ProdutoDTO produto,
			@RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		ProdutoDTO atualizado = produtoService.editar(id, produto, usuario);
		return new ResponseEntity<>(atualizado, HttpStatus.OK);
	}

	@DeleteMapping("/produtos/{id}")
	@Perfis({ Cargo.GERENTE, Cargo.PROPRIETARIO })
	public ResponseEntity<Map<String, String>> excluir(@PathVariable Integer id) {
		String mensagem = produtoService.excluir(id);
		return ResponseEntity.ok().body(Map.of("mensagem", mensagem));
	}

	@GetMapping("/categorias")
	public ResponseEntity<List<ItemSimplesDTO>> categorias() {
		return ResponseEntity.ok().body(produtoService.categorias());
	}

	@PostMapping("/categorias")
	@Perfis({ Cargo.ESTOQUISTA, Cargo.GERENTE, Cargo.PROPRIETARIO })
	public ResponseEntity<ItemSimplesDTO> incluirCategoria(@RequestBody ItemSimplesDTO categoria) {
		return new ResponseEntity<>(produtoService.incluirCategoria(categoria.nome()), HttpStatus.CREATED);
	}

	@GetMapping("/marcas")
	public ResponseEntity<List<ItemSimplesDTO>> marcas() {
		return ResponseEntity.ok().body(produtoService.marcas());
	}

	@PostMapping("/marcas")
	@Perfis({ Cargo.ESTOQUISTA, Cargo.GERENTE, Cargo.PROPRIETARIO })
	public ResponseEntity<ItemSimplesDTO> incluirMarca(@RequestBody ItemSimplesDTO marca) {
		return new ResponseEntity<>(produtoService.incluirMarca(marca.nome()), HttpStatus.CREATED);
	}
}
