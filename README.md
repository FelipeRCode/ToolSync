# ToolSync — Sistema de Gestão de Ferramentas

Aplicação web para uma loja de ferramentas, com quatro perfis de acesso: **Proprietário, Gerente, Vendedor e Estoquista**. Ela cobre clientes, funcionários, fornecedores, produtos, estoque, vendas com várias formas de pagamento, relatórios e backup do banco.

Projeto de Extensão Comunitária (Curricularização) — Fatec São José do Rio Preto, 4º semestre de 2026.

| Camada | Tecnologia |
| --- | --- |
| Front-end | HTML, CSS e JavaScript, servidos pelo próprio Spring Boot |
| Back-end | Java 17, Spring Boot 4.0.6 (Web MVC + Spring Data JPA / Hibernate 7), Lombok — mesmo padrão dos projetos da aula (war + ServletInitializer) |
| Banco de dados | SQL Server, com procedures, triggers, views e functions |
| Senhas | Hash BCrypt (`spring-security-crypto`) |

---

## Estrutura do repositório

```
ToolSync/
├── banco/
│   ├── toolsync_banco.sql      script completo: tabelas, procedures, triggers, views, functions e carga de dados
│   ├── DER_ToolSync.svg        diagrama entidade-relacionamento (vetorial)
│   └── DER_ToolSync.png
├── src/main/java/br/edu/fatecriopreto/toolsync/
│   ├── entities/               classes @Entity (herança, relacionamentos N x N com @EmbeddedId)
│   ├── repositories/           interfaces Spring Data JPA
│   ├── services/               regras de negócio; chamadas às procedures, views e functions
│   ├── controllers/            API REST (/api/...)
│   ├── dto/                    objetos de entrada e saída da API
│   ├── seguranca/              login por sessão e controle de acesso por perfil (@Perfis)
│   └── config/                 tratamento de erros, nomes de tabela/coluna, rotas
├── src/main/resources/
│   ├── application.properties  conexão com o SQL Server
│   └── static/                 front-end (login.html, proprietario/, gerente/, vendedor/, estoquista/, js/toolsync.js)
├── versao-console/             primeira versão do projeto (Java puro, menus no terminal)
├── pom.xml
└── mvnw / mvnw.cmd             Maven Wrapper (não precisa instalar o Maven)
```

## Como rodar

**Pré-requisitos:** JDK 17 ou superior e SQL Server 2017 ou superior. Funciona no SQL Server Express.

**1. Criar o banco.** Abra `banco/toolsync_banco.sql` no SSMS e execute o script inteiro. Ele cria o banco `ToolSync`, todos os objetos e a carga de dados de teste.

**2. Ajustar a conexão** em `src/main/resources/application.properties`:

```properties
# Autenticação do Windows (padrão, como no projeto "prova"). Troque máquina\instância e porta pelas da sua.
spring.datasource.url=jdbc:sqlserver://PRESSSTART\\SQLEXPRESS01:50562;databaseName=ToolSync;encrypt=true;trustServerCertificate=true;integratedSecurity=true

# Ou usuário do SQL Server (laboratório):
#spring.datasource.url=jdbc:sqlserver://localhost\\SQLEXPRESS:1433;databaseName=ToolSync;encrypt=true;trustServerCertificate=true
#spring.datasource.username=aluno
#spring.datasource.password=
```

A instância precisa estar com o protocolo **TCP/IP habilitado**, porque o driver JDBC só conecta por TCP. Para descobrir a porta, rode no SSMS:

```sql
select distinct local_tcp_port from sys.dm_exec_connections where local_tcp_port is not null
```

**3. Subir a aplicação:**

```powershell
.\mvnw.cmd spring-boot:run
```

**4. Abrir** <http://localhost:8080>.

Na autenticação do Windows (`integratedSecurity=true`), o driver precisa da DLL `mssql-jdbc_auth`. O próprio Maven a baixa para `target/native`, e o `spring-boot:run` já a coloca no caminho. O `mvnw package` gera um `.war` executável (como nos projetos da aula). Para rodá-lo direto, use:

```powershell
java "-Djava.library.path=target/native" -jar target/toolsync-0.0.1-SNAPSHOT.war
```

### Rodando pelo VS Code

O jeito mais simples é abrir a pasta `ToolSync` no VS Code e rodar `.\mvnw.cmd spring-boot:run` no terminal integrado.

Para usar o botão Run / Spring Boot Dashboard com a autenticação do Windows, rode uma vez `.\mvnw.cmd generate-resources` (baixa a DLL para `target/native`) e crie `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "ToolSync",
      "request": "launch",
      "mainClass": "br.edu.fatecriopreto.toolsync.ToolSyncApplication",
      "vmArgs": "-Djava.library.path=${workspaceFolder}/target/native -Dfile.encoding=UTF-8"
    }
  ]
}
```

### Porta 8080 ocupada

