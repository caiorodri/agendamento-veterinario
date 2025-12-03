package br.com.caiorodri.agendamentoveterinario.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoConsultaDTO {

    private Long id;
    private AgendamentoDTO agendamento;
    private String diagnosticoPrincipal;
    private String observacoesVeterinario;
    private LocalDateTime dataRealizacao;
    private List<ItemPrescricaoDTO> prescricoes;

}