/**
 * ARQUIVO: teladetalhesfornecedorp.js
 * DESCRIÇÃO: Tela de visualização, edição e exclusão detalhada de fornecedor (perfil Proprietário).
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.detalhesFornecedor({ voltar: 'telaconsultarfornecedoresp.html' }));
