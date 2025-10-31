package br.com.caiorodri.agendamentoveterinario.model;

import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Setter
@AllArgsConstructor
@ToString(exclude = {"animais", "agendamentos", "telefones"})
@EqualsAndHashCode(of = "id")
public class Usuario implements UserDetails {

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
    @JsonManagedReference("usuario-animal")
    @OrderBy("id ASC")
	private List<Animal> animais;
	
	@OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    @JsonManagedReference("cliente-agendamento")
	@OrderBy("dataAgendamentoInicio DESC")
	private List<Agendamento> agendamentos;
	
	@ElementCollection
	@CollectionTable(
	    name = "usuario_telefone",
	    joinColumns = @JoinColumn(name = "id_usuario")
	)
	@Column(name = "telefone")
	private Set<String> telefones;

	@Column(name = "email_realizar_consulta_recebido")
	private boolean emailRealizarConsultaRecebido;

    @Column(name = "receber_email")
    private boolean receberEmail;

	public Usuario() {
		
		this.endereco = new Endereco();
		this.telefones = new HashSet<>();
        this.animais = new ArrayList<>();
        this.agendamentos = new ArrayList<>();
		this.status = new Status();
		this.perfil = new Perfil();
		
	}
	
	public Usuario(Long id) {
		
		this.id = id;
		
	}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.perfil != null) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.perfil.getNome().toUpperCase()));
        }
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.status != null && this.status.getNome().equalsIgnoreCase("Ativo");
    }



    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status != null && this.status.getNome().equalsIgnoreCase("Ativo");
    }

}
