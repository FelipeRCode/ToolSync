package br.edu.fatecriopreto.toolsync.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Nomes usados pelo Hibernate no banco: as tabelas ficam exatamente como no
 * @Table (ex.: ProdutoFornecedor) e as colunas viram snake_case
 * (ex.: dataVenda -> data_venda), igual ao script SQL.
 */
public class NomesBancoStrategy extends CamelCaseToUnderscoresNamingStrategy {

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
		return name;
	}
}
