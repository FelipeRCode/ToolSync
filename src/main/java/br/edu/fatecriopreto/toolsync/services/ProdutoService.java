package br.edu.fatecriopreto.toolsync.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import br.edu.fatecriopreto.toolsync.config.NegocioException;
import br.edu.fatecriopreto.toolsync.dto.Dtos.ItemSimplesDTO;
import br.edu.fatecriopreto.toolsync.dto.Dtos.LocalEstoqueDTO;
import br.edu.fatecriopreto.toolsync.dto.Dtos.ProdutoDTO;
import br.edu.fatecriopreto.toolsync.entities.CategoriaEntity;
import br.edu.fatecriopreto.toolsync.entities.MarcaEntity;
import br.edu.fatecriopreto.toolsync.entities.ProdutoEntity;
import br.edu.fatecriopreto.toolsync.entities.ProdutoFornecedorEntity;
import br.edu.fatecriopreto.toolsync.entities.ProdutoFornecedorId;
import br.edu.fatecriopreto.toolsync.repositories.CategoriaRepository;
import br.edu.fatecriopreto.toolsync.repositories.FornecedorRepository;
import br.edu.fatecriopreto.toolsync.repositories.ItemVendaRepository;
import br.edu.fatecriopreto.toolsync.repositories.MarcaRepository;
import br.edu.fatecriopreto.toolsync.repositories.MovimentacaoEstoqueRepository;
import br.edu.fatecriopreto.toolsync.repositories.ProdutoFornecedorRepository;
import br.edu.fatecriopreto.toolsync.repositories.ProdutoLocalizacaoRepository;
import br.edu.fatecriopreto.toolsync.repositories.ProdutoRepository;
import br.edu.fatecriopreto.toolsync.seguranca.UsuarioSessao;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

	private final ProdutoRepository produtoRepository;
	private final CategoriaRepository categoriaRepository;
	private final MarcaRepository marcaRepository;
	private final FornecedorRepository fornecedorRepository;
	private final ProdutoFornecedorRepository produtoFornecedorRepository;
	private final ProdutoLocalizacaoRepository produtoLocalizacaoRepository;
	private final ItemVendaRepository itemVendaRepository;
	private final MovimentacaoEstoqueRepository movimentacaoRepository;
	private final EstoqueService estoqueService;
	private final TransactionTemplate transacao;

	@Transactional(readOnly = true)
	public List<ProdutoDTO> listarTodos(String categoria, String nome, String codigo, boolean somenteAtivos) {
		return produtoRepository.listarComCategoriaEMarca().stream()
				.filter(p -> !somenteAtivos || Boolean.TRUE.equals(p.getAtivo()))
				.filter(p -> vazio(categoria) || p.getCategoria().getNome().equalsIgnoreCase(categoria))
				.filter(p -> vazio(nome) || p.getNome().toLowerCase().contains(nome.toLowerCase()))
				.filter(p -> vazio(codigo) || p.getCodigo().toLowerCase().contains(codigo.toLowerCase()))
				.map(p -> dto(p, false)).toList();
	}

	@Transactional(readOnly = true)
	public ProdutoDTO buscar(Integer id) {
		return dto(obter(id), true);
	}

	@Transactional(readOnly = true)
	public ProdutoDTO buscarPorCodigo(String codigo) {
		return dto(produtoRepository.findByCodigo(codigo.trim())
				.orElseThrow(() -> new NoSuchElementException("Produto de código " + codigo + " não encontrado.")), true);
	}

	public List<ItemSimplesDTO> categorias() {
		return categoriaRepository.findAllByOrderByNomeAsc().stream().map(c -> new ItemSimplesDTO(c.getId(), c.getNome())).toList();
	}

	public List<ItemSimplesDTO> marcas() {
		return marcaRepository.findAllByOrderByNomeAsc().stream().map(m -> new ItemSimplesDTO(m.getId(), m.getNome())).toList();
	}

	@Transactional
	public ItemSimplesDTO incluirCategoria(String nome) {
		CategoriaEntity c = categoria(null, Validacao.obrigatorio(nome, "Nome da categoria"));
		return new ItemSimplesDTO(c.getId(), c.getNome());
	}

	@Transactional
	public ItemSimplesDTO incluirMarca(String nome) {
		MarcaEntity m = marca(null, Validacao.obrigatorio(nome, "Nome da marca"));
		return new ItemSimplesDTO(m.getId(), m.getNome());
	}

	/**
	 * Cadastra ou altera o produto. No cadastro, o estoque inicial entra pela
	 * procedure sp_EntradaEstoque (gera a movimentacao e o trigger soma o saldo).
	 */
	public ProdutoDTO incluir(ProdutoDTO dados, UsuarioSessao quem) {
		return salvar(null, dados, quem);
	}

	public ProdutoDTO editar(Integer id, ProdutoDTO dados, UsuarioSessao quem) {
		return salvar(id, dados, quem);
	}

	private ProdutoDTO salvar(Integer id, ProdutoDTO dados, UsuarioSessao quem) {
		Integer produtoId = transacao.execute(status -> gravar(id, dados));
		if (id == null && dados.estoqueInicial() != null && dados.estoqueInicial() > 0) {
			estoqueService.entrada(produtoId, dados.estoqueInicial(), dados.precoCusto(), dados.fornecedorId(),
					dados.localizacaoId(), "Estoque inicial no cadastro do produto", quem);
		}
		return transacao.execute(status -> dto(obter(produtoId), true));
	}

	private Integer gravar(Integer id, ProdutoDTO dados) {
		ProdutoEntity p = id == null ? new ProdutoEntity() : obter(id);
		String codigo = Validacao.obrigatorio(dados.codigo(), "Código");
		if (!codigo.equals(p.getCodigo()) && produtoRepository.existsByCodigo(codigo)) {
			throw new NegocioException("Já existe um produto com o código " + codigo + ".");
		}
		String nome = Validacao.obrigatorio(dados.nome(), "Nome");
		if (dados.precoVenda() == null || dados.precoVenda().compareTo(BigDecimal.ZERO) <= 0) {
			throw new NegocioException("Informe um preço de venda maior que zero.");
		}
		if (dados.precoCusto() != null && dados.precoCusto().compareTo(BigDecimal.ZERO) < 0) {
			throw new NegocioException("O preço de custo não pode ser negativo.");
		}
		if (dados.estoqueMinimo() != null && dados.estoqueMinimo() < 0) {
			throw new NegocioException("O estoque mínimo não pode ser negativo.");
		}

		p.setCodigo(codigo);
		p.setNome(nome);
		p.setDescricao(Validacao.opcional(dados.descricao()));
		p.setCategoria(categoria(dados.categoriaId(), dados.categoria()));
		p.setMarca(vazio(dados.marca()) && dados.marcaId() == null ? null : marca(dados.marcaId(), dados.marca()));
		p.setEan(Validacao.opcional(dados.ean()));
		p.setSku(Validacao.opcional(dados.sku()));
		p.setNcm(Validacao.opcional(dados.ncm()));
		if (dados.precoCusto() != null) {
			p.setPrecoCusto(dados.precoCusto());
		}
		p.setPrecoVenda(dados.precoVenda());
		p.setUnidadeCompra(vazio(dados.unidadeCompra()) ? "UN" : dados.unidadeCompra().trim().toUpperCase());
		p.setUnidadeVenda(vazio(dados.unidadeVenda()) ? "UN" : dados.unidadeVenda().trim().toUpperCase());
		p.setLarguraCm(positivoOuNulo(dados.larguraCm()));
		p.setAlturaCm(positivoOuNulo(dados.alturaCm()));
		p.setComprimentoCm(positivoOuNulo(dados.comprimentoCm()));
		p.setImagemUrl(Validacao.opcional(dados.imagemUrl()));
		p.setEstoqueMinimo(dados.estoqueMinimo() == null ? 0 : dados.estoqueMinimo());
		if (dados.ativo() != null) {
			p.setAtivo(dados.ativo());
		}
		produtoRepository.saveAndFlush(p);

		// vincula o fornecedor informado (N x N ProdutoFornecedor), se ainda nao estiver ligado
		if (dados.fornecedorId() != null && (dados.estoqueInicial() == null || dados.estoqueInicial() <= 0 || id != null)) {
			ProdutoFornecedorId chave = new ProdutoFornecedorId(p.getId(), dados.fornecedorId());
			if (!produtoFornecedorRepository.existsById(chave)) {
				ProdutoFornecedorEntity pf = new ProdutoFornecedorEntity();
				pf.setId(chave);
				pf.setProduto(p);
				pf.setFornecedor(fornecedorRepository.findById(dados.fornecedorId())
						.orElseThrow(() -> new NegocioException("Fornecedor não encontrado.")));
				produtoFornecedorRepository.saveAndFlush(pf);
			}
		}
		return p.getId();
	}

	/** exclui; se o produto ja foi vendido ou movimentado, so desativa */
	@Transactional
	public String excluir(Integer id) {
		ProdutoEntity p = obter(id);
		if (itemVendaRepository.existsByProdutoId(id) || movimentacaoRepository.existsByProdutoId(id)) {
			p.setAtivo(false);
			produtoRepository.saveAndFlush(p);
			return "O produto " + p.getNome() + " tem vendas ou movimentações registradas. Ele foi INATIVADO para manter o histórico.";
		}
		produtoFornecedorRepository.deleteAll(produtoFornecedorRepository.listarPorProduto(id));
		produtoLocalizacaoRepository.deleteAll(produtoLocalizacaoRepository.listarPorProduto(id));
		produtoRepository.delete(p);
		return "Produto " + p.getNome() + " excluído com sucesso.";
	}

	private ProdutoEntity obter(Integer id) {
		return produtoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Produto não encontrado."));
	}

	private CategoriaEntity categoria(Integer id, String nome) {
		if (id != null) {
			return categoriaRepository.findById(id).orElseThrow(() -> new NegocioException("Categoria não encontrada."));
		}
		String n = Validacao.obrigatorio(nome, "Categoria");
		return categoriaRepository.findByNome(n).orElseGet(() -> {
			CategoriaEntity c = new CategoriaEntity();
			c.setNome(n);
			return categoriaRepository.saveAndFlush(c);
		});
	}

	private MarcaEntity marca(Integer id, String nome) {
		if (id != null) {
			return marcaRepository.findById(id).orElseThrow(() -> new NegocioException("Marca não encontrada."));
		}
		String n = nome.trim();
		return marcaRepository.findByNome(n).orElseGet(() -> {
			MarcaEntity m = new MarcaEntity();
			m.setNome(n);
			return marcaRepository.saveAndFlush(m);
		});
	}

	private ProdutoDTO dto(ProdutoEntity p, boolean detalhes) {
		List<String> fornecedores = detalhes
				? produtoFornecedorRepository.listarPorProduto(p.getId()).stream().map(pf -> pf.getFornecedor().getNome()).toList()
				: null;
		List<LocalEstoqueDTO> locais = detalhes
				? produtoLocalizacaoRepository.listarPorProduto(p.getId()).stream().map(pl -> new LocalEstoqueDTO(
						pl.getLocalizacao().getId(), pl.getLocalizacao().getGalpao(), pl.getLocalizacao().getCorredor(),
						pl.getLocalizacao().getPrateleira(), pl.getLocalizacao().getBox(), pl.getQuantidade())).toList()
				: null;
		return new ProdutoDTO(p.getId(), p.getCodigo(), p.getNome(), p.getDescricao(),
				p.getCategoria().getId(), p.getCategoria().getNome(),
				p.getMarca() == null ? null : p.getMarca().getId(), p.getMarca() == null ? null : p.getMarca().getNome(),
				p.getEan(), p.getSku(), p.getNcm(), p.getPrecoCusto(), p.getPrecoVenda(), p.getUnidadeCompra(),
				p.getUnidadeVenda(), p.getLarguraCm(), p.getAlturaCm(), p.getComprimentoCm(), p.getImagemUrl(),
				p.getEstoqueAtual(), p.getEstoqueReservado(), p.getEstoqueDisponivel(), p.getEstoqueMinimo(), p.getAtivo(),
				fornecedores, locais, null, null, null);
	}

	private static BigDecimal positivoOuNulo(BigDecimal v) {
		return v == null || v.compareTo(BigDecimal.ZERO) <= 0 ? null : v;
	}

	private static boolean vazio(String s) {
		return s == null || s.isBlank();
	}
}
