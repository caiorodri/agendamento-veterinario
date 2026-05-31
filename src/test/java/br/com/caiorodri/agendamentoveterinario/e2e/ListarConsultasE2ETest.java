package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.model.*;
import br.com.caiorodri.agendamentoveterinario.service.AgendamentoService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(20)
public class ListarConsultasE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AgendamentoService agendamentoService;

    private Long idCliente;
    private Long idVeterinario;
    private Long idRecepcionista;
    private Long idAnimal;
    private String tokenRecepcionista;

    @BeforeEach
    public void setup() {
        Perfil perfilAdmin = perfilRepository.save(new Perfil(1, "ADMINISTRADOR"));
        Perfil perfilCliente = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Status statusAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSP = estadoRepository.save(new Estado("SP", "São Paulo"));

        agendamentoStatusRepository.save(new AgendamentoStatus(1, "ABERTO"));
        agendamentoTipoRepository.save(new AgendamentoTipo(1, "CONSULTA", 30));

        Especie especie = especieRepository.save(new Especie(null, "Cachorro"));
        Raca raca = racaRepository.save(new Raca(null, especie, "Vira-Lata"));
        Sexo sexo = sexoRepository.save(new Sexo(1, "Macho"));

        String senhaHash = passwordEncoder.encode("senha123");

        Usuario cliente = new Usuario();
        cliente.setEmail("cliente.lista@teste.com");
        cliente.setSenha(senhaHash);
        cliente.setCpf("75481358055");
        cliente.setNome("Cliente Lista");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(statusAtivo);
        cliente.getEndereco().setEstado(estadoSP);
        cliente.setReceberEmail(false);
        idCliente = usuarioRepository.save(cliente).getId();

        Usuario vet = new Usuario();
        vet.setEmail("vet.lista@teste.com");
        vet.setSenha(senhaHash);
        vet.setCpf("09377402096");
        vet.setNome("Vet Lista");
        vet.setPerfil(perfilAdmin);
        vet.setStatus(statusAtivo);
        vet.getEndereco().setEstado(estadoSP);
        vet.setReceberEmail(false);
        idVeterinario = usuarioRepository.save(vet).getId();

        Usuario rec = new Usuario();
        rec.setEmail("rec.lista@teste.com");
        rec.setSenha(senhaHash);
        rec.setCpf("84916327091");
        rec.setNome("Rec Lista");
        rec.setPerfil(perfilAdmin);
        rec.setStatus(statusAtivo);
        rec.getEndereco().setEstado(estadoSP);
        rec.setReceberEmail(false);
        idRecepcionista = usuarioRepository.save(rec).getId();

        Animal animal = new Animal();
        animal.setNome("Rex Lista");
        animal.setDono(new Usuario(idCliente));
        animal.setRaca(raca);
        animal.setSexo(sexo);
        animal.setDataNascimento(new java.util.Date());
        animal.setDescricao("Pet teste listagem");
        animal.setCastrado(false);
        animal.setPeso(10.0f);
        animal.setAltura(40.0f);
        idAnimal = animalRepository.save(animal).getId();

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(new Usuario(idCliente));
        agendamento.setVeterinario(new Usuario(idVeterinario));
        agendamento.setRecepcionista(new Usuario(idRecepcionista));
        agendamento.setAnimal(new Animal(idAnimal));
        agendamento.setStatus(new AgendamentoStatus(1));
        agendamento.setTipo(new AgendamentoTipo(1));
        agendamento.setDataCriacao(LocalDateTime.now());
        agendamento.setDataAgendamentoInicio(LocalDateTime.now().plusDays(5).withHour(10).withMinute(0).withSecond(0).withNano(0));
        agendamento.setDataAgendamentoFinal(LocalDateTime.now().plusDays(5).withHour(10).withMinute(30).withSecond(0).withNano(0));
        agendamento.setDescricao("Consulta inicial");
        agendamentoRepository.save(agendamento);

        tokenRecepcionista = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"rec.lista@teste.com\", \"senha\": \"senha123\" }")
                .post("/usuarios/autenticar").then().statusCode(200).extract().path("token");

    }

    @Test
    @Order(1)
    @DisplayName("CT172 - Listar Consultas: Fluxo API Encadeado - Listagem e edição de consulta")
    void deveListarEEditarConsulta() {
        Number idAgendamentoBuscado = given()
                .header("Authorization", "Bearer " + tokenRecepcionista)
                .contentType(ContentType.JSON)
                .when()
                .get("/agendamentos?pagina=0&quantidadeItens=10")
                .then()
                .statusCode(200)
                .body("content.size()", greaterThan(0))
                .extract().path("content[0].id");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String dataInicio = LocalDateTime.now().plusDays(6).withHour(10).withMinute(0).withSecond(0).format(fmt);
        String dataFim = LocalDateTime.now().plusDays(6).withHour(10).withMinute(30).withSecond(0).format(fmt);

        String jsonEdicao = String.format("""
                {
                    "id": %d,
                    "cliente": { "id": %d },
                    "veterinario": { "id": %d },
                    "recepcionista": { "id": %d },
                    "animal": { "id": %d },
                    "status": { "id": 1 },
                    "tipo": { "id": 1 },
                    "dataAgendamentoInicio": "%s",
                    "dataAgendamentoFinal": "%s",
                    "descricao": "Consulta editada via E2E"
                }
                """,
                idAgendamentoBuscado.longValue(), idCliente, idVeterinario, idRecepcionista, idAnimal,
                dataInicio, dataFim
        );

        given()
                .header("Authorization", "Bearer " + tokenRecepcionista)
                .contentType(ContentType.JSON)
                .body(jsonEdicao)
                .when()
                .put("/agendamentos")
                .then()
                .statusCode(200)
                .body("descricao", equalTo("Consulta editada via E2E"));
    }
}