/**
 * ARQUIVO: telainiciale.JS
 * DESCRIÇÃO: Painel principal (Dashboard) do Estoquista com resumo de alertas de estoque baixo e acesso rápido às funções.
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.dashboard());
