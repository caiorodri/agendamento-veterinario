package br.com.caiorodri.agendamentoveterinario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
public class AnimalSimplesDTO {

	private Long id;

	private String nome;

    private UsuarioSimplesDTO dono;

}
