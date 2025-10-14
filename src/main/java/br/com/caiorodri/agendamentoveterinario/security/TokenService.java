package br.com.caiorodri.agendamentoveterinario.security;

import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long  expiration;

    @PostConstruct
    public void verificarChaveSecreta() {
        System.out.println("==========================================================");
        if (secret == null || secret.isBlank() || secret.equals("${JWT_SECRET}")) {
            System.out.println("### ALERTA DE SEGURANÇA: A chave secreta JWT NÃO foi carregada corretamente! ###");
            System.out.println("### Valor lido: [" + secret + "] ###");
        } else {
            System.out.println(">>> Chave secreta JWT carregada com sucesso.");
        }
        System.out.println("==========================================================");
    }


    public String generateToken(Usuario usuario) {
        try {
            // A classe Algorithm vem da biblioteca auth0-java-jwt
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("agendamento-veterinario-api")
                    .withSubject(usuario.getEmail())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar o token JWT", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("agendamento-veterinario-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return "";
        }
    }

    private Instant genExpirationDate() {
        return Instant.now().plusMillis(expiration);
    }

}
