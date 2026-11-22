/**
 * ARQUIVO: teladetalhesfuncionariop.js
 * DESCRIÇÃO: Tela de visualização, edição e exclusão detalhada de funcionário (perfil Proprietário).
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.detalhesFuncionario({ voltar: 'telaconsultarfuncionariosp.html' }));
