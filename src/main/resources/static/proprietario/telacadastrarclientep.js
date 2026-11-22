/**
 * ARQUIVO: telacadastrarclientep.js
 * DESCRIÇÃO: Formulário e lógica para cadastro de novos clientes (perfil Proprietário).
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.cadastrarCliente({ voltar: 'telaconsultarclientesp.html' }));
