package br.com.caiorodri.agendamentoveterinario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PerfilDTO {

	private Integer id;
	
	private String nome;
	
	public PerfilDTO(Integer id) {
		
		this.id = id;
		
	}
	
}
