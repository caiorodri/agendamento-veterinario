package br.com.caiorodri.agendamentoveterinario.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String nome;

	@Column(unique = true, nullable = false)
	private String email;
	
	private String senha;
	
	@Column(length = 11, unique = true, nullable = false)
	private String cpf;
	
	@Embedded
	private Endereco endereco;
	
	@Column(name = "codigo_recuperacao")
	private String codigoRecuperacao;
	
	@Column(name = "expiracao_codigo", columnDefinition = "DATETIME")
	private LocalDateTime expiracaoCodigo;
	
	@Column(name = "data_nascimento")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date dataNascimento;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_status")
	private Status status;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_perfil")
	private Perfil perfil;
	
	@OneToMany(mappedBy = "dono", fetch = FetchType.LAZY)
    @JsonManagedReference
	private List<Animal> animais;
	
	@OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    @JsonManagedReference
	@OrderBy("dataAgendamentoInicio DESC")
	private List<Agendamento> agendamentos;
	
	@ElementCollection
	@CollectionTable(
	    name = "usuario_telefone",
	    joinColumns = @JoinColumn(name = "id_usuario")
	)
	@Column(name = "telefone")
	private List<String> telefones;

	@Column(name = "email_realizar_consulta_recebido")
	private boolean emailRealizarConsultaRecebido;
	
	public Usuario() {
		
		this.endereco = new Endereco();
		this.telefones = new ArrayList<>();
		this.status = new Status();
		this.perfil = new Perfil();
		
	}
	
	public Usuario(Long id) {
		
		this.id = id;
		
	}
	
}
