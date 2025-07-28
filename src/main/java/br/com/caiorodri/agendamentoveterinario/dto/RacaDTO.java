package br.com.caiorodri.agendamentoveterinario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class RacaDTO {

	private Integer id;
	
	private EspecieDTO especie;
	
	private String nome;
	
	public RacaDTO() {
		
		this.especie = new EspecieDTO();
		
	}
	
	public RacaDTO(Integer id) {
		
		this.id = id;
		
	}
	
}
