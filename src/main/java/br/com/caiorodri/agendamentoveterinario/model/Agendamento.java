package br.com.caiorodri.agendamentoveterinario.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Agendamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_animal")
    @JsonBackReference("animal-agendamento")
	private Animal animal;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cliente")
    @JsonBackReference("cliente-agendamento")
	private Usuario cliente;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_veterinario")
	private Usuario veterinario;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_recepcionista")
	private Usuario recepcionista;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_agendamento_status")
	private AgendamentoStatus status;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_agendamento_tipo")
	private AgendamentoTipo tipo;
	
	@Column(name = "data_criacao", columnDefinition = "TIMESTAMP")
	private LocalDateTime dataCriacao;
	
	@Column(name = "data_agendamento_inicio", columnDefinition = "TIMESTAMP")
	private LocalDateTime dataAgendamentoInicio;
	
	@Column(name = "data_agendamento_final", columnDefinition = "TIMESTAMP")
	private LocalDateTime dataAgendamentoFinal;
	
	private String descricao;
	
	public Agendamento(Long id) {
		
		this.id = id;
		
	}
	
}
