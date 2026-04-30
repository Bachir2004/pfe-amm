package ma.pfe.amm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.pfe.amm.model.*;
import ma.pfe.amm.repository.DossierAMMRepository;
import ma.pfe.amm.repository.HistoriqueActionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DossierAMMService {

    private final DossierAMMRepository dossierRepository;
    private final HistoriqueActionRepository historiqueRepository;

    public DossierAMM creerDossier(DossierAMM dossier) {
        dossier.setReference(genererReference());
        dossier.setStatut(StatutDossier.DEPOSE);
        dossier.setDateDepot(LocalDateTime.now());
        DossierAMM saved = dossierRepository.save(dossier);

        historiqueRepository.save(HistoriqueAction.creer(
                saved, saved.getNomLaboratoire(), "DEPOT_DOSSIER",
                null, StatutDossier.DEPOSE,
                "Dossier AMM soumis par " + saved.getNomLaboratoire()
        ));

        log.info("Dossier AMM créé : {}", saved.getReference());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<DossierAMM> getTousDossiers() {
        return dossierRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DossierAMM> getDossiersByProprietaire(Long proprietaireId) {
        return dossierRepository.findByProprietaireIdOrderByDateDepotDesc(proprietaireId);
    }

    @Transactional(readOnly = true)
    public DossierAMM getDossierById(Long id) {
        return dossierRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Dossier introuvable avec l'id : " + id));
    }

    @Transactional(readOnly = true)
    public DossierAMM getDossierByReference(String reference) {
        return dossierRepository.findByReference(reference)
                .orElseThrow(() -> new NoSuchElementException("Dossier introuvable avec la référence : " + reference));
    }

    @Transactional(readOnly = true)
    public List<DossierAMM> getDossiersByStatut(StatutDossier statut) {
        return dossierRepository.findByStatut(statut);
    }

    @Transactional(readOnly = true)
    public List<DossierAMM> getDossiersAVerifier() {
        return dossierRepository.findByStatutIn(List.of(StatutDossier.DEPOSE, StatutDossier.EN_VERIFICATION));
    }

    @Transactional(readOnly = true)
    public List<DossierAMM> getDossiersEnEvaluation() {
        return dossierRepository.findByStatut(StatutDossier.EN_EVALUATION);
    }

    @Transactional(readOnly = true)
    public List<DossierAMM> getDossiersEnValidation() {
        return dossierRepository.findByStatut(StatutDossier.EN_VALIDATION);
    }

    public DossierAMM mettreAJourStatut(Long id, StatutDossier nouveauStatut,
                                         String acteur, String action, String commentaire) {
        DossierAMM dossier = getDossierById(id);
        StatutDossier ancienStatut = dossier.getStatut();
        dossier.setStatut(nouveauStatut);
        if (commentaire != null && !commentaire.isBlank()) {
            dossier.setCommentaireAdmin(commentaire);
        }
        DossierAMM saved = dossierRepository.save(dossier);

        historiqueRepository.save(HistoriqueAction.creer(
                saved, acteur, action, ancienStatut, nouveauStatut, commentaire
        ));

        log.info("Dossier {} : {} -> {}", dossier.getReference(), ancienStatut, nouveauStatut);
        return saved;
    }

    public DossierAMM verifierDossier(Long id, String resultat, String acteur, String commentaire) {
        DossierAMM dossier = getDossierById(id);
        StatutDossier ancienStatut = dossier.getStatut();
        StatutDossier nouveauStatut = "COMPLET".equals(resultat)
                ? StatutDossier.EN_EVALUATION
                : StatutDossier.INCOMPLET;

        dossier.setStatut(nouveauStatut);
        dossier.setCommentaireAdmin(commentaire);
        DossierAMM saved = dossierRepository.save(dossier);

        historiqueRepository.save(HistoriqueAction.creer(
                saved, acteur, "VERIFICATION_ADMINISTRATIVE",
                ancienStatut, nouveauStatut,
                commentaire
        ));

        log.info("Vérification dossier {} : {}", dossier.getReference(), nouveauStatut);
        return saved;
    }

    public DossierAMM evaluerDossier(Long id, Map<String, Object> evalData, String acteur) {
        DossierAMM dossier = getDossierById(id);
        StatutDossier ancienStatut = dossier.getStatut();

        if (evalData.get("noteQualite") != null)
            dossier.setNoteQualite(Integer.parseInt(evalData.get("noteQualite").toString()));
        if (evalData.get("noteSecurity") != null)
            dossier.setNoteSecurity(Integer.parseInt(evalData.get("noteSecurity").toString()));
        if (evalData.get("noteEfficacite") != null)
            dossier.setNoteEfficacite(Integer.parseInt(evalData.get("noteEfficacite").toString()));
        if (evalData.get("rapportEvaluation") != null)
            dossier.setRapportEvaluation(evalData.get("rapportEvaluation").toString());
        if (evalData.get("commentaireEvaluateur") != null)
            dossier.setCommentaireEvaluateur(evalData.get("commentaireEvaluateur").toString());

        String avisStr = evalData.get("avisEvaluateur") != null ? evalData.get("avisEvaluateur").toString() : null;
        AvisEvaluateur avis = null;
        if (avisStr != null) {
            try { avis = AvisEvaluateur.valueOf(avisStr); } catch (IllegalArgumentException ignored) {}
        }
        dossier.setAvisEvaluateur(avis);
        dossier.setStatut(StatutDossier.EN_VALIDATION);
        DossierAMM saved = dossierRepository.save(dossier);

        historiqueRepository.save(HistoriqueAction.creer(
                saved, acteur, "EVALUATION_TECHNIQUE",
                ancienStatut, StatutDossier.EN_VALIDATION,
                "Avis : " + (avis != null ? avis.name() : "N/A") + " — " +
                        (dossier.getRapportEvaluation() != null ? dossier.getRapportEvaluation() : "")
        ));

        log.info("Évaluation dossier {} : avis={}", dossier.getReference(), avis);
        return saved;
    }

    public DossierAMM prendreDecision(Long id, String decision, String acteur, String commentaire) {
        DossierAMM dossier = getDossierById(id);
        StatutDossier ancienStatut = dossier.getStatut();
        StatutDossier nouveauStatut = "APPROUVE".equals(decision)
                ? StatutDossier.APPROUVE
                : StatutDossier.REJETE;

        dossier.setStatut(nouveauStatut);
        dossier.setCommentaireDirection(commentaire);

        if (nouveauStatut == StatutDossier.APPROUVE) {
            dossier.setNumeroAMM(genererNumeroAMM());
        }

        DossierAMM saved = dossierRepository.save(dossier);

        historiqueRepository.save(HistoriqueAction.creer(
                saved, acteur,
                nouveauStatut == StatutDossier.APPROUVE ? "DECISION_APPROBATION" : "DECISION_REJET",
                ancienStatut, nouveauStatut,
                commentaire
        ));

        log.info("Décision dossier {} : {} (AMM: {})", dossier.getReference(), nouveauStatut, dossier.getNumeroAMM());
        return saved;
    }

    public DossierAMM soumettreComplement(Long id, String acteur, String commentaire) {
        DossierAMM dossier = getDossierById(id);
        if (dossier.getStatut() != StatutDossier.INCOMPLET) {
            throw new IllegalStateException("Le dossier n'est pas dans l'état INCOMPLET");
        }
        return mettreAJourStatut(id, StatutDossier.EN_VERIFICATION, acteur, "SOUMISSION_COMPLEMENT", commentaire);
    }

    public DossierAMM lierProcessus(Long dossierId, Long processInstanceKey) {
        DossierAMM dossier = getDossierById(dossierId);
        dossier.setProcessInstanceKey(processInstanceKey);
        return dossierRepository.save(dossier);
    }

    @Transactional(readOnly = true)
    public List<HistoriqueAction> getHistorique(Long dossierId) {
        getDossierById(dossierId);
        return historiqueRepository.findByDossierIdOrderByDateActionDesc(dossierId);
    }

    @Transactional(readOnly = true)
    public List<DossierAMM> getDerniersDossiers() {
        return dossierRepository.findTop5ByOrderByDateDepotDesc();
    }

    private String genererReference() {
        int annee = LocalDateTime.now().getYear();
        long count = dossierRepository.count() + 1;
        return String.format("AMM-%d-%04d", annee, count);
    }

    private String genererNumeroAMM() {
        int annee = LocalDateTime.now().getYear();
        long count = dossierRepository.countByStatut(StatutDossier.APPROUVE) + 1;
        return String.format("AMM-MA-%d-%05d", annee, count);
    }
}
