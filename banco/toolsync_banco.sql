-------------------------------------------------------------
-- ToolSync - Sistema de Gestao de Ferramentas
-- Script SQL Completo (SQL Server 2016 ou superior)
--
-- Fatec Sao Jose do Rio Preto - Projeto Interdisciplinar
--
-- O que este script atende (documentacao do projeto):
--   Etapa 2 (3.5 / 3.6)
--     - 16 tabelas, com 3 relacionamentos N x N:
--         Vendas    x Produtos      -> ItensVenda
--         Produtos  x Fornecedores  -> ProdutoFornecedor
--         Produtos  x Localizacoes  -> ProdutoLocalizacao
--     - Heranca: Pessoas -> Clientes / Funcionarios / Fornecedores
--     - 4 perfis de acesso (Funcionarios.cargo): PROPRIETARIO,
--       GERENTE, VENDEDOR e ESTOQUISTA
--     - Injecao de dados para testes e apresentacao (final do script)
--   Etapa 3 (4.2 / 4.3)
--     - 7 procedures (transacoes chamadas pelo sistema Java)
--     - 4 triggers   (disparados por transacoes do sistema Java)
--     - 5 views      (consultas complexas chamadas pelo sistema Java)
--     - 4 functions  (2 retornam variavel e 2 retornam tabela)
--     - Backup do banco (sp_BackupBanco) e comandos de Restore
--
-- Senha de teste de TODOS os usuarios cadastrados: toolsync123
-- (gravada como hash BCrypt, o mesmo formato usado pelo Spring Security)
-------------------------------------------------------------

-------------------------------------------------------------
-- Para recriar o banco do zero, descomente as linhas abaixo:
-------------------------------------------------------------
-- use master
-- go
-- alter database ToolSync set single_user with rollback immediate
-- go
-- drop database ToolSync
-- go

-------------------------------------------------------------
-- Criar o banco de dados
-------------------------------------------------------------
create database ToolSync
go

-------------------------------------------------------------
-- Acessar o banco de dados
-------------------------------------------------------------
use ToolSync
go

-------------------------------------------------------------
-- Criar a tabela Pessoas (tabela MAE da heranca)
-- Guarda os dados comuns de clientes, funcionarios e fornecedores:
-- nome, contato e endereco.
-------------------------------------------------------------
create table Pessoas
(
	id				int				not null	primary key		identity,
	nome			varchar(150)	not null	check(len(ltrim(nome)) >= 3),
	email			varchar(150)		null	check(email like '%_@_%._%'),
	telefone		varchar(20)			null	check(	telefone like '([0-9][0-9]) [0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]'
													or telefone like '([0-9][0-9]) [0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]'),
	rua				varchar(150)		null,
	numero			varchar(10)			null,
	bairro			varchar(80)			null,
	cep				char(9)				null	check(cep like '[0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9]'),
	cidade			varchar(80)			null,
	uf				char(2)				null	check(uf in ('AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA',
															 'PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO')),
	data_cadastro	datetime		not null	default getdate()
)
go

-------------------------------------------------------------
-- Criar a tabela Clientes. Todo cliente e uma Pessoa.
-- Aceita pessoa fisica (CPF) e pessoa juridica (CNPJ).
-------------------------------------------------------------
create table Clientes
(
	pessoa_id		int				not null	primary key		references Pessoas(id),
	tipo_pessoa		char(2)			not null	check(tipo_pessoa in ('PF', 'PJ')),
	documento		varchar(18)		not null	unique,
	check(	(tipo_pessoa = 'PF' and documento like '[0-9][0-9][0-9].[0-9][0-9][0-9].[0-9][0-9][0-9]-[0-9][0-9]')
		or	(tipo_pessoa = 'PJ' and documento like '[0-9][0-9].[0-9][0-9][0-9].[0-9][0-9][0-9]/[0-9][0-9][0-9][0-9]-[0-9][0-9]') )
)
go

-------------------------------------------------------------
-- Criar a tabela Funcionarios. Todo funcionario e uma Pessoa.
-- O cargo define o nivel de acesso (perfil) no sistema.
-------------------------------------------------------------
create table Funcionarios
(
	pessoa_id		int				not null	primary key		references Pessoas(id),
	matricula		varchar(15)		not null	unique,
	rg				varchar(15)		not null,
	cpf				char(14)		not null	unique
					check(cpf like '[0-9][0-9][0-9].[0-9][0-9][0-9].[0-9][0-9][0-9]-[0-9][0-9]'),
	data_nascimento	date			not null	check(data_nascimento <= dateadd(year, -16, cast(getdate() as date))),
	cargo			varchar(15)		not null	check(cargo in ('PROPRIETARIO', 'GERENTE', 'VENDEDOR', 'ESTOQUISTA')),
	data_admissao	date			not null	default cast(getdate() as date),
	ativo			bit				not null	default 1
)
go

-------------------------------------------------------------
-- Criar a tabela Usuarios (login do sistema). Relacao 1 x 1 com
-- Funcionarios: so funcionario tem acesso ao sistema.
-- A senha NUNCA e gravada pura: o Java grava o hash BCrypt (60 caracteres).
-------------------------------------------------------------
create table Usuarios
(
	funcionario_id	int				not null	primary key		references Funcionarios(pessoa_id),
	login			varchar(50)		not null	unique	check(len(login) >= 3),
	senha_hash		char(60)		not null,
	ativo			bit				not null	default 1,
	ultimo_acesso	datetime			null
)
go

-------------------------------------------------------------
-- Criar a tabela Fornecedores. Todo fornecedor e uma Pessoa
-- (nome = razao social).
-------------------------------------------------------------
create table Fornecedores
(
	pessoa_id			int				not null	primary key		references Pessoas(id),
	cnpj				char(18)		not null	unique
						check(cnpj like '[0-9][0-9].[0-9][0-9][0-9].[0-9][0-9][0-9]/[0-9][0-9][0-9][0-9]-[0-9][0-9]'),
	ramo				varchar(100)	not null,
	regime_tributario	varchar(20)		not null	check(regime_tributario in ('Simples Nacional', 'Lucro Presumido', 'Lucro Real')),
	representante		varchar(100)	not null,
	dados_bancarios		varchar(150)		null,
	ativo				bit				not null	default 1
)
go

-------------------------------------------------------------
-- Criar a tabela Categorias de produtos
-------------------------------------------------------------
create table Categorias
(
	id			int				not null	primary key		identity,
	nome		varchar(60)		not null	unique,
	descricao	varchar(200)		null
)
go

-------------------------------------------------------------
-- Criar a tabela Marcas de produtos
-------------------------------------------------------------
create table Marcas
(
	id			int				not null	primary key		identity,
	nome		varchar(60)		not null	unique
)
go

-------------------------------------------------------------
-- Criar a tabela Produtos (ferramentas)
-- estoque_atual e estoque_reservado so mudam pelo trigger de
-- MovimentacoesEstoque. estoque_disponivel e calculado.
-------------------------------------------------------------
create table Produtos
(
	id					int				not null	primary key		identity,
	codigo				varchar(10)		not null	unique,
	nome				varchar(120)	not null,
	descricao			varchar(500)		null,
	categoria_id		int				not null	references Categorias(id),
	marca_id			int					null	references Marcas(id),
	ean					varchar(14)			null,
	sku					varchar(30)			null,
	ncm					varchar(10)			null,
	preco_custo			decimal(10,2)	not null	default 0	check(preco_custo >= 0),
	preco_venda			decimal(10,2)	not null	check(preco_venda > 0),
	unidade_compra		varchar(5)		not null	default 'UN',
	unidade_venda		varchar(5)		not null	default 'UN',
	largura_cm			decimal(7,2)		null	check(largura_cm > 0),
	altura_cm			decimal(7,2)		null	check(altura_cm > 0),
	comprimento_cm		decimal(7,2)		null	check(comprimento_cm > 0),
	imagem_url			varchar(255)		null,
	estoque_atual		int				not null	default 0	check(estoque_atual >= 0),
	estoque_reservado	int				not null	default 0	check(estoque_reservado >= 0),
	estoque_minimo		int				not null	default 0	check(estoque_minimo >= 0),
	estoque_disponivel	as (estoque_atual - estoque_reservado),
	ativo				bit				not null	default 1,
	check(estoque_reservado <= estoque_atual)
)
go

-- EAN e SKU sao opcionais, mas quando informados nao podem se repetir
create unique index UX_Produtos_ean on Produtos(ean) where ean is not null
go
create unique index UX_Produtos_sku on Produtos(sku) where sku is not null
go

-------------------------------------------------------------
-- Criar a tabela ProdutoFornecedor (N x N)
-- Um produto pode ser comprado de varios fornecedores e um
-- fornecedor vende varios produtos.
-------------------------------------------------------------
create table ProdutoFornecedor
(
	produto_id				int				not null	references Produtos(id),
	fornecedor_id			int				not null	references Fornecedores(pessoa_id),
	codigo_no_fornecedor	varchar(30)			null,
	ultimo_preco_compra		decimal(10,2)		null	check(ultimo_preco_compra >= 0),
	data_ultima_compra		datetime			null,
	primary key (produto_id, fornecedor_id)
)
go

-------------------------------------------------------------
-- Criar a tabela Localizacoes (enderecos fisicos do estoque)
-------------------------------------------------------------
create table Localizacoes
(
	id			int				not null	primary key		identity,
	galpao		varchar(50)		not null,
	corredor	varchar(10)		not null,
	prateleira	varchar(10)		not null,
	box			varchar(10)		not null,
	unique (galpao, corredor, prateleira, box)
)
go

-------------------------------------------------------------
-- Criar a tabela ProdutoLocalizacao (N x N)
-- Um produto pode estar guardado em varios locais e um local
-- guarda varios produtos. A quantidade indica quanto foi guardado
-- em cada local (atualizada nas entradas de estoque).
-------------------------------------------------------------
create table ProdutoLocalizacao
(
	produto_id		int		not null	references Produtos(id),
	localizacao_id	int		not null	references Localizacoes(id),
	quantidade		int		not null	default 0	check(quantidade >= 0),
	primary key (produto_id, localizacao_id)
)
go

-------------------------------------------------------------
-- Criar a tabela Vendas (cabecalho da venda)
-------------------------------------------------------------
create table Vendas
(
	id					int				not null	primary key		identity,
	data_venda			datetime		not null	default getdate(),
	cliente_id			int				not null	references Clientes(pessoa_id),
	vendedor_id			int				not null	references Funcionarios(pessoa_id),
	numero_cupom		varchar(20)			null,
	numero_nf			varchar(20)			null,
	valor_total			decimal(12,2)	not null	default 0	check(valor_total >= 0),
	status				varchar(12)		not null	default 'ABERTA'
						check(status in ('ABERTA', 'RESERVADA', 'FINALIZADA', 'CANCELADA')),
	data_cancelamento	datetime			null,
	motivo_cancelamento	varchar(200)		null,
	-- venda cancelada precisa ter a data do cancelamento (e so ela)
	check(	(status =  'CANCELADA' and data_cancelamento is not null)
		or	(status <> 'CANCELADA' and data_cancelamento is null) )
)
go

-------------------------------------------------------------
-- Criar a tabela ItensVenda (N x N entre Vendas e Produtos)
-- O preco unitario e copiado do produto no momento da venda, assim
-- uma mudanca de preco depois nao altera vendas antigas.
-------------------------------------------------------------
create table ItensVenda
(
	id				int				not null	primary key		identity,
	venda_id		int				not null	references Vendas(id),
	produto_id		int				not null	references Produtos(id),
	quantidade		int				not null	check(quantidade > 0),
	preco_unitario	decimal(10,2)	not null	check(preco_unitario >= 0),
	desconto		decimal(10,2)	not null	default 0	check(desconto >= 0),
	observacao		varchar(150)		null,
	subtotal		as (quantidade * preco_unitario - desconto) persisted,
	unique (venda_id, produto_id),
	check(desconto <= quantidade * preco_unitario)
)
go

-------------------------------------------------------------
-- Criar a tabela PagamentosVenda
-- Uma venda pode ser paga com mais de uma forma de pagamento
-- (ex.: parte no PIX e parte no dinheiro).
-------------------------------------------------------------
create table PagamentosVenda
(
	id				int				not null	primary key		identity,
	venda_id		int				not null	references Vendas(id),
	forma			varchar(15)		not null	check(forma in ('DINHEIRO', 'PIX', 'CARTAO_DEBITO', 'CARTAO_CREDITO')),
	valor			decimal(12,2)	not null	check(valor > 0),
	valor_recebido	decimal(12,2)		null,
	troco			as (isnull(valor_recebido - valor, 0)),
	parcelas		tinyint			not null	default 1	check(parcelas between 1 and 12),
	data_pagamento	datetime		not null	default getdate(),
	-- parcelamento so no cartao de credito
	check(forma = 'CARTAO_CREDITO' or parcelas = 1),
	-- valor recebido (para calcular troco) so no dinheiro e nunca menor que o valor
	check(valor_recebido is null or (forma = 'DINHEIRO' and valor_recebido >= valor))
)
go

-------------------------------------------------------------
-- Criar a tabela MovimentacoesEstoque
-- Toda mudanca de estoque passa por aqui (o trigger atualiza o saldo):
--   ENTRADA   -> soma no estoque atual   (compra / nota fiscal)
--   SAIDA     -> tira do estoque atual   (venda)
--   ESTORNO   -> devolve ao estoque atual (venda cancelada)
--   RESERVA   -> separa unidades para um cliente (estoque reservado)
--   LIBERACAO -> desfaz uma reserva
-------------------------------------------------------------
create table MovimentacoesEstoque
(
	id					int				not null	primary key		identity,
	produto_id			int				not null	references Produtos(id),
	tipo				varchar(10)		not null	check(tipo in ('ENTRADA', 'SAIDA', 'ESTORNO', 'RESERVA', 'LIBERACAO')),
	quantidade			int				not null	check(quantidade > 0),
	data_movimentacao	datetime		not null	default getdate(),
	funcionario_id		int				not null	references Funcionarios(pessoa_id),
	venda_id			int					null	references Vendas(id),
	fornecedor_id		int					null	references Fornecedores(pessoa_id),
	documento			varchar(150)		null
)
go

