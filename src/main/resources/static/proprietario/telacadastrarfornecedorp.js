/**
 * ARQUIVO: telacadastrarfornecedorp.js
 * DESCRIÇÃO: Formulário e lógica para cadastro de novos fornecedores (perfil Proprietário).
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.cadastrarFornecedor({ voltar: 'telaconsultarfornecedoresp.html' }));
