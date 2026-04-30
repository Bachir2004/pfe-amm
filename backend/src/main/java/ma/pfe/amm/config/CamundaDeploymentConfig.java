package ma.pfe.amm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.pfe.amm.service.CamundaService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CamundaDeploymentConfig {

    private final CamundaService camundaService;

    @EventListener(ApplicationReadyEvent.class)
    public void deployerProcessusAuDemarrage() {
        log.info("Déploiement du processus BPMN AMM...");
        camundaService.deployerProcessus("bpmn/processus-amm.bpmn");
    }
}
