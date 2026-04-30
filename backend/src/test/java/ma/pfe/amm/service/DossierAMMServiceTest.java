package ma.pfe.amm.service;

import ma.pfe.amm.model.DossierAMM;
import ma.pfe.amm.model.HistoriqueAction;
import ma.pfe.amm.model.StatutDossier;
import ma.pfe.amm.repository.DossierAMMRepository;
import ma.pfe.amm.repository.HistoriqueActionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - DossierAMMService")
class DossierAMMServiceTest {

    @Mock
    private DossierAMMRepository dossierRepository;

    @Mock
    private HistoriqueActionRepository historiqueRepository;

    @InjectMocks
    private DossierAMMService dossierService;

    private DossierAMM dossierTest;

    @BeforeEach
    void setUp() {
        dossierTest = new DossierAMM();
        dossierTest.setId(1L);
        dossierTest.setNomMedicament("Amoxicilline 500mg");
        dossierTest.setDci("Amoxicilline");
        dossierTest.setNomLaboratoire("Pharma Maroc");
        dossierTest.setEmailLaboratoire("contact@pharmamaroc.ma");
        dossierTest.setFormePharmaceutique("Comprime");
        dossierTest.setDosage("500mg");
        dossierTest.setStatut(StatutDossier.DEPOSE);
        dossierTest.setReference("AMM-2024-0001");
    }

    @Test
    @DisplayName("creerDossier : génère référence et sauvegarde")
    void creerDossier_devraitGenererReferenceEtSauvegarder() {
        when(dossierRepository.count()).thenReturn(0L);
        when(dossierRepository.save(any(DossierAMM.class))).thenReturn(dossierTest);
        when(historiqueRepository.save(any(HistoriqueAction.class))).thenReturn(new HistoriqueAction());

        DossierAMM resultat = dossierService.creerDossier(dossierTest);

        assertThat(resultat).isNotNull();
        assertThat(resultat.getStatut()).isEqualTo(StatutDossier.DEPOSE);
        verify(dossierRepository, times(1)).save(any(DossierAMM.class));
        verify(historiqueRepository, times(1)).save(any(HistoriqueAction.class));
    }

    @Test
    @DisplayName("getDossierById : retourne le dossier existant")
    void getDossierById_devraitRetournerDossier() {
        when(dossierRepository.findById(1L)).thenReturn(Optional.of(dossierTest));

        DossierAMM resultat = dossierService.getDossierById(1L);

        assertThat(resultat).isNotNull();
        assertThat(resultat.getId()).isEqualTo(1L);
        assertThat(resultat.getNomMedicament()).isEqualTo("Amoxicilline 500mg");
    }

    @Test
    @DisplayName("getDossierById : lève exception si introuvable")
    void getDossierById_devraitLeverExceptionSiIntrouvable() {
        when(dossierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dossierService.getDossierById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getDossierByReference : retourne dossier par référence")
    void getDossierByReference_devraitRetournerDossier() {
        when(dossierRepository.findByReference("AMM-2024-0001")).thenReturn(Optional.of(dossierTest));

        DossierAMM resultat = dossierService.getDossierByReference("AMM-2024-0001");

        assertThat(resultat.getReference()).isEqualTo("AMM-2024-0001");
    }

    @Test
    @DisplayName("mettreAJourStatut : change statut et sauvegarde historique")
    void mettreAJourStatut_devraitChangerStatutEtSauvegarderHistorique() {
        when(dossierRepository.findById(1L)).thenReturn(Optional.of(dossierTest));
        when(dossierRepository.save(any(DossierAMM.class))).thenReturn(dossierTest);
        when(historiqueRepository.save(any(HistoriqueAction.class))).thenReturn(new HistoriqueAction());

        DossierAMM resultat = dossierService.mettreAJourStatut(
                1L, StatutDossier.EN_VERIFICATION, "ADMIN", "VERIFICATION", "OK");

        verify(dossierRepository).save(any(DossierAMM.class));
        verify(historiqueRepository).save(any(HistoriqueAction.class));
    }

    @Test
    @DisplayName("getTousDossiers : retourne liste complète")
    void getTousDossiers_devraitRetournerTousLesDossiers() {
        when(dossierRepository.findAll()).thenReturn(List.of(dossierTest));

        List<DossierAMM> dossiers = dossierService.getTousDossiers();

        assertThat(dossiers).hasSize(1);
        assertThat(dossiers.get(0).getNomMedicament()).isEqualTo("Amoxicilline 500mg");
    }

    @Test
    @DisplayName("getDossiersByStatut : filtre par statut")
    void getDossiersByStatut_devraitFiltrerParStatut() {
        when(dossierRepository.findByStatut(StatutDossier.DEPOSE)).thenReturn(List.of(dossierTest));

        List<DossierAMM> dossiers = dossierService.getDossiersByStatut(StatutDossier.DEPOSE);

        assertThat(dossiers).hasSize(1);
        assertThat(dossiers.get(0).getStatut()).isEqualTo(StatutDossier.DEPOSE);
    }
}
