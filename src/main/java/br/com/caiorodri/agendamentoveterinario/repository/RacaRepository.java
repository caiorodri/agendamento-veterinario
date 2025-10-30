package br.com.caiorodri.agendamentoveterinario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.caiorodri.agendamentoveterinario.model.Raca;

@Repository
public interface RacaRepository extends JpaRepository<Raca, Integer>{

	@Query("SELECT r FROM Raca r " +
		   "WHERE r.especie.id = ?1")
	public List<Raca> findByEspecie(Integer idEspecie);

    @Query("SELECT r FROM Raca r " +
           "INNER JOIN r.especie e ")
    public List<Raca> findAll();

}
