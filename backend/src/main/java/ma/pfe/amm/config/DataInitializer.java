package ma.pfe.amm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.pfe.amm.model.*;
import ma.pfe.amm.repository.DossierAMMRepository;
import ma.pfe.amm.repository.HistoriqueActionRepository;
import ma.pfe.amm.repository.UtilisateurRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final DossierAMMRepository dossierRepository;
    private final HistoriqueActionRepository historiqueRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        initUtilisateurs();
        initDossiers();
        log.info("=== Données de test initialisées ===");
    }

    private void initUtilisateurs() {
        creerUser("Administrateur AMMPS", "admin@amm.ma",      "admin123", Role.ADMIN,      null, null);
        creerUser("Dr. Karim Benzakour",  "evaluateur@amm.ma", "eval123",  Role.EVALUATEUR, null, null);
        creerUser("Directeur Général",    "direction@amm.ma",  "dir123",   Role.DIRECTION,  null, null);
        creerUser("Sothema",              "labo@amm.ma",       "labo123",  Role.LABORATOIRE,
                "Sothema", TypeLaboratoire.LABORATOIRE_NATIONAL);
    }

    private void creerUser(String nom, String email, String password, Role role,
                            String nomLaboratoire, TypeLaboratoire type) {
        if (!utilisateurRepository.existsByEmail(email)) {
            Utilisateur u = new Utilisateur();
            u.setNom(nom);
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode(password));
            u.setRole(role);
            u.setNomLaboratoire(nomLaboratoire);
            u.setTypeLaboratoire(type);
            utilisateurRepository.save(u);
            log.info("  Utilisateur créé : {} ({})", email, role);
        }
    }

    private void initDossiers() {
        if (dossierRepository.count() > 0) {
            log.info("  Dossiers déjà présents, skip.");
            return;
        }

        Long laboId = utilisateurRepository.findByEmail("labo@amm.ma")
                .map(Utilisateur::getId).orElse(null);

        // AMM-2026-0001 : Amoxicilline — APPROUVE (Sothema)
        DossierAMM d1 = creerDossier("AMM-2026-0001", "Amoxicilline 500mg",
                "Sothema", "regulatory@sothema.ma",
                "Gélule", "Amoxicilline trihydratée", "500mg",
                StatutDossier.APPROUVE, LocalDateTime.now().minusDays(45), laboId);
        d1.setNumeroAMM("AMM-MA-2026-00001");
        d1.setVoieAdministration("Orale");
        d1.setClasseTherapeutique("Antibiotique - Pénicillines");
        dossierRepository.save(d1);
        ajouterHistorique(d1, "SYSTEME",           "DEPOT_DOSSIER",              null,                        StatutDossier.DEPOSE,        "Dossier soumis par Sothema", -45);
        ajouterHistorique(d1, "admin@amm.ma",      "VERIFICATION_ADMINISTRATIVE", StatutDossier.DEPOSE,        StatutDossier.EN_EVALUATION, "Dossier complet, tous les documents présents", -42);
        ajouterHistorique(d1, "evaluateur@amm.ma", "EVALUATION_TECHNIQUE",        StatutDossier.EN_EVALUATION, StatutDossier.EN_VALIDATION, "Études cliniques conformes, bioéquivalence validée", -30);
        ajouterHistorique(d1, "direction@amm.ma",  "DECISION_APPROBATION",        StatutDossier.EN_VALIDATION, StatutDossier.APPROUVE,      "AMM accordée. Numéro : AMM-MA-2026-00001", -20);

        // AMM-2026-0002 : Ibuprofène — EN_EVALUATION (Cooper Pharma)
        DossierAMM d2 = creerDossier("AMM-2026-0002", "Ibuprofène 400mg",
                "Cooper Pharma", "affaires-reglementaires@cooperpharma.ma",
                "Comprimé pelliculé", "Ibuprofène", "400mg",
                StatutDossier.EN_EVALUATION, LocalDateTime.now().minusDays(20), laboId);
        d2.setVoieAdministration("Orale");
        d2.setClasseTherapeutique("AINS - Anti-inflammatoire");
        dossierRepository.save(d2);
        ajouterHistorique(d2, "SYSTEME",      "DEPOT_DOSSIER",              null,                StatutDossier.DEPOSE,        "Dossier soumis par Cooper Pharma", -20);
        ajouterHistorique(d2, "admin@amm.ma", "VERIFICATION_ADMINISTRATIVE", StatutDossier.DEPOSE, StatutDossier.EN_EVALUATION, "Dossier complet, transmis à l'évaluateur", -17);

        // AMM-2026-0003 : Paracétamol — EN_VERIFICATION (Maphar)
        DossierAMM d3 = creerDossier("AMM-2026-0003", "Paracétamol 1g",
                "Maphar", "amm@maphar.ma",
                "Comprimé effervescent", "Paracétamol", "1000mg",
                StatutDossier.EN_VERIFICATION, LocalDateTime.now().minusDays(5), laboId);
        d3.setVoieAdministration("Orale");
        d3.setClasseTherapeutique("Analgésique - Antipyrétique");
        dossierRepository.save(d3);
        ajouterHistorique(d3, "SYSTEME", "DEPOT_DOSSIER",  null,                  StatutDossier.DEPOSE,          "Dossier soumis par Maphar", -5);
        ajouterHistorique(d3, "SYSTEME", "PRISE_EN_CHARGE", StatutDossier.DEPOSE, StatutDossier.EN_VERIFICATION, "Prise en charge pour vérification", -4);

        // AMM-2026-0004 : Oméprazole — INCOMPLET (Pharma 5)
        DossierAMM d4 = creerDossier("AMM-2026-0004", "Oméprazole 20mg",
                "Pharma 5", "regulatory@pharma5.ma",
                "Gélule gastrorésistante", "Oméprazole", "20mg",
                StatutDossier.INCOMPLET, LocalDateTime.now().minusDays(15), laboId);
        d4.setCommentaireAdmin("Documents manquants : études de stabilité et certificat de BPF");
        d4.setVoieAdministration("Orale");
        d4.setClasseTherapeutique("Inhibiteur de la pompe à protons");
        dossierRepository.save(d4);
        ajouterHistorique(d4, "SYSTEME",      "DEPOT_DOSSIER",              null,                StatutDossier.DEPOSE,    "Dossier soumis par Pharma 5", -15);
        ajouterHistorique(d4, "admin@amm.ma", "VERIFICATION_ADMINISTRATIVE", StatutDossier.DEPOSE, StatutDossier.INCOMPLET, "Documents manquants : études de stabilité et certificat de BPF", -12);

        // AMM-2026-0005 : Metformine — REJETE (Sothema)
        DossierAMM d5 = creerDossier("AMM-2026-0005", "Metformine 850mg",
                "Sothema", "regulatory@sothema.ma",
                "Comprimé pelliculé", "Metformine chlorhydrate", "850mg",
                StatutDossier.REJETE, LocalDateTime.now().minusDays(60), laboId);
        d5.setAvisEvaluateur(AvisEvaluateur.DEFAVORABLE);
        d5.setNoteQualite(6); d5.setNoteSecurity(5); d5.setNoteEfficacite(4);
        d5.setRapportEvaluation("Bioéquivalence non démontrée. Données cliniques insuffisantes.");
        d5.setCommentaireDirection("Rejeté : données cliniques non conformes aux exigences AMMPS");
        d5.setVoieAdministration("Orale");
        d5.setClasseTherapeutique("Antidiabétique - Biguanides");
        dossierRepository.save(d5);
        ajouterHistorique(d5, "SYSTEME",           "DEPOT_DOSSIER",              null,                        StatutDossier.DEPOSE,        "Dossier soumis par Sothema", -60);
        ajouterHistorique(d5, "admin@amm.ma",      "VERIFICATION_ADMINISTRATIVE", StatutDossier.DEPOSE,        StatutDossier.EN_EVALUATION, "Dossier complet", -57);
        ajouterHistorique(d5, "evaluateur@amm.ma", "EVALUATION_TECHNIQUE",        StatutDossier.EN_EVALUATION, StatutDossier.EN_VALIDATION, "Avis DEFAVORABLE — Bioéquivalence non démontrée", -45);
        ajouterHistorique(d5, "direction@amm.ma",  "DECISION_REJET",              StatutDossier.EN_VALIDATION, StatutDossier.REJETE,        "Rejeté : données cliniques non conformes aux exigences réglementaires AMMPS", -35);

        // AMM-2026-0006 : Atorvastatine — DEPOSE (Cooper Pharma)
        DossierAMM d6 = creerDossier("AMM-2026-0006", "Atorvastatine 10mg",
                "Cooper Pharma", "affaires-reglementaires@cooperpharma.ma",
                "Comprimé pelliculé", "Atorvastatine calcique", "10mg",
                StatutDossier.DEPOSE, LocalDateTime.now().minusDays(1), laboId);
        d6.setVoieAdministration("Orale");
        d6.setClasseTherapeutique("Hypolipémiant - Statine");
        dossierRepository.save(d6);
        ajouterHistorique(d6, "SYSTEME", "DEPOT_DOSSIER", null, StatutDossier.DEPOSE, "Dossier soumis par Cooper Pharma, en attente de prise en charge", -1);

        log.info("  6 dossiers de test créés avec historique complet.");
    }

    private DossierAMM creerDossier(String reference, String nomMedicament,
                                     String nomLaboratoire, String emailLabo,
                                     String forme, String dci, String dosage,
                                     StatutDossier statut, LocalDateTime dateDepot,
                                     Long proprietaireId) {
        DossierAMM d = new DossierAMM();
        d.setReference(reference);
        d.setNomMedicament(nomMedicament);
        d.setNomLaboratoire(nomLaboratoire);
        d.setEmailLaboratoire(emailLabo);
        d.setFormePharmaceutique(forme);
        d.setDci(dci);
        d.setDosage(dosage);
        d.setStatut(statut);
        d.setDateDepot(dateDepot);
        d.setDateDerniereModification(LocalDateTime.now());
        d.setProprietaireId(proprietaireId);
        return dossierRepository.save(d);
    }

    private void ajouterHistorique(DossierAMM dossier, String acteur, String action,
                                    StatutDossier ancienStatut, StatutDossier nouveauStatut,
                                    String commentaire, int joursOffset) {
        HistoriqueAction h = new HistoriqueAction();
        h.setDossier(dossier);
        h.setActeur(acteur);
        h.setAction(action);
        h.setAncienStatut(ancienStatut);
        h.setNouveauStatut(nouveauStatut);
        h.setCommentaire(commentaire);
        h.setDateAction(LocalDateTime.now().plusDays(joursOffset));
        historiqueRepository.save(h);
    }
}
