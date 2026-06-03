import entidades.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {

    static List<ClienteEntity>   clientes   = new ArrayList<>();
    static List<VendedorEntity>  vendedores = new ArrayList<>();
    static List<ProdutoEntity>   produtos   = new ArrayList<>();
    static List<CategoriaEntity> categorias = new ArrayList<>();
    static List<PedidoEntity>    pedidos    = new ArrayList<>();

    static int proximoIdPessoa    = 1;
    static int proximoIdProduto   = 1;
    static int proximoIdCategoria = 1;
    static int proximoIdPedido    = 1;
    static int proximoIdPagamento = 1;
    static int proximoIdItem      = 1;

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        carregarDadosIniciais();

        int opcao = -1;
        do {
            exibirMenuPrincipal();
            opcao = lerIntRange("Escolha uma opção: ", 0, 6);
            switch (opcao) {
                case 1  -> menuCadastrarCliente();
                case 2  -> menuEfetuarPedido();
                case 3  -> menuRelatorioVendas();
                case 4  -> menuCadastrarProduto();
                case 5  -> menuCadastrarFuncionario();
                case 6  -> menuConsultarCliente();
                case 0  -> System.out.println("\nSaindo do sistema. Até logo!");
            }
        } while (opcao != 0);

        sc.close();
    }

    // =========================================================
    // MENU PRINCIPAL
    // =========================================================
    static void exibirMenuPrincipal() {
        System.out.println("\n=========================================");
        System.out.println("         TOOL SYNC - SISTEMA DE VENDAS  ");
        System.out.println("=========================================");
        System.out.println(" [1] Cadastrar Cliente");
        System.out.println(" [2] Efetuar Pedido");
        System.out.println(" [3] Emitir Relatório de Vendas");
        System.out.println(" [4] Cadastrar Produto");
        System.out.println(" [5] Cadastrar Funcionário");
        System.out.println(" [6] Consultar Cliente");
        System.out.println(" [0] Sair");
        System.out.println("-----------------------------------------");
    }

    // =========================================================
    // F01 - CADASTRAR / EXCLUIR CLIENTE
    // =========================================================
    static void menuCadastrarCliente() {
        System.out.println("\n=== [F01] CLIENTES ===");
        System.out.println(" [1] Incluir novo cliente");
        System.out.println(" [2] Alterar cliente existente");
        System.out.println(" [3] Excluir cliente");
        System.out.println(" [0] Voltar");
        int op = lerIntRange("Opção: ", 0, 3);

        switch (op) {
            case 1 -> {
                System.out.println("\n-- Novo Cliente --");
                String nome     = lerTextoObrigatorio("Nome (mín. 3 letras): ", 3, 100);
                String cpf      = lerCPFouCNPJ("CPF ou CNPJ: ");
                String telefone = lerTelefone("Telefone (ex: (11) 91234-5678): ");
                String email    = lerEmail("E-mail: ");
                String endereco = lerTextoObrigatorio("Endereço (mín. 5 caracteres): ", 5, 200);

                ClienteEntity c = new ClienteEntity(proximoIdPessoa++, nome, cpf, telefone, email, endereco);
                c.cadastrar();
                clientes.add(c);
            }
            case 2 -> {
                if (clientes.isEmpty()) { System.out.println("[!] Nenhum cliente cadastrado."); return; }
                listarClientes();
                int id = lerIdExistente("ID do cliente a alterar: ", clientes.stream().mapToInt(c -> c.getIdPessoa()).toArray());
                ClienteEntity c = buscarClientePorId(id);

                System.out.println("Deixe em branco para manter o valor atual.");

                System.out.print("Novo nome [" + c.getNome() + "]: ");
                String nome = sc.nextLine().trim();
                if (!nome.isBlank() && nome.length() < 3) { System.out.println("[!] Nome muito curto. Mantendo o anterior."); nome = ""; }

                System.out.print("Novo telefone [" + c.getTelefone() + "]: ");
                String telefone = sc.nextLine().trim();
                if (!telefone.isBlank() && !validarTelefone(telefone)) { System.out.println("[!] Formato inválido. Mantendo o anterior."); telefone = ""; }

                System.out.print("Novo e-mail [" + c.getEmail() + "]: ");
                String email = sc.nextLine().trim();
                if (!email.isBlank() && !validarEmail(email)) { System.out.println("[!] E-mail inválido. Mantendo o anterior."); email = ""; }

                System.out.print("Novo endereço [" + c.getEndereco() + "]: ");
                String endereco = sc.nextLine().trim();
                if (!endereco.isBlank() && endereco.length() < 5) { System.out.println("[!] Endereço muito curto. Mantendo o anterior."); endereco = ""; }

                c.alterar(
                    nome.isBlank()     ? c.getNome()     : nome,
                    telefone.isBlank() ? c.getTelefone() : telefone,
                    email.isBlank()    ? c.getEmail()    : email,
                    endereco.isBlank() ? c.getEndereco() : endereco
                );
            }
            case 3 -> {
                if (clientes.isEmpty()) { System.out.println("[!] Nenhum cliente cadastrado."); return; }
                listarClientes();
                int id = lerIdExistente("ID do cliente a excluir: ", clientes.stream().mapToInt(c -> c.getIdPessoa()).toArray());
                ClienteEntity c = buscarClientePorId(id);

                // Verifica se há pedidos vinculados
                boolean temPedido = pedidos.stream().anyMatch(p -> p.getIdCliente() == id);
                if (temPedido) {
                    System.out.println("[!] Não é possível excluir: cliente possui pedidos registrados.");
                    return;
                }

                System.out.print("Confirmar exclusão de \"" + c.getNome() + "\"? (s/n): ");
                String conf = sc.nextLine().trim().toLowerCase();
                if (conf.equals("s")) {
                    clientes.remove(c);
                    System.out.println("[OK] Cliente excluído com sucesso.");
                } else {
                    System.out.println("[--] Exclusão cancelada.");
                }
            }
            case 0 -> {}
        }
    }

    // =========================================================
    // F02 - EFETUAR / EXCLUIR PEDIDO
    // =========================================================
    static void menuEfetuarPedido() {
        System.out.println("\n=== [F02] PEDIDOS ===");
        System.out.println(" [1] Efetuar novo pedido");
        System.out.println(" [2] Excluir pedido");
        System.out.println(" [0] Voltar");
        int op = lerIntRange("Opção: ", 0, 2);

        switch (op) {
            case 1 -> realizarPedido();
            case 2 -> excluirPedido();
            case 0 -> {}
        }
    }

    static void realizarPedido() {
        if (clientes.isEmpty())  { System.out.println("[!] Nenhum cliente cadastrado.");  return; }
        if (vendedores.isEmpty()){ System.out.println("[!] Nenhum vendedor cadastrado."); return; }
        if (produtos.isEmpty())  { System.out.println("[!] Nenhum produto cadastrado.");  return; }

        listarClientes();
        int idCliente = lerIdExistente("ID do cliente: ", clientes.stream().mapToInt(c -> c.getIdPessoa()).toArray());
        ClienteEntity cliente = buscarClientePorId(idCliente);

        listarVendedores();
        int idVendedor = lerIdExistente("ID do vendedor responsável: ", vendedores.stream().mapToInt(v -> v.getIdPessoa()).toArray());
        VendedorEntity vendedor = buscarVendedorPorId(idVendedor);

        PedidoEntity pedido = new PedidoEntity(
            proximoIdPedido++, LocalDateTime.now(), "ABERTO",
            cliente.getIdPessoa(), vendedor.getIdPessoa()
        );

        boolean adicionando = true;
        while (adicionando) {
            listarProdutos();
            System.out.println("  [0] Finalizar pedido");
            int idProduto = lerIntMin("ID do produto: ", 0);
            if (idProduto == 0) { adicionando = false; break; }

            ProdutoEntity produto = buscarProdutoPorId(idProduto);
            if (produto == null) { System.out.println("[!] ID inexistente. Escolha um ID da lista."); continue; }
            if (produto.getEstoqueAtual() == 0) { System.out.println("[!] Produto sem estoque: " + produto.getNome()); continue; }

            System.out.println("    Estoque disponível: " + produto.getEstoqueAtual() + " " + produto.getUnidadeMedida());
            int qtd = lerIntRange("    Quantidade (1 a " + produto.getEstoqueAtual() + "): ", 1, produto.getEstoqueAtual());

            ItemPedidoEntity item = new ItemPedidoEntity(proximoIdItem++, qtd, produto, pedido.getIdPedido());
            pedido.adicionarItem(item);
            System.out.println("[+] Item adicionado: " + produto.getNome() + " x" + qtd);
        }

        if (pedido.getItens().isEmpty()) {
            System.out.println("[!] Pedido cancelado — nenhum item adicionado.");
            proximoIdPedido--;
            return;
        }

        pedido.fecharPedido();
        pedido.exibirItensPedido();

        System.out.println("\n-- Registrar Pagamento --");
        System.out.println(" [1] Dinheiro");
        System.out.println(" [2] Cartão de Crédito");
        System.out.println(" [3] Cartão de Débito");
        System.out.println(" [4] Boleto Bancário");
        System.out.println(" [5] Pix");
        int opPag = lerIntRange("Forma de pagamento: ", 1, 5);
        String[] formas = {"Dinheiro", "Cartão de Crédito", "Cartão de Débito", "Boleto Bancário", "Pix"};
        String forma = formas[opPag - 1];

        PagamentoEntity pagamento = new PagamentoEntity(
            proximoIdPagamento++, forma, pedido.getValorTotal(),
            LocalDateTime.now(), "PENDENTE", pedido.getIdPedido()
        );
        pedido.setPagamento(pagamento);
        pagamento.confirmar();

        pedidos.add(pedido);
        cliente.adicionarPedido(pedido);
        vendedor.adicionarPedidoRealizado(pedido);
    }

    static void excluirPedido() {
        if (pedidos.isEmpty()) { System.out.println("[!] Nenhum pedido registrado."); return; }

        System.out.println("\n  Pedidos registrados:");
        for (PedidoEntity p : pedidos)
            System.out.println("  [" + p.getIdPedido() + "] Pedido #" + p.getIdPedido() +
                    " | Cliente ID: " + p.getIdCliente() +
                    " | Total: R$ " + String.format("%.2f", p.getValorTotal()) +
                    " | Status: " + p.getStatus());

        int id = lerIdExistente("ID do pedido a excluir: ", pedidos.stream().mapToInt(p -> p.getIdPedido()).toArray());
        PedidoEntity pedido = pedidos.stream().filter(p -> p.getIdPedido() == id).findFirst().orElse(null);

        System.out.print("Confirmar exclusão do Pedido #" + id + "? (s/n): ");
        String conf = sc.nextLine().trim().toLowerCase();
        if (conf.equals("s")) {
            // Devolve estoque dos itens ao excluir
            for (ItemPedidoEntity item : pedido.getItens()) {
                if (item.getProduto() != null)
                    item.getProduto().setEstoqueAtual(item.getProduto().getEstoqueAtual() + item.getQuantidade());
            }
            // Remove das listas dos clientes e vendedores
            for (ClienteEntity c : clientes)   c.getHistoricoPedidos().remove(pedido);
            for (VendedorEntity v : vendedores) v.getPedidosRealizados().remove(pedido);
            pedidos.remove(pedido);
            System.out.println("[OK] Pedido excluído e estoque revertido.");
        } else {
            System.out.println("[--] Exclusão cancelada.");
        }
    }

    // =========================================================
    // F03 - EMITIR RELATÓRIO DE VENDAS
    // =========================================================
    static void menuRelatorioVendas() {
        System.out.println("\n=== [F03] RELATÓRIO DE VENDAS ===");
        System.out.println(" [1] Relatório por vendedor");
        System.out.println(" [2] Relatório geral (todos os pedidos)");
        System.out.println(" [0] Voltar");
        int op = lerIntRange("Opção: ", 0, 2);

        switch (op) {
            case 1 -> {
                if (vendedores.isEmpty()) { System.out.println("[!] Nenhum vendedor cadastrado."); return; }
                listarVendedores();
                int id = lerIdExistente("ID do vendedor: ", vendedores.stream().mapToInt(v -> v.getIdPessoa()).toArray());
                buscarVendedorPorId(id).emitirRelatorioVendas();
            }
            case 2 -> {
                if (pedidos.isEmpty()) { System.out.println("[!] Nenhum pedido registrado."); return; }
                System.out.println("\n-----------------------------------------");
                System.out.println("  Relatório Geral de Pedidos");
                System.out.println("-----------------------------------------");
                double totalGeral = 0;
                for (PedidoEntity p : pedidos) {
                    System.out.println("  Pedido #" + p.getIdPedido() +
                            " | Data: " + p.getDataPedido().toLocalDate() +
                            " | Cliente ID: " + p.getIdCliente() +
                            " | Total: R$ " + String.format("%.2f", p.getValorTotal()) +
                            " | Status: " + p.getStatus());
                    if (p.getPagamento() != null)
                        System.out.println("    Pagamento: " + p.getPagamento().getFormaPagamento() +
                                " | Status: " + p.getPagamento().getStatus());
                    totalGeral += p.getValorTotal();
                }
                System.out.println("-----------------------------------------");
                System.out.println("  TOTAL GERAL: R$ " + String.format("%.2f", totalGeral));
                System.out.println("-----------------------------------------");
            }
            case 0 -> {}
        }
    }

    // =========================================================
    // F04 - CADASTRAR / EXCLUIR PRODUTO E CATEGORIA
    // =========================================================
    static void menuCadastrarProduto() {
        System.out.println("\n=== [F04] PRODUTOS ===");
        System.out.println(" [1] Incluir novo produto");
        System.out.println(" [2] Alterar produto existente");
        System.out.println(" [3] Excluir produto");
        System.out.println(" [4] Gerenciar categorias");
        System.out.println(" [0] Voltar");
        int op = lerIntRange("Opção: ", 0, 4);

        switch (op) {
            case 1 -> {
                if (categorias.isEmpty()) { System.out.println("[!] Cadastre uma categoria antes de incluir produtos."); return; }
                System.out.println("\n-- Novo Produto --");
                String nome    = lerTextoObrigatorio("Nome (mín. 3 caracteres): ", 3, 100);
                String desc    = lerTextoObrigatorio("Descrição: ", 3, 200);
                double preco   = lerDoubleMin("Preço (R$, mín. 0.01): ", 0.01);
                int estoque    = lerIntMin("Estoque inicial (mín. 0): ", 0);
                System.out.println(" Unidades: un | kg | jg | cx | lt | mt");
                String unidade = lerOpcaoTexto("Unidade de medida: ", new String[]{"un","kg","jg","cx","lt","mt"});
                listarCategorias();
                int idCat = lerIdExistente("ID da categoria: ", categorias.stream().mapToInt(c -> c.getIdCategoria()).toArray());
                ProdutoEntity p = new ProdutoEntity(proximoIdProduto++, nome, desc, preco, estoque, unidade, buscarCategoriaPorId(idCat));
                p.cadastrar();
                produtos.add(p);
            }
            case 2 -> {
                if (produtos.isEmpty()) { System.out.println("[!] Nenhum produto cadastrado."); return; }
                listarProdutos();
                int id = lerIdExistente("ID do produto a alterar: ", produtos.stream().mapToInt(p -> p.getIdProduto()).toArray());
                ProdutoEntity p = buscarProdutoPorId(id);

                System.out.println("Deixe em branco para manter o valor atual.");

                System.out.print("Novo nome [" + p.getNome() + "]: ");
                String nome = sc.nextLine().trim();
                if (!nome.isBlank() && nome.length() < 3) { System.out.println("[!] Nome muito curto. Mantendo."); nome = ""; }

                System.out.print("Nova descrição [" + p.getDescricao() + "]: ");
                String desc = sc.nextLine().trim();

                System.out.print("Novo preço [R$ " + String.format("%.2f", p.getPreco()) + "] (Enter para manter): ");
                String precoStr = sc.nextLine().trim().replace(",", ".");
                double preco = p.getPreco();
                if (!precoStr.isBlank()) {
                    try { double n = Double.parseDouble(precoStr); if (n < 0.01) System.out.println("[!] Preço inválido. Mantendo."); else preco = n; }
                    catch (NumberFormatException e) { System.out.println("[!] Valor inválido. Mantendo."); }
                }

                System.out.print("Novo estoque [" + p.getEstoqueAtual() + "] (Enter para manter): ");
                String estoqueStr = sc.nextLine().trim();
                int estoque = p.getEstoqueAtual();
                if (!estoqueStr.isBlank()) {
                    try { int n = Integer.parseInt(estoqueStr); if (n < 0) System.out.println("[!] Estoque negativo. Mantendo."); else estoque = n; }
                    catch (NumberFormatException e) { System.out.println("[!] Valor inválido. Mantendo."); }
                }

                p.alterar(nome.isBlank() ? p.getNome() : nome, preco, estoque, desc.isBlank() ? p.getDescricao() : desc);
            }
            case 3 -> {
                if (produtos.isEmpty()) { System.out.println("[!] Nenhum produto cadastrado."); return; }
                listarProdutos();
                int id = lerIdExistente("ID do produto a excluir: ", produtos.stream().mapToInt(p -> p.getIdProduto()).toArray());
                ProdutoEntity p = buscarProdutoPorId(id);

                // Verifica se está em algum pedido
                boolean emUso = pedidos.stream().anyMatch(ped ->
                    ped.getItens().stream().anyMatch(it -> it.getIdProduto() == id));
                if (emUso) { System.out.println("[!] Não é possível excluir: produto está vinculado a pedidos."); return; }

                System.out.print("Confirmar exclusão de \"" + p.getNome() + "\"? (s/n): ");
                String conf = sc.nextLine().trim().toLowerCase();
                if (conf.equals("s")) { produtos.remove(p); System.out.println("[OK] Produto excluído."); }
                else System.out.println("[--] Exclusão cancelada.");
            }
            case 4 -> menuCategorias();
            case 0 -> {}
        }
    }

    static void menuCategorias() {
        System.out.println("\n-- Gerenciar Categorias --");
        System.out.println(" [1] Nova categoria");
        System.out.println(" [2] Listar categorias");
        System.out.println(" [3] Excluir categoria");
        System.out.println(" [0] Voltar");
        int op = lerIntRange("Opção: ", 0, 3);
        switch (op) {
            case 1 -> {
                String nome = lerTextoObrigatorio("Nome da categoria (mín. 3 caracteres): ", 3, 80);
                String desc = lerTextoObrigatorio("Descrição (mín. 3 caracteres): ", 3, 200);
                CategoriaEntity cat = new CategoriaEntity(proximoIdCategoria++, nome, desc);
                categorias.add(cat);
                System.out.println("[+] Categoria criada: " + cat);
            }
            case 2 -> listarCategorias();
            case 3 -> {
                if (categorias.isEmpty()) { System.out.println("[!] Nenhuma categoria cadastrada."); return; }
                listarCategorias();
                int id = lerIdExistente("ID da categoria a excluir: ", categorias.stream().mapToInt(c -> c.getIdCategoria()).toArray());
                CategoriaEntity cat = buscarCategoriaPorId(id);

                // Verifica se há produtos usando essa categoria
                boolean emUso = produtos.stream().anyMatch(p -> p.getIdCategoria() == id);
                if (emUso) { System.out.println("[!] Não é possível excluir: existem produtos nessa categoria."); return; }

                System.out.print("Confirmar exclusão de \"" + cat.getNome() + "\"? (s/n): ");
                String conf = sc.nextLine().trim().toLowerCase();
                if (conf.equals("s")) { categorias.remove(cat); System.out.println("[OK] Categoria excluída."); }
                else System.out.println("[--] Exclusão cancelada.");
            }
            case 0 -> {}
        }
    }

    // =========================================================
    // F05 - CADASTRAR / EXCLUIR FUNCIONÁRIO
    // =========================================================
    static void menuCadastrarFuncionario() {
        System.out.println("\n=== [F05] FUNCIONÁRIOS ===");
        System.out.println(" [1] Incluir novo funcionário");
        System.out.println(" [2] Alterar funcionário existente");
        System.out.println(" [3] Excluir funcionário");
        System.out.println(" [0] Voltar");
        int op = lerIntRange("Opção: ", 0, 3);

        switch (op) {
            case 1 -> {
                System.out.println("\n-- Novo Funcionário --");
                String nome     = lerTextoObrigatorio("Nome (mín. 3 letras): ", 3, 100);
                String cpf      = lerCPFouCNPJ("CPF: ");
                String telefone = lerTelefone("Telefone (ex: (11) 91234-5678): ");
                String email    = lerEmail("E-mail: ");
                String endereco = lerTextoObrigatorio("Endereço (mín. 5 caracteres): ", 5, 200);
                String cargo    = lerTextoObrigatorio("Cargo (mín. 3 caracteres): ", 3, 80);

                VendedorEntity v = new VendedorEntity(proximoIdPessoa++, nome, cpf, telefone, email, endereco, cargo);
                v.cadastrar();
                vendedores.add(v);
            }
            case 2 -> {
                if (vendedores.isEmpty()) { System.out.println("[!] Nenhum funcionário cadastrado."); return; }
                listarVendedores();
                int id = lerIdExistente("ID do funcionário a alterar: ", vendedores.stream().mapToInt(v -> v.getIdPessoa()).toArray());
                VendedorEntity v = buscarVendedorPorId(id);

                System.out.println("Deixe em branco para manter o valor atual.");

                System.out.print("Novo cargo [" + v.getCargo() + "]: ");
                String cargo = sc.nextLine().trim();
                if (!cargo.isBlank() && cargo.length() < 3) { System.out.println("[!] Cargo muito curto. Mantendo."); cargo = ""; }

                System.out.print("Novo telefone [" + v.getTelefone() + "]: ");
                String telefone = sc.nextLine().trim();
                if (!telefone.isBlank() && !validarTelefone(telefone)) { System.out.println("[!] Formato inválido. Mantendo."); telefone = ""; }

                System.out.print("Novo e-mail [" + v.getEmail() + "]: ");
                String email = sc.nextLine().trim();
                if (!email.isBlank() && !validarEmail(email)) { System.out.println("[!] E-mail inválido. Mantendo."); email = ""; }

                v.alterar(
                    cargo.isBlank()    ? v.getCargo()    : cargo,
                    telefone.isBlank() ? v.getTelefone() : telefone,
                    email.isBlank()    ? v.getEmail()    : email
                );
            }
            case 3 -> {
                if (vendedores.isEmpty()) { System.out.println("[!] Nenhum funcionário cadastrado."); return; }
                listarVendedores();
                int id = lerIdExistente("ID do funcionário a excluir: ", vendedores.stream().mapToInt(v -> v.getIdPessoa()).toArray());
                VendedorEntity v = buscarVendedorPorId(id);

                boolean temPedido = pedidos.stream().anyMatch(p -> p.getIdVendedor() == id);
                if (temPedido) { System.out.println("[!] Não é possível excluir: funcionário possui pedidos registrados."); return; }

                System.out.print("Confirmar exclusão de \"" + v.getNome() + "\"? (s/n): ");
                String conf = sc.nextLine().trim().toLowerCase();
                if (conf.equals("s")) { vendedores.remove(v); System.out.println("[OK] Funcionário excluído."); }
                else System.out.println("[--] Exclusão cancelada.");
            }
            case 0 -> {}
        }
    }

    // =========================================================
    // F06 - CONSULTAR CLIENTE
    // =========================================================
    static void menuConsultarCliente() {
        System.out.println("\n=== [F06] CONSULTAR CLIENTE ===");
        if (clientes.isEmpty()) { System.out.println("[!] Nenhum cliente cadastrado."); return; }

        listarClientes();
        int id = lerIdExistente("ID do cliente: ", clientes.stream().mapToInt(c -> c.getIdPessoa()).toArray());
        ClienteEntity c = buscarClientePorId(id);

        System.out.println("\n-----------------------------------------");
        System.out.println("  Dados do Cliente");
        System.out.println("-----------------------------------------");
        System.out.println("  ID:       " + c.getIdPessoa());
        System.out.println("  Nome:     " + c.getNome());
        System.out.println("  CPF:      " + c.getCpf());
        System.out.println("  Telefone: " + c.getTelefone());
        System.out.println("  E-mail:   " + c.getEmail());
        System.out.println("  Endereço: " + c.getEndereco());
        c.consultarHistorico();
    }

    // =========================================================
    // HELPERS - Listagens
    // =========================================================
    static void listarClientes() {
        System.out.println("\n  Clientes cadastrados:");
        for (ClienteEntity c : clientes)
            System.out.println("  [" + c.getIdPessoa() + "] " + c.getNome() + " | CPF: " + c.getCpf());
    }

    static void listarVendedores() {
        System.out.println("\n  Funcionários cadastrados:");
        for (VendedorEntity v : vendedores)
            System.out.println("  [" + v.getIdPessoa() + "] " + v.getNome() + " | Cargo: " + v.getCargo());
    }

    static void listarProdutos() {
        System.out.println("\n  Produtos disponíveis:");
        for (ProdutoEntity p : produtos)
            System.out.println("  [" + p.getIdProduto() + "] " + p.getNome() +
                " | R$ " + String.format("%.2f", p.getPreco()) +
                " | Estoque: " + p.getEstoqueAtual() + " " + p.getUnidadeMedida());
    }

    static void listarCategorias() {
        System.out.println("\n  Categorias:");
        for (CategoriaEntity c : categorias)
            System.out.println("  [" + c.getIdCategoria() + "] " + c.getNome() + " — " + c.getDescricao());
    }

    // =========================================================
    // HELPERS - Buscas
    // =========================================================
    static ClienteEntity   buscarClientePorId(int id)   { for (ClienteEntity   c : clientes)   if (c.getIdPessoa()    == id) return c; return null; }
    static VendedorEntity  buscarVendedorPorId(int id)  { for (VendedorEntity  v : vendedores)  if (v.getIdPessoa()    == id) return v; return null; }
    static ProdutoEntity   buscarProdutoPorId(int id)   { for (ProdutoEntity   p : produtos)    if (p.getIdProduto()   == id) return p; return null; }
    static CategoriaEntity buscarCategoriaPorId(int id) { for (CategoriaEntity c : categorias)  if (c.getIdCategoria() == id) return c; return null; }

    // =========================================================
    // HELPERS - Validações e Leitura segura
    // =========================================================
    static int lerIntRange(String msg, int min, int max) {
        while (true) {
            System.out.print(msg);
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                if (val >= min && val <= max) return val;
                System.out.println("[!] Digite um número entre " + min + " e " + max + ".");
            } catch (NumberFormatException e) { System.out.println("[!] Entrada inválida. Digite um número inteiro."); }
        }
    }

    static int lerIntMin(String msg, int min) {
        while (true) {
            System.out.print(msg);
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                if (val >= min) return val;
                System.out.println("[!] O valor mínimo é " + min + ".");
            } catch (NumberFormatException e) { System.out.println("[!] Entrada inválida. Digite um número inteiro."); }
        }
    }

    static double lerDoubleMin(String msg, double min) {
        while (true) {
            System.out.print(msg);
            try {
                double val = Double.parseDouble(sc.nextLine().trim().replace(",", "."));
                if (val >= min) return val;
                System.out.println("[!] O valor mínimo é " + String.format("%.2f", min) + ".");
            } catch (NumberFormatException e) { System.out.println("[!] Entrada inválida. Use formato numérico (ex: 29.90)."); }
        }
    }

    static String lerTextoObrigatorio(String msg, int minLen, int maxLen) {
        while (true) {
            System.out.print(msg);
            String val = sc.nextLine().trim();
            if (val.isBlank())             System.out.println("[!] Este campo é obrigatório.");
            else if (val.length() < minLen) System.out.println("[!] Mínimo de " + minLen + " caracteres.");
            else if (val.length() > maxLen) System.out.println("[!] Máximo de " + maxLen + " caracteres.");
            else return val;
        }
    }

    static String lerEmail(String msg) {
        while (true) {
            System.out.print(msg);
            String val = sc.nextLine().trim();
            if (validarEmail(val)) return val;
            System.out.println("[!] E-mail inválido. Formato esperado: usuario@dominio.com");
        }
    }

    static boolean validarEmail(String email) {
        return email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    static String lerTelefone(String msg) {
        while (true) {
            System.out.print(msg);
            String val = sc.nextLine().trim();
            if (validarTelefone(val)) return val;
            System.out.println("[!] Telefone inválido. Use: (11) 91234-5678 ou (11) 1234-5678");
        }
    }

    static boolean validarTelefone(String tel) {
        return tel.matches("^\\(\\d{2}\\) \\d{4,5}-\\d{4}$");
    }

    static String lerCPFouCNPJ(String msg) {
        while (true) {
            System.out.print(msg);
            String val = sc.nextLine().trim();
            if (val.matches("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$")) return val;
            if (val.matches("^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$")) return val;
            System.out.println("[!] Formato inválido.  CPF: 000.000.000-00  |  CNPJ: 00.000.000/0000-00");
        }
    }

    static String lerOpcaoTexto(String msg, String[] opcoes) {
        while (true) {
            System.out.print(msg);
            String val = sc.nextLine().trim();
            for (String op : opcoes) if (op.equalsIgnoreCase(val)) return op;
            System.out.println("[!] Opção inválida. Escolha entre: " + String.join(" | ", opcoes));
        }
    }

    static int lerIdExistente(String msg, int[] idsValidos) {
        while (true) {
            System.out.print(msg);
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                for (int id : idsValidos) if (id == val) return val;
                System.out.println("[!] ID não encontrado. Escolha um ID da lista acima.");
            } catch (NumberFormatException e) { System.out.println("[!] Entrada inválida. Digite o número do ID."); }
        }
    }

    // =========================================================
    // DADOS INICIAIS
    // =========================================================
    static void carregarDadosIniciais() {
        CategoriaEntity ce = new CategoriaEntity(proximoIdCategoria++, "Ferramentas Elétricas", "Furadeiras, serras, lixadeiras");
        CategoriaEntity cm = new CategoriaEntity(proximoIdCategoria++, "Ferramentas Manuais", "Chaves, alicates, martelos");
        categorias.add(ce); categorias.add(cm);

        produtos.add(new ProdutoEntity(proximoIdProduto++, "Furadeira de Impacto 750W", "Bivolt com maleta",    349.90, 50,  "un", ce));
        produtos.add(new ProdutoEntity(proximoIdProduto++, "Parafusadeira 12V",          "2 baterias e carregador", 289.90, 30, "un", ce));
        produtos.add(new ProdutoEntity(proximoIdProduto++, "Jogo de Chaves 12 peças",    "Aço cromo vanádio",    89.90, 100, "jg", cm));

        vendedores.add(new VendedorEntity(proximoIdPessoa++, "Roberto Alves", "111.222.333-44",
            "(11) 91234-5678", "roberto@toolsync.com", "Rua das Ferramentas, 100", "Vendedor Sênior"));

        clientes.add(new ClienteEntity(proximoIdPessoa++, "Construtora Horizonte", "12.345.678/0001-90",
            "(11) 3456-7890", "compras@horizonte.com", "Av. Paulista, 1000 - SP"));

        System.out.println("[Sistema iniciado com dados de exemplo]");
    }
}
