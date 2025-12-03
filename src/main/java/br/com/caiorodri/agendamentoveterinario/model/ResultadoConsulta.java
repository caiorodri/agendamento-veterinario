package br.com.caiorodri.agendamentoveterinario.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resultado_consulta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_agendamento", nullable = false, unique = true)
    private Agendamento agendamento;

    @Column(name = "diagnostico_principal", nullable = false)
    private String diagnosticoPrincipal;

    @Column(name = "observacoes_veterinario", columnDefinition = "TEXT")
    private String observacoesVeterinario;

    @Column(name = "data_realizacao")
    private LocalDateTime dataRealizacao;

    @OneToMany(mappedBy = "resultadoConsulta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemPrescricao> prescricoes = new ArrayList<>();

    public void adicionarPrescricao(ItemPrescricao item) {
        prescricoes.add(item);
        item.setResultadoConsulta(this);
    }

    @PrePersist
    public void prePersist() {
        if (this.dataRealizacao == null) {
            this.dataRealizacao = LocalDateTime.now();
        }
    }
}