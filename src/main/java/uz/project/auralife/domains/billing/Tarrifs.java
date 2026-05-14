package uz.project.auralife.domains.billing;

import lombok.Getter;

import java.util.List;

@Getter
public enum Tarrifs {

    SALOM(
            new Plan(1,
                    "Salom",
                    "Entry level subscribtion for beginners or for anyone ",
                    50000.0,
                    "UZS",
                    30,
                    List.of(
                            new Entitlement("HAMMA_SEARCH", EntitlementType.CONSUMABLE, "Hamma AI credits","Use 250 AI credits to search for anything in Hamma, which is enough for 50 requests", 250.0, true),
                            new Entitlement("CONTRACT_RESEARCH", EntitlementType.CONSUMABLE, "AI document researches","20 document researches, analises the document throughout the one month", 20.0, true)
                            ),
                    true
                    ));

    private final Plan plan;
    Tarrifs(Plan plan){
        this.plan = plan;
    }
}
