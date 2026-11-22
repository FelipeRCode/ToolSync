package br.edu.fatecriopreto.toolsync.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Nomes usados pelo Hibernate no banco: as tabelas ficam exatamente como no
 * @Table (ex.: ProdutoFornecedor) e as colunas viram snake_case
 * (ex.: dataVenda -> data_venda), igual ao script SQL.
 */
public class NomesBancoStrategy implements PhysicalNamingStrategy {

	@Override
	public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment context) {
		return name;
	}

	@Override
	public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment context) {
		return name;
	}

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
		return name;
	}

	@Override
	public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
		return name;
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
		if (name == null) {
			return null;
		}
		// dataVenda -> data_venda ; larguraCm -> largura_cm
		String snake = name.getText().replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
		return Identifier.toIdentifier(snake, name.isQuoted());
	}
}
