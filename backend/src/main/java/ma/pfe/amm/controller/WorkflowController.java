package ma.pfe.amm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.pfe.amm.dto.DecisionRequest;
import ma.pfe.amm.dto.VerificationRequest;
import ma.pfe.amm.model.DossierAMM;
import ma.pfe.amm.model.StatutDossier;
import ma.pfe.amm.service.CamundaService;
import ma.pfe.amm.service.DossierAMMService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dossiers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class WorkflowController {

    private final CamundaService camundaService;
    private final DossierAMMService dossierService;

    @PostMapping("/{id}/verification")
    public ResponseEntity<Map<String, Object>> completerVerification(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Long jobKey,
            @RequestBody VerificationRequest request) {

        DossierAMM dossier = dossierService.getDossierById(id);
        boolean complet = Boolean.TRUE.equals(request.getDossierComplet());

        StatutDossier nouveauStatut = complet ? StatutDossier.EN_EVALUATION : StatutDossier.INCOMPLET;
        dossierService.mettreAJourStatut(id, nouveauStatut, "ADMIN",
                "VERIFICATION_ADMINISTRATIVE", request.getCommentaire());

        camundaService.completerVerification(jobKey, complet, request.getCommentaire());

        return ResponseEntity.ok(Map.of(
                "message", "Vérification complétée",
                "dossierComplet", complet,
                "nouveauStatut", nouveauStatut.name()
        ));
    }

    @PostMapping("/{id}/evaluation")
    public ResponseEntity<Map<String, Object>> completerEvaluation(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Long jobKey,
            @RequestBody Map<String, String> body) {

        String commentaire = body.getOrDefault("commentaire", "");
        dossierService.mettreAJourStatut(id, StatutDossier.EN_VALIDATION, "EVALUATEUR",
                "EVALUATION_TECHNIQUE", commentaire);

        camundaService.completerEvaluation(jobKey, commentaire);

        return ResponseEntity.ok(Map.of(
                "message", "Évaluation complétée",
                "nouveauStatut", StatutDossier.EN_VALIDATION.name()
        ));
    }

    @PostMapping("/{id}/decision")
    public ResponseEntity<Map<String, Object>> completerDecision(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Long jobKey,
            @RequestBody DecisionRequest request) {

        String decision = request.getDecision();
        if (!"APPROUVE".equals(decision) && !"REJETE".equals(decision)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Décision invalide. Valeurs acceptées : APPROUVE, REJETE"
            ));
        }

        camundaService.completerDecisionDirection(jobKey, decision, request.getMotif());

        return ResponseEntity.ok(Map.of(
                "message", "Décision enregistrée",
                "decision", decision
        ));
    }

    @PostMapping("/{id}/complement")
    public ResponseEntity<Map<String, Object>> completerComplement(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Long jobKey,
            @RequestBody Map<String, String> body) {

        String commentaire = body.getOrDefault("commentaire", "");
        dossierService.mettreAJourStatut(id, StatutDossier.EN_VERIFICATION, "LABORATOIRE",
                "COMPLEMENT_SOUMIS", commentaire);

        camundaService.completerComplement(jobKey, commentaire);

        return ResponseEntity.ok(Map.of(
                "message", "Complément soumis, retour en vérification",
                "nouveauStatut", StatutDossier.EN_VERIFICATION.name()
        ));
    }
}
