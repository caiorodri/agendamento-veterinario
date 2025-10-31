package br.com.caiorodri.agendamentoveterinario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.caiorodri.agendamentoveterinario.model.Animal;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long>{

	@Query("SELECT DISTINCT a FROM Animal a " +
		   "LEFT JOIN FETCH a.dono d ")
	public Page<Animal> findAll(Pageable pageable);
	
	@Query("SELECT a FROM Animal a " +
			"LEFT JOIN FETCH a.dono " +
			"LEFT JOIN FETCH a.raca " +
			"LEFT JOIN FETCH a.raca.especie " +
			"WHERE a.id = ?1")
	public Optional<Animal> findById(Long id);
	
	@Query("SELECT a FROM Animal a " +
			"LEFT JOIN FETCH a.dono d " +
			"LEFT JOIN FETCH a.raca r " +
			"LEFT JOIN FETCH a.raca.especie e " +
			"WHERE d.id = ?1")
	public Page<Animal> findByUsuarioId(Long id, Pageable pageable);
	
}
