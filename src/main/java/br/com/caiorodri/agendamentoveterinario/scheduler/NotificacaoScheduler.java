package br.com.caiorodri.agendamentoveterinario.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import br.com.caiorodri.agendamentoveterinario.email.EmailSender;
import br.com.caiorodri.agendamentoveterinario.model.Agendamento;
import br.com.caiorodri.agendamentoveterinario.model.Animal;
import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoRepository;
import br.com.caiorodri.agendamentoveterinario.repository.AnimalRepository;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;

@Service
public class NotificacaoScheduler {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailSender emailService;

    @Scheduled(cron = "0 0 10 * * MON")
    private void verificarAnimaisSemConsultaRecente() {

        List<Animal> animais = animalRepository.findAll();

        for(Animal animal : animais) {

            Agendamento ultimaConsulta = agendamentoRepository.findUltimaConsultaByAnimal(animal.getId());

            if(ultimaConsulta != null) {

                if(!animal.getDono().isEmailRealizarConsultaRecebido() && animal.getDono().isReceberEmail()) {

                    LocalDate dataUltimaConsulta = ultimaConsulta.getDataAgendamentoInicio().toLocalDate();
                    LocalDate hoje = LocalDate.now();

                    if (dataUltimaConsulta.isBefore(hoje.minusMonths(6))) {

                        emailService.enviarInformacaoRealizarConsultaEmail(animal, ultimaConsulta);

                        animal.getDono().setEmailRealizarConsultaRecebido(true);

                        usuarioRepository.save(animal.getDono());

                    }


                }

            }

        }

    }

}