package br.com.caiorodri.agendamentoveterinario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Estado {

	@Id
	@Column(name = "sigla", columnDefinition = "CHAR(2)")
	private String sigla;
	
	private String nome;
	
}
