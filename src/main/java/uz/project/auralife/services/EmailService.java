package uz.project.auralife.services;
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

    public String sendAuthCodeEmail(String to, String code, String type) throws MessagingException {
        String emailContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Your Verification Code</title>
                    <style>
                        body { font-family: 'Inter', Arial, sans-serif; background-color: #f4f6f8; margin: 0; padding: 40px 20px; }
                        .container { max-width: 500px; margin: 0 auto; background: #ffffff; padding: 40px; border-radius: 12px; 
                                     box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); text-align: center; }
                        h2 { color: #1a1a1a; margin-top: 0; }
                        p { color: #555; }
                        .code-box { display: inline-block; padding: 16px 32px; font-size: 28px; font-weight: bold; letter-spacing: 4px;
                                    color: #007bff; background: #f0f7ff; border: 1px dashed #007bff; border-radius: 8px; margin: 24px 0; }
                        .footer { margin-top: 30px; font-size: 12px; color: #888; border-top: 1px solid #eee; padding-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>Auralife Verification</h2>
                        <p>We received a request to verify your account.</p>
                        <p>Please enter the following 6-digit code:</p>
                        <div class="code-box">%s</div>
                        <p>This code will expire in 5 minutes.</p>
                        <p class="footer">If you did not request this code, please ignore this email.</p>
                    </div>
                </body>
                </html>
                """.formatted(code);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Auralife " + type + " Code");
        helper.setText(emailContent, true);

        mailSender.send(message);

        return "Successfully sent auth code to email : " + to;
    }

}
