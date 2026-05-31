package br.com.caiorodri.agendamentoveterinario.integration;

import br.com.caiorodri.agendamentoveterinario.model.*;
import br.com.caiorodri.agendamentoveterinario.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(13)
@Transactional
public class AgendamentoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private AgendamentoStatusRepository agendamentoStatusRepository;
    @Autowired private AgendamentoTipoRepository agendamentoTipoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PerfilRepository perfilRepository;
    @Autowired private StatusRepository statusRepository;
    @Autowired private EstadoRepository estadoRepository;
    @Autowired private EspecieRepository especieRepository;
    @Autowired private RacaRepository racaRepository;
    @Autowired private SexoRepository sexoRepository;
    @Autowired private AnimalRepository animalRepository;

    @Autowired private EntityManager entityManager;

    @Test
    @Order(1)
    @DisplayName("CT165 - Padronização: API deve retornar 404 para endpoint inexistente")
    void deveRetornar404ParaRotaInexistente() throws Exception {

        mockMvc.perform(get("/agendamento-veterinario/rota-inexistente")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    @Order(2)
    @DisplayName("CT166 - Listar Consultas: Deve retornar agendamentos filtrados por data com HTTP 200")
    @WithMockUser(roles = "RECEPCIONISTA")
    void deveListarAgendamentosPorData() throws Exception {

        Perfil perfilCliente = perfilRepository.save(new Perfil(1, "CLIENTE"));
        Status statusUsuarioAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSaoPaulo = estadoRepository.save(new Estado("SP", "São Paulo"));

        Especie especieCachorro = especieRepository.save(new Especie());
        Raca racaViraLata = new Raca();
        racaViraLata.setEspecie(especieCachorro);
        Raca racaSalva = racaRepository.save(racaViraLata);
        Sexo sexoMacho = sexoRepository.save(new Sexo(1, "Macho"));

        AgendamentoStatus statusAgendado = agendamentoStatusRepository.save(new AgendamentoStatus(1, "ABERTO"));
        AgendamentoTipo tipoConsulta = agendamentoTipoRepository.save(new AgendamentoTipo(1, "CONSULTA", 30));

        Usuario cliente = new Usuario();
        cliente.setNome("Cliente Agendamento");
        cliente.setCpf("11122233344");
        cliente.setEmail("cli.agend@teste.com");
        cliente.setSenha("123");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(statusUsuarioAtivo);
        cliente.getEndereco().setEstado(estadoSaoPaulo);
        Usuario clienteSalvo = usuarioRepository.save(cliente);

        Usuario veterinario = new Usuario();
        veterinario.setNome("Vet Agendamento");
        veterinario.setCpf("99988877766");
        veterinario.setEmail("vet.agend@teste.com");
        veterinario.setSenha("123");
        veterinario.setPerfil(perfilCliente);
        veterinario.setStatus(statusUsuarioAtivo);
        veterinario.getEndereco().setEstado(estadoSaoPaulo);
        Usuario veterinarioSalvo = usuarioRepository.save(veterinario);

        Usuario recepcionista = new Usuario();
        recepcionista.setNome("Recep Agendamento");
        recepcionista.setCpf("55566677788");
        recepcionista.setEmail("recep.agend@teste.com");
        recepcionista.setSenha("123");
        recepcionista.setPerfil(perfilCliente);
        recepcionista.setStatus(statusUsuarioAtivo);
        recepcionista.getEndereco().setEstado(estadoSaoPaulo);
        Usuario recepcionistaSalvo = usuarioRepository.save(recepcionista);

        Animal pet = new Animal();
        pet.setNome("Bob"); pet.setDono(clienteSalvo); pet.setRaca(racaSalva); pet.setSexo(sexoMacho);
        Animal petSalvo = animalRepository.save(pet);

        Agendamento agendamentoAlvo = new Agendamento();
        agendamentoAlvo.setCliente(clienteSalvo);
        agendamentoAlvo.setVeterinario(veterinarioSalvo);
        agendamentoAlvo.setRecepcionista(recepcionistaSalvo);
        agendamentoAlvo.setAnimal(petSalvo);
        agendamentoAlvo.setStatus(statusAgendado);
        agendamentoAlvo.setTipo(tipoConsulta);
        agendamentoAlvo.setDataAgendamentoInicio(LocalDateTime.of(2026, 4, 25, 14, 0));
        agendamentoAlvo.setDataAgendamentoFinal(LocalDateTime.of(2026, 4, 25, 14, 30));
        agendamentoRepository.save(agendamentoAlvo);

        Agendamento agendamentoRuido = new Agendamento();
        agendamentoRuido.setCliente(clienteSalvo);
        agendamentoRuido.setVeterinario(veterinarioSalvo);
        agendamentoRuido.setRecepcionista(recepcionistaSalvo);
        agendamentoRuido.setAnimal(petSalvo);
        agendamentoRuido.setStatus(statusAgendado);
        agendamentoRuido.setTipo(tipoConsulta);
        agendamentoRuido.setDataAgendamentoInicio(LocalDateTime.of(2026, 4, 26, 14, 0));
        agendamentoRuido.setDataAgendamentoFinal(LocalDateTime.of(2026, 4, 26, 14, 30));
        agendamentoRepository.save(agendamentoRuido);

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/agendamentos/data")
                        .param("data", "2026-04-25")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(agendamentoAlvo.getId()));
    }

}
