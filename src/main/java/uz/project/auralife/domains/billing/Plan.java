package uz.project.auralife.domains.billing;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Plan {
    private Integer id;
    private String name;
    private String description;
    private Double price;
    private String currency;
    private Integer durationInDays;
    private List<Entitlement> conditions;
    private Boolean isActive;
}