package br.com.caiorodri.agendamentoveterinario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.caiorodri.agendamentoveterinario.model.Perfil;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Integer>{

	@Query("SELECT p FROM Perfil p " +
			"WHERE p.id = 1")
	public List<Perfil> findAllToRecepcionista();
	

	@Query("SELECT p FROM Perfil p " +
			"WHERE p.id IN (2,3)")
	public List<Perfil> findAllToAdministrador();
	
}
