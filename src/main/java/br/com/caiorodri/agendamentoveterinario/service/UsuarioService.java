package br.com.caiorodri.agendamentoveterinario.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.caiorodri.agendamentoveterinario.dto.UsuarioRequestDTO;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    final static Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    /**
     * Recupera um usuário pelo seu ID.
     *
     * @param id ID do usuário.
     * @return Usuário encontrado.
     * @throws EntityNotFoundException caso não exista usuário com o id enviado.
     */
    public Usuario recuperar(Long id) {

        logger.info("[recuperar] - Buscando usuário com id = {}", id);

        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuário com id " + id + " não encontrado"));

        logger.info("[recuperar] - Usuário com id = {} encontrado", id);

        return usuario;
    }

    /**
     * Lista todos os usuários com paginação.
     *
     * @param pageable Dados de paginação.
     * @return Page com usuários.
     */
    public Page<Usuario> listar(Pageable pageable) {

        logger.info("[listar] - Listando usuários página = {}, tamanho = {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Usuario> usuarios = usuarioRepository.findAll(pageable);

        logger.info("[listar] - Encontrados {} usuários", usuarios.getTotalElements());

        return usuarios;
    }

    /**
     * Recupera um usuário pelo seu e-mail.
     *
     * @param email E-mail do usuário.
     * @return Usuário encontrado.
     * @throws EntityNotFoundException caso não exista usuário com o e-mail enviado.
     */
    public Usuario recuperarByEmail(String email) {

        logger.info("[recuperarByEmail] - Buscando usuário com email = {}", email);

        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);

        if (usuarioOptional.isEmpty()) {

            throw new EntityNotFoundException("Usuário com email " + email + " não encontrado");

        }

        logger.info("[recuperarByEmail] - Usuário com email = {} encontrado", email);

        return usuarioOptional.get();
    }

    /**
     * Autentica um usuário com base no email e senha.
     *
     * @param usuarioRequest DTO com credenciais de login.
     * @return Usuário autenticado.
     * @throws SecurityException caso as credenciais sejam inválidas.
     */
    public Usuario autenticar(UsuarioRequestDTO usuarioRequest) {

        logger.info("[autenticar] - Tentativa de autenticação para o email = {}", usuarioRequest.getEmail());

        Usuario usuario = usuarioRepository.findByEmailAndSenha(usuarioRequest.getEmail(), usuarioRequest.getSenha()).orElseThrow(() -> new SecurityException("Credenciais inválidas"));

        logger.info("[autenticar] - Usuário autenticado com sucesso: {}", usuario.getEmail());

        return usuario;
    }

    /**
     * Salva um novo usuário no banco de dados.
     *
     * @param usuario Objeto usuário a ser salvo.
     * @return Usuário salvo.
     * @throws IllegalArgumentException se dados obrigatórios estiverem ausentes ou inválidos.
     */
    public Usuario salvar(Usuario usuario) {

        logger.info("[salvar] - Iniciando salvamento de novo usuário");

        try {

            validarUsuario(usuario, true);

            Usuario usuarioSalvo = usuarioRepository.save(usuario);

            logger.info("[salvar] - Usuário salvo com id = {}", usuarioSalvo.getId());

            return usuarioSalvo;

        } catch (IllegalArgumentException e) {

            logger.error("[salvar] - Erro de validação ao salvar usuário: {}", e.getMessage());

            throw e;

        }

    }

    /**
     * Atualiza um usuário existente.
     *
     * @param usuario Objeto usuário com dados atualizados.
     * @return Usuário atualizado.
     * @throws EntityNotFoundException se o usuário não existir.
     * @throws IllegalArgumentException se os dados para atualização forem inválidos.
     */
    public Usuario atualizar(Usuario usuario) {

        logger.info("[atualizar] - Atualizando usuário id = {}", usuario.getId());

        if (usuario.getId() == null || !usuarioRepository.existsById(usuario.getId())) {

            logger.error("[atualizar] - Usuário não encontrado para id = {}", usuario.getId());

            throw new EntityNotFoundException("Usuário não encontrado para atualização.");
        }

        try {

            validarUsuario(usuario, false); // false para 'isNovoUsuario'

            Usuario usuarioAtualizado = usuarioRepository.save(usuario);

            logger.info("[atualizar] - Usuário atualizado com sucesso id = {}", usuarioAtualizado.getId());

            return usuarioAtualizado;

        } catch (IllegalArgumentException e) {

            logger.error("[atualizar] - Erro de validação ao atualizar usuário id = {}: {}", usuario.getId(), e.getMessage());

            throw e;
        }
    }

    /**
     * Deleta um usuário pelo seu ID.
     *
     * @param id ID do usuário a ser deletado.
     * @throws EntityNotFoundException se o usuário não existir.
     */
    public void deletar(Long id) {

        logger.info("[deletar] - Deletando usuário id = {}", id);

        if (!usuarioRepository.existsById(id)) {

            logger.error("[deletar] - Usuário não encontrado para id = {}", id);

            throw new EntityNotFoundException("Usuário não encontrado para exclusão.");
        }

        usuarioRepository.deleteById(id);

        logger.info("[deletar] - Usuário deletado com sucesso id = {}", id);
    }

    /**
     * Valida os dados do usuário.
     *
     * @param usuario Usuário a validar.
     * @param isNovoUsuario Flag para diferenciar validação de criação e atualização.
     * @throws IllegalArgumentException se validações falharem.
     */
    private void validarUsuario(Usuario usuario, boolean isNovoUsuario) {

        if (usuario == null) {

            throw new IllegalArgumentException("Usuário não pode ser nulo.");

        }

        if (usuario.getNome() == null || usuario.getNome().isBlank()) {

            throw new IllegalArgumentException("Nome do usuário é obrigatório.");
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {

            throw new IllegalArgumentException("Email do usuário é obrigatório.");
        }

        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {

            throw new IllegalArgumentException("Senha do usuário é obrigatória.");

        }

        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(usuario.getEmail());

        if (usuarioExistente.isPresent()) {

            if (isNovoUsuario) {

                throw new IllegalArgumentException("O e-mail informado já está em uso.");

            } else {

                if (!usuarioExistente.get().getId().equals(usuario.getId())) {

                    throw new IllegalArgumentException("O e-mail informado já está em uso por outro usuário.");

                }

            }

        }

    }

}