package entidades;

public class CategoriaEntity {
    // Atributos da tabela 'categoria'
    private int    idCategoria;
    private String nome;
    private String descricao;

    // Construtor
    public CategoriaEntity(int idCategoria, String nome, String descricao) {
        this.idCategoria = idCategoria;
        this.nome        = nome;
        this.descricao   = descricao;
    }

    // Getters e Setters
    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    @Override
    public String toString() {
        return "Categoria{id=" + idCategoria + ", nome='" + nome + "', descricao='" + descricao + "'}";
    }
}