-------------------------------------------------------------
-- Criar a tabela HistoricoPrecos (alimentada pelo trigger de Produtos)
-------------------------------------------------------------
create table HistoricoPrecos
(
	id						int				not null	primary key		identity,
	produto_id				int				not null	references Produtos(id),
	preco_custo_anterior	decimal(10,2)	not null,
	preco_custo_novo		decimal(10,2)	not null,
	preco_venda_anterior	decimal(10,2)	not null,
	preco_venda_novo		decimal(10,2)	not null,
	data_alteracao			datetime		not null	default getdate(),
	usuario_banco			varchar(128)	not null	default suser_sname()
)
go

-------------------------------------------------------------
-- Indices para as consultas mais usadas pelo sistema
-------------------------------------------------------------
create index IX_Vendas_data			on Vendas(data_venda)
go
create index IX_Vendas_cliente		on Vendas(cliente_id)
go
create index IX_Vendas_vendedor		on Vendas(vendedor_id)
go
create index IX_Movimentacoes_produto	on MovimentacoesEstoque(produto_id, data_movimentacao)
go


-------------------------------------------------------------
--                       TRIGGERS
-------------------------------------------------------------

-------------------------------------------------------------
-- TRIGGER 1
-- Sempre que itens forem inseridos em uma venda (pela procedure
-- sp_RegistrarVenda, chamada pelo sistema Java), o trigger:
--   1) impede incluir itens em venda finalizada ou cancelada;
--   2) gera a movimentacao de estoque de cada item
--      (SAIDA na venda normal / RESERVA na venda reservada);
--   3) recalcula o valor total da venda.
-- TABELA:	ItensVenda
-- EVENTO:	insert
-- TIPO:	After
-------------------------------------------------------------
create trigger tg_ItensVenda_GerarMovimentacao
on		ItensVenda
after	insert
as
begin
	set nocount on;

	-- 1) so aceita itens em venda ABERTA ou RESERVADA
	if exists (	select 1 from inserted as I inner join Vendas as V on V.id = I.venda_id
				where V.status in ('FINALIZADA', 'CANCELADA') )
		throw 50060, 'Nao e possivel incluir itens em uma venda finalizada ou cancelada.', 1;

	-- 2) gera a movimentacao de estoque (o trigger de MovimentacoesEstoque atualiza o saldo)
	insert into MovimentacoesEstoque (produto_id, tipo, quantidade, funcionario_id, venda_id, documento)
	select	I.produto_id,
			case when V.status = 'RESERVADA' then 'RESERVA' else 'SAIDA' end,
			I.quantidade,
			V.vendedor_id,
			V.id,
			'Venda #' + right('0000' + cast(V.id as varchar(10)), 4) + ' - ' + C.nome
	from inserted as I
		inner join Vendas	as V on V.id = I.venda_id
		inner join Pessoas	as C on C.id = V.cliente_id

	-- 3) recalcula o total das vendas que receberam itens
	update V set V.valor_total = (select isnull(sum(IV.subtotal), 0) from ItensVenda as IV where IV.venda_id = V.id)
	from Vendas as V
	where V.id in (select venda_id from inserted)
end
go

-------------------------------------------------------------
-- TRIGGER 2
-- Atualiza o saldo de estoque do produto a cada movimentacao e
-- BLOQUEIA a operacao se o estoque ficar negativo ou se a venda
-- tentar usar unidades que estao reservadas para outro cliente.
-- Disparado pelas transacoes de venda, cancelamento, reserva e
-- entrada de estoque feitas pelo sistema Java.
-- TABELA:	MovimentacoesEstoque
-- EVENTO:	insert
-- TIPO:	After
-------------------------------------------------------------
create trigger tg_MovimentacoesEstoque_AtualizarSaldo
on		MovimentacoesEstoque
after	insert
as
begin
	set nocount on;

	-- soma o efeito de todas as movimentacoes inseridas, por produto
	declare @Efeito table
	(
		produto_id		int		primary key,
		delta_atual		int,
		delta_reservado	int
	)

	insert into @Efeito (produto_id, delta_atual, delta_reservado)
	select	produto_id,
			sum(case	when tipo in ('ENTRADA', 'ESTORNO')	then  quantidade
						when tipo = 'SAIDA'					then -quantidade
						else 0 end),
			sum(case	when tipo = 'RESERVA'				then  quantidade
						when tipo = 'LIBERACAO'				then -quantidade
						else 0 end)
	from inserted
	group by produto_id

	-- valida antes de atualizar
	declare @produto varchar(150)

	select top 1 @produto = P.codigo + ' - ' + P.nome
	from Produtos as P inner join @Efeito as E on E.produto_id = P.id
	where	P.estoque_atual		+ E.delta_atual		< 0
		or	P.estoque_reservado	+ E.delta_reservado	< 0
		or	P.estoque_reservado	+ E.delta_reservado	> P.estoque_atual + E.delta_atual

	if @produto is not null
	begin
		declare @mensagem nvarchar(300) = N'Estoque insuficiente para o produto ' + @produto + N'.';
		throw 50061, @mensagem, 1;
	end

	-- atualiza o saldo
	update P set	P.estoque_atual		= P.estoque_atual		+ E.delta_atual,
					P.estoque_reservado	= P.estoque_reservado	+ E.delta_reservado
	from Produtos as P inner join @Efeito as E on E.produto_id = P.id
end
go

-------------------------------------------------------------
-- TRIGGER 3
-- Quando o preco de custo ou de venda de um produto for alterado
-- (tela de cadastro de produto ou entrada de estoque no Java),
-- grava o preco antigo e o novo na tabela HistoricoPrecos.
-- TABELA:	Produtos
-- EVENTO:	update
-- TIPO:	After
-------------------------------------------------------------
create trigger tg_Produtos_HistoricoPreco
on		Produtos
after	update
as
begin
	set nocount on;

	if update(preco_custo) or update(preco_venda)
		insert into HistoricoPrecos (produto_id, preco_custo_anterior, preco_custo_novo, preco_venda_anterior, preco_venda_novo)
		select	I.id, D.preco_custo, I.preco_custo, D.preco_venda, I.preco_venda
		from inserted as I inner join deleted as D on D.id = I.id
		where	I.preco_custo <> D.preco_custo
			or	I.preco_venda <> D.preco_venda
end
go

-------------------------------------------------------------
-- TRIGGER 4
-- Quando um funcionario for desativado ou reativado (tela de
-- detalhes do funcionario no Java), o login dele acompanha:
-- funcionario inativo nao entra no sistema.
-- Tambem garante que sempre exista pelo menos um PROPRIETARIO ativo.
-- TABELA:	Funcionarios
-- EVENTO:	update
-- TIPO:	After
-------------------------------------------------------------
create trigger tg_Funcionarios_SincronizarUsuario
on		Funcionarios
after	update
as
begin
	set nocount on;

	if not exists (select 1 from Funcionarios where cargo = 'PROPRIETARIO' and ativo = 1)
		throw 50062, 'O sistema precisa ter pelo menos um proprietario ativo.', 1;

	if update(ativo)
		update U set U.ativo = I.ativo
		from Usuarios as U
			inner join inserted as I on I.pessoa_id = U.funcionario_id
			inner join deleted	as D on D.pessoa_id = I.pessoa_id
		where I.ativo <> D.ativo
end
go


-------------------------------------------------------------
--                       FUNCTIONS
-------------------------------------------------------------

-------------------------------------------------------------
-- FUNCTION 1 (escalar: retorna uma variavel)
-- Retorna o total vendido (vendas FINALIZADAS) em um periodo.
-- Usada nos cards "Vendas do dia / semana / mes" dos dashboards
-- (atraves da view v_DashboardVendas).
-------------------------------------------------------------
create function fc_TotalVendasPeriodo
(	-- parametros recebidos
	@inicio		datetime,
	@fim		datetime	-- exclusivo: vendas antes desta data
)
returns decimal(12,2)
as
begin
	declare @total decimal(12,2)

	select @total = isnull(sum(valor_total), 0)
	from Vendas
	where	status = 'FINALIZADA'
		and	data_venda >= @inicio
		and	data_venda <  @fim

	return @total
end
go

-------------------------------------------------------------
-- FUNCTION 2 (escalar: retorna uma variavel)
-- Retorna a ultima compra do cliente ja formatada, do jeito que a
-- tela "Consultar Clientes" mostra. Ex.: '08/11/2026 (R$ 850,00)'
-------------------------------------------------------------
create function fc_UltimaCompraCliente
(	-- parametro recebido
	@clienteId	int
)
returns varchar(40)
as
begin
	declare @retorno varchar(40)

	select top 1 @retorno = convert(varchar(10), data_venda, 103) + ' (R$ ' + format(valor_total, 'N2', 'pt-BR') + ')'
	from Vendas
	where	cliente_id = @clienteId
		and	status = 'FINALIZADA'
	order by data_venda desc

	return isnull(@retorno, 'Sem compras')
end
go

-------------------------------------------------------------
-- FUNCTION 3 (table-value: retorna uma tabela)
-- Historico de compras e pagamentos de um cliente.
-- Usada na tela de consulta/detalhes do cliente.
-------------------------------------------------------------
create function fc_HistoricoCliente
(	-- parametro recebido
	@clienteId	int
)
returns table
as
	return	(
		select	V.id				as venda_id,
				V.data_venda,
				R.nome				as vendedor,
				(select sum(IV.quantidade) from ItensVenda as IV where IV.venda_id = V.id)	as qtd_produtos,
				V.valor_total,
				(	select string_agg(PV.forma, ' + ') within group (order by PV.id)
					from PagamentosVenda as PV where PV.venda_id = V.id )					as formas_pagamento,
				V.status
		from Vendas as V
			inner join Pessoas as R on R.id = V.vendedor_id
		where V.cliente_id = @clienteId
	)
go

-------------------------------------------------------------
-- FUNCTION 4 (table-value: retorna uma tabela)
-- Desempenho de cada vendedor em um periodo: quantidade de vendas,
-- total vendido e ticket medio. Usada na tela de Relatorios.
-------------------------------------------------------------
create function fc_VendasPorVendedor
(	-- parametros recebidos
	@inicio		datetime,
	@fim		datetime	-- exclusivo
)
returns table
as
	return	(
		select	F.pessoa_id						as vendedor_id,
				P.nome							as vendedor,
				F.cargo,
				count(V.id)						as qtd_vendas,
				isnull(sum(V.valor_total), 0)	as total_vendido,
				cast(isnull(avg(V.valor_total), 0) as decimal(12,2))	as ticket_medio
		from Funcionarios as F
			inner join	Pessoas	as P on P.id = F.pessoa_id
			left join	Vendas	as V on	V.vendedor_id = F.pessoa_id
									and	V.status = 'FINALIZADA'
									and	V.data_venda >= @inicio
									and	V.data_venda <  @fim
		where F.cargo in ('VENDEDOR', 'PROPRIETARIO')
		group by F.pessoa_id, P.nome, F.cargo
	)
go


-------------------------------------------------------------
--                         VIEWS
-------------------------------------------------------------

-------------------------------------------------------------
-- VIEW 1
-- Lista de vendas da tela "Consultar Vendas": cliente, responsavel,
-- quantidade de produtos e formas de pagamento agrupadas.
-------------------------------------------------------------
create view v_VendasDetalhadas
as
	select	V.id									as codigo,
			convert(char(5), V.data_venda, 108)		as hora,
			cast(V.data_venda as date)				as data,
			V.data_venda,
			V.cliente_id,
			C.nome									as cliente,
			V.vendedor_id,
			R.nome									as responsavel,
			F.cargo									as cargo_responsavel,
			isnull(IT.qtd_produtos, 0)				as qtd_produtos,
			V.valor_total,
			isnull(PG.formas_pagamento, '-')		as formas_pagamento,
			V.status,
			V.numero_cupom,
			V.numero_nf
	from Vendas as V
		inner join Pessoas		as C on C.id = V.cliente_id
		inner join Pessoas		as R on R.id = V.vendedor_id
		inner join Funcionarios	as F on F.pessoa_id = V.vendedor_id
		outer apply (	select sum(IV.quantidade) as qtd_produtos
						from ItensVenda as IV where IV.venda_id = V.id ) as IT
		outer apply (	select string_agg(	case PV.forma
												when 'DINHEIRO'			then 'Dinheiro'
												when 'PIX'				then 'PIX'
												when 'CARTAO_DEBITO'	then 'Cartão (Débito)'
												else 'Cartão (Crédito) ' + cast(PV.parcelas as varchar(2)) + 'x'
											end, ' + ') within group (order by PV.id) as formas_pagamento
						from PagamentosVenda as PV where PV.venda_id = V.id ) as PG
go

-------------------------------------------------------------
-- VIEW 2
-- Relatorio de vendas por item (tela "Relatorios"): vendedor,
-- cliente, peca, quantidade, valor e lucro de cada item vendido.
-- O Java filtra por vendedor, peca ou cliente.
-------------------------------------------------------------
create view v_RelatorioVendas
as
	select	V.id									as venda_id,
			V.data_venda,
			R.nome									as vendedor,
			C.nome									as cliente,
			P.codigo								as produto_codigo,
			P.nome									as produto,
			CA.nome									as categoria,
			IV.quantidade,
			IV.preco_unitario,
			IV.desconto,
			IV.subtotal,
			IV.subtotal - IV.quantidade * P.preco_custo	as lucro_estimado,
			V.status
	from ItensVenda as IV
		inner join Vendas		as V	on V.id = IV.venda_id
		inner join Produtos		as P	on P.id = IV.produto_id
		inner join Categorias	as CA	on CA.id = P.categoria_id
		inner join Pessoas		as R	on R.id = V.vendedor_id
		inner join Pessoas		as C	on C.id = V.cliente_id
go

