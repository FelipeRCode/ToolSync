/**
 * ARQUIVO: telaconsultarfornecedoresp.js
 * DESCRIÇÃO: Tela de listagem e pesquisa de fornecedores cadastrados (perfil Proprietário).
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.consultarFornecedores({ detalhe: 'teladetalhesfornecedorp.html' }));
