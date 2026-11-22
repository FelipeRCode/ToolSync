package br.edu.fatecriopreto.toolsync.seguranca;

import java.io.Serializable;

import br.edu.fatecriopreto.toolsync.entities.Cargo;

/**
 * Dados do usuario logado guardados na sessao HTTP (cookie JSESSIONID).
 */
public record UsuarioSessao(Integer funcionarioId, String nome, String login, Cargo cargo) implements Serializable {

	public static final String ATRIBUTO = "usuarioLogado";

	/** pagina inicial do front-end de cada perfil */
	public String telaInicial() {
		return switch (cargo) {
			case PROPRIETARIO -> "proprietario/telainicialp.html";
			case GERENTE -> "gerente/telainicialg.html";
			case VENDEDOR -> "vendedor/telainicialv.html";
			case ESTOQUISTA -> "estoquista/telainiciale.html";
		};
	}

	/** nome do cargo do jeito que as telas mostram */
	public String cargoDescricao() {
		return descricao(cargo);
	}

	public static String descricao(Cargo cargo) {
		return switch (cargo) {
			case PROPRIETARIO -> "Proprietário";
			case GERENTE -> "Gerente";
			case VENDEDOR -> "Vendedor";
			case ESTOQUISTA -> "Estoquista";
		};
	}
}
