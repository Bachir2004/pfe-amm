package ma.pfe.amm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String role;
    private String nom;
    private String email;
    private Long id;
    private String nomLaboratoire;
    private String typeLaboratoire;
}
