package br.com.caiorodri.agendamentoveterinario.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(4)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    @Order(1)
    @DisplayName("CT69 - Login inválidos: Deve lançar UsernameNotFoundException")
    void deveLancarExcecaoQuandoEmailNaoExistir() {

        String emailFalso = "emailInexistente@email.com";

        when(usuarioRepository.findByEmailWithSets(emailFalso)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(emailFalso);
        });

        assertNotNull(exception);
        verify(usuarioRepository, times(1)).findByEmailWithSets(emailFalso);

    }
}