package entidades;

import java.util.ArrayList;
import java.util.List;

public class VendedorEntity extends PessoaEntity {
    private String cargo;
    private List<PedidoEntity> pedidosRealizados;

    public VendedorEntity(int idPessoa, String nome, String cpf, String telefone, String email, String endereco, String cargo) {
        super(idPessoa, nome, cpf, telefone, email, endereco);
        this.cargo             = cargo;
        this.pedidosRealizados = new ArrayList<>();
    }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public List<PedidoEntity> getPedidosRealizados() { return pedidosRealizados; }

    public void cadastrar() {
        System.out.println("[Vendedor] Funcionário cadastrado: " + getNome() + " | Cargo: " + cargo);
    }

    public void alterar(String novoCargo, String novoTelefone, String novoEmail) {
        setCargo(novoCargo);
        setTelefone(novoTelefone);
        setEmail(novoEmail);
        System.out.println("[Vendedor] Cadastro atualizado: " + getNome() + " | Novo cargo: " + cargo);
    }

    // Funcionalidade 03 - Emitir Relatório de Vendas (sem período)
    public void emitirRelatorioVendas() {
        System.out.println("\n-----------------------------------------");
        System.out.println("  Relatório de Vendas: " + getNome());
        System.out.println("-----------------------------------------");
        if (pedidosRealizados.isEmpty()) {
            System.out.println("  Nenhuma venda registrada.");
        } else {
            double totalGeral = 0;
            for (PedidoEntity pedido : pedidosRealizados) {
                System.out.println("  Pedido #" + pedido.getIdPedido() +
                        " | Cliente ID: " + pedido.getIdCliente() +
                        " | Total: R$ " + String.format("%.2f", pedido.getValorTotal()) +
                        " | Status: " + pedido.getStatus());
                if (pedido.getPagamento() != null)
                    System.out.println("    Pagamento: " + pedido.getPagamento().getFormaPagamento() +
                            " | Status: " + pedido.getPagamento().getStatus());
                totalGeral += pedido.getValorTotal();
            }
            System.out.println("-----------------------------------------");
            System.out.println("  TOTAL GERAL: R$ " + String.format("%.2f", totalGeral));
        }
        System.out.println("-----------------------------------------");
    }

    public void adicionarPedidoRealizado(PedidoEntity pedido) {
        this.pedidosRealizados.add(pedido);
    }

    @Override
    public String toString() {
        return "Vendedor{id=" + getIdPessoa() + ", nome='" + getNome() + "', cargo='" + cargo + "'}";
    }
}
