package br.com.caiorodri.agendamentoveterinario.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import br.com.caiorodri.agendamentoveterinario.email.EmailSender;
import br.com.caiorodri.agendamentoveterinario.model.Agendamento;
import br.com.caiorodri.agendamentoveterinario.model.AgendamentoStatus;
import br.com.caiorodri.agendamentoveterinario.model.AgendamentoTipo;
import br.com.caiorodri.agendamentoveterinario.model.Animal;
import br.com.caiorodri.agendamentoveterinario.model.ItemPrescricao;
import br.com.caiorodri.agendamentoveterinario.model.ResultadoConsulta;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoRepository;
import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoStatusRepository;
import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoTipoRepository;
import br.com.caiorodri.agendamentoveterinario.repository.AnimalRepository;
import br.com.caiorodri.agendamentoveterinario.repository.ResultadoConsultaRepository;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(7)
class AgendamentoServiceTest {

    @InjectMocks
    private AgendamentoService agendamentoService;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AgendamentoStatusRepository agendamentoStatusRepository;

    @Mock
    private AgendamentoTipoRepository agendamentoTipoRepository;

    @Mock
    private ResultadoConsultaRepository resultadoConsultaRepository;

    @Mock
    private EmailSender emailSender;

    private Agendamento agendamentoValido;
    private ResultadoConsulta resultadoConsultaValido;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        agendamentoValido = new Agendamento();
        agendamentoValido.setId(100L);
        agendamentoValido.setDataAgendamentoInicio(LocalDateTime.now().plusDays(1).withHour(14));
        agendamentoValido.setDataAgendamentoFinal(LocalDateTime.now().plusDays(1).withHour(15));
        agendamentoValido.setAnimal(new Animal(1L));
        agendamentoValido.setRecepcionista(new Usuario(2L));
        agendamentoValido.setVeterinario(new Usuario(3L));

