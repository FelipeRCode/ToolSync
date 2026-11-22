/**
 * ARQUIVO: telacadastrarfornecedorg.js
 * DESCRIÇÃO: Formulário e lógica para cadastro de novos fornecedores pelo Gerente.
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.cadastrarFornecedor({ voltar: 'telaconsultarfornecedoresg.html' }));
