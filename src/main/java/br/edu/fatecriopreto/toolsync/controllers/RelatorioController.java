package br.edu.fatecriopreto.toolsync.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.fatecriopreto.toolsync.entities.Cargo;
import br.edu.fatecriopreto.toolsync.seguranca.Perfis;
import br.edu.fatecriopreto.toolsync.seguranca.UsuarioSessao;
import br.edu.fatecriopreto.toolsync.services.RelatorioService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RelatorioController {

	private final RelatorioService relatorioService;

	/** dashboard de qualquer perfil logado */
	@GetMapping("/dashboard")
	public Map<String, Object> dashboard(@RequestAttribute(UsuarioSessao.ATRIBUTO) UsuarioSessao usuario) {
		return relatorioService.dashboard(usuario);
	}

	@GetMapping("/relatorios/vendas")
	@Perfis({ Cargo.PROPRIETARIO, Cargo.GERENTE })
	public List<Map<String, Object>> vendas(@RequestParam(required = false) String vendedor, @RequestParam(required = false) String peca,
			@RequestParam(required = false) String cliente) {
		return relatorioService.vendas(vendedor, peca, cliente);
	}

	@GetMapping("/relatorios/vendedores")
	@Perfis({ Cargo.PROPRIETARIO, Cargo.GERENTE })
	public List<Map<String, Object>> vendedores(@RequestParam(required = false) LocalDate inicio, @RequestParam(required = false) LocalDate fim) {
		LocalDate f = fim == null ? LocalDate.now() : fim;
		LocalDate i = inicio == null ? f.withDayOfMonth(1) : inicio;
		return relatorioService.vendedores(i, f);
	}
}
