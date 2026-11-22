package br.edu.fatecriopreto.toolsync.seguranca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Confere, antes de cada chamada da API, se ha usuario logado (401) e se o
 * perfil dele pode usar aquele endpoint (403), conforme a anotacao @Perfis.
 */
@Component
public class AutenticacaoInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
		if (!(handler instanceof HandlerMethod metodo)) {
			return true;
		}

		HttpSession sessao = request.getSession(false);
		UsuarioSessao usuario = sessao == null ? null : (UsuarioSessao) sessao.getAttribute(UsuarioSessao.ATRIBUTO);
		if (usuario == null) {
			responder(response, HttpServletResponse.SC_UNAUTHORIZED, "Sua sessão expirou. Faça login novamente.");
			return false;
		}

		Perfis perfis = metodo.getMethodAnnotation(Perfis.class);
		if (perfis == null) {
			perfis = metodo.getBeanType().getAnnotation(Perfis.class);
		}
		if (perfis != null && Arrays.stream(perfis.value()).noneMatch(c -> c == usuario.cargo())) {
			responder(response, HttpServletResponse.SC_FORBIDDEN, "Seu perfil (" + usuario.cargoDescricao() + ") não tem acesso a esta função.");
			return false;
		}

		request.setAttribute(UsuarioSessao.ATRIBUTO, usuario);
		return true;
	}

	private void responder(HttpServletResponse response, int status, String mensagem) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write("{\"mensagem\":\"" + mensagem.replace("\"", "'") + "\"}");
	}
}
