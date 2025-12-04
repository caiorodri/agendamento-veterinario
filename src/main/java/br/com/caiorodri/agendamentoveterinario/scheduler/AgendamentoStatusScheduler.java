package br.com.caiorodri.agendamentoveterinario.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import br.com.caiorodri.agendamentoveterinario.model.Agendamento;
import br.com.caiorodri.agendamentoveterinario.model.AgendamentoStatus;
import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoRepository;
import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoStatusRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class AgendamentoStatusScheduler {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private AgendamentoStatusRepository agendamentoStatusRepository;

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoStatusScheduler.class);

    private static final Integer ID_STATUS_ABERTO = 1;
    private static final Integer ID_STATUS_PERDIDO = 4;

    @Scheduled(cron = "0 0/30 * * * *", zone = "America/Sao_Paulo")
    @Transactional
    public void marcarAgendamentosComoPerdido() {

        logger.info("Iniciando verificação de agendamentos expirados...");

        LocalDateTime dataLimite = LocalDateTime.now().minusHours(3);

        List<Agendamento> agendamentosExpirados = agendamentoRepository.findAgendamentosExpirados(ID_STATUS_ABERTO, dataLimite);

        if (agendamentosExpirados.isEmpty()) {
            logger.info("Nenhum agendamento expirado encontrado.");
            return;
        }

        AgendamentoStatus statusPerdido = agendamentoStatusRepository.findById(ID_STATUS_PERDIDO)
                .orElseThrow(() -> new EntityNotFoundException("Status 'Perdido' não encontrado no banco de dados."));

        for (Agendamento agendamento : agendamentosExpirados) {
            agendamento.setStatus(statusPerdido);
            logger.info("Agendamento ID {} marcado como PERDIDO. Data Inicio: {}", agendamento.getId(), agendamento.getDataAgendamentoInicio());
        }

        agendamentoRepository.saveAll(agendamentosExpirados);

        logger.info("{} agendamentos foram atualizados para 'Perdido'.", agendamentosExpirados.size());
    }
}