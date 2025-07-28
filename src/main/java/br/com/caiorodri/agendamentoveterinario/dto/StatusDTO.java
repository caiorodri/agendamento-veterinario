package br.com.caiorodri.agendamentoveterinario.dto;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "usuario_status")
public class StatusDTO {

	private Integer id;
	
	private String nome;

	public StatusDTO(Integer id) {
		
		this.id = id;
		
	}
	
}

