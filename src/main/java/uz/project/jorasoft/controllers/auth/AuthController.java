package uz.project.jorasoft.controllers.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.project.jorasoft.controllers.auth.signin.SigninDto;
import uz.project.jorasoft.controllers.auth.signup.ActivateRequestDTO;
import uz.project.jorasoft.controllers.auth.signup.ConfirmResetPasswordDTO;
import uz.project.jorasoft.controllers.auth.signup.SignupDto;
import uz.project.jorasoft.dtos.CheckUserExistaceDto;
import uz.project.jorasoft.services.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping ("/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupDto dto){
        return authService.signup(dto);
    }
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody SigninDto dto){  return authService.signin(dto); }
    @GetMapping("/checkup")
    public ResponseEntity<?> checkup(@RequestBody CheckUserExistaceDto dto){
        return ResponseEntity.ok(authService.checkExistance(dto));
    }
    @PostMapping("/code/send-again")
    public ResponseEntity<?> activateSendAgain(@RequestParam String email, @RequestParam String type){
     return  ResponseEntity.ok(authService.activateSendAgain(email,type));
    }
    @PostMapping("/activate")
    public ResponseEntity<?> activate(@RequestBody ActivateRequestDTO activateRequestDTO){
        return ResponseEntity.ok(authService.activate(activateRequestDTO));
    }
    @GetMapping("/password-reset-request")
    public ResponseEntity<?> requestResetPassword(@RequestParam String email){
        return ResponseEntity.ok(authService.resetPasswordRequest(email));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<?> confirmResetPassword(@RequestBody ConfirmResetPasswordDTO dto){
        return ResponseEntity.ok(authService.resetPassword(dto));
    }
}
