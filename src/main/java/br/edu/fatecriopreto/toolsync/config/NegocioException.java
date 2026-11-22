package br.edu.fatecriopreto.toolsync.config;

/** Erro de regra de negocio: vira HTTP 400 com a mensagem para a tela. */
public class NegocioException extends RuntimeException {

	public NegocioException(String mensagem) {
		super(mensagem);
	}
}
