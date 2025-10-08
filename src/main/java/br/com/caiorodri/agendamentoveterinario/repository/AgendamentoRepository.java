package br.com.caiorodri.agendamentoveterinario.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.caiorodri.agendamentoveterinario.model.Agendamento;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long>{

	
	@Query("SELECT COUNT(a) > 0 " +
		    "FROM Agendamento a " +
		    "WHERE (a.dataAgendamentoInicio <= ?1 " +
		    "AND a.dataAgendamentoFinal >= ?2) " +
		    "OR (a.dataAgendamentoInicio <= ?1 " +
		    "AND a.dataAgendamentoFinal > ?1) " +
		    "OR (a.dataAgendamentoInicio < ?2 " +
		    "AND a.dataAgendamentoFinal >= ?2)")
	boolean existeConflitoDeHorario(LocalDateTime inicio, LocalDateTime fim);

	@Query("SELECT a FROM Agendamento a " +
		    "WHERE (a.dataAgendamentoInicio < ?1 " +
		    "AND a.dataAgendamentoFinal > ?2) " +
		    "OR (a.dataAgendamentoInicio < ?1 " +
		    "AND a.dataAgendamentoFinal > ?1) " +
		    "OR (a.dataAgendamentoInicio < ?2 " +
		    "AND a.dataAgendamentoFinal > ?1)")
	List<Agendamento> findByHorario(LocalDateTime inicio, LocalDateTime fim);
	
	@Query("SELECT DISTINCT a FROM Agendamento a " +
            "INNER JOIN FETCH a.animal animal " +
            "INNER JOIN FETCH animal.dono d " +
			"ORDER BY a.dataAgendamentoInicio DESC")
	public Page<Agendamento> findAll(Pageable pageable);
	
	@Query("SELECT DISTINCT a FROM Agendamento a " +
			"ORDER BY a.dataAgendamentoInicio")
	public List<Agendamento> findAll();
	
	
	@Query("SELECT DISTINCT a FROM Agendamento a " +
		   "WHERE a.cliente.id = ?1")
	public Page<Agendamento> findByCliente(Long idCliente, Pageable pageable);
	
	@Query("SELECT DISTINCT a FROM Agendamento a " +
		   "WHERE a.cliente.id = ?1 " +
		   "AND a.dataAgendamentoInicio < ?2")
	public Page<Agendamento> findConcluidosByCliente(Long idCliente, LocalDateTime date, Pageable pageable);
	
	@Query("SELECT DISTINCT a FROM Agendamento a " +
		   "WHERE a.animal.id = ?1 " +
		   "AND a.dataAgendamentoInicio < ?2")
	public Page<Agendamento> findConcluidosByAnimal(Long idAnimal, LocalDateTime date, Pageable pageable);
	
	@Query("SELECT DISTINCT a FROM Agendamento a " +
		   "WHERE a.cliente.id = ?1 " +
		   "AND a.dataAgendamentoInicio > ?2")
	public Page<Agendamento> findProximosByCliente(Long idCliente, LocalDateTime date, Pageable pageable);
	
	@Query("SELECT DISTINCT a FROM Agendamento a " +
		   "WHERE a.animal.id = ?1 " +
		   "AND a.dataAgendamentoInicio > ?2")
	public Page<Agendamento> findProximosByAnimal(Long idAnimal, LocalDateTime date, Pageable pageable);
	
	@Query("SELECT DISTINCT a FROM Agendamento a " +
		   "WHERE a.animal.id = ?1 " +
            "ORDER BY a.dataAgendamentoInicio DESC")
	public Page<Agendamento> findByAnimal(Long idAnimal, Pageable pageable);
	
	@Query("SELECT DISTINCT a FROM Agendamento a " +
		   "WHERE a.cliente.id = ?1 " +
            "ORDER BY a.dataAgendamentoInicio DESC")
	public Page<Agendamento> findByUsuario(Long idUsuario, Pageable pageable);
	
	@Query("SELECT a FROM Agendamento a WHERE a.animal.id = ?1 ORDER BY a.dataAgendamentoInicio DESC LIMIT 1")
	public Agendamento findUltimaConsultaByAnimal(Long animalId);
	
	
	@Query("SELECT a FROM Agendamento a " +
			"INNER JOIN FETCH a.cliente " +
			"INNER JOIN FETCH a.veterinario " +
			"INNER JOIN FETCH a.recepcionista " +
			"INNER JOIN FETCH a.animal animal " +
            "INNER JOIN FETCH animal.dono " +
			"WHERE a.id = ?1")
	public Optional<Agendamento> findById(Long id);

	
}
