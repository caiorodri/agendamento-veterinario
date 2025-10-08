package br.com.caiorodri.agendamentoveterinario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class AnimalDTO {

	private Long id;
	
	@NotBlank(message = "O Nome é obrigatório.")
    @Size(min = 3, max = 50, message = "O nome deve ter entre 3 e 50 caracteres.")
	private String nome;
	
	private RacaDTO raca;
	
	private SexoDTO sexo;
	
	private String descricao;
	
	private boolean castrado;
	
	private Date dataNascimento;
	
	private float peso;
	
	private float altura;
	
	private UsuarioSimplesDTO dono;

    private List<AgendamentoDTO> agendamentos;
	
	public AnimalDTO() {
		
		this.raca = new RacaDTO();
		this.sexo = new SexoDTO();
		this.dono = new UsuarioSimplesDTO();
		
	}
	
	public AnimalDTO(Long id) {
		
		this.id = id;
		
	}
	
}
