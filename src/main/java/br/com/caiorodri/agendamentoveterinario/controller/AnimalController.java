package br.com.caiorodri.agendamentoveterinario.controller;

import br.com.caiorodri.agendamentoveterinario.dto.EspecieDTO;
import br.com.caiorodri.agendamentoveterinario.dto.RacaDTO;
import br.com.caiorodri.agendamentoveterinario.dto.SexoDTO;
import br.com.caiorodri.agendamentoveterinario.model.Especie;
import br.com.caiorodri.agendamentoveterinario.model.Raca;
import br.com.caiorodri.agendamentoveterinario.model.Sexo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import br.com.caiorodri.agendamentoveterinario.dto.AnimalDTO;
import br.com.caiorodri.agendamentoveterinario.mapper.Mapper;
import br.com.caiorodri.agendamentoveterinario.model.Animal;
import br.com.caiorodri.agendamentoveterinario.service.AnimalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;


@RestController
@RequestMapping("/animais")
@Tag(name = "Animais", description = "Endpoints para gerenciamento de animais")
public class AnimalController {

    @Autowired
    private AnimalService animalService;

    @Autowired
    private Mapper mapper;

    final static Logger logger = LoggerFactory.getLogger(AnimalController.class);

    @Operation(
            summary = "Listar animais",
            description = "Retorna uma lista paginada de todos os animais cadastrados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animais listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping
    public ResponseEntity<Page<AnimalDTO>> listar(
            @Parameter(description = "Número da página (inicia em 0)", required = true, example = "0") @RequestParam("pagina") int pagina,
            @Parameter(description = "Quantidade de itens por página", required = true, example = "10") @RequestParam("quantidadeItens") int quantidadeItens) {

        logger.info("[listar] - Início");

        Pageable pageable = PageRequest.of(pagina, quantidadeItens);

        Page<Animal> animais = animalService.listar(pageable);

        Page<AnimalDTO> animaisDto = new PageImpl<>(mapper.animalListToDtoList(animais.getContent()), pageable, animais.getTotalElements());

        logger.info("[listar] - Fim");

        return new ResponseEntity<>(animaisDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Buscar animal por ID",
            description = "Recupera um animal com base no ID informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animal encontrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AnimalDTO> recuperar(
            @Parameter(description = "ID do animal a ser buscado", required = true, example = "1") @PathVariable Long id) {

        logger.info("[recuperar] - Início");

        Animal animal = animalService.recuperar(id);

        AnimalDTO animalDto = mapper.animalToDto(animal);

        logger.info("[recuperar] - Fim");

        return new ResponseEntity<>(animalDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar animais por dono",
            description = "Retorna uma lista paginada de animais associados ao ID do dono informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animais listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Dono não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/dono/{idDono}")
    public ResponseEntity<Page<AnimalDTO>> listarByDono(
            @Parameter(description = "ID do dono (usuário) dos animais", required = true, example = "1") @PathVariable Long idDono,
            @Parameter(description = "Número da página (inicia em 0)", required = true, example = "0") @RequestParam("pagina") int pagina,
            @Parameter(description = "Quantidade de itens por página", required = true, example = "10") @RequestParam("quantidadeItens") int quantidadeItens) {

        logger.info("[listarByDono] - Início");

        Pageable pageable = PageRequest.of(pagina, quantidadeItens);

        Page<Animal> animais = animalService.listarByDonoId(idDono, pageable);

        Page<AnimalDTO> animaisDto = new PageImpl<>(mapper.animalListToDtoList(animais.getContent()), pageable, animais.getTotalElements());

        logger.info("[listarByDono] - Fim");

        return new ResponseEntity<>(animaisDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Cadastrar novo animal",
            description = "Cria um novo animal no sistema com base nos dados informados.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto JSON contendo os dados do novo animal.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Animal.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Animal criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para o cadastro (ex: campos obrigatórios ausentes)"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Dono, raça ou sexo associado não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PostMapping
    public ResponseEntity<AnimalDTO> salvar(@RequestBody Animal animal){

        logger.info("[salvar] - Início");

        Animal animalSalvo = animalService.salvar(animal);

        AnimalDTO animalDto = mapper.animalToDto(animalSalvo);

        logger.info("[salvar] - Fim");

        return new ResponseEntity<>(animalDto, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Atualizar animal",
            description = "Atualiza os dados de um animal já existente.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto JSON contendo os dados do animal a ser atualizado, incluindo seu ID.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Animal.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animal atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Animal, dono, raça ou sexo associado não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PutMapping
    public ResponseEntity<AnimalDTO> atualizar(@RequestBody Animal animal){

        logger.info("[atualizar] - Início");

        Animal animalAtualizado = animalService.atualizar(animal);

        AnimalDTO animalDto = mapper.animalToDto(animalAtualizado);

        logger.info("[atualizar] - Fim");

        return new ResponseEntity<>(animalDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Excluir animal",
            description = "Remove um animal existente do sistema. (Requer perfil: ADMINISTRADOR)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Animal excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Não é possível excluir, pois o animal possui agendamentos."),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do animal a ser excluído", required = true, example = "1") @PathVariable Long id) {

        logger.info("[deletar] - Início");

        animalService.deletar(id);

        logger.info("[deletar] - Fim");

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Listar todas as espécies",
            description = "Retorna uma lista de todas as espécies de animais disponíveis para cadastro (ex: Cão, Gato)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Espécies listadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/especies")
    public ResponseEntity<List<EspecieDTO>> listarEspecies() {

        logger.info("[listarEspecies] - Início");

        List<Especie> especies = animalService.listarEspecies();

        List<EspecieDTO> especiesDTO = mapper.especieListToDtoList(especies);

        logger.info("[listarEspecies] - Fim");

        return new ResponseEntity<>(especiesDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar raças por espécie",
            description = "Retorna uma lista de raças com base no ID da espécie informada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Raças listadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/racas/especie/{idEspecie}")
    public ResponseEntity<List<RacaDTO>> listarRacasPorEspecie(
            @Parameter(description = "ID da espécie para filtrar as raças", required = true, example = "1") @PathVariable Integer idEspecie) {

        logger.info("[listarRacasPorEspecie] - Início");

        List<Raca> racas = animalService.listarRacasByIdEspecie(idEspecie);

        List<RacaDTO> racasDTO = mapper.racaListToDtoList(racas);

        logger.info("[listarRacasPorEspecie] - Fim");

        return new ResponseEntity<>(racasDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar todos os sexos",
            description = "Retorna uma lista de todos os sexos de animais disponíveis para cadastro (ex: Macho, Fêmea)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sexos listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/sexos")
    public ResponseEntity<List<SexoDTO>> listarSexos() {

        logger.info("[listarSexos] - Início");

        List<Sexo> sexos = animalService.listarSexos();

        List<SexoDTO> sexosDTO = mapper.sexoListToDtoList(sexos);

        logger.info("[listarSexos] - Fim");

        return new ResponseEntity<>(sexosDTO, HttpStatus.OK);
    }

}