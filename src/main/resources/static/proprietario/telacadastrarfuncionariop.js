/**
 * ARQUIVO: telacadastrarfuncionariop.js
 * DESCRIÇÃO: Formulário e lógica para cadastro de novos funcionários (perfil Proprietário).
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.cadastrarFuncionario({ voltar: 'telaconsultarfuncionariosp.html' }));
