package br.com.caiorodri.agendamentoveterinario.security;

import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    TokenService tokenService;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException

    {
        var token = this.recoverToken(request);
        if (token != null) {
            var email = tokenService.validateToken(token);
            Usuario user = usuarioRepository.findByEmailWithSets(email).orElse(null);

            if (user != null) {

                Optional<Usuario> userWithAgendamentos = usuarioRepository.findByIdWithAgendamentos(user.getId());

                if (userWithAgendamentos.isPresent()) {
                    user.setAgendamentos(userWithAgendamentos.get().getAgendamentos());
                }

                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}