package br.edu.fatecriopreto.toolsync.seguranca;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.edu.fatecriopreto.toolsync.entities.Cargo;

/**
 * Perfis (cargos) que podem chamar o endpoint. Pode ir na classe do controller
 * (vale para todos os metodos) ou em cada metodo (tem prioridade).
 * Sem a anotacao, qualquer usuario logado pode chamar.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Perfis {
	Cargo[] value();
}
