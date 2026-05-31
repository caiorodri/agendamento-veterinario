package br.com.caiorodri.agendamentoveterinario;

import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Order(2)
@DisplayName("Infraestrutura - Preparação do Ambiente de Testes")
class AquecimentoJVMTest {

    @Mock
    private AgendamentoRepository mockRepository;

    @Test
    @DisplayName("Inicialização do Mockito")
    void aquecerJVM() {
        when(mockRepository.existsById(anyLong())).thenReturn(true);
        mockRepository.existsById(1L);

        assertTrue(true);
    }

}
