package br.com.caiorodri.agendamentoveterinario.repository;

import br.com.caiorodri.agendamentoveterinario.model.VeterinarioHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VeterinarioHorarioRepository extends JpaRepository<VeterinarioHorario, Long> {

    List<VeterinarioHorario> findByVeterinarioId(Long idVeterinario);

    @Query("SELECT vh FROM VeterinarioHorario vh " +
            "WHERE vh.veterinario.id = ?1 AND vh.diaSemana.id = ?2")
    List<VeterinarioHorario> findByVeterinarioIdAndDiaDaSemanaId(Long idVeterinario, Integer idDiaSemana);
}