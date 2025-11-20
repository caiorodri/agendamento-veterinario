package br.com.caiorodri.agendamentoveterinario.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class VeterinarioHorarioDTO {
    private Long id;
    private Integer idDiaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;
}