package br.com.caiorodri.agendamentoveterinario.enums;

import java.time.DayOfWeek;

public enum DiaSemanaEnum {
    DOMINGO(1, DayOfWeek.SUNDAY),
    SEGUNDA(2, DayOfWeek.MONDAY),
    TERCA(3, DayOfWeek.TUESDAY),
    QUARTA(4, DayOfWeek.WEDNESDAY),
    QUINTA(5, DayOfWeek.THURSDAY),
    SEXTA(6, DayOfWeek.FRIDAY),
    SABADO(7, DayOfWeek.SATURDAY);

    private final int id;
    private final DayOfWeek dayOfWeek;

    DiaSemanaEnum(int id, DayOfWeek dayOfWeek) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
    }

    public int getId() {
        return id;
    }

    public static int from(DayOfWeek dayOfWeek) {
        for (DiaSemanaEnum dia : values()) {
            if (dia.dayOfWeek == dayOfWeek) {
                return dia.id;
            }
        }
        return DOMINGO.getId();
    }
}