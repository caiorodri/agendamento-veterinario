package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.model.Estado;
import br.com.caiorodri.agendamentoveterinario.model.Perfil;
import br.com.caiorodri.agendamentoveterinario.model.Status;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@Order(18)
public class SegurancaE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long idUsuarioAdmin;

    @BeforeEach
    public void setUp() {
        prepararUsuarioParaLogin();
    }

    private void prepararUsuarioParaLogin() {
        Perfil perfilAdmin = perfilRepository.save(new Perfil(1, "ADMINISTRADOR"));
        Status statusAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSP = estadoRepository.save(new Estado("SP", "São Paulo"));

        Usuario admin = new Usuario();
        admin.setEmail("admin@teste.com");
        admin.setSenha(passwordEncoder.encode("senha123"));
        admin.setCpf("12345678901");
        admin.setNome("Admin");
        admin.setPerfil(perfilAdmin);
        admin.setStatus(statusAtivo);
        admin.getEndereco().setEstado(estadoSP);

        idUsuarioAdmin = usuarioRepository.save(admin).getId();
    }

    @Test
    @DisplayName("CT170 - E2E Segurança: Bloqueio sem token e acesso liberado com JWT válido")
    void deveValidarAutenticacaoEPermissaoDeAcesso() {

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/usuarios/" + idUsuarioAdmin)
                .then()
                .statusCode(403);

        String jsonLogin = "{ \"email\": \"admin@teste.com\", \"senha\": \"senha123\" }";

        String token = given()
                .contentType(ContentType.JSON)
                .body(jsonLogin)
                .when()
                .post("/usuarios/autenticar")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract().path("token");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/usuarios/" + idUsuarioAdmin)
                .then()
                .statusCode(200);
    }
}