package br.com.skillmatch.service;

import br.com.skillmatch.dto.*;
import br.com.skillmatch.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class UserService {

    @Inject
    EntityManager em;

    public Usuario getUsuario(Long id) {
        Usuario usuario = Usuario.findById(id);
        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }
        return usuario;
    }

    @Transactional
    public Usuario updateUsuario(Long id, Usuario dados) {
        Usuario usuario = getUsuario(id);
        usuario.nome = dados.nome;
        usuario.dataNascimento = dados.dataNascimento;
        return usuario;
    }

    public UsuarioCarreira getCarreiraAtual(Long usuarioId) {
        UsuarioCarreira uc = UsuarioCarreira.find("usuario.id", usuarioId).firstResult();
        if (uc == null) {
            throw new RuntimeException("Usuário sem carreira ativa");
        }
        return uc;
    }

    @Transactional
    public UsuarioCarreira selecionarCarreira(Long usuarioId, Long carreiraId) {
        // Verificar se usuário existe
        getUsuario(usuarioId);

        Carreira carreira = Carreira.findById(carreiraId);
        if (carreira == null) {
            throw new RuntimeException("Carreira não encontrada");
        }

        // Remove carreira atual
        em.createNativeQuery("DELETE FROM TB_USUARIO_CARREIRA WHERE id_usuario = ?1")
                .setParameter(1, usuarioId)
                .executeUpdate();

        // RESETA O PROGRESSO DE TODOS OS CURSOS DO USUÁRIO
        List<UsuarioCurso> cursosUsuario = UsuarioCurso.list("usuario.id", usuarioId);
        for (UsuarioCurso curso : cursosUsuario) {
            curso.progresso = 0.0;
            curso.status = "Pendente"; // Ou "Não iniciado" dependendo da sua lógica
        }

        // Insere nova carreira
        em.createNativeQuery(
                "INSERT INTO TB_USUARIO_CARREIRA (id_usuario, id_carreira, id_status_jornada, progresso_percentual, xp_total) "
                        +
                        "VALUES (?1, ?2, ?3, ?4, ?5)")
                .setParameter(1, usuarioId)
                .setParameter(2, carreiraId)
                .setParameter(3, 2L) // Em andamento
                .setParameter(4, 0.0)
                .setParameter(5, 0L)
                .executeUpdate();

        // Retorna a nova carreira do usuário
        UsuarioCurso.flush(); // Garante que as atualizações dos cursos sejam persistidas
        UsuarioCarreira novaCarreira = UsuarioCarreira.find("usuario.id", usuarioId).firstResult();
        if (novaCarreira == null) {
            throw new RuntimeException("Erro ao selecionar carreira");
        }

        return novaCarreira;
    }

    @Transactional
    public UsuarioCarreira atualizarProgressoCarreira(Long usuarioId, Double percentual) {
        UsuarioCarreira uc = getCarreiraAtual(usuarioId);
        uc.progresso = percentual;
        return uc;
    }

    public List<UsuarioCurso> getCursosUsuario(Long usuarioId) {
        return UsuarioCurso.list("usuario.id", usuarioId);
    }

    @Transactional
    public void adicionarXp(Long usuarioId, Long quantidadeXp) {
        UsuarioCarreira uc = UsuarioCarreira.find("usuario.id", usuarioId).firstResult();
        if (uc != null) {
            uc.xp = (uc.xp != null ? uc.xp : 0L) + quantidadeXp;
        }
    }

    public DashboardDTO getDashboard(Long usuarioId) {
        Usuario usuario = getUsuario(usuarioId);
        UsuarioCarreira uc = UsuarioCarreira.find("usuario.id", usuarioId).firstResult();
        long cursosConcluidos = UsuarioCurso.count("usuario.id = ?1 AND status = 'Concluído'", usuarioId);

        return new DashboardDTO(
                usuario.nome,
                uc != null ? uc.carreira.nome : "Nenhuma",
                uc != null ? uc.progresso : 0.0,
                uc != null ? uc.xp : 0L,
                cursosConcluidos);
    }

    public Map<String, Object> getEstatisticas(Long usuarioId) {
        return Map.of(
                "totalCursosIniciados", UsuarioCurso.count("usuario.id", usuarioId),
                "totalCursosConcluidos", UsuarioCurso.count("usuario.id = ?1 AND status = 'Concluído'", usuarioId));
    }
}