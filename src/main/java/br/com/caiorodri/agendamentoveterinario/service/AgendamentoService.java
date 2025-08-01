package br.com.caiorodri.agendamentoveterinario.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.caiorodri.agendamentoveterinario.model.Agendamento;
import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoRepository;
import br.com.caiorodri.agendamentoveterinario.repository.AnimalRepository;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class AgendamentoService {

	@Autowired
	private AgendamentoRepository agendamentoRepository;
	
	@Autowired
	private AnimalRepository animalRepository;
	
	@Autowired
	private UsuarioRepository usuarioRepository;

	final static Logger logger = LoggerFactory.getLogger(AgendamentoService.class);
	
    /**
     * Recupera um agendamento pelo seu ID.
     *
     * @param id ID do agendamento.
     * @return Agendamento encontrado ou null.
     * @throws EntityNotFoundException caso não exista agendamento com o id enviado
     */
	public Agendamento recuperar(Long id) {
		
		logger.info("[recuperar] - Buscando agendamento com id = {}", id);
		
		Agendamento agendamento = agendamentoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Agendamento com id " + id + " não encontrado"));
		
        logger.info("[recuperar] - Agendamento com id = {} encontrado", id);
        
		return agendamento;
		
	}
	
    /**
     * Lista todos os agendamentos com paginação.
     *
     * @param pageable Dados de paginação.
     * @return Page com agendamentos.
     */
	public Page<Agendamento> listar(Pageable pageable) {
		
		logger.info("[listar] - Listando agendamentos página = {}, tamanho = {}", pageable.getPageNumber(), pageable.getPageSize());

		Page<Agendamento> agendamentos = agendamentoRepository.findAll(pageable);
		
		logger.info("[listar] - Encontrados {} agendamentos", agendamentos.getTotalElements());
		
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
		
		logger.info("[listarByAnimalId] - Listando agendamentos para animal id = {}, página = {}, tamanho = {}", idAnimal, pageable.getPageNumber(), pageable.getPageSize());
		
		if(!animalRepository.existsById(idAnimal)) {
			
			throw new EntityNotFoundException("Animal com id " + idAnimal + " não encontrado");
			
		}
		
		Page<Agendamento> agendamentos = agendamentoRepository.findByAnimal(idAnimal, pageable);
		
		logger.info("[listarByAnimalId] - Encontrados {} agendamentos para o animal com id = " + idAnimal, agendamentos.getTotalElements());
		
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
		
		logger.info("[listarByUsuarioId] - Listando agendamentos para usuario id = {}, página = {}, tamanho = {}", idUsuario, pageable.getPageNumber(), pageable.getPageSize());
		
		if(!usuarioRepository.existsById(idUsuario)) {
			
			throw new EntityNotFoundException("Usuário com id " + idUsuario + " não encontrado");
			
		}
		
		Page<Agendamento> agendamentos = agendamentoRepository.findByUsuario(idUsuario, pageable);
		
		logger.info("[listarByUsuarioId] - Encontrados {} agendamentos para o usuario com id = " + idUsuario, agendamentos.getTotalElements());
		
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
		
		logger.info("[salvar] - Iniciando salvamento de agendamento");
		
		try {
				
	        validarAgendamento(agendamento);
			
			Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);

            logger.info("[salvar] - Agendamento salvo com id = {}", agendamentoSalvo.getId());
			
            return agendamentoSalvo;
			
		} catch (IllegalArgumentException e) {

            logger.error("[salvar] - Erro ao salvar agendamento: {}", e.getMessage());
            
            throw new IllegalArgumentException("Erro ao salvar agendamento: " + e.getMessage());
			
		} catch (Exception e) {

            logger.error("[salvar] - Erro ao salvar agendamento: {}", e.getMessage());
            
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
    public Agendamento atualizar(Agendamento agendamento) {
    	
        logger.info("[atualizar] - Atualizando agendamento id = {}", agendamento.getId());
        
        if (agendamento.getId() == null || !agendamentoRepository.existsById(agendamento.getId())) {
            
        	logger.error("[atualizar] - Agendamento não encontrado para id = {}", agendamento.getId());
            
        	throw new EntityNotFoundException("Agendamento não encontrado para atualização.");
         
        }
        
        try {

            validarAgendamento(agendamento);
            
        	Agendamento agendamentoAtualizado = agendamentoRepository.save(agendamento);
            
        	logger.info("[atualizar] - Agendamento atualizado com sucesso id = {}", agendamentoAtualizado.getId());
        
            return agendamentoAtualizado;
        
        } catch (IllegalArgumentException e) {
     
        	logger.error("[atualizar] - Erro ao atualizar agendamento id = {}", agendamento.getId(), e);
        
        	throw new IllegalArgumentException("Erro ao atualizar agendamento: " + e.getMessage());
        
        } catch (Exception e) {
        	
           	logger.error("[atualizar] - Erro ao atualizar agendamento id = {}", agendamento.getId(), e);
            
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
       
    	logger.info("[deletar] - Deletando agendamento id = {}", id);
        
    	if (!agendamentoRepository.existsById(id)) {
        
    		logger.error("[deletar] - Agendamento não encontrado para id = {}", id);
        
    		throw new EntityNotFoundException("Agendamento não encontrado para exclusão.");
        
    	}
        
    	agendamentoRepository.deleteById(id);
        
    	logger.info("[deletar] - Agendamento deletado com sucesso id = {}", id);
    }

	
    /**
     * Valida os dados do agendamento.
     *
     * @param agendamento Agendamento a validar.
     * @throws IllegalArgumentException se validações falharem.
     */
    private void validarAgendamento(Agendamento agendamento) {
    	
        if (agendamento == null) {
        
        	throw new IllegalArgumentException("Agendamento não pode ser nulo.");
        
        }
        
        if (agendamento.getDataAgendamentoInicio() == null || agendamento.getDataAgendamentoFinal() == null) {
        
        	throw new IllegalArgumentException("Data e hora de início e fim do agendamento são obrigatórios.");
        
        }
        
        if (agendamento.getAnimal() == null || agendamento.getAnimal().getId() == null) {
        
        	throw new IllegalArgumentException("Animal do agendamento é obrigatório.");
        
        }
        
        if (agendamento.getRecepcionista() == null || agendamento.getRecepcionista().getId() == null) {
        
        	throw new IllegalArgumentException("Recepcionista do agendamento é obrigatório.");
        
        }
        
        verificarConflitoHorario(agendamento);
    }
    
    private void verificarConflitoHorario(Agendamento agendamento) {
    	
        if(agendamentoRepository.existeConflitoDeHorario(agendamento.getDataAgendamentoInicio(), agendamento.getDataAgendamentoFinal())) {
        	
        	if(!(agendamento.getId() != null && agendamento.getId() > 0)) {
        		
        		throw new IllegalArgumentException("Já existe um agendamento para esse horário");
        		
        	}
        	
    		List<Agendamento> agendamentosConflitantes = agendamentoRepository.findByHorario(agendamento.getDataAgendamentoInicio(), agendamento.getDataAgendamentoFinal());
    		
    		for(Agendamento agendamentoConflitante : agendamentosConflitantes) {
    			
    			if(!agendamentoConflitante.getId().equals(agendamento.getId())) {
    				
    	        	throw new IllegalArgumentException("Já existe um agendamento para esse horário");
    				
    			}
    			
    		}
        	
        	
        }
    	
    }
	
}
