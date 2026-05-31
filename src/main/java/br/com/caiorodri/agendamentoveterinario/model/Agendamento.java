package br.com.caiorodri.agendamentoveterinario.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "agendamento", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vet_data_inicio", columnNames = {"id_veterinario", "data_agendamento_inicio"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"animal", "cliente", "veterinario", "recepcionista"})
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

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_agendamento_status")
	private AgendamentoStatus status;
	
	@ManyToOne(fetch = FetchType.EAGER)
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
