package br.com.caiorodri.agendamentoveterinario.service;

import java.time.LocalDateTime;
import java.util.List;

import br.com.caiorodri.agendamentoveterinario.email.EmailSender;
import br.com.caiorodri.agendamentoveterinario.model.AgendamentoStatus;
import br.com.caiorodri.agendamentoveterinario.model.AgendamentoTipo;
import br.com.caiorodri.agendamentoveterinario.model.Status;
import br.com.caiorodri.agendamentoveterinario.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.caiorodri.agendamentoveterinario.model.Agendamento;
import jakarta.persistence.EntityNotFoundException;

@Service
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private AgendamentoStatusRepository agendamentoStatusRepository;

    @Autowired
    private AgendamentoTipoRepository agendamentoTipoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // @Autowired
    // private EmailSender emailSender;

    final static Logger logger = LoggerFactory.getLogger(AgendamentoService.class);

    /**
     * Recupera um agendamento pelo seu ID.
     *
     * @param id ID do agendamento.
     * @return Agendamento encontrado ou null.
     * @throws EntityNotFoundException caso não exista agendamento com o id enviado
     */
    public Agendamento recuperar(Long id) {

        try {

            logger.info("[recuperar] - Inicio - Buscando agendamento com id = {}", id);
            Agendamento agendamento = agendamentoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Agendamento com id " + id + " não encontrado"));
            logger.info("[recuperar] - Fim - Agendamento com id = {} encontrado", id);
            return agendamento;

        } catch (EntityNotFoundException e){

            logger.error("[recuperar] - Fim - Erro ao tentar encontrar agendamento com id = {}: {}", id, e.getMessage());
            throw e;

        }
    }

    /**
     * Lista todos os agendamentos com paginação.
     *
     * @param pageable Dados de paginação.
     * @return Page com agendamentos.
     */
    public Page<Agendamento> listar(Pageable pageable) {

        logger.info("[listar] - Inicio - Listando agendamentos: página = {}, tamanho = {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Agendamento> agendamentos = agendamentoRepository.findAll(pageable);

        logger.info("[listar] - Fim - Listagem concluída. Encontrados {} agendamentos no total.", agendamentos.getTotalElements());

        return agendamentos;

    }

    /**
     * Lista agendamentos filtrando pelo ID do animal com paginação.
     *
     * @param idAnimal ID do animal.
     * @param pageable Dados de paginação.
     * @return Page com agendamentos do animal.
     * @throws EntityNotFoundException caso o animal não seja encontrado
     */
    public Page<Agendamento> listarByAnimalId(Long idAnimal, Pageable pageable) {

        logger.info("[listarByAnimalId] - Inicio - Buscando agendamentos para o animal com id = {}", idAnimal);

        if(!animalRepository.existsById(idAnimal)) {

            logger.error("[listarByAnimalId] - Fim - Erro: Animal com id {} não encontrado.", idAnimal);
            throw new EntityNotFoundException("Animal com id " + idAnimal + " não encontrado");

        }

        Page<Agendamento> agendamentos = agendamentoRepository.findByAnimal(idAnimal, pageable);

        logger.info("[listarByAnimalId] - Fim - Busca concluída. Encontrados {} agendamentos para o animal com id = {}", agendamentos.getTotalElements(), idAnimal);

        return agendamentos;

    }

    /**
     * Lista agendamentos filtrando pelo ID do usuário com paginação.
     *
     * @param idUsuario ID do usuário.
     * @param pageable Dados de paginação.
     * @return Page com agendamentos do usuário.
     * @throws EntityNotFoundException caso o usuário não seja encontrado
     */
    public Page<Agendamento> listarByUsuarioId(Long idUsuario, Pageable pageable) {

        logger.info("[listarByUsuarioId] - Inicio - Buscando agendamentos para o usuário com id = {}", idUsuario);

        if(!usuarioRepository.existsById(idUsuario)) {

            logger.error("[listarByUsuarioId] - Fim - Erro: Usuário com id {} não encontrado.", idUsuario);
            throw new EntityNotFoundException("Usuário com id " + idUsuario + " não encontrado");

        }

        Page<Agendamento> agendamentos = agendamentoRepository.findByUsuario(idUsuario, pageable);

        logger.info("[listarByUsuarioId] - Fim - Busca concluída. Encontrados {} agendamentos para o usuário com id = {}", agendamentos.getTotalElements(), idUsuario);

        return agendamentos;

    }

    /**
     * Salva um novo agendamento no banco de dados.
     *
     * @param agendamento Objeto agendamento a ser salvo.
     * @return Agendamento salvo.
     * @throws IllegalArgumentException se dados obrigatórios estiverem ausentes.
     * @throws RuntimeException se ocorrer algum erro interno
     */
    public Agendamento salvar(Agendamento agendamento) {

        logger.info("[salvar] - Inicio - Tentativa de salvar um novo agendamento.");

        try {

            validarAgendamento(agendamento);

            agendamento.setDataCriacao(LocalDateTime.now());

            Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);

            //emailSender.enviarInformacaoCadastroAgendamentoEmail(agendamento, false);

            logger.info("[salvar] - Fim - Agendamento salvo com sucesso com o id = {}", agendamentoSalvo.getId());

            return agendamentoSalvo;

        } catch (IllegalArgumentException e) {

            logger.error("[salvar] - Fim - Erro de validação ao salvar agendamento: {}", e.getMessage());
            throw e;

        } catch (Exception e) {

            logger.error("[salvar] - Fim - Erro inesperado ao salvar agendamento: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar agendamento: " + e.getMessage());

        }
    }

    /**
     * Atualiza um agendamento existente.
     *
     * @param agendamento Objeto agendamento com dados atualizados.
     * @return Agendamento atualizado.
     * @throws EntityNotFoundException se o agendamento não existir.
     * @throws IllegalArgumentException se estiver algum argumento inválido
     * @throws RuntimeException se ocorrer algum erro interno
     */
    @Transactional
    public Agendamento atualizar(Agendamento agendamento) {

        logger.info("[atualizar] - Inicio - Tentativa de atualizar o agendamento com id = {}", agendamento.getId());

        if (agendamento.getId() == null || !agendamentoRepository.existsById(agendamento.getId())) {

            logger.error("[atualizar] - Fim - Erro: Agendamento com id {} não encontrado para atualização.", agendamento.getId());
            throw new EntityNotFoundException("Agendamento não encontrado para atualização.");

        }

        try {

            validarAgendamento(agendamento);

            Agendamento agendamentoSalvo = agendamentoRepository.findById(agendamento.getId()).get();

            agendamentoSalvo.setAnimal(agendamento.getAnimal());
            agendamentoSalvo.setVeterinario(agendamento.getVeterinario());
            agendamentoSalvo.setDescricao(agendamento.getDescricao());
            agendamentoSalvo.setTipo(agendamento.getTipo());
            agendamentoSalvo.setStatus(agendamento.getStatus());
            agendamentoSalvo.setDataCriacao(LocalDateTime.now());

            agendamentoRepository.saveAndFlush(agendamentoSalvo);

            Agendamento agendamentoAtualizado = agendamentoRepository.findById(agendamento.getId()).get();

            // emailSender.enviarInformacaoCadastroAgendamentoEmail(agendamento, true);

            logger.info("[atualizar] - Fim - Agendamento com id = {} atualizado com sucesso.", agendamentoAtualizado.getId());

            return agendamentoAtualizado;

        } catch (IllegalArgumentException e) {

            logger.error("[atualizar] - Fim - Erro de validação ao atualizar agendamento com id = {}: {}", agendamento.getId(), e.getMessage());
            throw e;

        } catch (Exception e) {

            logger.error("[atualizar] - Fim - Erro inesperado ao atualizar agendamento com id = {}: {}", agendamento.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar agendamento: " + e.getMessage());

        }
    }

    /**
     * Deleta um agendamento pelo seu ID.
     *
     * @param id ID do agendamento a ser deletado.
     * @throws EntityNotFoundException se o agendamento não existir.
     */
    public void deletar(Long id) {

        logger.info("[deletar] - Inicio - Tentativa de deletar o agendamento com id = {}", id);

        try {

            if (!agendamentoRepository.existsById(id)) {

                logger.error("[deletar] - Fim - Erro: Agendamento com id {} não encontrado para exclusão.", id);
                throw new EntityNotFoundException("Agendamento não encontrado para exclusão.");

            }

            agendamentoRepository.deleteById(id);

            logger.info("[deletar] - Fim - Agendamento com id = {} deletado com sucesso.", id);

        } catch(EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[deletar] - Fim - Erro inesperado ao deletar agendamento com id = {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao deletar agendamento: " + e.getMessage());
        }
    }

    /**
     * Valida os dados do agendamento.
     *
     * @param agendamento Agendamento a validar.
     * @throws IllegalArgumentException se validações falharem.
     */
    private void validarAgendamento(Agendamento agendamento) {

        logger.info("[validarAgendamento] - Inicio - Validando dados do agendamento.");

        if (agendamento == null) {

            logger.error("[validarAgendamento] - Fim - Erro de validação: O objeto agendamento não pode ser nulo.");
            throw new IllegalArgumentException("Agendamento não pode ser nulo.");

        }

        if (agendamento.getDataAgendamentoInicio() == null || agendamento.getDataAgendamentoFinal() == null) {

            logger.error("[validarAgendamento] - Fim - Erro de validação: As datas de início e fim são obrigatórias.");
            throw new IllegalArgumentException("Data e hora de início e fim do agendamento são obrigatórios.");

        }

        if (agendamento.getAnimal() == null || agendamento.getAnimal().getId() == null) {

            logger.error("[validarAgendamento] - Fim - Erro de validação: O animal é obrigatório.");
            throw new IllegalArgumentException("Animal do agendamento é obrigatório.");

        }

        if (agendamento.getRecepcionista() == null || agendamento.getRecepcionista().getId() == null) {
            logger.error("[validarAgendamento] - Fim - Erro de validação: O recepcionista é obrigatório.");
            throw new IllegalArgumentException("Recepcionista do agendamento é obrigatório.");

        }

        verificarConflitoHorario(agendamento);

        logger.info("[validarAgendamento] - Fim - Validação concluída com sucesso.");

    }

    private void verificarConflitoHorario(Agendamento agendamento) {

        logger.info("[verificarConflitoHorario] - Inicio - Verificando conflitos de horário.");

        if(agendamentoRepository.existeConflitoDeHorario(agendamento.getDataAgendamentoInicio(), agendamento.getDataAgendamentoFinal())) {

            if(agendamento.getId() == null || agendamento.getId() <= 0) {

                logger.error("[verificarConflitoHorario] - Fim - Erro: Conflito de horário detectado para um novo agendamento.");
                throw new IllegalArgumentException("Já existe um agendamento para esse horário");

            }

            List<Agendamento> agendamentosConflitantes = agendamentoRepository.findByHorario(agendamento.getDataAgendamentoInicio(), agendamento.getDataAgendamentoFinal());

            for(Agendamento agendamentoConflitante : agendamentosConflitantes) {

                if(!agendamentoConflitante.getId().equals(agendamento.getId())) {

                    logger.error("[verificarConflitoHorario] - Fim - Erro: Conflito de horário detectado com o agendamento id = {}", agendamentoConflitante.getId());
                    throw new IllegalArgumentException("Já existe um agendamento para esse horário");

                }
            }
        }

        logger.info("[verificarConflitoHorario] - Fim - Verificação de conflitos concluída, nenhum conflito encontrado.");

    }

    /**
     * Lista todos os status dos agendamentos
     *
     * @return List com os status dos agendamentos.
     */
    public List<AgendamentoStatus> listarAgendamentoStatus() {

        logger.info("[listarAgendamentoStatus] - Inicio - Buscando todos os status de agendamento.");

        List<AgendamentoStatus> listaStatus = agendamentoStatusRepository.findAll();

        logger.info("[listarAgendamentoStatus] - Fim - Busca concluída. Encontrados {} status.", listaStatus.size());

        return listaStatus;

    }

    /**
     * Lista todos os tipos dos agendamentos
     *
     * @return List com os tipos dos agendamentos.
     */
    public List<AgendamentoTipo> listarAgendamentoTipo() {

        logger.info("[listarAgendamentoTipo] - Inicio - Buscando todos os tipos de agendamento.");

        List<AgendamentoTipo> tipos = agendamentoTipoRepository.findAll();

        logger.info("[listarAgendamentoTipo] - Fim - Busca concluída. Encontrados {} tipos.", tipos.size());

        return tipos;

    }
}