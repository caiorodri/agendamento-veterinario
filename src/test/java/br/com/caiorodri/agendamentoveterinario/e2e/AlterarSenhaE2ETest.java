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

@Order(22)
public class AlterarSenhaE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenCliente;

    @BeforeEach
    public void setup() {
        Perfil perfilCliente = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Status statusAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSP = estadoRepository.save(new Estado("SP", "São Paulo"));

        Usuario cliente = new Usuario();
        cliente.setEmail("cliente.senha@teste.com");
        cliente.setSenha(passwordEncoder.encode("senhaAtual123"));
        cliente.setCpf("41231231234");
        cliente.setNome("Cliente Senha");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(statusAtivo);
        cliente.getEndereco().setEstado(estadoSP);
        usuarioRepository.save(cliente);

        tokenCliente = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"cliente.senha@teste.com\", \"senha\": \"senhaAtual123\" }")
                .post("/usuarios/autenticar").then().statusCode(200).extract().path("token");
    }

    @Test
    @DisplayName("CT174 - Alterar Senha E2E: Fluxo de alteração e novo login")
    void deveAlterarSenhaEAutenticarComNovaSenha() {
        String jsonAlterarSenha = "{ \"senhaAntiga\": \"senhaAtual123\", \"senhaNova\": \"novaSenha456\" }";

        given()
                .header("Authorization", "Bearer " + tokenCliente)
                .contentType(ContentType.JSON)
                .body(jsonAlterarSenha)
                .when().put("/usuarios/alterar-senha")
                .then().statusCode(204);

        given().contentType(ContentType.JSON)
                .body("{ \"email\": \"cliente.senha@teste.com\", \"senha\": \"senhaAtual123\" }")
                .when().post("/usuarios/autenticar")
                .then().statusCode(401);

        given().contentType(ContentType.JSON)
                .body("{ \"email\": \"cliente.senha@teste.com\", \"senha\": \"novaSenha456\" }")
                .when().post("/usuarios/autenticar")
                .then().statusCode(200).body("token", notNullValue());
    }
}