package br.edu.fatecriopreto.toolsync.services;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.fatecriopreto.toolsync.config.NegocioException;
import br.edu.fatecriopreto.toolsync.dto.Dtos.FuncionarioDTO;
import br.edu.fatecriopreto.toolsync.entities.Cargo;
import br.edu.fatecriopreto.toolsync.entities.FuncionarioEntity;
import br.edu.fatecriopreto.toolsync.entities.UsuarioEntity;
import br.edu.fatecriopreto.toolsync.repositories.FuncionarioRepository;
import br.edu.fatecriopreto.toolsync.repositories.MovimentacaoEstoqueRepository;
import br.edu.fatecriopreto.toolsync.repositories.UsuarioRepository;
import br.edu.fatecriopreto.toolsync.repositories.VendaRepository;
import br.edu.fatecriopreto.toolsync.seguranca.UsuarioSessao;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

	private final FuncionarioRepository funcionarioRepository;
	private final UsuarioRepository usuarioRepository;
	private final VendaRepository vendaRepository;
	private final MovimentacaoEstoqueRepository movimentacaoRepository;
	private final BancoService banco;

	public List<FuncionarioDTO> listarTodos() {
		return funcionarioRepository.findAllByOrderByNomeAsc().stream()
				.map(f -> dto(f, usuarioRepository.findById(f.getId()).orElse(null))).toList();
	}

	public FuncionarioDTO buscar(Integer id) {
		FuncionarioEntity f = obter(id);
		return dto(f, usuarioRepository.findById(id).orElse(null));
	}

	/**
	 * Cadastro pela procedure sp_CadastrarFuncionario: Pessoas + Funcionarios +
	 * Usuarios numa unica transacao. A senha vira hash BCrypt aqui no Java.
	 */
	public FuncionarioDTO incluir(FuncionarioDTO dados, UsuarioSessao quem) {
		Cargo cargo = cargo(dados.cargo());
		verificarPermissao(quem, cargo);
		String nome = Validacao.obrigatorio(dados.nome(), "Nome");
		Validacao.nome(nome);
		String cpf = Validacao.obrigatorio(dados.cpf(), "CPF");
		Validacao.cpf(cpf);
		String celular = Validacao.obrigatorio(dados.celular(), "Celular");
		Validacao.telefone(celular);
		String email = Validacao.obrigatorio(dados.email(), "E-mail");
		Validacao.email(email);
		String cep = Validacao.opcional(dados.cep());
		Validacao.cep(cep);
		if (dados.dataNascimento() == null) {
			throw new NegocioException("Preencha a data de nascimento.");
		}
		String login = Validacao.obrigatorio(dados.login(), "Login").toLowerCase();
		String senha = Validacao.obrigatorio(dados.senha(), "Senha");
		if (senha.length() < 6) {
			throw new NegocioException("A senha precisa ter pelo menos 6 caracteres.");
		}

		Map<String, Object> r = banco.consultarUm("""
				exec sp_CadastrarFuncionario @nome = ?, @email = ?, @telefone = ?, @rua = ?, @numero = ?,
				     @bairro = ?, @cep = ?, @cidade = ?, @uf = ?, @matricula = ?, @rg = ?, @cpf = ?,
				     @dataNascimento = ?, @cargo = ?, @login = ?, @senhaHash = ?
				""",
				nome, email, celular, Validacao.opcional(dados.rua()), Validacao.opcional(dados.numero()),
				Validacao.opcional(dados.bairro()), cep, Validacao.opcional(dados.cidade()), Validacao.uf(dados.uf()),
				Validacao.obrigatorio(dados.matricula(), "Matrícula"), Validacao.obrigatorio(dados.rg(), "RG"), cpf,
				java.sql.Date.valueOf(dados.dataNascimento()), cargo.name(), login, AuthService.BCRYPT.encode(senha));
		return buscar((Integer) r.get("funcionario_id"));
	}

	@Transactional
	public FuncionarioDTO editar(Integer id, FuncionarioDTO dados, UsuarioSessao quem) {
		FuncionarioEntity f = obter(id);
		Cargo novoCargo = cargo(dados.cargo());
		verificarPermissao(quem, f.getCargo());
		verificarPermissao(quem, novoCargo);
		if (f.getId().equals(quem.funcionarioId()) && novoCargo != f.getCargo()) {
			throw new NegocioException("Você não pode alterar o seu próprio cargo.");
		}

		String nome = Validacao.obrigatorio(dados.nome(), "Nome");
		Validacao.nome(nome);
		String cpf = Validacao.obrigatorio(dados.cpf(), "CPF");
		Validacao.cpf(cpf);
		String celular = Validacao.obrigatorio(dados.celular(), "Celular");
		Validacao.telefone(celular);
		String email = Validacao.obrigatorio(dados.email(), "E-mail");
		Validacao.email(email);
		String cep = Validacao.opcional(dados.cep());
		Validacao.cep(cep);

		f.setNome(nome);
		f.setMatricula(Validacao.obrigatorio(dados.matricula(), "Matrícula"));
		f.setRg(Validacao.obrigatorio(dados.rg(), "RG"));
		f.setCpf(cpf);
		if (dados.dataNascimento() != null) {
			f.setDataNascimento(dados.dataNascimento());
		}
		f.setCargo(novoCargo);
		f.setTelefone(celular);
		f.setEmail(email);
		f.setRua(Validacao.opcional(dados.rua()));
		f.setNumero(Validacao.opcional(dados.numero()));
		f.setBairro(Validacao.opcional(dados.bairro()));
		f.setCep(cep);
		f.setCidade(Validacao.opcional(dados.cidade()));
		f.setUf(Validacao.uf(dados.uf()));
		if (dados.ativo() != null) {
			if (!dados.ativo() && f.getId().equals(quem.funcionarioId())) {
				throw new NegocioException("Você não pode desativar o seu próprio usuário.");
			}
			// ao desativar, o trigger tg_Funcionarios_SincronizarUsuario desativa o login
			f.setAtivo(dados.ativo());
		}
		funcionarioRepository.saveAndFlush(f);

		UsuarioEntity usuario = usuarioRepository.findById(id).orElse(null);
		if (usuario != null) {
			String login = Validacao.opcional(dados.login());
			if (login != null && !login.equalsIgnoreCase(usuario.getLogin())) {
				if (usuarioRepository.existsByLogin(login.toLowerCase())) {
					throw new NegocioException("Este login já está em uso.");
				}
				usuario.setLogin(login.toLowerCase());
			}
			String senha = Validacao.opcional(dados.senha());
			if (senha != null) {
				if (senha.length() < 6) {
					throw new NegocioException("A senha precisa ter pelo menos 6 caracteres.");
				}
				usuario.setSenhaHash(AuthService.BCRYPT.encode(senha));
			}
		}
		return buscar(id);
	}

	/**
	 * Exclui o funcionario. Se ele ja tem vendas ou movimentacoes de estoque, o
	 * historico precisa ser mantido: nesse caso ele e desativado.
	 */
	@Transactional
	public String excluir(Integer id, UsuarioSessao quem) {
		FuncionarioEntity f = obter(id);
		verificarPermissao(quem, f.getCargo());
		if (f.getId().equals(quem.funcionarioId())) {
			throw new NegocioException("Você não pode excluir o seu próprio usuário.");
		}
		if (vendaRepository.existsByVendedorId(id) || movimentacaoRepository.existsByFuncionarioId(id)) {
			f.setAtivo(false);
			funcionarioRepository.saveAndFlush(f);
			return "O funcionário " + f.getNome() + " tem vendas ou movimentações registradas. Ele foi DESATIVADO (o login foi bloqueado) para manter o histórico.";
		}
		usuarioRepository.findById(id).ifPresent(usuarioRepository::delete);
		usuarioRepository.flush();
		funcionarioRepository.delete(f);
		return "Funcionário " + f.getNome() + " excluído com sucesso.";
	}

	private FuncionarioEntity obter(Integer id) {
		return funcionarioRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Funcionário não encontrado."));
	}

	/** gerente so administra vendedores e estoquistas */
	private void verificarPermissao(UsuarioSessao quem, Cargo cargoAlvo) {
		if (quem.cargo() == Cargo.GERENTE && (cargoAlvo == Cargo.PROPRIETARIO || cargoAlvo == Cargo.GERENTE)) {
			throw new NegocioException("O gerente só pode cadastrar e alterar vendedores e estoquistas.");
		}
	}

	private Cargo cargo(String valor) {
		String v = Validacao.obrigatorio(valor, "Função").toUpperCase()
				.replace("Á", "A").replace("PROPRIETÁRIO", "PROPRIETARIO");
		if (v.startsWith("VENDEDOR")) {
			return Cargo.VENDEDOR;
		}
		try {
			return Cargo.valueOf(v);
		} catch (IllegalArgumentException e) {
			throw new NegocioException("Função inválida. Use Proprietário, Gerente, Vendedor ou Estoquista.");
		}
	}

	private FuncionarioDTO dto(FuncionarioEntity f, UsuarioEntity u) {
		return new FuncionarioDTO(f.getId(), String.format("%03d", f.getId()), f.getNome(), f.getMatricula(), f.getRg(),
				f.getCpf(), f.getDataNascimento(), f.getCargo().name(), UsuarioSessao.descricao(f.getCargo()), f.getEmail(),
				f.getTelefone(), f.getRua(), f.getNumero(), f.getBairro(), f.getCep(), f.getCidade(), f.getUf(),
				f.getAtivo(), u == null ? null : u.getLogin(), null);
	}
}
