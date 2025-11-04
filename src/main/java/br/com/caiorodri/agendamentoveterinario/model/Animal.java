package br.com.caiorodri.agendamentoveterinario.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Table
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"agendamentos", "dono"})
public class Animal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String nome;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_raca")
	private Raca raca;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_sexo")
	private Sexo sexo;
	
	private String descricao;
	
	private Boolean castrado;
	
	@Column(name = "data_nascimento")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date dataNascimento;
	
	private float peso;
	
	private float altura;
	
	@OneToMany(mappedBy = "animal", fetch = FetchType.LAZY)
    @JsonManagedReference("animal-agendamento")
	@OrderBy("dataAgendamentoInicio DESC")
	private List<Agendamento> agendamentos;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_dono")
    @JsonBackReference("usuario-animal")
	private Usuario dono;

    private String urlImagem;
	
	public Animal(Long id) {
		
		this.id = id;
		
	}
	
}
