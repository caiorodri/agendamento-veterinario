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
import static org.hamcrest.Matchers.equalTo;

@Order(30)
public class AtualizarAnimalE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private Long idAnimal;
    private Integer idRaca;
    private Integer idSexo;
    private Long idCliente;

    @BeforeEach
    public void setup() {
        Perfil perfil = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Status status = statusRepository.save(new Status(1, "ATIVO"));
        Estado estado = estadoRepository.save(new Estado("SP", "São Paulo"));

        Especie especie = especieRepository.save(new Especie(null, "Cachorro"));
        idRaca = racaRepository.save(new Raca(null, especie, "Poodle")).getId();
        idSexo = sexoRepository.save(new Sexo(1, "Macho")).getId();

        Usuario cliente = new Usuario();
        cliente.setEmail("cli.animal.put@teste.com");
        cliente.setSenha(passwordEncoder.encode("123"));
        cliente.setCpf("09708800066");
        cliente.setNome("Cliente Pets Put");
        cliente.setPerfil(perfil);
        cliente.setStatus(status);
        cliente.getEndereco().setEstado(estado);
        idCliente = usuarioRepository.save(cliente).getId();

        Animal animal = new Animal();
        animal.setNome("Rex Original");
        animal.setPeso(5.0f);
        animal.setRaca(new Raca(idRaca));
        animal.setSexo(new Sexo(idSexo));
        animal.setDono(new Usuario(idCliente));
        idAnimal = animalRepository.save(animal).getId();

        token = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"cli.animal.put@teste.com\", \"senha\": \"123\" }")
                .post("/usuarios/autenticar").then().extract().path("token");
    }

    @Test
    @DisplayName("CT184 - Atualizar Animal: Modificar nome e peso do pet")
    void deveAtualizarPesoENomeDoAnimal() {
        String jsonUpdate = String.format("""
                {
                    "id": %d,
                    "nome": "Rex Modificado",
                    "peso": 6.5,
                    "raca": { "id": %d },
                    "sexo": { "id": %d },
                    "dono": { "id": %d }
                }
                """, idAnimal, idRaca, idSexo, idCliente);

        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON).body(jsonUpdate)
                .when().put("/animais")
                .then().statusCode(200).body("nome", equalTo("Rex Modificado"));
    }
}