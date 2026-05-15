package uz.project.auralife.domains.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum Apps {
    AURALIFE("AURALIFE", null),
    TEXTORA("TEXTORA", null),
    HAYDA("HAYDA", null),
    TABIB_CLINIX("TABIB_CLINIX", null),
    TABIB_BUSINESS("TABIB_BUSINESS", null),
    TABIB("TABIB", null),
    CLINIX("CLINIX", null),
    CLINIX_PATIENTS("CLINIX_PATIENTS", null),
    CLINIX_STAFF("CLINIX_STAFF", null);

    private String value;
    private String redirectUri;

    Apps(String value, String redirectUri) {
        this.value = value;
        this.redirectUri = redirectUri;
    }


}