-------------------------------------------------------------
-- VIEW 3
-- Produtos ativos com estoque disponivel menor ou igual ao minimo
-- (card "Produtos com estoque baixo" dos dashboards), com a
-- situacao, quanto falta e de quais fornecedores comprar.
-------------------------------------------------------------
create view v_EstoqueBaixo
as
	select	P.id,
			P.codigo,
			P.nome,
			CA.nome									as categoria,
			P.estoque_atual,
			P.estoque_reservado,
			P.estoque_disponivel,
			P.estoque_minimo,
			P.estoque_minimo - P.estoque_disponivel	as quantidade_faltante,
			case	when P.estoque_disponivel = 0								then 'SEM ESTOQUE'
					when P.estoque_disponivel <= P.estoque_minimo / 2			then 'CRITICO'
					else 'BAIXO' end				as situacao,
			isnull(FO.fornecedores, 'Sem fornecedor cadastrado')	as fornecedores
	from Produtos as P
		inner join Categorias as CA on CA.id = P.categoria_id
		outer apply (	select string_agg(PE.nome, ', ') within group (order by PE.nome) as fornecedores
						from ProdutoFornecedor as PF
							inner join Pessoas as PE on PE.id = PF.fornecedor_id
						where PF.produto_id = P.id ) as FO
	where	P.ativo = 1
		and	P.estoque_disponivel <= P.estoque_minimo
go

-------------------------------------------------------------
-- VIEW 4
-- Historico de movimentacoes de estoque (tela "Estoque"): produto,
-- tipo, quantidade com sinal, responsavel e documento.
-------------------------------------------------------------
create view v_HistoricoMovimentacoes
as
	select	M.id,
			M.data_movimentacao,
			P.codigo								as produto_codigo,
			P.nome									as produto,
			M.tipo,
			M.quantidade,
			case	when M.tipo in ('ENTRADA', 'ESTORNO')	then  M.quantidade
					when M.tipo = 'SAIDA'					then -M.quantidade
					else 0 end						as efeito_estoque,
			R.nome + ' (' + F.cargo + ')'			as responsavel,
			FO.nome									as fornecedor,
			M.venda_id,
			M.documento
	from MovimentacoesEstoque as M
		inner join	Produtos		as P	on P.id = M.produto_id
		inner join	Pessoas			as R	on R.id = M.funcionario_id
		inner join	Funcionarios	as F	on F.pessoa_id = M.funcionario_id
		left join	Pessoas			as FO	on FO.id = M.fornecedor_id
go

-------------------------------------------------------------
-- VIEW 5
-- Numeros dos cards dos dashboards em uma unica linha.
-- Usa a function fc_TotalVendasPeriodo e a view v_EstoqueBaixo.
-------------------------------------------------------------
create view v_DashboardVendas
as
	select	dbo.fc_TotalVendasPeriodo(cast(cast(getdate() as date) as datetime),
									  dateadd(day, 1, cast(cast(getdate() as date) as datetime)))	as vendas_dia,
			dbo.fc_TotalVendasPeriodo(dateadd(day, -6, cast(cast(getdate() as date) as datetime)),
									  dateadd(day, 1, cast(cast(getdate() as date) as datetime)))	as vendas_semana,
			dbo.fc_TotalVendasPeriodo(cast(datefromparts(year(getdate()), month(getdate()), 1) as datetime),
									  dateadd(day, 1, cast(cast(getdate() as date) as datetime)))	as vendas_mes,
			(select count(*) from Vendas where status = 'RESERVADA')								as reservas_abertas,
			(select count(*) from v_EstoqueBaixo)													as produtos_estoque_baixo
go


-------------------------------------------------------------
--                       PROCEDURES
-------------------------------------------------------------

-------------------------------------------------------------
-- PROCEDURE 1
-- Cadastra um funcionario completo em uma unica transacao:
-- Pessoas (dados comuns) + Funcionarios + Usuarios (login).
-- Se qualquer insert falhar, nada e gravado.
-- Chamada pelo Java na tela "Cadastrar Funcionario".
-------------------------------------------------------------
create procedure sp_CadastrarFuncionario
(	-- parametros recebidos
	@nome			varchar(150),
	@email			varchar(150),
	@telefone		varchar(20),
	@rua			varchar(150),
	@numero			varchar(10),
	@bairro			varchar(80),
	@cep			char(9),
	@cidade			varchar(80),
	@uf				char(2),
	@matricula		varchar(15),
	@rg				varchar(15),
	@cpf			char(14),
	@dataNascimento	date,
	@cargo			varchar(15),
	@login			varchar(50),
	@senhaHash		char(60),				-- hash BCrypt gerado pelo Java
	@funcionarioId	int = null	output		-- devolve o id gerado
)
as
begin
	set nocount on;

	begin try
		-- validacoes com mensagens claras para a tela
		if @email is null or @telefone is null
			throw 50010, 'E-mail e celular sao obrigatorios para funcionarios.', 1;
		if exists (select 1 from Funcionarios where cpf = @cpf)
			throw 50011, 'Ja existe um funcionario cadastrado com este CPF.', 1;
		if exists (select 1 from Funcionarios where matricula = @matricula)
			throw 50012, 'Ja existe um funcionario cadastrado com esta matricula.', 1;
		if exists (select 1 from Usuarios where login = @login)
			throw 50013, 'Este login ja esta em uso.', 1;

		begin tran		-- inicio da transacao
			-- 1) dados comuns
			insert into Pessoas (nome, email, telefone, rua, numero, bairro, cep, cidade, uf)
			values (@nome, @email, @telefone, @rua, @numero, @bairro, @cep, @cidade, @uf)

			set @funcionarioId = scope_identity()

			-- 2) dados de funcionario
			insert into Funcionarios (pessoa_id, matricula, rg, cpf, data_nascimento, cargo)
			values (@funcionarioId, @matricula, @rg, @cpf, @dataNascimento, @cargo)

			-- 3) login de acesso
			insert into Usuarios (funcionario_id, login, senha_hash)
			values (@funcionarioId, @login, @senhaHash)
		commit			-- tudo certo, confirmar os dados

		select @funcionarioId as funcionario_id
	end try
	begin catch
		-- deu erro, desfazer a operacao
		if @@trancount > 0
			rollback;
		throw;
	end catch
end
go

-------------------------------------------------------------
-- PROCEDURE 2
-- Registra a entrada de mercadoria (nota fiscal de compra):
--   1) gera a movimentacao de ENTRADA (o trigger soma no estoque);
--   2) atualiza o preco de custo (o trigger grava o historico);
--   3) atualiza o vinculo produto x fornecedor (ultima compra);
--   4) soma a quantidade no local onde foi guardado.
-- Chamada pelo Java na tela de estoque (Estoquista/Gerente/Proprietario).
-------------------------------------------------------------
create procedure sp_EntradaEstoque
(	-- parametros recebidos
	@produtoId		int,
	@quantidade		int,
	@precoCusto		decimal(10,2)	= null,
	@fornecedorId	int				= null,
	@localizacaoId	int				= null,
	@funcionarioId	int,
	@documento		varchar(150)	= null	-- ex.: 'NF-8841 - Bosch'
)
as
begin
	set nocount on;

	begin try
		if not exists (	select 1 from Funcionarios
						where pessoa_id = @funcionarioId and ativo = 1
						  and cargo in ('ESTOQUISTA', 'GERENTE', 'PROPRIETARIO') )
			throw 50020, 'Somente estoquista, gerente ou proprietario ativos podem dar entrada no estoque.', 1;
		if not exists (select 1 from Produtos where id = @produtoId and ativo = 1)
			throw 50021, 'Produto inexistente ou inativo.', 1;
		if @quantidade is null or @quantidade <= 0
			throw 50022, 'A quantidade da entrada deve ser maior que zero.', 1;
		if @fornecedorId is not null and not exists (select 1 from Fornecedores where pessoa_id = @fornecedorId and ativo = 1)
			throw 50023, 'Fornecedor inexistente ou inativo.', 1;

		begin tran
			-- 1) movimentacao de entrada
			insert into MovimentacoesEstoque (produto_id, tipo, quantidade, funcionario_id, fornecedor_id, documento)
			values (@produtoId, 'ENTRADA', @quantidade, @funcionarioId, @fornecedorId, @documento)

			-- 2) novo preco de custo
			if @precoCusto is not null
				update Produtos set preco_custo = @precoCusto
				where id = @produtoId and preco_custo <> @precoCusto

			-- 3) vinculo com o fornecedor
			if @fornecedorId is not null
			begin
				if exists (select 1 from ProdutoFornecedor where produto_id = @produtoId and fornecedor_id = @fornecedorId)
					update ProdutoFornecedor
					set	ultimo_preco_compra	= isnull(@precoCusto, ultimo_preco_compra),
						data_ultima_compra	= getdate()
					where produto_id = @produtoId and fornecedor_id = @fornecedorId
				else
					insert into ProdutoFornecedor (produto_id, fornecedor_id, ultimo_preco_compra, data_ultima_compra)
					values (@produtoId, @fornecedorId, @precoCusto, getdate())
			end

			-- 4) local onde a mercadoria foi guardada
			if @localizacaoId is not null
			begin
				if exists (select 1 from ProdutoLocalizacao where produto_id = @produtoId and localizacao_id = @localizacaoId)
					update ProdutoLocalizacao set quantidade = quantidade + @quantidade
					where produto_id = @produtoId and localizacao_id = @localizacaoId
				else
					insert into ProdutoLocalizacao (produto_id, localizacao_id, quantidade)
					values (@produtoId, @localizacaoId, @quantidade)
			end
		commit

		select id as produto_id, estoque_atual, estoque_disponivel from Produtos where id = @produtoId
	end try
	begin catch
		if @@trancount > 0
			rollback;
		throw;
	end catch
end
go

-------------------------------------------------------------
-- PROCEDURE 3 (auxiliar)
-- Grava os pagamentos de uma venda a partir de uma lista JSON e
-- confere se a soma bate com o total da venda.
-- E chamada DENTRO da transacao de sp_RegistrarVenda e de
-- sp_ConcluirReserva (se der erro, a venda inteira e desfeita).
-- Formato do JSON enviado pelo Java:
--   [ {"forma":"PIX","valor":100.00},
--     {"forma":"DINHEIRO","valor":50.00,"valorRecebido":60.00},
--     {"forma":"CARTAO_CREDITO","valor":200.00,"parcelas":3} ]
-------------------------------------------------------------
create procedure sp_RegistrarPagamentos
(	-- parametros recebidos
	@vendaId		int,
	@pagamentosJson	nvarchar(max)
)
as
begin
	set nocount on;

	if @pagamentosJson is null or isjson(@pagamentosJson) = 0
		throw 50030, 'Lista de pagamentos invalida.', 1;

	declare @Pagamentos table
	(
		forma			varchar(15),
		valor			decimal(12,2),
		valor_recebido	decimal(12,2),
		parcelas		tinyint
	)

	insert into @Pagamentos (forma, valor, valor_recebido, parcelas)
	select	upper(J.forma), J.valor, J.valorRecebido, isnull(J.parcelas, 1)
	from openjson(@pagamentosJson)
		with (	forma			varchar(15)		'$.forma',
				valor			decimal(12,2)	'$.valor',
				valorRecebido	decimal(12,2)	'$.valorRecebido',
				parcelas		tinyint			'$.parcelas'	) as J

	-- validacoes com mensagens claras para a tela
	if not exists (select 1 from @Pagamentos)
		throw 50032, 'Informe pelo menos uma forma de pagamento.', 1;
	if exists (select 1 from @Pagamentos where forma is null or forma not in ('DINHEIRO', 'PIX', 'CARTAO_DEBITO', 'CARTAO_CREDITO'))
		throw 50033, 'Forma de pagamento invalida. Use DINHEIRO, PIX, CARTAO_DEBITO ou CARTAO_CREDITO.', 1;
	if exists (select 1 from @Pagamentos where valor is null or valor <= 0)
		throw 50034, 'O valor de cada pagamento deve ser maior que zero.', 1;
	if exists (select 1 from @Pagamentos where valor_recebido is not null and (forma <> 'DINHEIRO' or valor_recebido < valor))
		throw 50035, 'O valor recebido em dinheiro nao pode ser menor que o valor pago.', 1;
	if exists (select 1 from @Pagamentos where parcelas not between 1 and 12 or (forma <> 'CARTAO_CREDITO' and parcelas > 1))
		throw 50036, 'Parcelamento so no cartao de credito, de 1 a 12 vezes.', 1;

	insert into PagamentosVenda (venda_id, forma, valor, valor_recebido, parcelas)
	select @vendaId, forma, valor, valor_recebido, parcelas
	from @Pagamentos

	declare @total	decimal(12,2) = (select valor_total from Vendas where id = @vendaId)
	declare @pago	decimal(12,2) = (select isnull(sum(valor), 0) from PagamentosVenda where venda_id = @vendaId)

	if @pago <> @total
	begin
		declare @mensagem nvarchar(300) = concat(	N'A soma dos pagamentos (R$ ', format(@pago, 'N2', 'pt-BR'),
													N') e diferente do total da venda (R$ ', format(@total, 'N2', 'pt-BR'), N').');
		throw 50031, @mensagem, 1;
	end
end
go

