package br.com.caiorodri.agendamentoveterinario.e2e;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Order(15)
@DisplayName("Infraestrutura - Aquecimento da JVM")
public class AquecimentoE2ETest extends BaseE2ETest {

    @Test
    @DisplayName("Aquecimento da JVM - Absorvendo o Cold Start Web e Banco")
    void deveAquecerOContextoDoSpringBoot() {

        System.out.println("Iniciando o aquecimento forçado do Tomcat, Security e Hibernate...");

        String jsonFalso = "{ \"email\": \"aquecer@teste.com\", \"senha\": \"123\" }";

        given()
                .contentType(ContentType.JSON)
                .body(jsonFalso)
                .when()
                .post("/usuarios/autenticar");

        System.out.println("Aquecimento concluído!");
        assertTrue(true);
    }
}