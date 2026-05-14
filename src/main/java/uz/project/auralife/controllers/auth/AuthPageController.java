package uz.project.auralife.controllers.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthPageController {

    @GetMapping("/auth/page/login")
    public String loginPage(
            @RequestParam(required = false) String username, 
            @RequestParam(required = false) String redirect_uri, 
            @RequestParam(required = false) String app_id,
            Model model, 
            HttpSession session) {
        if (username != null) {
            model.addAttribute("username", username);
        }
        if (redirect_uri != null) {
            session.setAttribute("redirect_uri", redirect_uri);
            model.addAttribute("redirect_uri", redirect_uri);
        } else {
            model.addAttribute("redirect_uri", session.getAttribute("redirect_uri"));
        }
        if (app_id != null) {
            session.setAttribute("app_id", app_id);
            model.addAttribute("app_id", app_id);
        } else {
            model.addAttribute("app_id", session.getAttribute("app_id"));
        }
        return "login";
    }

    @GetMapping("/auth/page/signup")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/auth/page/account-chooser")
    public String accountChooserPage(
            @RequestParam(required = false) String app_id, 
            @RequestParam(required = false) String redirect_uri,
            Model model, 
            HttpSession session) {
        if (app_id != null) {
            session.setAttribute("app_id", app_id);
            model.addAttribute("app_id", app_id);
        } else {
            model.addAttribute("app_id", session.getAttribute("app_id"));
        }
        if (redirect_uri != null) {
            session.setAttribute("redirect_uri", redirect_uri);
            model.addAttribute("redirect_uri", redirect_uri);
        } else {
            model.addAttribute("redirect_uri", session.getAttribute("redirect_uri"));
        }
        return "account-chooser";
    }

    @GetMapping("/auth/page/activate")
    public String activatePage(@RequestParam(required = false) String email, Model model) {
        if (email != null) {
            model.addAttribute("email", email);
        }
        return "activate";
    }

    @GetMapping("/auth/page/logout")
    public String logoutPage(@RequestParam(required = false) String email, @RequestParam(required = false) String redirect_uri, Model model) {
        if (email != null) {
            model.addAttribute("email", email);
        }
        if (redirect_uri != null) {
            model.addAttribute("redirect_uri", redirect_uri);
        }
        return "logout";
    }

    @GetMapping("/auth/page/terms")
    public String termsPage() {
        return "terms-of-use";
    }
}
