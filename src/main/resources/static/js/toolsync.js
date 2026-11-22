/**
 * ARQUIVO: js/toolsync.js
 * DESCRIÇÃO: Biblioteca compartilhada do front-end ToolSync. Faz a comunicação com a
 * API REST do back-end Spring Boot (que grava no SQL Server), controla a sessão do
 * usuário logado e contém a lógica de cada tela. Cada tela chama a sua função em
 * ToolSync.telas (ex.: ToolSync.telas.consultarClientes()).
 */
const ToolSync = (() => {
    // páginas ficam em subpastas (proprietario/, gerente/...): a raiz do site é "../"
    const RAIZ = /\/(proprietario|gerente|vendedor|estoquista)\//.test(location.pathname) ? '../' : '';
    const PASTA_PERFIL = { proprietario: 'PROPRIETARIO', gerente: 'GERENTE', vendedor: 'VENDEDOR', estoquista: 'ESTOQUISTA' };
    let usuario = null;

    // ------------------------------------------------------------------ API
    async function api(metodo, url, corpo) {
        const opcoes = { method: metodo, headers: {}, credentials: 'same-origin' };
        if (corpo !== undefined) {
            opcoes.headers['Content-Type'] = 'application/json';
            opcoes.body = JSON.stringify(corpo);
        }
        let resposta;
        try {
            resposta = await fetch(RAIZ + url.replace(/^\//, ''), opcoes);
        } catch (e) {
            throw new Error('Não foi possível falar com o servidor. Verifique se o sistema está rodando.');
        }
        // sessão expirada no meio do uso: volta para o login (as chamadas de /api/auth/ tratam o 401 por conta própria)
        if (resposta.status === 401 && !url.includes('/api/auth/')) {
            alert('Sua sessão expirou. Faça login novamente.');
            location.href = RAIZ + 'login.html';
            throw new Error('Sessão expirada');
        }
        const texto = await resposta.text();
        const dados = texto ? JSON.parse(texto) : null;
        if (!resposta.ok) {
            throw new Error((dados && dados.mensagem) || `Erro ${resposta.status}`);
        }
        return dados;
    }
    const get = url => api('GET', url);
    const post = (url, corpo) => api('POST', url, corpo === undefined ? {} : corpo);
    const put = (url, corpo) => api('PUT', url, corpo);
    const del = url => api('DELETE', url);

    // ------------------------------------------------------------------ formatação
    function moeda(v) {
        return 'R$ ' + Number(v || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
    function moedaSemSimbolo(v) {
        return Number(v || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
    /** "R$ 1.234,56", "1234.56" ou "1234,56" -> 1234.56 */
    function numero(texto) {
        if (texto === null || texto === undefined) return 0;
        let s = String(texto).replace(/R\$|\s/g, '');
        if (s.includes(',')) s = s.replace(/\./g, '').replace(',', '.');
        const n = parseFloat(s);
        return isNaN(n) ? 0 : n;
    }
    function numeroOuNulo(texto) {
        return texto === null || texto === undefined || String(texto).trim() === '' ? null : numero(texto);
    }
    function data(iso) {
        if (!iso) return '-';
        const [d] = String(iso).split('T');
        const [a, m, dia] = d.split('-');
        return `${dia}/${m}/${a}`;
    }
    function hora(iso) {
        return iso && iso.includes('T') ? iso.split('T')[1].slice(0, 5) : '';
    }
    function esc(s) {
        return String(s === null || s === undefined ? '' : s)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }
    const $ = id => document.getElementById(id);
    const valor = id => ($(id) ? $(id).value.trim() : '');
    function definir(id, v) { if ($(id)) $(id).value = v === null || v === undefined ? '' : v; }
    const STATUS = { FINALIZADA: 'Finalizado', RESERVADA: 'Reservado', CANCELADA: 'Cancelado', ABERTA: 'Aberto' };
    const FORMAS = { DINHEIRO: 'Dinheiro', PIX: 'PIX', CARTAO_DEBITO: 'Cartão (Débito)', CARTAO_CREDITO: 'Cartão (Crédito)' };
    const CARGOS = { PROPRIETARIO: 'Proprietário', GERENTE: 'Gerente', VENDEDOR: 'Vendedor', ESTOQUISTA: 'Estoquista' };
    function erro(e) { if (e && e.message !== 'Sessão expirada') alert(e.message); }

    // ------------------------------------------------------------------ sessão
    /** confere o login, o perfil da pasta da página e preenche o cabeçalho */
    async function iniciar() {
        try {
            usuario = await get('/api/auth/me');
        } catch (e) {
            location.href = RAIZ + 'login.html';
            return new Promise(() => {});
        }
        const pasta = (location.pathname.match(/\/(proprietario|gerente|vendedor|estoquista)\//) || [])[1];
        if (pasta && PASTA_PERFIL[pasta] !== usuario.cargo) {
            location.href = RAIZ + usuario.telaInicial;
            return new Promise(() => {});
        }
        if ($('userNameDisplay')) $('userNameDisplay').textContent = usuario.nome;
        if ($('userRoleDisplay')) $('userRoleDisplay').textContent = usuario.cargoDescricao;
        const sair = $('btnLogout');
        if (sair) {
            sair.addEventListener('click', async ev => {
                ev.preventDefault();
                try { await post('/api/auth/logout'); } catch (e) { /* ignora */ }
                location.href = RAIZ + 'login.html';
            });
        }
        // o proprietário tem a tela de Configurações (backup e restore do banco)
        if (usuario.cargo === 'PROPRIETARIO') {
            const menu = document.querySelector('.nav-menu');
            if (menu && !menu.querySelector('a[href="telaconfiguracoesp.html"]')) {
                const link = document.createElement('a');
                link.href = 'telaconfiguracoesp.html';
                link.className = 'nav-btn' + (location.pathname.endsWith('telaconfiguracoesp.html') ? ' active' : '');
                link.textContent = 'Configurações';
                const rel = menu.querySelector('a[href="telarelatoriosp.html"]');
                if (rel) rel.insertAdjacentElement('afterend', link); else menu.appendChild(link);
            }
        }
        return usuario;
    }

    function preencherDatalist(id, itens) {
        let lista = $(id);
        if (!lista) {
            lista = document.createElement('datalist');
            lista.id = id;
            document.body.appendChild(lista);
        }
        lista.innerHTML = itens.map(t => `<option value="${esc(t)}"></option>`).join('');
    }

    // ==================================================================== TELAS
    const telas = {};

    /** telas que só mostram o menu (Cadastrar, Venda) */
    telas.menu = async () => { await iniciar(); };

    // ------------------------------------------------------------------ login
    telas.login = () => {
        const form = $('loginForm');
        const senha = $('senha');
        const btnOlho = $('togglePassword');
        const olho = $('eyeIcon');
        if (btnOlho && senha && olho) {
            btnOlho.addEventListener('click', () => {
                const oculta = senha.getAttribute('type') === 'password';
                senha.setAttribute('type', oculta ? 'text' : 'password');
                olho.classList.toggle('fa-eye', !oculta);
                olho.classList.toggle('fa-eye-slash', oculta);
            });
        }
        // se já estiver logado, vai direto para a tela inicial
        get('/api/auth/me').then(u => { location.href = u.telaInicial; }).catch(() => {});
        form.addEventListener('submit', async ev => {
            ev.preventDefault();
            const botao = form.querySelector('button[type="submit"], .btn-entrar, button');
            if (botao) botao.disabled = true;
            try {
                const u = await post('/api/auth/login', { login: valor('usuario'), senha: senha.value });
                location.href = u.telaInicial;
            } catch (e) {
                alert(e.message);
            } finally {
                if (botao) botao.disabled = false;
            }
        });
    };

    // ------------------------------------------------------------------ dashboard
    telas.dashboard = async () => {
        const u = await iniciar();
        let d;
        try { d = await get('/api/dashboard'); } catch (e) { return erro(e); }

        // cards: procura os títulos "Vendas do mês / da semana / do dia"
        const valores = { 'vendas do mês': d.cards.vendas_mes, 'vendas da semana': d.cards.vendas_semana, 'vendas do dia': d.cards.vendas_dia };
        document.querySelectorAll('.metric-card').forEach(card => {
            const titulo = (card.querySelector('h3') || {}).textContent;
            const alvo = card.querySelector('.amount');
            const v = titulo && valores[titulo.trim().toLowerCase()];
            if (alvo && v !== undefined) alvo.innerHTML = `<span class="currency">R$</span> ${moedaSemSimbolo(v)}`;
        });
        document.querySelectorAll('main div').forEach(div => {
            const t = div.textContent.trim().toLowerCase();
            if (div.children.length === 0 && valores[t] !== undefined && div.nextElementSibling) {
                div.nextElementSibling.textContent = moeda(valores[t]);
            }
        });

        // gráfico de linha: vendas por mês do ano
        const canvasMes = $('chartVendasMes') || $('chartMes');
        if (canvasMes && window.Chart) {
            new Chart(canvasMes, {
                type: 'line',
                data: {
                    labels: d.vendasPorMes.map(m => m.mes),
                    datasets: [{ label: 'Vendas (R$)', data: d.vendasPorMes.map(m => Number(m.total)), borderColor: '#1d5288',
                        backgroundColor: 'rgba(29, 82, 136, 0.1)', borderWidth: 2, fill: false, tension: 0.1 }]
                },
                options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } },
                    scales: { y: { beginAtZero: true, ticks: { callback: v => 'R$ ' + v } } } }
            });
        }
        // gráfico de barras: vendas dos últimos 7 dias por vendedor
        const canvasSemana = $('chartVendasSemana') || $('chartSemana');
        if (canvasSemana && window.Chart) {
            const vend = d.vendasSemanaPorVendedor;
            new Chart(canvasSemana, {
                type: 'bar',
                data: {
                    labels: vend.map(v => v.vendedor.split(' ')[0]),
                    datasets: [{ label: 'Vendas (R$)', data: vend.map(v => Number(v.total_vendido)), backgroundColor: '#1d5288' }]
                },
                options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } },
                    scales: { y: { beginAtZero: true, ticks: { callback: v => 'R$ ' + v } } } }
            });
        }

        // produtos com estoque baixo
        const estoque = d.estoqueBaixo.slice(0, 5);
        const pastaEstoque = { PROPRIETARIO: 'telaestoquep.html', GERENTE: 'telaestoqueg.html', ESTOQUISTA: 'telaconsultarestoquee.html' }[u.cargo];
        const titulo = [...document.querySelectorAll('h3')].find(h => h.textContent.toLowerCase().includes('estoque baixo'));
        const caixa = document.querySelector('.stock-items-wrapper') || (titulo && titulo.nextElementSibling);
        if (caixa) {
            if (estoque.length === 0) {
                caixa.innerHTML = '<p style="text-align:center; padding:10px;">Nenhum produto abaixo do estoque mínimo.</p>';
            } else if (caixa.classList.contains('stock-items-wrapper')) {
                caixa.innerHTML = estoque.map(p => {
                    const cor = p.situacao === 'BAIXO' ? 'alert-orange' : 'alert-red';
                    const link = pastaEstoque === 'telaconsultarestoquee.html' ? `${pastaEstoque}?codigo=${encodeURIComponent(p.codigo)}` : pastaEstoque;
                    return `<div class="stock-item" style="cursor:pointer" onclick="location.href='${link}'">
                        <h4>${esc(p.nome)}</h4>
                        <p>Código produto: <strong>${esc(p.codigo)}</strong></p>
                        <p>Quantidade disponível: <span class="${cor}">${String(p.estoque_disponivel).padStart(2, '0')}</span></p>
                        <p>Estoque min: <span class="red-text">${String(p.estoque_minimo).padStart(2, '0')}</span></p>
                    </div>`;
                }).join('');
            } else {
                caixa.innerHTML = estoque.map(p => `
                    <div style="background:#d0d0d0; padding:8px; border-radius:2px; text-align:center; font-size:12px;">
                        <strong>${esc(p.nome)}</strong><br>Código produto: ${esc(p.codigo)}<br>
                        Quantidade disponível: <span style="color:${p.situacao === 'BAIXO' ? '#d97700' : 'red'}; font-weight:bold;">${String(p.estoque_disponivel).padStart(2, '0')}</span><br>
                        Estoque min: ${String(p.estoque_minimo).padStart(2, '0')}
                    </div>`).join('');
            }
        }
        document.querySelectorAll('a').forEach(a => {
            if (a.textContent.includes('Ver produtos') && a.getAttribute('href') === '#') a.style.display = 'none';
        });

        // estoquista: últimas saídas
        const corpo = $('ultimasSaidasBody');
        if (corpo && d.ultimasSaidas) {
            corpo.innerHTML = d.ultimasSaidas.length === 0 ? '<tr><td colspan="5">Nenhuma saída registrada.</td></tr>'
                : d.ultimasSaidas.map(s => `<tr>
                    <td><strong>${esc(s.produto_codigo)}</strong></td>
                    <td>${esc(s.produto)}</td>
                    <td><span class="red-text">- ${String(s.quantidade).padStart(2, '0')} un</span></td>
                    <td>${data(s.data_movimentacao)} ${hora(s.data_movimentacao)}</td>
                    <td>${esc(s.documento || '-')}</td>
                </tr>`).join('');
        }
    };

    // ------------------------------------------------------------------ clientes
    telas.consultarClientes = async () => {
        const u = await iniciar();
        const tbody = $('tabelaClientesBody');
        const filtro = $('filterBy');
        const busca = $('inputBusca');
        const btn = $('btnBuscarCli') || document.querySelector('.btn-buscar');
        let clientes = [];

        window.verCliente = async id => {
            try {
                const c = clientes.find(x => x.id === id);
                const hist = await get(`/api/clientes/${id}/historico`);
                const compras = hist.length === 0 ? 'Nenhuma compra registrada.'
                    : hist.slice(0, 8).map(h => `#${String(h.venda_id).padStart(4, '0')}  ${data(h.data_venda)}  ${moeda(h.valor_total)}  ${STATUS[h.status] || h.status}`).join('\n');
                alert(`Cliente: ${c.nome}\n${c.tipoPessoa === 'PJ' ? 'CNPJ' : 'CPF'}: ${c.documento}\n` +
                    `Endereço: ${c.rua || ''}, ${c.numero || ''} - ${c.bairro || ''} (${c.cidade || ''}/${c.uf || ''})\n` +
                    `Telefone: ${c.telefone || '-'}  E-mail: ${c.email || '-'}\n\nHistórico de compras:\n${compras}`);
            } catch (e) { erro(e); }
        };
        window.excluirClienteLista = async (id, nome) => {
            if (!confirm(`Deseja excluir o cliente "${nome}"?`)) return;
            try {
                await del(`/api/clientes/${id}`);
                alert(`Cliente "${nome}" excluído com sucesso!`);
                await carregar();
            } catch (e) { erro(e); }
        };

        function render() {
            const termo = busca.value.trim().toLowerCase();
            const crit = (filtro.value || '').toLowerCase();
            const lista = clientes.filter(c => !termo || (crit.includes('cpf') ? (c.documento || '') : (c.nome || '')).toLowerCase().includes(termo));
            if (lista.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6">Nenhum cliente encontrado.</td></tr>';
                return;
            }
            tbody.innerHTML = lista.map(c => `<tr>
                <td><strong>${c.codigo}</strong></td>
                <td>${esc(c.documento)}</td>
                <td>${esc(c.nome)}</td>
                <td>${data(c.dataCadastro)}</td>
                <td>${esc(c.ultimaCompra || 'Sem compras')}</td>
                <td>
                    <button type="button" class="btn-row-edit" onclick="verCliente(${c.id})"><i class="fa-solid fa-eye"></i> Detalhes</button>
                    ${u.cargo === 'PROPRIETARIO' ? `<button type="button" class="btn-row-delete" onclick="excluirClienteLista(${c.id}, '${esc(c.nome)}')"><i class="fa-solid fa-trash"></i> Excluir</button>` : ''}
                </td>
            </tr>`).join('');
        }
        async function carregar() {
            try { clientes = await get('/api/clientes'); render(); } catch (e) { erro(e); }
        }
        await carregar();
        if (btn) btn.addEventListener('click', render);
        busca.addEventListener('keyup', render);
    };

    telas.cadastrarCliente = async ({ voltar }) => {
        await iniciar();
        definir('cliCod', 'Novo');
        $('btnSalvarCliente').addEventListener('click', async () => {
            const dados = {
                nome: valor('cliNome'), documento: valor('cliCPF'), rua: valor('cliRua'), bairro: valor('cliBairro'),
                numero: valor('cliNum'), cep: valor('cliCEP'), cidade: valor('cliCidade'), uf: valor('cliUF'),
                telefone: valor('cliTelefone'), email: valor('cliEmail')
            };
            if (!dados.nome || !dados.documento) return alert('Por favor, preencha o Nome e o CPF/CNPJ do cliente.');
            try {
                const c = await post('/api/clientes', dados);
                alert(`Cliente ${c.nome} cadastrado com sucesso! Código: ${c.codigo}`);
                location.href = voltar;
            } catch (e) { erro(e); }
        });
    };

    // ------------------------------------------------------------------ funcionários
    telas.consultarFuncionarios = async ({ detalhe }) => {
        await iniciar();
        const tbody = $('tabelaFuncionariosBody');
        const busca = $('inputBusca');
        const filtro = $('filterBy');
        let lista = [];

        window.excluirFuncionarioLista = async (id, nome) => {
            if (!confirm(`Tem certeza que deseja excluir o funcionário "${nome}"?`)) return;
            try {
                const r = await del(`/api/funcionarios/${id}`);
                alert(r.mensagem);
                await carregar();
            } catch (e) { erro(e); }
        };
        function render() {
            const termo = busca.value.trim().toLowerCase();
            const crit = filtro ? filtro.value : 'nome';
            const filtrados = lista.filter(f => {
                if (!termo) return true;
                if (crit === 'matricula') return (f.matricula || '').toLowerCase().includes(termo);
                if (crit === 'cpf') return (f.cpf || '').toLowerCase().includes(termo);
                if (crit === 'funcao') return (f.cargoDescricao || '').toLowerCase().includes(termo);
                return (f.nome || '').toLowerCase().includes(termo);
            });
            if (filtrados.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7">Nenhum funcionário encontrado.</td></tr>';
                return;
            }
            tbody.innerHTML = filtrados.map(f => `<tr${f.ativo ? '' : ' style="opacity:.55"'}>
                <td><a href="${detalhe}?id=${f.id}" class="link-entity" title="Clique para ver, editar ou excluir dados">${esc(f.nome)}</a></td>
                <td><strong>${esc(f.cargoDescricao)}</strong>${f.ativo ? '' : ' (inativo)'}</td>
                <td>RG: ${esc(f.rg)} | CPF: ${esc(f.cpf)}</td>
                <td>${data(f.dataNascimento)}</td>
                <td><strong>${esc(f.matricula)}</strong></td>
                <td>${esc(f.celular)}</td>
                <td>
                    <a href="${detalhe}?id=${f.id}" class="btn-row-edit" title="Editar"><i class="fa-solid fa-pen"></i> Editar</a>
                    <button type="button" class="btn-row-delete" onclick="excluirFuncionarioLista(${f.id}, '${esc(f.nome)}')" title="Excluir Funcionário"><i class="fa-solid fa-trash"></i> Excluir</button>
                </td>
            </tr>`).join('');
        }
        async function carregar() {
            try { lista = await get('/api/funcionarios'); render(); } catch (e) { erro(e); }
        }
        await carregar();
        $('btnBuscarFunc').addEventListener('click', render);
        busca.addEventListener('keyup', render);
    };

    function dadosFuncionario(prefixo) {
        return {
            nome: valor(prefixo + 'Nome'), matricula: valor(prefixo + 'Matricula'), rg: valor(prefixo + 'RG'),
            cpf: valor(prefixo + 'CPF'), dataNascimento: valor(prefixo + 'Nascimento') || null, cargo: valor(prefixo + 'Funcao'),
            rua: valor(prefixo + 'Rua'), bairro: valor(prefixo + 'Bairro'), numero: valor(prefixo + 'Num'), cep: valor(prefixo + 'CEP'),
            cidade: valor(prefixo + 'Cidade'), uf: valor(prefixo + 'UF'), celular: valor(prefixo + 'Celular'), email: valor(prefixo + 'Email'),
            login: valor(prefixo + 'Login'), senha: $(prefixo + 'Senha') ? $(prefixo + 'Senha').value : null
        };
    }

    telas.cadastrarFuncionario = async ({ voltar }) => {
        await iniciar();
        definir('funcCod', 'Novo');
        definir('funcMatricula', 'MAT-' + new Date().getFullYear() + String(Math.floor(Math.random() * 900) + 100));
        $('btnSalvarFuncionario').addEventListener('click', async () => {
            const dados = dadosFuncionario('func');
            if (!dados.nome || !dados.cpf || !dados.rg || !dados.matricula) return alert('Por favor, preencha Nome Completo, RG, CPF e Matrícula.');
            if (!dados.cargo) return alert('Escolha a função do funcionário.');
            if (!dados.login || !dados.senha) return alert('Informe o login e a senha inicial de acesso ao sistema.');
            try {
                const f = await post('/api/funcionarios', dados);
                alert(`Funcionário ${f.nome} (${f.cargoDescricao}) cadastrado com sucesso!\nLogin: ${f.login}`);
                location.href = voltar;
            } catch (e) { erro(e); }
        });
    };

    telas.detalhesFuncionario = async ({ voltar }) => {
        await iniciar();
        const id = new URLSearchParams(location.search).get('id');
        let f;
        try { f = await get(`/api/funcionarios/${id}`); } catch (e) { erro(e); location.href = voltar; return; }
        definir('editFuncCod', f.codigo); definir('editFuncNome', f.nome); definir('editFuncMatricula', f.matricula);
        definir('editFuncRG', f.rg); definir('editFuncCPF', f.cpf); definir('editFuncNascimento', f.dataNascimento);
        definir('editFuncFuncao', f.cargo); definir('editFuncRua', f.rua); definir('editFuncBairro', f.bairro);
        definir('editFuncNum', f.numero); definir('editFuncCEP', f.cep); definir('editFuncCidade', f.cidade);
        definir('editFuncUF', f.uf); definir('editFuncCelular', f.celular); definir('editFuncEmail', f.email);
        definir('editFuncLogin', f.login); definir('editFuncAtivo', f.ativo ? 'true' : 'false');
        $('btnSalvarEdicaoFunc').addEventListener('click', async () => {
            const dados = dadosFuncionario('editFunc');
            if ($('editFuncAtivo')) dados.ativo = $('editFuncAtivo').value === 'true';
            try {
                await put(`/api/funcionarios/${id}`, dados);
                alert('Informações do funcionário atualizadas com sucesso!');
                location.href = voltar;
            } catch (e) { erro(e); }
        });
        $('btnExcluirFunc').addEventListener('click', async () => {
            if (!confirm(`Tem certeza que deseja excluir o funcionário "${f.nome}"?`)) return;
            try {
                const r = await del(`/api/funcionarios/${id}`);
                alert(r.mensagem);
                location.href = voltar;
            } catch (e) { erro(e); }
        });
    };

    // ------------------------------------------------------------------ fornecedores
    telas.consultarFornecedores = async ({ detalhe }) => {
        await iniciar();
        const tbody = $('tabelaFornecedoresBody');
        const busca = $('inputBusca');
        const filtro = $('filterBy');
        let lista = [];
        window.excluirFornecedorLista = async (id, nome) => {
            if (!confirm(`Tem certeza que deseja excluir o fornecedor "${nome}"?`)) return;
            try {
                const r = await del(`/api/fornecedores/${id}`);
                alert(r.mensagem);
                await carregar();
            } catch (e) { erro(e); }
        };
        function render() {
            const termo = busca.value.trim().toLowerCase();
            const crit = filtro ? filtro.value : 'razao';
            const filtrados = lista.filter(f => {
                if (!termo) return true;
                if (crit === 'tipo') return (f.tipoProduto || '').toLowerCase().includes(termo);
                if (crit === 'representante') return (f.representante || '').toLowerCase().includes(termo);
                return (f.razaoSocial || '').toLowerCase().includes(termo);
            });
            if (filtrados.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5">Nenhum fornecedor encontrado.</td></tr>';
                return;
            }
            tbody.innerHTML = filtrados.map(f => `<tr${f.ativo ? '' : ' style="opacity:.55"'}>
                <td><a href="${detalhe}?id=${f.id}" class="link-entity" title="Clique para ver, editar ou excluir dados">${esc(f.razaoSocial)}</a>${f.ativo ? '' : ' (inativo)'}</td>
                <td>${esc(f.tipoProduto)}</td>
                <td>${esc(f.telefone)}</td>
                <td>${esc(f.representante)}</td>
                <td>
                    <a href="${detalhe}?id=${f.id}" class="btn-row-edit" title="Editar"><i class="fa-solid fa-pen"></i> Editar</a>
                    <button type="button" class="btn-row-delete" onclick="excluirFornecedorLista(${f.id}, '${esc(f.razaoSocial)}')" title="Excluir Fornecedor"><i class="fa-solid fa-trash"></i> Excluir</button>
                </td>
            </tr>`).join('');
        }
        async function carregar() {
            try { lista = await get('/api/fornecedores'); render(); } catch (e) { erro(e); }
        }
        await carregar();
        $('btnBuscarForn').addEventListener('click', render);
        busca.addEventListener('keyup', render);
    };

    function dadosFornecedor(p) {
        return {
            razaoSocial: valor(p + 'Razao'), cnpj: valor(p + 'CNPJ'), tipoProduto: valor(p + 'Tipo'), regimeTributario: valor(p + 'Regime'),
            rua: valor(p + 'Rua'), bairro: valor(p + 'Bairro'), numero: valor(p + 'Num'), cep: valor(p + 'CEP'), cidade: valor(p + 'Cidade'),
            uf: valor(p + 'UF'), representante: valor(p + 'Representante'), telefone: valor(p + 'Telefone'), email: valor(p + 'Email'),
            dadosBancarios: valor(p + 'Banco')
        };
    }

    telas.cadastrarFornecedor = async ({ voltar }) => {
        await iniciar();
        definir('fornCod', 'Novo');
        $('btnSalvarFornecedor').addEventListener('click', async () => {
            const dados = dadosFornecedor('forn');
            if (!dados.razaoSocial || !dados.cnpj || !dados.tipoProduto) return alert('Por favor, preencha Razão Social, CNPJ e Tipo de Produto ou Serviço.');
            try {
                const f = await post('/api/fornecedores', dados);
                alert(`Fornecedor ${f.razaoSocial} cadastrado com sucesso!`);
                location.href = voltar;
            } catch (e) { erro(e); }
        });
    };

    telas.detalhesFornecedor = async ({ voltar }) => {
        await iniciar();
        const id = new URLSearchParams(location.search).get('id');
        let f;
        try { f = await get(`/api/fornecedores/${id}`); } catch (e) { erro(e); location.href = voltar; return; }
        definir('editFornCod', f.codigo); definir('editFornRazao', f.razaoSocial); definir('editFornCNPJ', f.cnpj);
        definir('editFornTipo', f.tipoProduto); definir('editFornRegime', f.regimeTributario); definir('editFornRua', f.rua);
        definir('editFornBairro', f.bairro); definir('editFornNum', f.numero); definir('editFornCEP', f.cep);
        definir('editFornCidade', f.cidade); definir('editFornUF', f.uf); definir('editFornRepresentante', f.representante);
        definir('editFornTelefone', f.telefone); definir('editFornEmail', f.email); definir('editFornBanco', f.dadosBancarios);
        $('btnSalvarEdicaoForn').addEventListener('click', async () => {
            const dados = dadosFornecedor('editForn');
            dados.ativo = true;
            try {
                await put(`/api/fornecedores/${id}`, dados);
                alert('Informações do fornecedor atualizadas com sucesso!');
                location.href = voltar;
            } catch (e) { erro(e); }
        });
        $('btnExcluirForn').addEventListener('click', async () => {
            if (!confirm(`Tem certeza que deseja excluir o fornecedor "${f.razaoSocial}"?`)) return;
            try {
                const r = await del(`/api/fornecedores/${id}`);
                alert(r.mensagem);
                location.href = voltar;
            } catch (e) { erro(e); }
        });
    };

    // ------------------------------------------------------------------ estoque (proprietário e gerente)
    telas.estoque = async () => {
        await iniciar();
        const tbody = $('tabelaEstoqueBody');
        const tbodyMov = $('tabelaMovimentacoesBody');
        const titulo = $('movTituloPeca');
        const filtroCat = $('filterCategoria');
        const buscaNome = $('buscaNomeProd');
        const buscaCod = $('buscaCodigoProd');
        let produtos = [];
        let selecionado = null;

        try {
            const cats = await get('/api/categorias');
            filtroCat.innerHTML = '<option value="">Todas as Categorias</option>' + cats.map(c => `<option value="${esc(c.nome)}">${esc(c.nome)}</option>`).join('');
        } catch (e) { erro(e); }

        async function mostrarMovimentacoes(p) {
            selecionado = p.codigo;
            titulo.innerHTML = `<i class="fa-solid fa-clock-rotate-left"></i> Histórico de Movimentações: [${esc(p.codigo)}] ${esc(p.nome)} (Estoque Atual: ${p.estoqueAtual} un. | Reservado: ${p.estoqueReservado} un.)`;
            try {
                const movs = await get(`/api/estoque/movimentacoes?codigo=${encodeURIComponent(p.codigo)}`);
                const cls = { ENTRADA: 'badge-entrada', ESTORNO: 'badge-entrada', SAIDA: 'badge-saida', RESERVA: 'badge-reserva', LIBERACAO: 'badge-reserva' };
                const nome = { ENTRADA: 'Entrada', SAIDA: 'Saída', ESTORNO: 'Estorno', RESERVA: 'Reserva', LIBERACAO: 'Liberação' };
                tbodyMov.innerHTML = movs.length === 0 ? '<tr><td colspan="5">Nenhuma movimentação registrada para esta peça.</td></tr>'
                    : movs.map(m => `<tr>
                        <td>${data(m.data_movimentacao)}</td>
                        <td><span class="${cls[m.tipo]}">${nome[m.tipo]}</span></td>
                        <td><strong>${m.quantidade} un.</strong></td>
                        <td>${esc(m.responsavel)}</td>
                        <td>${esc(m.documento || '-')}${m.fornecedor ? ' (' + esc(m.fornecedor) + ')' : ''}</td>
                    </tr>`).join('');
            } catch (e) { erro(e); }
        }
        function render() {
            const cat = filtroCat.value;
            const nome = buscaNome.value.trim().toLowerCase();
            const cod = buscaCod.value.trim().toLowerCase();
            const lista = produtos.filter(p => (!cat || p.categoria === cat) && (!nome || p.nome.toLowerCase().includes(nome))
                && (!cod || p.codigo.toLowerCase().includes(cod)));
            if (lista.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6">Nenhum produto encontrado com os filtros informados.</td></tr>';
                return;
            }
            if ((cod || !selecionado) && lista.length > 0 && lista[0].codigo !== selecionado) mostrarMovimentacoes(lista[0]);
            tbody.innerHTML = '';
            lista.forEach(p => {
                const tr = document.createElement('tr');
                tr.style.cursor = 'pointer';
                if (p.codigo === selecionado) tr.classList.add('row-selected');
                if (!p.ativo) tr.style.opacity = '.55';
                const cor = p.estoqueDisponivel <= p.estoqueMinimo ? 'color:#d90000; font-weight:bold;' : 'color:#008000; font-weight:bold;';
                tr.innerHTML = `
                    <td><div class="prod-thumb"><i class="fa-solid fa-screwdriver-wrench"></i></div></td>
                    <td><strong>${esc(p.codigo)}</strong></td>
                    <td>${esc(p.nome)}${p.ativo ? '' : ' (inativo)'}</td>
                    <td>${esc(p.categoria)}</td>
                    <td>${moeda(p.precoVenda)}</td>
                    <td style="${cor}">${p.estoqueAtual} un.${p.estoqueReservado ? ` <small>(${p.estoqueReservado} reserv.)</small>` : ''}</td>`;
                tr.addEventListener('click', () => { mostrarMovimentacoes(p); render(); });
                tbody.appendChild(tr);
            });
        }
        try { produtos = await get('/api/produtos'); render(); } catch (e) { erro(e); }
        $('btnBuscarEstoque').addEventListener('click', render);
        filtroCat.addEventListener('change', render);
        buscaNome.addEventListener('keyup', render);
        buscaCod.addEventListener('keyup', render);
    };

    // ------------------------------------------------------------------ relatórios
    telas.relatorios = async () => {
        await iniciar();
        const tbody = $('tabelaRelatoriosBody');
        const fv = $('filtroVendedor'), fp = $('filtroPeca'), fc = $('filtroCliente');
        let espera;
        async function carregar() {
            try {
                const q = new URLSearchParams({ vendedor: fv.value.trim(), peca: fp.value.trim(), cliente: fc.value.trim() });
                const linhas = await get('/api/relatorios/vendas?' + q);
                let itens = 0, total = 0;
                tbody.innerHTML = linhas.length === 0 ? '<tr><td colspan="8">Nenhum histórico encontrado para os filtros informados.</td></tr>'
                    : linhas.map(r => {
                        itens += Number(r.quantidade);
                        if (r.status === 'FINALIZADA') total += Number(r.subtotal);
                        const cls = r.status === 'FINALIZADA' ? 'status-finalizado' : 'status-reservado';
                        const vermelho = r.status === 'CANCELADA' ? ' style="background:#d90000; color:#fff;"' : '';
                        return `<tr>
                            <td><strong>#${String(r.venda_id).padStart(4, '0')}</strong></td>
                            <td>${data(r.data_venda)}</td>
                            <td>${esc(r.vendedor)}</td>
                            <td>${esc(r.cliente)}</td>
                            <td>${esc(r.produto)} (${esc(r.produto_codigo)})</td>
                            <td>${r.quantidade}</td>
                            <td><strong>${moeda(r.subtotal)}</strong></td>
                            <td><span class="${cls}"${vermelho}>${STATUS[r.status] || r.status}</span></td>
                        </tr>`;
                    }).join('');
                if ($('totalRegistros')) $('totalRegistros').textContent = linhas.length;
                if ($('totalItens')) $('totalItens').textContent = `${itens} un. | Vendido (finalizado): ${moeda(total)}`;
            } catch (e) { erro(e); }
        }
        const atrasar = () => { clearTimeout(espera); espera = setTimeout(carregar, 300); };
        await carregar();
        $('btnBuscarRelatorio').addEventListener('click', carregar);
        [fv, fp, fc].forEach(i => i.addEventListener('keyup', atrasar));
        $('btnGerarRelatorio').addEventListener('click', () => window.print());
    };

    // ------------------------------------------------------------------ vendas
    telas.consultarVendas = async () => {
        const u = await iniciar();
        const tbody = $('tabelaVendasBody');
        const filtro = $('filterBy');
        const busca = $('inputBusca');
        let vendas = [];
        // coluna de ações (detalhes / concluir reserva / cancelar)
        const cab = tbody.closest('table').querySelector('thead tr');
        if (cab && !cab.querySelector('.th-acoes')) cab.insertAdjacentHTML('beforeend', '<th class="th-acoes">Ações</th>');

        window.detalheVenda = async id => {
            try {
                const v = await get(`/api/vendas/${id}`);
                const itens = v.itens.map(i => `${i.quantidade}x ${i.produto} (${i.codigo}) = ${moeda(i.subtotal)}${i.observacao ? ' - ' + i.observacao : ''}`).join('\n');
                const pags = v.pagamentos.length === 0 ? 'Aguardando pagamento (reserva)'
                    : v.pagamentos.map(p => `${FORMAS[p.forma]}${p.parcelas > 1 ? ' ' + p.parcelas + 'x' : ''}: ${moeda(p.valor)}${p.troco > 0 ? ' (troco ' + moeda(p.troco) + ')' : ''}`).join('\n');
                alert(`Venda #${String(v.codigo).padStart(4, '0')} - ${STATUS[v.status]}\nData: ${data(v.data_venda)} ${v.hora}\nCliente: ${v.cliente}\nResponsável: ${v.responsavel}\n\nItens:\n${itens}\n\nTotal: ${moeda(v.valor_total)}\nPagamento:\n${pags}` +
                    (v.motivo_cancelamento ? `\n\nMotivo do cancelamento: ${v.motivo_cancelamento}` : ''));
            } catch (e) { erro(e); }
        };
        window.concluirReserva = async (id, total) => {
            const forma = prompt(`Concluir a reserva #${String(id).padStart(4, '0')} (${moeda(total)}).\nForma de pagamento: digite DINHEIRO, PIX, DEBITO ou CREDITO`, 'PIX');
            if (!forma) return;
            const mapa = { DINHEIRO: 'DINHEIRO', PIX: 'PIX', DEBITO: 'CARTAO_DEBITO', CREDITO: 'CARTAO_CREDITO' };
            const f = mapa[forma.trim().toUpperCase().replace('É', 'E')];
            if (!f) return alert('Forma de pagamento inválida.');
            const pagamento = { forma: f, valor: Number(total) };
            if (f === 'CARTAO_CREDITO') pagamento.parcelas = parseInt(prompt('Número de parcelas (1 a 12):', '1'), 10) || 1;
            if (f === 'DINHEIRO') {
                const recebido = numero(prompt('Valor recebido em dinheiro:', moedaSemSimbolo(total)));
                if (recebido < Number(total)) return alert('O valor recebido é menor que o total.');
                pagamento.valorRecebido = recebido;
            }
            try {
                await post(`/api/vendas/${id}/concluir`, { pagamentos: [pagamento] });
                alert('Reserva concluída e venda finalizada!' + (pagamento.valorRecebido ? `\nTroco: ${moeda(pagamento.valorRecebido - total)}` : ''));
                await carregar();
            } catch (e) { erro(e); }
        };
        window.cancelarVenda = async id => {
            const motivo = prompt(`Motivo do cancelamento da venda #${String(id).padStart(4, '0')}:`);
            if (motivo === null) return;
            try {
                await post(`/api/vendas/${id}/cancelar`, { motivo });
                alert('Venda cancelada. Os itens voltaram para o estoque.');
                await carregar();
            } catch (e) { erro(e); }
        };

        function render() {
            const termo = busca.value.trim().toLowerCase();
            const crit = filtro.value;
            const lista = vendas.filter(v => {
                if (!termo) return true;
                if (crit === 'cliente') return v.cliente.toLowerCase().includes(termo);
                if (crit === 'vendedor') return v.responsavel.toLowerCase().includes(termo);
                return String(v.codigo).padStart(4, '0').includes(termo);
            });
            if (lista.length === 0) {
                tbody.innerHTML = '<tr><td colspan="10">Nenhuma venda encontrada.</td></tr>';
                return;
            }
            tbody.innerHTML = lista.map(v => {
                const cls = v.status === 'FINALIZADA' ? 'status-finalizado' : (v.status === 'RESERVADA' ? 'status-reservado' : 'status-cancelado');
                const estilo = v.status === 'CANCELADA' ? ' style="background:#d90000; color:#fff; padding:2px 6px; border-radius:3px;"' : '';
                let acoes = `<button type="button" class="btn-row-edit" onclick="detalheVenda(${v.codigo})"><i class="fa-solid fa-eye"></i></button>`;
                if (v.status === 'RESERVADA' && (u.cargo === 'PROPRIETARIO' || u.cargo === 'VENDEDOR')) {
                    acoes += ` <button type="button" class="btn-row-edit" onclick="concluirReserva(${v.codigo}, ${v.valor_total})" title="Concluir reserva"><i class="fa-solid fa-cash-register"></i> Concluir</button>`;
                }
                if ((v.status === 'FINALIZADA' || v.status === 'RESERVADA') && (u.cargo === 'PROPRIETARIO' || u.cargo === 'GERENTE')) {
                    acoes += ` <button type="button" class="btn-row-delete" onclick="cancelarVenda(${v.codigo})" title="Cancelar venda"><i class="fa-solid fa-ban"></i> Cancelar</button>`;
                }
                return `<tr>
                    <td><strong>${String(v.codigo).padStart(4, '0')}</strong></td>
                    <td>${v.hora}</td>
                    <td>${data(v.data)}</td>
                    <td>${esc(v.cliente)}</td>
                    <td>${esc(v.responsavel)}</td>
                    <td>${v.qtd_produtos}</td>
                    <td><strong>${moeda(v.valor_total)}</strong></td>
                    <td>${esc(v.formas_pagamento)}</td>
                    <td><span class="${cls}"${estilo}>${STATUS[v.status] || v.status}</span></td>
                    <td style="white-space:nowrap">${acoes}</td>
                </tr>`;
            }).join('');
        }
        async function carregar() {
            try { vendas = await get('/api/vendas'); render(); } catch (e) { erro(e); }
        }
        await carregar();
        $('btnBuscarVenda').addEventListener('click', render);
        busca.addEventListener('keyup', render);
    };

    telas.novaVenda = async ({ voltar }) => {
        const u = await iniciar();
        const itens = [];
        let produtos = [], clientes = [], produtoAtual = null;

        definir('codUsuarioInput', String(u.id).padStart(3, '0') + ' - ' + u.login);
        definir('vendedorNomeInput', u.nome);
        definir('numPedidoInput', 'Nova');
        definir('dataVendaInput', new Date().toLocaleDateString('pt-BR'));
        definir('prodPreco', ''); definir('prodTotal', '');
        definir('clienteCodigoInput', ''); definir('clienteVendaNome', '');
        if ($('modalCodUsuario')) $('modalCodUsuario').textContent = u.login;
        if ($('modalNumeroVenda')) $('modalNumeroVenda').textContent = 'Nova';

        try {
            [produtos, clientes] = await Promise.all([get('/api/produtos?ativos=true'), get('/api/clientes')]);
        } catch (e) { return erro(e); }
        preencherDatalist('listaProdutos', produtos.map(p => `${p.codigo} - ${p.nome}`));
        preencherDatalist('listaClientes', clientes.map(c => `${c.codigo} - ${c.nome}`));
        $('prodSearch').setAttribute('list', 'listaProdutos');
        $('clienteCodigoInput').setAttribute('list', 'listaClientes');
        $('clienteVendaNome').setAttribute('list', 'listaClientes');

        function acharProduto(texto) {
            const t = texto.trim().toLowerCase();
            if (!t) return null;
            const cod = t.split(' - ')[0];
            return produtos.find(p => p.codigo.toLowerCase() === cod) || produtos.find(p => p.nome.toLowerCase() === t)
                || produtos.find(p => `${p.codigo} - ${p.nome}`.toLowerCase() === t);
        }
        function atualizarLinhaProduto() {
            produtoAtual = acharProduto($('prodSearch').value);
            const qtd = parseInt($('prodQtd').value, 10) || 1;
            definir('prodPreco', produtoAtual ? moeda(produtoAtual.precoVenda) : '');
            definir('prodTotal', produtoAtual ? moeda(produtoAtual.precoVenda * qtd) : '');
        }
        function acharCliente(texto) {
            const t = texto.trim().toLowerCase();
            if (!t) return null;
            const cod = t.split(' - ')[0].padStart(4, '0');
            return clientes.find(c => c.codigo === cod) || clientes.find(c => c.nome.toLowerCase() === t)
                || clientes.find(c => `${c.codigo} - ${c.nome}`.toLowerCase() === t);
        }
        let cliente = null;
        function escolherCliente(texto) {
            cliente = acharCliente(texto);
            if (cliente) {
                definir('clienteCodigoInput', cliente.codigo);
                definir('clienteVendaNome', cliente.nome);
            }
        }
        $('prodSearch').addEventListener('input', atualizarLinhaProduto);
        $('prodQtd').addEventListener('input', atualizarLinhaProduto);
        $('clienteCodigoInput').addEventListener('change', e => escolherCliente(e.target.value));
        $('clienteVendaNome').addEventListener('change', e => escolherCliente(e.target.value));

        const total = () => itens.reduce((s, i) => s + i.produto.precoVenda * i.quantidade, 0);
        function render() {
            const tbody = $('tbodyItensVenda');
            tbody.innerHTML = itens.length === 0 ? '<tr><td colspan="9">Nenhum produto adicionado. Busque pelo código ou nome e clique em "+ Add".</td></tr>'
                : itens.map((i, n) => {
                    const disp = i.produto.estoqueDisponivel;
                    const cor = disp - i.quantidade < 0 ? '#d90000' : (disp - i.quantidade <= i.produto.estoqueMinimo ? '#d97700' : '#008000');
                    const sub = i.produto.precoVenda * i.quantidade;
                    return `<tr>
                        <td><button type="button" title="Remover item" onclick="removerItemVenda(${n})" style="border:none;background:none;color:#d90000;cursor:pointer;font-weight:bold;">&times;</button> ${String(n + 1).padStart(2, '0')}</td>
                        <td>${esc(i.produto.codigo)}</td>
                        <td>${esc(i.produto.nome)}</td>
                        <td>${i.quantidade}</td>
                        <td style="font-weight:bold; color:${cor};">${String(disp).padStart(2, '0')}</td>
                        <td>${moeda(i.produto.precoVenda)}</td>
                        <td>${moeda(sub)}</td>
                        <td><input type="text" class="input-styled" style="padding:2px 4px;" value="${esc(i.observacao)}" onchange="obsItemVenda(${n}, this.value)" placeholder="Observação"></td>
                        <td>${moeda(sub)}</td>
                    </tr>`;
                }).join('');
            definir('qtdeItensInput', itens.reduce((s, i) => s + i.quantidade, 0));
            definir('valorTotalVendaInput', moeda(total()));
        }
        window.removerItemVenda = n => { itens.splice(n, 1); render(); };
        window.obsItemVenda = (n, texto) => { itens[n].observacao = texto; };
        $('btnAddProd').addEventListener('click', () => {
            atualizarLinhaProduto();
            if (!produtoAtual) return alert('Produto não encontrado. Escolha um produto da lista (código - nome).');
            const qtd = parseInt($('prodQtd').value, 10);
            if (!qtd || qtd < 1) return alert('Informe uma quantidade válida.');
            const existente = itens.find(i => i.produto.id === produtoAtual.id);
            const novaQtd = (existente ? existente.quantidade : 0) + qtd;
            if (novaQtd > produtoAtual.estoqueDisponivel) {
                return alert(`Estoque insuficiente: só há ${produtoAtual.estoqueDisponivel} un. disponíveis de ${produtoAtual.nome}.`);
            }
            if (existente) existente.quantidade = novaQtd; else itens.push({ produto: produtoAtual, quantidade: qtd, observacao: '' });
            definir('prodSearch', ''); definir('prodQtd', 1); definir('prodPreco', ''); definir('prodTotal', '');
            produtoAtual = null;
            render();
        });
        render();

        function validarBasico() {
            if (!cliente) escolherCliente(valor('clienteCodigoInput') || valor('clienteVendaNome'));
            if (!cliente) { alert('Escolha o cliente (código ou nome da lista).'); return false; }
            if (itens.length === 0) { alert('Adicione pelo menos um produto.'); return false; }
            return true;
        }
        const corpoVenda = () => ({
            clienteId: cliente.id,
            itens: itens.map(i => ({ produtoId: i.produto.id, quantidade: i.quantidade, observacao: i.observacao || null })),
            numeroCupom: valor('cupomInput') || null, numeroNf: valor('nfInput') || null
        });

        // SALVAR = venda reservada (sem pagamento, aguardando o caixa)
        $('btnSalvarVenda').addEventListener('click', async () => {
            if (!validarBasico()) return;
            if (!confirm('Salvar sem pagamento? A venda fica RESERVADA (os produtos são separados no estoque) até ser concluída no caixa.')) return;
            try {
                const r = await post('/api/vendas', { ...corpoVenda(), reserva: true });
                alert(`Venda #${String(r.venda_id).padStart(4, '0')} salva como RESERVADA. Total: ${moeda(r.valor_total)}`);
                location.href = voltar;
            } catch (e) { erro(e); }
        });

        // CAIXA = forma de pagamento
        const modal = $('modalCaixa');
        function calcularPagamento() {
            const t = total();
            const pix = numero(valor('payPix'));
            const cartao = numero(valor('payCartao'));
            const recebido = numero(valor('payDinheiro'));
            const restante = Math.round((t - pix - cartao) * 100) / 100;
            definir('trocoDinheiro', restante > 0 && recebido > restante ? moedaSemSimbolo(recebido - restante) : '');
            const parcelas = parseInt(valor('parcelasCartao'), 10) || 1;
            definir('valorParcela', cartao > 0 ? moedaSemSimbolo(cartao / parcelas) : '');
            return { t, pix, cartao, recebido, restante, parcelas };
        }
        ['payPix', 'payCartao', 'payDinheiro', 'parcelasCartao'].forEach(id => $(id) && $(id).addEventListener('input', calcularPagamento));
        $('btnAbrirCaixa').addEventListener('click', () => {
            if (!validarBasico()) return;
            $('modalClienteNome').textContent = cliente.nome;
            $('modalValorTotal').textContent = moeda(total());
            ['payPix', 'payCartao', 'payDinheiro'].forEach(id => definir(id, ''));
            definir('payDinheiro', moedaSemSimbolo(total()));
            calcularPagamento();
            modal.classList.add('active');
        });
        $('btnFecharCaixa').addEventListener('click', () => modal.classList.remove('active'));
        $('btnConfirmarPagamento').addEventListener('click', async () => {
            const c = calcularPagamento();
            if (c.restante < 0) return alert('Os valores de PIX e cartão passam do total da venda.');
            const pagamentos = [];
            if (c.pix > 0) pagamentos.push({ forma: 'PIX', valor: c.pix });
            if (c.cartao > 0) {
                const credito = (document.querySelector('input[name="tipoCartao"]:checked') || {}).value === 'credito';
                pagamentos.push({ forma: credito ? 'CARTAO_CREDITO' : 'CARTAO_DEBITO', valor: c.cartao, parcelas: credito ? c.parcelas : 1 });
            }
            if (c.restante > 0) {
                if (c.recebido < c.restante) return alert(`Falta pagar ${moeda(c.restante)}. Informe o valor em dinheiro, PIX ou cartão.`);
                pagamentos.push({ forma: 'DINHEIRO', valor: c.restante, valorRecebido: c.recebido });
            }
            try {
                const r = await post('/api/vendas', { ...corpoVenda(), reserva: false, pagamentos });
                const troco = c.restante > 0 && c.recebido > c.restante ? `\nTroco: ${moeda(c.recebido - c.restante)}` : '';
                alert(`Pagamento registrado! Venda #${String(r.venda_id).padStart(4, '0')} finalizada. Total: ${moeda(r.valor_total)}${troco}`);
                location.href = voltar;
            } catch (e) { erro(e); }
        });
    };

    // ------------------------------------------------------------------ estoquista: consultar estoque + entrada
    telas.consultarEstoque = async () => {
        await iniciar();
        const container = $('resultadoEstoqueContainer');
        let produtos = [], fornecedores = [], locais = [];
        try {
            [produtos, fornecedores, locais] = await Promise.all([get('/api/produtos'), get('/api/fornecedores?ativos=true'), get('/api/estoque/localizacoes')]);
        } catch (e) { return erro(e); }
        preencherDatalist('listaCodigos', produtos.map(p => p.codigo));
        $('codigoProduto').setAttribute('list', 'listaCodigos');
        // os filtros "status" e "corredor" listam produtos; o código abre o produto
        const filtro = $('filtrarPor');
        if (filtro) {
            filtro.innerHTML = '<option value="">Selecione...</option><option value="codigo">Código (ID)</option>' +
                '<option value="baixo">Estoque baixo</option><option value="todos">Todos os produtos</option>';
        }

        async function abrir(codigo) {
            let p;
            try { p = await get(`/api/produtos/codigo/${encodeURIComponent(codigo)}`); } catch (e) {
                container.innerHTML = `<div class="aviso-nao-encontrado">${esc(e.message)}</div>`;
                return;
            }
            const baixo = p.estoqueDisponivel <= p.estoqueMinimo;
            const locs = (p.localizacoes || []).map(l => `<tr><td><strong>${esc(l.galpao)}</strong></td><td>${esc(l.corredor)}</td><td>${esc(l.prateleira)}</td><td>${esc(l.box)}</td><td><strong>${l.quantidade} un</strong></td></tr>`).join('')
                || '<tr><td colspan="5">Nenhuma locação registrada.</td></tr>';
            container.innerHTML = `
                <div class="produto-info-card">
                    <div class="produto-info-header">
                        <h2>[${esc(p.codigo)}] ${esc(p.nome)}</h2>
                        <span class="status-pill ${baixo ? 'status-baixo' : 'status-ativo'}">${!p.ativo ? 'Inativo' : (baixo ? 'Estoque Baixo' : 'Ativo')}</span>
                    </div>
                    <div class="produto-detalhes-grid">
                        <div class="detalhe-item"><span>Categoria</span><strong>${esc(p.categoria)}</strong></div>
                        <div class="detalhe-item"><span>Marca</span><strong>${esc(p.marca || 'Não informada')}</strong></div>
                        <div class="detalhe-item"><span>SKU / EAN</span><strong>${esc(p.sku || '-')} / ${esc(p.ean || '-')}</strong></div>
                        <div class="detalhe-item"><span>Preço de Venda</span><strong>${moeda(p.precoVenda)}</strong></div>
                        <div class="detalhe-item"><span>Quantidade Total em Estoque</span><strong>${p.estoqueAtual} unidades</strong></div>
                        <div class="detalhe-item"><span>Reservado / Disponível</span><strong>${p.estoqueReservado} / ${p.estoqueDisponivel} unidades</strong></div>
                        <div class="detalhe-item"><span>Estoque Mínimo</span><strong>${p.estoqueMinimo} unidades</strong></div>
                        <div class="detalhe-item"><span>Fornecedores</span><strong>${esc((p.fornecedores || []).join(', ') || '-')}</strong></div>
                    </div>
                </div>
                <div class="locacoes-box">
                    <h3><i class="fa-solid fa-warehouse"></i> Locações do Produto no Estoque</h3>
                    <table class="locacoes-table">
                        <thead><tr><th>Galpão / Setor</th><th>Corredor</th><th>Prateleira</th><th>Box / Posição</th><th>Quantidade Alocada</th></tr></thead>
                        <tbody>${locs}</tbody>
                    </table>
                </div>
                <div class="locacoes-box">
                    <h3><i class="fa-solid fa-truck-ramp-box"></i> Registrar entrada de mercadoria</h3>
                    <div style="display:flex; flex-wrap:wrap; gap:14px; align-items:flex-end; padding:12px; font-size:13px; font-weight:bold;">
                        <label style="display:flex; flex-direction:column; gap:4px;">Quantidade<input type="number" id="entQtd" min="1" value="1" style="width:90px; padding:5px;"></label>
                        <label style="display:flex; flex-direction:column; gap:4px;">Preço de custo (R$)<input type="text" id="entCusto" value="${moedaSemSimbolo(p.precoCusto)}" style="width:120px; padding:5px;"></label>
                        <label style="display:flex; flex-direction:column; gap:4px;">Fornecedor<select id="entFornecedor" style="padding:5px;"><option value="">(nenhum)</option>${fornecedores.map(f => `<option value="${f.id}">${esc(f.razaoSocial)}</option>`).join('')}</select></label>
                        <label style="display:flex; flex-direction:column; gap:4px;">Local<select id="entLocal" style="padding:5px;"><option value="">(nenhum)</option>${locais.map(l => `<option value="${l.localizacaoId}">${esc(l.galpao)} ${esc(l.corredor)} ${esc(l.prateleira)} ${esc(l.box)}</option>`).join('')}</select></label>
                        <label style="display:flex; flex-direction:column; gap:4px;">Nota fiscal / documento<input type="text" id="entDoc" placeholder="NF-0000" style="width:150px; padding:5px;"></label>
                        <button type="button" class="btn-buscar" id="btnEntrada">Registrar entrada</button>
                    </div>
                </div>`;
            $('btnEntrada').addEventListener('click', async () => {
                try {
                    const r = await post('/api/estoque/entradas', {
                        produtoId: p.id, quantidade: parseInt(valor('entQtd'), 10), precoCusto: numeroOuNulo(valor('entCusto')),
                        fornecedorId: valor('entFornecedor') ? Number(valor('entFornecedor')) : null,
                        localizacaoId: valor('entLocal') ? Number(valor('entLocal')) : null, documento: valor('entDoc')
                    });
                    alert(`Entrada registrada! Estoque atual: ${r.estoque_atual} un.`);
                    abrir(p.codigo);
                } catch (e) { erro(e); }
            });
        }
        async function listar(apenasBaixo) {
            let lista = produtos;
            if (apenasBaixo) {
                try { const baixos = await get('/api/estoque/baixo'); lista = produtos.filter(p => baixos.some(b => b.codigo === p.codigo)); } catch (e) { return erro(e); }
            }
            container.innerHTML = `<div class="locacoes-box"><h3>${apenasBaixo ? 'Produtos com estoque baixo' : 'Todos os produtos'}</h3>
                <table class="locacoes-table"><thead><tr><th>Código</th><th>Produto</th><th>Categoria</th><th>Estoque</th><th>Mínimo</th></tr></thead><tbody>
                ${lista.map(p => `<tr style="cursor:pointer" onclick="document.getElementById('codigoProduto').value='${esc(p.codigo)}'; document.getElementById('btnBuscar').click();">
                    <td><strong>${esc(p.codigo)}</strong></td><td>${esc(p.nome)}</td><td>${esc(p.categoria)}</td><td>${p.estoqueDisponivel} un</td><td>${p.estoqueMinimo} un</td></tr>`).join('')
                    || '<tr><td colspan="5">Nenhum produto.</td></tr>'}
                </tbody></table></div>`;
        }
        function buscar() {
            const codigo = valor('codigoProduto');
            if (codigo) return abrir(codigo);
            if (filtro && filtro.value === 'baixo') return listar(true);
            if (filtro && filtro.value === 'todos') return listar(false);
            container.innerHTML = '';
        }
        $('btnBuscar').addEventListener('click', buscar);
        $('codigoProduto').addEventListener('keypress', e => { if (e.key === 'Enter') buscar(); });
        if (filtro) filtro.addEventListener('change', buscar);
        const cod = new URLSearchParams(location.search).get('codigo');
        if (cod) { definir('codigoProduto', cod); abrir(cod); }
    };

    // ------------------------------------------------------------------ estoquista: cadastrar produto
    telas.cadastrarProduto = async () => {
        await iniciar();
        let status = 'Ativo', produtoId = null, fornecedores = [], locais = [];
        window.setStatus = s => {
            status = s;
            $('btnAtivo').classList.toggle('active', s === 'Ativo');
            $('btnInativo').classList.toggle('active', s !== 'Ativo');
        };
        try {
            const [cats, marcas, forns, locs] = await Promise.all([get('/api/categorias'), get('/api/marcas'), get('/api/fornecedores?ativos=true'), get('/api/estoque/localizacoes')]);
            fornecedores = forns; locais = locs;
            preencherDatalist('listaCategorias', cats.map(c => c.nome));
            preencherDatalist('listaMarcas', marcas.map(m => m.nome));
            preencherDatalist('listaFornecedores', forns.map(f => f.razaoSocial));
            $('categoria').setAttribute('list', 'listaCategorias');
            $('marca').setAttribute('list', 'listaMarcas');
            $('fornecedor').setAttribute('list', 'listaFornecedores');
        } catch (e) { return erro(e); }

        // campo de localização (onde a entrada inicial será guardada)
        const estoqueMin = $('estoqueMinimo');
        if (estoqueMin && !$('localizacao')) {
            const grupo = estoqueMin.parentElement;
            const novo = grupo.cloneNode(false);
            novo.innerHTML = `<label for="localizacao">Local no estoque</label><select id="localizacao" class="${estoqueMin.className}" style="min-width:220px">
                <option value="">(nenhum)</option>${locais.map(l => `<option value="${l.localizacaoId}">${esc(l.galpao)} ${esc(l.corredor)} ${esc(l.prateleira)} ${esc(l.box)}</option>`).join('')}</select>`;
            grupo.insertAdjacentElement('afterend', novo);
        }

        window.adicionarRapido = async tipo => {
            const nome = prompt(`Digite o nome da nova ${tipo}:`);
            if (!nome || !nome.trim()) return;
            if (tipo === 'Fornecedor') return alert('Fornecedores são cadastrados pelo Gerente ou Proprietário. Escolha um da lista.');
            try {
                const r = await post(tipo === 'Categoria' ? '/api/categorias' : '/api/marcas', { nome: nome.trim() });
                const lista = $(tipo === 'Categoria' ? 'listaCategorias' : 'listaMarcas');
                lista.insertAdjacentHTML('beforeend', `<option value="${esc(r.nome)}"></option>`);
                definir(tipo.toLowerCase(), r.nome);
            } catch (e) { erro(e); }
        };

        // ao digitar um código que já existe, carrega o produto para edição
        $('codigo').addEventListener('change', async () => {
            const codigo = valor('codigo');
            if (!codigo) return;
            try {
                const p = await get(`/api/produtos/codigo/${encodeURIComponent(codigo)}`);
                if (!confirm(`O produto ${p.codigo} (${p.nome}) já existe. Carregar os dados para alterar?`)) return;
                produtoId = p.id;
                definir('categoria', p.categoria); definir('nome', p.nome); definir('marca', p.marca); definir('ean', p.ean);
                definir('sku', p.sku); definir('ncm', p.ncm); definir('precoVenda', moedaSemSimbolo(p.precoVenda));
                definir('precoCusto', moedaSemSimbolo(p.precoCusto)); definir('unidadeCompra', p.unidadeCompra);
                definir('unidadeVenda', p.unidadeVenda); definir('descricao', p.descricao); definir('largura', p.larguraCm);
                definir('altura', p.alturaCm); definir('comprimento', p.comprimentoCm); definir('estoqueAtual', p.estoqueAtual);
                definir('estoqueMinimo', p.estoqueMinimo); definir('fornecedor', (p.fornecedores || [])[0] || '');
                $('estoqueAtual').disabled = true;
                $('estoqueAtual').title = 'Para mudar o estoque, registre uma entrada na tela Estoque.';
                window.setStatus(p.ativo ? 'Ativo' : 'Inativo');
            } catch (e) { produtoId = null; $('estoqueAtual').disabled = false; }
        });

        $('btnSalvarProduto').addEventListener('click', async () => {
            const codigo = valor('codigo'), nome = valor('nome');
            if (!codigo || !nome) return alert('Por favor, preencha ao menos o Código e o Nome do produto.');
            const fornNome = valor('fornecedor');
            const forn = fornecedores.find(f => f.razaoSocial.toLowerCase() === fornNome.toLowerCase());
            if (fornNome && !forn) return alert('Fornecedor não encontrado. Escolha um fornecedor da lista.');
            const dados = {
                codigo, nome, categoria: valor('categoria'), marca: valor('marca'), ean: valor('ean'), sku: valor('sku'), ncm: valor('ncm'),
                precoVenda: numeroOuNulo(valor('precoVenda')), precoCusto: numeroOuNulo(valor('precoCusto')),
                unidadeCompra: valor('unidadeCompra'), unidadeVenda: valor('unidadeVenda'), descricao: valor('descricao'),
                larguraCm: numeroOuNulo(valor('largura')), alturaCm: numeroOuNulo(valor('altura')), comprimentoCm: numeroOuNulo(valor('comprimento')),
                estoqueMinimo: parseInt(valor('estoqueMinimo'), 10) || 0, ativo: status === 'Ativo',
                estoqueInicial: produtoId ? null : (parseInt(valor('estoqueAtual'), 10) || 0),
                fornecedorId: forn ? forn.id : null, localizacaoId: valor('localizacao') ? Number(valor('localizacao')) : null
            };
            try {
                const p = produtoId ? await put(`/api/produtos/${produtoId}`, dados) : await post('/api/produtos', dados);
                alert(`Produto "${p.nome}" (Código: ${p.codigo}) ${produtoId ? 'alterado' : 'cadastrado'} com sucesso! Estoque atual: ${p.estoqueAtual} un.`);
                $('formCadastrarProduto').reset();
                $('estoqueAtual').disabled = false;
                produtoId = null;
                if ($('imagePreview')) $('imagePreview').innerHTML = '<i class="fa-solid fa-image"></i>';
                window.setStatus('Ativo');
            } catch (e) { erro(e); }
        });
        const inputImagem = $('inputImagem');
        if (inputImagem) {
            inputImagem.addEventListener('change', e => {
                const arquivo = e.target.files[0];
                if (!arquivo) return;
                const leitor = new FileReader();
                leitor.onload = ev => { $('imagePreview').innerHTML = `<img src="${ev.target.result}" alt="Pré-visualização">`; };
                leitor.readAsDataURL(arquivo);
            });
        }
    };

    // ------------------------------------------------------------------ proprietário: configurações (backup e restore)
    telas.configuracoes = async () => {
        await iniciar();
        const tbody = $('tabelaBackupsBody');
        async function carregar() {
            try {
                const lista = await get('/api/sistema/backups');
                tbody.innerHTML = lista.length === 0 ? '<tr><td colspan="4">Nenhum backup feito ainda.</td></tr>'
                    : lista.map((b, n) => `<tr>
                        <td>${data(b.data)} ${hora(b.data)}</td>
                        <td style="word-break:break-all">${esc(b.arquivo)}</td>
                        <td>${b.tamanho_mb} MB</td>
                        <td><button type="button" class="btn-row-delete" onclick="restaurarBackup(${n})"><i class="fa-solid fa-rotate-left"></i> Restaurar</button></td>
                    </tr>`).join('');
                window._backups = lista;
            } catch (e) { erro(e); }
        }
        window.restaurarBackup = async n => {
            const b = window._backups[n];
            if (!confirm(`Restaurar o banco para ${data(b.data)} ${hora(b.data)}?\n\nTUDO o que foi gravado depois desse backup será perdido.`)) return;
            try {
                const r = await post('/api/sistema/restore', { arquivo: b.arquivo });
                alert(r.mensagem + '\nFaça login novamente.');
                location.href = '../login.html';
            } catch (e) { erro(e); }
        };
        $('btnFazerBackup').addEventListener('click', async () => {
            try {
                const r = await post('/api/sistema/backup');
                alert('Backup gerado com sucesso:\n' + r.arquivo_backup);
                await carregar();
            } catch (e) { erro(e); }
        });
        await carregar();
    };

    return { api, get, post, put, del, moeda, numero, data, esc, iniciar, telas, usuario: () => usuario, CARGOS };
})();
