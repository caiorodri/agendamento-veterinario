package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.model.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Order(29)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BuscarUsuarioE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAdmin;
    private Long idCliente;

    @BeforeEach
    public void setup() {
        Perfil perfilAdmin = perfilRepository.save(new Perfil(1, "ADMINISTRADOR"));
        Perfil perfilCliente = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Status statusAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSP = estadoRepository.save(new Estado("SP", "São Paulo"));

        Usuario admin = new Usuario();
        admin.setEmail("admin.get@teste.com");
        admin.setSenha(passwordEncoder.encode("123"));
        admin.setCpf("18544976077");
        admin.setNome("Admin Get");
        admin.setPerfil(perfilAdmin);
        admin.setStatus(statusAtivo);
        admin.getEndereco().setEstado(estadoSP);
        usuarioRepository.save(admin);

        Usuario cliente = new Usuario();
        cliente.setEmail("cliente.get@teste.com");
        cliente.setSenha(passwordEncoder.encode("123"));
        cliente.setCpf("09708800066");
        cliente.setNome("Cliente Buscado");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(statusAtivo);
        cliente.getEndereco().setEstado(estadoSP);
        idCliente = usuarioRepository.save(cliente).getId();

        tokenAdmin = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"admin.get@teste.com\", \"senha\": \"123\" }")
                .post("/usuarios/autenticar").then().extract().path("token");
    }

    @Test
    @Order(1)
    @DisplayName("CT181 - Buscar Usuário: Listar com paginação")
    void deveListarUsuariosComPaginacao() {
        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/usuarios?pagina=0&quantidadeItens=5")
                .then().statusCode(200);
    }

    @Test
    @Order(2)
    @DisplayName("CT182 - Buscar Usuário: Recuperar por e-mail exato")
    void deveBuscarUsuarioPorEmailExato() {
        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/usuarios/email/cliente.get@teste.com")
                .then().statusCode(200).body("id", equalTo(idCliente.intValue()));
    }

    @Test
    @Order(3)
    @DisplayName("CT183 - Buscar Usuário: Listar profissionais por perfil")
    void deveListarVeterinariosERecepcionistas() {
        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/usuarios/veterinarios")
                .then().statusCode(200);

        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/usuarios/recepcionistas")
                .then().statusCode(200);
    }
}