-------------------------------------------------------------
-- PROCEDURE 4
-- Registra uma venda completa em uma unica transacao:
--   1) valida cliente, vendedor e itens;
--   2) grava o cabecalho da venda;
--   3) grava os itens com o preco do cadastro do produto
--      (o trigger baixa/reserva o estoque e calcula o total);
--   4) venda normal: grava os pagamentos e finaliza a venda;
--      reserva: fica com status RESERVADA, sem pagamento.
-- Se faltar estoque ou o pagamento nao bater, nada e gravado.
-- Chamada pelo Java na tela "Nova Venda" (Vendedor/Proprietario).
-- Formato do JSON de itens:
--   [ {"produtoId":1,"quantidade":2},
--     {"produtoId":5,"quantidade":1,"desconto":10.00,"observacao":"Cliente antigo"} ]
-------------------------------------------------------------
create procedure sp_RegistrarVenda
(	-- parametros recebidos
	@clienteId		int,
	@vendedorId		int,
	@itensJson		nvarchar(max),
	@pagamentosJson	nvarchar(max)	= null,	-- obrigatorio quando nao for reserva
	@reserva		bit				= 0,
	@numeroCupom	varchar(20)		= null,
	@numeroNf		varchar(20)		= null,
	@vendaId		int				= null	output	-- devolve o id gerado
)
as
begin
	set nocount on;

	begin try
		-- 1) validacoes
		if not exists (select 1 from Clientes where pessoa_id = @clienteId)
			throw 50040, 'Cliente nao encontrado.', 1;
		if not exists (	select 1 from Funcionarios
						where pessoa_id = @vendedorId and ativo = 1
						  and cargo in ('VENDEDOR', 'PROPRIETARIO') )
			throw 50041, 'Somente vendedores ou o proprietario ativos podem registrar vendas.', 1;
		if @itensJson is null or isjson(@itensJson) = 0
			throw 50042, 'Lista de itens invalida.', 1;

		declare @Itens table
		(
			produto_id	int,
			quantidade	int,
			desconto	decimal(10,2),
			observacao	varchar(150)
		)

		insert into @Itens (produto_id, quantidade, desconto, observacao)
		select produtoId, quantidade, isnull(desconto, 0), observacao
		from openjson(@itensJson)
			with (	produtoId	int,
					quantidade	int,
					desconto	decimal(10,2),
					observacao	varchar(150)	)

		if not exists (select 1 from @Itens)
			throw 50043, 'A venda precisa ter pelo menos um item.', 1;
		if exists (select produto_id from @Itens group by produto_id having count(*) > 1)
			throw 50044, 'O mesmo produto aparece mais de uma vez na lista de itens.', 1;
		if exists (	select 1 from @Itens as I left join Produtos as P on P.id = I.produto_id and P.ativo = 1
					where P.id is null )
			throw 50045, 'A lista de itens tem produto inexistente ou inativo.', 1;
		if exists (select 1 from @Itens where quantidade is null or quantidade <= 0)
			throw 50046, 'A quantidade de cada item deve ser maior que zero.', 1;
		if @reserva = 0 and @pagamentosJson is null
			throw 50047, 'Informe os pagamentos da venda.', 1;

		begin tran
			-- 2) cabecalho
			insert into Vendas (cliente_id, vendedor_id, numero_cupom, numero_nf, status)
			values (@clienteId, @vendedorId, @numeroCupom, @numeroNf,
					case when @reserva = 1 then 'RESERVADA' else 'ABERTA' end)

			set @vendaId = scope_identity()

			-- 3) itens (o preco vem do cadastro, nao do valor enviado pela tela)
			insert into ItensVenda (venda_id, produto_id, quantidade, preco_unitario, desconto, observacao)
			select @vendaId, I.produto_id, I.quantidade, P.preco_venda, I.desconto, I.observacao
			from @Itens as I inner join Produtos as P on P.id = I.produto_id
			-- aqui o trigger tg_ItensVenda_GerarMovimentacao baixa (ou reserva) o estoque

			-- 4) pagamento e finalizacao
			if @reserva = 0
			begin
				exec sp_RegistrarPagamentos @vendaId, @pagamentosJson

				update Vendas set status = 'FINALIZADA' where id = @vendaId
			end
		commit

		select id as venda_id, valor_total, status from Vendas where id = @vendaId
	end try
	begin catch
		if @@trancount > 0
			rollback;
		throw;
	end catch
end
go

-------------------------------------------------------------
-- PROCEDURE 5
-- Conclui uma venda RESERVADA: libera a reserva, da a saida
-- definitiva do estoque, grava os pagamentos e finaliza a venda.
-- Chamada pelo Java na tela de vendas (botao "Concluir reserva").
-------------------------------------------------------------
create procedure sp_ConcluirReserva
(	-- parametros recebidos
	@vendaId		int,
	@pagamentosJson	nvarchar(max),
	@funcionarioId	int
)
as
begin
	set nocount on;

	begin try
		if not exists (select 1 from Vendas where id = @vendaId and status = 'RESERVADA')
			throw 50050, 'Venda nao encontrada ou nao esta reservada.', 1;
		if not exists (	select 1 from Funcionarios
						where pessoa_id = @funcionarioId and ativo = 1
						  and cargo in ('VENDEDOR', 'PROPRIETARIO') )
			throw 50051, 'Somente vendedores ou o proprietario ativos podem concluir reservas.', 1;

		begin tran
			-- libera a reserva e da a saida (as duas movimentacoes de cada item juntas)
			insert into MovimentacoesEstoque (produto_id, tipo, quantidade, funcionario_id, venda_id, documento)
			select	IV.produto_id, T.tipo, IV.quantidade, @funcionarioId, @vendaId,
					'Conclusao da reserva #' + right('0000' + cast(@vendaId as varchar(10)), 4)
			from ItensVenda as IV
				cross join (values ('LIBERACAO'), ('SAIDA')) as T(tipo)
			where IV.venda_id = @vendaId

			exec sp_RegistrarPagamentos @vendaId, @pagamentosJson

			update Vendas set status = 'FINALIZADA', data_venda = getdate() where id = @vendaId
		commit

		select id as venda_id, valor_total, status from Vendas where id = @vendaId
	end try
	begin catch
		if @@trancount > 0
			rollback;
		throw;
	end catch
end
go

-------------------------------------------------------------
-- PROCEDURE 6
-- Cancela uma venda FINALIZADA (devolve os itens ao estoque) ou
-- RESERVADA (libera a reserva). So gerente ou proprietario.
-- Os pagamentos ficam registrados como historico.
-- Chamada pelo Java na tela de detalhes da venda.
-------------------------------------------------------------
create procedure sp_CancelarVenda
(	-- parametros recebidos
	@vendaId		int,
	@funcionarioId	int,
	@motivo			varchar(200)
)
as
begin
	set nocount on;

	begin try
		declare @status varchar(12) = (select status from Vendas where id = @vendaId)

		if @status is null
			throw 50070, 'Venda nao encontrada.', 1;
		if @status not in ('FINALIZADA', 'RESERVADA')
			throw 50071, 'So e possivel cancelar vendas finalizadas ou reservadas.', 1;
		if not exists (	select 1 from Funcionarios
						where pessoa_id = @funcionarioId and ativo = 1
						  and cargo in ('GERENTE', 'PROPRIETARIO') )
			throw 50072, 'Somente gerente ou proprietario podem cancelar vendas.', 1;
		if @motivo is null or len(ltrim(@motivo)) < 5
			throw 50073, 'Informe o motivo do cancelamento (minimo 5 caracteres).', 1;

		begin tran
			-- devolve ao estoque (ESTORNO) ou libera a reserva (LIBERACAO)
			insert into MovimentacoesEstoque (produto_id, tipo, quantidade, funcionario_id, venda_id, documento)
			select	IV.produto_id,
					case when @status = 'RESERVADA' then 'LIBERACAO' else 'ESTORNO' end,
					IV.quantidade, @funcionarioId, @vendaId,
					'Cancelamento da venda #' + right('0000' + cast(@vendaId as varchar(10)), 4)
			from ItensVenda as IV
			where IV.venda_id = @vendaId

			update Vendas
			set	status				= 'CANCELADA',
				data_cancelamento	= getdate(),
				motivo_cancelamento	= @motivo
			where id = @vendaId
		commit

		select id as venda_id, status, data_cancelamento from Vendas where id = @vendaId
	end try
	begin catch
		if @@trancount > 0
			rollback;
		throw;
	end catch
end
go

-------------------------------------------------------------
-- PROCEDURE 7 - BACKUP (ponto extra)
-- Gera um backup completo do banco. Se a pasta nao for informada,
-- usa a pasta padrao de backup do SQL Server.
-- Chamada pelo Java na tela "Configuracoes" (somente Proprietario).
-- Obs.: o servico do SQL Server precisa ter permissao de escrita na pasta.
-------------------------------------------------------------
create procedure sp_BackupBanco
(	-- parametro recebido
	@pasta	nvarchar(260) = null
)
as
begin
	set nocount on;

	if @pasta is null
		set @pasta = cast(serverproperty('InstanceDefaultBackupPath') as nvarchar(260))

	declare @arquivo nvarchar(400) = @pasta + N'\ToolSync_' + format(getdate(), 'yyyyMMdd_HHmmss') + N'.bak'

	backup database ToolSync
	to disk = @arquivo
	with init, checksum, name = N'ToolSync - backup completo'

	select @arquivo as arquivo_backup
end
go

-------------------------------------------------------------
-- RESTORE (ponto extra)
-- O restore nao pode rodar conectado no proprio ToolSync, por isso
-- o Java abre uma conexao no banco master e executa:
--
--   alter database ToolSync set single_user with rollback immediate;
--   restore database ToolSync from disk = '<arquivo .bak>' with replace;
--   alter database ToolSync set multi_user;
-------------------------------------------------------------


