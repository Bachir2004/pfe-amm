package ma.pfe.amm.repository;

import ma.pfe.amm.model.HistoriqueAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueActionRepository extends JpaRepository<HistoriqueAction, Long> {

    List<HistoriqueAction> findByDossierIdOrderByDateActionDesc(Long dossierId);

    List<HistoriqueAction> findByDossierReference(String reference);
}
