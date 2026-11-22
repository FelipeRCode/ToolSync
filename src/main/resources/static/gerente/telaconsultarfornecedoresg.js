/**
 * ARQUIVO: telaconsultarfornecedoresg.js
 * DESCRIÇÃO: Tela de listagem, pesquisa e filtro de fornecedores cadastrados (perfil Gerente).
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.consultarFornecedores({ detalhe: 'teladetalhesfornecedorg.html' }));
