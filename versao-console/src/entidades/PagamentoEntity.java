package entidades;

import java.time.LocalDateTime;

public class PagamentoEntity {
    // Atributos da tabela 'pagamento'
    private int           idPagamento;
    private String        formaPagamento;
    private double        valor;
    private LocalDateTime dataPagamento;
    private String        status;
    private int           idPedido;

    // Construtor
    public PagamentoEntity(int idPagamento, String formaPagamento, double valor,
                            LocalDateTime dataPagamento, String status, int idPedido) {
        this.idPagamento    = idPagamento;
        this.formaPagamento = formaPagamento;
        this.valor          = valor;
        this.dataPagamento  = dataPagamento;
        this.status         = status;
        this.idPedido       = idPedido;
    }

    // Getters e Setters
    public int getIdPagamento() { return idPagamento; }
    public void setIdPagamento(int idPagamento) { this.idPagamento = idPagamento; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDateTime dataPagamento) { this.dataPagamento = dataPagamento; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public void confirmar() {
        this.status = "CONFIRMADO";
        System.out.println("[Pagamento] Pagamento #" + idPagamento + " confirmado | R$ " +
                String.format("%.2f", valor) + " via " + formaPagamento);
    }

    @Override
    public String toString() {
        return "Pagamento{id=" + idPagamento +
                ", forma='" + formaPagamento +
                "', valor=R$" + String.format("%.2f", valor) +
                ", status='" + status + "'}";
    }
}
