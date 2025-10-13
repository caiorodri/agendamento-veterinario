package br.com.caiorodri.agendamentoveterinario.email;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import br.com.caiorodri.agendamentoveterinario.model.Agendamento;
import br.com.caiorodri.agendamentoveterinario.model.Animal;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;
import br.com.caiorodri.agendamentoveterinario.repository.UsuarioRepository;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailSender {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${spring.mail.username}")
    private String remetente;

    @Value("${endereco}")
    private String endereco;

    @Value("${contato}")
    private String contato;

    public boolean enviarCodigoEmail(String destinatario) {
        try {

            Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(destinatario);

            if (usuarioOptional.isEmpty()) {
                return false;
            }

            Usuario usuario = usuarioOptional.get();

            String codigo = gerarCodigo();

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String htmlContent = """
	            <!DOCTYPE html>
	            <html lang="pt-BR">
	            <head>
	                <meta charset="UTF-8">
	                <title>Código de Verificação</title>
	                <style>
	                    body {
	                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
	                        background-color: #f4f4f4;
	                        color: #333;
	                        padding: 30px;
	                    }
	                    .container {
	                        background-color: #fff;
	                        max-width: 500px;
	                        margin: auto;
	                        padding: 30px;
	                        border-radius: 10px;
	                        box-shadow: 0 0 12px rgba(0,0,0,0.1);
	                        text-align: center;
	                    }
	                    h2 {
	                        color: #2a9d8f;
	                    }
	                    .codigo {
	                        font-size: 32px;
	                        font-weight: bold;
	                        color: #264653;
	                        background-color: #e0f7fa;
	                        padding: 10px 20px;
	                        display: inline-block;
	                        border-radius: 8px;
	                        margin: 20px 0;
	                        letter-spacing: 4px;
	                    }
	                    p {
	                        font-size: 16px;
	                    }
	                    .footer {
	                        margin-top: 30px;
	                        font-size: 14px;
	                        color: #888;
	                    }
	                </style>
	            </head>
	            <body>
	                <div class="container">
	                    <h2>Seu código de verificação</h2>
	                    <p>Olá <strong>%s</strong>,</p>
	                    <p>Use o código abaixo para continuar com sua solicitação. Ele é válido por <strong>15 minutos</strong>:</p>
	                    <div class="codigo">%s</div>
	                    <p>Se você não solicitou este código, ignore este e-mail.</p>
	                    <div class="footer">
	                        Equipe AgenPet<br/>
	                        Atendimento ao Cliente
	                    </div>
	                </div>
	            </body>
	            </html>
	        """.formatted(usuario.getNome(), codigo);

            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject("Seu código de verificação - AgenPet");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);

            usuario.setCodigoRecuperacao(codigo);
            usuario.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(15));
            usuarioRepository.save(usuario);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean enviarInformacaoCadastroUsuarioEmail(Usuario usuario) {

        try {

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String titulo = "Cadastro Realizado com Sucesso!";
            String assunto = "Bem-vindo(a) à AgenPet 🐶🐱";

            String htmlContent = """
	            <!DOCTYPE html>
	            <html lang="pt-BR">
	            <head>
	                <meta charset="UTF-8">
	                <title>%s</title>
	                <style>
	                    body {
	                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
	                        background-color: #f4f4f4;
	                        color: #333;
	                        padding: 30px;
	                    }
	                    .container {
	                        background-color: #ffffff;
	                        max-width: 600px;
	                        margin: auto;
	                        padding: 30px;
	                        border-radius: 10px;
	                        box-shadow: 0 0 12px rgba(0, 0, 0, 0.1);
	                        text-align: center;
	                    }
	                    h2 {
	                        color: #2a9d8f;
	                    }
	                    p {
	                        font-size: 16px;
	                        line-height: 1.6;
	                    }
	                    .footer {
	                        margin-top: 30px;
	                        font-size: 14px;
	                        color: #777;
	                    }
	                    .highlight {
	                        background-color: #e0f7fa;
	                        padding: 10px;
	                        border-radius: 8px;
	                        display: inline-block;
	                        font-weight: bold;
	                    }
	                </style>
	            </head>
	            <body>
	                <div class="container">
	                    <h2>%s</h2>
	                    <p>Olá <strong>%s</strong>,</p>
	                    <p>Seja muito bem-vindo(a) à <strong>AgenPet</strong>, o sistema de agendamento que cuida do seu pet com carinho e praticidade.</p>
	                    <p>Seu cadastro foi realizado com sucesso em nossa plataforma. Agora você pode agendar consultas, acompanhar seus atendimentos e receber lembretes diretamente pelo seu e-mail.</p>
	                    <p class="highlight">Estamos felizes em ter você com a gente! 🐾</p>
	                    <p>Caso precise de ajuda, entre em contato com nosso suporte:</p>
	                    <p><strong>📍 %s</strong><br><strong>📞 %s</strong></p>
	                    <div class="footer">
	                        Atenciosamente,<br>
	                        <strong>Equipe AgenPet</strong>
	                    </div>
	                </div>
	            </body>
	            </html>
	        """.formatted(
                    titulo,
                    titulo,
                    usuario.getNome(),
                    endereco,
                    contato
            );

            helper.setFrom(remetente);
            helper.setTo(usuario.getEmail());
            helper.setSubject(assunto);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean enviarInformacaoCadastroAnimalEmail(Animal animal, Boolean atualizar) {

        try {

            Usuario dono = animal.getDono();

            if(dono.getEmail() == null) {

                dono = usuarioRepository.findById(dono.getId()).orElse(null);

                if(dono == null) {

                    return false;

                }

            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String titulo = atualizar ? "Informações do Animal Atualizadas com Sucesso!"
                    : "Animal Cadastrado com Sucesso!";
            String assunto = atualizar ? "Atualização de informações do seu Pet 🐾"
                    : "Seu Pet agora faz parte da AgenPet 🐾";

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String dataNascimentoFormatada = (animal.getDataNascimento() != null)
                    ? sdf.format(animal.getDataNascimento())
                    : "Não informada";

            String htmlContent = """
	            <!DOCTYPE html>
	            <html lang="pt-BR">
	            <head>
	                <meta charset="UTF-8">
	                <title>%s</title>
	                <style>
	                    body {
	                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
	                        background-color: #f4f4f4;
	                        color: #333;
	                        padding: 30px;
	                    }
	                    .container {
	                        background-color: #ffffff;
	                        max-width: 600px;
	                        margin: auto;
	                        padding: 30px;
	                        border-radius: 10px;
	                        box-shadow: 0 0 12px rgba(0, 0, 0, 0.1);
	                        text-align: center;
	                    }
	                    h2 {
	                        color: #2a9d8f;
	                    }
	                    p {
	                        font-size: 16px;
	                        line-height: 1.6;
	                    }
	                    table {
	                        width: 100%%;
	                        margin-top: 20px;
	                        border-collapse: collapse;
	                    }
	                    td {
	                        padding: 10px;
	                        border: 1px solid #e0e0e0;
	                        background-color: #f9f9f9;
	                    }
	                    td:first-child {
	                        font-weight: bold;
	                        background-color: #f1f1f1;
	                        width: 40%%;
	                    }
	                    .footer {
	                        margin-top: 30px;
	                        font-size: 14px;
	                        color: #777;
	                    }
	                </style>
	            </head>
	            <body>
	                <div class="container">
	                    <h2>%s</h2>
	                    <p>Olá <strong>%s</strong>,</p>
	                    <p>As informações do seu pet <strong>%s</strong> %s com sucesso em nosso sistema.</p>
	                    <table>
	                        <tr>
	                            <td>🐾 Nome do Animal</td>
	                            <td>%s</td>
	                        </tr>
	                        <tr>
	                            <td>📅 Data de Nascimento</td>
	                            <td>%s</td>
	                        </tr>
	                        <tr>
	                            <td>📘 Espécie</td>
	                            <td>%s</td>
	                        </tr>
	                        <tr>
	                            <td>⚖️ Peso</td>
	                            <td>%.2f kg</td>
	                        </tr>
	                        <tr>
	                            <td>⚖️ Altura</td>
	                            <td>%.1f cm</td>
	                        </tr>
	                    </table>
	                    <p class="footer">
	                        Caso tenha dúvidas ou precise de suporte, entre em contato conosco:<br>
	                        <strong>📍 %s</strong><br>
	                        <strong>📞 %s</strong><br><br>
	                        Atenciosamente,<br>
	                        <strong>Equipe AgenPet</strong>
	                    </p>
	                </div>
	            </body>
	            </html>
	        """.formatted(
                    titulo,
                    titulo,
                    dono.getNome(),
                    animal.getNome(),
                    atualizar ? "foram atualizadas" : "foi cadastrado",
                    animal.getNome(),
                    dataNascimentoFormatada,
                    animal.getRaca() != null && animal.getRaca().getEspecie() != null ? animal.getRaca().getEspecie().getNome() : "Não informada",
                    animal.getPeso(),
                    animal.getAltura(),
                    endereco,
                    contato
            );

            helper.setFrom(remetente);
            helper.setTo(dono.getEmail());
            helper.setSubject(assunto);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean enviarInformacaoCadastroAgendamentoEmail(Agendamento agendamento, Boolean atualizacao) {

        try {

            Usuario cliente = agendamento.getCliente();

            if(cliente.getEmail() == null) {

                cliente = usuarioRepository.findById(cliente.getId()).orElse(null);

                if(cliente == null) {

                    return false;

                }

            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String titulo = atualizacao
                    ? "Agendamento Atualizado. Verifique as informações!"
                    : "Agendamento Confirmado com Sucesso!";

            String assunto = atualizacao
                    ? "Agendamento Atualizado"
                    : "Agendamento Realizado";

            String htmlContent = """
	            <!DOCTYPE html>
	            <html lang="pt-BR">
	            <head>
	                <meta charset="UTF-8">
	                <title>%s</title>
	                <style>
	                    body {
	                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
	                        background-color: #f4f4f4;
	                        color: #333;
	                        margin: 0;
	                        padding: 20px;
	                    }
	                    .container {
	                        background-color: #ffffff;
	                        max-width: 600px;
	                        margin: auto;
	                        border-radius: 10px;
	                        padding: 30px;
	                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
	                        border: 1px solid #e0e0e0;
	                    }
	                    h2 {
	                        color: #2a9d8f;
	                        margin-bottom: 20px;
	                    }
	                    p {
	                        line-height: 1.6;
	                    }
	                    table {
	                        width: 100%%;
	                        border-collapse: collapse;
	                        margin-top: 15px;
	                    }
	                    td {
	                        padding: 10px;
	                        border: 1px solid #e0e0e0;
	                        background-color: #f9f9f9;
	                    }
	                    td:first-child {
	                        font-weight: bold;
	                        background-color: #f1f1f1;
	                        width: 40%%;
	                    }
	                    .footer {
	                        margin-top: 30px;
	                        font-size: 14px;
	                        color: #777;
	                    }
	                </style>
	            </head>
	            <body>
	                <div class="container">
	                    <h2>%s</h2>
	                    <p>Olá <strong>%s</strong>,</p>
	                    <p>Seu agendamento %s:</p>
	                    <table>
	                        <tr>
	                            <td>🐾 Animal</td>
	                            <td>%s</td>
	                        </tr>
	                        <tr>
	                            <td>📅 Data</td>
	                            <td>%s</td>
	                        </tr>
	                        <tr>
	                            <td>⏰ Horário</td>
	                            <td>%s</td>
	                        </tr>
	                        <tr>
	                            <td>👨‍⚕️ Veterinário</td>
	                            <td>%s</td>
	                        </tr>
	                        <tr>
	                            <td>🧑‍ Recepcionista</td>
	                            <td>%s</td>
	                        </tr>
	                        <tr>
	                            <td>📍 Endereço</td>
	                            <td>%s</td>
	                        </tr>
	                    </table>
	                    <p class="footer">
	                        Caso tenha dúvidas ou precise remarcar, entre em contato com nossa equipe.<br><strong>%s</strong><br><br>
	                        Atenciosamente,<br>
	                        <strong>Equipe AgenPet</strong>
	                    </p>
	                </div>
	            </body>
	            </html>
	            """.formatted(
                    titulo,
                    titulo,
                    cliente.getNome(),
                    atualizacao ? "foi atualizado" : "foi realizado",
                    agendamento.getAnimal().getNome(),
                    agendamento.getDataAgendamentoInicio().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    agendamento.getDataAgendamentoInicio().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    agendamento.getVeterinario().getNome(),
                    agendamento.getRecepcionista().getNome(),
                    endereco,
                    contato
            );

            helper.setFrom(remetente);
            helper.setTo(cliente.getEmail());
            helper.setSubject(assunto);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean enviarInformacaoRealizarConsultaEmail(Animal animal, Agendamento ultimaConsulta) {

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String titulo = "Já faz um tempo desde a última consulta...";
            String assunto = "Hora de cuidar da saúde do seu pet 🐶🐱";

            Usuario dono = animal.getDono();

            String htmlContent = """
	            <!DOCTYPE html>
	            <html lang="pt-BR">
	            <head>
	                <meta charset="UTF-8">
	                <title>%s</title>
	                <style>
	                    body {
	                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
	                        background-color: #f4f4f4;
	                        color: #333;
	                        padding: 30px;
	                    }
	                    .container {
	                        background-color: #ffffff;
	                        max-width: 600px;
	                        margin: auto;
	                        padding: 30px;
	                        border-radius: 10px;
	                        box-shadow: 0 0 12px rgba(0, 0, 0, 0.1);
	                        text-align: center;
	                    }
	                    h2 {
	                        color: #e76f51;
	                    }
	                    p {
	                        font-size: 16px;
	                        line-height: 1.6;
	                    }
	                    .footer {
	                        margin-top: 30px;
	                        font-size: 14px;
	                        color: #777;
	                    }
	                </style>
	            </head>
	            <body>
	                <div class="container">
	                    <h2>%s</h2>
	                    <p>Olá <strong>%s</strong>,</p>
	                    <p>Percebemos que já se passaram mais de <strong>6 meses</strong> desde a última consulta do seu pet <strong>%s</strong>.</p>
	                    <p>A última consulta foi em <strong>%s</strong>.</p>
	                    <p>Que tal agendar uma nova visita e garantir o bem-estar dele?</p>
	                    <p class="footer">
	                        Caso tenha dúvidas ou precise de suporte, entre em contato conosco:<br>
	                        <strong>📍 %s</strong><br>
	                        <strong>📞 %s</strong><br><br>
	                        Atenciosamente,<br>
	                        <strong>Equipe AgenPet</strong>
	                    </p>
	                </div>
	            </body>
	            </html>
	        """.formatted(
                    titulo,
                    titulo,
                    dono.getNome(),
                    animal.getNome(),
                    ultimaConsulta.getDataAgendamentoInicio().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    endereco,
                    contato
            );

            helper.setFrom(remetente);
            helper.setTo(dono.getEmail());
            helper.setSubject(assunto);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }

    }

    public boolean enviarInformacaoCampanhaVacinaEmail(Usuario usuario) {

        try {

            if(usuario.getEmail() == null) {

                usuario = usuarioRepository.findById(usuario.getId()).orElse(null);

                if(usuario == null) {

                    return false;

                }

            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String titulo = "Campanha de Vacinação para Pets";
            String assunto = "Proteja quem você ama! 🐾 Vacinação disponível";

            String htmlContent = """
	            <!DOCTYPE html>
	            <html lang="pt-BR">
	            <head>
	                <meta charset="UTF-8">
	                <title>%s</title>
	                <style>
	                    body {
	                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
	                        background-color: #f4f4f4;
	                        color: #333;
	                        padding: 30px;
	                    }
	                    .container {
	                        background-color: #ffffff;
	                        max-width: 600px;
	                        margin: auto;
	                        padding: 30px;
	                        border-radius: 10px;
	                        box-shadow: 0 0 12px rgba(0, 0, 0, 0.1);
	                        text-align: center;
	                    }
	                    h2 {
	                        color: #2a9d8f;
	                    }
	                    p {
	                        font-size: 16px;
	                        line-height: 1.6;
	                    }
	                    .footer {
	                        margin-top: 30px;
	                        font-size: 14px;
	                        color: #777;
	                    }
	                </style>
	            </head>
	            <body>
	                <div class="container">
	                    <h2>%s</h2>
	                    <p>Olá <strong>%s</strong>,</p>
	                    <p>Estamos realizando uma <strong>campanha especial de vacinação</strong> para cães e gatos!</p>
	                    <p>Essa é a oportunidade ideal para proteger a saúde do seu pet contra diversas doenças.</p>
	                    <p>As vacinas estão com condições especiais e o atendimento está sendo feito com agendamento prévio para seu conforto e segurança.</p>
	                    <p>Entre em contato conosco para mais informações ou agendar um horário.</p>
	                    <p class="footer">
	                        📍 <strong>%s</strong><br>
	                        📞 <strong>%s</strong><br><br>
	                        Atenciosamente,<br>
	                        <strong>Equipe AgenPet</strong>
	                    </p>
	                </div>
	            </body>
	            </html>
	        """.formatted(
                    titulo,
                    titulo,
                    usuario.getNome(),
                    endereco,
                    contato
            );

            helper.setFrom(remetente);
            helper.setTo(usuario.getEmail());
            helper.setSubject(assunto);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }

    }

    private String gerarCodigo() {

        Random random = new Random();

        return "" + random.nextInt(10000, 100000);
    }

}