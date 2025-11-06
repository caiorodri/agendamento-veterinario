package br.com.caiorodri.agendamentoveterinario.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import br.com.caiorodri.agendamentoveterinario.email.EmailSender;
import br.com.caiorodri.agendamentoveterinario.enums.DiaSemanaEnum;
import br.com.caiorodri.agendamentoveterinario.model.*;
import br.com.caiorodri.agendamentoveterinario.repository.AgendamentoRepository;
import br.com.caiorodri.agendamentoveterinario.repository.StatusRepository;
import br.com.caiorodri.agendamentoveterinario.repository.VeterinarioHorarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private StatusRepository statusRepository;

//    @Autowired
//    private EmailSender emailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VeterinarioHorarioRepository veterinarioHorarioRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    final static Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    final String NOME_RECEPCIONISTA_AUTO_ATENDIMENTO = "AUTO ATENDIMENTO";

    final Integer ID_VETERINARIO = 2;

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

//            emailSender.enviarInformacaoCadastroUsuarioEmail(usuarioSalvo);

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

            if(usuario.getUrlImagem() != null){

                usuarioExistente.setUrlImagem(usuario.getUrlImagem());

            }

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
     * Lista todos os clientes com paginação.
     *
     * @param pageable Dados de paginação.
     * @return Page com clientes.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar os clientes.
     */
    @Transactional(readOnly = true)
    public Page<Usuario> listarClientes(Pageable pageable) {

        logger.info("[listarClientes] - Inicio - Listando clientes: página = {}, tamanho = {}", pageable.getPageNumber(), pageable.getPageSize());

        try {

            Page<Usuario> clientes = usuarioRepository.findClientes(pageable);

            logger.info("[listarClientes] - Fim - Encontrados {} clientes no total.", clientes.getTotalElements());

            return clientes;

        } catch (Exception e) {

            logger.error("[listarClientes] - Fim - Erro inesperado ao listar clientes: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar clientes", e);

        }
    }

    /**
     * Lista todos os usuários com perfil de recepcionista.
     *
     * @return List com recepcionistas.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar os recepcionistas.
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarRecepcionistas() {

        logger.info("[listarRecepcionistas] - Inicio - Buscando todos os recepcionistas.");

        try {

            List<Usuario> listaRecepcionistas = usuarioRepository.findRecepcionista();

            logger.info("[listarRecepcionistas] - Fim - Busca concluída. Encontrados {} recepcionistas.", listaRecepcionistas.size());

            return listaRecepcionistas;

        } catch (Exception e) {

            logger.error("[listarRecepcionistas] - Fim - Erro inesperado ao listar recepcionistas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar recepcionistas", e);

        }
    }

    /**
     * Retorna usuário com perfil de recepcionista e com nome AUTO ATENDIMENTO.
     *
     * @return Usuario.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar os recepcionistas.
     */
    @Transactional(readOnly = true)
    public Usuario recuperarRecepcionistaAutoAtendimento() {

        logger.info("[recuperarRecepcionistaAutoAtendimento] - Inicio - Buscando recepcionista de auto atendimento.");

        try {

            List<Usuario> listaRecepcionistas = usuarioRepository.findRecepcionista();

            for(Usuario recepcionista : listaRecepcionistas){

                if(recepcionista.getNome().equals(NOME_RECEPCIONISTA_AUTO_ATENDIMENTO)){

                    logger.info("[recuperarRecepcionistaAutoAtendimento] - Fim - Busca concluída. Recepcionista AUTO ATENDIMENTO encontrado com sucesso");

                    return recepcionista;

                }

            }

            logger.warn("[recuperarRecepcionistaAutoAtendimento] - Fim - Recepcionista AUTO ATENDIMENTO não encontrado");

            return null;

        } catch (Exception e) {

            logger.error("[listarRecepcionistas] - Fim - Erro inesperado ao listar recepcionistas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar recepcionistas", e);

        }
    }

    /**
     * Lista todos os usuários com perfil de veterinário.
     *
     * @return List com veterinários.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar os veterinários.
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarVeterinarios() {

        logger.info("[listarVeterinarios] - Inicio - Buscando todos os veterinários.");

        try {

            List<Usuario> listaVeterinarios = usuarioRepository.findVeterinarios();

            logger.info("[listarVeterinarios] - Fim - Busca concluída. Encontrados {} veterinários.", listaVeterinarios.size());

            return listaVeterinarios;

        } catch (Exception e) {

            logger.error("[listarVeterinarios] - Fim - Erro inesperado ao listar veterinários: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar veterinários", e);

        }
    }

    /**
     * Lista todos os funcionários (Recepcionistas e Veterinários) com paginação.
     *
     * @param pageable Dados de paginação.
     * @return Page com funcionários.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar os funcionários.
     */
    @Transactional(readOnly = true)
    public Page<Usuario> listarFuncionarios(Pageable pageable) {

        logger.info("[listarFuncionarios] - Inicio - Listando funcionários: página = {}, tamanho = {}", pageable.getPageNumber(), pageable.getPageSize());

        try {

            Page<Usuario> funcionarios = usuarioRepository.findFuncionarios(pageable);

            logger.info("[listarFuncionarios] - Fim - Encontrados {} funcionários no total.", funcionarios.getTotalElements());

            return funcionarios;

        } catch (Exception e) {

            logger.error("[listarFuncionarios] - Fim - Erro inesperado ao listar funcionários: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar funcionários", e);

        }
    }

    /**
     * Lista todos os funcionários (Recepcionistas e Veterinários).
     *
     * @return List com funcionários.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar os funcionários.
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarFuncionarios() {

        logger.info("[listarFuncionarios] - Inicio - Buscando todos os funcionários.");

        try {

            List<Usuario> listaFuncionarios = usuarioRepository.findFuncionarios();

            logger.info("[listarFuncionarios] - Fim - Busca concluída. Encontrados {} funcionários.", listaFuncionarios.size());

            return listaFuncionarios;

        } catch (Exception e) {

            logger.error("[listarFuncionarios] - Fim - Erro inesperado ao listar funcionários: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar funcionários", e);

        }
    }

    /**
     * Lista todos os estados (UF) do Brasil.
     *
     * @return List com estados.
     * @throws RuntimeException se ocorrer um erro inesperado ao consultar os estados.
     */
    @Transactional(readOnly = true)
    public List<Estado> listarEstados() {

        logger.info("[listarEstados] - Inicio - Buscando todos os estados.");

        try {

            List<Estado> listaEstados = usuarioRepository.findEstados();

            logger.info("[listarEstados] - Fim - Busca concluída. Encontrados {} estados.", listaEstados.size());

            return listaEstados;

        } catch (Exception e) {

            logger.error("[listarEstados] - Fim - Erro inesperado ao listar estados: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar estados", e);

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

//            emailSender.enviarCodigoEmail(email);

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
//                        emailSender.enviarInformacaoCampanhaVacinaEmail(usuario);

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

    @Transactional(readOnly = true)
    public List<VeterinarioHorario> listarHorariosVeterinario(Long idVeterinario) {
        logger.info("[listarHorariosVeterinario] - Inicio - Buscando horários para o veterinário ID: {}", idVeterinario);

        Usuario vet = usuarioRepository.findById(idVeterinario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário com ID " + idVeterinario + " não encontrado."));

        if (!Objects.equals(vet.getPerfil().getId(), ID_VETERINARIO)){
            throw new IllegalArgumentException("Usuário com ID " + idVeterinario + " não é um veterinário.");
        }

        List<VeterinarioHorario> horarios = veterinarioHorarioRepository.findByVeterinarioId(idVeterinario);

        logger.info("[listarHorariosVeterinario] - Fim - Encontrados {} blocos de horário.", horarios.size());

        return horarios;

    }

    @Transactional(readOnly = true)
    public List<String> listarHorariosDisponiveis(Long idVeterinario, LocalDate data, int duracaoSlotsMinutos) {
        logger.info("[listarHorariosDisponiveis] - Inicio - Buscando slots para Vet ID: {} na data: {}", idVeterinario, data);

        int idDiaSemana = DiaSemanaEnum.from(data.getDayOfWeek());

        List<VeterinarioHorario> blocosDeTrabalho = veterinarioHorarioRepository.findByVeterinarioIdAndDiaDaSemanaId(idVeterinario, idDiaSemana);

        if (blocosDeTrabalho.isEmpty()) {
            logger.warn("[listarHorariosDisponiveis] - Fim - Veterinário ID: {} não trabalha neste dia da semana (ID {}).", idVeterinario, idDiaSemana);
            return new ArrayList<>();
        }

        List<LocalTime> slotsPossiveis = new ArrayList<>();
        for (VeterinarioHorario bloco : blocosDeTrabalho) {
            LocalTime slotAtual = bloco.getHoraInicio();
            while (slotAtual.isBefore(bloco.getHoraFim())) {
                slotsPossiveis.add(slotAtual);
                slotAtual = slotAtual.plusMinutes(duracaoSlotsMinutos);
            }
        }

        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.plusDays(1).atStartOfDay();

        List<Agendamento> agendamentosOcupados = agendamentoRepository.findAgendamentosByVeterinarioNaData(
                idVeterinario,
                inicioDoDia,
                fimDoDia,
                2
        );

        List<LocalTime> horariosOcupados = agendamentosOcupados.stream()
                .map(ag -> ag.getDataAgendamentoInicio().toLocalTime())
                .toList();

        LocalTime agora = LocalTime.now();
        boolean isHoje = data.isEqual(LocalDate.now());

        List<String> horariosDisponiveis = slotsPossiveis.stream()
                .filter(slot -> !horariosOcupados.contains(slot))
                .filter(slot -> !isHoje || slot.isAfter(agora))
                .map(LocalTime::toString)
                .collect(Collectors.toList());

        logger.info("[listarHorariosDisponiveis] - Fim - Encontrados {} slots disponíveis.", horariosDisponiveis.size());
        return horariosDisponiveis;
    }

}