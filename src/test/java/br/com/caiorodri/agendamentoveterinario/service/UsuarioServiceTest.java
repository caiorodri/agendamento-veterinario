package br.com.caiorodri.agendamentoveterinario.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.caiorodri.agendamentoveterinario.email.EmailSender;
import br.com.caiorodri.agendamentoveterinario.enums.DiaSemanaEnum;
import br.com.caiorodri.agendamentoveterinario.model.Agendamento;
import br.com.caiorodri.agendamentoveterinario.model.AgendamentoTipo;
import br.com.caiorodri.agendamentoveterinario.model.Estado;
import br.com.caiorodri.agendamentoveterinario.model.Perfil;
import br.com.caiorodri.agendamentoveterinario.model.Status;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.model.UsuarioAlterarSenha;
import br.com.caiorodri.agendamentoveterinario.model.VeterinarioHorario;
import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoRepository;
import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoTipoRepository;
import br.com.caiorodri.agendamentoveterinario.repository.EstadoRepository;
import br.com.caiorodri.agendamentoveterinario.repository.StatusRepository;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;
import br.com.caiorodri.agendamentoveterinario.repository.VeterinarioHorarioRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(3)
class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailSender emailSender;

    @Mock
    private VeterinarioHorarioRepository veterinarioHorarioRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AgendamentoTipoRepository agendamentoTipoRepository;

    @Mock
    private EstadoRepository estadoRepository;

    private Usuario usuarioValido;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        usuarioValido = new Usuario();
        usuarioValido.setId(1L);
        usuarioValido.setNome("Caio Rodrigues");
        usuarioValido.setEmail("caio@teste.com");
        usuarioValido.setCpf("34920375000");
        usuarioValido.setSenha("senhaSegura123");
        usuarioValido.setPerfil(new Perfil(2, "CLIENTE"));
        usuarioValido.setTelefones(new HashSet<>());
        usuarioValido.setReceberEmail(true);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @Order(1)
    @DisplayName("CT01 - Lançar exceção ao salvar usuário com CPF já existente")
    void deveLancarExcecaoAoSalvarUsuarioComCpfExistente() {
        when(usuarioRepository.existsByCpf(usuarioValido.getCpf())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.salvar(usuarioValido));
    }

    @Test
    @Order(2)
    @DisplayName("CT02 - Garantir senha encriptada e salvar com sucesso")
    void deveSalvarUsuarioComSucesso() {
        when(usuarioRepository.existsByCpf(usuarioValido.getCpf())).thenReturn(false);
        when(usuarioRepository.findByEmail(usuarioValido.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(usuarioValido.getSenha())).thenReturn("hashSecret");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioValido);

        Usuario resultado = usuarioService.salvar(usuarioValido);

        assertNotNull(resultado);
        assertEquals("hashSecret", resultado.getSenha());
        verify(emailSender, times(1)).enviarInformacaoCadastroUsuarioEmail(any());
        verify(usuarioRepository).save(usuarioValido);
    }

    @Test
    @Order(3)
    @DisplayName("CT03 - Atualizar dados adicionais como telefones e URL imagem")
    void deveAtualizarUsuarioComSucesso() {
        Usuario usuarioExistenteNoBanco = new Usuario();
        usuarioExistenteNoBanco.setId(1L);
        usuarioExistenteNoBanco.setNome("Caio Rodrigues");
        usuarioExistenteNoBanco.setEmail("caio@teste.com");
        usuarioExistenteNoBanco.setTelefones(new HashSet<>());

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistenteNoBanco));
        when(usuarioRepository.findByEmail(usuarioValido.getEmail())).thenReturn(Optional.of(usuarioExistenteNoBanco));
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenReturn(usuarioExistenteNoBanco);
        when(usuarioRepository.findByIdWithSets(1L)).thenReturn(Optional.of(usuarioExistenteNoBanco));
        when(usuarioRepository.findByIdWithAgendamentos(1L)).thenReturn(Optional.of(usuarioExistenteNoBanco));

        usuarioValido.getTelefones().add("11999999999");
        usuarioValido.setUrlImagem("http://imagem.com/foto.png");

        Usuario resultado = usuarioService.atualizar(usuarioValido);

        assertNotNull(resultado);
        assertFalse(resultado.getTelefones().isEmpty());
        assertEquals("http://imagem.com/foto.png", resultado.getUrlImagem());
    }

    @Test
    @Order(4)
    @DisplayName("CT04 - Deletar o usuário com sucesso quando ID existir")
    void deveDeletarUsuarioComSucesso() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> usuarioService.deletar(1L));
        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @Order(5)
    @DisplayName("CT05 - Recuperar o usuário por ID com sucesso")
    void deveRecuperarUsuarioPorId() {
        when(usuarioRepository.findByIdWithSets(1L)).thenReturn(Optional.of(usuarioValido));
        when(usuarioRepository.findByIdWithAgendamentos(1L)).thenReturn(Optional.of(usuarioValido));

        Usuario resultado = usuarioService.recuperar(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @Order(6)
    @DisplayName("CT06 - Lançar exceção ao salvar usuário com E-mail já existente")
    void deveLancarExcecaoAoSalvarUsuarioComEmailExistente() {
        Usuario usuarioExistente = new Usuario(2L);
        when(usuarioRepository.existsByCpf(usuarioValido.getCpf())).thenReturn(false);
        when(usuarioRepository.findByEmail(usuarioValido.getEmail())).thenReturn(Optional.of(usuarioExistente));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.salvar(usuarioValido));
    }

    @Test
    @Order(7)
    @DisplayName("CT07 - Listar usuários com paginação")
    void deveListarUsuariosComPaginacao() {
        when(usuarioRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(usuarioValido)));
        assertFalse(usuarioService.listar(pageable).isEmpty());
    }

    @Test
    @Order(8)
    @DisplayName("CT08 - Recuperar usuário por email com sucesso")
    void deveRecuperarUsuarioPorEmail() {
        when(usuarioRepository.findByEmailWithSets(anyString())).thenReturn(Optional.of(usuarioValido));
        when(usuarioRepository.findByIdWithAgendamentos(1L)).thenReturn(Optional.of(usuarioValido));

        Usuario resultado = usuarioService.recuperarByEmail("caio@teste.com");

        assertNotNull(resultado);
        assertEquals("caio@teste.com", resultado.getEmail());
    }

    @Test
    @Order(9)
    @DisplayName("CT09 - Lançar exceção ao recuperar usuário por email inexistente")
    void deveLancarExcecaoAoRecuperarUsuarioPorEmailInexistente() {
        when(usuarioRepository.findByEmailWithSets(anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.recuperarByEmail("inexistente@teste.com"));
    }

    @Test
    @Order(10)
    @DisplayName("CT10 - Lançar exceção ao atualizar usuário inexistente")
    void deveLancarExcecaoAoAtualizarUsuarioInexistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.atualizar(usuarioValido));
    }

    @Test
    @Order(11)
    @DisplayName("CT11 - Alterar senha com sucesso")
    void deveAlterarSenhaComSucesso() {
        UsuarioAlterarSenha dto = new UsuarioAlterarSenha("senhaAntiga123", "novaSenhaSegura!");

        when(passwordEncoder.matches("senhaAntiga123", usuarioValido.getSenha())).thenReturn(true);
        when(passwordEncoder.matches("novaSenhaSegura!", usuarioValido.getSenha())).thenReturn(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
        when(passwordEncoder.encode("novaSenhaSegura!")).thenReturn("hashNovo");

        assertDoesNotThrow(() -> usuarioService.alterarSenha(usuarioValido, dto));
        verify(usuarioRepository).save(usuarioValido);
    }

    @Test
    @Order(12)
    @DisplayName("CT12 - Lançar exceção ao alterar senha com senha nova inválida")
    void deveLancarExcecaoAoAlterarSenhaComNovaSenhaCurta() {
        UsuarioAlterarSenha dto = new UsuarioAlterarSenha("senhaAntiga123", "1234567");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.alterarSenha(usuarioValido, dto));
    }

    @Test
    @Order(13)
    @DisplayName("CT13 - Lançar exceção ao alterar senha com senha nova em branco")
    void deveLancarExcecaoAoAlterarSenhaComNovaSenhaEmBranco() {
        UsuarioAlterarSenha dto = new UsuarioAlterarSenha("senhaAntiga123", "   ");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.alterarSenha(usuarioValido, dto));
    }

    @Test
    @Order(14)
    @DisplayName("CT14 - Lançar exceção ao deletar usuário inexistente")
    void deveLancarExcecaoAoDeletarUsuarioInexistente() {
        when(usuarioRepository.existsById(999L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> usuarioService.deletar(999L));
    }

    @Test
    @Order(15)
    @DisplayName("CT15 - Listar status")
    void deveListarStatus() {
        when(statusRepository.findAll()).thenReturn(List.of(new Status(1, "ATIVO")));
        assertFalse(usuarioService.listarStatus().isEmpty());
    }

    @Test
    @Order(16)
    @DisplayName("CT16 - Listar clientes paginado")
    void deveListarClientesPaginado() {
        when(usuarioRepository.findClientes(pageable)).thenReturn(new PageImpl<>(List.of(usuarioValido)));
        assertFalse(usuarioService.listarClientes(pageable).isEmpty());
    }

    @Test
    @Order(17)
    @DisplayName("CT17 - Listar clientes (sem paginação)")
    void deveListarClientesSemPaginacao() {
        when(usuarioRepository.findClientesAtivos()).thenReturn(List.of(usuarioValido));
        assertFalse(usuarioService.listarClientes().isEmpty());
    }

    @Test
    @Order(18)
    @DisplayName("CT18 - Listar recepcionistas")
    void deveListarRecepcionistas() {
        when(usuarioRepository.findRecepcionista()).thenReturn(List.of(usuarioValido));
        assertFalse(usuarioService.listarRecepcionistas().isEmpty());
    }

    @Test
    @Order(19)
    @DisplayName("CT19 - Recuperar recepcionista de auto atendimento")
    void deveRecuperarRecepcionistaAutoAtendimentoComSucesso() {
        Usuario autoAtendimento = new Usuario(2L);
        autoAtendimento.setNome("AUTO ATENDIMENTO");

        when(usuarioRepository.findRecepcionista()).thenReturn(List.of(usuarioValido, autoAtendimento));

        Usuario resultado = usuarioService.recuperarRecepcionistaAutoAtendimento();

        assertNotNull(resultado);
        assertEquals("AUTO ATENDIMENTO", resultado.getNome());
    }

    @Test
    @Order(20)
    @DisplayName("CT20 - Retornar nulo se não encontrar recepcionista auto atendimento")
    void deveRetornarNuloSeRecepcionistaAutoAtendimentoNaoExistir() {
        when(usuarioRepository.findRecepcionista()).thenReturn(List.of(usuarioValido));

        Usuario resultado = usuarioService.recuperarRecepcionistaAutoAtendimento();

        assertNull(resultado);
    }

    @Test
    @Order(21)
    @DisplayName("CT21 - Listar veterinários")
    void deveListarVeterinarios() {
        when(usuarioRepository.findVeterinarios()).thenReturn(List.of(usuarioValido));
        assertFalse(usuarioService.listarVeterinarios().isEmpty());
    }

    @Test
    @Order(22)
    @DisplayName("CT22 - Listar funcionários paginado")
    void deveListarFuncionariosPaginado() {
        when(usuarioRepository.findFuncionarios(pageable)).thenReturn(new PageImpl<>(List.of(usuarioValido)));
        assertFalse(usuarioService.listarFuncionarios(pageable).isEmpty());
    }

    @Test
    @Order(23)
    @DisplayName("CT23 - Listar funcionários")
    void deveListarFuncionarios() {
        when(usuarioRepository.findFuncionarios()).thenReturn(List.of(usuarioValido));
        assertFalse(usuarioService.listarFuncionarios().isEmpty());
    }

    @Test
    @Order(24)
    @DisplayName("CT24 - Listar estados")
    void deveListarEstados() {
        when(estadoRepository.findEstados()).thenReturn(List.of(new Estado("SP", "São Paulo")));
        assertFalse(usuarioService.listarEstados().isEmpty());
    }

    @Test
    @Order(25)
    @DisplayName("CT25 - Enviar código email com sucesso")
    void deveEnviarCodigoEmailComSucesso() {
        when(usuarioRepository.findByEmail(usuarioValido.getEmail())).thenReturn(Optional.of(usuarioValido));

        assertTrue(usuarioService.enviarCodigoEmail(usuarioValido.getEmail()));
        verify(emailSender, times(1)).enviarCodigoEmail(any(), any(), any());
    }

    @Test
    @Order(26)
    @DisplayName("CT26 - Retornar false ao enviar código para email inexistente")
    void deveRetornarFalseAoEnviarCodigoEmailInexistente() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertFalse(usuarioService.enviarCodigoEmail("inexistente@teste.com"));
    }

    @Test
    @Order(27)
    @DisplayName("CT27 - Validar código de recuperação com sucesso")
    void deveValidarCodigoComSucesso() {
        usuarioValido.setCodigoRecuperacao("12345");
        usuarioValido.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(10));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));

        assertTrue(usuarioService.validarCodigo(1L, "12345"));
        assertNull(usuarioValido.getCodigoRecuperacao());
        assertNull(usuarioValido.getExpiracaoCodigo());
        verify(usuarioRepository).save(usuarioValido);
    }

    @Test
    @Order(28)
    @DisplayName("CT28 - Retornar false ao validar código expirado")
    void deveRetornarFalseAoValidarCodigoSemExpiracao() {
        usuarioValido.setExpiracaoCodigo(null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));

        assertFalse(usuarioService.validarCodigo(1L, "12345"));
    }

    @Test
    @Order(29)
    @DisplayName("CT29 - Retornar false ao validar código incorreto e expirado")
    void deveRetornarFalseAoValidarCodigoIncorretoEExpirado() {
        usuarioValido.setCodigoRecuperacao("99999");
        usuarioValido.setExpiracaoCodigo(LocalDateTime.now().minusMinutes(10));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));

        assertFalse(usuarioService.validarCodigo(1L, "12345"));
    }

    @Test
    @Order(30)
    @DisplayName("CT30 - Retornar false se usuário não possuir código ativo")
    void deveRetornarFalseAoValidarCodigoUsuarioInexistente() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertFalse(usuarioService.validarCodigo(999L, "12345"));
    }

    @Test
    @Order(31)
    @DisplayName("CT31 - Enviar email clientes campanha vacinação")
    void deveEnviarEmailClientesCampanhaVacinacao() {
        Usuario clienteSemEmail = new Usuario(2L);
        clienteSemEmail.setReceberEmail(false);

        when(usuarioRepository.findClientesAtivos()).thenReturn(List.of(usuarioValido, clienteSemEmail));

        assertDoesNotThrow(() -> usuarioService.enviarEmailClientesCampanhaVacinacao());
        verify(emailSender, times(1)).enviarInformacaoCampanhaVacinaEmail(usuarioValido);
        verify(emailSender, never()).enviarInformacaoCampanhaVacinaEmail(clienteSemEmail);
    }

    @Test
    @Order(32)
    @DisplayName("CT32 - Listar horários veterinário")
    void deveListarHorariosVeterinario() {
        usuarioValido.setPerfil(new Perfil(3, "VETERINARIO"));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
        when(veterinarioHorarioRepository.findByVeterinarioId(1L)).thenReturn(List.of(new VeterinarioHorario()));

        List<VeterinarioHorario> resultado = usuarioService.listarHorariosVeterinario(1L);

        assertFalse(resultado.isEmpty());
    }

    @Test
    @Order(33)
    @DisplayName("CT33 - Lançar exceção ao listar horários de usuário que não é veterinário")
    void deveLancarExcecaoAoListarHorariosDeNaoVeterinario() {
        usuarioValido.setPerfil(new Perfil(2, "CLIENTE"));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.listarHorariosVeterinario(1L));
    }

    @Test
    @Order(34)
    @DisplayName("CT34 - Listar horários disponíveis")
    void deveListarHorariosDisponiveis() {
        AgendamentoTipo tipo = new AgendamentoTipo(1, "CONSULTA", 30);
        when(agendamentoTipoRepository.findById(1)).thenReturn(Optional.of(tipo));

        LocalDate data = LocalDate.now().plusDays(1);
        int diaSemanaId = DiaSemanaEnum.from(data.getDayOfWeek());

        VeterinarioHorario bloco = new VeterinarioHorario();
        bloco.setHoraInicio(LocalTime.of(8, 0));
        bloco.setHoraFim(LocalTime.of(10, 0));
        when(veterinarioHorarioRepository.findByVeterinarioIdAndDiaDaSemanaId(1L, diaSemanaId)).thenReturn(List.of(bloco));

        Agendamento agendamentoOcupado = new Agendamento();
        agendamentoOcupado.setDataAgendamentoInicio(data.atTime(8, 30));
        agendamentoOcupado.setDataAgendamentoFinal(data.atTime(9, 0));
        when(agendamentoRepository.findAgendamentosByVeterinarioNaData(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq(2))).thenReturn(List.of(agendamentoOcupado));

        List<String> horarios = usuarioService.listarHorariosDisponiveis(1L, data, 1);

        assertEquals(3, horarios.size());
        assertTrue(horarios.contains("08:00"));
        assertTrue(horarios.contains("09:00"));
        assertTrue(horarios.contains("09:30"));
        assertFalse(horarios.contains("08:30"));
    }

    @Test
    @Order(35)
    @DisplayName("CT35 - Retornar vazio se veterinário não trabalha no dia")
    void deveRetornarListaVaziaSeVeterinarioNaoTrabalhaNoDia() {
        AgendamentoTipo tipo = new AgendamentoTipo(1, "CONSULTA", 30);
        when(agendamentoTipoRepository.findById(1)).thenReturn(Optional.of(tipo));
        when(veterinarioHorarioRepository.findByVeterinarioIdAndDiaDaSemanaId(anyLong(), anyInt())).thenReturn(Collections.emptyList());

        List<String> horarios = usuarioService.listarHorariosDisponiveis(1L, LocalDate.now(), 1);

        assertTrue(horarios.isEmpty());
    }

    @Test
    @Order(36)
    @DisplayName("CT36 - Lançar exceção se usuário for nulo")
    void deveLancarExcecaoAoSalvarUsuarioNulo() {
        assertThrows(IllegalArgumentException.class, () -> usuarioService.salvar(null));
    }

    @Test
    @Order(37)
    @DisplayName("CT37 - Lançar exceção se nome for vazio")
    void deveLancarExcecaoAoSalvarUsuarioComNomeVazio() {
        usuarioValido.setNome("");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.salvar(usuarioValido));
    }

    @Test
    @Order(38)
    @DisplayName("CT38 - Lançar exceção se nome for apenas espaços em branco")
    void deveLancarExcecaoAoSalvarUsuarioComNomeEspacosEmBranco() {
        usuarioValido.setNome("   ");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.salvar(usuarioValido));
    }

    @Test
    @Order(39)
    @DisplayName("CT39 - Lançar exceção se email for nulo")
    void deveLancarExcecaoAoSalvarUsuarioComEmailNulo() {
        usuarioValido.setEmail(null);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.salvar(usuarioValido));
    }

    @Test
    @Order(40)
    @DisplayName("CT40 - Lançar exceção se email for apenas espaços em branco")
    void deveLancarExcecaoAoSalvarUsuarioComEmailVazio() {
        usuarioValido.setEmail("   ");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.salvar(usuarioValido));
    }

    @Test
    @Order(41)
    @DisplayName("CT41 - Lançar exceção se senha for vazia em novo usuario")
    void deveLancarExcecaoAoSalvarUsuarioComSenhaVazia() {
        usuarioValido.setSenha("");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.salvar(usuarioValido));
    }

    @Test
    @Order(42)
    @DisplayName("CT42 - Lançar exceção se senha for apenas espaços em branco")
    void deveLancarExcecaoAoSalvarUsuarioComSenhaEspacosEmBranco() {
        usuarioValido.setSenha("   ");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.salvar(usuarioValido));
    }

    @Test
    @Order(43)
    @DisplayName("CT43 - Lançar exceção se email estiver em uso por outro id ao atualizar")
    void deveLancarExcecaoAoAtualizarUsuarioComEmailDeOutro_ct45() {
        Usuario usuarioExistente = new Usuario(99L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
        when(usuarioRepository.findByEmail(usuarioValido.getEmail())).thenReturn(Optional.of(usuarioExistente));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.atualizar(usuarioValido));
    }

    @Test
    @Order(44)
    @DisplayName("CT44 - Continuar loop se falhar envio de campanha")
    void deveProtegerProcessoPrincipalDeCampanhaCasoDbFalhe() {
        when(usuarioRepository.findClientesAtivos()).thenThrow(new RuntimeException("DB Down"));

        assertDoesNotThrow(() -> usuarioService.enviarEmailClientesCampanhaVacinacao());
    }

    @Test
    @Order(45)
    @DisplayName("CT45 - Lançar exceção genérica ao listar status")
    void deveLancarExcecaoGenericaAoListarStatus() {
        when(statusRepository.findAll()).thenThrow(new RuntimeException("DB Error"));
        assertThrows(RuntimeException.class, () -> usuarioService.listarStatus());
    }

    @Test
    @Order(46)
    @DisplayName("CT46 - Lançar exceção genérica ao listar clientes")
    void deveLancarExcecaoGenericaAoListarClientes() {
        when(usuarioRepository.findClientesAtivos()).thenThrow(new RuntimeException("DB Error"));
        assertThrows(RuntimeException.class, () -> usuarioService.listarClientes());
    }

    @Test
    @Order(47)
    @DisplayName("CT47 - Lançar exceção genérica ao recuperar by email")
    void deveLancarExcecaoGenericaEmRecuperarPorEmail() {
        when(usuarioRepository.findByEmailWithSets(anyString())).thenThrow(new RuntimeException("DB"));

        assertThrows(RuntimeException.class, () -> usuarioService.recuperarByEmail("teste@teste.com"));
    }

    @Test
    @Order(48)
    @DisplayName("CT48 - Lançar exceção se alterar senha com senha antiga errada")
    void deveLancarExcecaoAoAlterarSenhaComSenhaAntigaIncorreta_ct50() {
        UsuarioAlterarSenha dto = new UsuarioAlterarSenha("senhaErrada", "novaSenhaSegura!");
        when(passwordEncoder.matches("senhaErrada", usuarioValido.getSenha())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.alterarSenha(usuarioValido, dto));
    }

    @Test
    @Order(49)
    @DisplayName("CT49 - Lançar exceção se nova senha for igual a antiga")
    void deveLancarExcecaoAoAlterarSenhaIgualAntiga_ct51() {
        UsuarioAlterarSenha dto = new UsuarioAlterarSenha("senhaSegura123", "senhaSegura123");
        when(passwordEncoder.matches("senhaSegura123", usuarioValido.getSenha())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.alterarSenha(usuarioValido, dto));
    }

    @Test
    @Order(50)
    @DisplayName("CT50 - Lançar exceção ao recuperar usuário por id inexistente")
    void deveLancarExcecaoAoRecuperarUsuarioPorIdInexistente_ct52() {
        when(usuarioRepository.findByIdWithSets(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.recuperar(999L));
    }

    @Test
    @Order(51)
    @DisplayName("CT51 - Lançar exceção se usuário logado não encontrado ao alterar senha")
    void deveLancarExcecaoAoAlterarSenhaUsuarioInexistente() {
        UsuarioAlterarSenha dto = new UsuarioAlterarSenha("senhaAntiga123", "novaSenhaSegura!");

        when(passwordEncoder.matches("senhaAntiga123", usuarioValido.getSenha())).thenReturn(true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.alterarSenha(usuarioValido, dto));
    }

    @Test
    @Order(52)
    @DisplayName("CT52 - Lançar exceção se veterinário não for encontrado em listar horários")
    void deveLancarExcecaoAoListarHorariosVeterinarioInexistente() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.listarHorariosVeterinario(999L));
    }

    @Test
    @Order(53)
    @DisplayName("CT53 - Lançar exceção se tipo agendamento não encontrado em listar disponiveis")
    void deveLancarExcecaoAoListarHorariosDisponiveisTipoInexistente() {
        when(agendamentoTipoRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.listarHorariosDisponiveis(1L, LocalDate.now(), 999));
    }

    @Test
    @Order(54)
    @DisplayName("CT54 - Cobrir catch genérico em recuperar por id")
    void deveLancarExcecaoGenericaEmRecuperar() {
        when(usuarioRepository.findByIdWithSets(1L)).thenThrow(new RuntimeException("DB"));

        assertThrows(RuntimeException.class, () -> usuarioService.recuperar(1L));
    }

    @Test
    @Order(55)
    @DisplayName("CT55 - Cobrir catch genérico em salvar")
    void deveLancarExcecaoGenericaEmSalvar() {
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenThrow(new RuntimeException("DB"));

        assertThrows(RuntimeException.class, () -> usuarioService.salvar(usuarioValido));
    }

    @Test
    @Order(56)
    @DisplayName("CT56 - Cobrir catch genérico em atualizar")
    void deveLancarExcecaoGenericaEmAtualizar() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new RuntimeException("DB"));

        assertThrows(RuntimeException.class, () -> usuarioService.atualizar(usuarioValido));
    }

    @Test
    @Order(57)
    @DisplayName("CT57 - Cobrir catch genérico em alterar senha")
    void deveLancarExcecaoGenericaEmAlterarSenha() {
        UsuarioAlterarSenha dto = new UsuarioAlterarSenha("senhaAntiga123", "novaSenhaSegura!");
        when(passwordEncoder.matches(anyString(), anyString())).thenThrow(new RuntimeException("Error Crypt"));

        assertThrows(RuntimeException.class, () -> usuarioService.alterarSenha(usuarioValido, dto));
    }

    @Test
    @Order(58)
    @DisplayName("CT58 - Cobrir catch genérico em deletar")
    void deveLancarExcecaoGenericaEmDeletar() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("DB")).when(usuarioRepository).deleteById(1L);

        assertThrows(RuntimeException.class, () -> usuarioService.deletar(1L));
    }

    @Test
    @Order(59)
    @DisplayName("CT59 - Retornar false se ocorrer exceção genérica em enviarCodigoEmail")
    void deveRetornarFalseSeCatchEstourarNoCodigoEmail() {
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new RuntimeException("Falha Banco"));

        assertFalse(usuarioService.enviarCodigoEmail("teste@teste.com"));
    }

    @Test
    @Order(60)
    @DisplayName("CT60 - Retornar false se ocorrer exceção genérica em validarCodigo")
    void deveRetornarFalseSeErroInesperadoNaValidacaoDoCodigo() {
        when(usuarioRepository.findById(anyLong())).thenThrow(new RuntimeException("DB Down"));

        assertFalse(usuarioService.validarCodigo(1L, "123"));
    }

    @Test
    @Order(61)
    @DisplayName("CT61 - Cobrir catch genérico em listar")
    void deveLancarExcecaoGenericaEmListar() {
        when(usuarioRepository.findAll(pageable)).thenThrow(new RuntimeException("DB"));
        assertThrows(RuntimeException.class, () -> usuarioService.listar(pageable));
    }

    @Test
    @Order(62)
    @DisplayName("CT62 - Cobrir catch genérico em listarClientes Paginado")
    void deveCobrirCatchEmListarClientesPaginado() {
        when(usuarioRepository.findClientes(pageable)).thenThrow(new RuntimeException("DB Error"));
        assertThrows(RuntimeException.class, () -> usuarioService.listarClientes(pageable));
    }

    @Test
    @Order(63)
    @DisplayName("CT63 - Cobrir catch genérico em listarRecepcionistas")
    void deveCobrirCatchEmListarRecepcionistas() {
        when(usuarioRepository.findRecepcionista()).thenThrow(new RuntimeException("DB Error"));
        assertThrows(RuntimeException.class, () -> usuarioService.listarRecepcionistas());
    }

    @Test
    @Order(64)
    @DisplayName("CT64 - Cobrir catch genérico em recuperarRecepcionistaAutoAtendimento")
    void deveLancarExcecaoGenericaAoRecuperarAutoAtendimento() {
        when(usuarioRepository.findRecepcionista()).thenThrow(new RuntimeException("Error DB"));

        assertThrows(RuntimeException.class, () -> usuarioService.recuperarRecepcionistaAutoAtendimento());
    }

    @Test
    @Order(65)
    @DisplayName("CT65 - Cobrir catch genérico em listarVeterinarios")
    void deveCobrirCatchEmListarVeterinarios() {
        when(usuarioRepository.findVeterinarios()).thenThrow(new RuntimeException("DB Error"));
        assertThrows(RuntimeException.class, () -> usuarioService.listarVeterinarios());
    }

    @Test
    @Order(66)
    @DisplayName("CT66 - Cobrir catch genérico em listarFuncionarios Paginado")
    void deveCobrirCatchEmListarFuncionariosPaginado() {
        when(usuarioRepository.findFuncionarios(pageable)).thenThrow(new RuntimeException("DB Error"));
        assertThrows(RuntimeException.class, () -> usuarioService.listarFuncionarios(pageable));
    }

    @Test
    @Order(67)
    @DisplayName("CT67 - Cobrir catch genérico em listarFuncionarios")
    void deveCobrirCatchEmListarFuncionarios() {
        when(usuarioRepository.findFuncionarios()).thenThrow(new RuntimeException("DB Error"));
        assertThrows(RuntimeException.class, () -> usuarioService.listarFuncionarios());
    }

    @Test
    @Order(68)
    @DisplayName("CT68 - Cobrir catch genérico em listarEstados")
    void deveCobrirCatchEmListarEstados() {
        when(estadoRepository.findEstados()).thenThrow(new RuntimeException("DB Error"));
        assertThrows(RuntimeException.class, () -> usuarioService.listarEstados());
    }

    @Test
    @Order(69)
    @DisplayName("CT161 - Deve lançar IllegalArgumentException ao salvar usuário com CPF inválido")
    void deveLancarExcecaoAoSalvarUsuarioComCpfInvalido() {
        usuarioValido.setCpf("11111111111");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.salvar(usuarioValido));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @Order(70)
    @DisplayName("CT205 - Retornar false ao tentar validar um código de recuperação incorreto")
    void deveRetornarFalseAoValidarCodigoIncorreto() {
        Long idUsuario = 1L;
        String codigoNoBanco = "123456";
        String codigoErrado = "999999";

        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);
        usuario.setCodigoRecuperacao(codigoNoBanco);
        usuario.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(15));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        boolean resultado = usuarioService.validarCodigo(idUsuario, codigoErrado);

        assertFalse(resultado);
        verify(usuarioRepository, times(1)).findById(idUsuario);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

}