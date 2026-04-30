package ma.pfe.amm.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DecisionRequest {

    private String decision;
    private String motif;
}
