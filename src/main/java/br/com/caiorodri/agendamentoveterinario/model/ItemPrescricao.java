package br.com.caiorodri.agendamentoveterinario.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "item_prescricao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemPrescricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_medicamento", nullable = false, length = 100)
    private String nomeMedicamento;

    @Column(name = "dosagem", nullable = false, length = 100)
    private String dosagem;

    @Column(name = "instrucoes_uso", nullable = false)
    private String instrucoesUso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_resultado_consulta", nullable = false)
    @JsonBackReference
    private ResultadoConsulta resultadoConsulta;
}
