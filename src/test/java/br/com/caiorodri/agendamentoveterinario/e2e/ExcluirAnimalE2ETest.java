package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.model.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.restassured.RestAssured.given;

@Order(26)
public class ExcluirAnimalE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long idRaca;
    private Long idSexo;
    private Long idCliente;
    private String tokenCliente;

    @BeforeEach
    public void setup() {
        Perfil perfilCliente = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Status statusAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSP = estadoRepository.save(new Estado("SP", "São Paulo"));

        Especie especie = especieRepository.save(new Especie(null, "Gato"));
        idRaca = (long) racaRepository.save(new Raca(null, especie, "Persa")).getId();
        idSexo = (long) sexoRepository.save(new Sexo(2, "Fêmea")).getId();

        Usuario cliente = new Usuario();
        cliente.setEmail("cli.excluir@teste.com");
        cliente.setSenha(passwordEncoder.encode("123"));
        cliente.setCpf("77777777777");
        cliente.setNome("Cli Excluir");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(statusAtivo);
        cliente.getEndereco().setEstado(estadoSP);
        idCliente = usuarioRepository.save(cliente).getId();

        tokenCliente = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"cli.excluir@teste.com\", \"senha\": \"123\" }")
                .post("/usuarios/autenticar").then().extract().path("token");
    }

    @Test
    @DisplayName("CT178 - Excluir Animal E2E: Deleção de pet isolado sem histórico")
    void deveDeletarAnimalSemAgendamento() {
        String jsonNovoAnimal = String.format("""
                {
                    "nome": "Bidu Exclusao",
                    "peso": 10.0,
                    "raca": { "id": %d },
                    "sexo": { "id": %d },
                    "dono": { "id": %d }
                }
                """, idRaca, idSexo, idCliente);

        Number animalIdNumber = given()
                .header("Authorization", "Bearer " + tokenCliente)
                .contentType(ContentType.JSON)
                .body(jsonNovoAnimal)
                .when().post("/animais")
                .then().statusCode(201)
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + tokenCliente)
                .contentType(ContentType.JSON)
                .when().delete("/animais/" + animalIdNumber.longValue())
                .then().statusCode(204);
    }
}