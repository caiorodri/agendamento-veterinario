package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.model.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Order(31)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BuscarAnimalE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private Long idAnimal;
    private Long idCliente;

    @BeforeEach
    public void setup() {
        Perfil perfil = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Status status = statusRepository.save(new Status(1, "ATIVO"));
        Estado estado = estadoRepository.save(new Estado("SP", "São Paulo"));

        Especie especie = especieRepository.save(new Especie(null, "Cachorro"));
        Raca raca = racaRepository.save(new Raca(null, especie, "Beagle"));
        Sexo sexo = sexoRepository.save(new Sexo(1, "Macho"));

        Usuario cliente = new Usuario();
        cliente.setEmail("cli.animal.get@teste.com");
        cliente.setSenha(passwordEncoder.encode("123"));
        cliente.setCpf("18544976077");
        cliente.setNome("Cliente Pets Get");
        cliente.setPerfil(perfil);
        cliente.setStatus(status);
        cliente.getEndereco().setEstado(estado);
        idCliente = usuarioRepository.save(cliente).getId();

        Animal animal = new Animal();
        animal.setNome("Snoopy");
        animal.setPeso(10.5f);
        animal.setRaca(raca);
        animal.setSexo(sexo);
        animal.setDono(new Usuario(idCliente));
        idAnimal = animalRepository.save(animal).getId();

        token = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"cli.animal.get@teste.com\", \"senha\": \"123\" }")
                .post("/usuarios/autenticar").then().extract().path("token");
    }

    @Test
    @Order(1)
    @DisplayName("CT185 - Buscar Animal: Recuperar por ID único")
    void deveBuscarAnimalPorId() {
        given().header("Authorization", "Bearer " + token)
                .when().get("/animais/" + idAnimal)
                .then().statusCode(200)
                .body("nome", equalTo("Snoopy"))
                .body("peso", equalTo(10.5f));
    }

    @Test
    @Order(2)
    @DisplayName("CT186 - Buscar Animal: Listar animais com paginação")
    void deveListarAnimaisComPaginacao() {
        given().header("Authorization", "Bearer " + token)
                .when().get("/animais?pagina=0&quantidadeItens=5")
                .then().statusCode(200);
    }

    @Test
    @Order(3)
    @DisplayName("CT187 - Buscar Animal: Filtrar animais pelo ID do dono")
    void deveListarAnimaisPeloIdDoDono() {
        given().header("Authorization", "Bearer " + token)
                .when().get("/animais/dono/" + idCliente + "?pagina=0&quantidadeItens=10")
                .then().statusCode(200);
    }
}