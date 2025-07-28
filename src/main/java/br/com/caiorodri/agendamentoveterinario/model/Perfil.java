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
@Table(name = "perfil")
public class Perfil {

	@Id
	private Integer id;
	
	private String nome;
	
	public Perfil(Integer id) {
		
		this.id = id;
		
	}
	
}
