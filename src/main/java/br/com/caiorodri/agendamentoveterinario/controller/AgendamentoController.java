package br.com.caiorodri.agendamentoveterinario.controller;

import br.com.caiorodri.agendamentoveterinario.dto.AgendamentoStatusDTO;
import br.com.caiorodri.agendamentoveterinario.dto.AgendamentoTipoDTO;
import br.com.caiorodri.agendamentoveterinario.model.AgendamentoStatus;
import br.com.caiorodri.agendamentoveterinario.model.AgendamentoTipo;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.caiorodri.agendamentoveterinario.dto.AgendamentoDTO;
import br.com.caiorodri.agendamentoveterinario.mapper.Mapper;
import br.com.caiorodri.agendamentoveterinario.model.Agendamento;
import br.com.caiorodri.agendamentoveterinario.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;


@RestController
@RequestMapping("agendamentos")
public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private Mapper mapper;

    final static Logger logger = LoggerFactory.getLogger(AgendamentoController.class);

    @Operation(
            summary = "Listar agendamentos",
            description = "Retorna uma lista paginada de todos os agendamentos cadastrados.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Agendamentos listados com sucesso"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @GetMapping("")
    public ResponseEntity<Page<AgendamentoDTO>> listar(@RequestParam("pagina") int pagina, @RequestParam("quantidadeItens") int quantidadeItens) {

        logger.info("[listar] - Início");

        Pageable pageable = PageRequest.of(pagina, quantidadeItens);

        Page<Agendamento> agendamentos = agendamentoService.listar(pageable);

        Page<AgendamentoDTO> agendamentosDto = new PageImpl<>(mapper.agendamentoListToDtoList(agendamentos.getContent()), pageable, agendamentos.getTotalElements());

        logger.info("[listar] - Fim");

        return new ResponseEntity<>(agendamentosDto, HttpStatus.OK);

    }

    @Operation(
            summary = "Buscar agendamento por ID",
            description = "Recupera um agendamento com base no ID informado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Agendamento encontrado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoDTO> recuperar(@PathVariable(name = "id") Long id) {

        logger.info("[recuperar] - Início");

        Agendamento agendamento = agendamentoService.recuperar(id);

        AgendamentoDTO agendamentoDto = mapper.agendamentoToDto(agendamento);

        logger.info("[recuperar] - Fim");

        return new ResponseEntity<>(agendamentoDto, HttpStatus.OK);

    }

    @Operation(
            summary = "Listar agendamentos por animal",
            description = "Retorna uma lista paginada de agendamentos associados ao animal informado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Agendamentos listados com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Animal não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @GetMapping("/animal/{idAnimal}")
    public ResponseEntity<Page<AgendamentoDTO>> listarByAnimal(@PathVariable Long idAnimal, @RequestParam("pagina") int pagina, @RequestParam("quantidadeItens") int quantidadeItens) {

        logger.info("[listarByAnimal] - Início");

        Pageable pageable = PageRequest.of(pagina, quantidadeItens);

        Page<Agendamento> agendamentos = agendamentoService.listarByAnimalId(idAnimal, pageable);

        Page<AgendamentoDTO> agendamentosDto = new PageImpl<>(mapper.agendamentoListToDtoList(agendamentos.getContent()), pageable, agendamentos.getTotalElements());

        logger.info("[listarByAnimal] - Fim");

        return new ResponseEntity<>(agendamentosDto, HttpStatus.OK);

    }

    @Operation(
            summary = "Listar agendamentos por usuário",
            description = "Retorna uma lista paginada de agendamentos associados ao usuário informado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Agendamentos listados com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<Page<AgendamentoDTO>> listarByUsuario(@PathVariable Long idUsuario, @RequestParam("pagina") int pagina, @RequestParam("quantidadeItens") int quantidadeItens) {

        logger.info("[listarByUsuario] - Início");

        Pageable pageable = PageRequest.of(pagina, quantidadeItens);

        Page<Agendamento> agendamentos = agendamentoService.listarByUsuarioId(idUsuario, pageable);

        Page<AgendamentoDTO> agendamentosDto = new PageImpl<>(mapper.agendamentoListToDtoList(agendamentos.getContent()), pageable, agendamentos.getTotalElements());

        logger.info("[listarByUsuario] - Fim");

        return new ResponseEntity<>(agendamentosDto, HttpStatus.OK);

    }

    @Operation(
            summary = "Cadastrar novo agendamento",
            description = "Cria um novo agendamento no sistema com base nos dados informados.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Agendamento criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Erro ao cadastrar o agendamento"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @PostMapping("")
    public ResponseEntity<AgendamentoDTO> salvar(@RequestBody Agendamento agendamento){

        logger.info("[salvar] - Início");

        Agendamento agendamentoSalvo = agendamentoService.salvar(agendamento);

        AgendamentoDTO agendamentoDto = mapper.agendamentoToDto(agendamentoSalvo);

        logger.info("[salvar] - Fim");

        return new ResponseEntity<>(agendamentoDto, HttpStatus.CREATED);

    }

    @Operation(
            summary = "Atualizar agendamento",
            description = "Atualiza os dados de um agendamento já existente.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Agendamento atualizado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Erro ao atualizar o agendamento"),
                    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @PutMapping("")
    public ResponseEntity<AgendamentoDTO> atualizar(@RequestBody Agendamento agendamento){

        logger.info("[atualizar] - Início");

        AgendamentoDTO agendamentoDto = mapper.agendamentoToDto(agendamentoService.atualizar(agendamento));

        logger.info("[atualizar] - Fim");

        return new ResponseEntity<>(agendamentoDto, HttpStatus.OK);


    }

    @Operation(
            summary = "Excluir agendamento",
            description = "Remove um agendamento existente do sistema.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Agendamento excluído com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Erro ao excluir agendamento"),
                    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {

        logger.info("[deletar] - Início");

        agendamentoService.deletar(id);

        logger.info("[deletar] - Fim");

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }


    @Operation(
            summary = "Listar todas os status",
            description = "Retorna uma lista de todos os status de agendamento."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status listados com sucesso")
    })
    @GetMapping("/status")
    public ResponseEntity<List<AgendamentoStatusDTO>> listarAgendamentoStatus() {

        logger.info("[listarAgendamentoStatus] - Início");

        List<AgendamentoStatus> status = agendamentoService.listarAgendamentoStatus();

        List<AgendamentoStatusDTO> statusDTO = mapper.agendamentoStatusListToDtoList(status);

        logger.info("[listarAgendamentoStatus] - Fim");

        return new ResponseEntity<>(statusDTO, HttpStatus.OK);
    }


    @Operation(
            summary = "Listar todas os tipos de agendamentos",
            description = "Retorna uma lista de todos os tipos de agendamento."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipos listados com sucesso")
    })
    @GetMapping("/tipos")
    public ResponseEntity<List<AgendamentoTipoDTO>> listarAgendamentoTipo() {

        logger.info("[listarAgendamentoTipo] - Início");

        List<AgendamentoTipo> tipos = agendamentoService.listarAgendamentoTipo();

        List<AgendamentoTipoDTO> tiposDTO = mapper.agendamentoTipoListToDtoList(tipos);

        logger.info("[listarAgendamentoTipo] - Fim");

        return new ResponseEntity<>(tiposDTO, HttpStatus.OK);
    }

}
