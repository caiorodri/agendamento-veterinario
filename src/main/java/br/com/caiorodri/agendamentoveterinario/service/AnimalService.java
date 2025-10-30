package br.com.caiorodri.agendamentoveterinario.service;

import br.com.caiorodri.agendamentoveterinario.email.EmailSender;
import br.com.caiorodri.agendamentoveterinario.model.Especie;
import br.com.caiorodri.agendamentoveterinario.model.Raca;
import br.com.caiorodri.agendamentoveterinario.model.Sexo;
import br.com.caiorodri.agendamentoveterinario.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.caiorodri.agendamentoveterinario.model.Animal;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AnimalService {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RacaRepository racaRepository;

    @Autowired
    private EspecieRepository especieRepository;

    @Autowired
    private SexoRepository sexoRepository;

    // @Autowired
    // private EmailSender emailSender;

    final static Logger logger = LoggerFactory.getLogger(AnimalService.class);

    /**
     * Recupera um animal pelo seu ID.
     *
     * @param id ID do animal.
     * @return Animal encontrado.
     * @throws EntityNotFoundException caso não exista animal com o id enviado.
     */
    @Transactional(readOnly = true)
    public Animal recuperar(Long id) {

        logger.info("[recuperar] - Inicio - Buscando animal com id = {}", id);

        try {

            Animal animal = animalRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Animal com id " + id + " não encontrado"));

            logger.info("[recuperar] - Fim - Animal com id = {} encontrado", id);

            return animal;

        } catch (EntityNotFoundException e) {

            logger.error("[recuperar] - Fim - Erro: {}", e.getMessage());
            throw e;

        }
    }

    /**
     * Lista todos os animais com paginação.
     *
     * @param pageable Dados de paginação.
     * @return Page com animais.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar os animais.
     */
    @Transactional(readOnly = true)
    public Page<Animal> listar(Pageable pageable) {

        logger.info("[listar] - Inicio - Listando animais: página = {}, tamanho = {}", pageable.getPageNumber(), pageable.getPageSize());

        try {

            Page<Animal> animais = animalRepository.findAll(pageable);

            logger.info("[listar] - Fim - Encontrados {} animais no total.", animais.getTotalElements());

            return animais;

        } catch (Exception e) {

            logger.error("[listar] - Fim - Erro inesperado ao listar animais: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar animais", e);

        }
    }

    /**
     * Lista animais filtrando pelo ID do dono com paginação.
     *
     * @param idDono ID do dono do animal.
     * @param pageable Dados de paginação.
     * @return Page com animais do dono.
     * @throws EntityNotFoundException caso o dono não seja encontrado.
     */
    @Transactional(readOnly = true)
    public Page<Animal> listarByDonoId(Long idDono, Pageable pageable) {

        logger.info("[listarByDonoId] - Inicio - Listando animais para dono id = {}", idDono);

        try {

            if(!usuarioRepository.existsById(idDono)) {

                throw new EntityNotFoundException("Dono com id " + idDono + " não encontrado");

            }

            Page<Animal> animais = animalRepository.findByUsuarioId(idDono, pageable);

            logger.info("[listarByDonoId] - Fim - Encontrados {} animais para o dono com id = {}", animais.getTotalElements(), idDono);

            return animais;

        } catch (EntityNotFoundException e) {

            logger.error("[listarByDonoId] - Fim - Erro: {}", e.getMessage());
            throw e;

        }
    }

    /**
     * Salva um novo animal no banco de dados.
     *
     * @param animal Objeto animal a ser salvo.
     * @return Animal salvo.
     * @throws IllegalArgumentException se dados obrigatórios estiverem ausentes ou inválidos.
     * @throws EntityNotFoundException se o dono, raça ou sexo associado não for encontrado.
     * @throws RuntimeException se ocorrer um erro inesperado ao salvar.
     */
    @Transactional
    public Animal salvar(Animal animal) {

        logger.info("[salvar] - Inicio - Tentativa de salvar um novo animal.");

        try {

            validarAnimal(animal);

            Animal animalSalvo = animalRepository.save(animal);

            // emailSender.enviarInformacaoCadastroAnimalEmail(animal, false);

            logger.info("[salvar] - Fim - Animal salvo com sucesso com o id = {}", animalSalvo.getId());

            return animalSalvo;

        } catch (IllegalArgumentException | EntityNotFoundException e) {

            logger.error("[salvar] - Fim - Erro de validação ao salvar animal: {}", e.getMessage());
            throw e;

        } catch (Exception e) {

            logger.error("[salvar] - Fim - Erro inesperado ao salvar animal: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar animal", e);

        }
    }

    /**
     * Atualiza um animal existente.
     *
     * @param animal Objeto animal com dados atualizados.
     * @return Animal atualizado.
     * @throws EntityNotFoundException se o animal, dono, raça ou sexo não forem encontrados.
     * @throws IllegalArgumentException se os dados para atualização forem inválidos.
     * @throws RuntimeException se ocorrer um erro inesperado ao atualizar.
     */
    @Transactional
    public Animal atualizar(Animal animal) {

        logger.info("[atualizar] - Inicio - Tentativa de atualizar o animal com id = {}", animal.getId());

        try {

            if (animal.getId() == null || !animalRepository.existsById(animal.getId())) {

                throw new EntityNotFoundException("Animal com id " + animal.getId() + " não encontrado para atualização.");

            }

            validarAnimal(animal);

            Animal animalAtualizado = animalRepository.save(animal);

            // emailSender.enviarInformacaoCadastroAnimalEmail(animal, true);

            logger.info("[atualizar] - Fim - Animal com id = {} atualizado com sucesso.", animalAtualizado.getId());

            return animalAtualizado;

        } catch (IllegalArgumentException | EntityNotFoundException e) {

            logger.error("[atualizar] - Fim - Erro de validação ao atualizar animal com id = {}: {}", animal.getId(), e.getMessage());
            throw e;

        } catch (Exception e) {

            logger.error("[atualizar] - Fim - Erro inesperado ao atualizar animal com id = {}: {}", animal.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar animal", e);

        }
    }

    /**
     * Deleta um animal pelo seu ID.
     *
     * @param id ID do animal a ser deletado.
     * @throws EntityNotFoundException se o animal não existir.
     * @throws RuntimeException se ocorrer um erro inesperado ao deletar.
     */
    @Transactional
    public void deletar(Long id) {

        logger.info("[deletar] - Inicio - Tentativa de deletar o animal com id = {}", id);

        try {

            if (!animalRepository.existsById(id)) {

                throw new EntityNotFoundException("Animal com id " + id + " não encontrado para exclusão.");
            }

            Animal animal = animalRepository.findById(id).get();

            if(animal.getAgendamentos().size() > 0){

                throw new Exception("Não pode deletar o animal com id " + id + " pois ele possui agendamentos associados");

            }

            animalRepository.deleteById(id);

            logger.info("[deletar] - Fim - Animal com id = {} deletado com sucesso.", id);

        } catch (EntityNotFoundException e) {

            logger.error("[deletar] - Fim - Erro: {}", e.getMessage());
            throw e;

        } catch (Exception e) {

            logger.error("[deletar] - Fim - Erro inesperado ao deletar animal com id = {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao deletar animal", e);

        }
    }

    /**
     * Valida os dados do animal.
     *
     * @param animal Animal a validar.
     * @throws IllegalArgumentException se validações de dados falharem.
     * @throws EntityNotFoundException se entidades associadas (dono, raça, sexo) não existirem.
     */
    private void validarAnimal(Animal animal) {

        logger.info("[validarAnimal] - Inicio - Validando dados do animal.");

        if (animal == null) {
            throw new IllegalArgumentException("Objeto Animal não pode ser nulo.");
        }
        if (animal.getNome() == null || animal.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do animal é obrigatório.");
        }
        if (animal.getDono() == null || animal.getDono().getId() == null) {
            throw new IllegalArgumentException("O animal deve estar associado a um dono.");
        }
        if (animal.getRaca() == null || animal.getRaca().getId() == null) {
            throw new IllegalArgumentException("A raça do animal é obrigatória.");
        }
        if (animal.getSexo() == null || animal.getSexo().getId() == null) {
            throw new IllegalArgumentException("O sexo do animal é obrigatório.");
        }

        if (!usuarioRepository.existsById(animal.getDono().getId())) {
            throw new EntityNotFoundException("Dono com id " + animal.getDono().getId() + " não foi encontrado.");
        }
        if (!racaRepository.existsById(animal.getRaca().getId())) {
            throw new EntityNotFoundException("Raça com id " + animal.getRaca().getId() + " não foi encontrada.");
        }
        if (!sexoRepository.existsById(animal.getSexo().getId())) {
            throw new EntityNotFoundException("Sexo com id " + animal.getSexo().getId() + " não foi encontrado.");
        }

        logger.info("[validarAnimal] - Fim - Validação concluída com sucesso.");
    }

    /**
     * Lista todos os sexos dos animais.
     *
     * @return List com os sexos dos animais.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar os sexos.
     */
    @Transactional(readOnly = true)
    public List<Sexo> listarSexos() {

        logger.info("[listarSexos] - Inicio - Buscando todos os sexos.");

        try {

            List<Sexo> sexos = sexoRepository.findAll();

            logger.info("[listarSexos] - Fim - Busca concluída. Encontrados {} sexos.", sexos.size());

            return sexos;

        } catch (Exception e) {

            logger.error("[listarSexos] - Fim - Erro inesperado ao listar sexos: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar sexos", e);

        }
    }

    /**
     * Lista as raças da espécie com determinado ID.
     *
     * @param idEspecie ID da especie para buscar as raças.
     * @return List com as raças dos animais.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar as raças.
     */
    @Transactional(readOnly = true)
    public List<Raca> listarRacasByIdEspecie(Integer idEspecie) {

        logger.info("[listarRacasByIdEspecie] - Inicio - Listando raças da especie com id = {}", idEspecie);

        try {

            List<Raca> racas = racaRepository.findByEspecie(idEspecie);

            logger.info("[listarRacasByIdEspecie] - Fim - Encontrados {} raças para a especie com id = {}.", racas.size(), idEspecie);

            return racas;

        } catch (Exception e) {

            logger.error("[listarRacasByIdEspecie] - Fim - Erro inesperado ao listar raças: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar raças", e);

        }
    }

    /**
     * Lista todas as raças de animais.
     *
     * @return List com as raças dos animais.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar as raças.
     */
    @Transactional(readOnly = true)
    public List<Raca> listarRacas() {

        logger.info("[listarRacas] - Inicio - Listando raças");

        try {

            List<Raca> racas = racaRepository.findAll();

            logger.info("[listarRacas] - Fim - Encontrados {} raças.", racas.size());

            return racas;

        } catch (Exception e) {

            logger.error("[listarRacas] - Fim - Erro inesperado ao listar raças: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar raças", e);

        }
    }


    /**
     * Lista todas as espécies dos animais.
     *
     * @return List com as espécies dos animais.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar as espécies.
     */
    public List<Especie> listarEspecies() {

        logger.info("[listarEspecies] - Inicio - Buscando todas as espécies.");

        try {

            List<Especie> especies = especieRepository.findAll();

            logger.info("[listarEspecies] - Fim - Busca concluída. Encontradas {} espécies.", especies.size());

            return especies;

        } catch (Exception e) {

            logger.error("[listarEspecies] - Fim - Erro inesperado ao listar espécies: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar espécies", e);

        }
    }
}