-------------------------------------------------------------
-- DICIONARIO DE DADOS NO PROPRIO BANCO (MS_Description)
-- As descricoes aparecem no SSMS (propriedades da tabela/coluna)
-- e podem ser consultadas em sys.extended_properties.
-------------------------------------------------------------
exec sp_addextendedproperty 'MS_Description', N'Tabela mãe da herança. Guarda nome, contato e endereço comuns a clientes, funcionários e fornecedores.', 'SCHEMA', 'dbo', 'TABLE', 'Pessoas'
exec sp_addextendedproperty 'MS_Description', N'Identificador da pessoa (gerado automaticamente)', 'SCHEMA', 'dbo', 'TABLE', 'Pessoas', 'COLUMN', 'id'
exec sp_addextendedproperty 'MS_Description', N'Nome completo ou razão social', 'SCHEMA', 'dbo', 'TABLE', 'Pessoas', 'COLUMN', 'nome'
exec sp_addextendedproperty 'MS_Description', N'E-mail de contato', 'SCHEMA', 'dbo', 'TABLE', 'Pessoas', 'COLUMN', 'email'
exec sp_addextendedproperty 'MS_Description', N'Telefone ou celular no formato (00) 0000-0000 ou (00) 00000-0000', 'SCHEMA', 'dbo', 'TABLE', 'Pessoas', 'COLUMN', 'telefone'
exec sp_addextendedproperty 'MS_Description', N'Rua, avenida ou rodovia', 'SCHEMA', 'dbo', 'TABLE', 'Pessoas', 'COLUMN', 'rua'
exec sp_addextendedproperty 'MS_Description', N'Número do endereço', 'SCHEMA', 'dbo', 'TABLE', 'Pessoas', 'COLUMN', 'numero'
exec sp_addextendedproperty 'MS_Description', N'Bairro', 'SCHEMA', 'dbo', 'TABLE', 'Pessoas', 'COLUMN', 'bairro'
exec sp_addextendedproperty 'MS_Description', N'CEP no formato 00000-000', 'SCHEMA', 'dbo', 'TABLE', 'Pessoas', 'COLUMN', 'cep'
exec sp_addextendedproperty 'MS_Description', N'Cidade', 'SCHEMA', 'dbo', 'TABLE', 'Pessoas', 'COLUMN', 'cidade'
exec sp_addextendedproperty 'MS_Description', N'Sigla do estado', 'SCHEMA', 'dbo', 'TABLE', 'Pessoas', 'COLUMN', 'uf'
exec sp_addextendedproperty 'MS_Description', N'Data e hora do cadastro', 'SCHEMA', 'dbo', 'TABLE', 'Pessoas', 'COLUMN', 'data_cadastro'
go
exec sp_addextendedproperty 'MS_Description', N'Clientes da loja (pessoa física ou jurídica). Herda de Pessoas.', 'SCHEMA', 'dbo', 'TABLE', 'Clientes'
exec sp_addextendedproperty 'MS_Description', N'Identificador do cliente (o mesmo id da Pessoa)', 'SCHEMA', 'dbo', 'TABLE', 'Clientes', 'COLUMN', 'pessoa_id'
exec sp_addextendedproperty 'MS_Description', N'PF = pessoa física, PJ = pessoa jurídica', 'SCHEMA', 'dbo', 'TABLE', 'Clientes', 'COLUMN', 'tipo_pessoa'
exec sp_addextendedproperty 'MS_Description', N'CPF (PF) ou CNPJ (PJ), com pontuação', 'SCHEMA', 'dbo', 'TABLE', 'Clientes', 'COLUMN', 'documento'
go
exec sp_addextendedproperty 'MS_Description', N'Funcionários da loja. O cargo define o perfil de acesso ao sistema. Herda de Pessoas.', 'SCHEMA', 'dbo', 'TABLE', 'Funcionarios'
exec sp_addextendedproperty 'MS_Description', N'Identificador do funcionário (o mesmo id da Pessoa)', 'SCHEMA', 'dbo', 'TABLE', 'Funcionarios', 'COLUMN', 'pessoa_id'
exec sp_addextendedproperty 'MS_Description', N'Matrícula interna (ex.: MAT-202501)', 'SCHEMA', 'dbo', 'TABLE', 'Funcionarios', 'COLUMN', 'matricula'
exec sp_addextendedproperty 'MS_Description', N'RG do funcionário', 'SCHEMA', 'dbo', 'TABLE', 'Funcionarios', 'COLUMN', 'rg'
exec sp_addextendedproperty 'MS_Description', N'CPF no formato 000.000.000-00', 'SCHEMA', 'dbo', 'TABLE', 'Funcionarios', 'COLUMN', 'cpf'
exec sp_addextendedproperty 'MS_Description', N'Data de nascimento (mínimo de 16 anos)', 'SCHEMA', 'dbo', 'TABLE', 'Funcionarios', 'COLUMN', 'data_nascimento'
exec sp_addextendedproperty 'MS_Description', N'Cargo e perfil de acesso: PROPRIETARIO, GERENTE, VENDEDOR ou ESTOQUISTA', 'SCHEMA', 'dbo', 'TABLE', 'Funcionarios', 'COLUMN', 'cargo'
exec sp_addextendedproperty 'MS_Description', N'Data de admissão', 'SCHEMA', 'dbo', 'TABLE', 'Funcionarios', 'COLUMN', 'data_admissao'
exec sp_addextendedproperty 'MS_Description', N'1 = trabalha na loja, 0 = desligado', 'SCHEMA', 'dbo', 'TABLE', 'Funcionarios', 'COLUMN', 'ativo'
go
exec sp_addextendedproperty 'MS_Description', N'Login de acesso ao sistema (1 x 1 com Funcionarios). Guarda só o hash BCrypt da senha.', 'SCHEMA', 'dbo', 'TABLE', 'Usuarios'
exec sp_addextendedproperty 'MS_Description', N'Funcionário dono do login', 'SCHEMA', 'dbo', 'TABLE', 'Usuarios', 'COLUMN', 'funcionario_id'
exec sp_addextendedproperty 'MS_Description', N'Nome de usuário para entrar no sistema', 'SCHEMA', 'dbo', 'TABLE', 'Usuarios', 'COLUMN', 'login'
exec sp_addextendedproperty 'MS_Description', N'Hash BCrypt da senha (60 caracteres); a senha pura nunca é gravada', 'SCHEMA', 'dbo', 'TABLE', 'Usuarios', 'COLUMN', 'senha_hash'
exec sp_addextendedproperty 'MS_Description', N'1 = pode entrar no sistema; segue o campo ativo do funcionário', 'SCHEMA', 'dbo', 'TABLE', 'Usuarios', 'COLUMN', 'ativo'
exec sp_addextendedproperty 'MS_Description', N'Data e hora do último login', 'SCHEMA', 'dbo', 'TABLE', 'Usuarios', 'COLUMN', 'ultimo_acesso'
go
exec sp_addextendedproperty 'MS_Description', N'Empresas que vendem mercadoria para a loja. Herda de Pessoas (nome = razão social).', 'SCHEMA', 'dbo', 'TABLE', 'Fornecedores'
exec sp_addextendedproperty 'MS_Description', N'Identificador do fornecedor (o mesmo id da Pessoa)', 'SCHEMA', 'dbo', 'TABLE', 'Fornecedores', 'COLUMN', 'pessoa_id'
exec sp_addextendedproperty 'MS_Description', N'CNPJ no formato 00.000.000/0000-00', 'SCHEMA', 'dbo', 'TABLE', 'Fornecedores', 'COLUMN', 'cnpj'
exec sp_addextendedproperty 'MS_Description', N'Tipo de produto fornecido', 'SCHEMA', 'dbo', 'TABLE', 'Fornecedores', 'COLUMN', 'ramo'
exec sp_addextendedproperty 'MS_Description', N'Simples Nacional, Lucro Presumido ou Lucro Real', 'SCHEMA', 'dbo', 'TABLE', 'Fornecedores', 'COLUMN', 'regime_tributario'
exec sp_addextendedproperty 'MS_Description', N'Contato comercial no fornecedor', 'SCHEMA', 'dbo', 'TABLE', 'Fornecedores', 'COLUMN', 'representante'
exec sp_addextendedproperty 'MS_Description', N'Banco, agência e conta ou chave PIX', 'SCHEMA', 'dbo', 'TABLE', 'Fornecedores', 'COLUMN', 'dados_bancarios'
exec sp_addextendedproperty 'MS_Description', N'1 = fornecedor ativo', 'SCHEMA', 'dbo', 'TABLE', 'Fornecedores', 'COLUMN', 'ativo'
go
exec sp_addextendedproperty 'MS_Description', N'Categorias de produtos (Elétricas, Manuais, EPIs...).', 'SCHEMA', 'dbo', 'TABLE', 'Categorias'
exec sp_addextendedproperty 'MS_Description', N'Identificador da categoria', 'SCHEMA', 'dbo', 'TABLE', 'Categorias', 'COLUMN', 'id'
exec sp_addextendedproperty 'MS_Description', N'Nome da categoria', 'SCHEMA', 'dbo', 'TABLE', 'Categorias', 'COLUMN', 'nome'
exec sp_addextendedproperty 'MS_Description', N'Descrição da categoria', 'SCHEMA', 'dbo', 'TABLE', 'Categorias', 'COLUMN', 'descricao'
go
exec sp_addextendedproperty 'MS_Description', N'Marcas dos produtos.', 'SCHEMA', 'dbo', 'TABLE', 'Marcas'
exec sp_addextendedproperty 'MS_Description', N'Identificador da marca', 'SCHEMA', 'dbo', 'TABLE', 'Marcas', 'COLUMN', 'id'
exec sp_addextendedproperty 'MS_Description', N'Nome da marca', 'SCHEMA', 'dbo', 'TABLE', 'Marcas', 'COLUMN', 'nome'
go
exec sp_addextendedproperty 'MS_Description', N'Ferramentas e acessórios vendidos, com preços, dimensões e saldo de estoque.', 'SCHEMA', 'dbo', 'TABLE', 'Produtos'
exec sp_addextendedproperty 'MS_Description', N'Identificador do produto', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'id'
exec sp_addextendedproperty 'MS_Description', N'Código do produto usado nas telas (ex.: 0001)', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'codigo'
exec sp_addextendedproperty 'MS_Description', N'Nome do produto', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'nome'
exec sp_addextendedproperty 'MS_Description', N'Descrição detalhada', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'descricao'
exec sp_addextendedproperty 'MS_Description', N'Categoria do produto', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'categoria_id'
exec sp_addextendedproperty 'MS_Description', N'Marca do produto', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'marca_id'
exec sp_addextendedproperty 'MS_Description', N'Código de barras EAN', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'ean'
exec sp_addextendedproperty 'MS_Description', N'Código SKU interno', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'sku'
exec sp_addextendedproperty 'MS_Description', N'Classificação fiscal NCM', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'ncm'
exec sp_addextendedproperty 'MS_Description', N'Preço de custo (atualizado nas entradas de estoque)', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'preco_custo'
exec sp_addextendedproperty 'MS_Description', N'Preço de venda', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'preco_venda'
exec sp_addextendedproperty 'MS_Description', N'Unidade de compra (UN, CX...)', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'unidade_compra'
exec sp_addextendedproperty 'MS_Description', N'Unidade de venda (UN, JG, PR...)', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'unidade_venda'
exec sp_addextendedproperty 'MS_Description', N'Largura em centímetros', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'largura_cm'
exec sp_addextendedproperty 'MS_Description', N'Altura em centímetros', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'altura_cm'
exec sp_addextendedproperty 'MS_Description', N'Comprimento em centímetros', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'comprimento_cm'
exec sp_addextendedproperty 'MS_Description', N'Caminho da imagem do produto', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'imagem_url'
exec sp_addextendedproperty 'MS_Description', N'Unidades em estoque (alterado só pelo trigger de movimentações)', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'estoque_atual'
exec sp_addextendedproperty 'MS_Description', N'Unidades reservadas para clientes (alterado só pelo trigger)', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'estoque_reservado'
exec sp_addextendedproperty 'MS_Description', N'Quantidade mínima antes do alerta de estoque baixo', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'estoque_minimo'
exec sp_addextendedproperty 'MS_Description', N'Estoque atual menos o reservado', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'estoque_disponivel'
exec sp_addextendedproperty 'MS_Description', N'1 = produto à venda', 'SCHEMA', 'dbo', 'TABLE', 'Produtos', 'COLUMN', 'ativo'
go
exec sp_addextendedproperty 'MS_Description', N'Relacionamento N x N entre Produtos e Fornecedores, com os dados da última compra.', 'SCHEMA', 'dbo', 'TABLE', 'ProdutoFornecedor'
exec sp_addextendedproperty 'MS_Description', N'Produto fornecido', 'SCHEMA', 'dbo', 'TABLE', 'ProdutoFornecedor', 'COLUMN', 'produto_id'
exec sp_addextendedproperty 'MS_Description', N'Fornecedor do produto', 'SCHEMA', 'dbo', 'TABLE', 'ProdutoFornecedor', 'COLUMN', 'fornecedor_id'
exec sp_addextendedproperty 'MS_Description', N'Código do produto no catálogo do fornecedor', 'SCHEMA', 'dbo', 'TABLE', 'ProdutoFornecedor', 'COLUMN', 'codigo_no_fornecedor'
exec sp_addextendedproperty 'MS_Description', N'Preço pago na última compra', 'SCHEMA', 'dbo', 'TABLE', 'ProdutoFornecedor', 'COLUMN', 'ultimo_preco_compra'
exec sp_addextendedproperty 'MS_Description', N'Data da última compra', 'SCHEMA', 'dbo', 'TABLE', 'ProdutoFornecedor', 'COLUMN', 'data_ultima_compra'
go
exec sp_addextendedproperty 'MS_Description', N'Endereços físicos do estoque (galpão, corredor, prateleira e box).', 'SCHEMA', 'dbo', 'TABLE', 'Localizacoes'
exec sp_addextendedproperty 'MS_Description', N'Identificador do local', 'SCHEMA', 'dbo', 'TABLE', 'Localizacoes', 'COLUMN', 'id'
exec sp_addextendedproperty 'MS_Description', N'Galpão ou área', 'SCHEMA', 'dbo', 'TABLE', 'Localizacoes', 'COLUMN', 'galpao'
exec sp_addextendedproperty 'MS_Description', N'Corredor', 'SCHEMA', 'dbo', 'TABLE', 'Localizacoes', 'COLUMN', 'corredor'
exec sp_addextendedproperty 'MS_Description', N'Prateleira', 'SCHEMA', 'dbo', 'TABLE', 'Localizacoes', 'COLUMN', 'prateleira'
exec sp_addextendedproperty 'MS_Description', N'Box ou posição na prateleira', 'SCHEMA', 'dbo', 'TABLE', 'Localizacoes', 'COLUMN', 'box'
go
exec sp_addextendedproperty 'MS_Description', N'Relacionamento N x N entre Produtos e Localizacoes, com a quantidade guardada em cada local.', 'SCHEMA', 'dbo', 'TABLE', 'ProdutoLocalizacao'
exec sp_addextendedproperty 'MS_Description', N'Produto guardado', 'SCHEMA', 'dbo', 'TABLE', 'ProdutoLocalizacao', 'COLUMN', 'produto_id'
exec sp_addextendedproperty 'MS_Description', N'Local onde está guardado', 'SCHEMA', 'dbo', 'TABLE', 'ProdutoLocalizacao', 'COLUMN', 'localizacao_id'
exec sp_addextendedproperty 'MS_Description', N'Quantidade guardada neste local', 'SCHEMA', 'dbo', 'TABLE', 'ProdutoLocalizacao', 'COLUMN', 'quantidade'
go
exec sp_addextendedproperty 'MS_Description', N'Cabeçalho de cada venda: cliente, vendedor, total e situação.', 'SCHEMA', 'dbo', 'TABLE', 'Vendas'
exec sp_addextendedproperty 'MS_Description', N'Número da venda', 'SCHEMA', 'dbo', 'TABLE', 'Vendas', 'COLUMN', 'id'
exec sp_addextendedproperty 'MS_Description', N'Data e hora da venda', 'SCHEMA', 'dbo', 'TABLE', 'Vendas', 'COLUMN', 'data_venda'
exec sp_addextendedproperty 'MS_Description', N'Cliente que comprou', 'SCHEMA', 'dbo', 'TABLE', 'Vendas', 'COLUMN', 'cliente_id'
exec sp_addextendedproperty 'MS_Description', N'Funcionário que fez a venda (vendedor ou proprietário)', 'SCHEMA', 'dbo', 'TABLE', 'Vendas', 'COLUMN', 'vendedor_id'
exec sp_addextendedproperty 'MS_Description', N'Número do cupom fiscal', 'SCHEMA', 'dbo', 'TABLE', 'Vendas', 'COLUMN', 'numero_cupom'
exec sp_addextendedproperty 'MS_Description', N'Número da nota fiscal', 'SCHEMA', 'dbo', 'TABLE', 'Vendas', 'COLUMN', 'numero_nf'
exec sp_addextendedproperty 'MS_Description', N'Soma dos subtotais dos itens (calculada pelo trigger)', 'SCHEMA', 'dbo', 'TABLE', 'Vendas', 'COLUMN', 'valor_total'
exec sp_addextendedproperty 'MS_Description', N'ABERTA, RESERVADA, FINALIZADA ou CANCELADA', 'SCHEMA', 'dbo', 'TABLE', 'Vendas', 'COLUMN', 'status'
exec sp_addextendedproperty 'MS_Description', N'Data do cancelamento (só em venda cancelada)', 'SCHEMA', 'dbo', 'TABLE', 'Vendas', 'COLUMN', 'data_cancelamento'
exec sp_addextendedproperty 'MS_Description', N'Motivo informado no cancelamento', 'SCHEMA', 'dbo', 'TABLE', 'Vendas', 'COLUMN', 'motivo_cancelamento'
go
exec sp_addextendedproperty 'MS_Description', N'Relacionamento N x N entre Vendas e Produtos: os itens de cada venda.', 'SCHEMA', 'dbo', 'TABLE', 'ItensVenda'
exec sp_addextendedproperty 'MS_Description', N'Identificador do item', 'SCHEMA', 'dbo', 'TABLE', 'ItensVenda', 'COLUMN', 'id'
exec sp_addextendedproperty 'MS_Description', N'Venda à qual o item pertence', 'SCHEMA', 'dbo', 'TABLE', 'ItensVenda', 'COLUMN', 'venda_id'
exec sp_addextendedproperty 'MS_Description', N'Produto vendido', 'SCHEMA', 'dbo', 'TABLE', 'ItensVenda', 'COLUMN', 'produto_id'
exec sp_addextendedproperty 'MS_Description', N'Quantidade vendida', 'SCHEMA', 'dbo', 'TABLE', 'ItensVenda', 'COLUMN', 'quantidade'
exec sp_addextendedproperty 'MS_Description', N'Preço do produto no momento da venda', 'SCHEMA', 'dbo', 'TABLE', 'ItensVenda', 'COLUMN', 'preco_unitario'
exec sp_addextendedproperty 'MS_Description', N'Desconto em reais no item', 'SCHEMA', 'dbo', 'TABLE', 'ItensVenda', 'COLUMN', 'desconto'
exec sp_addextendedproperty 'MS_Description', N'Observação do item', 'SCHEMA', 'dbo', 'TABLE', 'ItensVenda', 'COLUMN', 'observacao'
exec sp_addextendedproperty 'MS_Description', N'quantidade x preco_unitario - desconto', 'SCHEMA', 'dbo', 'TABLE', 'ItensVenda', 'COLUMN', 'subtotal'
go
exec sp_addextendedproperty 'MS_Description', N'Pagamentos de cada venda. Uma venda pode ter várias formas de pagamento.', 'SCHEMA', 'dbo', 'TABLE', 'PagamentosVenda'
exec sp_addextendedproperty 'MS_Description', N'Identificador do pagamento', 'SCHEMA', 'dbo', 'TABLE', 'PagamentosVenda', 'COLUMN', 'id'
exec sp_addextendedproperty 'MS_Description', N'Venda paga', 'SCHEMA', 'dbo', 'TABLE', 'PagamentosVenda', 'COLUMN', 'venda_id'
exec sp_addextendedproperty 'MS_Description', N'DINHEIRO, PIX, CARTAO_DEBITO ou CARTAO_CREDITO', 'SCHEMA', 'dbo', 'TABLE', 'PagamentosVenda', 'COLUMN', 'forma'
exec sp_addextendedproperty 'MS_Description', N'Valor pago nesta forma', 'SCHEMA', 'dbo', 'TABLE', 'PagamentosVenda', 'COLUMN', 'valor'
exec sp_addextendedproperty 'MS_Description', N'Valor entregue pelo cliente (só dinheiro)', 'SCHEMA', 'dbo', 'TABLE', 'PagamentosVenda', 'COLUMN', 'valor_recebido'
exec sp_addextendedproperty 'MS_Description', N'valor_recebido - valor', 'SCHEMA', 'dbo', 'TABLE', 'PagamentosVenda', 'COLUMN', 'troco'
exec sp_addextendedproperty 'MS_Description', N'Número de parcelas (só cartão de crédito)', 'SCHEMA', 'dbo', 'TABLE', 'PagamentosVenda', 'COLUMN', 'parcelas'
exec sp_addextendedproperty 'MS_Description', N'Data e hora do pagamento', 'SCHEMA', 'dbo', 'TABLE', 'PagamentosVenda', 'COLUMN', 'data_pagamento'
go
exec sp_addextendedproperty 'MS_Description', N'Toda entrada, saída, estorno, reserva ou liberação de estoque. O trigger atualiza o saldo do produto.', 'SCHEMA', 'dbo', 'TABLE', 'MovimentacoesEstoque'
exec sp_addextendedproperty 'MS_Description', N'Identificador da movimentação', 'SCHEMA', 'dbo', 'TABLE', 'MovimentacoesEstoque', 'COLUMN', 'id'
exec sp_addextendedproperty 'MS_Description', N'Produto movimentado', 'SCHEMA', 'dbo', 'TABLE', 'MovimentacoesEstoque', 'COLUMN', 'produto_id'
exec sp_addextendedproperty 'MS_Description', N'ENTRADA, SAIDA, ESTORNO, RESERVA ou LIBERACAO', 'SCHEMA', 'dbo', 'TABLE', 'MovimentacoesEstoque', 'COLUMN', 'tipo'
exec sp_addextendedproperty 'MS_Description', N'Quantidade movimentada', 'SCHEMA', 'dbo', 'TABLE', 'MovimentacoesEstoque', 'COLUMN', 'quantidade'
exec sp_addextendedproperty 'MS_Description', N'Data e hora da movimentação', 'SCHEMA', 'dbo', 'TABLE', 'MovimentacoesEstoque', 'COLUMN', 'data_movimentacao'
exec sp_addextendedproperty 'MS_Description', N'Funcionário responsável', 'SCHEMA', 'dbo', 'TABLE', 'MovimentacoesEstoque', 'COLUMN', 'funcionario_id'
exec sp_addextendedproperty 'MS_Description', N'Venda que gerou a movimentação (quando houver)', 'SCHEMA', 'dbo', 'TABLE', 'MovimentacoesEstoque', 'COLUMN', 'venda_id'
exec sp_addextendedproperty 'MS_Description', N'Fornecedor da entrada (quando houver)', 'SCHEMA', 'dbo', 'TABLE', 'MovimentacoesEstoque', 'COLUMN', 'fornecedor_id'
exec sp_addextendedproperty 'MS_Description', N'Nota fiscal ou observação', 'SCHEMA', 'dbo', 'TABLE', 'MovimentacoesEstoque', 'COLUMN', 'documento'
go
exec sp_addextendedproperty 'MS_Description', N'Histórico de alterações de preço dos produtos, gravado pelo trigger de Produtos.', 'SCHEMA', 'dbo', 'TABLE', 'HistoricoPrecos'
exec sp_addextendedproperty 'MS_Description', N'Identificador do registro', 'SCHEMA', 'dbo', 'TABLE', 'HistoricoPrecos', 'COLUMN', 'id'
exec sp_addextendedproperty 'MS_Description', N'Produto que teve o preço alterado', 'SCHEMA', 'dbo', 'TABLE', 'HistoricoPrecos', 'COLUMN', 'produto_id'
exec sp_addextendedproperty 'MS_Description', N'Preço de custo antes da alteração', 'SCHEMA', 'dbo', 'TABLE', 'HistoricoPrecos', 'COLUMN', 'preco_custo_anterior'
exec sp_addextendedproperty 'MS_Description', N'Preço de custo depois da alteração', 'SCHEMA', 'dbo', 'TABLE', 'HistoricoPrecos', 'COLUMN', 'preco_custo_novo'
exec sp_addextendedproperty 'MS_Description', N'Preço de venda antes da alteração', 'SCHEMA', 'dbo', 'TABLE', 'HistoricoPrecos', 'COLUMN', 'preco_venda_anterior'
exec sp_addextendedproperty 'MS_Description', N'Preço de venda depois da alteração', 'SCHEMA', 'dbo', 'TABLE', 'HistoricoPrecos', 'COLUMN', 'preco_venda_novo'
exec sp_addextendedproperty 'MS_Description', N'Data e hora da alteração', 'SCHEMA', 'dbo', 'TABLE', 'HistoricoPrecos', 'COLUMN', 'data_alteracao'
exec sp_addextendedproperty 'MS_Description', N'Usuário do banco que fez a alteração', 'SCHEMA', 'dbo', 'TABLE', 'HistoricoPrecos', 'COLUMN', 'usuario_banco'
go


