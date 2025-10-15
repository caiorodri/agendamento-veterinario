package br.com.caiorodri.agendamentoveterinario.security;

import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.service.AnimalService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long  expiration;

    final static Logger logger = LoggerFactory.getLogger(TokenService.class);


    public String generateToken(Usuario usuario) {

        logger.info("[generateToken] - Inicio - Gerando Token");

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("agendamento-veterinario-api")
                    .withSubject(usuario.getEmail())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);

            logger.info("[generateToken] - Fim - Token gerado com sucesso");

            return token;
        } catch (JWTCreationException exception) {

            logger.error("[generateToken] - Fim - Erro ao gerar token");
            throw new RuntimeException("Erro ao gerar o token JWT", exception);
        }
    }

    public String validateToken(String token) {

        logger.info("[validateToken] - Inicio - Validando Token");

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            logger.info("[validateToken] - Fim - Token válidado");

            return JWT.require(algorithm)
                    .withIssuer("agendamento-veterinario-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {

            logger.error("[validateToken] - Fim - Falha ao validar token: ", exception);
            return "";
        }
    }

    private Instant genExpirationDate() {
        return Instant.now().plusMillis(expiration);
    }

}
