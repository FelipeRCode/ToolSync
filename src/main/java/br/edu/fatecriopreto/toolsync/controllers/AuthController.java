package br.edu.fatecriopreto.toolsync.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.fatecriopreto.toolsync.dto.Dtos.LoginRequest;
import br.edu.fatecriopreto.toolsync.dto.Dtos.UsuarioLogadoDTO;
import br.edu.fatecriopreto.toolsync.seguranca.UsuarioSessao;
import br.edu.fatecriopreto.toolsync.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/** Login e logout. O usuario fica guardado na sessao (cookie JSESSIONID). */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public UsuarioLogadoDTO login(@RequestBody LoginRequest dados, HttpServletRequest request) {
		UsuarioSessao usuario = authService.autenticar(dados.login(), dados.senha());
		HttpSession antiga = request.getSession(false);
		if (antiga != null) {
			antiga.invalidate();
		}
		request.getSession(true).setAttribute(UsuarioSessao.ATRIBUTO, usuario);
		return dto(usuario);
	}

	@GetMapping("/me")
	public UsuarioLogadoDTO usuarioLogado(@RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		return dto(usuario);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		HttpSession sessao = request.getSession(false);
		if (sessao != null) {
			sessao.invalidate();
		}
		return ResponseEntity.noContent().build();
	}

	private UsuarioLogadoDTO dto(UsuarioSessao u) {
		return new UsuarioLogadoDTO(u.funcionarioId(), u.nome(), u.login(), u.cargo().name(), u.cargoDescricao(), u.telaInicial());
	}
}
