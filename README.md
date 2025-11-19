# SkillMatch API 🚀

API RESTful desenvolvida em Java com Quarkus para a plataforma SkillMatch. O sistema foca em requalificação profissional, gerenciando trilhas de aprendizado, progresso de cursos e gamificação para engajar usuários em transição de carreira.

## 📋 Sumário

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Endpoints da API](#-endpoints-da-api)
- [Autores](#-autores)

## 💡 Sobre o Projeto

O SkillMatch é uma solução para apoiar pessoas em processo de requalificação e recolocação profissional. Através de uma jornada estruturada, o usuário recebe recomendações de carreiras compatíveis, segue uma trilha de skills e cursos, e é recompensado através de um sistema de gamificação com XP e Ranking.

Esta API serve como o núcleo do backend, gerenciando dados de usuários, progressão e regras de negócio, conectando-se a um banco de dados Oracle.

## ✨ Funcionalidades

- **Autenticação & Usuários**: Registro, login e gestão de perfil.
- **Jornada de Carreira**: Seleção de carreira e acompanhamento de trilhas de aprendizado.
- **Gestão de Cursos**: Iniciar cursos, atualizar progresso e marcar conclusão.
- **Gamificação**:
  - Sistema de XP por curso concluído.
  - Ranking mensal de usuários.
  - Dashboard consolidado com estatísticas.
- **Integração de Dados**: Persistência robusta de relacionamentos entre Carreiras, Skills e Cursos.

## 🛠 Tecnologias

- **Java 17**: Linguagem base.
- **Quarkus 3.x**: Framework Java Supersônico e Subatômico.
- **Hibernate ORM com Panache**: Implementação do padrão Active Record para persistência simplificada.
- **Oracle Database**: Banco de dados relacional.
- **Maven**: Gerenciamento de dependências e build.
- **Docker (Opcional)**: Para containerização da aplicação.

## 📡 Endpoints da API

### 🔐 Autenticação

- `POST /auth/login` - Autenticar usuário (Retorna dados do usuário)
- `POST /auth/register` - Registrar novo usuário

### 👤 Usuários & Dashboard

- `GET /usuarios/{id}` - Buscar dados do perfil
- `PUT /usuarios/{id}` - Atualizar perfil
- `GET /usuarios/{id}/dashboard` - Retorna XP, progresso atual e resumos para a home
- `GET /usuarios/{id}/estatisticas` - Estatísticas de aprendizado

### 🚀 Carreiras & Skills

- `GET /carreiras` - Listar todas as carreiras
- `GET /carreiras/{id}` - Detalhes de uma carreira
- `GET /carreiras/{id}/skills` - Listar a trilha de skills da carreira
- `POST /usuarios/{id}/carreira` - Selecionar/Matricular usuário em uma carreira
- `GET /usuarios/{id}/carreira-atual` - Buscar a carreira ativa do usuário

### 📚 Cursos & Progresso

- `GET /skills/{id}/cursos` - Listar cursos de uma skill específica
- `GET /usuarios/{id}/cursos` - Listar cursos matriculados do usuário
- `POST /cursos/{id}/iniciar` - Iniciar um curso (Status: "Em andamento")
- `PUT /cursos/{id}/progresso` - Atualizar % de progresso
- `PUT /cursos/{id}/concluir` - Finalizar curso (Gera XP)

### 🏆 Ranking & XP

- `GET /ranking/{mes}` - Buscar ranking mensal (ex: 2024-01)
- `GET /usuarios/{id}/ranking` - Posição individual do usuário
- `POST /usuarios/{id}/xp` - Adicionar XP manualmente (bônus)

## 👥 Autores

| Nome | RM |
|------|-----|
| Lucas Grillo Alcântara | RM 561413 |
| Augusto Buguas Rodrigues | RM 563858 |
| Pietro Abrahamian | RM 561469 |

Feito com ❤️ pela Equipe SkillMatch para um futuro profissional mais acessível.