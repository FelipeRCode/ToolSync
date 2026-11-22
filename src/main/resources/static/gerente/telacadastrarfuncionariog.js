/**
 * ARQUIVO: telacadastrarfuncionariog.js
 * DESCRIÇÃO: Formulário e lógica para cadastro de novos funcionários e geração de matrícula (perfil Gerente).
 * Os dados vêm da API do back-end (Spring Boot + SQL Server); a lógica fica em ../js/toolsync.js.
 */
document.addEventListener('DOMContentLoaded', () => ToolSync.telas.cadastrarFuncionario({ voltar: 'telaconsultarfuncionariosg.html' }));
