package br.com.caiorodri.agendamentoveterinario.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import br.com.caiorodri.agendamentoveterinario.model.*;
import br.com.caiorodri.agendamentoveterinario.repository.*;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Order(14)
@Transactional
class AnimalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("CT167 - Múltiplos Pets: Deve cadastrar 3 animais para o mesmo cliente com sucesso")
    @WithMockUser(roles = "RECEPCIONISTA")
    void deveCadastrarMultiplosPets() throws Exception {

        Perfil perfil = new Perfil(1,"CLIENTE");
        Perfil perfilSalvo = perfilRepository.save(perfil);

        Status status = new Status(1, "ATIVO");
        Status statusSalvo = statusRepository.save(status);

        Estado estado = new Estado();
        estado.setSigla("RJ"); estado.setNome("Rio de Janeiro");
        Estado estadoSalvo = estadoRepository.save(estado);

        Especie especie = new Especie();
        especie.setNome("Cachorro");
        Especie especieSalva = especieRepository.save(especie);

        Raca raca = new Raca();
        raca.setNome("Vira-Lata"); raca.setEspecie(especieSalva);
        Raca racaSalva = racaRepository.save(raca);

        Sexo sexo = new Sexo(1, "Macho");
        Sexo sexoSalvo = sexoRepository.save(sexo);

        Usuario cliente = new Usuario();
        cliente.setNome("Dono de Vários Pets");
        cliente.setEmail("donopets@teste.com");
        cliente.setCpf("12312312312");
        cliente.setSenha("senha123");
        cliente.setPerfil(perfilSalvo);
        cliente.setStatus(statusSalvo);
        cliente.getEndereco().setEstado(estadoSalvo);
        Usuario donoSalvo = usuarioRepository.save(cliente);

        String jsonPetTemplate = """
            {
                "nome": "%s",
                "peso": 10.5,
                "dono": { "id": %d },
                "raca": { "id": %d },
                "sexo": { "id": %d }
            }
            """;

        String[] nomesPets = {"Rex", "Totó", "Bolinha"};

        for (String nome : nomesPets) {
            String jsonRequest = String.format(jsonPetTemplate, nome, donoSalvo.getId(), racaSalva.getId(), sexoSalvo.getId());

            mockMvc.perform(post("/animais")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isCreated());
        }

        entityManager.flush();
        entityManager.clear();

        long totalPetsDoDono = animalRepository.findByUsuarioId(donoSalvo.getId(), org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        assertEquals(3, totalPetsDoDono, "O banco de dados deve registrar 3 pets para este cliente");
    }
}