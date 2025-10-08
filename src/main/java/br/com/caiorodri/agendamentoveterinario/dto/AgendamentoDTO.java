package br.com.caiorodri.agendamentoveterinario.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class AgendamentoDTO {

	private Long id;

	private AnimalSimplesDTO animal;

	private UsuarioSimplesDTO cliente;

	private UsuarioSimplesDTO veterinario;
	
	private UsuarioSimplesDTO recepcionista;

	private AgendamentoStatusDTO status;

	private AgendamentoTipoDTO tipo;
	
	private String descricao;
	
	private LocalDateTime dataCriacao;

	private LocalDateTime dataAgendamentoInicio;

	private LocalDateTime dataAgendamentoFinal;
	
	public AgendamentoDTO() {
		
		this.animal = new AnimalSimplesDTO();
		this.cliente = new UsuarioSimplesDTO();
		this.veterinario = new UsuarioSimplesDTO();
		this.recepcionista = new UsuarioSimplesDTO();
		this.status = new AgendamentoStatusDTO();
		this.tipo = new AgendamentoTipoDTO();
	
	}
	
	public AgendamentoDTO(Long id) {
		
		this.id = id;
		
	}
	
}
