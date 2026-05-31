package br.com.caiorodri.agendamentoveterinario.integration;

import br.com.caiorodri.agendamentoveterinario.controller.UsuarioController;
import br.com.caiorodri.agendamentoveterinario.model.Estado;
import br.com.caiorodri.agendamentoveterinario.model.Perfil;
import br.com.caiorodri.agendamentoveterinario.model.Status;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.repository.EstadoRepository;
import br.com.caiorodri.agendamentoveterinario.repository.PerfilRepository;
import br.com.caiorodri.agendamentoveterinario.repository.StatusRepository;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;
import br.com.caiorodri.agendamentoveterinario.service.UsuarioService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(12)
@Transactional
public class UsuarioIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PerfilRepository perfilRepository;
    @Autowired private StatusRepository statusRepository;
    @Autowired private EstadoRepository estadoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @Order(1)
    @DisplayName("CT163 - Segurança: Deve autenticar usuário com credenciais válidas e retornar token JWT (HTTP 200)")
    void deveAutenticarERetornarTokenJwt() throws Exception {

        Perfil perfil = new Perfil(1, "CLIENTE");
        Perfil perfilSalvo = perfilRepository.save(perfil);

        Status status = new Status(1, "ATIVO");
        Status statusSalvo = statusRepository.save(status);

        Estado estado = new Estado("SP", "São Paulo");
        Estado estadoSalvo = estadoRepository.save(estado);

        Usuario cliente = new Usuario();
        cliente.setNome("Cliente Login");
        cliente.setEmail("login@teste.com");
        cliente.setCpf("11122233344");

        cliente.setSenha(passwordEncoder.encode("senha123"));

        cliente.setPerfil(perfilSalvo);
        cliente.setStatus(statusSalvo);
        cliente.getEndereco().setEstado(estadoSalvo);

        usuarioRepository.save(cliente);

        String jsonLogin = """
                {
                    "email": "login@teste.com",
                    "senha": "senha123"
                }
                """;

        mockMvc.perform(post("/usuarios/autenticar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonLogin))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());

    }

    @Test
    @DisplayName("CT164 - Excluir Cliente: API deve deletar cliente no BD e retornar 204 'No Content'")
    @WithMockUser(roles = "ADMINISTRADOR")
    @Order(2)
    void deveDeletarClienteERetornar204() throws Exception {

        Perfil perfil = new Perfil(1,"CLIENTE");
        Perfil perfilSalvo = perfilRepository.save(perfil);

        Status status = new Status(1, "ATIVO");
        Status statusSalvo = statusRepository.save(status);

        Estado estado = new Estado();
        estado.setSigla("SP");
        estado.setNome("São Paulo");
        Estado estadoSalvo = estadoRepository.save(estado);

        Usuario cliente = new Usuario();
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@teste.com");
        cliente.setCpf("99988877766");
        cliente.setSenha("senha123");
        cliente.setPerfil(perfilSalvo);
        cliente.setStatus(statusSalvo);
        cliente.getEndereco().setEstado(estadoSalvo);

        Usuario clienteSalvo = usuarioRepository.save(cliente);

        mockMvc.perform(delete("/usuarios/" + clienteSalvo.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        boolean aindaExiste = usuarioRepository.existsById(clienteSalvo.getId());
        assertFalse(aindaExiste, "O usuário deveria ter sido excluído do banco de dados H2");

    }

}
