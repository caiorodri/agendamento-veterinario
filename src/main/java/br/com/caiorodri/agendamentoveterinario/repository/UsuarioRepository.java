package br.com.caiorodri.agendamentoveterinario.repository;

import java.util.List;

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
		   "LEFT JOIN FETCH u.perfil p " +
		   "WHERE u.email = ?1 AND u.senha = ?2 " + 
		   "AND u.status.id = 1")
	public Usuario findByEmailAndSenha(String email, String senha);

	@Query("SELECT u FROM Usuario u " +
			"LEFT JOIN FETCH u.perfil p " +
			"WHERE u.email = ?1")
	public Usuario findByEmail(String email);
	
	@Query("SELECT u FROM Usuario u " +
		   "LEFT JOIN FETCH u.status s " +
		   "WHERE u.perfil.id = 1")
	public Page<Usuario> findClientes(Pageable pageable);
	
	@Query("SELECT u FROM Usuario u " +
		   "LEFT JOIN FETCH u.status s " +
		   "WHERE u.perfil.id = 1")
	public List<Usuario> findClientes();	
	
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
	
	@Query("SELECT e FROM Estado e")
	public List<Estado> findEstados();
	
}
