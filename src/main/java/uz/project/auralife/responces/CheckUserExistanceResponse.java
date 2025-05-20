package uz.project.auralife.responces;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CheckUserExistanceResponse {
        private Boolean exists;
        private String credentials;
        private String phoneNumber;
 }
