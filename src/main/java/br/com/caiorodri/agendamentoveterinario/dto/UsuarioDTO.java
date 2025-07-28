package br.com.caiorodri.agendamentoveterinario.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class UsuarioDTO {

	private Long id;
	
	private String nome;

    @Email(message = "Formato de e-mail inválido.")
	private String email;
	
	private String cpf;
	
	private EnderecoDTO endereco;
	
	private StatusDTO status;
	
	private PerfilDTO perfil;

	private Date dataNascimento;
	
	private List<String> telefones;

	private boolean emailRealizarConsultaRecebido;
	
	public UsuarioDTO() {
		
		this.endereco = new EnderecoDTO();
		this.telefones = new ArrayList<>();
		this.perfil = new PerfilDTO();
		this.status = new StatusDTO();
		
	}
	
	public UsuarioDTO(Long id) {
		
		this.id = id;
		
	}
	
}
