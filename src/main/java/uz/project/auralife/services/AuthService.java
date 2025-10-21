package uz.project.auralife.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.project.auralife.config.UserContext;
import uz.project.auralife.config.security.JwtProvider;
import uz.project.auralife.controllers.auth.signin.SigninByEmailDto;
import uz.project.auralife.controllers.auth.signup.ActivateRequestDTO;
import uz.project.auralife.controllers.auth.signup.ConfirmResetPasswordDTO;
import uz.project.auralife.controllers.auth.signup.SignUpResponseDto;
import uz.project.auralife.controllers.dto.UserApiAuthDto;
import uz.project.auralife.domains.*;
import uz.project.auralife.domains.enums.Apps;
import uz.project.auralife.domains.enums.CodeTypes;
import uz.project.auralife.repositories.*;
import uz.project.auralife.responces.*;
import uz.project.auralife.dtos.CheckUserExistaceDto;
import uz.project.auralife.controllers.auth.signin.SigninDto;
import uz.project.auralife.controllers.auth.signup.SignupDto;

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
    private final PhotoRepository photoRepository;
    private final DeviceRepository deviceRepository;
    private final UserContext userContext;

    private CodeTypes codeType;

    @Value("${api.url}")
    private String link;

    public User getUserEntity(SignupDto dto) {
        System.out.println(dto.phoneNumber());
        StringBuilder apps = new StringBuilder();
        for (Apps app : Apps.values()) {
            if (dto.app().equals(app.toString())) {
                apps.append(app.toString());
            }
        }
        return User.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .username(dto.username())
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

    public ResponseEntity<Response> signup(SignupDto dto) {
        CheckUserExistanceResponse userExistanceResponse = checkExistance(new CheckUserExistaceDto(dto.phoneNumber(), dto.email(), dto.username()));

        if (!userExistanceResponse.getExists()) {
            User user = getUserEntity(dto);
            if (user != null) {
                user.setProfilePictures(List.of(getOrCreateDefaultPhoto(dto.gender())));
                rolesInit();
                user.setRoles(Collections.singletonList(roleRepository.findByName("USER").get()));
                User savedUser = userRepository.save(user);
                Device device = getOrRegisterDevice(dto.deviceName(),dto.deviceType(), savedUser.getId(), dto.app(),true);
                String token = jwtProvider.generate(user, device.getIotDeviceId());
                activationSender(dto.email(), CodeTypes.ACCOUNT_ACTIVISION.getType());
                return ResponseEntity.ok(new SignUpResponseDto(200,new JwtResponse(token, "Activation code has been sent to your account."),
                        device.getIotDeviceId(),
                        "Successfully registred, now go to your email and confirm activation code."));
            }
        }
        return new ResponseEntity<>(new ExceptionResponse("User already exists, change phone number or email or both",
                "", "Email or phone number is already exists",
                "", ""), HttpStatus.CONFLICT);
    }

    @Value("${api.url}")
    private String apiUrl;

    @Transactional
    public Photo getOrCreateDefaultPhoto(String gender) {
        // Normalize gender input
        String purposeName = gender.equalsIgnoreCase("male") ? "Male" : "Female";
        String fileName = gender.equalsIgnoreCase("male") ? "male.png" : "female.png";
        String filePath = "src/main/resources/icons/" + fileName;

        // Try to find existing photo ID without fetching the whole entity
        Optional<Photo> existingId = photoRepository.findByPurposeName(purposeName);
        if (existingId.isPresent()) {
            return existingId.get();
        }

        // Create and save if not found
        Photo saved = photoRepository.save(
                new Photo(
                        filePath,
                        gender.toLowerCase(),
                        apiUrl + "/photo/get?photoPath=" + filePath,
                        "Auralife",
                        purposeName
                )
        );
        return saved;
    }

    public ResponseEntity<SignInResponse> signin(SigninDto dto) {
        Optional<User> user = userRepository.findByPhoneNumber(dto.phoneNumber());
        return signIn(user, dto.password(), dto.deviceName(), dto.deviceType(), dto.app() ,false);
    }

    public ResponseEntity<SignInResponse> signinByEmail(SigninByEmailDto dto) {
        Optional<User> user = userRepository.findByEmail(dto.email());
        return signIn(user, dto.password(), dto.deviceName(), dto.deviceType(), dto.app(), false );
    }

    private ResponseEntity<SignInResponse> signIn(Optional<User> user, String password, String deviceName, String deviceType, String app, Boolean isPrime) {
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {

            Device device = getOrRegisterDevice(deviceName, deviceType, user.get().getId(), app, isPrime);
            String token = jwtProvider.generate(user.get(), device.getIotDeviceId() );
            return new ResponseEntity<>(new SignInResponse(
                    200, "Successfuly signed in as a device "+device.getIotDeviceId(),
                    token,device.getIotDeviceId()), HttpStatus.OK);
        }
        return null;
    }
    public Device getOrRegisterDevice(String deviceName, String deviceType, Long userId, String app, Boolean isPrime) {
        Optional<Device> device = deviceRepository
                .findByUserIdAndDeviceNameAndDeviceTypeAndPermittedApps(userId, deviceName, deviceType, app);

        return device.orElseGet(() -> deviceRepository.save(new Device(
                userId,
                isPrime,
                UUID.randomUUID().toString(),
                deviceName,
                deviceType,
                app,
                LocalDateTime.now(),
                LocalDateTime.now(),
                ""
        )));
    }


    public void quitFromDevice(){
        deviceRepository.deleteByIotDeviceId(userContext.getIotDeviceId());
    }
    public Boolean checkExistanceByUserId(Long userId) {
        return userRepository.existsById(userId);
    }

    public CheckUserExistanceResponse checkExistance(CheckUserExistaceDto dto) {
        if (dto.phoneNumber().isEmpty() && dto.email().isEmpty() && dto.username().isEmpty()) {
            return new CheckUserExistanceResponse(null, "Please enter a valid phone number or email", null);
        }
        if (!dto.phoneNumber().isEmpty()) {
            String phoneNumber = dto.phoneNumber();
            return new CheckUserExistanceResponse(userRepository.existsByPhoneNumber(phoneNumber), "possible to sing in", phoneNumber);

        }
        if (!dto.email().isEmpty()) {
            String email = dto.email();
            return new CheckUserExistanceResponse(userRepository.existsByEmail(email), "email ", email);
        }
        if (!dto.username().isEmpty()) {
            String username = dto.username();
            return new CheckUserExistanceResponse(userRepository.existsByUsername(username), "username", username);
        }
        return new CheckUserExistanceResponse(false, "Phone number or email is free", dto.email() + " or " + dto.phoneNumber());
    }

    public ResponseEntity<?> activate(ActivateRequestDTO code) {
        Response response = new Response();
        activationCodeRepository.findByRecieverEmail(code.email()).ifPresent(activationCode -> {
            if (activationCode.getCode().equals(code.code()) && checkExpiry(activationCode.getExpityDateTime())) {
                activationCodeRepository.delete(activationCode);
                userRepository.updateStatusByEmail("active", activationCode.getRecieverEmail());

                response.setMessage("Successfully activated account with email : " + code.email());
                response.setStatus(200);
            } else {
                if (!checkExpiry(activationCode.getExpityDateTime())) {
                    activationCodeRepository.delete(activationCode);
                }
                response.setMessage("Could not find user with or expired code : " + code.email());
                response.setStatus(404);
            }
        });
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private boolean checkExpiry(String expityDateTime) {
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
            activationSender(email, CodeTypes.PASSWORD_RESET.getType());

            return ResponseEntity.ok("We have sent you email with code to : " + email + " check your email. ");
        }
        return ResponseEntity.ok("User with email : " + email + " does not exist : Existance " + exists);

    }

    public ResponseEntity<?> resetPassword(ConfirmResetPasswordDTO dto) {
        Response response = new Response();
        activationCodeRepository.findByRecieverEmail(dto.email()).ifPresent(activationCode -> {
            if (activationCode.getCode().equals(dto.code()) && checkExpiry(activationCode.getExpityDateTime())) {
                activationCodeRepository.delete(activationCode);
                String password = passwordEncoder.encode(dto.password());
                userRepository.updatePasswordByEmail(password, dto.email());

                response.setMessage("Successfully changed the password of account with email : " + dto.email());
                response.setStatus(200);
            } else {
                if (!checkExpiry(activationCode.getExpityDateTime())) {
                    activationCodeRepository.delete(activationCode);
                    response.setMessage("Failed to activate code has been expired of user : " + dto.email());
                } else {
                    response.setMessage("Could not find user with email : " + dto.email());
                    response.setStatus(404);
                }
            }
        });
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    private Boolean activationSender(String email, String type) {
        ActivisationCode activisationCode = new ActivisationCode(
                email,
                generateCode(6),
                false,
                LocalDateTime.now().plusMinutes(5l).toString(),
                type
        );
        String messageCode = "Your " + type + " code is " + activisationCode.getCode() +
                "\n You can use this before expiry time of 5 minutes ends." + activisationCode.getExpityDateTime() + " ";
        //                     emailMessage = emailService.sendActivationEmail(dto.email(), dto.firstName(), link + "/auth/activate?email=" + dto.email() + "&token=" + activisationCode + "&date" + LocalDateTime.now());
        String emailMessage = emailService.sendEmail(email, email, messageCode + "\nGo to " + link + " To get access to JORASOFT");
        activationCodeRepository.save(activisationCode);
        return true;
    }

    public ResponseEntity<?> activateSendAgain(String email, String type) {
        if (type.equals(CodeTypes.ACCOUNT_ACTIVISION.getType())) {
            if (userRepository.existsByEmailAndStatusIgnoreCase(email, "active")) {
                return ResponseEntity.ok("You have already activated your account");
            }
        }
        Optional<ActivisationCode> activeCode = activationCodeRepository.findByRecieverEmail(email);
        String expityDateTime = activeCode.get().getExpityDateTime();
        LocalDateTime expiry = LocalDateTime.parse(expityDateTime);

        if (expiry.minusMinutes(3l).isAfter(LocalDateTime.now())) {
        }
        activeCode.ifPresent(activationCode -> {

            activationCodeRepository.delete(activationCode);
            activationSender(email, type);
        });
        return ResponseEntity.ok("We send " + type + " code " + "account with email : " + email);
    }

    private boolean rolesInit() {
        if (roleRepository.findByName("USER").isPresent()) {
            return true;
        }
        roleRepository.save(new Role("USER", "AURALIFE"));
        return true;
    }

//    @Value("${secret.key}")
//    private String secretKey;

//    public User getUserById( UserApiAuthDto dto) {
//        if(dto.secretKey().equals(secretKey)) return userRepository.findById(dto.userId()).orElse(null);
//        else return null;
//    }

    public Boolean userMatch(UserApiAuthDto dto) {
        return passwordEncoder.matches(dto.password(), userRepository.findById(dto.userId()).get().getPassword());
    }
}


