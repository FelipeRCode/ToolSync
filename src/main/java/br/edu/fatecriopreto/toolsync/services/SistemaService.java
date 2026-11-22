package br.edu.fatecriopreto.toolsync.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariDataSource;

import br.edu.fatecriopreto.toolsync.config.NegocioException;
import lombok.RequiredArgsConstructor;

/** Backup e restore do banco (ponto extra do item 4.3). */
@Service
@RequiredArgsConstructor
public class SistemaService {

	private final BancoService banco;
	private final DataSource dataSource;

	@Value("${spring.datasource.url}")
	private String url;

	@Value("${spring.datasource.username:}")
	private String usuario;

	@Value("${spring.datasource.password:}")
	private String senha;

	/** procedure sp_BackupBanco: gera o .bak na pasta padrao de backup do SQL Server */
	public Map<String, Object> backup() {
		return banco.consultarUm("exec sp_BackupBanco");
	}

	/** backups ja feitos deste banco (historico do proprio SQL Server, no msdb) */
	public List<Map<String, Object>> backups() {
		return banco.consultar("""
				select top 20 m.physical_device_name as arquivo, b.backup_finish_date as data,
				       cast(b.backup_size / 1024.0 / 1024.0 as decimal(10,2)) as tamanho_mb
				from msdb.dbo.backupset as b
				inner join msdb.dbo.backupmediafamily as m on m.media_set_id = b.media_set_id
				where b.database_name = 'ToolSync' and b.type = 'D'
				order by b.backup_finish_date desc
				""");
	}

	/**
	 * Restaura um backup. O SQL Server nao restaura o banco em uso pela propria
	 * conexao, entao abrimos uma conexao separada no banco master.
	 */
	public Map<String, Object> restore(String arquivo) {
		if (arquivo == null || !arquivo.toLowerCase().endsWith(".bak")) {
			throw new NegocioException("Escolha um arquivo de backup (.bak).");
		}
		boolean conhecido = backups().stream().anyMatch(b -> arquivo.equalsIgnoreCase((String) b.get("arquivo")));
		if (!conhecido) {
			throw new NegocioException("Só é possível restaurar backups gerados pelo próprio sistema.");
		}
		String urlMaster = url.replaceAll("(?i)databaseName=[^;]*", "databaseName=master");
		Properties props = new Properties();
		if (!usuario.isBlank()) {
			props.setProperty("user", usuario);
			props.setProperty("password", senha);
		}
		String nome = arquivo.replace("'", "''");
		try (Connection c = DriverManager.getConnection(urlMaster, props); Statement st = c.createStatement()) {
			st.execute("alter database ToolSync set single_user with rollback immediate");
			try {
				st.execute("restore database ToolSync from disk = N'" + nome + "' with replace");
			} finally {
				st.execute("alter database ToolSync set multi_user");
			}
		} catch (SQLException e) {
			throw new NegocioException("Não foi possível restaurar: " + e.getMessage());
		}
		// as conexoes antigas do pool foram derrubadas pelo single_user: troca por novas
		if (dataSource instanceof HikariDataSource hikari && hikari.getHikariPoolMXBean() != null) {
			hikari.getHikariPoolMXBean().softEvictConnections();
		}
		return Map.of("mensagem", "Banco restaurado a partir de " + arquivo + ".");
	}
}
