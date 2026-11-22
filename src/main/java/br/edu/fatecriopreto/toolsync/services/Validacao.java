package br.edu.fatecriopreto.toolsync.services;

import br.edu.fatecriopreto.toolsync.config.NegocioException;

/**
 * Validacoes de formato feitas antes de gravar, com mensagens claras para a
 * tela. O banco confere as mesmas regras com CHECK constraints.
 */
public final class Validacao {

	private Validacao() {
	}

	public static String obrigatorio(String valor, String campo) {
		if (valor == null || valor.isBlank()) {
			throw new NegocioException("Preencha o campo " + campo + ".");
		}
		return valor.trim();
	}

	/** devolve null para texto vazio (assim a coluna fica NULL no banco) */
	public static String opcional(String valor) {
		return valor == null || valor.isBlank() ? null : valor.trim();
	}

	public static void nome(String valor) {
		if (valor.length() < 3) {
			throw new NegocioException("O nome precisa ter pelo menos 3 letras.");
		}
	}

	public static boolean cpfValido(String v) {
		return v != null && v.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");
	}

	public static boolean cnpjValido(String v) {
		return v != null && v.matches("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}");
	}

	public static void cpf(String v) {
		if (!cpfValido(v)) {
			throw new NegocioException("CPF inválido. Use o formato 000.000.000-00.");
		}
	}

	public static void cnpj(String v) {
		if (!cnpjValido(v)) {
			throw new NegocioException("CNPJ inválido. Use o formato 00.000.000/0000-00.");
		}
	}

	public static void telefone(String v) {
		if (v != null && !v.matches("\\(\\d{2}\\) \\d{4,5}-\\d{4}")) {
			throw new NegocioException("Telefone inválido. Use (00) 0000-0000 ou (00) 00000-0000.");
		}
	}

	public static void email(String v) {
		if (v != null && !v.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
			throw new NegocioException("E-mail inválido.");
		}
	}

	public static void cep(String v) {
		if (v != null && !v.matches("\\d{5}-\\d{3}")) {
			throw new NegocioException("CEP inválido. Use o formato 00000-000.");
		}
	}

	public static String uf(String v) {
		String uf = opcional(v);
		if (uf == null) {
			return null;
		}
		uf = uf.toUpperCase();
		if (!uf.matches("AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO")) {
			throw new NegocioException("UF inválida.");
		}
		return uf;
	}
}
