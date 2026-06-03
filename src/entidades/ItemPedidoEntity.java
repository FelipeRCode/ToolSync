package entidades;

public class ItemPedidoEntity {
    // Atributos da tabela 'item_pedido'
    private int            idItemPedido;
    private int            quantidade;
    private double         precoUnitario;
    private double         subtotal;
    private int            idPedido;
    private int            idProduto;
    private ProdutoEntity  produto;

    // Construtor
    public ItemPedidoEntity(int idItemPedido, int quantidade, ProdutoEntity produto, int idPedido) {
        this.idItemPedido   = idItemPedido;
        this.quantidade     = quantidade;
        this.produto        = produto;
        this.idProduto      = produto != null ? produto.getIdProduto() : 0;
        this.precoUnitario  = produto != null ? produto.getPreco() : 0;
        this.subtotal       = this.precoUnitario * quantidade;
        this.idPedido       = idPedido;
    }

    // Getters e Setters
    public int getIdItemPedido() { return idItemPedido; }
    public void setIdItemPedido(int idItemPedido) { this.idItemPedido = idItemPedido; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) {
        this.quantidade    = quantidade;
        this.subtotal      = this.precoUnitario * quantidade;
    }

    public double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
        this.subtotal      = precoUnitario * quantidade;
    }

    public double getSubtotal() { return subtotal; }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public int getIdProduto() { return idProduto; }
    public void setIdProduto(int idProduto) { this.idProduto = idProduto; }

    public ProdutoEntity getProduto() { return produto; }
    public void setProduto(ProdutoEntity produto) { this.produto = produto; }

    @Override
    public String toString() {
        String nomeProduto = produto != null ? produto.getNome() : "ID " + idProduto;
        return "Item{produto='" + nomeProduto +
                "', qtd=" + quantidade +
                ", unitario=R$" + String.format("%.2f", precoUnitario) +
                ", subtotal=R$" + String.format("%.2f", subtotal) + "}";
    }
}
