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

import java.util.List;

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
    public Animal recuperar(Long id) {

        logger.info("[recuperar] - Buscando animal com id = {}", id);

        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Animal com id " + id + " não encontrado"));

        logger.info("[recuperar] - Animal com id = {} encontrado", id);

        return animal;
    }

    /**
     * Lista todos os animais com paginação.
     *
     * @param pageable Dados de paginação.
     * @return Page com animais.
     */
    public Page<Animal> listar(Pageable pageable) {

        logger.info("[listar] - Listando animais página = {}, tamanho = {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Animal> animais = animalRepository.findAll(pageable);

        logger.info("[listar] - Encontrados {} animais", animais.getTotalElements());

        return animais;
    }

    /**
     * Lista animais filtrando pelo ID do dono com paginação.
     *
     * @param idDono ID do dono do animal.
     * @param pageable Dados de paginação.
     * @return Page com animais do dono.
     * @throws EntityNotFoundException caso o dono não seja encontrado.
     */
    public Page<Animal> listarByDonoId(Long idDono, Pageable pageable) {

        logger.info("[listarByDonoId] - Listando animais para dono id = {}, página = {}, tamanho = {}", idDono, pageable.getPageNumber(), pageable.getPageSize());

        if(!usuarioRepository.existsById(idDono)) {
            throw new EntityNotFoundException("Dono com id " + idDono + " não encontrado");
        }

        Page<Animal> animais = animalRepository.findByUsuarioId(idDono, pageable);

        logger.info("[listarByDonoId] - Encontrados {} animais para o dono com id = {}", animais.getTotalElements(), idDono);

        return animais;
    }

    /**
     * Salva um novo animal no banco de dados.
     *
     * @param animal Objeto animal a ser salvo.
     * @return Animal salvo.
     * @throws IllegalArgumentException se dados obrigatórios estiverem ausentes ou inválidos.
     */
    public Animal salvar(Animal animal) {

        logger.info("[salvar] - Iniciando salvamento de novo animal");

        try {

            validarAnimal(animal);

            Animal animalSalvo = animalRepository.save(animal);

            // emailSender.enviarInformacaoCadastroAnimalEmail(animal, false);

            logger.info("[salvar] - Animal salvo com id = {}", animalSalvo.getId());

            return animalSalvo;

        } catch (IllegalArgumentException | EntityNotFoundException e) {

            logger.error("[salvar] - Erro de validação ao salvar animal: {}", e.getMessage());

            throw e;

        }

    }

    /**
     * Atualiza um animal existente.
     *
     * @param animal Objeto animal com dados atualizados.
     * @return Animal atualizado.
     * @throws EntityNotFoundException se o animal não existir.
     * @throws IllegalArgumentException se os dados para atualização forem inválidos.
     */
    public Animal atualizar(Animal animal) {

        logger.info("[atualizar] - Atualizando animal id = {}", animal.getId());

        if (animal.getId() == null || !animalRepository.existsById(animal.getId())) {

            logger.error("[atualizar] - Animal não encontrado para id = {}", animal.getId());

            throw new EntityNotFoundException("Animal não encontrado para atualização.");
        }

        try {

            validarAnimal(animal);

            Animal animalAtualizado = animalRepository.save(animal);

            // emailSender.enviarInformacaoCadastroAnimalEmail(animal, true);

            logger.info("[atualizar] - Animal atualizado com sucesso id = {}", animalAtualizado.getId());

            return animalAtualizado;

        } catch (IllegalArgumentException | EntityNotFoundException e) {

            logger.error("[atualizar] - Erro de validação ao atualizar animal id = {}: {}", animal.getId(), e.getMessage());

            throw e;
        }
    }

    /**
     * Deleta um animal pelo seu ID.
     *
     * @param id ID do animal a ser deletado.
     * @throws EntityNotFoundException se o animal não existir.
     */
    public void deletar(Long id) {

        logger.info("[deletar] - Deletando animal id = {}", id);

        if (!animalRepository.existsById(id)) {

            logger.error("[deletar] - Animal não encontrado para id = {}", id);

            throw new EntityNotFoundException("Animal não encontrado para exclusão.");
        }

        animalRepository.deleteById(id);

        logger.info("[deletar] - Animal deletado com sucesso id = {}", id);
    }

    /**
     * Valida os dados do animal.
     *
     * @param animal Animal a validar.
     * @throws IllegalArgumentException se validações de dados falharem.
     * @throws EntityNotFoundException se entidades associadas (dono, raça, sexo) não existirem.
     */
    private void validarAnimal(Animal animal) {

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

        // Valida a existência das entidades associadas no banco de dados
        if (!usuarioRepository.existsById(animal.getDono().getId())) {
            throw new EntityNotFoundException("Dono com id " + animal.getDono().getId() + " não foi encontrado.");
        }
        if (!racaRepository.existsById(animal.getRaca().getId())) {
            throw new EntityNotFoundException("Raça com id " + animal.getRaca().getId() + " não foi encontrada.");
        }
        if (!sexoRepository.existsById(animal.getSexo().getId())) {
            throw new EntityNotFoundException("Sexo com id " + animal.getSexo().getId() + " não foi encontrado.");
        }
    }

    /**
     * Lista todos os sexos dos animais
     *
     * @return List com os sexos dos animais.
     */
    public List<Sexo> listarSexos() {

        logger.info("[listarSexos] - Listando sexo dos animais");

        List<Sexo> sexos = sexoRepository.findAll();

        logger.info("[listarSexos] - Encontrados {} sexos", sexos.size());

        return sexos;
    }

    /**
     * Lista as raças da especie com determinado ID
     *
     * @param idEspecie ID da especie para buscar as raças.
     *
     * @return List com as raças dos animais.
     */
    public List<Raca> listarRacasByIdEspecie(Integer idEspecie) {

        logger.info("[listarRacas] - Listando raças da especie com id = {}", idEspecie);

        List<Raca> racas = racaRepository.findByEspecie(idEspecie);

        logger.info("[listarRacas] - Encontrados {} raças", racas.size());

        return racas;
    }

    /**
     * Lista todos as especies dos animais
     *
     * @return List com as especies dos animais.
     */
    public List<Especie> listarEspecies() {

        logger.info("[listarEspecies] - Listando sexo dos animais");

        List<Especie> especies = especieRepository.findAll();

        logger.info("[listarEspecies] - Encontrados {} especies", especies.size());

        return especies;
    }

}
