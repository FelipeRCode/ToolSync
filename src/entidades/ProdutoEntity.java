package entidades;

public class ProdutoEntity {
    // Atributos da tabela 'produto'
    private int             idProduto;
    private String          nome;
    private String          descricao;
    private double          preco;
    private int             estoqueAtual;
    private String          unidadeMedida;
    private int             idCategoria;
    private CategoriaEntity categoria;

    // Construtor
    public ProdutoEntity(int idProduto, String nome, String descricao, double preco,
                         int estoqueAtual, String unidadeMedida, CategoriaEntity categoria) {
        this.idProduto     = idProduto;
        this.nome          = nome;
        this.descricao     = descricao;
        this.preco         = preco;
        this.estoqueAtual  = estoqueAtual;
        this.unidadeMedida = unidadeMedida;
        this.categoria     = categoria;
        this.idCategoria   = categoria != null ? categoria.getIdCategoria() : 0;
    }

    // Getters e Setters
    public int getIdProduto() { return idProduto; }
    public void setIdProduto(int idProduto) { this.idProduto = idProduto; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public int getEstoqueAtual() { return estoqueAtual; }
    public void setEstoqueAtual(int estoqueAtual) { this.estoqueAtual = estoqueAtual; }

    public String getUnidadeMedida() { return unidadeMedida; }
    public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public CategoriaEntity getCategoria() { return categoria; }
    public void setCategoria(CategoriaEntity categoria) { this.categoria = categoria; }

    // Funcionalidade 04 - Cadastrar Produto (inclusão e alteração)
    public void cadastrar() {
        System.out.println("[Produto] Produto cadastrado: " + nome +
                " | Preço: R$ " + String.format("%.2f", preco) +
                " | Estoque: " + estoqueAtual + " " + unidadeMedida);
    }

    public void alterar(String novoNome, double novoPreco, int novoEstoque, String novaDescricao) {
        setNome(novoNome);
        setPreco(novoPreco);
        setEstoqueAtual(novoEstoque);
        setDescricao(novaDescricao);
        System.out.println("[Produto] Produto atualizado: " + nome +
                " | Novo preço: R$ " + String.format("%.2f", preco));
    }

    public void reduzirEstoque(int quantidade) {
        if (quantidade <= estoqueAtual) {
            this.estoqueAtual -= quantidade;
        } else {
            System.out.println("[Produto] Estoque insuficiente para: " + nome);
        }
    }

    @Override
    public String toString() {
        return "Produto{id=" + idProduto + ", nome='" + nome +
                "', preco=R$" + String.format("%.2f", preco) +
                ", estoque=" + estoqueAtual + " " + unidadeMedida + "}";
    }
}
