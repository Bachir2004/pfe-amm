package ma.pfe.amm.service;

import lombok.RequiredArgsConstructor;
import ma.pfe.amm.dto.StatistiquesDto;
import ma.pfe.amm.model.DossierAMM;
import ma.pfe.amm.model.StatutDossier;
import ma.pfe.amm.repository.DossierAMMRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatistiquesService {

    private final DossierAMMRepository dossierRepository;

    public StatistiquesDto calculerStatistiques() {
        List<DossierAMM> tous = dossierRepository.findAll();

        StatistiquesDto dto = new StatistiquesDto();
        dto.setTotalDossiers(tous.size());
        dto.setDeposes(compter(tous, StatutDossier.DEPOSE));
        dto.setEnVerification(compter(tous, StatutDossier.EN_VERIFICATION));
        dto.setIncomplets(compter(tous, StatutDossier.INCOMPLET));
        dto.setEnEvaluation(compter(tous, StatutDossier.EN_EVALUATION));
        dto.setEnValidation(compter(tous, StatutDossier.EN_VALIDATION));
        dto.setApprouves(compter(tous, StatutDossier.APPROUVE));
        dto.setRejetes(compter(tous, StatutDossier.REJETE));
        dto.setAnnules(compter(tous, StatutDossier.ANNULE));
        dto.setParLaboratoire(calculerParLaboratoire(tous));

        LocalDateTime debutMois = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        dto.setDossiersCresCeMois(dossierRepository.countByDateDepotAfter(debutMois));

        dto.setTopLaboratoires(calculerTopLaboratoires());

        return dto;
    }

    private long compter(List<DossierAMM> dossiers, StatutDossier statut) {
        return dossiers.stream().filter(d -> d.getStatut() == statut).count();
    }

    private Map<String, Long> calculerParLaboratoire(List<DossierAMM> dossiers) {
        Map<String, Long> parLaboratoire = new HashMap<>();
        for (DossierAMM dossier : dossiers) {
            parLaboratoire.merge(dossier.getNomLaboratoire(), 1L, Long::sum);
        }
        return parLaboratoire;
    }

    private List<Map<String, Object>> calculerTopLaboratoires() {
        List<Object[]> rows = dossierRepository.countByLaboratoire();
        List<Map<String, Object>> result = new ArrayList<>();
        int limit = Math.min(3, rows.size());
        for (int i = 0; i < limit; i++) {
            Object[] row = rows.get(i);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("laboratoire", row[0]);
            entry.put("total", row[1]);
            result.add(entry);
        }
        return result;
    }
}
