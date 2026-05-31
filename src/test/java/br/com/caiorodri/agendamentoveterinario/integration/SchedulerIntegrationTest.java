package br.com.caiorodri.agendamentoveterinario.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.caiorodri.agendamentoveterinario.model.*;
import br.com.caiorodri.agendamentoveterinario.repository.*;
import br.com.caiorodri.agendamentoveterinario.scheduler.AgendamentoStatusScheduler;
import jakarta.persistence.EntityManager;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(11)
@Transactional
public class SchedulerIntegrationTest {

    @Autowired private AgendamentoStatusScheduler scheduler;
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
    @DisplayName("CT162 - Scheduler: Deve marcar agendamentos expirados como PERDIDO/CANCELADO em lote")
    void deveAtualizarLoteDeAgendamentosExpirados() {

        Perfil perfilCliente = perfilRepository.save(new Perfil(1, "CLIENTE"));
        Status statusUsuarioAtivo = statusRepository.save(new Status(1, "ATIVO"));
        Estado estadoSaoPaulo = estadoRepository.save(new Estado("SP", "São Paulo"));

        Especie especieCachorro = especieRepository.save(new Especie());
        Raca racaViraLata = new Raca();
        racaViraLata.setEspecie(especieCachorro);
        Raca racaSalva = racaRepository.save(racaViraLata);
        Sexo sexoMacho = sexoRepository.save(new Sexo(1, "Macho"));

        AgendamentoStatus statusAgendado = agendamentoStatusRepository.save(new AgendamentoStatus(1, "ABERTO"));
        AgendamentoStatus statusPerdido = agendamentoStatusRepository.save(new AgendamentoStatus(4, "PERDIDO"));
        AgendamentoTipo tipoConsulta = agendamentoTipoRepository.save(new AgendamentoTipo(1, "CONSULTA", 30));

        Usuario cliente = new Usuario();
        cliente.setNome("Cliente");
        cliente.setCpf("12345678901");
        cliente.setEmail("cliente@teste.com");
        cliente.setSenha("123");
        cliente.setPerfil(perfilCliente);
        cliente.setStatus(statusUsuarioAtivo);
        cliente.getEndereco().setEstado(estadoSaoPaulo);
        Usuario clienteSalvo = usuarioRepository.save(cliente);

        Usuario veterinario = new Usuario();
        veterinario.setNome("Veterinário");
        veterinario.setCpf("12345678902");
        veterinario.setEmail("veterinario@teste.com");
        veterinario.setSenha("123");
        veterinario.setPerfil(perfilCliente);
        veterinario.setStatus(statusUsuarioAtivo);
        veterinario.getEndereco().setEstado(estadoSaoPaulo);
        Usuario veterinarioSalvo = usuarioRepository.save(veterinario);

        Usuario recepcionista = new Usuario();
        recepcionista.setNome("Recep Scheduler");
        recepcionista.setCpf("55566677788");
        recepcionista.setEmail("recep@teste.com");
        recepcionista.setSenha("123");
        recepcionista.setPerfil(perfilCliente);
        recepcionista.setStatus(statusUsuarioAtivo);
        recepcionista.getEndereco().setEstado(estadoSaoPaulo);
        Usuario recepcionistaSalvo = usuarioRepository.save(recepcionista);

        Animal pet = new Animal();
        pet.setNome("Bob");
        pet.setDono(clienteSalvo);
        pet.setRaca(racaSalva);
        pet.setSexo(sexoMacho);
        Animal petSalvo = animalRepository.save(pet);

        Agendamento agendamentoAtrasado = new Agendamento();
        agendamentoAtrasado.setCliente(clienteSalvo);
        agendamentoAtrasado.setVeterinario(veterinarioSalvo);
        agendamentoAtrasado.setRecepcionista(recepcionistaSalvo);
        agendamentoAtrasado.setAnimal(petSalvo);
        agendamentoAtrasado.setStatus(statusAgendado);
        agendamentoAtrasado.setTipo(tipoConsulta);
        agendamentoAtrasado.setDataAgendamentoInicio(LocalDateTime.now().minusDays(1));
        agendamentoAtrasado.setDataAgendamentoFinal(LocalDateTime.now().minusDays(1).plusHours(1));
        Agendamento agendamentoAtrasadoSalvo = agendamentoRepository.save(agendamentoAtrasado);

        Agendamento agendamentoFuturo = new Agendamento();
        agendamentoFuturo.setCliente(clienteSalvo);
        agendamentoFuturo.setVeterinario(veterinarioSalvo);
        agendamentoFuturo.setRecepcionista(recepcionistaSalvo);
        agendamentoFuturo.setAnimal(petSalvo);
        agendamentoFuturo.setStatus(statusAgendado);
        agendamentoFuturo.setTipo(tipoConsulta);
        agendamentoFuturo.setDataAgendamentoInicio(LocalDateTime.now().plusDays(1));
        agendamentoFuturo.setDataAgendamentoFinal(LocalDateTime.now().plusDays(1).plusHours(1));
        Agendamento agendamentoFuturoSalvo = agendamentoRepository.save(agendamentoFuturo);

        entityManager.flush();
        entityManager.clear();

        scheduler.marcarAgendamentosComoPerdido();

        entityManager.flush();
        entityManager.clear();

        Agendamento agendamentoAtrasadoPosScheduler = agendamentoRepository.findById(agendamentoAtrasadoSalvo.getId()).orElseThrow();
        Agendamento agendamentoFuturoPosScheduler = agendamentoRepository.findById(agendamentoFuturoSalvo.getId()).orElseThrow();

        assertNotEquals(1, agendamentoAtrasadoPosScheduler.getStatus().getId());
        assertEquals(1, agendamentoFuturoPosScheduler.getStatus().getId());
    }
}