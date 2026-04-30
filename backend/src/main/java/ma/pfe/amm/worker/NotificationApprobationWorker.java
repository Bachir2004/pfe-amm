package ma.pfe.amm.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.pfe.amm.model.DossierAMM;
import ma.pfe.amm.model.StatutDossier;
import ma.pfe.amm.repository.DossierAMMRepository;
import ma.pfe.amm.service.DossierAMMService;
import ma.pfe.amm.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationApprobationWorker {

    private final DossierAMMService dossierService;
    private final DossierAMMRepository dossierRepository;
    private final NotificationService notificationService;

    @JobWorker(type = "notification-approbation")
    public void handleNotificationApprobation(final JobClient client, final ActivatedJob job) {
        log.info("Worker notification-approbation déclenché : jobKey={}", job.getKey());

        try {
            String reference = (String) job.getVariablesAsMap().get("reference");

            DossierAMM dossier = dossierRepository.findByReference(reference)
                    .orElseThrow(() -> new RuntimeException("Dossier introuvable : " + reference));

            dossierService.mettreAJourStatut(
                    dossier.getId(),
                    StatutDossier.APPROUVE,
                    "SYSTEME",
                    "APPROBATION_AMM",
                    "AMM approuvée par la direction"
            );

            notificationService.notifierApprobation(dossier);

            client.newCompleteCommand(job.getKey()).send().join();
            log.info("AMM {} approuvée avec succès", reference);

        } catch (Exception e) {
            log.error("Erreur worker approbation : {}", e.getMessage(), e);
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
        }
    }
}
