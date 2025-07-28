package br.com.caiorodri.agendamentoveterinario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

	private String email;
	
	private String senha;
	
}