-------------------------------------------------------------
--                   INJECAO DE DADOS
-- Os dados foram tirados das telas do front-end. Funcionarios,
-- entradas de estoque e vendas sao cadastrados PELAS PROCEDURES,
-- entao a propria carga ja testa procedures e triggers.
-------------------------------------------------------------

-- Categorias (6)
insert into Categorias (nome, descricao)
values	('Elétricas',			'Furadeiras, parafusadeiras e ferramentas elétricas em geral'),
		('Manuais',				'Chaves, alicates, martelos e ferramentas manuais'),
		('Corte e Desbaste',	'Serras, esmerilhadeiras e discos'),
		('Medição',				'Trenas, níveis e instrumentos de medida'),
		('EPIs',				'Equipamentos de proteção individual'),
		('Acessórios',			'Brocas, bits, discos e acessórios')
go

-- Marcas (7)
insert into Marcas (nome)
values	('Bosch'), ('Makita'), ('Tramontina'), ('DeWalt'), ('Stanley'), ('Vonder'), ('3M')
go

-- Localizacoes (8) -> as 6 primeiras aparecem na tela do Estoquista
insert into Localizacoes (galpao, corredor, prateleira, box)
values	('Galpão Principal',	'A-01',		'P-02', 'B-04'),
		('Área de Expedição',	'EXP-01',	'P-01', 'B-01'),
		('Galpão Principal',	'C-03',		'P-01', 'B-02'),
		('Galpão Secundário',	'B-02',		'P-03', 'B-05'),
		('Galpão Principal',	'A-02',		'P-04', 'B-01'),
		('Galpão Principal',	'C-04',		'P-02', 'B-03'),
		('Galpão Secundário',	'D-01',		'P-01', 'B-01'),
		('Galpão Secundário',	'D-02',		'P-02', 'B-02')
go

-- Funcionarios (8) -> cadastrados pela procedure (Pessoas + Funcionarios + Usuarios)
-- Os logins batem com os nomes que o front-end ja usa (ana, carlos, pedro, joao).
declare @senha char(60) = '$2a$10$IVcJonOauSwPDIVnga/rw.dvVC89ve.vE8h9JBNCLa/ZUc/NS3czy'	-- toolsync123

exec sp_CadastrarFuncionario 'Ana Beatriz Moraes', 'ana.beatriz@toolsync.com.br', '(17) 99700-1122',
	'Rua Siqueira Campos', '1500', 'Centro', '15010-040', 'São José do Rio Preto', 'SP',
	'MAT-202301', '41.222.333-4', '301.222.444-55', '1982-07-21', 'PROPRIETARIO', 'ana', @senha

exec sp_CadastrarFuncionario 'Carlos Silva', 'carlos.silva@toolsync.com.br', '(17) 99811-2233',
	'Av. Alberto Andaló', '2450', 'Centro', '15015-000', 'São José do Rio Preto', 'SP',
	'MAT-202401', '39.845.120-4', '298.741.365-10', '1988-03-10', 'GERENTE', 'carlos', @senha

exec sp_CadastrarFuncionario 'Pedro Henrique da Costa Amaral', 'pedro.henrique@toolsync.com.br', '(17) 99123-4567',
	'Rua Bernardino de Campos', '1240', 'Centro', '15015-300', 'São José do Rio Preto', 'SP',
	'MAT-202501', '48.123.456-7', '389.456.123-01', '2004-12-17', 'VENDEDOR', 'pedro', @senha

exec sp_CadastrarFuncionario 'João Lucas Ferreira', 'joao.lucas@toolsync.com.br', '(17) 99234-5678',
	'Av. Bady Bassitt', '3100', 'Boa Vista', '15025-000', 'São José do Rio Preto', 'SP',
	'MAT-202502', '52.987.654-3', '412.789.654-22', '1999-05-14', 'ESTOQUISTA', 'joao', @senha

exec sp_CadastrarFuncionario 'Mariana Souza Almeida', 'mariana.almeida@toolsync.com.br', '(17) 99345-6789',
	'Rua Independência', '890', 'Vila Redentora', '15015-110', 'São José do Rio Preto', 'SP',
	'MAT-202503', '44.555.666-8', '355.666.777-88', '1996-09-22', 'VENDEDOR', 'mariana', @senha

exec sp_CadastrarFuncionario 'Rafael Teixeira Gomes', 'rafael.gomes@toolsync.com.br', '(17) 99456-7890',
	'Rua Saldanha Marinho', '2210', 'Centro', '15010-100', 'São José do Rio Preto', 'SP',
	'MAT-202504', '47.321.654-9', '367.111.222-33', '2001-02-08', 'VENDEDOR', 'rafael', @senha

exec sp_CadastrarFuncionario 'Beatriz Lima Santos', 'beatriz.santos@toolsync.com.br', '(17) 99567-8901',
	'Rua Tiradentes', '455', 'Vila Ercília', '15013-000', 'São José do Rio Preto', 'SP',
	'MAT-202505', '50.444.777-2', '423.555.888-99', '1998-11-30', 'ESTOQUISTA', 'beatriz', @senha

exec sp_CadastrarFuncionario 'Lucas Andrade Vieira', 'lucas.vieira@toolsync.com.br', '(17) 99678-9012',
	'Av. Murchid Homsi', '1800', 'Jardim Vivendas', '15090-220', 'São José do Rio Preto', 'SP',
	'MAT-202506', '46.888.999-0', '378.999.000-11', '2000-06-15', 'VENDEDOR', 'lucas', @senha
go

-- Clientes (10): primeiro os dados comuns em Pessoas...
insert into Pessoas (nome, email, telefone, rua, numero, bairro, cep, cidade, uf, data_cadastro)
values	('Poliane Soares da Cruz',	'poliane.cruz@email.com',		'(17) 99111-2233', 'Rua General Glicério',			'3420', 'Centro',			'15015-400', 'São José do Rio Preto', 'SP', dateadd(day, -190, getdate())),
		('Carlos Eduardo Mendes',	'carlos.mendes@email.com',		'(17) 99222-3344', 'Av. José Munia',				'5100', 'Nova Redentora',	'15090-500', 'São José do Rio Preto', 'SP', dateadd(day, -150, getdate())),
		('Mariana Fonseca Rocha',	'mariana.rocha@email.com',		'(17) 99333-4455', 'Rua Voluntários de São Paulo',	'2890', 'Centro',			'15015-200', 'São José do Rio Preto', 'SP', dateadd(day, -100, getdate())),
		('Construtora Alfa Ltda',	'compras@construtoraalfa.com.br','(17) 3222-4455', 'Av. Brigadeiro Faria Lima',		'6363', 'Jardim Morumbi',	'15090-000', 'São José do Rio Preto', 'SP', dateadd(day, -90, getdate())),
		('Juliana Pereira Campos',	'juliana.campos@email.com',		'(17) 99444-5566', 'Rua Coronel Spínola de Castro',	'3700', 'Centro',			'15015-500', 'São José do Rio Preto', 'SP', dateadd(day, -80, getdate())),
		('Serralheria Rio Preto ME','contato@serralheriarp.com.br',	'(17) 3233-7788', 'Rua Marechal Deodoro',			'1020', 'Boa Vista',		'15025-060', 'São José do Rio Preto', 'SP', dateadd(day, -70, getdate())),
		('Roberto Nogueira Lima',	'roberto.nogueira@email.com',	'(17) 99555-6677', 'Rua Pedro Amaral',				'640',  'Vila Imperial',	'15015-650', 'São José do Rio Preto', 'SP', dateadd(day, -60, getdate())),
		('Fernanda Ribeiro Alves',	'fernanda.ribeiro@email.com',	'(17) 99666-7788', 'Av. Philadelpho Gouveia Netto',	'2150', 'Vila Diniz',		'15013-300', 'São José do Rio Preto', 'SP', dateadd(day, -40, getdate())),
		('Thiago Martins Barbosa',	'thiago.barbosa@email.com',		'(17) 99777-8899', 'Rua Bernardino de Campos',		'980',  'Vila Redentora',	'15015-300', 'São José do Rio Preto', 'SP', dateadd(day, -20, getdate())),
		('Aline Cristina Duarte',	'aline.duarte@email.com',		'(17) 99888-9900', 'Rua Delegado Pinto de Toledo',	'1370', 'Parque Industrial','15025-700', 'São José do Rio Preto', 'SP', dateadd(day, -15, getdate()))
go

-- ...depois a parte de Clientes, ligando pelo e-mail (assim nao depende dos ids gerados)
insert into Clientes (pessoa_id, tipo_pessoa, documento)
select P.id, D.tipo, D.documento
from (values	('poliane.cruz@email.com',			'PF', '123.456.789-00'),
				('carlos.mendes@email.com',			'PF', '987.654.321-11'),
				('mariana.rocha@email.com',			'PF', '456.789.123-22'),
				('compras@construtoraalfa.com.br',	'PJ', '12.345.678/0001-90'),
				('juliana.campos@email.com',		'PF', '234.567.891-33'),
				('contato@serralheriarp.com.br',	'PJ', '23.456.789/0001-12'),
				('roberto.nogueira@email.com',		'PF', '345.678.912-44'),
				('fernanda.ribeiro@email.com',		'PF', '567.891.234-55'),
				('thiago.barbosa@email.com',		'PF', '678.912.345-66'),
				('aline.duarte@email.com',			'PF', '789.123.456-77')
	) as D(email, tipo, documento)
	inner join Pessoas as P on P.email = D.email
