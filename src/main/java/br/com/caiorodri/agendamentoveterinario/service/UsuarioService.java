package br.com.caiorodri.agendamentoveterinario.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import br.com.caiorodri.agendamentoveterinario.email.EmailSender;
import br.com.caiorodri.agendamentoveterinario.model.Status;
import br.com.caiorodri.agendamentoveterinario.repository.StatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.caiorodri.agendamentoveterinario.dto.UsuarioRequestDTO;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private StatusRepository statusRepository;

    // @Autowired
    // private EmailSender emailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        Usuario usuario = usuarioRepository.findByEmail(usuarioRequest.getEmail())
                .orElseThrow(() -> new SecurityException("Credenciais inválidas"));

        if (passwordEncoder.matches(usuarioRequest.getSenha(), usuario.getSenha())) {

            logger.info("[autenticar] - Usuário autenticado com sucesso: {}", usuario.getEmail());
            return usuario;

        } else {

            throw new SecurityException("Credenciais inválidas");

        }

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

            String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
            usuario.setSenha(senhaCriptografada);

            Usuario usuarioSalvo = usuarioRepository.save(usuario);

            // emailSender.enviarInformacaoCadastroUsuarioEmail(usuarioSalvo);

            logger.info("[salvar] - Usuário salvo com id = {}", usuarioSalvo.getId());

            return usuarioSalvo;

        } catch (IllegalArgumentException e) {

            logger.error("[salvar] - Erro de validação ao salvar usuário: {}", e.getMessage());

            throw e;

        } catch (Exception e){

            logger.error("[salvar] - Erro ao salvar usuário: {}", e.getMessage());

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

        Usuario usuarioExistente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado para atualização."));

        try {

            validarUsuario(usuario, false);

            if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
                String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
                usuarioExistente.setSenha(senhaCriptografada);
            }

            usuarioExistente.setNome(usuario.getNome());
            usuarioExistente.setEmail(usuario.getEmail());
            usuarioExistente.setDataNascimento(usuario.getDataNascimento());
            usuarioExistente.setEndereco(usuario.getEndereco());
            usuarioExistente.setTelefones(usuario.getTelefones());
            usuarioExistente.setStatus(usuario.getStatus());
            usuarioExistente.setPerfil(usuario.getPerfil());
            usuarioExistente.setReceberEmail(usuario.isReceberEmail());

            Usuario usuarioAtualizado = usuarioRepository.save(usuarioExistente);

            logger.info("[atualizar] - Usuário atualizado com sucesso id = {}", usuarioAtualizado.getId());

            return usuarioAtualizado;

        } catch (IllegalArgumentException e) {

            logger.error("[atualizar] - Erro de validação ao atualizar usuário id = {}: {}", usuario.getId(), e.getMessage());

            throw e;

        } catch (Exception e){

            logger.error("[atualizar] - Erro ao atualizar usuário id = {}: {}", usuario.getId(), e.getMessage());

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

        if (isNovoUsuario && (usuario.getSenha() == null || usuario.getSenha().isBlank())) {

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


    /**
     * Lista todos as status do usuário
     *
     * @return List com os status do usuário.
     */
    public List<Status> listarStatus() {

        logger.info("[listarStatus] - Listando sexo dos animais");

        List<Status> listaStatus = statusRepository.findAll();

        logger.info("[listarStatus] - Encontrados {} status", listaStatus.size());

        return listaStatus;
    }

    /**
     * Envia código de recuperação para o usuário por email
     *
     * @return Resultado do envio.
     */
    public boolean enviarCodigoEmail(String email){

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if(usuario == null){

            return false;

        }

        // emailSender.enviarCodigoEmail(email);

        return true;

    }

    /**
     * Valida o código de recuperação do usuário, checando também se não expirou.
     *
     * @param idUsuario Id do usuário
     * @param codigo Código a ser validado
     *
     * @return true se o código for válido e não estiver expirado, caso contrário false.
     */
    public boolean validarCodigo(Long idUsuario, String codigo) {

        logger.info("[validarCodigo] - Validando código para o usuário ID: {}", idUsuario);

        Optional<Usuario> usuarioOptional = usuarioRepository.findById(idUsuario);

        if (usuarioOptional.isEmpty()) {
            logger.warn("[validarCodigo] - Tentativa de validação para usuário inexistente. ID: {}", idUsuario);
            return false;
        }

        Usuario usuario = usuarioOptional.get();

        if(usuario.getExpiracaoCodigo() == null){

            logger.warn("[validarCodigo] - Código de recuperação expirou para o usuário ID: {}", idUsuario);
            return false;

        }

        boolean codigoCorreto = codigo != null && codigo.equals(usuario.getCodigoRecuperacao());
        boolean naoExpirou = usuario.getExpiracaoCodigo() != null && LocalDateTime.now().isBefore(usuario.getExpiracaoCodigo());

        if (codigoCorreto && naoExpirou) {

            usuario.setCodigoRecuperacao(null);
            usuario.setExpiracaoCodigo(null);
            usuarioRepository.save(usuario);

            logger.info("[validarCodigo] - Código validado com sucesso para o usuário ID: {}", idUsuario);
            return true;
        } else {
            if (!codigoCorreto) {
                logger.warn("[validarCodigo] - Código fornecido é inválido para o usuário ID: {}", idUsuario);
            }
            if (!naoExpirou) {
                logger.warn("[validarCodigo] - Código de recuperação expirou para o usuário ID: {}", idUsuario);
            }
            return false;
        }
    }

    /**
     * Envia um aviso de campanha de vacinação para cada usuário por email
     *
     */
    public void enviarEmailClientesCampanhaVacinacao() {

        List<Usuario> usuarios = usuarioRepository.findClientesAtivos();

        for(Usuario usuario : usuarios) {

            if(usuario.isReceberEmail()) {

                // emailSender.enviarInformacaoCampanhaVacinaEmail(usuario);

            }


        }

    }

}
