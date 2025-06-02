package uz.project.auralife.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.project.auralife.config.security.JwtProvider;
import uz.project.auralife.controllers.auth.signup.ActivateRequestDTO;
import uz.project.auralife.controllers.auth.signup.ConfirmResetPasswordDTO;
import uz.project.auralife.domains.ActivisationCode;
import uz.project.auralife.domains.enums.Apps;
import uz.project.auralife.domains.enums.CodeTypes;
import uz.project.auralife.dtos.ProfileDTO;
import uz.project.auralife.repositories.ActivisationCodeRepository;
import uz.project.auralife.responces.ExceptionResponse;
import uz.project.auralife.responces.JwtResponse;
import uz.project.auralife.domains.User;
import uz.project.auralife.dtos.CheckUserExistaceDto;
import uz.project.auralife.controllers.auth.signin.SigninDto;
import uz.project.auralife.controllers.auth.signup.SignupDto;
import uz.project.auralife.repositories.RoleRepository;
import uz.project.auralife.repositories.UserRepository;
import uz.project.auralife.responces.CheckUserExistanceResponse;
import uz.project.auralife.responces.Response;

import org.springframework.beans.factory.annotation.Value;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ActivisationCodeRepository activationCodeRepository;
    private final EmailService emailService;
    private CodeTypes codeType;

    @Value("${api.url}")
    private String link;

    public User getUserEntity(SignupDto dto){
        System.out.println(dto.phoneNumber());
        StringBuilder apps = new StringBuilder();
        for(Apps app : Apps.values()){
            if(dto.app().equals(app.toString())){
                apps.append(app.toString());
            }
        }
        return User.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .phoneNumber(dto.phoneNumber())
                .birthDate(dto.birthDate())
                .gender(dto.gender())
                .status("non-active")
                .apps(apps.toString())
                .build();
    }
    public static String generateCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int digit = random.nextInt(10); // Generates a digit from 0 to 9
            code.append(digit);
        }
        return code.toString();
    }
    public ResponseEntity<Response> signup(SignupDto dto)  {
        CheckUserExistanceResponse userExistanceResponse = checkExistance(new CheckUserExistaceDto(dto.phoneNumber(), dto.email()));

        if (!userExistanceResponse.getExists()) {
            User user = getUserEntity(dto);
            if (user != null) {
            user.setRoles(Collections.singletonList(roleRepository.findByName("USER").get()));
            userRepository.save(user);
            String token = jwtProvider.generate(user);
            activationSender(dto.email(), CodeTypes.ACCOUNT_ACTIVISION);
            return ResponseEntity.ok(new JwtResponse(token, "Activation code has been sent to your account."));
        }
        }
        return new ResponseEntity<>(new ExceptionResponse("User already exists, change phone number or email or both",
                                                            "", "Email or phone number is already exists",
                                          "",""  ),HttpStatus.CONFLICT);
    }
    public ResponseEntity<JwtResponse> signin(SigninDto signinDto){
        Optional<User> user = userRepository.findByPhoneNumber(signinDto.phoneNumber());
        if (user.isPresent() && passwordEncoder.matches(signinDto.password(), user.get().getPassword())){
            String token = jwtProvider.generate(user.orElse(null));
            return new ResponseEntity<>(new JwtResponse(token), HttpStatus.OK);
        }
        return null;
    }
    public JwtResponse refreshCode(User user){
            String token = jwtProvider.generate(user );
            return new JwtResponse(token);
    }
    public CheckUserExistanceResponse checkExistance(CheckUserExistaceDto dto) {
        if(dto.phoneNumber().isEmpty() && dto.email().isEmpty()){
            return new CheckUserExistanceResponse(null, "Please enter a valid phone number or email",null);
        }
        if (!dto.phoneNumber().isEmpty()){
            String phoneNumber = dto.phoneNumber();
            return  new CheckUserExistanceResponse(userRepository.existsByPhoneNumber(phoneNumber),"possible to sing in",phoneNumber);

        }
        if (!dto.email().isEmpty()) {
            String email = dto.email();
            return  new CheckUserExistanceResponse(userRepository.existsByEmail(email),"email ",email);
        }
        return new CheckUserExistanceResponse(false, "Phone number or email is free", dto.email()+" or "+dto.phoneNumber());
    }
    public ResponseEntity<?> activate(ActivateRequestDTO code) {
        Response response = new Response();
        activationCodeRepository.findByRecieverEmail(code.email()).ifPresent(activationCode -> {
            if(activationCode.getCode().equals(code.code())&&checkExpiry(activationCode.getExpityDateTime())){
                activationCodeRepository.delete(activationCode);
                userRepository.updateStatusByEmail("active", activationCode.getRecieverEmail());

                response.setMessage("Successfully activated account with email : "+code.email());
                response.setStatus(200);
            } else {
                if (!checkExpiry(activationCode.getExpityDateTime())) {
                    activationCodeRepository.delete(activationCode);
                }
                response.setMessage("Could not find user with or expired code : "+code.email());
                response.setStatus(404);
            }
    });
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    private boolean checkExpiry( String expityDateTime) {
        LocalDateTime expiry = LocalDateTime.parse(expityDateTime);
        return LocalDateTime.now().isBefore(expiry);
    }

    public ResponseEntity<?> resetPasswordRequest(String email) {
        boolean exists = userRepository.existsByEmail(email);

//        ActivisationCode activisationCode = activationCodeRepository.findByRecieverEmailAndType(email, CodeTypes.PASSWORD_RESET.getType()).get();
//        if(activisationCode==null){
//            return ResponseEntity.ok(new Response(309,"Failed to generate activation code" ));
//        }
        if (exists) {
            activationSender(email, CodeTypes.PASSWORD_RESET);

            return ResponseEntity.ok("We send email with code to : "+email+" check your email. ");
        }
        return ResponseEntity.ok("User with email : "+email+" does not exist : Existance " + exists);

    }

    public ResponseEntity<?> resetPassword(ConfirmResetPasswordDTO dto) {
        Response response = new Response();
        activationCodeRepository.findByRecieverEmail(dto.email()).ifPresent(activationCode -> {
            if(activationCode.getCode().equals(dto.code())&&checkExpiry(activationCode.getExpityDateTime())){
                activationCodeRepository.delete(activationCode);
                String password = passwordEncoder.encode(dto.password());
                userRepository.updatePasswordByEmail(password, dto.email() );

                response.setMessage("Successfully changed the password of account with email : "+dto.email());
                response.setStatus(200);
            }else {
                if (!checkExpiry(activationCode.getExpityDateTime())) {
                    activationCodeRepository.delete(activationCode);
                    response.setMessage("Failed to activate code has been expired of user : "+ dto.email());
                }else{
                    response.setMessage("Could not find user with email : "+ dto.email());
                    response.setStatus(404);
                }
            }
        });
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    private Boolean activationSender(String email, CodeTypes type) {
        ActivisationCode activisationCode = new ActivisationCode(
                email,
                generateCode(6),
                false,
                LocalDateTime.now().plusMinutes(5l).toString(),
                type.getType()
        );
        String messageCode = "Your " + type.getType() +" code is "+activisationCode.getCode() +
                "\n You can use this before expiry time of 5 minutes ends." + activisationCode.getExpityDateTime()+" ";
        //                     emailMessage = emailService.sendActivationEmail(dto.email(), dto.firstName(), link + "/auth/activate?email=" + dto.email() + "&token=" + activisationCode + "&date" + LocalDateTime.now());
        String emailMessage = emailService.sendEmail(email, email, messageCode+"\nGo to " + link +" To get access to JORASOFT");
        activationCodeRepository.save(activisationCode);
        return true;
    }

    public ResponseEntity<?> activateSendAgain(String email, String type) {
        if(type.equals(CodeTypes.ACCOUNT_ACTIVISION.getType())){
            if (userRepository.existsByEmailAndStatusIgnoreCase(email, "active")) {
                return ResponseEntity.ok("You have already activated your account");
            }
        }
        Optional<ActivisationCode> activeCode = activationCodeRepository.findByRecieverEmail(email);
        String expityDateTime = activeCode.get().getExpityDateTime();
        LocalDateTime expiry = LocalDateTime.parse(expityDateTime);

        if (expiry.minusMinutes(3l).isAfter(LocalDateTime.now())) {}
        activeCode.ifPresent(activationCode -> {

            activationCodeRepository.delete(activationCode);
            activationSender(email, codeType.getCodeType(type));
        });
        return ResponseEntity.ok("We send " + type+" code "+"account with email : " + email);
    }



}


