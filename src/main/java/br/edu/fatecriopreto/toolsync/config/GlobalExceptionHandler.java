package br.edu.fatecriopreto.toolsync.config;

import java.sql.SQLException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converte os erros em respostas JSON {"mensagem": "..."} que as telas mostram.
 * Os THROW das procedures e triggers (erro 50000 ou mais) chegam aqui com a
 * mensagem escrita no SQL, por exemplo "Estoque insuficiente para o produto ...".
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final Pattern VALOR_DUPLICADO = Pattern.compile("valor da chave duplicada é \\((.*?)\\)|duplicate key value is \\((.*?)\\)");

	@ExceptionHandler(NegocioException.class)
	public ResponseEntity<Map<String, String>> negocio(NegocioException e) {
		return resposta(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<Map<String, String>> naoEncontrado(NoSuchElementException e) {
		return resposta(HttpStatus.NOT_FOUND, e.getMessage() == null ? "Registro não encontrado." : e.getMessage());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException e) {
		return resposta(HttpStatus.BAD_REQUEST, "Dados enviados em formato inválido. Confira datas e valores numéricos.");
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<Map<String, String>> banco(DataAccessException e) {
		Throwable causa = NestedExceptionUtils.getMostSpecificCause(e);
		if (causa instanceof SQLException sql) {
			int codigo = sql.getErrorCode();
			if (codigo >= 50000) {
				// THROW das procedures / triggers: a mensagem ja e para o usuario
				return resposta(HttpStatus.BAD_REQUEST, sql.getMessage());
			}
			if (codigo == 2627 || codigo == 2601) {
				Matcher m = VALOR_DUPLICADO.matcher(sql.getMessage());
				String valor = m.find() ? (m.group(1) != null ? m.group(1) : m.group(2)) : null;
				return resposta(HttpStatus.CONFLICT, valor == null ? "Já existe um registro com estes dados."
						: "Já existe um registro com o valor " + valor + ".");
			}
			if (codigo == 547) {
				String msg = sql.getMessage().toLowerCase();
				if (msg.contains("delete")) {
					return resposta(HttpStatus.CONFLICT, "Não é possível excluir: o registro está ligado a outros dados.");
				}
				return resposta(HttpStatus.BAD_REQUEST, "Algum campo está fora do formato ou do valor permitido. Confira os dados e tente de novo.");
			}
			if (codigo == 515) {
				return resposta(HttpStatus.BAD_REQUEST, "Preencha todos os campos obrigatórios.");
			}
		}
		log.error("Erro de banco", e);
		return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro no banco de dados: " + causa.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> geral(Exception e) {
		log.error("Erro inesperado", e);
		return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado: " + e.getMessage());
	}

	private ResponseEntity<Map<String, String>> resposta(HttpStatus status, String mensagem) {
		return ResponseEntity.status(status).body(Map.of("mensagem", mensagem));
	}
}
