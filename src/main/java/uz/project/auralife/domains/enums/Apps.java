package uz.project.auralife.domains.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum Apps {
    AURALIFE("AURALIFE"),
    TEXTORA("TEXTORA"),
    HAYDA("HAYDA");

    private String value;

    Apps(String value) {
        this.value = value;
    }


}
