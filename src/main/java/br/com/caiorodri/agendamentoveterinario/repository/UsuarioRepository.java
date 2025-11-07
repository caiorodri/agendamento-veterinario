package br.com.caiorodri.agendamentoveterinario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.caiorodri.agendamentoveterinario.model.Estado;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>{

	public Page<Usuario> findAll(Pageable pageable);
	
	public boolean existsByCpf(String cpf);

	public boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.perfil perfil " +
            "LEFT JOIN FETCH u.status status " +
            "LEFT JOIN FETCH u.telefones telefones " +
            "WHERE u.email = ?1")
    public Optional<Usuario> findByEmail(String email);

	@Query("SELECT u FROM Usuario u " +
			"LEFT JOIN FETCH u.perfil perfil " +
            "LEFT JOIN FETCH u.status status " +
            "LEFT JOIN FETCH u.telefones telefones " +
            "LEFT JOIN FETCH u.animais " +
			"WHERE u.email = ?1")
	public Optional<Usuario> findByEmailWithSets(String email);

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.perfil perfil " +
            "LEFT JOIN FETCH u.status status " +
            "LEFT JOIN FETCH u.telefones telefones " +
            "LEFT JOIN FETCH u.animais " +
            "WHERE u.id = ?1")
    public Optional<Usuario> findByIdWithSets(Long id);

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.agendamentos ag " +
            "LEFT JOIN FETCH ag.animal " +
            "LEFT JOIN FETCH ag.veterinario " +
            "LEFT JOIN FETCH ag.recepcionista " +
            "LEFT JOIN FETCH ag.status " +
            "LEFT JOIN FETCH ag.tipo " +
            "WHERE u.id = ?1")
    public Optional<Usuario> findByIdWithAgendamentos(Long id);
	
	@Query("SELECT u FROM Usuario u " +
		   "LEFT JOIN FETCH u.status s " +
		   "WHERE u.perfil.id = 1")
	public Page<Usuario> findClientes(Pageable pageable);
	
	@Query("SELECT u FROM Usuario u " +
		   "LEFT JOIN FETCH u.status s " +
		   "WHERE u.perfil.id = 1 " +
           "AND s.id = 1")
	public List<Usuario> findClientesAtivos();
	
	@Query("SELECT u FROM Usuario u " +
			   "LEFT JOIN FETCH u.status s " +
			   "WHERE u.perfil.id = 2")
	public List<Usuario> findRecepcionista();
	
	@Query("SELECT u FROM Usuario u " +
		   "LEFT JOIN FETCH u.status s " +
		   "WHERE u.perfil.id = 3")
	public List<Usuario> findVeterinarios();
	
	@Query("SELECT u FROM Usuario u " +
		   "LEFT JOIN FETCH u.perfil p " +
		   "LEFT JOIN FETCH u.status s " +
		   "WHERE p.id IN (2,3)")
	public Page<Usuario> findFuncionarios(Pageable pageable);
	
	@Query("SELECT u FROM Usuario u " +
		   "LEFT JOIN FETCH u.perfil p " +
		   "LEFT JOIN FETCH u.status s " +
		   "WHERE p.id IN (2,3)")
	public List<Usuario> findFuncionarios();
	
}
