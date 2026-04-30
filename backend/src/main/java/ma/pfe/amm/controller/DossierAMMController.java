package ma.pfe.amm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.pfe.amm.model.*;
import ma.pfe.amm.service.CamundaService;
import ma.pfe.amm.service.DossierAMMService;
import ma.pfe.amm.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dossiers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class DossierAMMController {

    private final DossierAMMService dossierService;
    private final CamundaService camundaService;
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<DossierAMM> creerDossier(
            @Valid @RequestBody DossierAMM dossier,
            Authentication authentication) {

        Utilisateur user = (Utilisateur) authentication.getPrincipal();
        dossier.setProprietaireId(user.getId());
        dossier.setEmailLaboratoire(user.getEmail());
        if (user.getNomLaboratoire() != null && !user.getNomLaboratoire().isBlank()) {
            dossier.setNomLaboratoire(user.getNomLaboratoire());
        }

        DossierAMM saved = dossierService.creerDossier(dossier);

        try {
            Long processInstanceKey = camundaService.demarrerProcessus(
                    saved.getReference(), saved.getNomMedicament(),
                    saved.getNomLaboratoire(), saved.getEmailLaboratoire());
            dossierService.lierProcessus(saved.getId(), processInstanceKey);
            saved.setProcessInstanceKey(processInstanceKey);
        } catch (Exception e) {
            log.warn("Processus Zeebe non démarré pour {} : {}", saved.getReference(), e.getMessage());
        }

        notificationService.notifierDepot(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<DossierAMM>> getTousDossiers(Authentication authentication) {
        Utilisateur user = (Utilisateur) authentication.getPrincipal();
        if (user.getRole() == Role.LABORATOIRE) {
            return ResponseEntity.ok(dossierService.getDossiersByProprietaire(user.getId()));
        }
        return ResponseEntity.ok(dossierService.getTousDossiers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DossierAMM> getDossierById(
            @PathVariable Long id, Authentication authentication) {
        DossierAMM dossier = dossierService.getDossierById(id);
        Utilisateur user = (Utilisateur) authentication.getPrincipal();
        if (user.getRole() == Role.LABORATOIRE && !user.getId().equals(dossier.getProprietaireId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dossier);
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<DossierAMM> getDossierByReference(@PathVariable String reference) {
        return ResponseEntity.ok(dossierService.getDossierByReference(reference));
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<DossierAMM>> getDossiersByStatut(@PathVariable StatutDossier statut) {
        return ResponseEntity.ok(dossierService.getDossiersByStatut(statut));
    }

    @GetMapping("/file-attente/verification")
    public ResponseEntity<List<DossierAMM>> getDossiersAVerifier() {
        return ResponseEntity.ok(dossierService.getDossiersAVerifier());
    }

    @GetMapping("/file-attente/evaluation")
    public ResponseEntity<List<DossierAMM>> getDossiersEnEvaluation() {
        return ResponseEntity.ok(dossierService.getDossiersEnEvaluation());
    }

    @GetMapping("/file-attente/validation")
    public ResponseEntity<List<DossierAMM>> getDossiersEnValidation() {
        return ResponseEntity.ok(dossierService.getDossiersEnValidation());
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<DossierAMM> mettreAJourStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        StatutDossier statut = StatutDossier.valueOf(body.get("statut"));
        Utilisateur user = (Utilisateur) authentication.getPrincipal();
        String acteur = user.getEmail();
        String commentaire = body.getOrDefault("commentaire", "");
        DossierAMM updated = dossierService.mettreAJourStatut(
                id, statut, acteur, "MISE_A_JOUR_STATUT", commentaire);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/verification")
    public ResponseEntity<DossierAMM> verifierDossier(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Utilisateur user = (Utilisateur) authentication.getPrincipal();
        String resultat = body.get("resultat"); // COMPLET ou INCOMPLET
        String commentaire = body.getOrDefault("commentaire", "");
        DossierAMM updated = dossierService.verifierDossier(id, resultat, user.getEmail(), commentaire);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/evaluation")
    public ResponseEntity<DossierAMM> evaluerDossier(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        Utilisateur user = (Utilisateur) authentication.getPrincipal();
        DossierAMM updated = dossierService.evaluerDossier(id, body, user.getEmail());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/decision")
    public ResponseEntity<DossierAMM> prendreDecision(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Utilisateur user = (Utilisateur) authentication.getPrincipal();
        String decision = body.get("decision"); // APPROUVE ou REJETE
        String commentaire = body.getOrDefault("commentaire", "");
        DossierAMM updated = dossierService.prendreDecision(id, decision, user.getEmail(), commentaire);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/complement")
    public ResponseEntity<DossierAMM> soumettreComplement(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Utilisateur user = (Utilisateur) authentication.getPrincipal();
        String commentaire = body.getOrDefault("commentaire", "Complément soumis");
        DossierAMM updated = dossierService.soumettreComplement(id, user.getEmail(), commentaire);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/historique")
    public ResponseEntity<List<HistoriqueAction>> getHistorique(@PathVariable Long id) {
        return ResponseEntity.ok(dossierService.getHistorique(id));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<DossierAMM>> getDerniersDossiers() {
        return ResponseEntity.ok(dossierService.getDerniersDossiers());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AMM Backend — AMMPS",
                "version", "1.0.0"
        ));
    }
}
