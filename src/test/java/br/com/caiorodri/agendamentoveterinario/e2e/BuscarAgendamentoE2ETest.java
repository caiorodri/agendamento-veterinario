package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.model.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;

@Order(33)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BuscarAgendamentoE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private String tokenVet;
    private Long idCliente;
    private Long idAnimal;
    private String dataDoAgendamento;

    @BeforeEach
    public void setup() {
        Perfil perfilCliente = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Perfil perfilVet = perfilRepository.save(new Perfil(3, "VETERINARIO"));
        Status status = statusRepository.save(new Status(1, "ATIVO"));
        Estado estado = estadoRepository.save(new Estado("SP", "São Paulo"));

        agendamentoStatusRepository.save(new AgendamentoStatus(1, "ABERTO"));
        agendamentoTipoRepository.save(new AgendamentoTipo(1, "CONSULTA", 30));

        Especie especie = especieRepository.save(new Especie(null, "Gato"));
        Raca raca = racaRepository.save(new Raca(null, especie, "Siamês"));
        Sexo sexo = sexoRepository.save(new Sexo(1, "Fêmea"));

        Usuario cliente = new Usuario();
        cliente.setEmail("cliente@teste.com");
        cliente.setSenha(passwordEncoder.encode("123"));
        cliente.setCpf("18544976077");
        cliente.setNome("Cliente Agenda Get");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(status);
        cliente.getEndereco().setEstado(estado);
        idCliente = usuarioRepository.save(cliente).getId();

        Usuario vet = new Usuario();
        vet.setEmail("veterinario@teste.com");
        vet.setSenha(passwordEncoder.encode("123"));
        vet.setCpf("09708800066");
        vet.setNome("Vet Agenda Get");
        vet.setPerfil(perfilVet);
        vet.setStatus(status);
        vet.getEndereco().setEstado(estado);
        Long idVet = usuarioRepository.save(vet).getId();

        Animal animal = new Animal();
        animal.setNome("Luna");
        animal.setRaca(raca);
        animal.setSexo(sexo);
        animal.setDono(new Usuario(idCliente));
        idAnimal = animalRepository.save(animal).getId();

        LocalDateTime dataInicio = LocalDateTime.now().plusDays(10).withHour(9).withMinute(0).withSecond(0).withNano(0);
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

        token = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"cliente@teste.com\", \"senha\": \"123\" }")
                .post("/usuarios/autenticar").then().extract().path("token");

        tokenVet = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"veterinario@teste.com\", \"senha\": \"123\" }")
                .post("/usuarios/autenticar").then().extract().path("token");
    }

    @Test
    @Order(1)
    @DisplayName("CT189 - Buscar Agendamento: Listar com paginação")
    void deveListarAgendamentosComPaginacao() {
        given().header("Authorization", "Bearer " + tokenVet)
                .when().get("/agendamentos?pagina=0&quantidadeItens=5")
                .then().statusCode(200);
    }

    @Test
    @Order(2)
    @DisplayName("CT190 - Buscar Agendamento: Filtrar pelo ID do cliente")
    void deveFiltrarAgendamentosPorIdDoCliente() {
        given().header("Authorization", "Bearer " + token)
                .when().get("/agendamentos/usuario/" + idCliente + "?pagina=0&quantidadeItens=10")
                .then().statusCode(200);
    }

    @Test
    @Order(3)
    @DisplayName("CT191 - Buscar Agendamento: Filtrar pelo ID do animal")
    void deveFiltrarAgendamentosPorIdDoAnimal() {
        given().header("Authorization", "Bearer " + token)
                .when().get("/agendamentos/animal/" + idAnimal + "?pagina=0&quantidadeItens=10")
                .then().statusCode(200);
    }

    @Test
    @Order(4)
    @DisplayName("CT192 - Buscar Agendamento: Filtrar pela data específica")
    void deveFiltrarAgendamentosPorDataEspecifica() {
        given().header("Authorization", "Bearer " + token)
                .when().get("/agendamentos/data?data=" + dataDoAgendamento)
                .then().statusCode(200);
    }
}