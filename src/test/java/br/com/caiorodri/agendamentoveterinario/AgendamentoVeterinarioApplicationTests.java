package br.com.caiorodri.agendamentoveterinario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Order(1)
@DisplayName("Infraestrutura - Aquecimento do Contexto do Spring")
class AgendamentoVeterinarioApplicationTests {

	@Test
	void contextLoads() {
	}

}
