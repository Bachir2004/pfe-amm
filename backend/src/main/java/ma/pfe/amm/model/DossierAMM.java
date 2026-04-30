package ma.pfe.amm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "dossiers_amm")
@Getter
@Setter
@NoArgsConstructor
public class DossierAMM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reference;

    // ID du propriétaire (utilisateur LABORATOIRE qui a soumis)
    private Long proprietaireId;

    // === Étape 1 : Identification du médicament ===
    @NotBlank(message = "Le nom du médicament est obligatoire")
    @Column(nullable = false)
    private String nomMedicament;

    @NotBlank(message = "La DCI est obligatoire")
    @Column(nullable = false)
    private String dci;

    @NotBlank(message = "La forme pharmaceutique est obligatoire")
    @Column(nullable = false)
    private String formePharmaceutique;

    @NotBlank(message = "Le dosage est obligatoire")
    @Column(nullable = false)
    private String dosage;

    private String voieAdministration;
    private String classeTherapeutique;
    private String conditionnement;

    // === Étape 2 : Titulaire de l'AMM ===
    @NotBlank(message = "Le nom du laboratoire est obligatoire")
    @Column(nullable = false)
    private String nomLaboratoire;

    @NotBlank(message = "L'email du laboratoire est obligatoire")
    @Email(message = "Email invalide")
    @Column(nullable = false)
    private String emailLaboratoire;

    private String adresseLaboratoire;
    private String paysFabrication;
    private String nomFabricant;

    // === Étape 3 : Checklist documents ===
    @Column(nullable = false)
    private Boolean dossierAdministratif = false;

    @Column(nullable = false)
    private Boolean dossierChimique = false;

    @Column(nullable = false)
    private Boolean dossierToxicologique = false;

    @Column(nullable = false)
    private Boolean dossierClinique = false;

    @Column(nullable = false)
    private Boolean resumeCaracteristiques = false;

    @Column(nullable = false)
    private Boolean maquetteConditionnement = false;

    // === Statut et workflow ===
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDossier statut = StatutDossier.DEPOSE;

    private Long processInstanceKey;

    @Column(columnDefinition = "TEXT")
    private String commentaireAdmin;

    // === Évaluation scientifique (EVALUATEUR) ===
    private Integer noteQualite;
    private Integer noteSecurity;
    private Integer noteEfficacite;

    @Enumerated(EnumType.STRING)
    private AvisEvaluateur avisEvaluateur;

    @Column(columnDefinition = "TEXT")
    private String rapportEvaluation;

    @Column(columnDefinition = "TEXT")
    private String commentaireEvaluateur;

    // === Décision finale (DIRECTION) ===
    private String numeroAMM;

    @Column(columnDefinition = "TEXT")
    private String commentaireDirection;

    // === Dates ===
    @Column(nullable = false)
    private LocalDateTime dateDepot = LocalDateTime.now();

    private LocalDateTime dateDerniereModification;

    @PrePersist
    public void prePersist() {
        if (dateDepot == null) dateDepot = LocalDateTime.now();
        if (dossierAdministratif == null) dossierAdministratif = false;
        if (dossierChimique == null) dossierChimique = false;
        if (dossierToxicologique == null) dossierToxicologique = false;
        if (dossierClinique == null) dossierClinique = false;
        if (resumeCaracteristiques == null) resumeCaracteristiques = false;
        if (maquetteConditionnement == null) maquetteConditionnement = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.dateDerniereModification = LocalDateTime.now();
    }
}
