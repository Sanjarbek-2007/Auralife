package uz.project.auralife.domains.billing;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Entitlement {
    private String featureKey;
    private EntitlementType type;
    private String displayName;
    private String description;
    private Double amount;
    private Boolean allowRollover;
}
