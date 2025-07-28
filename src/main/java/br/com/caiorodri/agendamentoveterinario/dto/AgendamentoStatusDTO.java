package br.com.caiorodri.agendamentoveterinario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AgendamentoStatusDTO {

	private Integer id;
	
	private String nome;
	
	public AgendamentoStatusDTO(Integer id) {
		
		this.id = id;
		
	}
	
}

