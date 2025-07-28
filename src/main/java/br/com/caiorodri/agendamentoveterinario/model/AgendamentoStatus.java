package br.com.caiorodri.agendamentoveterinario.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "agendamento_status")
public class AgendamentoStatus {

	@Id
	private Integer id;
	
	private String nome;
	
	public AgendamentoStatus(Integer id) {
		
		this.id = id;
		
	}
	
}

