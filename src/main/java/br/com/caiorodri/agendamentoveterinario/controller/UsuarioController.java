package br.com.caiorodri.agendamentoveterinario.controller;

import br.com.caiorodri.agendamentoveterinario.dto.StatusDTO;
import br.com.caiorodri.agendamentoveterinario.model.Status;
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

import br.com.caiorodri.agendamentoveterinario.dto.UsuarioDTO;
import br.com.caiorodri.agendamentoveterinario.dto.UsuarioRequestDTO;
import br.com.caiorodri.agendamentoveterinario.mapper.Mapper;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private Mapper mapper;

    final static Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @Operation(
            summary = "Listar usuários",
            description = "Retorna uma lista paginada de usuários cadastrados no sistema.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso")
            }
    )
    @GetMapping
    public ResponseEntity<Page<UsuarioDTO>> listar(@RequestParam("pagina") int pagina, @RequestParam("quantidadeItens") int quantidadeItens) {

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
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> recuperar(@PathVariable Long id) {

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
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioDTO> recuperarByEmail(@PathVariable String email) {

        logger.info("[recuperarByEmail] - Início");

        Usuario usuario = usuarioService.recuperarByEmail(email);

        UsuarioDTO usuarioDto = mapper.usuarioToDto(usuario);

        logger.info("[recuperarByEmail] - Fim");

        return new ResponseEntity<>(usuarioDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Autenticar usuário",
            description = "Realiza o login de um usuário com base no e-mail e senha informados.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário autenticado com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
            }
    )
    @PostMapping("/autenticar")
    public ResponseEntity<UsuarioDTO> autenticar(@RequestBody UsuarioRequestDTO usuarioRequest) {

        logger.info("[autenticar] - Início");

        Usuario usuario = usuarioService.autenticar(usuarioRequest);

        UsuarioDTO usuarioDto = mapper.usuarioToDto(usuario);

        logger.info("[autenticar] - Fim");

        return new ResponseEntity<>(usuarioDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Cadastrar novo usuário",
            description = "Cria um novo usuário no sistema.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos para o cadastro")
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
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
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
            description = "Remove um usuário existente do sistema.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {

        logger.info("[deletar] - Início");

        usuarioService.deletar(id);

        logger.info("[deletar] - Fim");

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Listar todas os status",
            description = "Retorna uma lista de todos os status do usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status listados com sucesso")
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
            description = "Inicia o processo de recuperação de senha. Se o e-mail informado existir na base de dados, um código de recuperação será enviado para ele."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação processada. Se o e-mail existir, o código foi enviado."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado com o e-mail fornecido.")
    })
    @GetMapping("/recuperar-senha/{email}")
    public ResponseEntity<Void> enviarCodigoEmail(@PathVariable String email) {

        logger.info("[enviarCodigoEmail] - Início do processo de recuperação de senha para o e-mail: {}", email);

        boolean emailEnviado = usuarioService.enviarCodigoEmail(email);

        if (emailEnviado) {
            logger.info("[enviarCodigoEmail] - Fim. E-mail de recuperação enviado para {}", email);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            logger.warn("[enviarCodigoEmail] - Fim. Tentativa de recuperação para um e-mail não cadastrado: {}", email);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Enviar e-mail de campanha de vacinação",
            description = "Dispara o envio de um e-mail informativo sobre a campanha de vacinação para todos os clientes ativos que optaram por receber e-mails. (Apenas Administradores e Recepcionistas)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Processo de envio de e-mails iniciado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não tem permissão para esta ação")
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
            description = "Verifica se o código de recuperação fornecido para um usuário específico é válido. Este é um passo do fluxo de redefinição de senha."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código válido."),
            @ApiResponse(responseCode = "400", description = "Código inválido ou usuário não encontrado.")
    })
    @GetMapping("/{id}/validar-codigo/{codigo}")
    public ResponseEntity<Void> validarCodigoRecuperacao(@PathVariable Long id, @PathVariable String codigo) {

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

}
