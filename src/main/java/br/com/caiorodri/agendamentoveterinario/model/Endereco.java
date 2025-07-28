package br.com.caiorodri.agendamentoveterinario.model;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Endereco {

	private String logradouro;
	
	private String numero;
	
	private String complemento;
	
	private String cidade;
	
	@ManyToOne
	@JoinColumn(name = "sigla_estado")
	private Estado estado;
	
	private String cep;
	
	public Endereco() {
		
		this.estado = new Estado();
		
	}
	
}
