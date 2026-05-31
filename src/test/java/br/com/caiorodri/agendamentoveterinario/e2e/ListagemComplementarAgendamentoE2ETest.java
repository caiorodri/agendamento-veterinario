package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.model.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@Order(35)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ListagemComplementarAgendamentoE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAdmin;
    private Long idVet;
    private String dataDoAgendamento;

    @BeforeEach
    public void setup() {
        Perfil perfilAdmin = perfilRepository.save(new Perfil(4, "ADMINISTRADOR"));
        Perfil perfilCliente = perfilRepository.save(new Perfil(1, "CLIENTE"));
        Perfil perfilVet = perfilRepository.save(new Perfil(3, "VETERINARIO"));
        Status statusAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSP = estadoRepository.save(new Estado("SP", "São Paulo"));

        agendamentoStatusRepository.save(new AgendamentoStatus(1, "ABERTO"));
        agendamentoTipoRepository.save(new AgendamentoTipo(1, "CONSULTA", 30));

        Especie especie = especieRepository.save(new Especie(null, "Gato"));
        Raca raca = racaRepository.save(new Raca(null, especie, "Persa"));
        Sexo sexo = sexoRepository.save(new Sexo(1, "Macho"));

        Usuario admin = new Usuario();
        admin.setEmail("admin.agd.extra@teste.com");
        admin.setSenha(passwordEncoder.encode("123"));
        admin.setCpf("18544976077");
        admin.setNome("Admin Agendamento Extra");
        admin.setPerfil(perfilAdmin);
        admin.setStatus(statusAtivo);
        admin.getEndereco().setEstado(estadoSP);
        usuarioRepository.save(admin);

        Usuario cliente = new Usuario();
        cliente.setEmail("cli.agd.extra@teste.com");
        cliente.setSenha(passwordEncoder.encode("123"));
        cliente.setCpf("09708800066");
        cliente.setNome("Cliente Agd Extra");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(statusAtivo);
        cliente.getEndereco().setEstado(estadoSP);
        Long idCliente = usuarioRepository.save(cliente).getId();

        Usuario vet = new Usuario();
        vet.setEmail("vet.agd.extra@teste.com");
        vet.setSenha(passwordEncoder.encode("123"));
        vet.setCpf("62947118029");
        vet.setNome("Vet Agd Extra");
        vet.setPerfil(perfilVet);
        vet.setStatus(statusAtivo);
        vet.getEndereco().setEstado(estadoSP);
        idVet = usuarioRepository.save(vet).getId();

        Animal animal = new Animal();
        animal.setNome("Mingau");
        animal.setRaca(raca);
        animal.setSexo(sexo);
        animal.setDono(new Usuario(idCliente));
        Long idAnimal = animalRepository.save(animal).getId();

        LocalDateTime dataInicio = LocalDateTime.now().plusDays(15).withHour(10).withMinute(0).withSecond(0).withNano(0);
        dataDoAgendamento = dataInicio.toLocalDate().toString();

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(new Usuario(idCliente));
        agendamento.setVeterinario(new Usuario(idVet));
        agendamento.setRecepcionista(new Usuario(idVet));
        agendamento.setAnimal(new Animal(idAnimal));
        agendamento.setStatus(new AgendamentoStatus(1));
        agendamento.setTipo(new AgendamentoTipo(1));
        agendamento.setDataCriacao(LocalDateTime.now());
        agendamento.setDataAgendamentoInicio(dataInicio);
        agendamento.setDataAgendamentoFinal(dataInicio.plusMinutes(30));
        agendamentoRepository.save(agendamento);

        tokenAdmin = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"admin.agd.extra@teste.com\", \"senha\": \"123\" }")
                .post("/usuarios/autenticar").then().extract().path("token");
    }

    @Test
    @Order(1)
    @DisplayName("CT200 - Dicionários Agendamento: Listar Status e Tipos de Agendamento")
    void deveListarStatusETiposDeAgendamento() {
        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/agendamentos/status")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));

        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/agendamentos/tipos")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(2)
    @DisplayName("CT201 - Agendamentos do Veterinário: Listar por ID geral e por data")
    void deveListarAgendamentosDoVeterinario() {
        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/agendamentos/veterinario/" + idVet)
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));

        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/agendamentos/veterinario/" + idVet + "/data?data=" + dataDoAgendamento)
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }
}