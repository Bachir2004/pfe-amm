package ma.pfe.amm.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerificationRequest {

    private Boolean dossierComplet;
    private String commentaire;
}
