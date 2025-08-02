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

}
