package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.model.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Order(23)
public class ResultadoConsultaE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long idAgendamento;
    private String tokenVeterinario;
    private String tokenCliente;

    @BeforeEach
    public void setup() {
        Perfil perfilCliente = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Perfil perfilVet = perfilRepository.save(new Perfil(3, "VETERINARIO"));
        Status statusAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSP = estadoRepository.save(new Estado("SP", "São Paulo"));

        Especie especie = especieRepository.save(new Especie(null, "Gato"));
        Raca raca = racaRepository.save(new Raca(null, especie, "Persa"));
        Sexo sexo = sexoRepository.save(new Sexo(2, "Fêmea"));

        agendamentoStatusRepository.save(new AgendamentoStatus(1, "ABERTO"));
        agendamentoStatusRepository.save(new AgendamentoStatus(3, "CONCLUÍDO"));
        agendamentoTipoRepository.save(new AgendamentoTipo(1, "CONSULTA", 30));

        Usuario cliente = new Usuario();
        cliente.setEmail("cli.resultado@teste.com");
        cliente.setSenha(passwordEncoder.encode("123"));
        cliente.setCpf("12312312312");
        cliente.setNome("Cli Res");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(statusAtivo);
        cliente.getEndereco().setEstado(estadoSP);
        Long idCliente = usuarioRepository.save(cliente).getId();

        Usuario vet = new Usuario();
        vet.setEmail("vet.resultado@teste.com");
        vet.setSenha(passwordEncoder.encode("123"));
        vet.setCpf("32132132132");
        vet.setNome("Vet Res");
        vet.setPerfil(perfilVet);
        vet.setStatus(statusAtivo);
        vet.getEndereco().setEstado(estadoSP);
        Long idVet = usuarioRepository.save(vet).getId();

        Animal animal = new Animal();
        animal.setNome("Mia");
        animal.setDono(new Usuario(idCliente));
        animal.setRaca(raca);
        animal.setSexo(sexo);
        Long idAnimal = animalRepository.save(animal).getId();

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(new Usuario(idCliente));
        agendamento.setVeterinario(new Usuario(idVet));
        agendamento.setRecepcionista(new Usuario(idVet));
        agendamento.setAnimal(new Animal(idAnimal));
        agendamento.setStatus(new AgendamentoStatus(1));
        agendamento.setTipo(new AgendamentoTipo(1));
        agendamento.setDataCriacao(LocalDateTime.now());
        agendamento.setDataAgendamentoInicio(LocalDateTime.now().minusDays(1));
        agendamento.setDataAgendamentoFinal(LocalDateTime.now().minusDays(1).plusMinutes(30));
        idAgendamento = agendamentoRepository.save(agendamento).getId();

        tokenVeterinario = given().contentType(ContentType.JSON).body("{ \"email\": \"vet.resultado@teste.com\", \"senha\": \"123\" }").post("/usuarios/autenticar").then().extract().path("token");
        tokenCliente = given().contentType(ContentType.JSON).body("{ \"email\": \"cli.resultado@teste.com\", \"senha\": \"123\" }").post("/usuarios/autenticar").then().extract().path("token");
    }

    @Test
    @DisplayName("CT175 - Resultados E2E: Veterinário preenche resultado e adiciona prescrição")
    void deveSalvarERecuperarResultadoDeConsulta() {
        String jsonResultado = String.format("""
                {
                    "agendamento": { "id": %d },
                    "diagnosticoPrincipal": "Gripe felina leve",
                    "observacoesVeterinario": "Repouso recomendado.",
                    "prescricoes": [ { "nomeMedicamento": "Antibiótico", "dosagem": "1 comp", "instrucoesUso": "A cada 12h" } ]
                }
                """, idAgendamento);

        given()
                .header("Authorization", "Bearer " + tokenVeterinario)
                .contentType(ContentType.JSON)
                .body(jsonResultado)
                .when().post("/agendamentos/resultado")
                .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + tokenCliente)
                .contentType(ContentType.JSON)
                .when().get("/agendamentos/" + idAgendamento + "/resultado")
                .then().statusCode(200)
                .body("diagnosticoPrincipal", equalTo("Gripe felina leve"))
                .body("prescricoes[0].nomeMedicamento", equalTo("Antibiótico"));
    }
}