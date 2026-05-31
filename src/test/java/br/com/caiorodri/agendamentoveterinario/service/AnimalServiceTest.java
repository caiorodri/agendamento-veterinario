package br.com.caiorodri.agendamentoveterinario.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
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
import br.com.caiorodri.agendamentoveterinario.model.Animal;
import br.com.caiorodri.agendamentoveterinario.model.Especie;
import br.com.caiorodri.agendamentoveterinario.model.Raca;
import br.com.caiorodri.agendamentoveterinario.model.Sexo;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.repository.AnimalRepository;
import br.com.caiorodri.agendamentoveterinario.repository.EspecieRepository;
import br.com.caiorodri.agendamentoveterinario.repository.RacaRepository;
import br.com.caiorodri.agendamentoveterinario.repository.SexoRepository;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(6)
class AnimalServiceTest {

    @InjectMocks
    private AnimalService animalService;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RacaRepository racaRepository;

    @Mock
    private SexoRepository sexoRepository;

    @Mock
    private EspecieRepository especieRepository;

    @Mock
    private EmailSender emailSender;

    private Animal animalValido;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        Usuario donoValido = new Usuario();
        donoValido.setId(1L);

        Raca racaValida = new Raca();
        racaValida.setId(5);

        Sexo sexoValido = new Sexo();
        sexoValido.setId(1);

