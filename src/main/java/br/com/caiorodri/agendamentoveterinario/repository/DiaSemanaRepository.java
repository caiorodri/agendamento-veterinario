package br.com.caiorodri.agendamentoveterinario.repository;

import br.com.caiorodri.agendamentoveterinario.model.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaSemanaRepository extends JpaRepository<DiaSemana, Integer> {
}
