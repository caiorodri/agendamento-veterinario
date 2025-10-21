package br.com.caiorodri.agendamentoveterinario.controller;

import br.com.caiorodri.agendamentoveterinario.dto.LoginResponseDTO;
import br.com.caiorodri.agendamentoveterinario.dto.StatusDTO;
import br.com.caiorodri.agendamentoveterinario.model.Status;
import br.com.caiorodri.agendamentoveterinario.security.TokenService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import br.com.caiorodri.agendamentoveterinario.dto.UsuarioDTO;
import br.com.caiorodri.agendamentoveterinario.dto.UsuarioRequestDTO;
import br.com.caiorodri.agendamentoveterinario.mapper.Mapper;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private Mapper mapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    final static Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @Operation(
            summary = "Listar usuários",
            description = "Retorna uma lista paginada de usuários cadastrados no sistema. (Requer perfil: ADMINISTRADOR, RECEPCIONISTA ou VETERINARIO)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                    @ApiResponse(responseCode = "403", description = "Usuário não tem permissão para esta ação"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'VETERINARIO')")
    public ResponseEntity<Page<UsuarioDTO>> listar(
            @Parameter(description = "Número da página (inicia em 0)", required = true, example = "0") @RequestParam("pagina") int pagina,
            @Parameter(description = "Quantidade de itens por página", required = true, example = "10") @RequestParam("quantidadeItens") int quantidadeItens) {

        logger.info("[listar] - Início");

        Pageable pageable = PageRequest.of(pagina, quantidadeItens);

        Page<Usuario> usuarios = usuarioService.listar(pageable);

        Page<UsuarioDTO> usuariosDto = new PageImpl<>(mapper.usuarioListToDtoList(usuarios.getContent()), usuarios.getPageable(), usuarios.getTotalElements());

        logger.info("[listar] - Fim");

        return new ResponseEntity<>(usuariosDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Buscar usuário por ID",
            description = "Recupera um usuário específico com base no seu ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> recuperar(
            @Parameter(description = "ID do usuário a ser buscado", required = true, example = "1") @PathVariable Long id) {

        logger.info("[recuperar] - Início");

        Usuario usuario = usuarioService.recuperar(id);

        UsuarioDTO usuarioDto = mapper.usuarioToDto(usuario);

        logger.info("[recuperar] - Fim");

        return new ResponseEntity<>(usuarioDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Buscar usuário por e-mail",
            description = "Recupera um usuário com base no e-mail informado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioDTO> recuperarByEmail(
            @Parameter(description = "E-mail do usuário a ser buscado", required = true, example = "cliente@email.com") @PathVariable String email) {

        logger.info("[recuperarByEmail] - Início");

        Usuario usuario = usuarioService.recuperarByEmail(email);

        UsuarioDTO usuarioDto = mapper.usuarioToDto(usuario);

        logger.info("[recuperarByEmail] - Fim");

        return new ResponseEntity<>(usuarioDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Autenticar usuário",
            description = "Realiza o login de um usuário com base no e-mail e senha informados, retornando um token JWT e os dados do usuário.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciais de e-mail e senha para login.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UsuarioRequestDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário autenticado com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @PostMapping("/autenticar")
    public ResponseEntity<LoginResponseDTO> autenticar(@RequestBody UsuarioRequestDTO usuarioRequest) {

        logger.info("[autenticar] - Início");

        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(usuarioRequest.getEmail(), usuarioRequest.getSenha());
        Authentication auth = this.authenticationManager.authenticate(usernamePassword);

        Usuario usuario = (Usuario) auth.getPrincipal();

        Usuario usuarioCompleto = usuarioService.recuperar(usuario.getId());

        var token = tokenService.generateToken(usuarioCompleto);

        var usuarioDto = mapper.usuarioToDto(usuarioCompleto);

        logger.info("[autenticar] - Fim");

        return new ResponseEntity<>(new LoginResponseDTO(token, usuarioDto), HttpStatus.OK);
    }

    @Operation(
            summary = "Cadastrar novo usuário",
            description = "Cria um novo usuário no sistema. (Endpoint público)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto JSON contendo os dados do novo usuário.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Usuario.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos para o cadastro (ex: e-mail ou CPF já em uso)"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @PostMapping
    public ResponseEntity<UsuarioDTO> salvar(@RequestBody Usuario usuario){

        logger.info("[salvar] - Início");

        Usuario usuarioSalvo = usuarioService.salvar(usuario);

        UsuarioDTO usuarioDto = mapper.usuarioToDto(usuarioSalvo);

        logger.info("[salvar] - Fim");

        return new ResponseEntity<>(usuarioDto, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Atualizar usuário",
            description = "Atualiza os dados de um usuário existente.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto JSON contendo os dados do usuário a ser atualizado, incluindo seu ID.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Usuario.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização"),
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @PutMapping
    public ResponseEntity<UsuarioDTO> atualizar(@RequestBody Usuario usuario){

        logger.info("[atualizar] - Início");

        Usuario usuarioAtualizado = usuarioService.atualizar(usuario);

        UsuarioDTO usuarioDto = mapper.usuarioToDto(usuarioAtualizado);

        logger.info("[atualizar] - Fim");

        return new ResponseEntity<>(usuarioDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Excluir usuário",
            description = "Remove um usuário existente do sistema. (Requer perfil: ADMINISTRADOR)",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                    @ApiResponse(responseCode = "403", description = "Usuário não tem permissão para esta ação"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do usuário a ser excluído", required = true, example = "1") @PathVariable Long id) {

        logger.info("[deletar] - Início");

        usuarioService.deletar(id);

        logger.info("[deletar] - Fim");

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Listar todos os status",
            description = "Retorna uma lista de todos os status de usuário possíveis (ex: Ativo, Inativo)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/status")
    public ResponseEntity<List<StatusDTO>> listarStatus() {

        logger.info("[listarStatus] - Início");

        List<Status> status = usuarioService.listarStatus();

        List<StatusDTO> statusDTO = mapper.statusListToDtoList(status);

        logger.info("[listarStatus] - Fim");

        return new ResponseEntity<>(statusDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Enviar código de recuperação por e-mail",
            description = "Inicia o processo de recuperação de senha. (Endpoint público). Para evitar enumeração de e-mail, este endpoint *sempre* retornará 200 OK, mesmo que o e-mail não exista."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação processada com sucesso."),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/recuperar-senha/{email}")
    public ResponseEntity<Void> enviarCodigoEmail(
            @Parameter(description = "E-mail para o qual o código de recuperação será enviado", required = true, example = "esqueci@email.com") @PathVariable String email) {

        logger.info("[enviarCodigoEmail] - Início do processo de recuperação de senha para o e-mail: {}", email);

        usuarioService.enviarCodigoEmail(email);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Operation(
            summary = "Enviar e-mail de campanha de vacinação",
            description = "Dispara o envio de um e-mail informativo sobre a campanha de vacinação para todos os clientes ativos que optaram por receber e-mails. (Requer perfil: ADMINISTRADOR ou RECEPCIONISTA)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Processo de envio de e-mails iniciado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não tem permissão para esta ação"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PostMapping("/enviar-campanha-vacinacao")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
    public ResponseEntity<Void> enviarEmailCampanhaVacinacao() {

        logger.info("[enviarEmailCampanhaVacinacao] - Início");

        usuarioService.enviarEmailClientesCampanhaVacinacao();

        logger.info("[enviarEmailCampanhaVacinacao] - Solicitação de envio recebida. O processo ocorrerá em segundo plano.");

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Operation(
            summary = "Validar código de recuperação",
            description = "Verifica se o código de recuperação fornecido para um usuário específico é válido. (Endpoint público)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código válido."),
            @ApiResponse(responseCode = "400", description = "Código inválido, expirado ou usuário não encontrado."),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/{id}/validar-codigo/{codigo}")
    public ResponseEntity<Void> validarCodigoRecuperacao(
            @Parameter(description = "ID do usuário que está validando o código", required = true, example = "1") @PathVariable Long id,
            @Parameter(description = "Código de 6 dígitos recebido por e-mail", required = true, example = "123456") @PathVariable String codigo) {

        logger.info("[validarCodigoRecuperacao] - Início da validação de código para o usuário ID: {}", id);

        boolean isCodigoValido = usuarioService.validarCodigo(id, codigo);

        if (isCodigoValido) {
            logger.info("[validarCodigoRecuperacao] - Código válido para o usuário ID: {}", id);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            logger.warn("[validarCodigoRecuperacao] - Código inválido ou não encontrado para o usuário ID: {}", id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
            summary = "Recuperar usuário logado",
            description = "Retorna os dados completos do usuário que está autenticado no momento da consulta."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário retornado com sucesso."),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getUsuarioLogado(@AuthenticationPrincipal Usuario usuario) {

        logger.info("[getUsuarioLogado] - Início");

        Usuario usuarioCompleto = usuarioService.recuperar(usuario.getId());

        var usuarioDto = mapper.usuarioToDto(usuarioCompleto);

        logger.info("[getUsuarioLogado] - Fim");

        return new ResponseEntity<>(usuarioDto, HttpStatus.OK);
    }

}