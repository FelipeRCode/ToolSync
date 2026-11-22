package entidades;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoEntity {
    // Atributos da tabela 'pedido'
    private int                   idPedido;
    private LocalDateTime         dataPedido;
    private String                status;
    private double                valorTotal;
    private int                   idCliente;
    private int                   idVendedor;
    private List<ItemPedidoEntity> itens;
    private PagamentoEntity        pagamento;

    // Construtor
    public PedidoEntity(int idPedido, LocalDateTime dataPedido, String status,
                         int idCliente, int idVendedor) {
        this.idPedido   = idPedido;
        this.dataPedido = dataPedido;
        this.status     = status;
        this.idCliente  = idCliente;
        this.idVendedor = idVendedor;
        this.valorTotal = 0;
        this.itens      = new ArrayList<>();
    }

    // Getters e Setters
    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public LocalDateTime getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDateTime dataPedido) { this.dataPedido = dataPedido; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public int getIdVendedor() { return idVendedor; }
    public void setIdVendedor(int idVendedor) { this.idVendedor = idVendedor; }

    public List<ItemPedidoEntity> getItens() { return itens; }
    public void setItens(List<ItemPedidoEntity> itens) { this.itens = itens; }

    public PagamentoEntity getPagamento() { return pagamento; }
    public void setPagamento(PagamentoEntity pagamento) { this.pagamento = pagamento; }

    // Funcionalidade 02 - Efetuar Pedido
    public void adicionarItem(ItemPedidoEntity item) {
        item.setIdPedido(this.idPedido);
        this.itens.add(item);
        recalcularTotal();
        // Reduz estoque do produto
        if (item.getProduto() != null) {
            item.getProduto().reduzirEstoque(item.getQuantidade());
        }
    }

    public void recalcularTotal() {
        this.valorTotal = itens.stream().mapToDouble(ItemPedidoEntity::getSubtotal).sum();
    }

    public void fecharPedido() {
        this.status = "FECHADO";
    }

    public void exibirItensPedido() {
        System.out.println("\n-----------------------------------------");
        System.out.println("  Pedido #" + idPedido + " | Data: " + dataPedido.toLocalDate() + " | Status: " + status);
        System.out.println("-----------------------------------------");
        for (ItemPedidoEntity item : itens) {
            String nomeProduto = item.getProduto() != null ? item.getProduto().getNome() : "ID " + item.getIdProduto();
            System.out.println("  " + nomeProduto +
                    " | Qtd: " + item.getQuantidade() +
                    " | Unit: R$ " + String.format("%.2f", item.getPrecoUnitario()) +
                    " | Subtotal: R$ " + String.format("%.2f", item.getSubtotal()));
        }
        System.out.println("-----------------------------------------");
        System.out.println("  TOTAL: R$ " + String.format("%.2f", valorTotal));
        System.out.println("-----------------------------------------");
    }

    @Override
    public String toString() {
        return "Pedido{id=" + idPedido +
                ", data=" + dataPedido.toLocalDate() +
                ", status='" + status +
                "', total=R$" + String.format("%.2f", valorTotal) + "}";
    }
}
