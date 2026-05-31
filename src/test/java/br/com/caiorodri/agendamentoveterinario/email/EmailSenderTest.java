package br.com.caiorodri.agendamentoveterinario.email;

import br.com.caiorodri.agendamentoveterinario.model.*;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;
import jakarta.mail.internet.MimeMessage;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(10)
class EmailSenderTest {

    @InjectMocks
    private EmailSender emailSender;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailSender, "remetente", "nao-responda@agenpet.com");
        ReflectionTestUtils.setField(emailSender, "endereco", "Rua Fictícia, 123 - SP");
        ReflectionTestUtils.setField(emailSender, "contato", "(11) 99999-9999");
    }

    @Test
    @Order(1)
    @DisplayName("CT152 - Enviar Confirmação de Cadastro de Usuário")
    void deveEnviarEmailDeCadastroDeUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Novo Cliente");
        usuario.setEmail("novo@teste.com");

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.enviarInformacaoCadastroUsuarioEmail(usuario);

        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @Order(2)
    @DisplayName("CT153 - Cobrir branches de atributos nulos no Animal e busca BD se sem email")
    void deveBuscarDonoNoBancoAoEnviarCadastroAnimalSemEmail() {
        Usuario donoSemEmail = new Usuario();
        donoSemEmail.setId(1L);
        donoSemEmail.setNome("Carlos");

        Usuario donoDoBanco = new Usuario();
        donoDoBanco.setId(1L);
        donoDoBanco.setNome("Carlos");
        donoDoBanco.setEmail("carlos.banco@teste.com");

        Animal pet = new Animal();
        pet.setNome("Rex");
        pet.setDono(donoSemEmail);

        when(usuarioRepository.findByIdWithSets(1L)).thenReturn(Optional.of(donoDoBanco));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.enviarInformacaoCadastroAnimalEmail(pet, true);

        verify(usuarioRepository, times(1)).findByIdWithSets(1L);
        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @Order(3)
    @DisplayName("CT154 - Retornar false se dono do animal não existir no banco (Email Sender)")
    void deveRetornarFalseSeDonoNaoForEncontradoAoSalvarAnimal() {
        Usuario donoSemEmail = new Usuario();
        donoSemEmail.setId(99L);
        Animal pet = new Animal();
        pet.setDono(donoSemEmail);

        when(usuarioRepository.findByIdWithSets(99L)).thenReturn(Optional.empty());

        emailSender.enviarInformacaoCadastroAnimalEmail(pet, false);

        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @Order(4)
    @DisplayName("CT155 - Retornar false se ocorrer Exception no cadastro/atualizacao")
    void deveRetornarFalseSeCatchEstourarNoCadastroUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Novo Cliente");
        usuario.setEmail("novo@teste.com");

        when(javaMailSender.createMimeMessage()).thenThrow(new RuntimeException("Falha SMTP"));

        emailSender.enviarInformacaoCadastroUsuarioEmail(usuario);

        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @Order(5)
    @DisplayName("CT156 - Enviar Confirmação de Agendamento (Novo e Atualização)")
    void deveEnviarEmailDeAgendamentoNovo() {
        Usuario cliente = new Usuario();
        cliente.setNome("Maria");
        cliente.setEmail("maria@teste.com");

        Usuario vet = new Usuario();
        vet.setNome("Dr. João");

        Usuario rec = new Usuario();
        rec.setNome("Ana");

        Animal pet = new Animal();
        pet.setNome("Bolinha");

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setVeterinario(vet);
        agendamento.setRecepcionista(rec);
        agendamento.setAnimal(pet);
        agendamento.setDataAgendamentoInicio(LocalDateTime.now().plusDays(1));

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.enviarInformacaoCadastroAgendamentoEmail(agendamento, false);

        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @Order(6)
    @DisplayName("CT157 - Retornar false se cliente do agendamento não for achado (Sem Email)")
    void deveRetornarFalseSeClienteDoAgendamentoNaoExistirNoBanco() {
        Usuario clienteSemEmail = new Usuario();
        clienteSemEmail.setId(99L);

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(clienteSemEmail);

        when(usuarioRepository.findByIdWithSets(99L)).thenReturn(Optional.empty());

        emailSender.enviarInformacaoCadastroAgendamentoEmail(agendamento, false);

        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @Order(7)
    @DisplayName("CT158 - Enviar aviso de consulta vencida (> 6 meses) Email")
    void deveEnviarEmailDeRevisaoDeConsulta() {
        Usuario dono = new Usuario();
        dono.setNome("José");
        dono.setEmail("jose@teste.com");

        Animal pet = new Animal();
        pet.setNome("Mingau");
        pet.setDono(dono);

        Agendamento ultimaConsulta = new Agendamento();
        ultimaConsulta.setDataAgendamentoInicio(LocalDateTime.now().minusMonths(7));

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.enviarInformacaoRealizarConsultaEmail(pet, ultimaConsulta);

        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @Order(8)
    @DisplayName("CT159 - Enviar e-mail de campanha de vacinação e buscar cliente se sem e-mail")
    void deveEnviarEmailDeCampanha() {
        Usuario usuario = new Usuario();
        usuario.setNome("Fernanda");
        usuario.setEmail("fernanda@teste.com");

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.enviarInformacaoCampanhaVacinaEmail(usuario);

        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @Order(9)
    @DisplayName("CT160 - Retornar false se ocorrer Exception no Agendamento/Consulta/Campanha")
    void deveRetornarFalseSeOcorrerExceptionAoEnviar() {
        Usuario usuario = new Usuario();
        usuario.setNome("Erro");
        usuario.setEmail("erro@teste.com");

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Servidor SMTP Fora do Ar")).when(javaMailSender).send(mimeMessage);

        emailSender.enviarInformacaoCampanhaVacinaEmail(usuario);

        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @Order(10)
    @DisplayName("CT202 - Não disparar email se destinatário for nulo ou vazio")
    void naoDeveDispararEmailSeDestinatarioForNuloOuVazio() {
        emailSender.enviarCodigoEmail("João", "", "123456");
        emailSender.enviarCodigoEmail("Maria", null, "654321");

        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @Order(11)
    @DisplayName("CT203 - Não buscar no banco se o usuário for nulo (obterUsuarioComEmailCompleto)")
    void naoDeveBuscarNoBancoOuEnviarSeUsuarioForNulo() {
        emailSender.enviarInformacaoCampanhaVacinaEmail(null);

        verify(usuarioRepository, never()).findByIdWithSets(anyLong());
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @Order(12)
    @DisplayName("CT204 - Não enviar aviso de revisão se o dono do animal for nulo")
    void naoDeveEnviarEmailDeRevisaoSeDonoDoAnimalForNulo() {
        Animal petSemDono = new Animal();
        petSemDono.setNome("Bidu");
        petSemDono.setDono(null);

        Agendamento ultimaConsulta = new Agendamento();
        ultimaConsulta.setDataAgendamentoInicio(LocalDateTime.now().minusMonths(7));

        emailSender.enviarInformacaoRealizarConsultaEmail(petSemDono, ultimaConsulta);

        verify(usuarioRepository, never()).findByIdWithSets(anyLong());
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }
}