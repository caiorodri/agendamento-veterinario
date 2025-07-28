package br.com.caiorodri.agendamentoveterinario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoTipoDTO {

	private Integer id;
	
	private String nome;
	
	public AgendamentoTipoDTO(Integer id) {
		
		this.id = id;
		
	}
	
}
