/**
 * ARQUIVO: telainicialg.JS
 * DESCRIÇÃO: Painel principal (Dashboard) do Gerente com indicadores gerenciais, gráficos e atalhos administrativos.
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.dashboard());
