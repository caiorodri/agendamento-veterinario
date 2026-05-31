package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.model.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(19)
public class CadastrarClienteE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long idRaca;
    private Long idSexo;

    @BeforeEach
    public void setup() {
        perfilRepository.save(new Perfil(2, "CLIENTE"));
        statusRepository.save(new Status(1, "ATIVO"));
        estadoRepository.save(new Estado("SP", "São Paulo"));

        Especie especie = especieRepository.save(new Especie(null, "Cachorro"));
        Raca raca = racaRepository.save(new Raca(null, especie, "Vira-Lata"));
        idRaca = (long) raca.getId();

        Sexo sexo = sexoRepository.save(new Sexo(1, "Macho"));
        idSexo = (long) sexo.getId();
    }

    @Test
    @Order(1)
    @DisplayName("CT171 - Cadastrar Cliente: Fluxo API Encadeado - Cadastro de cliente e pet")
    void deveCadastrarClienteEPetEmSequencia() {
        String jsonNovoCliente = """
                {
                    "nome": "Novo Cliente Fluxo",
                    "email": "novo.cliente@teste.com",
                    "cpf": "09708800066",
                    "senha": "senha123",
                    "perfil": { "id": 2 },
                    "status": { "id": 1 },
                    "receberEmail": false,
                    "endereco": { "estado": { "sigla": "SP" }, "logradouro": "Rua X", "numero": "123", "cidade": "São Paulo", "cep": "01010101" }
                }
                """;

        Number idNovoCliente = given()
                .contentType(ContentType.JSON)
                .body(jsonNovoCliente)
                .when()
                .post("/usuarios")
                .then()
                .statusCode(201)
                .extract().path("id");

        String tokenNovoCliente = given()
                .contentType(ContentType.JSON)
                .body("{ \"email\": \"novo.cliente@teste.com\", \"senha\": \"senha123\" }")
                .when()
                .post("/usuarios/autenticar")
                .then()
                .statusCode(200)
                .extract().path("token");

        String jsonNovoPet = String.format("""
                {
                    "nome": "Novo Pet Fluxo",
                    "raca": { "id": %d },
                    "sexo": { "id": %d },
                    "dono": { "id": %d },
                    "dataNascimento": "2023-01-01",
                    "descricao": "Pet dócil",
                    "peso": 5.5,
                    "altura": 30.0
                }
                """, idRaca, idSexo, idNovoCliente.longValue());

        given()
                .header("Authorization", "Bearer " + tokenNovoCliente)
                .contentType(ContentType.JSON)
                .body(jsonNovoPet)
                .when()
                .post("/animais")
                .then()
                .statusCode(201)
                .body("nome", equalTo("Novo Pet Fluxo"));
    }
}