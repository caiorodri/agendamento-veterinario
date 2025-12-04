package br.com.caiorodri.agendamentoveterinario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@SpringBootApplication
@EnableScheduling
public class AgendamentoVeterinarioApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgendamentoVeterinarioApplication.class, args);
	}

}
