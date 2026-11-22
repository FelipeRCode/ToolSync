package entidades;

import java.util.ArrayList;
import java.util.List;

public class ClienteEntity extends PessoaEntity {
    private List<PedidoEntity> historicoPedidos;

    public ClienteEntity(int idPessoa, String nome, String cpf, String telefone, String email, String endereco) {
        super(idPessoa, nome, cpf, telefone, email, endereco);
        this.historicoPedidos = new ArrayList<>();
    }

    public List<PedidoEntity> getHistoricoPedidos() { return historicoPedidos; }
    public void setHistoricoPedidos(List<PedidoEntity> historicoPedidos) { this.historicoPedidos = historicoPedidos; }

    public void cadastrar() {
        System.out.println("[Cliente] Cadastro realizado: " + getNome() + " | CPF: " + getCpf());
    }

    public void alterar(String novoNome, String novoTelefone, String novoEmail, String novoEndereco) {
        setNome(novoNome);
        setTelefone(novoTelefone);
        setEmail(novoEmail);
        setEndereco(novoEndereco);
        System.out.println("[Cliente] Cadastro atualizado: " + getNome());
    }

    // Funcionalidade 06 - Consultar Cliente (histórico de compras e pagamentos)
    public void consultarHistorico() {
        System.out.println("\n-----------------------------------------");
        System.out.println("  Histórico de Compras: " + getNome());
        System.out.println("-----------------------------------------");
        if (historicoPedidos.isEmpty()) {
            System.out.println("  Nenhum pedido encontrado.");
        } else {
            double totalGasto = 0;
            for (PedidoEntity pedido : historicoPedidos) {
                System.out.println("  Pedido #" + pedido.getIdPedido() +
                        " | Data: " + pedido.getDataPedido().toLocalDate() +
                        " | Total: R$ " + String.format("%.2f", pedido.getValorTotal()) +
                        " | Status: " + pedido.getStatus());
                if (pedido.getPagamento() != null)
                    System.out.println("    Pagamento: " + pedido.getPagamento().getFormaPagamento() +
                            " | Status: " + pedido.getPagamento().getStatus());
                totalGasto += pedido.getValorTotal();
            }
            System.out.println("-----------------------------------------");
            System.out.println("  TOTAL GASTO: R$ " + String.format("%.2f", totalGasto));
        }
        System.out.println("-----------------------------------------");
    }

    public void adicionarPedido(PedidoEntity pedido) {
        this.historicoPedidos.add(pedido);
    }

    @Override
    public String toString() {
        return "Cliente{id=" + getIdPessoa() + ", nome='" + getNome() + "', email='" + getEmail() + "'}";
    }
}
