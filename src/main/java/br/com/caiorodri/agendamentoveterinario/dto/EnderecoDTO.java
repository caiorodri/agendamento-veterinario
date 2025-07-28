package br.com.caiorodri.agendamentoveterinario.dto;

import br.com.caiorodri.agendamentoveterinario.model.Estado;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnderecoDTO {

	private String logradouro;
	
	private String numero;
	
	private String complemento;
	
	private String cidade;
	
	private Estado estado;
	
	private String cep;
	
	public EnderecoDTO() {
		
		this.estado = new Estado();
		
	}
	
}
