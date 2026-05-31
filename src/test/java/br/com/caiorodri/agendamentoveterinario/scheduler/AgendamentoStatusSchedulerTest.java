package br.com.caiorodri.agendamentoveterinario.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.caiorodri.agendamentoveterinario.model.Agendamento;
import br.com.caiorodri.agendamentoveterinario.model.AgendamentoStatus;
import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoRepository;
import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoStatusRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(8)
class AgendamentoStatusSchedulerTest {

    @InjectMocks
    private AgendamentoStatusScheduler scheduler;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AgendamentoStatusRepository agendamentoStatusRepository;

    private final Integer ID_PERDIDO = 4;

    @Test
    @Order(1)
    @DisplayName("CT146 - Atualizar agendamentos expirados p/ status PERDIDO")
    void deveMarcarAgendamentosComoPerdidoComSucesso() {

        Agendamento agendamentoMock = new Agendamento(100L);
        List<Agendamento> agendamentosExpirados = List.of(agendamentoMock);

        AgendamentoStatus statusPerdidoMock = new AgendamentoStatus(ID_PERDIDO, "Perdido");

        when(agendamentoRepository.findAgendamentosExpirados(eq(1), any(LocalDateTime.class)))
                .thenReturn(agendamentosExpirados);

        when(agendamentoStatusRepository.findById(ID_PERDIDO))
                .thenReturn(Optional.of(statusPerdidoMock));

        scheduler.marcarAgendamentosComoPerdido();

        assertEquals(statusPerdidoMock, agendamentoMock.getStatus(), "O status do agendamento deveria ter sido alterado para Perdido");

        verify(agendamentoRepository, times(1)).saveAll(agendamentosExpirados);
    }

    @Test
    @Order(2)
    @DisplayName("CT147 - Não fazer nada quando não houver agendamentos expirados")
    void naoDeveFazerNadaQuandoListaVazia() {

        when(agendamentoRepository.findAgendamentosExpirados(eq(1), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        scheduler.marcarAgendamentosComoPerdido();

        verify(agendamentoStatusRepository, never()).findById(anyInt());

        verify(agendamentoRepository, never()).saveAll(any());
    }

    @Test
    @Order(3)
    @DisplayName("CT148 - Lançar EntityNotFoundException se o status PERDIDO não existir")
    void deveLancarExcecaoQuandoStatusNaoEncontrado() {

        Agendamento agendamentoMock = new Agendamento(100L);
        List<Agendamento> agendamentosExpirados = List.of(agendamentoMock);

        when(agendamentoRepository.findAgendamentosExpirados(eq(1), any(LocalDateTime.class)))
                .thenReturn(agendamentosExpirados);

        when(agendamentoStatusRepository.findById(ID_PERDIDO))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                scheduler.marcarAgendamentosComoPerdido()
        );

        assertEquals("Status 'Perdido' não encontrado no banco de dados.", exception.getMessage());
        verify(agendamentoRepository, never()).saveAll(any());

    }
}