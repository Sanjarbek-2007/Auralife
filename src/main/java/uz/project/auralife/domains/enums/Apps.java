package uz.project.auralife.domains.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum Apps {
    AURALIFE("AURALIFE", null),
    TEXTORA("TEXTORA", null),
    HAYDA("HAYDA", null),
    TABIB_CLINIX("TABIB_CLINIX", "http://localhost:3003/auth/callback"),
    TABIB_BUSINESS("TABIB_BUSINESS", "http://localhost:3002/auth/callback"),
    TABIB("TABIB", "http://localhost:3003/auth/callback"),
    CLINIX("CLINIX", "http://localhost:3003/auth/callback"),
    CLINIX_PATIENTS("CLINIX_PATIENTS", "http://localhost:3003/auth/callback"),
    CLINIX_STAFF("CLINIX_STAFF", "http://localhost:3002/auth/callback");

    private String value;
    private String redirectUri;

    Apps(String value, String redirectUri) {
        this.value = value;
        this.redirectUri = redirectUri;
    }


}
