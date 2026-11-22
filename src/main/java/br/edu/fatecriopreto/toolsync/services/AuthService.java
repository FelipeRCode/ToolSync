package br.edu.fatecriopreto.toolsync.services;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.fatecriopreto.toolsync.config.NegocioException;
import br.edu.fatecriopreto.toolsync.entities.FuncionarioEntity;
import br.edu.fatecriopreto.toolsync.entities.UsuarioEntity;
import br.edu.fatecriopreto.toolsync.repositories.UsuarioRepository;
import br.edu.fatecriopreto.toolsync.seguranca.UsuarioSessao;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	/** mesmo algoritmo do hash gravado em Usuarios.senha_hash */
	public static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

	private final UsuarioRepository usuarioRepository;

	@Transactional
	public UsuarioSessao autenticar(String login, String senha) {
		if (login == null || login.isBlank() || senha == null || senha.isBlank()) {
			throw new NegocioException("Informe o usuário e a senha.");
		}
		UsuarioEntity usuario = usuarioRepository.findByLogin(login.trim().toLowerCase())
				.orElseThrow(() -> new NegocioException("Usuário ou senha inválidos."));
		if (!BCRYPT.matches(senha, usuario.getSenhaHash().trim())) {
			throw new NegocioException("Usuário ou senha inválidos.");
		}
		FuncionarioEntity funcionario = usuario.getFuncionario();
		if (!Boolean.TRUE.equals(usuario.getAtivo()) || !Boolean.TRUE.equals(funcionario.getAtivo())) {
			throw new NegocioException("Este usuário está inativo. Procure o gerente ou o proprietário.");
		}
		usuario.setUltimoAcesso(LocalDateTime.now());
		return new UsuarioSessao(funcionario.getId(), funcionario.getNome(), usuario.getLogin(), funcionario.getCargo());
	}
}