go

-- Fornecedores (6): dados comuns em Pessoas (nome = razao social)...
insert into Pessoas (nome, email, telefone, rua, numero, bairro, cep, cidade, uf)
values	('Bosch Ferramentas Elétricas Ltda',		'vendas.corporativas@boschfornecedor.com.br',	'(19) 3745-1000', 'Via Anhanguera, Km 98',	'9800', 'Boa Vista',		'13065-900', 'Campinas',		'SP'),
		('Tramontina Pro Industrial S.A.',			'atendimento@tramontinapro.com.br',				'(54) 3461-8200', 'Rua Maurício Cardoso',	'519',  'Centro',			'95185-000', 'Carlos Barbosa',	'RS'),
		('Makita do Brasil Ferramentas Ltda',		'comercial@makitabrasil.com.br',				'(11) 3699-4500', 'Av. dos Autonomistas',	'4900', 'Vila Yara',		'06090-020', 'Osasco',			'SP'),
		('Stanley Black & Decker do Brasil Ltda',	'distribuicao@sbdbrasil.com.br',				'(11) 3048-7700', 'Rua Gomes de Carvalho',	'1510', 'Vila Olímpia',		'04547-005', 'São Paulo',		'SP'),
		('3M do Brasil Ltda',						'epi.vendas@3mfornecedor.com.br',				'(19) 3838-7000', 'Rodovia Anhanguera, Km 110', 's/n', 'Jardim Bandeirantes', '13181-900', 'Sumaré',	'SP'),
		('OVD Importadora e Distribuidora (Vonder)','vendas@vonderdistribuidor.com.br',				'(41) 2101-8400', 'Rua Saturno',			'125',  'Distrito Industrial', '81460-140', 'Curitiba',	'PR')
go

-- ...depois a parte de Fornecedores, ligando pelo e-mail
insert into Fornecedores (pessoa_id, cnpj, ramo, regime_tributario, representante, dados_bancarios)
select P.id, D.cnpj, D.ramo, D.regime, D.representante, D.banco
from (values	('vendas.corporativas@boschfornecedor.com.br',	'45.990.181/0001-89', 'Ferramentas Elétricas e Furadeiras',		'Lucro Real',		'Roberto Mendes',	'Banco do Brasil | Ag: 3412-0 | CC: 88412-9'),
				('atendimento@tramontinapro.com.br',			'90.049.792/0001-81', 'Ferramentas Manuais e Chaves',			'Lucro Presumido',	'Fernanda Costa',	'Itaú | Ag: 0921 | CC: 45120-3'),
				('comercial@makitabrasil.com.br',				'45.865.920/0001-61', 'Serras, Lixadeiras e Parafusadeiras',	'Lucro Real',		'Eduardo Lima',		'Bradesco | Ag: 2380 | CC: 19283-4'),
				('distribuicao@sbdbrasil.com.br',				'31.204.555/0001-07', 'Ferramentas Manuais, Medição e DeWalt',	'Lucro Real',		'Patrícia Gouveia',	'Santander | Ag: 0451 | CC: 13002-8'),
				('epi.vendas@3mfornecedor.com.br',				'28.112.903/0001-52', 'EPIs e Abrasivos',						'Lucro Real',		'Marcos Antunes',	'Caixa | Ag: 1203 | CC: 00045-1'),
				('vendas@vonderdistribuidor.com.br',			'19.776.140/0001-36', 'Ferramentas, Medição e EPIs',			'Lucro Presumido',	'Sandra Kowalski',	'Sicredi | Ag: 0812 | Chave PIX: vendas@vonderdistribuidor.com.br')
	) as D(email, cnpj, ramo, regime, representante, banco)
	inner join Pessoas as P on P.email = D.email
go

-- Produtos (12) -> os 4 primeiros sao os que aparecem nas telas. Estoque comeca em zero:
-- quem coloca a mercadoria no estoque sao as entradas logo abaixo.
insert into Produtos (codigo, nome, descricao, categoria_id, marca_id, ean, sku, ncm,
					  preco_custo, preco_venda, unidade_compra, unidade_venda, largura_cm, altura_cm, comprimento_cm, estoque_minimo)
select	D.codigo, D.nome, D.descricao, C.id, M.id, D.ean, D.sku, D.ncm,
		D.custo, D.venda, D.ucompra, D.uvenda, D.largura, D.altura, D.comprimento, D.minimo
from (values
	('0001', 'Furadeira de Impacto 750W',				'Bivolt, mandril 1/2", com maleta',		'Elétricas',		'Bosch',		'7891234500011', 'BOS-FI750',	'8467.21.00', 250.00, 389.90, 'CX', 'UN', 28.0, 9.0, 30.0,  5),
	('0013', 'Jogo de Chaves Combinadas 12 Peças',		'Aço cromo vanádio, 6 a 22 mm',			'Manuais',			'Tramontina',	'7891234500134', 'TRA-JC12',	'8204.11.00',  85.00, 145.00, 'CX', 'JG', 30.0, 4.0, 22.0, 10),
	('0047', 'Parafusadeira Sem Fio 12V Bivolt',		'2 baterias e carregador',				'Elétricas',		'Makita',		'7891234500479', 'MAK-PS12',	'8467.21.00', 190.00, 299.00, 'CX', 'UN', 25.0, 8.0, 21.0, 10),
	('0052', 'Esmerilhadeira Angular 4.1/2" 850W',		'Com empunhadura lateral',				'Corte e Desbaste',	'Bosch',		'7891234500523', 'BOS-EA850',	'8467.29.00', 210.00, 320.00, 'CX', 'UN', 30.0, 10.0, 12.0, 6),
	('0060', 'Serra Circular 7.1/4" 1400W',				'Disco de 24 dentes incluso',			'Corte e Desbaste',	'DeWalt',		'7891234500608', 'DEW-SC1400',	'8467.22.00', 420.00, 649.90, 'CX', 'UN', 35.0, 25.0, 30.0, 3),
	('0071', 'Martelo de Unha 27mm Cabo Fibra',			'Cabeça forjada',						'Manuais',			'Tramontina',	'7891234500714', 'TRA-MU27',	'8205.20.00',  32.00,  59.90, 'CX', 'UN', 12.0, 3.0, 33.0,  8),
	('0072', 'Alicate Universal 8"',					'Isolado 1000V',						'Manuais',			'Stanley',		'7891234500721', 'STA-AU8',		'8203.20.10',  28.00,  49.90, 'CX', 'UN',  7.0, 2.0, 20.0, 10),
	('0080', 'Trena de Aço 5m',							'Trava automática',						'Medição',			'Stanley',		'7891234500806', 'STA-TR5',		'9017.80.10',  18.00,  34.90, 'CX', 'UN',  7.0, 7.0,  4.0, 15),
	('0081', 'Nível de Bolha 60cm',						'Alumínio, 3 bolhas',					'Medição',			'Vonder',		'7891234500813', 'VON-NB60',	'9015.80.10',  35.00,  69.90, 'CX', 'UN',  6.0, 2.5, 60.0,  5),
	('0090', 'Óculos de Proteção Incolor',				'Lente antirrisco, CA 9722',			'EPIs',				'3M',			'7891234500905', '3M-OCINC',	'9004.90.20',   6.50,  14.90, 'CX', 'UN', 15.0, 5.0,  6.0, 30),
	('0091', 'Luva de Vaqueta',							'Tamanho único, CA 10003',				'EPIs',				'Vonder',		'7891234500912', 'VON-LV',		'4203.29.00',  12.00,  24.90, 'CX', 'PR', 12.0, 2.0, 25.0, 20),
	('0100', 'Jogo de Brocas para Concreto 5 Peças',	'Pontas de metal duro, 5 a 10 mm',		'Acessórios',		'Bosch',		'7891234501001', 'BOS-BC5',		'8207.19.00',  22.00,  44.90, 'CX', 'JG',  8.0, 2.0, 16.0, 10)
	) as D(codigo, nome, descricao, categoria, marca, ean, sku, ncm, custo, venda, ucompra, uvenda, largura, altura, comprimento, minimo)
	inner join	Categorias	as C on C.nome = D.categoria
	inner join	Marcas		as M on M.nome = D.marca
go

-- Entradas de estoque (14) -> feitas pela procedure sp_EntradaEstoque.
-- Cada entrada gera a movimentacao (trigger soma no estoque), o vinculo
-- produto x fornecedor (N x N) e a quantidade por local (N x N).
-- Brocas e luvas vem de dois fornecedores cada.
declare @Entradas table
(
	ordem		int identity,
	codigo		varchar(10),
	quantidade	int,
	custo		decimal(10,2),
	cnpj		char(18),
	galpao		varchar(50),
	corredor	varchar(10),
	documento	varchar(150)
)

insert into @Entradas (codigo, quantidade, custo, cnpj, galpao, corredor, documento)
values	('0001',  10, 250.00, '45.990.181/0001-89', 'Galpão Principal',	'A-01',		'NF-8841 - Bosch'),
		('0013',  20,  85.00, '90.049.792/0001-81', 'Galpão Principal',	'C-03',		'NF-9022 - Tramontina'),
		('0047',  15, 190.00, '45.865.920/0001-61', 'Galpão Principal',	'A-02',		'NF-7741 - Makita'),
		('0052',  25, 210.00, '45.990.181/0001-89', 'Galpão Principal',	'C-04',		'NF-8890 - Bosch'),
		('0060',   6, 420.00, '31.204.555/0001-07', 'Galpão Secundário',	'B-02',		'NF-3310 - Stanley B&D'),
		('0071',  30,  32.00, '90.049.792/0001-81', 'Galpão Secundário',	'D-01',		'NF-9023 - Tramontina'),
		('0072',  25,  28.00, '31.204.555/0001-07', 'Galpão Secundário',	'D-01',		'NF-3311 - Stanley B&D'),
		('0080',  40,  18.00, '31.204.555/0001-07', 'Galpão Secundário',	'D-02',		'NF-3312 - Stanley B&D'),
		('0081',  10,  35.00, '19.776.140/0001-36', 'Galpão Secundário',	'D-02',		'NF-1207 - Vonder'),
		('0090', 100,   6.50, '28.112.903/0001-52', 'Área de Expedição',	'EXP-01',	'NF-5520 - 3M'),
		('0091',  30,  12.00, '19.776.140/0001-36', 'Área de Expedição',	'EXP-01',	'NF-1208 - Vonder'),
		('0091',  20,  12.00, '28.112.903/0001-52', 'Área de Expedição',	'EXP-01',	'NF-5521 - 3M'),
		('0100',  20,  22.00, '45.990.181/0001-89', 'Galpão Secundário',	'D-02',		'NF-8891 - Bosch'),
		('0100',  10,  22.00, '19.776.140/0001-36', 'Galpão Secundário',	'D-02',		'NF-1209 - Vonder')

declare @joao int = (select pessoa_id from Funcionarios where matricula = 'MAT-202502')
declare @produtoId int, @qtd int, @custo decimal(10,2), @fornecedorId int, @localId int, @documento varchar(150)

declare cEntradas cursor local fast_forward for
	select	P.id, E.quantidade, E.custo, F.pessoa_id, L.id, E.documento
	from @Entradas as E
		inner join	Produtos		as P on P.codigo = E.codigo
		inner join	Fornecedores	as F on F.cnpj = E.cnpj
		inner join	Localizacoes	as L on L.galpao = E.galpao and L.corredor = E.corredor
	order by E.ordem

open cEntradas
fetch next from cEntradas into @produtoId, @qtd, @custo, @fornecedorId, @localId, @documento
while @@fetch_status = 0
begin
	exec sp_EntradaEstoque @produtoId, @qtd, @custo, @fornecedorId, @localId, @joao, @documento
	fetch next from cEntradas into @produtoId, @qtd, @custo, @fornecedorId, @localId, @documento
end
close cEntradas
deallocate cEntradas

-- as entradas aconteceram ha cerca de 70 dias (antes das vendas)
update MovimentacoesEstoque set data_movimentacao = dateadd(day, -70, getdate()) where tipo = 'ENTRADA'
update ProdutoFornecedor	set data_ultima_compra = dateadd(day, -70, getdate())
go

-- Vendas (11) -> feitas pela procedure sp_RegistrarVenda (JSON de itens e pagamentos).
-- O trigger de ItensVenda gera as movimentacoes e o trigger de
-- MovimentacoesEstoque baixa o estoque. Uma venda fica RESERVADA
-- e uma e CANCELADA (devolvendo o estoque).
declare @pedro		int = (select pessoa_id from Funcionarios where matricula = 'MAT-202501')
declare @mariana	int = (select pessoa_id from Funcionarios where matricula = 'MAT-202503')
declare @rafael		int = (select pessoa_id from Funcionarios where matricula = 'MAT-202504')
declare @ana		int = (select pessoa_id from Funcionarios where matricula = 'MAT-202301')
declare @carlos		int = (select pessoa_id from Funcionarios where matricula = 'MAT-202401')

declare @itens nvarchar(max), @pagamentos nvarchar(max), @total decimal(12,2), @cliente int
declare @v1 int, @v2 int, @v3 int, @v4 int, @v5 int, @v6 int, @v7 int, @v8 int, @v9 int, @v10 int, @v11 int

-- Venda 1: Poliane / Pedro / 2 furadeiras / cartao de debito
set @cliente	= (select pessoa_id from Clientes where documento = '123.456.789-00')
set @itens		= (	select P.id as produtoId, I.qtd as quantidade
					from (values ('0001', 2)) as I(codigo, qtd) inner join Produtos as P on P.codigo = I.codigo for json path )
set @total		= (	select sum(P.preco_venda * J.quantidade) from openjson(@itens) with (produtoId int, quantidade int) as J
					inner join Produtos as P on P.id = J.produtoId )
