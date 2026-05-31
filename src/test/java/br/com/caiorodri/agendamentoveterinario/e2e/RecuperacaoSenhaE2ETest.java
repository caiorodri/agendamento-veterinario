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

@Order(24)
public class RecuperacaoSenhaE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long idUsuario;

    @BeforeEach
    public void setup() {
        Perfil perfil = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Status status = statusRepository.save(new Status(1, "ATIVO"));
        Estado estado = estadoRepository.save(new Estado("SP", "São Paulo"));

        Usuario usuario = new Usuario();
        usuario.setEmail("esqueci@teste.com");
        usuario.setSenha(passwordEncoder.encode("123"));
        usuario.setCpf("99999999999");
        usuario.setNome("Esquecido");
        usuario.setPerfil(perfil);
        usuario.setStatus(status);
        usuario.getEndereco().setEstado(estado);
        idUsuario = usuarioRepository.save(usuario).getId();
    }

    @Test
    @DisplayName("CT176 - Recuperação de Senha: Enviar código e falhar com código incorreto")
    void deveDispararEmailEFalharAoValidarCodigoErrado() {

        given()
                .when().get("/usuarios/recuperar-senha/esqueci@teste.com")
                .then().statusCode(200);

        given()
                .when().get("/usuarios/" + idUsuario + "/validar-codigo/000000")
                .then().statusCode(400);
    }
}