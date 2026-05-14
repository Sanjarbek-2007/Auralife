package uz.project.auralife.domains.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum Apps {
    AURALIFE("AURALIFE"),
    TEXTORA("TEXTORA"),
    HAYDA("HAYDA"),
    TABIB_CLINIX("TABIB_CLINIX"),
    TABIB_BUSINESS("TABIB_BUSINESS"),
    TABIB("TABIB"),
    CLINIX("CLINIX"),
    CLINIX_PATIENTS("CLINIX_PATIENTS"),
    CLINIX_STAFF("CLINIX_STAFF");

    private String value;

    Apps(String value) {
        this.value = value;
    }


}
