package uz.project.jorasoft.services;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public String sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@gmail.com"); // Ensure this is the same as the configured username
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        return to;
    }
    public String sendActivationEmail(String to, String username, String activationLink) throws MessagingException {
        String emailContent = generateEmailContent(username, activationLink);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Activate Your Account");
        helper.setText(emailContent, true);  // true -> HTML format

        mailSender.send(message);

        return "Successfully sent activation link to email : " +username+" ";
    }

    private String generateEmailContent(String username, String activationLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Account Activation</title>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                        .container { max-width: 600px; background: #ffffff; padding: 20px; border-radius: 8px; 
                                     box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); text-align: center; }
                        .button { display: inline-block; padding: 12px 20px; font-size: 16px; color: #ffffff; 
                                  background: #007bff; text-decoration: none; border-radius: 5px; margin-top: 20px; }
                        .footer { margin-top: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>Activate Your Account</h2>
                        <p>Hello, <strong>%s</strong>,</p>
                        <p>Thank you for signing up! Please click the button below to activate your account:</p>
                        <a href="%s" class="button">Activate Account</a>
                        <p>If the button above does not work, you can also click this link:</p>
                        <p><a href="%s">%s</a></p>
                        <p class="footer">If you did not sign up for this account, please ignore this email.</p>
                    </div>
                </body>
                </html>
                """.formatted(username, activationLink, activationLink, activationLink);
    }

}

