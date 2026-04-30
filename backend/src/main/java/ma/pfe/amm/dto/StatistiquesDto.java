package ma.pfe.amm.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class StatistiquesDto {

    private long totalDossiers;
    private long deposes;
    private long enVerification;
    private long incomplets;
    private long enEvaluation;
    private long enValidation;
    private long approuves;
    private long rejetes;
    private long annules;
    private Map<String, Long> parLaboratoire;
    private long dossiersCresCeMois;
    private List<Map<String, Object>> topLaboratoires;
}
