/**
 * ARQUIVO: login.JS
 * DESCRIÇÃO: Tela de autenticação do ToolSync. Envia usuário e senha para a API (/api/auth/login),
 * que confere o hash BCrypt no SQL Server, e abre o painel do perfil do usuário.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.login());
