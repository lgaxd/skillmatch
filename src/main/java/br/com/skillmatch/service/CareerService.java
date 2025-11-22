package br.com.skillmatch.service;
import br.com.skillmatch.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CareerService {

    public List<Carreira> listarCarreiras() {
        return Carreira.listAll();
    }

    public Carreira detalheCarreira(Long id) {
        Carreira carreira = Carreira.findById(id);
        if (carreira == null) {
            throw new RuntimeException("Carreira não encontrada");
        }
        return carreira;
    }

    public List<CarreiraSkill> listarSkillsDaCarreira(Long idCarreira) {
        return CarreiraSkill.list("carreira.id = ?1 ORDER BY ordem", idCarreira);
    }

    public List<Curso> listarCursosDaSkill(Long idSkill) {
        return Curso.list("skill.id", idSkill);
    }

    @Transactional
    public UsuarioCurso iniciarCurso(Long idCurso, Long idUsuario) {
        if (UsuarioCurso.count("curso.id = ?1 AND usuario.id = ?2", idCurso, idUsuario) > 0) {
            throw new RuntimeException("Curso já iniciado");
        }

        Curso curso = Curso.findById(idCurso);
        Usuario usuario = Usuario.findById(idUsuario);
        
        if (curso == null || usuario == null) {
            throw new RuntimeException("Curso ou usuário não encontrado");
        }

        UsuarioCurso uc = new UsuarioCurso();
        uc.curso = curso;
        uc.usuario = usuario;
        uc.status = "Em andamento";
        uc.progresso = 0.0;
        uc.persist();
        
        return uc;
    }

    @Transactional
    public UsuarioCurso concluirCurso(Long idCurso, Long idUsuario) {
        UsuarioCurso uc = UsuarioCurso.find("curso.id = ?1 AND usuario.id = ?2", idCurso, idUsuario).firstResult();
        if (uc == null) {
            throw new RuntimeException("Curso não encontrado para este usuário");
        }
        
        uc.status = "Concluído";
        uc.progresso = 100.0;
        return uc;
    }

    @Transactional
    public UsuarioCurso atualizarProgressoCurso(Long idCurso, Long idUsuario, Double percentual) {
        UsuarioCurso uc = UsuarioCurso.find("curso.id = ?1 AND usuario.id = ?2", idCurso, idUsuario).firstResult();
        if (uc == null) {
            throw new RuntimeException("Curso não encontrado para este usuário");
        }
        
        uc.progresso = percentual;
        if (uc.progresso >= 100.0) {
            uc.status = "Concluído";
        }
        return uc;
    }
}