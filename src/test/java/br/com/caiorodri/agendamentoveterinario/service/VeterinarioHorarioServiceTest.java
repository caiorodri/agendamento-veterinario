package br.com.caiorodri.agendamentoveterinario.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.caiorodri.agendamentoveterinario.model.VeterinarioHorario;
import br.com.caiorodri.agendamentoveterinario.repository.VeterinarioHorarioRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(5)
class VeterinarioHorarioServiceTest {

    @InjectMocks
    private VeterinarioHorarioService horarioService;

    @Mock
    private VeterinarioHorarioRepository repository;

    private VeterinarioHorario horarioValido;

    @BeforeEach
    void setUp() {
        horarioValido = new VeterinarioHorario();
        horarioValido.setId(1L);
        horarioValido.setHoraInicio(LocalTime.of(8, 0));
        horarioValido.setHoraFim(LocalTime.of(12, 0));
    }

    @Test
    @Order(1)
    @DisplayName("CT70 - Listar os horários de um veterinário pelo ID")
    void deveListarHorariosPorVeterinario() {
        Long idVeterinario = 3L;
        when(repository.findByVeterinarioIdOrderByDiaSemanaIdAscHoraInicioAsc(idVeterinario))
                .thenReturn(List.of(horarioValido));

        List<VeterinarioHorario> resultado = horarioService.listarPorVeterinario(idVeterinario);

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(repository, times(1)).findByVeterinarioIdOrderByDiaSemanaIdAscHoraInicioAsc(idVeterinario);
    }

    @Test
    @Order(2)
    @DisplayName("CT71 - Salvar horário com sucesso quando hora início < fim")
    void deveSalvarHorarioComSucesso() {
        when(repository.save(any(VeterinarioHorario.class))).thenReturn(horarioValido);

        VeterinarioHorario resultado = horarioService.salvar(horarioValido);

        assertNotNull(resultado);
        assertEquals(LocalTime.of(8, 0), resultado.getHoraInicio());
        verify(repository, times(1)).save(horarioValido);
    }

    @Test
    @Order(3)
    @DisplayName("CT72 - Lançar exceção quando hora início > fim")
    void deveLancarExcecaoQuandoInicioForDepoisDoFim() {
        horarioValido.setHoraInicio(LocalTime.of(14, 0));
        horarioValido.setHoraFim(LocalTime.of(10, 0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            horarioService.salvar(horarioValido);
        });

        assertEquals("A hora de início deve ser anterior à hora de fim.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @Order(4)
    @DisplayName("CT73 - Lançar exceção quando hora início = fim")
    void deveLancarExcecaoQuandoInicioForIgualAoFim() {
        horarioValido.setHoraInicio(LocalTime.of(10, 0));
        horarioValido.setHoraFim(LocalTime.of(10, 0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            horarioService.salvar(horarioValido);
        });

        assertEquals("A hora de inicio e fim não podem ser iguais.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @Order(5)
    @DisplayName("CT74 - Deletar horário com sucesso quando o ID existir")
    void deveDeletarComSucesso() {
        when(repository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> {
            horarioService.deletar(1L);
        });

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    @Order(6)
    @DisplayName("CT75 - Lançar exceção ao tentar deletar um horário inexistente")
    void deveLancarExcecaoAoDeletarHorarioInexistente() {
        when(repository.existsById(99L)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            horarioService.deletar(99L);
        });

        assertEquals("Horário não encontrado.", exception.getMessage());
        verify(repository, never()).deleteById(anyLong());
    }
}