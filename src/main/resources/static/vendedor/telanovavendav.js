/**
 * ARQUIVO: telanovavendav.js
 * DESCRIÇÃO: Tela de PDV (Ponto de Venda) para registro de vendas, adição de itens ao carrinho e baixa de estoque (perfil Vendedor).
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.novaVenda({ voltar: 'telaconsultarvendasv.html' }));
