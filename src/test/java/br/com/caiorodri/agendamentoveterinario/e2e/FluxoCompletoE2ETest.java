package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.model.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Order(17)
public class FluxoCompletoE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long idDono;
    private Long idVeterinario;
    private Long idRecepcionista;
    private Long idRaca;
    private Long idSexo;

    @BeforeEach
    public void setUp() {
        prepararDicionariosEFuncionarios();
    }

    private void prepararDicionariosEFuncionarios() {

        Perfil perfilAdmin = perfilRepository.save(new Perfil(1, "ADMINISTRADOR"));
        Perfil perfilCliente = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Status statusAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSP = estadoRepository.save(new Estado("SP", "São Paulo"));

        agendamentoStatusRepository.save(new AgendamentoStatus(1, "ABERTO"));
        agendamentoTipoRepository.save(new AgendamentoTipo(1, "CONSULTA", 30));

        Especie especie = new Especie();
        especie.setNome("Cachorro");
        Especie especieCachorro = especieRepository.save(especie);

        Raca racaGolden = new Raca();
        racaGolden.setEspecie(especieCachorro);
        racaGolden.setNome("Golden Retriever");
        racaRepository.save(racaGolden);

        idRaca = (long) racaGolden.getId();

        Sexo sexoMacho = sexoRepository.save(new Sexo(1, "Macho"));
        idSexo = (long) sexoMacho.getId();

        String senhaHasheada = passwordEncoder.encode("senha123");

        Usuario cliente = new Usuario();
        cliente.setEmail("cliente@teste.com");
        cliente.setSenha(senhaHasheada);
        cliente.setCpf("11122233344");
        cliente.setNome("Cliente");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(statusAtivo);
        cliente.getEndereco().setEstado(estadoSP);
        idDono = usuarioRepository.save(cliente).getId();

        Usuario veterinario = new Usuario();
        veterinario.setEmail("veterinario@teste.com");
        veterinario.setSenha(senhaHasheada);
        veterinario.setCpf("99988877766");
        veterinario.setNome("Veterinário");
        veterinario.setPerfil(perfilAdmin);
        veterinario.setStatus(statusAtivo);
        veterinario.getEndereco().setEstado(estadoSP);
        idVeterinario = usuarioRepository.save(veterinario).getId();

        Usuario recepcionista = new Usuario();
        recepcionista.setEmail("recepcionista@teste.com");
        recepcionista.setSenha(senhaHasheada);
        recepcionista.setCpf("55544433322");
        recepcionista.setNome("Recepcionista");
        recepcionista.setPerfil(perfilAdmin);
        recepcionista.setStatus(statusAtivo);
        recepcionista.getEndereco().setEstado(estadoSP);
        idRecepcionista = usuarioRepository.save(recepcionista).getId();
    }

    @Test
    @DisplayName("CT169 - E2E Jornada Feliz: Login -> Cadastrar Animal -> Criar Agendamento")
    void deveCompletarJornadaDeAgendamentoDoCliente() {

        String jsonLogin = "{ \"email\": \"cliente@teste.com\", \"senha\": \"senha123\" }";

        String token = given()
                .contentType(ContentType.JSON)
                .body(jsonLogin)
                .when()
                .post("/usuarios/autenticar")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract().path("token");

        String jsonAnimal = String.format("""
                {
                    "nome": "Thor",
                    "peso": 25.5,
                    "altura": 55.0,
                    "raca": { "id": %d },
                    "sexo": { "id": %d },
                    "dono": { "id": %d }
                }
                """, idRaca, idSexo, idDono);

        Number animalIdNumber = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(jsonAnimal)
                .when()
                .post("/animais")
                .then()
                .statusCode(201)
                .extract().path("id");

        Long idAnimalNovo = animalIdNumber.longValue();

        String jsonAgendamento = String.format("""
                {
                    "cliente": { "id": %d },
                    "veterinario": { "id": %d },
                    "recepcionista": { "id": %d },
                    "animal": { "id": %d },
                    "status": { "id": 1 },
                    "tipo": { "id": 1 },
                    "dataAgendamentoInicio": "%s",
                    "dataAgendamentoFinal": "%s"
                }
                """, idDono, idVeterinario, idRecepcionista, idAnimalNovo,
                LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().plusDays(2).withHour(14).withMinute(30).withSecond(0).withNano(0));

        Number agendamentoIdNumber = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(jsonAgendamento)
                .when()
                .post("/agendamentos")
                .then()
                .statusCode(201)
                .extract().path("id");

        Long idAgendamentoNovo = agendamentoIdNumber.longValue();

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/agendamentos/" + idAgendamentoNovo)
                .then()
                .statusCode(200)
                .body("animal.nome", equalTo("Thor"))
                .body("veterinario.nome", equalTo("Veterinário"));
    }
}