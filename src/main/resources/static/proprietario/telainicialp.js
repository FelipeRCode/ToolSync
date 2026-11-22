/**
 * ARQUIVO: telainicialp.js
 * DESCRIÇÃO: Painel principal (Dashboard) do Proprietário com visão geral financeira, operacional e atalhos completos.
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.dashboard());
