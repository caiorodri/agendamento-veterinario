package br.com.caiorodri.agendamentoveterinario.repository;

import br.com.caiorodri.agendamentoveterinario.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, String> {

    @Query("SELECT e FROM Estado e")
    public List<Estado> findEstados();

}
