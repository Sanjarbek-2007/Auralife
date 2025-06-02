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
    @GetMapping("/1")
    public String try1Controller() {
        return "Hello22222222222222222222222222222 World doniyor gay";
    } @GetMapping("/sunnat-bratishka")
    public String trySunnatController() {
        return "       Sunnat decided to learn dating from a YouTube guru named Sergey—bald guy, leopard shirt, Bluetooth headset.\n" +
                "\n" +
                "       Day 1: Walked around with a rose in his mouth. Got sneezed on by a pigeon.\n" +
                "\n" +
                "       Day 3: Tried a “seduction stare” at a girl. Turned out to be his old math teacher. She gave him homework.\n" +
                "\n" +
                "       Day 5: Did the “Confused Alpha” move—told a girl, “Do I know you from destiny?” She said, “Yeah, 6th grade. You cried over a sandwich.”\n" +
                "\n" +
                "       Day 7: Rented a tux, smoke machine, sax player. Shouted in the food court: “I’m ready to love!”\n" +
                "\n" +
                "       Security escorted him out. But a girl laughed, said, “You're weird. I like that.”\n" +
                "\n" +
                "       Now they pretend to date just to annoy her ex. Sunnat didn’t find love—he found TikTok fame.\n" +
                "\n" +
                "Moral: Never trust a dating coach with Bluetooth.\n";
    }
    @GetMapping("/2")
    public String try2Controller() {
        return "Hellooooooooooooooooooooooo 2";
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
