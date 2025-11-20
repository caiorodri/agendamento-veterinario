package br.com.caiorodri.agendamentoveterinario.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dia_semana")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaSemana {

    @Id
    private Integer id;

    private String nome;
}