/**
 * ARQUIVO: telaconsultarfuncionariosp.js
 * DESCRIÇÃO: Tela de listagem e pesquisa de funcionários cadastrados (perfil Proprietário).
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.consultarFuncionarios({ detalhe: 'teladetalhesfuncionariop.html' }));