set @pagamentos	= (select 'CARTAO_DEBITO' as forma, @total as valor for json path)
exec sp_RegistrarVenda @clienteId = @cliente, @vendedorId = @pedro, @itensJson = @itens, @pagamentosJson = @pagamentos,
					   @numeroCupom = 'CF-001041', @vendaId = @v1 output

-- Venda 2: Carlos Eduardo / Pedro / 2 jogos de chaves / PIX
set @cliente	= (select pessoa_id from Clientes where documento = '987.654.321-11')
set @itens		= (	select P.id as produtoId, I.qtd as quantidade
					from (values ('0013', 2)) as I(codigo, qtd) inner join Produtos as P on P.codigo = I.codigo for json path )
set @total		= (	select sum(P.preco_venda * J.quantidade) from openjson(@itens) with (produtoId int, quantidade int) as J
					inner join Produtos as P on P.id = J.produtoId )
set @pagamentos	= (select 'PIX' as forma, @total as valor for json path)
exec sp_RegistrarVenda @clienteId = @cliente, @vendedorId = @pedro, @itensJson = @itens, @pagamentosJson = @pagamentos,
					   @numeroCupom = 'CF-001042', @vendaId = @v2 output

-- Venda 3: Mariana Fonseca / Mariana / 3 parafusadeiras / dinheiro com troco
set @cliente	= (select pessoa_id from Clientes where documento = '456.789.123-22')
set @itens		= (	select P.id as produtoId, I.qtd as quantidade
					from (values ('0047', 3)) as I(codigo, qtd) inner join Produtos as P on P.codigo = I.codigo for json path )
set @total		= (	select sum(P.preco_venda * J.quantidade) from openjson(@itens) with (produtoId int, quantidade int) as J
					inner join Produtos as P on P.id = J.produtoId )
set @pagamentos	= (select 'DINHEIRO' as forma, @total as valor, 900.00 as valorRecebido for json path)
exec sp_RegistrarVenda @clienteId = @cliente, @vendedorId = @mariana, @itensJson = @itens, @pagamentosJson = @pagamentos,
					   @numeroCupom = 'CF-001043', @vendaId = @v3 output

-- Venda 4: Construtora Alfa (PJ) / Pedro / esmerilhadeiras + EPIs / credito em 3x / com NF
set @cliente	= (select pessoa_id from Clientes where documento = '12.345.678/0001-90')
set @itens		= (	select P.id as produtoId, I.qtd as quantidade
					from (values ('0052', 7), ('0090', 20), ('0091', 10)) as I(codigo, qtd) inner join Produtos as P on P.codigo = I.codigo for json path )
set @total		= (	select sum(P.preco_venda * J.quantidade) from openjson(@itens) with (produtoId int, quantidade int) as J
					inner join Produtos as P on P.id = J.produtoId )
set @pagamentos	= (select 'CARTAO_CREDITO' as forma, @total as valor, 3 as parcelas for json path)
exec sp_RegistrarVenda @clienteId = @cliente, @vendedorId = @pedro, @itensJson = @itens, @pagamentosJson = @pagamentos,
					   @numeroNf = 'NF-000512', @vendaId = @v4 output

-- Venda 5: Juliana / Rafael / martelos, alicates e trenas / PIX
set @cliente	= (select pessoa_id from Clientes where documento = '234.567.891-33')
set @itens		= (	select P.id as produtoId, I.qtd as quantidade
					from (values ('0071', 2), ('0072', 2), ('0080', 2)) as I(codigo, qtd) inner join Produtos as P on P.codigo = I.codigo for json path )
set @total		= (	select sum(P.preco_venda * J.quantidade) from openjson(@itens) with (produtoId int, quantidade int) as J
					inner join Produtos as P on P.id = J.produtoId )
set @pagamentos	= (select 'PIX' as forma, @total as valor for json path)
exec sp_RegistrarVenda @clienteId = @cliente, @vendedorId = @rafael, @itensJson = @itens, @pagamentosJson = @pagamentos,
					   @numeroCupom = 'CF-001045', @vendaId = @v5 output

-- Venda 6: Serralheria (PJ) / Ana (proprietaria) / serras e brocas / PIX + debito
set @cliente	= (select pessoa_id from Clientes where documento = '23.456.789/0001-12')
set @itens		= (	select P.id as produtoId, I.qtd as quantidade
					from (values ('0060', 2), ('0100', 3)) as I(codigo, qtd) inner join Produtos as P on P.codigo = I.codigo for json path )
set @total		= (	select sum(P.preco_venda * J.quantidade) from openjson(@itens) with (produtoId int, quantidade int) as J
					inner join Produtos as P on P.id = J.produtoId )
set @pagamentos	= (	select forma, valor from (values (1, 'PIX', 1000.00), (2, 'CARTAO_DEBITO', @total - 1000.00)) as X(ordem, forma, valor)
					order by ordem for json path )
exec sp_RegistrarVenda @clienteId = @cliente, @vendedorId = @ana, @itensJson = @itens, @pagamentosJson = @pagamentos,
					   @numeroNf = 'NF-000513', @vendaId = @v6 output

-- Venda 7: Aline / Pedro / oculos / PIX -> sera CANCELADA logo abaixo
set @cliente	= (select pessoa_id from Clientes where documento = '789.123.456-77')
set @itens		= (	select P.id as produtoId, I.qtd as quantidade
					from (values ('0090', 5)) as I(codigo, qtd) inner join Produtos as P on P.codigo = I.codigo for json path )
set @total		= (	select sum(P.preco_venda * J.quantidade) from openjson(@itens) with (produtoId int, quantidade int) as J
					inner join Produtos as P on P.id = J.produtoId )
set @pagamentos	= (select 'PIX' as forma, @total as valor for json path)
exec sp_RegistrarVenda @clienteId = @cliente, @vendedorId = @pedro, @itensJson = @itens, @pagamentosJson = @pagamentos,
					   @numeroCupom = 'CF-001047', @vendaId = @v7 output

exec sp_CancelarVenda @v7, @carlos, 'Cliente desistiu da compra'

-- Venda 8: Roberto / Mariana / jogos de chaves e nivel / credito em 2x
set @cliente	= (select pessoa_id from Clientes where documento = '345.678.912-44')
set @itens		= (	select P.id as produtoId, I.qtd as quantidade
					from (values ('0013', 8), ('0081', 1)) as I(codigo, qtd) inner join Produtos as P on P.codigo = I.codigo for json path )
set @total		= (	select sum(P.preco_venda * J.quantidade) from openjson(@itens) with (produtoId int, quantidade int) as J
					inner join Produtos as P on P.id = J.produtoId )
set @pagamentos	= (select 'CARTAO_CREDITO' as forma, @total as valor, 2 as parcelas for json path)
exec sp_RegistrarVenda @clienteId = @cliente, @vendedorId = @mariana, @itensJson = @itens, @pagamentosJson = @pagamentos,
					   @numeroCupom = 'CF-001048', @vendaId = @v8 output

-- Venda 9: Fernanda / Pedro / furadeiras e trenas / dinheiro (com troco) + PIX
set @cliente	= (select pessoa_id from Clientes where documento = '567.891.234-55')
set @itens		= (	select P.id as produtoId, I.qtd as quantidade
					from (values ('0001', 3), ('0080', 3)) as I(codigo, qtd) inner join Produtos as P on P.codigo = I.codigo for json path )
set @total		= (	select sum(P.preco_venda * J.quantidade) from openjson(@itens) with (produtoId int, quantidade int) as J
					inner join Produtos as P on P.id = J.produtoId )
set @pagamentos	= (	select forma, valor, valorRecebido
					from (values (1, 'DINHEIRO', @total - 1000.00, 300.00), (2, 'PIX', 1000.00, null)) as X(ordem, forma, valor, valorRecebido)
					order by ordem for json path )
exec sp_RegistrarVenda @clienteId = @cliente, @vendedorId = @pedro, @itensJson = @itens, @pagamentosJson = @pagamentos,
					   @numeroCupom = 'CF-001049', @vendaId = @v9 output

-- Venda 10: Thiago / Rafael / furadeiras, martelo e parafusadeira / PIX
set @cliente	= (select pessoa_id from Clientes where documento = '678.912.345-66')
set @itens		= (	select P.id as produtoId, I.qtd as quantidade
					from (values ('0001', 2), ('0071', 1), ('0047', 1)) as I(codigo, qtd) inner join Produtos as P on P.codigo = I.codigo for json path )
set @total		= (	select sum(P.preco_venda * J.quantidade) from openjson(@itens) with (produtoId int, quantidade int) as J
					inner join Produtos as P on P.id = J.produtoId )
set @pagamentos	= (select 'PIX' as forma, @total as valor for json path)
exec sp_RegistrarVenda @clienteId = @cliente, @vendedorId = @rafael, @itensJson = @itens, @pagamentosJson = @pagamentos,
					   @numeroCupom = 'CF-001050', @vendaId = @v10 output

-- Venda 11: Poliane / Mariana / 1 furadeira RESERVADA (sem pagamento ainda)
set @cliente	= (select pessoa_id from Clientes where documento = '123.456.789-00')
set @itens		= (	select P.id as produtoId, I.qtd as quantidade, 'Retira na loja' as observacao
					from (values ('0001', 1)) as I(codigo, qtd) inner join Produtos as P on P.codigo = I.codigo for json path )
exec sp_RegistrarVenda @clienteId = @cliente, @vendedorId = @mariana, @itensJson = @itens, @reserva = 1, @vendaId = @v11 output

-- Espalha as vendas no tempo para os dashboards e relatorios terem dados
-- (dia, semana e mes). Movimentacoes e pagamentos acompanham a data da venda.
declare @Datas table (venda_id int, data datetime)
insert into @Datas values	(@v1,  dateadd(day, -60, getdate())),	(@v2,  dateadd(day, -45, getdate())),
							(@v3,  dateadd(day, -35, getdate())),	(@v4,  dateadd(day, -20, getdate())),
							(@v5,  dateadd(day, -12, getdate())),	(@v6,  dateadd(day,  -6, getdate())),
							(@v7,  dateadd(day,  -8, getdate())),	(@v8,  dateadd(day,  -3, getdate())),
							(@v9,  dateadd(day,  -1, getdate())),	(@v10, dateadd(minute, -90, getdate())),
							(@v11, dateadd(minute, -30, getdate()))

update V set V.data_venda = D.data
from Vendas as V inner join @Datas as D on D.venda_id = V.id

update PV set PV.data_pagamento = D.data
from PagamentosVenda as PV inner join @Datas as D on D.venda_id = PV.venda_id

update M set M.data_movimentacao = D.data
from MovimentacoesEstoque as M inner join @Datas as D on D.venda_id = M.venda_id
where M.tipo in ('SAIDA', 'RESERVA')

-- a venda 7 foi cancelada um dia depois de feita
update Vendas set data_cancelamento = dateadd(day, -7, getdate()) where id = @v7
update MovimentacoesEstoque set data_movimentacao = dateadd(day, -7, getdate()) where venda_id = @v7 and tipo = 'ESTORNO'
go

-- Alteracao de preco -> dispara o trigger tg_Produtos_HistoricoPreco
update Produtos set preco_venda = 36.90 where codigo = '0080'
go

-- Funcionario desligado -> dispara o trigger tg_Funcionarios_SincronizarUsuario
-- (o login do Lucas fica inativo automaticamente)
update Funcionarios set ativo = 0 where matricula = 'MAT-202506'
go


-------------------------------------------------------------
-- Conferir os dados cadastrados
-------------------------------------------------------------
select * from Pessoas
go
select * from Clientes
go
select * from Funcionarios
go
select funcionario_id, login, ativo from Usuarios
go
select * from Fornecedores
go
select * from Produtos
go
select * from ProdutoFornecedor
go
select * from ProdutoLocalizacao
go
select * from Vendas
go
select * from ItensVenda
go
select * from PagamentosVenda
go
select * from MovimentacoesEstoque
go
select * from HistoricoPrecos
go

-------------------------------------------------------------
-- Testar views e functions (as mesmas consultas que o Java faz)
-------------------------------------------------------------
select * from v_DashboardVendas
go
select * from v_VendasDetalhadas order by data_venda desc
go
select * from v_RelatorioVendas where vendedor like '%Pedro%'
go
select * from v_EstoqueBaixo
go
select * from v_HistoricoMovimentacoes where produto_codigo = '0001' order by data_movimentacao
go
select dbo.fc_TotalVendasPeriodo(dateadd(day, -30, getdate()), getdate()) as total_ultimos_30_dias
go
select P.nome as cliente, dbo.fc_UltimaCompraCliente(C.pessoa_id) as ultima_compra
from Clientes as C inner join Pessoas as P on P.id = C.pessoa_id
go
select * from dbo.fc_HistoricoCliente((select pessoa_id from Clientes where documento = '123.456.789-00'))
go
select * from dbo.fc_VendasPorVendedor(dateadd(month, -3, getdate()), getdate()) order by total_vendido desc
go

-------------------------------------------------------------
-- Testes das regras (descomente para ver o erro acontecendo)
-------------------------------------------------------------
-- (ids da carga acima: 1 a 8 = funcionarios, 9 a 18 = clientes; produto 1 = '0001', produto 2 = '0013')
-- 1) Vender mais do que tem no estoque -> trigger bloqueia e a procedure desfaz tudo
-- exec sp_RegistrarVenda @clienteId = 9, @vendedorId = 3, @itensJson = N'[{"produtoId":1,"quantidade":999}]',
--                        @pagamentosJson = N'[{"forma":"PIX","valor":1.00}]'
--
-- 2) Pagamento diferente do total -> procedure desfaz a venda
-- exec sp_RegistrarVenda @clienteId = 9, @vendedorId = 3, @itensJson = N'[{"produtoId":2,"quantidade":1}]',
--                        @pagamentosJson = N'[{"forma":"PIX","valor":10.00}]'
--
-- 3) Estoquista tentando vender -> procedure recusa
-- exec sp_RegistrarVenda @clienteId = 9, @vendedorId = 4, @itensJson = N'[{"produtoId":2,"quantidade":1}]',
--                        @pagamentosJson = N'[{"forma":"PIX","valor":145.00}]'
--
-- 4) Desativar a unica proprietaria -> trigger bloqueia
-- update Funcionarios set ativo = 0 where cargo = 'PROPRIETARIO'
--
-- 5) Backup do banco (ponto extra)
-- exec sp_BackupBanco
