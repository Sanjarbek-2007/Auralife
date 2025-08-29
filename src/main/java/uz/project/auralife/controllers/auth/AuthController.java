package uz.project.auralife.controllers.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.project.auralife.controllers.auth.signin.SigninDto;
import uz.project.auralife.controllers.auth.signup.ActivateRequestDTO;
import uz.project.auralife.controllers.auth.signup.ConfirmResetPasswordDTO;
import uz.project.auralife.controllers.auth.signup.SignupDto;
import uz.project.auralife.controllers.dto.UserApiAuthDto;
import uz.project.auralife.dtos.CheckUserExistaceDto;
import uz.project.auralife.dtos.ProfileDTO;
import uz.project.auralife.services.AuthService;
import uz.project.auralife.controllers.auth.signin.SigninByEmailDto;
import uz.project.auralife.services.ProfileService;

@RestController
@RequiredArgsConstructor
@RequestMapping ("/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final ProfileService profileService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupDto dto){
        return authService.signup(dto);
    }
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody SigninDto dto){  return authService.signin(dto); }
    @PostMapping("/signin-byemail")
    public ResponseEntity<?> signinByEmail(@RequestBody SigninByEmailDto dto){  return authService.signinByEmail(dto); }
    @PostMapping ("/checkup")
    public ResponseEntity<?> checkUp(@RequestBody CheckUserExistaceDto dto){
        return ResponseEntity.ok(authService.checkExistance(dto));
    }
    @GetMapping ("/check-up-by-id")
    public ResponseEntity<?> checkUpById(@RequestParam("userId") Long userId){
        Boolean body = authService.checkExistanceByUserId(userId);
        return ResponseEntity.ok(body);
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

//    @PostMapping("/get-profile-by-userId")
//    public ResponseEntity<?> getProfileByUserId(@RequestBody UserApiAuthDto dto){
//        return ResponseEntity.ok(authService.getUserById(dto));
//    }
    @PostMapping("/get-match")
    public Boolean userMatch(@RequestBody UserApiAuthDto dto){
        return authService.userMatch(dto);
    }


}