        animalValido = new Animal();
        animalValido.setId(1L);
        animalValido.setNome("Rex");
        animalValido.setDono(donoValido);
        animalValido.setRaca(racaValida);
        animalValido.setSexo(sexoValido);
        animalValido.setAgendamentos(new ArrayList<>());

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @Order(1)
    @DisplayName("CT76 - Impedir exclusão e lançar exceção se possuir histórico")
    void deveLancarExcecaoAoDeletarAnimalComHistorico() {
        when(animalRepository.existsById(1L)).thenReturn(true);
        animalValido.getAgendamentos().add(new Agendamento(99L));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animalValido));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> animalService.deletar(1L));

        assertNotNull(exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("possui agendamentos associados"));
        verify(animalRepository, never()).deleteById(anyLong());
    }

    @Test
    @Order(2)
    @DisplayName("CT77 - Deletar o pet com sucesso quando não possuir agendamentos")
    void deveDeletarAnimalSemHistorico() {
        when(animalRepository.existsById(1L)).thenReturn(true);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animalValido));

        assertDoesNotThrow(() -> animalService.deletar(1L));
        verify(animalRepository).deleteById(1L);
    }

    @Test
    @Order(3)
    @DisplayName("CT78 - Salvar novo animal sucesso quando dados e FKs válidos")
    void deveSalvarAnimalComSucesso() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(racaRepository.existsById(5)).thenReturn(true);
        when(sexoRepository.existsById(1)).thenReturn(true);
        when(animalRepository.save(any(Animal.class))).thenReturn(animalValido);

        Animal resultado = animalService.salvar(animalValido);

        assertNotNull(resultado);
        verify(animalRepository).save(animalValido);
        verify(emailSender).enviarInformacaoCadastroAnimalEmail(animalValido, false);
    }

    @Test
    @Order(4)
    @DisplayName("CT79 - Lançar exceção ao tentar salvar pet sem informar o dono")
    void deveLancarExcecaoQuandoDonoNulo() {
        animalValido.setDono(null);

        assertThrows(IllegalArgumentException.class, () -> animalService.salvar(animalValido));
    }

    @Test
    @Order(5)
    @DisplayName("CT80 - Atualizar peso, idade ou raça do pet com sucesso")
    void deveAtualizarAnimalComSucesso() {
        when(animalRepository.existsById(1L)).thenReturn(true);
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(racaRepository.existsById(5)).thenReturn(true);
        when(sexoRepository.existsById(1)).thenReturn(true);
        when(animalRepository.save(any(Animal.class))).thenReturn(animalValido);

        Animal resultado = animalService.atualizar(animalValido);

        assertNotNull(resultado);
        verify(animalRepository).save(animalValido);
        verify(emailSender).enviarInformacaoCadastroAnimalEmail(animalValido, true);
    }

    @Test
    @Order(6)
    @DisplayName("CT81 - Listar todos os pets vinculados ao cliente com sucesso")
    void deveListarPetsPorDono() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(animalRepository.findByUsuarioId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(animalValido)));

        Page<Animal> resultado = animalService.listarByDonoId(1L, pageable);

        assertFalse(resultado.isEmpty());
        verify(animalRepository).findByUsuarioId(1L, pageable);
    }

    @Test
    @Order(7)
    @DisplayName("CT82 - Lançar exceção se ID do dono não existir no banco ao salvar")
    void deveLancarExcecaoQuandoDonoNaoExistirNoBanco() {
        when(usuarioRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> animalService.salvar(animalValido));
    }

    @Test
    @Order(8)
    @DisplayName("CT83 - Recuperar animal com sucesso")
    void deveRecuperarAnimalComSucesso() {
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animalValido));

        Animal resultado = animalService.recuperar(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @Order(9)
    @DisplayName("CT84 - Lançar exceção ao recuperar animal inexistente")
    void deveLancarExcecaoAoRecuperarAnimalInexistente() {
        when(animalRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> animalService.recuperar(999L));
    }

    @Test
    @Order(10)
    @DisplayName("CT85 - Listar animais com paginação")
    void deveListarAnimaisComPaginacao() {
        when(animalRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(animalValido)));

        Page<Animal> resultado = animalService.listar(pageable);

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @Order(11)
    @DisplayName("CT86 - Listar animais sem paginação")
    void deveListarAnimaisSemPaginacao() {
        when(animalRepository.findAll()).thenReturn(List.of(animalValido));

        List<Animal> resultado = animalService.listar();

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    @Order(12)
    @DisplayName("CT87 - Lançar exceção ao atualizar animal inexistente")
    void deveLancarExcecaoAoAtualizarAnimalInexistente() {
        when(animalRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> animalService.atualizar(animalValido));
    }

    @Test
    @Order(13)
    @DisplayName("CT88 - Lançar exceção ao atualizar animal com id nulo")
    void deveLancarExcecaoAoAtualizarAnimalComIdNulo() {
        animalValido.setId(null);

        assertThrows(EntityNotFoundException.class, () -> animalService.atualizar(animalValido));
    }

    @Test
    @Order(14)
    @DisplayName("CT89 - Lançar exceção ao deletar animal inexistente")
    void deveLancarExcecaoAoDeletarAnimalInexistente() {
        when(animalRepository.existsById(999L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> animalService.deletar(999L));
    }

    @Test
    @Order(15)
    @DisplayName("CT90 - Lançar exceção ao salvar animal com nome nulo")
    void deveLancarExcecaoAoSalvarAnimalComNomeNulo() {
        animalValido.setNome(null);

        assertThrows(IllegalArgumentException.class, () -> animalService.salvar(animalValido));
    }

    @Test
    @Order(16)
    @DisplayName("CT91 - Lançar exceção ao salvar animal com nome vazio")
    void deveLancarExcecaoAoSalvarAnimalComNomeVazioString() {
        animalValido.setNome("");
        assertThrows(IllegalArgumentException.class, () -> animalService.salvar(animalValido));
    }

    @Test
    @Order(17)
    @DisplayName("CT92 - Lançar exceção ao salvar animal com nome em branco")
    void deveLancarExcecaoAoSalvarAnimalComNomeVazio() {
        animalValido.setNome("   ");

        assertThrows(IllegalArgumentException.class, () -> animalService.salvar(animalValido));
    }

    @Test
    @Order(18)
    @DisplayName("CT93 - Lançar exceção ao salvar animal sem raça")
    void deveLancarExcecaoQuandoRacaNula() {
        animalValido.setRaca(null);

        assertThrows(IllegalArgumentException.class, () -> animalService.salvar(animalValido));
    }

    @Test
    @Order(19)
    @DisplayName("CT94 - Lançar exceção ao salvar animal sem id da raça")
    void deveLancarExcecaoQuandoRacaIdNula() {
        animalValido.getRaca().setId(null);

        assertThrows(IllegalArgumentException.class, () -> animalService.salvar(animalValido));
    }

    @Test
    @Order(20)
    @DisplayName("CT95 - Lançar exceção ao salvar animal sem sexo")
    void deveLancarExcecaoQuandoSexoNulo() {
        animalValido.setSexo(null);

        assertThrows(IllegalArgumentException.class, () -> animalService.salvar(animalValido));
    }

    @Test
    @Order(21)
    @DisplayName("CT96 - Lançar exceção ao salvar animal sem id do sexo")
    void deveLancarExcecaoQuandoSexoIdNulo() {
        animalValido.getSexo().setId(null);

        assertThrows(IllegalArgumentException.class, () -> animalService.salvar(animalValido));
    }

    @Test
    @Order(22)
    @DisplayName("CT97 - Lançar exceção quando a raça não for encontrada no banco")
    void deveLancarExcecaoQuandoRacaNaoEncontrada() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(racaRepository.existsById(5)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> animalService.salvar(animalValido));
    }

    @Test
    @Order(23)
    @DisplayName("CT98 - Lançar exceção quando o sexo não for encontrado no banco")
    void deveLancarExcecaoQuandoSexoNaoEncontrado() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(racaRepository.existsById(5)).thenReturn(true);
        when(sexoRepository.existsById(1)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> animalService.salvar(animalValido));
    }

    @Test
    @Order(24)
    @DisplayName("CT99 - Lançar exceção ao listar animais genérica")
    void deveCobrirCatchEmListarPaginado() {
        when(animalRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("DB Out"));

        assertThrows(RuntimeException.class, () -> animalService.listar(pageable));
    }

    @Test
    @Order(25)
    @DisplayName("CT100 - Listar sexos")
    void deveListarSexos() {
        when(sexoRepository.findAll()).thenReturn(List.of(new Sexo()));

        List<Sexo> resultado = animalService.listarSexos();

        assertFalse(resultado.isEmpty());
    }

    @Test
    @Order(26)
    @DisplayName("CT101 - Listar raças por espécie")
    void deveListarRacasPorEspecie() {
        when(racaRepository.findByEspecie(1)).thenReturn(List.of(new Raca()));

        List<Raca> resultado = animalService.listarRacasByIdEspecie(1);

        assertFalse(resultado.isEmpty());
    }

    @Test
    @Order(27)
    @DisplayName("CT102 - Listar raças")
    void deveListarRacas() {
        when(racaRepository.findAll()).thenReturn(List.of(new Raca()));

        List<Raca> resultado = animalService.listarRacas();

        assertFalse(resultado.isEmpty());
    }

    @Test
    @Order(28)
    @DisplayName("CT103 - Listar espécies")
    void deveListarEspecies() {
        when(especieRepository.findAll()).thenReturn(List.of(new Especie()));

        List<Especie> resultado = animalService.listarEspecies();

        assertFalse(resultado.isEmpty());
    }

    @Test
    @Order(29)
    @DisplayName("CT104 - Cobrir catch genérico em recuperar")
    void deveCobrirCatchEmRecuperar() {
        when(animalRepository.findById(anyLong())).thenThrow(new RuntimeException("DB Out"));

        assertThrows(RuntimeException.class, () -> animalService.recuperar(1L));
    }

    @Test
    @Order(30)
    @DisplayName("CT105 - Cobrir catch genérico em listarByDonoId")
    void deveCobrirCatchEmListarByDonoId() {
        when(usuarioRepository.existsById(anyLong())).thenReturn(true);
        when(animalRepository.findByUsuarioId(anyLong(), any(Pageable.class))).thenThrow(new RuntimeException("DB Out"));

        assertThrows(RuntimeException.class, () -> animalService.listarByDonoId(1L, pageable));
    }

    @Test
    @Order(31)
    @DisplayName("CT106 - Cobrir catch genérico em atualizar")
    void deveCobrirCatchEmAtualizar() {
        when(animalRepository.existsById(anyLong())).thenReturn(true);
        when(usuarioRepository.existsById(anyLong())).thenReturn(true);
        when(racaRepository.existsById(anyInt())).thenReturn(true);
        when(sexoRepository.existsById(anyInt())).thenReturn(true);
        when(animalRepository.save(any(Animal.class))).thenThrow(new RuntimeException("DB Out"));

        assertThrows(RuntimeException.class, () -> animalService.atualizar(animalValido));
    }
}