Rodar em outra porta, sem editar arquivos:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Ver e encerrar o processo que está usando a porta 8080:

```powershell
Get-Process -Id (Get-NetTCPConnection -LocalPort 8080 -State Listen).OwningProcess
Stop-Process -Id (Get-NetTCPConnection -LocalPort 8080 -State Listen).OwningProcess -Force
```

### Usuários de teste

A senha de todos é `toolsync123`.

| Login | Perfil | O que acessa |
| --- | --- | --- |
| `ana` | Proprietário | tudo, inclusive Configurações (backup e restore) |
| `carlos` | Gerente | funcionários, fornecedores, estoque e relatórios |
| `pedro`, `mariana`, `rafael` | Vendedor | clientes e vendas (só as próprias vendas) |
| `joao`, `beatriz` | Estoquista | cadastro de produtos, consulta de estoque e entrada de mercadoria |
| `lucas` | Vendedor inativo | não entra; demonstra o trigger que bloqueia o login de funcionário desligado |

---

## Banco de dados

O banco tem 16 tabelas e 3 relacionamentos N x N:
- Vendas x Produtos (`ItensVenda`)
- Produtos x Fornecedores (`ProdutoFornecedor`)
- Produtos x Localizações (`ProdutoLocalizacao`)

Também há herança: `Pessoas` é a tabela-mãe de `Clientes`, `Funcionarios` e `Fornecedores`. O diagrama completo está em [`banco/DER_ToolSync.png`](banco/DER_ToolSync.png).

| Tipo | Objeto | Onde o sistema usa |
| --- | --- | --- |
| Procedure | `sp_CadastrarFuncionario` | Cadastrar funcionário (Pessoas + Funcionarios + Usuarios na mesma transação) |
| Procedure | `sp_EntradaEstoque` | Entrada de mercadoria e estoque inicial do produto |
| Procedure | `sp_RegistrarVenda` / `sp_RegistrarPagamentos` | Nova venda (itens e pagamentos em JSON) |
| Procedure | `sp_ConcluirReserva` | Concluir venda reservada |
| Procedure | `sp_CancelarVenda` | Cancelar venda (devolve ao estoque) |
| Procedure | `sp_BackupBanco` | Configurações → Fazer backup |
| Trigger | `tg_ItensVenda_GerarMovimentacao` | Baixa ou reserva o estoque e calcula o total da venda |
| Trigger | `tg_MovimentacoesEstoque_AtualizarSaldo` | Atualiza o saldo e bloqueia estoque negativo |
| Trigger | `tg_Produtos_HistoricoPreco` | Grava o histórico de preços |
| Trigger | `tg_Funcionarios_SincronizarUsuario` | Desativa o login do funcionário desligado |
| View | `v_VendasDetalhadas` | Consultar vendas |
| View | `v_RelatorioVendas` | Relatórios |
| View | `v_EstoqueBaixo` | Dashboards |
| View | `v_HistoricoMovimentacoes` | Estoque (histórico) e dashboard do estoquista |
| View | `v_DashboardVendas` | Cards de vendas do dia, da semana e do mês |
| Function | `fc_TotalVendasPeriodo` (escalar) | Vendas por mês nos dashboards |
| Function | `fc_UltimaCompraCliente` (escalar) | Coluna "Última compra" de clientes |
| Function | `fc_HistoricoCliente` (tabela) | Detalhes do cliente |
| Function | `fc_VendasPorVendedor` (tabela) | Gráfico de vendas da semana |

O restore do backup é feito pelo Java numa conexão separada ao banco `master`, porque o SQL Server não restaura um banco em uso pela própria conexão.

## API REST (resumo)

Todas as rotas exigem login, menos `/api/auth/login`. O perfil de cada rota é conferido pela anotação `@Perfis`.

| Rota | Descrição |
| --- | --- |
| `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me` | sessão |
| `/api/clientes`, `/api/funcionarios`, `/api/fornecedores`, `/api/produtos` | CRUD (GET, POST, PUT, DELETE) |
| `GET /api/categorias`, `GET /api/marcas`, `GET /api/estoque/localizacoes` | listas auxiliares |
| `POST /api/estoque/entradas`, `GET /api/estoque/movimentacoes`, `GET /api/estoque/baixo` | estoque |
| `GET/POST /api/vendas`, `POST /api/vendas/{id}/concluir`, `POST /api/vendas/{id}/cancelar` | vendas |
| `GET /api/dashboard`, `GET /api/relatorios/vendas`, `GET /api/relatorios/vendedores` | indicadores |
| `POST /api/sistema/backup`, `GET /api/sistema/backups`, `POST /api/sistema/restore` | backup e restore (proprietário) |

---

## Integrantes

- Gabriel Parra
- Felipe dos Reis
- Pedro Henrique do Amaral
- Poliane Soares da Cruz
- Ygor Vinicius da Silva Borges
