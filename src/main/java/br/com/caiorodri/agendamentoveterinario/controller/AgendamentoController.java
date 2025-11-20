package br.com.caiorodri.agendamentoveterinario.controller;

import br.com.caiorodri.agendamentoveterinario.dto.AgendamentoStatusDTO;
import br.com.caiorodri.agendamentoveterinario.dto.AgendamentoTipoDTO;
import br.com.caiorodri.agendamentoveterinario.model.AgendamentoStatus;
import br.com.caiorodri.agendamentoveterinario.model.AgendamentoTipo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("agendamentos")
@Tag(name = "Agendamentos", description = "Endpoints para gerenciamento de agendamentos")
public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private Mapper mapper;

    final static Logger logger = LoggerFactory.getLogger(AgendamentoController.class);

    @Operation(
            summary = "Listar agendamentos",
            description = "Retorna uma lista paginada de todos os agendamentos cadastrados. (Requer perfil: ADMINISTRADOR, VETERINARIO ou RECEPCIONISTA)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Agendamentos listados com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                    @ApiResponse(responseCode = "403", description = "Usuário não tem permissão para esta ação"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO', 'RECEPCIONISTA')")
    public ResponseEntity<Page<AgendamentoDTO>> listar(
            @Parameter(description = "Número da página (inicia em 0)", required = true, example = "0") @RequestParam("pagina") int pagina,
            @Parameter(description = "Quantidade de itens por página", required = true, example = "10") @RequestParam("quantidadeItens") int quantidadeItens) {

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
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoDTO> recuperar(
            @Parameter(description = "ID do agendamento a ser buscado", required = true, example = "1") @PathVariable(name = "id") Long id) {

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
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                    @ApiResponse(responseCode = "404", description = "Animal não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @GetMapping("/animal/{idAnimal}")
    public ResponseEntity<Page<AgendamentoDTO>> listarByAnimal(
            @Parameter(description = "ID do animal para filtrar os agendamentos", required = true, example = "1") @PathVariable Long idAnimal,
            @Parameter(description = "Número da página (inicia em 0)", required = true, example = "0") @RequestParam("pagina") int pagina,
            @Parameter(description = "Quantidade de itens por página", required = true, example = "10") @RequestParam("quantidadeItens") int quantidadeItens) {

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
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<Page<AgendamentoDTO>> listarByUsuario(
            @Parameter(description = "ID do usuário (cliente) para filtrar os agendamentos", required = true, example = "1") @PathVariable Long idUsuario,
            @Parameter(description = "Número da página (inicia em 0)", required = true, example = "0") @RequestParam("pagina") int pagina,
            @Parameter(description = "Quantidade de itens por página", required = true, example = "10") @RequestParam("quantidadeItens") int quantidadeItens) {

        logger.info("[listarByUsuario] - Início");

        Pageable pageable = PageRequest.of(pagina, quantidadeItens);

        Page<Agendamento> agendamentos = agendamentoService.listarByUsuarioId(idUsuario, pageable);

        Page<AgendamentoDTO> agendamentosDto = new PageImpl<>(mapper.agendamentoListToDtoList(agendamentos.getContent()), pageable, agendamentos.getTotalElements());

        logger.info("[listarByUsuario] - Fim");

        return new ResponseEntity<>(agendamentosDto, HttpStatus.OK);

    }

    @Operation(
            summary = "Listar agendamentos por data",
            description = "Retorna uma lista de todos os agendamentos em uma data específica.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Agendamentos listados com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @GetMapping("/data")
    public ResponseEntity<List<AgendamentoDTO>> listarByData(
            @Parameter(description = "Data para filtro (formato ISO: yyyy-MM-dd)", required = true, example = "2024-12-25")
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        logger.info("[listarPorData] - Início");

        List<Agendamento> agendamentos = agendamentoService.listarAgendamentosNaData(data);

        List<AgendamentoDTO> agendamentosDto = mapper.agendamentoListToDtoList(agendamentos);

        logger.info("[listarPorData] - Fim");

        return new ResponseEntity<>(agendamentosDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar agendamentos por veterinário e data",
            description = "Retorna uma lista de agendamentos de um veterinário específico em uma data.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Agendamentos listados com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                    @ApiResponse(responseCode = "404", description = "Veterinário não encontrado (se validado no service)"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @GetMapping("/veterinario/{idVeterinario}/data")
    public ResponseEntity<List<AgendamentoDTO>> listarByVeterinarioAndData(
            @Parameter(description = "ID do veterinário", required = true, example = "1") @PathVariable Long idVeterinario,
            @Parameter(description = "Data para filtro (formato ISO: yyyy-MM-dd)", required = true, example = "2023-12-25")
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        logger.info("[listarPorVeterinarioEData] - Início");

        List<Agendamento> agendamentos = agendamentoService.listarAgendamentosVeterinarioNaData(idVeterinario, data);

        List<AgendamentoDTO> agendamentosDto = mapper.agendamentoListToDtoList(agendamentos);

        logger.info("[listarPorVeterinarioEData] - Fim");

        return new ResponseEntity<>(agendamentosDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Cadastrar novo agendamento",
            description = "Cria um novo agendamento no sistema com base nos dados informados.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto JSON contendo os dados do novo agendamento.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Agendamento.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Agendamento criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Erro ao cadastrar o agendamento (ex: conflito de horário, dados inválidos)"),
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
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
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto JSON contendo os dados do agendamento a ser atualizado, incluindo seu ID.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Agendamento.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Agendamento atualizado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Erro ao atualizar o agendamento (ex: conflito de horário, dados inválidos)"),
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
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
            description = "Remove um agendamento existente do sistema. (Requer perfil: ADMINISTRADOR)",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Agendamento excluído com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Erro ao excluir agendamento"),
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                    @ApiResponse(responseCode = "403", description = "Usuário não tem permissão para esta ação"),
                    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do agendamento a ser excluído", required = true, example = "1") @PathVariable Long id) {

        logger.info("[deletar] - Início");

        agendamentoService.deletar(id);

        logger.info("[deletar] - Fim");

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }


    @Operation(
            summary = "Listar status de agendamento",
            description = "Retorna uma lista de todos os status possíveis para um agendamento (ex: Pendente, Confirmado, Cancelado)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
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
            summary = "Listar tipos de agendamento",
            description = "Retorna uma lista de todos os tipos possíveis para um agendamento (ex: Consulta, Cirurgia, Vacina)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipos listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
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