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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(21)
public class ConsultarPetsE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenCliente;

    @BeforeEach
    public void setup() {
        Perfil perfilCliente = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Status statusAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSP = estadoRepository.save(new Estado("SP", "São Paulo"));

        Especie especie = especieRepository.save(new Especie(null, "Cachorro"));
        Raca raca = racaRepository.save(new Raca(null, especie, "Vira-Lata"));
        Sexo sexo = sexoRepository.save(new Sexo(1, "Macho"));

        Usuario cliente = new Usuario();
        cliente.setEmail("cliente.consulta@teste.com");
        cliente.setSenha(passwordEncoder.encode("senha123"));
        cliente.setCpf("62947118029");
        cliente.setNome("Cliente Consulta");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(statusAtivo);
        cliente.getEndereco().setEstado(estadoSP);
        cliente.setReceberEmail(false);
        Long idCliente = usuarioRepository.save(cliente).getId();

        Animal animal = new Animal();
        animal.setNome("Rex Consulta");
        animal.setDono(new Usuario(idCliente));
        animal.setRaca(raca);
        animal.setSexo(sexo);
        animal.setDataNascimento(new java.util.Date());
        animal.setDescricao("Pet teste consulta");
        animal.setCastrado(false);
        animal.setPeso(10.0f);
        animal.setAltura(40.0f);
        animalRepository.save(animal);

        tokenCliente = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"cliente.consulta@teste.com\", \"senha\": \"senha123\" }")
                .post("/usuarios/autenticar").then().statusCode(200).extract().path("token");
    }

    @Test
    @Order(1)
    @DisplayName("CT173 - Consultar Pets: Fluxo API Encadeado - Consolidado de pets do usuário")
    void deveConsultarListaConsolidadaDePetsDoUsuario() {
        given()
                .header("Authorization", "Bearer " + tokenCliente)
                .contentType(ContentType.JSON)
                .when()
                .get("/usuarios/me")
                .then()
                .statusCode(200)
                .body("animais", notNullValue())
                .body("animais.size()", greaterThanOrEqualTo(1))
                .body("animais[0].nome", equalTo("Rex Consulta"));
    }
}