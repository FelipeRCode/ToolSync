/**
 * ARQUIVO: telaconsultarfuncionariosg.js
 * DESCRIÇÃO: Tela de listagem, pesquisa, edição e exclusão de Vendedores e Estoquista (perfil Gerente).
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.consultarFuncionarios({ detalhe: 'teladetalhesfuncionariog.html' }));
