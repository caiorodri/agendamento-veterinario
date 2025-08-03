package br.com.caiorodri.agendamentoveterinario.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.caiorodri.agendamentoveterinario.dto.AnimalDTO;
import br.com.caiorodri.agendamentoveterinario.mapper.Mapper;
import br.com.caiorodri.agendamentoveterinario.model.Animal;
import br.com.caiorodri.agendamentoveterinario.service.AnimalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@RestController
@RequestMapping("/animais")
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
            @ApiResponse(responseCode = "200", description = "Animais listados com sucesso")
    })
    @GetMapping
    public ResponseEntity<Page<AnimalDTO>> listar(@RequestParam("pagina") int pagina, @RequestParam("quantidadeItens") int quantidadeItens) {

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
            @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AnimalDTO> recuperar(@PathVariable Long id) {

        logger.info("[recuperar] - Início");

        Animal animal = animalService.recuperar(id);

        AnimalDTO animalDto = mapper.animalToDto(animal);

        logger.info("[recuperar] - Fim");

        return new ResponseEntity<>(animalDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar animais por dono",
            description = "Retorna uma lista paginada de animais associados ao dono informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animais listados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Dono não encontrado")
    })
    @GetMapping("/dono/{idDono}")
    public ResponseEntity<Page<AnimalDTO>> listarByDono(@PathVariable Long idDono, @RequestParam("pagina") int pagina, @RequestParam("quantidadeItens") int quantidadeItens) {

        logger.info("[listarByDono] - Início");

        Pageable pageable = PageRequest.of(pagina, quantidadeItens);

        Page<Animal> animais = animalService.listarByDonoId(idDono, pageable);

        Page<AnimalDTO> animaisDto = new PageImpl<>(mapper.animalListToDtoList(animais.getContent()), pageable, animais.getTotalElements());

        logger.info("[listarByDono] - Fim");

        return new ResponseEntity<>(animaisDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Cadastrar novo animal",
            description = "Cria um novo animal no sistema com base nos dados informados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Animal criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para o cadastro"),
            @ApiResponse(responseCode = "404", description = "Dono, raça ou sexo associado não encontrado")
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
            description = "Atualiza os dados de um animal já existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animal atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização"),
            @ApiResponse(responseCode = "404", description = "Animal, dono, raça ou sexo associado não encontrado")
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
            description = "Remove um animal existente do sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Animal excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {

        logger.info("[deletar] - Início");

        animalService.deletar(id);

        logger.info("[deletar] - Fim");

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}