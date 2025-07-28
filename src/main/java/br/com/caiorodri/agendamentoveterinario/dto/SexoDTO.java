package br.com.caiorodri.agendamentoveterinario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class SexoDTO {
	
	private Integer id;
	
	private String nome;

	public SexoDTO(Integer id) {
		
		this.id = id;
		
	}
	
}
