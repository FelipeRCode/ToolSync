package br.edu.fatecriopreto.toolsync.services;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.fatecriopreto.toolsync.config.NegocioException;
import br.edu.fatecriopreto.toolsync.dto.Dtos.ClienteDTO;
import br.edu.fatecriopreto.toolsync.entities.ClienteEntity;
import br.edu.fatecriopreto.toolsync.repositories.ClienteRepository;
import br.edu.fatecriopreto.toolsync.repositories.VendaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

	private final ClienteRepository clienteRepository;
	private final VendaRepository vendaRepository;
	private final BancoService banco;

	/** lista com a ultima compra de cada cliente (function fc_UltimaCompraCliente) */
	public List<ClienteDTO> listar() {
		List<Map<String, Object>> linhas = banco.consultar("""
				select P.id, P.nome, C.tipo_pessoa, C.documento, P.email, P.telefone, P.rua, P.numero,
				       P.bairro, P.cep, P.cidade, P.uf, P.data_cadastro,
				       dbo.fc_UltimaCompraCliente(C.pessoa_id) as ultima_compra
				from Clientes as C inner join Pessoas as P on P.id = C.pessoa_id
				order by P.nome
				""");
		return linhas.stream().map(l -> new ClienteDTO(
				(Integer) l.get("id"), codigo((Integer) l.get("id")), (String) l.get("nome"),
				(String) l.get("tipo_pessoa"), (String) l.get("documento"), (String) l.get("email"),
				(String) l.get("telefone"), (String) l.get("rua"), (String) l.get("numero"),
				(String) l.get("bairro"), (String) l.get("cep"), (String) l.get("cidade"), (String) l.get("uf"),
				(String) l.get("data_cadastro"), (String) l.get("ultima_compra"))).toList();
	}

	public ClienteDTO buscar(Integer id) {
		ClienteEntity c = clienteRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Cliente não encontrado."));
		Map<String, Object> ultima = banco.consultarUm("select dbo.fc_UltimaCompraCliente(?) as ultima", id);
		return dto(c, (String) ultima.get("ultima"));
	}

	/** historico de compras e pagamentos (function de tabela fc_HistoricoCliente) */
	public List<Map<String, Object>> historico(Integer id) {
		return banco.consultar("select * from dbo.fc_HistoricoCliente(?) order by data_venda desc", id);
	}

	@Transactional
	public ClienteDTO salvar(Integer id, ClienteDTO dados) {
		ClienteEntity c = id == null ? new ClienteEntity()
				: clienteRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Cliente não encontrado."));

		String nome = Validacao.obrigatorio(dados.nome(), "Nome");
		Validacao.nome(nome);
		String documento = Validacao.obrigatorio(dados.documento(), "CPF/CNPJ");
		String tipo;
		if (Validacao.cpfValido(documento)) {
			tipo = "PF";
		} else if (Validacao.cnpjValido(documento)) {
			tipo = "PJ";
		} else {
			throw new NegocioException("Documento inválido. Use CPF (000.000.000-00) ou CNPJ (00.000.000/0000-00).");
		}
		if (!documento.equals(c.getDocumento()) && clienteRepository.existsByDocumento(documento)) {
			throw new NegocioException("Já existe um cliente com este CPF/CNPJ.");
		}
		String telefone = Validacao.opcional(dados.telefone());
		String email = Validacao.opcional(dados.email());
		String cep = Validacao.opcional(dados.cep());
		Validacao.telefone(telefone);
		Validacao.email(email);
		Validacao.cep(cep);

		c.setNome(nome);
		c.setDocumento(documento);
		c.setTipoPessoa(tipo);
		c.setTelefone(telefone);
		c.setEmail(email);
		c.setRua(Validacao.opcional(dados.rua()));
		c.setNumero(Validacao.opcional(dados.numero()));
		c.setBairro(Validacao.opcional(dados.bairro()));
		c.setCep(cep);
		c.setCidade(Validacao.opcional(dados.cidade()));
		c.setUf(Validacao.uf(dados.uf()));
		clienteRepository.saveAndFlush(c);
		return dto(c, null);
	}

	@Transactional
	public void excluir(Integer id) {
		ClienteEntity c = clienteRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Cliente não encontrado."));
		if (vendaRepository.existsByClienteId(id)) {
			throw new NegocioException("O cliente " + c.getNome() + " tem vendas registradas e não pode ser excluído.");
		}
		clienteRepository.delete(c);
	}

	private ClienteDTO dto(ClienteEntity c, String ultimaCompra) {
		return new ClienteDTO(c.getId(), codigo(c.getId()), c.getNome(), c.getTipoPessoa(), c.getDocumento(),
				c.getEmail(), c.getTelefone(), c.getRua(), c.getNumero(), c.getBairro(), c.getCep(), c.getCidade(),
				c.getUf(), c.getDataCadastro() == null ? null : c.getDataCadastro().withNano(0).toString(), ultimaCompra);
	}

	static String codigo(Integer id) {
		return id == null ? null : String.format("%04d", id);
	}
}
