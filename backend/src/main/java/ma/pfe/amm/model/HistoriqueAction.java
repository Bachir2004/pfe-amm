package ma.pfe.amm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_actions")
@Getter
@Setter
@NoArgsConstructor
public class HistoriqueAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private DossierAMM dossier;

    @Column(nullable = false)
    private String acteur;

    @Column(nullable = false)
    private String action;

    @Enumerated(EnumType.STRING)
    private StatutDossier ancienStatut;

    @Enumerated(EnumType.STRING)
    private StatutDossier nouveauStatut;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(nullable = false)
    private LocalDateTime dateAction = LocalDateTime.now();

    public static HistoriqueAction creer(DossierAMM dossier, String acteur, String action,
                                          StatutDossier ancienStatut, StatutDossier nouveauStatut,
                                          String commentaire) {
        HistoriqueAction h = new HistoriqueAction();
        h.setDossier(dossier);
        h.setActeur(acteur);
        h.setAction(action);
        h.setAncienStatut(ancienStatut);
        h.setNouveauStatut(nouveauStatut);
        h.setCommentaire(commentaire);
        h.setDateAction(LocalDateTime.now());
        return h;
    }
}
