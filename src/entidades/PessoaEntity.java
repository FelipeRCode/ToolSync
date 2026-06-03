package entidades;

public class PessoaEntity {
    // Atributos baseados na tabela 'pessoa'
    private int    idPessoa;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private String endereco;

    // Construtor
    public PessoaEntity(int idPessoa, String nome, String cpf, String telefone, String email, String endereco) {
        this.idPessoa = idPessoa;
        this.nome     = nome;
        this.cpf      = cpf;
        this.telefone = telefone;
        this.email    = email;
        this.endereco = endereco;
    }

    // Getters e Setters
    public int getIdPessoa() { return idPessoa; }
    public void setIdPessoa(int idPessoa) { this.idPessoa = idPessoa; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    @Override
    public String toString() {
        return "Pessoa{id=" + idPessoa + ", nome='" + nome + "', cpf='" + cpf + "', email='" + email + "'}";
    }
}
