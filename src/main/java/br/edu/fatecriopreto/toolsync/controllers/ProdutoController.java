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
	public List<ProdutoDTO> listar(@RequestParam(required = false) String categoria, @RequestParam(required = false) String nome,
			@RequestParam(required = false) String codigo, @RequestParam(defaultValue = "false") boolean ativos) {
		return produtoService.listar(categoria, nome, codigo, ativos);
	}

	@GetMapping("/produtos/{id}")
	public ProdutoDTO buscar(@PathVariable Integer id) {
		return produtoService.buscar(id);
	}

	@GetMapping("/produtos/codigo/{codigo}")
	public ProdutoDTO buscarPorCodigo(@PathVariable String codigo) {
		return produtoService.buscarPorCodigo(codigo);
	}

	@PostMapping("/produtos")
	@Perfis({ Cargo.ESTOQUISTA, Cargo.GERENTE, Cargo.PROPRIETARIO })
	public ResponseEntity<ProdutoDTO> incluir(@RequestBody ProdutoDTO dados, @RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.salvar(null, dados, usuario));
	}

	@PutMapping("/produtos/{id}")
	@Perfis({ Cargo.ESTOQUISTA, Cargo.GERENTE, Cargo.PROPRIETARIO })
	public ProdutoDTO editar(@PathVariable Integer id, @RequestBody ProdutoDTO dados, @RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		return produtoService.salvar(id, dados, usuario);
	}

	@DeleteMapping("/produtos/{id}")
	@Perfis({ Cargo.GERENTE, Cargo.PROPRIETARIO })
	public Map<String, String> excluir(@PathVariable Integer id) {
		return Map.of("mensagem", produtoService.excluir(id));
	}

	@GetMapping("/categorias")
	public List<ItemSimplesDTO> categorias() {
		return produtoService.categorias();
	}

	@PostMapping("/categorias")
	@Perfis({ Cargo.ESTOQUISTA, Cargo.GERENTE, Cargo.PROPRIETARIO })
	public ItemSimplesDTO incluirCategoria(@RequestBody ItemSimplesDTO dados) {
		return produtoService.incluirCategoria(dados.nome());
	}

	@GetMapping("/marcas")
	public List<ItemSimplesDTO> marcas() {
		return produtoService.marcas();
	}

	@PostMapping("/marcas")
	@Perfis({ Cargo.ESTOQUISTA, Cargo.GERENTE, Cargo.PROPRIETARIO })
	public ItemSimplesDTO incluirMarca(@RequestBody ItemSimplesDTO dados) {
		return produtoService.incluirMarca(dados.nome());
	}
}
