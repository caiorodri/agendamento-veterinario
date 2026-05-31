package br.com.caiorodri.agendamentoveterinario.e2e;

import br.com.caiorodri.agendamentoveterinario.model.*;
import br.com.caiorodri.agendamentoveterinario.repository.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Order(16)
public class EscalabilidadeE2ETest extends BaseE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAutenticacao;
    private Long idDono;
    private Long idVeterinario;
    private Long idRecepcionista;
    private Long idAnimal;

    @BeforeEach
    public void setUp() {

        prepararDadosParaConcorrencia();

        String jsonLogin = "{ \"email\": \"administrador.concorrencia@teste.com\", \"senha\": \"senha123\" }";

        tokenAutenticacao = given()
                .contentType(ContentType.JSON)
                .body(jsonLogin)
                .post("/usuarios/autenticar")
                .then()
                .statusCode(200)
                .extract().path("token");
    }

    private void prepararDadosParaConcorrencia() {

        Perfil perfil = perfilRepository.save(new Perfil(1, "ADMINISTRADOR"));
        Status status = statusRepository.save(new Status(1, "ATIVO"));
        Estado estado = estadoRepository.save(new Estado("SP", "São Paulo"));

        agendamentoStatusRepository.save(new AgendamentoStatus(1, "ABERTO"));
        agendamentoTipoRepository.save(new AgendamentoTipo(1, "CONSULTA", 30));

        Especie especie = especieRepository.save(new Especie());
        Raca raca = new Raca();
        raca.setEspecie(especie);
        racaRepository.save(raca);
        Sexo sexo = sexoRepository.save(new Sexo(1, "Macho"));

        String senhaHash = passwordEncoder.encode("senha123");

        Usuario administrador = new Usuario();
        administrador.setEmail("administrador.concorrencia@teste.com");
        administrador.setSenha(senhaHash);
        administrador.setCpf("00000000001");
        administrador.setNome("Administrador");
        administrador.setPerfil(perfil);
        administrador.setStatus(status);
        administrador.getEndereco().setEstado(estado);
        idDono = usuarioRepository.save(administrador).getId();

        Usuario veterinario = new Usuario();
        veterinario.setEmail("veterinario.concorrencia@teste.com");
        veterinario.setSenha(senhaHash);
        veterinario.setCpf("00000000002");
        veterinario.setNome("Veterinário");
        veterinario.setPerfil(perfil);
        veterinario.setStatus(status);
        veterinario.getEndereco().setEstado(estado);
        idVeterinario = usuarioRepository.save(veterinario).getId();

        Usuario recepcionista = new Usuario();
        recepcionista.setEmail("recepcionista.concorrencia@teste.com");
        recepcionista.setSenha(senhaHash);
        recepcionista.setCpf("00000000003");
        recepcionista.setNome("Recepcionista");
        recepcionista.setPerfil(perfil);
        recepcionista.setStatus(status);
        recepcionista.getEndereco().setEstado(estado);
        idRecepcionista = usuarioRepository.save(recepcionista).getId();

        Animal animal = new Animal();
        animal.setNome("Bolinha");
        animal.setDono(new Usuario(idDono));
        animal.setRaca(raca);
        animal.setSexo(sexo);
        idAnimal = animalRepository.save(animal).getId();
    }

    @Test
    @DisplayName("CT168 - Concorrência: 50 agendamentos simultâneos (1 aprovado, 49 rejeitados)")
    void deveProcessarApenasUmAgendamentoESuportarCarga() throws InterruptedException {

        int totalRequisicoes = 50;
        AtomicInteger sucessos = new AtomicInteger(0);
        AtomicInteger falhas = new AtomicInteger(0);

        CountDownLatch largada = new CountDownLatch(1);
        CountDownLatch conclusao = new CountDownLatch(totalRequisicoes);

        String jsonAgendamento = gerarJsonAgendamento();

        var requestSpec = given()
                .header("Authorization", "Bearer " + tokenAutenticacao)
                .contentType(ContentType.JSON)
                .body(jsonAgendamento);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < totalRequisicoes; i++) {

                executor.submit(() -> {
                    try {
                        largada.await();

                        Response response = given().spec(requestSpec).post("/agendamentos");

                        if (response.statusCode() == 201) sucessos.incrementAndGet();
                        else falhas.incrementAndGet();
                    } catch (Exception e) {
                        falhas.incrementAndGet();
                    } finally {
                        conclusao.countDown();
                    }
                });
            }

            largada.countDown();
            conclusao.await();
        }

        assertEquals(1, sucessos.get(), "Apenas um agendamento deve ser criado para o mesmo horário");
        assertEquals(49, falhas.get(), "As demais requisições devem falhar por conflito");
    }

    private String gerarJsonAgendamento() {
        return String.format("""
            {
                "cliente": { "id": %d },
                "veterinario": { "id": %d },
                "recepcionista": { "id": %d },
                "animal": { "id": %d },
                "status": { "id": 1 },
                "tipo": { "id": 1 },
                "dataAgendamentoInicio": "%s",
                "dataAgendamentoFinal": "%s"
            }
            """, idDono, idVeterinario, idRecepcionista, idAnimal,
                LocalDateTime.now().plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().plusDays(10).withHour(10).withMinute(30).withSecond(0).withNano(0));
    }
}