package br.com.caiorodri.agendamentoveterinario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.caiorodri.agendamentoveterinario.model.Sexo;

@Repository
public interface SexoRepository extends JpaRepository<Sexo, Integer>{

}
