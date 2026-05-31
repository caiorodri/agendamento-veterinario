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
import static org.hamcrest.Matchers.greaterThan;

@Order(27)
public class DicionariosE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;

    @BeforeEach
    public void setup() {
        Perfil perfil = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Status status = statusRepository.save(new Status(1, "ATIVO"));
        Estado estado = estadoRepository.save(new Estado("SP", "São Paulo"));

        Especie e = especieRepository.save(new Especie(null, "Cachorro"));
        racaRepository.save(new Raca(null, e, "Vira-Lata"));
        sexoRepository.save(new Sexo(1, "Macho"));

        Usuario usuario = new Usuario();
        usuario.setEmail("dicionario.teste@teste.com");
        usuario.setSenha(passwordEncoder.encode("123"));
        usuario.setCpf("55555555555");
        usuario.setNome("User Dicionario");
        usuario.setPerfil(perfil);
        usuario.setStatus(status);
        usuario.getEndereco().setEstado(estado);
        usuarioRepository.save(usuario);

        token = given()
                .contentType(ContentType.JSON)
                .body("{ \"email\": \"dicionario.teste@teste.com\", \"senha\": \"123\" }")
                .post("/usuarios/autenticar")
                .then()
                .extract()
                .path("token");
    }

    @Test
    @DisplayName("CT179 - Dicionários E2E: Buscar listas de domínios")
    void deveBuscarDicionariosSemErros() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/usuarios/estados")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/animais/especies")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/animais/racas")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/animais/sexos")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }
}