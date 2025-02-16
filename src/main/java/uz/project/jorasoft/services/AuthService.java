package uz.project.jorasoft.services;

import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.project.jorasoft.config.security.JwtProvider;
import uz.project.jorasoft.responces.ExceptionResponse;
import uz.project.jorasoft.responces.JwtResponse;
import uz.project.jorasoft.domains.User;
import uz.project.jorasoft.dtos.CheckUserExistaceDto;
import uz.project.jorasoft.controllers.auth.signin.SigninDto;
import uz.project.jorasoft.controllers.auth.signup.SignupDto;
import uz.project.jorasoft.repositories.RoleRepository;
import uz.project.jorasoft.repositories.UserRepository;
import uz.project.jorasoft.responces.CheckUserExistanceResponse;
import uz.project.jorasoft.responces.Response;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public User getUserEntity(SignupDto dto){
        System.out.println(dto.phoneNumber());
        return User.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .phoneNumber(dto.phoneNumber())
                .birthDate(dto.birthDate())
                .gender(dto.gender())
                .build();
    }

    public ResponseEntity<Response> signup(SignupDto dto)  {
        CheckUserExistanceResponse userExistanceResponse = checkExistance(new CheckUserExistaceDto(dto.phoneNumber(), dto.email()));
        if (!userExistanceResponse.getExists()) {
            User user = getUserEntity(dto);
            if (user != null) {
            user.setRoles(Collections.singletonList(roleRepository.findByName("USER").get()));
            userRepository.save(user);
            String token = jwtProvider.generate(user);
            return new ResponseEntity<>(new JwtResponse(token), HttpStatus.OK);
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
    public JwtResponse refreshToken(User user){
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
            return  new CheckUserExistanceResponse(userRepository.existsByEmail(email),"",email);
        }
        return new CheckUserExistanceResponse(false, "Phone number or email is free", dto.email()+" or "+dto.phoneNumber());
    }
}
