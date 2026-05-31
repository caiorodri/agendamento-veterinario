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
import static org.hamcrest.Matchers.notNullValue;

@Order(28)
public class AtualizarUsuarioE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAdmin;
    private Long idCliente;
    private String siglaEstado;

    @BeforeEach
    public void setup() {
        Perfil perfilAdmin = perfilRepository.save(new Perfil(1, "ADMINISTRADOR"));
        Perfil perfilCliente = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Status statusAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSP = estadoRepository.save(new Estado("SP", "São Paulo"));
        siglaEstado = estadoSP.getSigla();

        Usuario admin = new Usuario();
        admin.setEmail("admin.put@teste.com");
        admin.setSenha(passwordEncoder.encode("123"));
        admin.setCpf("18544976077");
        admin.setNome("Admin Put");
        admin.setPerfil(perfilAdmin);
        admin.setStatus(statusAtivo);
        admin.getEndereco().setEstado(estadoSP);
        usuarioRepository.save(admin);

        Usuario cliente = new Usuario();
        cliente.setEmail("cliente.put@teste.com");
        cliente.setSenha(passwordEncoder.encode("123"));
        cliente.setCpf("09708800066");
        cliente.setNome("Cliente Antigo");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(statusAtivo);
        cliente.getEndereco().setEstado(estadoSP);
        idCliente = usuarioRepository.save(cliente).getId();

        tokenAdmin = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"admin.put@teste.com\", \"senha\": \"123\" }")
                .post("/usuarios/autenticar").then().extract().path("token");
    }

    @Test
    @DisplayName("CT180 - Atualizar Usuário: Alterar dados de cadastro com sucesso")
    void deveAtualizarDadosDoUsuarioComSucesso() {
        String jsonUpdate = String.format("""
                {
                    "id": %d,
                    "nome": "Cliente Atualizado",
                    "email": "cliente.put@teste.com",
                    "cpf": "09708800066",
                    "perfil": { "id": 2 },
                    "status": { "id": 1 },
                    "telefones": ["11999999999"],
                    "endereco": {
                        "cep": "01001000",
                        "logradouro": "Praca da Se",
                        "numero": "123",
                        "cidade": "Sao Paulo",
                        "estado": { "sigla": "%s" }
                    }
                }
                """, idCliente, siglaEstado);

        given().header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON).body(jsonUpdate)
                .when().put("/usuarios")
                .then().statusCode(200).body("token", notNullValue());

        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/usuarios/email/cliente.put@teste.com")
                .then().statusCode(200).body("nome", equalTo("Cliente Atualizado"));
    }
}