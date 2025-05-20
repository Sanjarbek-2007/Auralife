package uz.project.auralife.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.project.auralife.services.EmailService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/try")
public class TryController {
    private final EmailService emailService;
    @GetMapping
    public String tryController() {
        return "Hello World";
    }
    @PostMapping("/send")
    public String sendEmail(@RequestParam String to,
                            @RequestParam String subject,
                            @RequestParam String body) {
        log.info("Sending email to " + to);
        emailService.sendEmail(to, subject, body);
        return "Email sent successfully to " + to;
    }
}
