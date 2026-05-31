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

@Order(32)
public class AtualizarAgendamentoE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private Long idAgendamento;
    private Long idCliente;
    private Long idVet;
    private Long idAnimal;

    @BeforeEach
    public void setup() {
        Perfil perfilCliente = perfilRepository.save(new Perfil(2, "CLIENTE"));
        Perfil perfilVet = perfilRepository.save(new Perfil(3, "VETERINARIO"));
        Status status = statusRepository.save(new Status(1, "ATIVO"));
        Estado estado = estadoRepository.save(new Estado("SP", "São Paulo"));

        agendamentoStatusRepository.save(new AgendamentoStatus(1, "ABERTO"));
        agendamentoTipoRepository.save(new AgendamentoTipo(1, "CONSULTA", 30));

        Especie especie = especieRepository.save(new Especie(null, "Cachorro"));
        Raca raca = racaRepository.save(new Raca(null, especie, "Poodle"));
        Sexo sexo = sexoRepository.save(new Sexo(1, "Macho"));

        Usuario cliente = new Usuario();
        cliente.setEmail("cli.agenda.put@teste.com");
        cliente.setSenha(passwordEncoder.encode("123"));
        cliente.setCpf("09708800066");
        cliente.setNome("Cliente Agenda Put");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(status);
        cliente.getEndereco().setEstado(estado);
        idCliente = usuarioRepository.save(cliente).getId();

        Usuario vet = new Usuario();
        vet.setEmail("vet.agenda.put@teste.com");
        vet.setSenha(passwordEncoder.encode("123"));
        vet.setCpf("18544976077");
        vet.setNome("Vet Agenda Put");
        vet.setPerfil(perfilVet);
        vet.setStatus(status);
        vet.getEndereco().setEstado(estado);
        idVet = usuarioRepository.save(vet).getId();

        Animal animal = new Animal();
        animal.setNome("Thor");
        animal.setRaca(raca);
        animal.setSexo(sexo);
        animal.setDono(new Usuario(idCliente));
        idAnimal = animalRepository.save(animal).getId();

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(new Usuario(idCliente));
        agendamento.setVeterinario(new Usuario(idVet));
        agendamento.setRecepcionista(new Usuario(idVet));
        agendamento.setAnimal(new Animal(idAnimal));
        agendamento.setStatus(new AgendamentoStatus(1));
        agendamento.setTipo(new AgendamentoTipo(1));
        agendamento.setDataCriacao(LocalDateTime.now());
        agendamento.setDataAgendamentoInicio(LocalDateTime.now().plusDays(5).withHour(10).withMinute(0));
        agendamento.setDataAgendamentoFinal(LocalDateTime.now().plusDays(5).withHour(10).withMinute(30));
        idAgendamento = agendamentoRepository.save(agendamento).getId();

        token = given().contentType(ContentType.JSON)
                .body("{ \"email\": \"cli.agenda.put@teste.com\", \"senha\": \"123\" }")
                .post("/usuarios/autenticar").then().extract().path("token");
    }

    @Test
    @DisplayName("CT188 - Atualizar Agendamento: Reagendar data e horário da consulta")
    void deveReagendarDataEHorarioDaConsulta() {
        LocalDateTime novaData = LocalDateTime.now().plusDays(6).withHour(14).withMinute(0).withSecond(0).withNano(0);

        String jsonUpdate = String.format("""
                {
                    "id": %d,
                    "cliente": { "id": %d },
                    "recepcionista": { "id": %d },
                    "veterinario": { "id": %d },
                    "animal": { "id": %d },
                    "tipo": { "id": 1 },
                    "status": { "id": 1 },
                    "dataAgendamentoInicio": "%s",
                    "dataAgendamentoFinal": "%s"
                }
                """, idAgendamento, idCliente, idVet, idVet, idAnimal, novaData.toString(), novaData.plusMinutes(30).toString());

        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON).body(jsonUpdate)
                .when().put("/agendamentos")
                .then().statusCode(200);
    }
}