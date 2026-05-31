package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.model.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

@Order(25)
public class VeterinarioHorariosE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long idVet;
    private String tokenVet;

    @BeforeEach
    public void setup() {

        diaSemanaRepository.save(new DiaSemana(2, "Segunda-feira"));
        Perfil perfilVet = perfilRepository.save(new Perfil(3, "VETERINARIO"));
        Status statusAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSP = estadoRepository.save(new Estado("SP", "São Paulo"));
        agendamentoTipoRepository.save(new AgendamentoTipo(1, "CONSULTA", 30));

        Usuario vet = new Usuario();
        vet.setEmail("vet.agenda@teste.com");
        vet.setSenha(passwordEncoder.encode("123"));
        vet.setCpf("88888888888");
        vet.setNome("Vet Agenda");
        vet.setPerfil(perfilVet);
        vet.setStatus(statusAtivo);
        vet.getEndereco().setEstado(estadoSP);
        idVet = usuarioRepository.save(vet).getId();

        tokenVet = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"vet.agenda@teste.com\", \"senha\": \"123\" }")
                .post("/usuarios/autenticar").then().extract().path("token");
    }

    @Test
    @DisplayName("CT177 - Horários E2E: Criar horário, listar horários e slots disponíveis")
    void deveGerenciarHorariosDeTrabalhoDoVeterinario() {
        String jsonHorario = String.format("""
                {
                    "idVeterinario": %d,
                    "idDiaSemana": 2,
                    "horaInicio": "08:00:00",
                    "horaFim": "12:00:00"
                }
                """, idVet);

        Number idHorarioGerado = given()
                .header("Authorization", "Bearer " + tokenVet)
                .contentType(ContentType.JSON)
                .body(jsonHorario)
                .when().post("/usuarios/veterinarios/horarios")
                .then().statusCode(201)
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + tokenVet)
                .when().get("/usuarios/veterinarios/horarios/" + idVet)
                .then().statusCode(200)
                .body("size()", greaterThan(0));

        given()
                .header("Authorization", "Bearer " + tokenVet)
                .when().delete("/usuarios/veterinarios/horarios/" + idHorarioGerado.longValue())
                .then().statusCode(204);
    }
}