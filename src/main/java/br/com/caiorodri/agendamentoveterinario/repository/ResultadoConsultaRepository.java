package br.com.caiorodri.agendamentoveterinario.repository;

import br.com.caiorodri.agendamentoveterinario.model.ResultadoConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResultadoConsultaRepository extends JpaRepository<ResultadoConsulta, Long> {

    Optional<ResultadoConsulta> findByAgendamentoId(Long idAgendamento);

    boolean existsByAgendamentoId(Long idAgendamento);

}