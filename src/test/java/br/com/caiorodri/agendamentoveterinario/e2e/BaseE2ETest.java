package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.email.EmailSender;
import br.com.caiorodri.agendamentoveterinario.repository.*;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.TestClassOrder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.lazy-initialization=true",
                "spring.jpa.show-sql=false",
                "spring.datasource.hikari.maximum-pool-size=60",
                "server.tomcat.threads.min-spare=60"
        })
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public abstract class BaseE2ETest {

    @LocalServerPort
    private int port;

    @Mock
    protected EmailSender emailSender;

    @Autowired protected AgendamentoRepository agendamentoRepository;
    @Autowired protected AnimalRepository animalRepository;
    @Autowired protected UsuarioRepository usuarioRepository;
    @Autowired protected AgendamentoStatusRepository agendamentoStatusRepository;
    @Autowired protected AgendamentoTipoRepository agendamentoTipoRepository;
    @Autowired protected PerfilRepository perfilRepository;
    @Autowired protected StatusRepository statusRepository;
    @Autowired protected EstadoRepository estadoRepository;
    @Autowired protected EspecieRepository especieRepository;
    @Autowired protected RacaRepository racaRepository;
    @Autowired protected SexoRepository sexoRepository;
    @Autowired protected ResultadoConsultaRepository resultadoConsultaRepository;
    @Autowired protected DiaSemanaRepository diaSemanaRepository;

    @BeforeEach
    public void baseSetUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/agendamento-veterinario";

        limparBancoDeDados();
    }

    private void limparBancoDeDados() {

        resultadoConsultaRepository.deleteAll();
        agendamentoRepository.deleteAll();
        animalRepository.deleteAll();
        usuarioRepository.deleteAll();

        agendamentoStatusRepository.deleteAll();
        agendamentoTipoRepository.deleteAll();
        racaRepository.deleteAll();
        especieRepository.deleteAll();
        sexoRepository.deleteAll();
        statusRepository.deleteAll();
        perfilRepository.deleteAll();
        estadoRepository.deleteAll();
        diaSemanaRepository.deleteAll();
    }
}