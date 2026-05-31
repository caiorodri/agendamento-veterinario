package br.com.caiorodri.agendamentoveterinario.scheduler;

import br.com.caiorodri.agendamentoveterinario.email.EmailSender;
import br.com.caiorodri.agendamentoveterinario.model.Agendamento;
import br.com.caiorodri.agendamentoveterinario.model.Animal;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoRepository;
import br.com.caiorodri.agendamentoveterinario.repository.AnimalRepository;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(9)
class NotificacaoSchedulerTest {

    @InjectMocks
    private NotificacaoScheduler notificacaoScheduler;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailSender emailService;

    @Test
    @Order(1)
    @DisplayName("CT149 - Enviar email para animal com consulta antiga (> 6 meses)")
    void deveEnviarEmailParaConsultaAntiga() {

        Usuario dono = new Usuario();
        dono.setId(1L);
        dono.setReceberEmail(true);
        dono.setEmailRealizarConsultaRecebido(false);

        Animal pet = new Animal();
        pet.setId(10L);
        pet.setDono(dono);

        Agendamento consultaAntiga = new Agendamento();
        consultaAntiga.setDataAgendamentoInicio(LocalDateTime.now().minusMonths(7));

        when(animalRepository.findAll()).thenReturn(List.of(pet));
        when(agendamentoRepository.findUltimaConsultaByAnimal(10L)).thenReturn(consultaAntiga);

        ReflectionTestUtils.invokeMethod(notificacaoScheduler, "verificarAnimaisSemConsultaRecente");

        verify(emailService, times(1)).enviarInformacaoRealizarConsultaEmail(pet, consultaAntiga);

        verify(usuarioRepository, times(1)).save(dono);
    }

    @Test
    @Order(2)
    @DisplayName("CT150 - Não enviar email se a consulta for recente (< 6 meses)")
    void naoDeveEnviarEmailSeConsultaRecente() {

        Usuario dono = new Usuario();
        dono.setReceberEmail(true);
        dono.setEmailRealizarConsultaRecebido(false);

        Animal pet = new Animal();
        pet.setId(10L);
        pet.setDono(dono);

        Agendamento consultaRecente = new Agendamento();
        consultaRecente.setDataAgendamentoInicio(LocalDateTime.now().minusMonths(2));

        when(animalRepository.findAll()).thenReturn(List.of(pet));
        when(agendamentoRepository.findUltimaConsultaByAnimal(10L)).thenReturn(consultaRecente);

        ReflectionTestUtils.invokeMethod(notificacaoScheduler, "verificarAnimaisSemConsultaRecente");

        verify(emailService, never()).enviarInformacaoRealizarConsultaEmail(any(), any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @Order(3)
    @DisplayName("CT151 - Não enviar se o dono desativou 'Receber Emails'")
    void naoDeveEnviarSeDonoDesativouNotificacoes() {
        Usuario dono = new Usuario();
        dono.setReceberEmail(false);
        dono.setEmailRealizarConsultaRecebido(false);

        Animal pet = new Animal();
        pet.setId(10L);
        pet.setDono(dono);

        Agendamento consultaAntiga = new Agendamento();
        consultaAntiga.setDataAgendamentoInicio(LocalDateTime.now().minusMonths(7));

        when(animalRepository.findAll()).thenReturn(List.of(pet));
        when(agendamentoRepository.findUltimaConsultaByAnimal(10L)).thenReturn(consultaAntiga);

        ReflectionTestUtils.invokeMethod(notificacaoScheduler, "verificarAnimaisSemConsultaRecente");

        verify(emailService, never()).enviarInformacaoRealizarConsultaEmail(any(), any());
    }
}