        resultadoConsultaValido = new ResultadoConsulta();
        resultadoConsultaValido.setId(1L);
        resultadoConsultaValido.setAgendamento(agendamentoValido);
        resultadoConsultaValido.setPrescricoes(List.of(new ItemPrescricao()));

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @Order(1)
    @DisplayName("CT107 - Impedir agendamento e lançar exceção ao sobrepor horários")
    void deveLancarExcecaoQuandoHouverConflitoDeHorarioNovoAgendamento() {
        agendamentoValido.setId(null);
        when(agendamentoRepository.existeConflitoDeHorario(any(), any(), eq(3L), eq(2))).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> agendamentoService.salvar(agendamentoValido));
    }

    @Test
    @Order(2)
    @DisplayName("CT108 - Salvar agendamento com sucesso quando dados válidos")
    void deveSalvarAgendamentoComSucesso() {
        agendamentoValido.setId(null);
        when(agendamentoRepository.existeConflitoDeHorario(any(), any(), eq(3L), eq(2))).thenReturn(false);
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamentoValido);

        Agendamento resultado = agendamentoService.salvar(agendamentoValido);

        assertNotNull(resultado);
        verify(emailSender, times(1)).enviarInformacaoCadastroAgendamentoEmail(any(), any());
        verify(agendamentoRepository).save(agendamentoValido);
    }

    @Test
    @Order(3)
    @DisplayName("CT109 - Lançar exceção ao salvar agendamento sem informar o Pet")
    void deveLancarExcecaoQuandoAnimalNulo() {
        agendamentoValido.setAnimal(null);

        assertThrows(IllegalArgumentException.class, () -> agendamentoService.salvar(agendamentoValido));
    }

    @Test
    @Order(4)
    @DisplayName("CT110 - Listar consultas filtradas pelo ID do cliente com sucesso")
    void deveListarAgendamentosPorUsuarioId() {
        when(usuarioRepository.existsById(2L)).thenReturn(true);
        when(agendamentoRepository.findByUsuario(2L, pageable)).thenReturn(new PageImpl<>(List.of(agendamentoValido)));

        Page<Agendamento> resultado = agendamentoService.listarByUsuarioId(2L, pageable);

        assertFalse(resultado.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("CT111 - Lançar EntityNotFoundException ao deletar agendamento inexistente")
    void deveLancarExcecaoAoDeletarAgendamentoInexistente() {
        when(agendamentoRepository.existsById(999L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> agendamentoService.deletar(999L));
    }

    @Test
    @Order(6)
    @DisplayName("CT112 - Recuperar agendamento por ID com sucesso")
    void deveRecuperarAgendamentoPorId() {
        when(agendamentoRepository.findById(100L)).thenReturn(Optional.of(agendamentoValido));

        Agendamento resultado = agendamentoService.recuperar(100L);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
    }

    @Test
    @Order(7)
    @DisplayName("CT113 - Lançar exceção ao recuperar agendamento inexistente")
    void deveLancarExcecaoAoRecuperarAgendamentoInexistente() {
        when(agendamentoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> agendamentoService.recuperar(999L));
    }

    @Test
    @Order(8)
    @DisplayName("CT114 - Listar agendamentos com paginação")
    void deveListarAgendamentosComPaginacao() {
        when(agendamentoRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(agendamentoValido)));

        Page<Agendamento> resultado = agendamentoService.listar(pageable);

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @Order(9)
    @DisplayName("CT115 - Listar agendamentos por animal id com paginação")
    void deveListarAgendamentosPorAnimalId() {
        when(animalRepository.existsById(1L)).thenReturn(true);
        when(agendamentoRepository.findByAnimal(1L, pageable)).thenReturn(new PageImpl<>(List.of(agendamentoValido)));

        Page<Agendamento> resultado = agendamentoService.listarByAnimalId(1L, pageable);

        assertFalse(resultado.isEmpty());
    }

    @Test
    @Order(10)
    @DisplayName("CT116 - Lançar exceção ao listar agendamentos por animal inexistente")
    void deveLancarExcecaoAoListarAgendamentosPorAnimalInexistente() {
        when(animalRepository.existsById(999L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> agendamentoService.listarByAnimalId(999L, pageable));
    }

    @Test
    @Order(11)
    @DisplayName("CT117 - Lançar exceção ao listar agendamentos por usuário inexistente")
    void deveLancarExcecaoAoListarAgendamentosPorUsuarioInexistente() {
        when(usuarioRepository.existsById(999L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> agendamentoService.listarByUsuarioId(999L, pageable));
    }

    @Test
    @Order(12)
    @DisplayName("CT118 - Atualizar agendamento com sucesso")
    void deveAtualizarAgendamentoComSucesso() {
        when(agendamentoRepository.existsById(100L)).thenReturn(true);
        when(agendamentoRepository.existeConflitoDeHorario(any(), any(), eq(3L), eq(2))).thenReturn(false);
        when(agendamentoRepository.findById(100L)).thenReturn(Optional.of(agendamentoValido));

        Agendamento resultado = agendamentoService.atualizar(agendamentoValido);

        assertNotNull(resultado);
        verify(emailSender, times(1)).enviarInformacaoCadastroAgendamentoEmail(any(), any());
        verify(agendamentoRepository).saveAndFlush(any(Agendamento.class));
    }

    @Test
    @Order(13)
    @DisplayName("CT119 - Lançar exceção ao atualizar agendamento inexistente")
    void deveLancarExcecaoAoAtualizarAgendamentoInexistente() {
        when(agendamentoRepository.existsById(100L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> agendamentoService.atualizar(agendamentoValido));
    }

    @Test
    @Order(14)
    @DisplayName("CT120 - Lançar exceção ao atualizar agendamento com ID nulo")
    void deveLancarExcecaoAoAtualizarAgendamentoSemId() {
        agendamentoValido.setId(null);

        assertThrows(EntityNotFoundException.class, () -> agendamentoService.atualizar(agendamentoValido));
    }

    @Test
    @Order(15)
    @DisplayName("CT121 - Atualizar agendamento quando conflito for com ele mesmo")
    void deveAtualizarAgendamentoIgnorandoConflitoComEleMesmo() {
        when(agendamentoRepository.existsById(100L)).thenReturn(true);
        when(agendamentoRepository.existeConflitoDeHorario(any(), any(), eq(3L), eq(2))).thenReturn(true);
        when(agendamentoRepository.findByHorario(any(), any(), eq(3L), eq(2))).thenReturn(List.of(agendamentoValido));
        when(agendamentoRepository.findById(100L)).thenReturn(Optional.of(agendamentoValido));

        assertDoesNotThrow(() -> agendamentoService.atualizar(agendamentoValido));
        verify(emailSender, times(1)).enviarInformacaoCadastroAgendamentoEmail(any(), any());
        verify(agendamentoRepository).saveAndFlush(any(Agendamento.class));
    }

    @Test
    @Order(16)
    @DisplayName("CT122 - Deletar agendamento com sucesso")
    void deveDeletarAgendamentoComSucesso() {
        when(agendamentoRepository.existsById(100L)).thenReturn(true);

        assertDoesNotThrow(() -> agendamentoService.deletar(100L));
        verify(agendamentoRepository).deleteById(100L);
    }

    @Test
    @Order(17)
    @DisplayName("CT123 - Listar agendamento status")
    void deveListarAgendamentoStatus() {
        when(agendamentoStatusRepository.findAll()).thenReturn(List.of(new AgendamentoStatus()));

        List<AgendamentoStatus> resultado = agendamentoService.listarAgendamentoStatus();

        assertFalse(resultado.isEmpty());
    }

    @Test
    @Order(18)
    @DisplayName("CT124 - Listar agendamento tipo")
    void deveListarAgendamentoTipo() {
        when(agendamentoTipoRepository.findAll()).thenReturn(List.of(new AgendamentoTipo()));

        List<AgendamentoTipo> resultado = agendamentoService.listarAgendamentoTipo();

        assertFalse(resultado.isEmpty());
    }

    @Test
    @Order(19)
    @DisplayName("CT125 - Listar agendamentos na data")
    void deveListarAgendamentosNaData() {
        when(agendamentoRepository.findAgendamentosNaData(any(LocalDateTime.class), any(LocalDateTime.class), eq(2))).thenReturn(List.of(agendamentoValido));

        List<Agendamento> resultado = agendamentoService.listarAgendamentosNaData(LocalDate.now());

        assertFalse(resultado.isEmpty());
    }

    @Test
    @Order(20)
    @DisplayName("CT126 - Listar agendamentos por veterinário na data")
    void deveListarAgendamentosByVeterinarioNaData() {
        when(agendamentoRepository.findAgendamentosByVeterinarioNaData(eq(3L), any(LocalDateTime.class), any(LocalDateTime.class), eq(2))).thenReturn(List.of(agendamentoValido));

        List<Agendamento> resultado = agendamentoService.listarAgendamentosByVeterinarioNaData(3L, LocalDate.now());

        assertFalse(resultado.isEmpty());
    }

    @Test
    @Order(21)
    @DisplayName("CT127 - Listar agendamentos por veterinário")
    void deveListarAgendamentosByVeterinario() {
        when(agendamentoRepository.findAgendamentosByVeterinario(3L)).thenReturn(List.of(agendamentoValido));

        List<Agendamento> resultado = agendamentoService.listarAgendamentosByVeterinario(3L);

        assertFalse(resultado.isEmpty());
    }

    @Test
    @Order(22)
    @DisplayName("CT128 - Salvar resultado consulta com sucesso")
    void deveSalvarResultadoConsultaComSucesso() {
        when(agendamentoRepository.findById(100L)).thenReturn(Optional.of(agendamentoValido));
        when(resultadoConsultaRepository.existsByAgendamentoId(100L)).thenReturn(false);
        when(resultadoConsultaRepository.save(any(ResultadoConsulta.class))).thenReturn(resultadoConsultaValido);
        when(agendamentoStatusRepository.findById(3)).thenReturn(Optional.of(new AgendamentoStatus(3, "CONCLUÍDO")));

        ResultadoConsulta resultado = agendamentoService.salvarResultadoConsulta(resultadoConsultaValido);

        assertNotNull(resultado);
        assertNotNull(resultadoConsultaValido.getPrescricoes().get(0).getResultadoConsulta());
        verify(agendamentoRepository).save(agendamentoValido);
    }

    @Test
    @Order(23)
    @DisplayName("CT129 - Salvar resultado consulta sem prescrições com sucesso")
    void deveSalvarResultadoConsultaSemPrescricoesComSucesso() {
        resultadoConsultaValido.setPrescricoes(null);
        when(agendamentoRepository.findById(100L)).thenReturn(Optional.of(agendamentoValido));
        when(resultadoConsultaRepository.existsByAgendamentoId(100L)).thenReturn(false);
        when(resultadoConsultaRepository.save(any(ResultadoConsulta.class))).thenReturn(resultadoConsultaValido);
        when(agendamentoStatusRepository.findById(3)).thenReturn(Optional.of(new AgendamentoStatus(3, "CONCLUÍDO")));

        ResultadoConsulta resultado = agendamentoService.salvarResultadoConsulta(resultadoConsultaValido);

        assertNotNull(resultado);
        verify(agendamentoRepository).save(agendamentoValido);
    }

    @Test
    @Order(24)
    @DisplayName("CT130 - Lançar exceção ao salvar resultado com agendamento inexistente")
    void deveLancarExcecaoAoSalvarResultadoComAgendamentoInexistente() {
        when(agendamentoRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> agendamentoService.salvarResultadoConsulta(resultadoConsultaValido));
    }

    @Test
    @Order(25)
    @DisplayName("CT131 - Lançar exceção ao salvar resultado sem encontrar status concluído")
    void deveLancarExcecaoAoSalvarResultadoSemEncontrarStatusConcluido() {
        when(agendamentoRepository.findById(100L)).thenReturn(Optional.of(agendamentoValido));
        when(resultadoConsultaRepository.existsByAgendamentoId(100L)).thenReturn(false);
        when(resultadoConsultaRepository.save(any(ResultadoConsulta.class))).thenReturn(resultadoConsultaValido);
        when(agendamentoStatusRepository.findById(3)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> agendamentoService.salvarResultadoConsulta(resultadoConsultaValido));
    }

    @Test
    @Order(26)
    @DisplayName("CT132 - Lançar exceção ao salvar resultado duplicado")
    void deveLancarExcecaoAoSalvarResultadoDuplicado() {
        when(agendamentoRepository.findById(100L)).thenReturn(Optional.of(agendamentoValido));
        when(resultadoConsultaRepository.existsByAgendamentoId(100L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> agendamentoService.salvarResultadoConsulta(resultadoConsultaValido));
    }

    @Test
    @Order(27)
    @DisplayName("CT133 - Recuperar resultado por agendamento")
    void deveRecuperarResultadoPorAgendamento() {
        when(resultadoConsultaRepository.findByAgendamentoId(100L)).thenReturn(Optional.of(resultadoConsultaValido));

        ResultadoConsulta resultado = agendamentoService.recuperarResultadoPorAgendamento(100L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @Order(28)
    @DisplayName("CT134 - Retornar nulo ao recuperar resultado inexistente")
    void deveRetornarNullAoRecuperarResultadoInexistente() {
        when(resultadoConsultaRepository.findByAgendamentoId(100L)).thenReturn(Optional.empty());

        ResultadoConsulta resultado = agendamentoService.recuperarResultadoPorAgendamento(100L);

        assertNull(resultado);
    }

    @Test
    @Order(29)
    @DisplayName("CT135 - Lançar exceção quando agendamento nulo")
    void deveLancarExcecaoQuandoAgendamentoNulo() {
        assertThrows(IllegalArgumentException.class, () -> agendamentoService.salvar(null));
    }

    @Test
    @Order(30)
    @DisplayName("CT136 - Lançar exceção quando data de início nula")
    void deveLancarExcecaoQuandoDataInicioNula() {
        agendamentoValido.setDataAgendamentoInicio(null);

        assertThrows(IllegalArgumentException.class, () -> agendamentoService.salvar(agendamentoValido));
    }

    @Test
    @Order(31)
    @DisplayName("CT137 - Lançar exceção quando data de fim nula")
    void deveLancarExcecaoQuandoDataFinalNula() {
        agendamentoValido.setDataAgendamentoFinal(null);

        assertThrows(IllegalArgumentException.class, () -> agendamentoService.salvar(agendamentoValido));
    }

    @Test
    @Order(32)
    @DisplayName("CT138 - Lançar exceção quando animal id nulo")
    void deveLancarExcecaoQuandoAnimalIdNulo() {
        agendamentoValido.getAnimal().setId(null);

        assertThrows(IllegalArgumentException.class, () -> agendamentoService.salvar(agendamentoValido));
    }

    @Test
    @Order(33)
    @DisplayName("CT139 - Lançar exceção quando recepcionista nulo")
    void deveLancarExcecaoQuandoRecepcionistaNulo() {
        agendamentoValido.setRecepcionista(null);

        assertThrows(IllegalArgumentException.class, () -> agendamentoService.salvar(agendamentoValido));
    }

    @Test
    @Order(34)
    @DisplayName("CT140 - Lançar exceção quando recepcionista id nulo")
    void deveLancarExcecaoQuandoRecepcionistaIdNulo() {
        agendamentoValido.getRecepcionista().setId(null);

        assertThrows(IllegalArgumentException.class, () -> agendamentoService.salvar(agendamentoValido));
    }

    @Test
    @Order(35)
    @DisplayName("CT141 - Lançar exceção genérica ao listar agendamentos")
    void deveLancarExcecaoGenericaAoListarAgendamentos() {
        when(agendamentoRepository.findAll(pageable)).thenThrow(new RuntimeException("DB Timeout"));
        assertThrows(RuntimeException.class, () -> agendamentoService.listar(pageable));
    }

    @Test
    @Order(36)
    @DisplayName("CT142 - Lançar exceção genérica ao listar por animal id")
    void deveLancarExcecaoGenericaAoListarPorAnimalId() {
        when(animalRepository.existsById(anyLong())).thenReturn(true);
        when(agendamentoRepository.findByAnimal(anyLong(), any(Pageable.class))).thenThrow(new RuntimeException("DB Lock"));
        assertThrows(RuntimeException.class, () -> agendamentoService.listarByAnimalId(1L, pageable));
    }

    @Test
    @Order(37)
    @DisplayName("CT143 - Lançar exceção genérica ao listar por usuario id")
    void deveLancarExcecaoGenericaAoListarPorUsuarioId() {
        when(usuarioRepository.existsById(anyLong())).thenReturn(true);
        when(agendamentoRepository.findByUsuario(anyLong(), any(Pageable.class))).thenThrow(new RuntimeException("DB Conn"));
        assertThrows(RuntimeException.class, () -> agendamentoService.listarByUsuarioId(1L, pageable));
    }

    @Test
    @Order(38)
    @DisplayName("CT144 - Cobrir catch genérico ao deletar agendamento")
    void deveLancarExcecaoGenericaAoDeletarAgendamento() {
        when(agendamentoRepository.existsById(100L)).thenReturn(true);
        doThrow(new RuntimeException("DB Lock")).when(agendamentoRepository).deleteById(100L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> agendamentoService.deletar(100L));
        assertTrue(exception.getMessage().contains("Erro ao deletar agendamento"));
    }

    @Test
    @Order(39)
    @DisplayName("CT145 - Cobrir catch genérico em recuperar")
    void deveCobrirCatchEmRecuperarAgendamento() {
        when(agendamentoRepository.findById(anyLong())).thenThrow(new RuntimeException("DB Fail"));
        assertThrows(RuntimeException.class, () -> agendamentoService.recuperar(1L));
    }
}