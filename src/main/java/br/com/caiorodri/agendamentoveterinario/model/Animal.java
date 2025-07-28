package br.com.caiorodri.agendamentoveterinario.model;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table
@Entity
@AllArgsConstructor
@NoArgsConstructor
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
	private List<Agendamento> agendamentos;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_dono")
	private Usuario dono;
	
	public Animal(Long id) {
		
		this.id = id;
		
	}
	
}
