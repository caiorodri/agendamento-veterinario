package br.com.caiorodri.agendamentoveterinario.service;

import br.com.caiorodri.agendamentoveterinario.dto.VeterinarioHorarioDTO;
import br.com.caiorodri.agendamentoveterinario.enums.DiaSemanaEnum;
import br.com.caiorodri.agendamentoveterinario.model.DiaSemana;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.model.VeterinarioHorario;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;
import br.com.caiorodri.agendamentoveterinario.repository.VeterinarioHorarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class VeterinarioHorarioService {

    @Autowired
    private VeterinarioHorarioRepository repository;

    final static Logger logger = LoggerFactory.getLogger(VeterinarioHorarioService.class);

    @Transactional
    public VeterinarioHorario salvar(VeterinarioHorario horario) {

        logger.info("[salvar] - Inicio");

        if (horario.getHoraInicio().isAfter(horario.getHoraFim())) {
            throw new IllegalArgumentException("A hora de início deve ser anterior à hora de fim.");
        }

        if(horario.getHoraInicio().equals(horario.getHoraFim())){
            throw new IllegalArgumentException("A hora de inicio e fim não podem ser iguais.");
        }

        logger.info("[salvar] - Fim");

        return repository.save(horario);

    }

    public List<VeterinarioHorario> listarPorVeterinario(Long idVeterinario) {

        logger.info("[listarPorVeterinario] - Inicio");

        List<VeterinarioHorario> lista = repository.findByVeterinarioIdOrderByDiaSemanaIdAscHoraInicioAsc(idVeterinario);

        logger.info("[listarPorVeterinario] - Fim");

        return lista;

    }

    @Transactional
    public void deletar(Long id) {

        logger.info("[deletar] - Inicio");

        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Horário não encontrado.");
        }
        repository.deleteById(id);

        logger.info("[deletar] - Fim");

    }

}
