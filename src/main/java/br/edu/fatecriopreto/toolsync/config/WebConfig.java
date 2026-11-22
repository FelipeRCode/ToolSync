package br.edu.fatecriopreto.toolsync.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import br.edu.fatecriopreto.toolsync.seguranca.AutenticacaoInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final AutenticacaoInterceptor autenticacaoInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// toda a API exige login, menos a propria tela de login
		registry.addInterceptor(autenticacaoInterceptor)
				.addPathPatterns("/api/**")
				.excludePathPatterns("/api/auth/login");
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		// http://localhost:8080 abre a tela de login do front-end
		registry.addRedirectViewController("/", "/login.html");
	}
}
