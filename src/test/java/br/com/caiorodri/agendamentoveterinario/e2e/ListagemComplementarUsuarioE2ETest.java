package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.model.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Order(34)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ListagemComplementarUsuarioE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAdmin;
    private Long idCliente;
    private Long idAutoAtendimento;

    @BeforeEach
    public void setup() {
        Perfil perfilCliente = perfilRepository.save(new Perfil(1, "CLIENTE"));
        Perfil perfilRecepcionista = perfilRepository.save(new Perfil(2, "RECEPCIONISTA"));
        Perfil perfilVeterinario = perfilRepository.save(new Perfil(3, "VETERINARIO"));
        Perfil perfilAdmin = perfilRepository.save(new Perfil(4, "ADMINISTRADOR"));
        Status statusAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSP = estadoRepository.save(new Estado("SP", "São Paulo"));

        Usuario admin = new Usuario();
        admin.setEmail("admin.extra@teste.com");
        admin.setSenha(passwordEncoder.encode("123"));
        admin.setCpf("18544976077");
        admin.setNome("Admin Extra");
        admin.setPerfil(perfilAdmin);
        admin.setStatus(statusAtivo);
        admin.getEndereco().setEstado(estadoSP);
        usuarioRepository.save(admin);

        Usuario cliente = new Usuario();
        cliente.setEmail("cliente.extra@teste.com");
        cliente.setSenha(passwordEncoder.encode("123"));
        cliente.setCpf("09708800066");
        cliente.setNome("Cliente Extra");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(statusAtivo);
        cliente.getEndereco().setEstado(estadoSP);
        idCliente = usuarioRepository.save(cliente).getId();

        Usuario autoAtendimento = new Usuario();
        autoAtendimento.setEmail("auto@teste.com");
        autoAtendimento.setSenha(passwordEncoder.encode("123"));
        autoAtendimento.setCpf("62947118029");
        autoAtendimento.setNome("AUTO ATENDIMENTO");
        autoAtendimento.setPerfil(perfilRecepcionista);
        autoAtendimento.setStatus(statusAtivo);
        autoAtendimento.getEndereco().setEstado(estadoSP);
        idAutoAtendimento = usuarioRepository.save(autoAtendimento).getId();

        Usuario vet = new Usuario();
        vet.setEmail("vet.extra@teste.com");
        vet.setSenha(passwordEncoder.encode("123"));
        vet.setCpf("75481358055");
        vet.setNome("Vet Extra");
        vet.setPerfil(perfilVeterinario);
        vet.setStatus(statusAtivo);
        vet.getEndereco().setEstado(estadoSP);
        usuarioRepository.save(vet);

        tokenAdmin = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"admin.extra@teste.com\", \"senha\": \"123\" }")
                .post("/usuarios/autenticar").then().extract().path("token");
    }

    @Test
    @Order(1)
    @DisplayName("CT193 - Listar clientes paginado e lista completa")
    void deveListarClientesPaginadoETodos() {
        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/usuarios/clientes?pagina=0&quantidadeItens=5")
                .then().statusCode(200)
                .body("content.size()", greaterThanOrEqualTo(1));

        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/usuarios/clientes/ativo")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(2)
    @DisplayName("CT194 - Listar funcionários paginado e lista completa")
    void deveListarFuncionariosPaginadoETodos() {
        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/usuarios/funcionarios?pagina=0&quantidadeItens=5")
                .then().statusCode(200)
                .body("content.size()", greaterThanOrEqualTo(1));

        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/usuarios/funcionarios/todos")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(3)
    @DisplayName("CT195 - Autoatendimento: Deve retornar 200 ao encontrar o usuário")
    void deveRetornarRecepcionistaAutoAtendimento() {
        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/usuarios/recepcionista/auto-atendimento")
                .then().statusCode(200)
                .body("nome", equalTo("AUTO ATENDIMENTO"));
    }

    @Test
    @Order(4)
    @DisplayName("CT196 - Autoatendimento: Deve retornar 404 ao não encontrar o usuário")
    void deveRetornar404QuandoAutoAtendimentoNaoExistir() {
        usuarioRepository.deleteById(idAutoAtendimento);

        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/usuarios/recepcionista/auto-atendimento")
                .then().statusCode(404);
    }

    @Test
    @Order(5)
    @DisplayName("CT197 - Validar Código Recuperação: Deve retornar 200 para código válido")
    void deveRetornar200ParaCodigoValido() {

        Usuario cliente = usuarioRepository.findById(idCliente).orElseThrow();
        cliente.setCodigoRecuperacao("123456");
        cliente.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(cliente);

        given()
                .when().get("/usuarios/" + idCliente + "/validar-codigo/123456")
                .then().statusCode(200);
    }

    @Test
    @Order(6)
    @DisplayName("CT198 - Renovar Token: Deve retornar os dados do usuário com um novo token JWT")
    void deveRenovarTokenEBuscarUsuarioLogado() {
        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/usuarios/me/token")
                .then().statusCode(200)
                .body("token", notNullValue())
                .body("usuario.nome", equalTo("Admin Extra"));
    }

    @Test
    @Order(7)
    @DisplayName("CT199 - Disparar campanha de vacinação: Deve retornar 202 Accepted")
    void deveAceitarRequisicaoParaCampanhaDeVacinacao() {
        given().header("Authorization", "Bearer " + tokenAdmin)
                .when().post("/usuarios/enviar-campanha-vacinacao")
                .then().statusCode(202);
    }
}



