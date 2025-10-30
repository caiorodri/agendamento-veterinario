package br.com.caiorodri.agendamentoveterinario.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import br.com.caiorodri.agendamentoveterinario.email.EmailSender;
import br.com.caiorodri.agendamentoveterinario.model.Status;
import br.com.caiorodri.agendamentoveterinario.model.UsuarioAlterarSenha;
import br.com.caiorodri.agendamentoveterinario.repository.StatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private EmailSender emailSender;

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
    @Transactional(readOnly = true)
    public Usuario recuperar(Long id) {

        logger.info("[recuperar] - Inicio - Buscando usuário com id = {}", id);

        try {

            Usuario usuario = usuarioRepository.findByIdWithSets(id).orElseThrow(() -> new EntityNotFoundException("Usuário com id " + id + " não encontrado"));

            usuario = usuarioRepository.findByIdWithAgendamentos(id).get();

            logger.info("[recuperar] - Fim - Usuário com id = {} encontrado", id);

            return usuario;

        } catch (EntityNotFoundException e) {

            logger.error("[recuperar] - Fim - Erro: {}", e.getMessage());
            throw e;

        }
    }

    /**
     * Lista todos os usuários com paginação.
     *
     * @param pageable Dados de paginação.
     * @return Page com usuários.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar os usuários.
     */
    @Transactional(readOnly = true)
    public Page<Usuario> listar(Pageable pageable) {

        logger.info("[listar] - Inicio - Listando usuários: página = {}, tamanho = {}", pageable.getPageNumber(), pageable.getPageSize());

        try {

            Page<Usuario> usuarios = usuarioRepository.findAll(pageable);

            logger.info("[listar] - Fim - Encontrados {} usuários no total.", usuarios.getTotalElements());

            return usuarios;

        } catch (Exception e) {

            logger.error("[listar] - Fim - Erro inesperado ao listar usuários: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar usuários", e);

        }
    }

    /**
     * Recupera um usuário pelo seu e-mail.
     *
     * @param email E-mail do usuário.
     * @return Usuário encontrado.
     * @throws EntityNotFoundException caso não exista usuário com o e-mail enviado.
     */
    @Transactional(readOnly = true)
    public Usuario recuperarByEmail(String email) {

        logger.info("[recuperarByEmail] - Inicio - Buscando usuário com email = {}", email);

        try {

            Usuario usuario = usuarioRepository.findByEmailWithSets(email)
                    .orElseThrow(() -> new EntityNotFoundException("Usuário com email " + email + " não encontrado"));

            usuario = usuarioRepository.findByIdWithAgendamentos(usuario.getId()).get();

            logger.info("[recuperarByEmail] - Fim - Usuário com email = {} encontrado", email);

            return usuario;

        } catch (EntityNotFoundException e) {

            logger.error("[recuperarByEmail] - Fim - Erro: {}", e.getMessage());
            throw e;

        }
    }

    /**
     * Salva um novo usuário no banco de dados.
     *
     * @param usuario Objeto usuário a ser salvo.
     * @return Usuário salvo.
     * @throws IllegalArgumentException se dados obrigatórios estiverem ausentes ou inválidos.
     * @throws RuntimeException se ocorrer um erro inesperado ao salvar.
     */
    @Transactional
    public Usuario salvar(Usuario usuario) {

        logger.info("[salvar] - Inicio - Tentativa de salvar um novo usuário.");

        try {

            validarUsuario(usuario, true);

            String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
            usuario.setSenha(senhaCriptografada);

            Usuario usuarioSalvo = usuarioRepository.save(usuario);

            emailSender.enviarInformacaoCadastroUsuarioEmail(usuarioSalvo);

            logger.info("[salvar] - Fim - Usuário salvo com sucesso com o id = {}", usuarioSalvo.getId());

            return usuarioSalvo;

        } catch (IllegalArgumentException e) {

            logger.error("[salvar] - Fim - Erro de validação ao salvar usuário: {}", e.getMessage());
            throw e;

        } catch (Exception e){

            logger.error("[salvar] - Fim - Erro inesperado ao salvar usuário: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar usuário", e);

        }
    }

    /**
     * Atualiza um usuário existente.
     *
     * @param usuario Objeto usuário com dados atualizados.
     * @return Usuário atualizado.
     * @throws EntityNotFoundException se o usuário não existir.
     * @throws IllegalArgumentException se os dados para atualização forem inválidos.
     * @throws RuntimeException se ocorrer um erro inesperado ao atualizar.
     */
    @Transactional
    public Usuario atualizar(Usuario usuario) {

        logger.info("[atualizar] - Inicio - Tentativa de atualizar o usuário com id = {}", usuario.getId());

        try {

            Usuario usuarioExistente = usuarioRepository.findById(usuario.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuário com id " + usuario.getId() + " não encontrado para atualização."));

            validarUsuario(usuario, false);

            usuarioExistente.setNome(usuario.getNome());
            usuarioExistente.setEmail(usuario.getEmail());
            usuarioExistente.setDataNascimento(usuario.getDataNascimento());
            usuarioExistente.setEndereco(usuario.getEndereco());

            if (usuario.getTelefones() != null) {
                usuarioExistente.getTelefones().clear();
                usuarioExistente.getTelefones().addAll(usuario.getTelefones());
            }

            usuarioExistente.setStatus(usuario.getStatus());
            usuarioExistente.setPerfil(usuario.getPerfil());
            usuarioExistente.setReceberEmail(usuario.isReceberEmail());

            usuarioRepository.saveAndFlush(usuarioExistente);

            Usuario usuarioAtualizado = this.recuperar(usuario.getId());

            logger.info("[atualizar] - Fim - Usuário com id = {} atualizado com sucesso.", usuarioAtualizado.getId());

            return usuarioAtualizado;

        } catch (EntityNotFoundException | IllegalArgumentException e) {

            logger.error("[atualizar] - Fim - Erro de validação ao atualizar usuário com id = {}: {}", usuario.getId(), e.getMessage());
            throw e;

        } catch (Exception e){

            logger.error("[atualizar] - Fim - Erro inesperado ao atualizar usuário com id = {}: {}", usuario.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar usuário", e);

        }

    }

    /**
     * Altera a senha de um usuário autenticado, validando sua senha antiga.
     *
     * @param usuarioLogado O usuário autenticado (principal).
     * @param usuarioAlterarSenha DTO contendo a senha antiga e a nova senha.
     * @throws IllegalArgumentException se a senha antiga não conferir ou a nova senha for inválida.
     * @throws RuntimeException se ocorrer um erro inesperado.
     */
    @Transactional
    public void alterarSenha(Usuario usuarioLogado, UsuarioAlterarSenha usuarioAlterarSenha) {

        logger.info("[alterarSenha] - Inicio - Tentativa de alteração de senha para o usuário ID: {}", usuarioLogado.getId());

        try {

            if (usuarioAlterarSenha.getSenhaNova() == null || usuarioAlterarSenha.getSenhaNova().isBlank()) {
                throw new IllegalArgumentException("A nova senha não pode estar em branco.");
            }
            if (usuarioAlterarSenha.getSenhaNova().length() < 8) {
                throw new IllegalArgumentException("A nova senha deve ter no mínimo 8 caracteres.");
            }

            if (usuarioAlterarSenha.getSenhaAntiga() == null || !passwordEncoder.matches(usuarioAlterarSenha.getSenhaAntiga(), usuarioLogado.getSenha())) {

                logger.warn("[alterarSenha] - Fim - Senha antiga inválida para o usuário ID: {}", usuarioLogado.getId());
                throw new IllegalArgumentException("A senha antiga está incorreta.");

            }

            if (passwordEncoder.matches(usuarioAlterarSenha.getSenhaNova(), usuarioLogado.getSenha())) {
                throw new IllegalArgumentException("A nova senha não pode ser igual à senha antiga.");
            }

            Usuario usuario = usuarioRepository.findById(usuarioLogado.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuário ID " + usuarioLogado.getId() + " não encontrado na transação."));

            String senhaCriptografada = passwordEncoder.encode(usuarioAlterarSenha.getSenhaNova());
            usuario.setSenha(senhaCriptografada);

            usuarioRepository.save(usuario);

            logger.info("[alterarSenha] - Fim - Senha alterada com sucesso para o usuário ID: {}", usuarioLogado.getId());

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            logger.error("[alterarSenha] - Fim - Erro: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("[alterarSenha] - Fim - Erro inesperado ao alterar senha para o ID {}: {}", usuarioLogado.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao alterar senha", e);
        }
    }

    /**
     * Deleta um usuário pelo seu ID.
     *
     * @param id ID do usuário a ser deletado.
     * @throws EntityNotFoundException se o usuário não existir.
     * @throws RuntimeException se ocorrer um erro inesperado ao deletar.
     */
    @Transactional
    public void deletar(Long id) {

        logger.info("[deletar] - Inicio - Tentativa de deletar o usuário com id = {}", id);

        try {

            if (!usuarioRepository.existsById(id)) {

                throw new EntityNotFoundException("Usuário com id " + id + " não encontrado para exclusão.");

            }

            usuarioRepository.deleteById(id);

            logger.info("[deletar] - Fim - Usuário com id = {} deletado com sucesso.", id);

        } catch (EntityNotFoundException e) {

            logger.error("[deletar] - Fim - Erro: {}", e.getMessage());
            throw e;

        } catch (Exception e) {

            logger.error("[deletar] - Fim - Erro inesperado ao deletar usuário com id = {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao deletar usuário", e);

        }
    }

    /**
     * Valida os dados do usuário.
     *
     * @param usuario Usuário a validar.
     * @param isNovoUsuario Flag para diferenciar validação de criação e atualização.
     * @throws IllegalArgumentException se validações falharem.
     */
    private void validarUsuario(Usuario usuario, boolean isNovoUsuario) {

        logger.info("[validarUsuario] - Inicio - Validando dados do usuário com email: {}", usuario.getEmail());

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

        if (isNovoUsuario && usuarioRepository.existsByCpf(usuario.getCpf())) {

            throw new IllegalArgumentException("CPF já cadastrado no sistema.");

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

        logger.info("[validarUsuario] - Fim - Validação concluída com sucesso para o usuário com email: {}", usuario.getEmail());

    }


    /**
     * Lista todos os status do usuário.
     *
     * @return List com os status do usuário.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar os status.
     */
    @Transactional(readOnly = true)
    public List<Status> listarStatus() {

        logger.info("[listarStatus] - Inicio - Buscando todos os status de usuário.");

        try {

            List<Status> listaStatus = statusRepository.findAll();

            logger.info("[listarStatus] - Fim - Busca concluída. Encontrados {} status.", listaStatus.size());

            return listaStatus;

        } catch (Exception e) {

            logger.error("[listarStatus] - Fim - Erro inesperado ao listar status: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar status", e);

        }
    }

    /**
     * Envia código de recuperação para o usuário por email.
     *
     * @param email Email do destinatário.
     * @return {@code true} se o usuário existe e o processo de envio foi iniciado, {@code false} caso contrário.
     */
    @Transactional(readOnly = true)
    public boolean enviarCodigoEmail(String email){

        logger.info("[enviarCodigoEmail] - Inicio - Tentativa de envio de código para o email: {}", email);

        try {

            Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);

            if(usuarioOptional.isEmpty()){

                logger.warn("[enviarCodigoEmail] - Fim - Usuário com email {} não encontrado.", email);
                return false;

            }

            emailSender.enviarCodigoEmail(email);

            logger.info("[enviarCodigoEmail] - Fim - Processo de envio de código iniciado para o email: {}", email);
            return true;

        } catch (Exception e) {

            logger.error("[enviarCodigoEmail] - Fim - Erro inesperado ao enviar código para o email {}: {}", email, e.getMessage(), e);
            return false;

        }
    }

    /**
     * Valida o código de recuperação do usuário, checando também se não expirou.
     *
     * @param idUsuario Id do usuário
     * @param codigo Código a ser validado
     * @return {@code true} se o código for válido e não estiver expirado, caso contrário {@code false}.
     */
    @Transactional
    public boolean validarCodigo(Long idUsuario, String codigo) {

        logger.info("[validarCodigo] - Inicio - Validando código para o usuário ID: {}", idUsuario);

        try {

            Optional<Usuario> usuarioOptional = usuarioRepository.findById(idUsuario);

            if (usuarioOptional.isEmpty()) {

                logger.warn("[validarCodigo] - Fim - Tentativa de validação para usuário inexistente. ID: {}", idUsuario);
                return false;

            }

            Usuario usuario = usuarioOptional.get();

            if(usuario.getExpiracaoCodigo() == null){

                logger.warn("[validarCodigo] - Fim - Usuário ID {} não possui um código de recuperação ativo.", idUsuario);
                return false;

            }

            boolean codigoCorreto = codigo != null && codigo.equals(usuario.getCodigoRecuperacao());
            boolean naoExpirou = LocalDateTime.now().isBefore(usuario.getExpiracaoCodigo());

            if (codigoCorreto && naoExpirou) {

                usuario.setCodigoRecuperacao(null);
                usuario.setExpiracaoCodigo(null);
                usuarioRepository.save(usuario);

                logger.info("[validarCodigo] - Fim - Código validado com sucesso para o usuário ID: {}", idUsuario);
                return true;

            } else {

                if (!codigoCorreto) {
                    logger.warn("[validarCodigo] - Fim - Código fornecido é inválido para o usuário ID: {}", idUsuario);
                }
                if (!naoExpirou) {
                    logger.warn("[validarCodigo] - Fim - Código de recuperação expirou para o usuário ID: {}", idUsuario);
                }
                return false;
            }

        } catch (Exception e) {

            logger.error("[validarCodigo] - Fim - Erro inesperado ao validar código para o usuário ID {}: {}", idUsuario, e.getMessage(), e);
            return false;

        }
    }

    /**
     * Envia um aviso de campanha de vacinação para cada usuário por email.
     * O processo continua mesmo que o envio para um usuário falhe.
     */
    @Transactional(readOnly = true)
    public void enviarEmailClientesCampanhaVacinacao() {

        logger.info("[enviarEmailClientesCampanhaVacinacao] - Inicio - Buscando clientes para envio de campanha.");

        try {

            List<Usuario> usuarios = usuarioRepository.findClientesAtivos();
            logger.info("[enviarEmailClientesCampanhaVacinacao] - Encontrados {} clientes ativos.", usuarios.size());

            for(Usuario usuario : usuarios) {

                try {

                    if(usuario.isReceberEmail()) {

                        logger.info("[enviarEmailClientesCampanhaVacinacao] - Enviando email para o usuário id {}", usuario.getId());
                        emailSender.enviarInformacaoCampanhaVacinaEmail(usuario);

                    } else {

                        logger.info("[enviarEmailClientesCampanhaVacinacao] - Usuário id {} não optou por receber emails.", usuario.getId());

                    }

                } catch (Exception e) {

                    logger.error("[enviarEmailClientesCampanhaVacinacao] - Falha ao enviar email de campanha para o usuário id {}: {}", usuario.getId(), e.getMessage());
                }
            }
            logger.info("[enviarEmailClientesCampanhaVacinacao] - Fim - Processo de envio de campanha concluído.");

        } catch (Exception e) {

            logger.error("[enviarEmailClientesCampanhaVacinacao] - Fim - Erro crítico ao buscar usuários para campanha: {}", e.getMessage(), e);

        }
    }
}