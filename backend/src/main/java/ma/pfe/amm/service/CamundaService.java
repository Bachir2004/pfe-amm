package ma.pfe.amm.service;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CamundaService {

    private final ZeebeClient zeebeClient;

    public Long demarrerProcessus(String reference, String nomMedicament,
                                   String nomLaboratoire, String emailLaboratoire) {
        try {
            ProcessInstanceEvent event = zeebeClient
                    .newCreateInstanceCommand()
                    .bpmnProcessId("processus-amm")
                    .latestVersion()
                    .variables(Map.of(
                            "reference", reference,
                            "nomMedicament", nomMedicament,
                            "nomLaboratoire", nomLaboratoire,
                            "emailLaboratoire", emailLaboratoire,
                            "dossierComplet", false,
                            "decision", ""
                    ))
                    .send()
                    .join();

            log.info("Processus AMM démarré pour {} : instanceKey={}",
                    reference, event.getProcessInstanceKey());
            return event.getProcessInstanceKey();

        } catch (Exception e) {
            log.error("Erreur démarrage processus pour {} : {}", reference, e.getMessage());
            throw new RuntimeException("Impossible de démarrer le processus Zeebe : " + e.getMessage(), e);
        }
    }

    public void completerVerification(Long jobKey, boolean dossierComplet, String commentaire) {
        if (jobKey == 0L) return;
        try {
            zeebeClient.newCompleteCommand(jobKey)
                    .variables(Map.of(
                            "dossierComplet", dossierComplet,
                            "commentaireVerification", commentaire != null ? commentaire : ""
                    ))
                    .send()
                    .join();
            log.info("Vérification complétée : jobKey={}, complet={}", jobKey, dossierComplet);
        } catch (Exception e) {
            log.warn("Zeebe non disponible pour vérification jobKey={} : {}", jobKey, e.getMessage());
        }
    }

    public void completerEvaluation(Long jobKey, String commentaire) {
        if (jobKey == 0L) return;
        try {
            zeebeClient.newCompleteCommand(jobKey)
                    .variables(Map.of(
                            "commentaireEvaluation", commentaire != null ? commentaire : ""
                    ))
                    .send()
                    .join();
            log.info("Évaluation complétée : jobKey={}", jobKey);
        } catch (Exception e) {
            log.warn("Zeebe non disponible pour évaluation jobKey={} : {}", jobKey, e.getMessage());
        }
    }

    public void completerDecisionDirection(Long jobKey, String decision, String motif) {
        if (jobKey == 0L) return;
        try {
            zeebeClient.newCompleteCommand(jobKey)
                    .variables(Map.of(
                            "decision", decision,
                            "motifDecision", motif != null ? motif : ""
                    ))
                    .send()
                    .join();
            log.info("Décision direction complétée : jobKey={}, decision={}", jobKey, decision);
        } catch (Exception e) {
            log.warn("Zeebe non disponible pour décision jobKey={} : {}", jobKey, e.getMessage());
        }
    }

    public void completerComplement(Long jobKey, String commentaire) {
        if (jobKey == 0L) return;
        try {
            zeebeClient.newCompleteCommand(jobKey)
                    .variables(Map.of(
                            "commentaireComplement", commentaire != null ? commentaire : ""
                    ))
                    .send()
                    .join();
            log.info("Complément complété : jobKey={}", jobKey);
        } catch (Exception e) {
            log.warn("Zeebe non disponible pour complément jobKey={} : {}", jobKey, e.getMessage());
        }
    }

    public void deployerProcessus(String resourcePath) {
        try {
            DeploymentEvent event = zeebeClient
                    .newDeployResourceCommand()
                    .addResourceFromClasspath(resourcePath)
                    .send()
                    .join();
            log.info("Processus déployé : {} ressource(s)", event.getProcesses().size());
            event.getProcesses().forEach(p ->
                    log.info("  -> {} v{} (key={})", p.getBpmnProcessId(), p.getVersion(), p.getProcessDefinitionKey())
            );
        } catch (Exception e) {
            log.error("Erreur déploiement BPMN '{}' : {}", resourcePath, e.getMessage());
        }
    }
}
