package uz.project.jorasoft.domains.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum CodeTypes {
    PASSWORD_RESET("password_reset"),
    ACCOUNT_ACTIVISION("account_activision");

    private final String type;
    CodeTypes(String type) {
        this.type = type;
    }

    public  CodeTypes getCodeType(String type) {
        for (CodeTypes c : CodeTypes.values()) {
            if (c.type.equals(type)) {
                return c;
            }
        }
        return null;
    }

}
