package br.com.caiorodri.agendamentoveterinario.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioAlterarSenha {

    private String senhaAntiga;
    private String senhaNova;

}
