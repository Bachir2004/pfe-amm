package ma.pfe.amm.service;

import lombok.extern.slf4j.Slf4j;
import ma.pfe.amm.model.DossierAMM;
import ma.pfe.amm.model.StatutDossier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void envoyerNotification(DossierAMM dossier, String sujet, String message) {
        log.info("=== NOTIFICATION ===");
        log.info("Destinataire : {} <{}>", dossier.getNomLaboratoire(), dossier.getEmailLaboratoire());
        log.info("Sujet        : {}", sujet);
        log.info("Référence    : {}", dossier.getReference());
        log.info("Médicament   : {}", dossier.getNomMedicament());
        log.info("Message      : {}", message);
        log.info("====================");
    }

    public void notifierApprobation(DossierAMM dossier) {
        String sujet = "AMM Approuvée - " + dossier.getReference();
        String message = String.format(
                "Votre demande d'Autorisation de Mise sur le Marché pour le médicament '%s' " +
                "(référence %s) a été APPROUVÉE. " +
                "Vous pouvez procéder à la commercialisation du produit.",
                dossier.getNomMedicament(), dossier.getReference()
        );
        envoyerNotification(dossier, sujet, message);
    }

    public void notifierRejet(DossierAMM dossier) {
        String sujet = "AMM Rejetée - " + dossier.getReference();
        String message = String.format(
                "Votre demande d'Autorisation de Mise sur le Marché pour le médicament '%s' " +
                "(référence %s) a été REJETÉE. " +
                "Pour plus d'informations, veuillez contacter nos services.",
                dossier.getNomMedicament(), dossier.getReference()
        );
        envoyerNotification(dossier, sujet, message);
    }

    public void notifierComplement(DossierAMM dossier, String documentsManquants) {
        String sujet = "Complément requis - " + dossier.getReference();
        String message = String.format(
                "Votre dossier AMM (référence %s) pour le médicament '%s' est incomplet. " +
                "Documents requis : %s. Veuillez soumettre les compléments dans les 30 jours.",
                dossier.getReference(), dossier.getNomMedicament(), documentsManquants
        );
        envoyerNotification(dossier, sujet, message);
    }

    public void notifierDepot(DossierAMM dossier) {
        String sujet = "Dossier AMM reçu - " + dossier.getReference();
        String message = String.format(
                "Votre dossier AMM pour le médicament '%s' a été reçu avec la référence %s. " +
                "Il sera examiné dans les meilleurs délais.",
                dossier.getNomMedicament(), dossier.getReference()
        );
        envoyerNotification(dossier, sujet, message);
    }

    public void notifierChangementStatut(DossierAMM dossier, StatutDossier ancienStatut) {
        String sujet = "Mise à jour dossier AMM - " + dossier.getReference();
        String message = String.format(
                "Le statut de votre dossier AMM (référence %s) a changé de %s à %s.",
                dossier.getReference(), ancienStatut, dossier.getStatut()
        );
        envoyerNotification(dossier, sujet, message);
    }
}
