package br.com.caiorodri.agendamentoveterinario.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemPrescricaoDTO {

    private Long id;
    private String nomeMedicamento;
    private String dosagem;
    private String instrucoesUso;

}