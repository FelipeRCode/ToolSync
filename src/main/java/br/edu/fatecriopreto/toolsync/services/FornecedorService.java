package br.edu.fatecriopreto.toolsync.services;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.fatecriopreto.toolsync.config.NegocioException;
import br.edu.fatecriopreto.toolsync.dto.Dtos.FornecedorDTO;
import br.edu.fatecriopreto.toolsync.entities.FornecedorEntity;
import br.edu.fatecriopreto.toolsync.repositories.FornecedorRepository;
import br.edu.fatecriopreto.toolsync.repositories.MovimentacaoEstoqueRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FornecedorService {

	private static final List<String> REGIMES = List.of("Simples Nacional", "Lucro Presumido", "Lucro Real");

	private final FornecedorRepository fornecedorRepository;
	private final MovimentacaoEstoqueRepository movimentacaoRepository;
	private final BancoService banco;

	public List<FornecedorDTO> listar(boolean somenteAtivos) {
		List<FornecedorEntity> lista = somenteAtivos ? fornecedorRepository.findByAtivoTrueOrderByNomeAsc()
				: fornecedorRepository.findAllByOrderByNomeAsc();
		return lista.stream().map(this::dto).toList();
	}

	public FornecedorDTO buscar(Integer id) {
		return dto(obter(id));
	}

	@Transactional
	public FornecedorDTO salvar(Integer id, FornecedorDTO dados) {
		FornecedorEntity f = id == null ? new FornecedorEntity() : obter(id);
		String razao = Validacao.obrigatorio(dados.razaoSocial(), "Razão Social");
		Validacao.nome(razao);
		String cnpj = Validacao.obrigatorio(dados.cnpj(), "CNPJ");
		Validacao.cnpj(cnpj);
		if (!cnpj.equals(f.getCnpj()) && fornecedorRepository.existsByCnpj(cnpj)) {
			throw new NegocioException("Já existe um fornecedor com este CNPJ.");
		}
		String regime = Validacao.obrigatorio(dados.regimeTributario(), "Regime Tributário");
		if (!REGIMES.contains(regime)) {
			throw new NegocioException("Regime tributário inválido. Use Simples Nacional, Lucro Presumido ou Lucro Real.");
		}
		String telefone = Validacao.obrigatorio(dados.telefone(), "Telefone");
		Validacao.telefone(telefone);
		String email = Validacao.opcional(dados.email());
		Validacao.email(email);
		String cep = Validacao.opcional(dados.cep());
		Validacao.cep(cep);

		f.setNome(razao);
		f.setCnpj(cnpj);
		f.setRamo(Validacao.obrigatorio(dados.tipoProduto(), "Tipo de Produto"));
		f.setRegimeTributario(regime);
		f.setRepresentante(Validacao.obrigatorio(dados.representante(), "Representante"));
		f.setTelefone(telefone);
		f.setEmail(email);
		f.setDadosBancarios(Validacao.opcional(dados.dadosBancarios()));
		f.setRua(Validacao.opcional(dados.rua()));
		f.setNumero(Validacao.opcional(dados.numero()));
		f.setBairro(Validacao.opcional(dados.bairro()));
		f.setCep(cep);
		f.setCidade(Validacao.opcional(dados.cidade()));
		f.setUf(Validacao.uf(dados.uf()));
		if (dados.ativo() != null) {
			f.setAtivo(dados.ativo());
		}
		fornecedorRepository.saveAndFlush(f);
		return dto(f);
	}

	/** exclui; se ja forneceu produtos ou teve entradas de estoque, so desativa */
	@Transactional
	public String excluir(Integer id) {
		FornecedorEntity f = obter(id);
		Integer vinculos = (Integer) banco.consultarUm("select count(*) as n from ProdutoFornecedor where fornecedor_id = ?", id).get("n");
		if (vinculos > 0 || movimentacaoRepository.existsByFornecedorId(id)) {
			f.setAtivo(false);
			fornecedorRepository.saveAndFlush(f);
			return "O fornecedor " + f.getNome() + " tem produtos e entradas de estoque registrados. Ele foi DESATIVADO para manter o histórico.";
		}
		fornecedorRepository.delete(f);
		return "Fornecedor " + f.getNome() + " excluído com sucesso.";
	}

	private FornecedorEntity obter(Integer id) {
		return fornecedorRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Fornecedor não encontrado."));
	}

	private FornecedorDTO dto(FornecedorEntity f) {
		return new FornecedorDTO(f.getId(), String.format("%03d", f.getId()), f.getNome(), f.getCnpj(), f.getRamo(),
				f.getRegimeTributario(), f.getRepresentante(), f.getTelefone(), f.getEmail(), f.getDadosBancarios(),
				f.getRua(), f.getNumero(), f.getBairro(), f.getCep(), f.getCidade(), f.getUf(), f.getAtivo());
	}
}
