package br.com.caiorodri.agendamentoveterinario.config;

import br.com.caiorodri.agendamentoveterinario.email.EmailSender;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public EmailSender emailSender(){
        return Mockito.mock(EmailSender.class);
    }

}
