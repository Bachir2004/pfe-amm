package ma.pfe.amm.repository;

import ma.pfe.amm.model.DossierAMM;
import ma.pfe.amm.model.StatutDossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DossierAMMRepository extends JpaRepository<DossierAMM, Long> {

    Optional<DossierAMM> findByReference(String reference);

    List<DossierAMM> findByStatut(StatutDossier statut);

    List<DossierAMM> findByStatutIn(List<StatutDossier> statuts);

    List<DossierAMM> findByNomLaboratoire(String nomLaboratoire);

    List<DossierAMM> findByProprietaireId(Long proprietaireId);

    List<DossierAMM> findByProprietaireIdOrderByDateDepotDesc(Long proprietaireId);

    Optional<DossierAMM> findByProcessInstanceKey(Long processInstanceKey);

    boolean existsByReference(String reference);

    List<DossierAMM> findTop5ByOrderByDateDepotDesc();

    long countByDateDepotAfter(LocalDateTime date);

    long countByStatut(StatutDossier statut);

    @Query("SELECT d.nomLaboratoire, COUNT(d) FROM DossierAMM d GROUP BY d.nomLaboratoire ORDER BY COUNT(d) DESC")
    List<Object[]> countByLaboratoire();
}
