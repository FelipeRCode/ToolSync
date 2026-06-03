# Tool Sync — Sistema de Vendas

## Sobre o Projeto

O **Tool Sync** é um sistema back-end desenvolvido em Java como projeto acadêmico da disciplina de Linguagem de Programação. O objetivo do sistema é simular o fluxo completo de cadastro, venda e acompanhamento de pedidos de uma loja de ferramentas, aplicando na prática os principais conceitos da programação orientada a objetos.

A ideia central do projeto é oferecer uma estrutura organizada onde clientes possam ter seus pedidos registrados, vendedores possam gerenciar as vendas e emitir relatórios, e o sistema mantenha o controle de estoque, pagamentos e histórico de compras de forma integrada.

---

## Funcionalidades

O sistema oferece seis funcionalidades principais, acessadas por meio de menus interativos no terminal:

- **Cadastrar Cliente** — Permite incluir novos clientes e alterar ou excluir registros existentes.
- **Efetuar Pedido** — Registra pedidos com múltiplos itens, atualiza o estoque automaticamente e registra o pagamento ao finalizar.
- **Emitir Relatório de Vendas** — Gera relatório por vendedor ou relatório geral com todos os pedidos e totais.
- **Cadastrar Produto** — Permite incluir, alterar e excluir produtos (ferramentas), além de gerenciar as categorias de produtos.
- **Cadastrar Funcionário** — Permite incluir, alterar e excluir vendedores do sistema.
- **Consultar Cliente** — Exibe os dados completos de um cliente junto ao seu histórico de compras e pagamentos.

Todas as entradas do usuário passam por validação: campos obrigatórios, formatos de CPF/CNPJ, e-mail e telefone, intervalos numéricos e seleção de IDs existentes são verificados antes de qualquer operação.

---

## Modelagem Orientada a Objetos

O projeto foi desenvolvido com foco na aplicação correta dos pilares da orientação a objetos.

O **encapsulamento** foi aplicado em todas as classes, com os atributos declarados como privados e acessados apenas por meio de getters e setters.

A **herança** foi utilizada nas classes `ClienteEntity` e `VendedorEntity`, que estendem a classe base `PessoaEntity`, reaproveitando os atributos comuns como nome, CPF, telefone, e-mail e endereço.

A **composição** está presente na relação entre as entidades: um `PedidoEntity` é composto por uma lista de `ItemPedidoEntity`, cada item referencia um `ProdutoEntity`, e o pedido se associa a um `PagamentoEntity` ao ser finalizado.

---

## Estrutura de Classes

| Classe | Tipo | Descrição |
|---|---|---|
| `PessoaEntity` | Superclasse | Atributos comuns de toda pessoa (nome, CPF, telefone, e-mail, endereço) |
| `ClienteEntity` | Subclasse de Pessoa | Representa o cliente, armazena histórico de pedidos |
| `VendedorEntity` | Subclasse de Pessoa | Representa o funcionário/vendedor, armazena pedidos realizados |
| `ProdutoEntity` | Entidade | Produto (ferramenta) com nome, preço, estoque e categoria |
| `CategoriaEntity` | Entidade | Categoria de produto com nome e descrição |
| `PedidoEntity` | Entidade | Pedido com lista de itens, status e pagamento associado |
| `ItemPedidoEntity` | Entidade | Item individual de um pedido com quantidade e subtotal |
| `PagamentoEntity` | Entidade | Registro de pagamento vinculado a um pedido |

---

## Estrutura do Projeto

```
ToolSync/
├── src/
│   ├── App.java
│   └── entidades/
│       ├── PessoaEntity.java
│       ├── ClienteEntity.java
│       ├── VendedorEntity.java
│       ├── CategoriaEntity.java
│       ├── ProdutoEntity.java
│       ├── ItemPedidoEntity.java
│       ├── PedidoEntity.java
│       └── PagamentoEntity.java
└── bin/
    ├── App.class
    └── entidades/
        ├── PessoaEntity.class
        ├── ClienteEntity.class
        ├── VendedorEntity.class
        ├── CategoriaEntity.class
        ├── ProdutoEntity.class
        ├── ItemPedidoEntity.class
        ├── PedidoEntity.class
        └── PagamentoEntity.class
```

---

## Como Rodar o Projeto

Para executar o projeto é necessário ter o **Java JDK 17** ou superior instalado na máquina, além do **VS Code** com a extensão **Extension Pack for Java**.

### Executando pelo VS Code

1. Abra a pasta `ToolSync` no VS Code
2. Navegue até `src/App.java`
3. Clique em **Run** ou pressione `F5`

### Executando pelos arquivos compilados (pasta `bin`)

Caso queira rodar diretamente sem recompilar, acesse a pasta `bin` pelo terminal e execute:

```bash
cd ToolSync/bin
java -Dfile.encoding=UTF-8 -cp . App
```

### Recompilando manualmente

```bash
cd ToolSync/src
javac -encoding UTF-8 -d ../bin entidades/*.java
javac -encoding UTF-8 -cp ../bin -d ../bin App.java
```

Integrantes

Felipe Dos Reis

Pedro Henrique da Costa Amaral 

Poliane Soares da Cruz 

Ygor Vinicius da Silva Borges 

Otavio Luiz Russa Pinto 


Disciplina
Linguagem de Programação — Trabalho Interdisciplinar
Professora Luciene Cavalcanti
