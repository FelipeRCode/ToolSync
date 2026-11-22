/**
 * ARQUIVO: teladetalhesfornecedorg.js
 * DESCRIÇÃO: Tela de visualização, edição e exclusão dos dados detalhados de um fornecedor (perfil Gerente).
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.detalhesFornecedor({ voltar: 'telaconsultarfornecedoresg.html' }));
