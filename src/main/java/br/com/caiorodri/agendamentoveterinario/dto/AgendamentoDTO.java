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

	private AnimalDTO animal;

	private UsuarioDTO cliente;

	private UsuarioDTO veterinario;
	
	private UsuarioDTO recepcionista;

	private AgendamentoStatusDTO status;

	private AgendamentoTipoDTO tipo;
	
	private String descricao;
	
	private LocalDateTime dataCriacao;

	private LocalDateTime dataAgendamentoInicio;

	private LocalDateTime dataAgendamentoFinal;
	
	private String dataAgendamentoFormatada;
	
	private String horaAgendamentoFormatada;
	
	public AgendamentoDTO() {
		
		this.animal = new AnimalDTO();
		this.cliente = new UsuarioDTO();
		this.veterinario = new UsuarioDTO();
		this.recepcionista = new UsuarioDTO();
		this.status = new AgendamentoStatusDTO();
		this.tipo = new AgendamentoTipoDTO();
	
	}
	
	public AgendamentoDTO(Long id) {
		
		this.id = id;
		
	}
	
}
