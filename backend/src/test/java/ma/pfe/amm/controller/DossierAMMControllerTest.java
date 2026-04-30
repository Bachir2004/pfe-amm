package ma.pfe.amm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.pfe.amm.model.DossierAMM;
import ma.pfe.amm.model.StatutDossier;
import ma.pfe.amm.security.JwtUtil;
import ma.pfe.amm.service.AuthService;
import ma.pfe.amm.service.CamundaService;
import ma.pfe.amm.service.DossierAMMService;
import ma.pfe.amm.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DossierAMMController.class)
@DisplayName("Tests intégration - DossierAMMController")
class DossierAMMControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DossierAMMService dossierService;

    @MockBean
    private CamundaService camundaService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private DossierAMM dossierTest;

    @BeforeEach
    void setUp() {
        dossierTest = new DossierAMM();
        dossierTest.setId(1L);
        dossierTest.setReference("AMM-2024-0001");
        dossierTest.setNomMedicament("Amoxicilline 500mg");
        dossierTest.setDci("Amoxicilline");
        dossierTest.setNomLaboratoire("Pharma Maroc");
        dossierTest.setEmailLaboratoire("contact@pharmamaroc.ma");
        dossierTest.setFormePharmaceutique("Comprime");
        dossierTest.setDosage("500mg");
        dossierTest.setStatut(StatutDossier.DEPOSE);
        dossierTest.setDateDepot(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/dossiers : retourne liste vide")
    void getTousDossiers_devraitRetournerListeVide() throws Exception {
        when(dossierService.getTousDossiers()).thenReturn(List.of());

        mockMvc.perform(get("/api/dossiers"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/dossiers : retourne liste avec dossiers")
    void getTousDossiers_devraitRetournerDossiers() throws Exception {
        when(dossierService.getTousDossiers()).thenReturn(List.of(dossierTest));

        mockMvc.perform(get("/api/dossiers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reference").value("AMM-2024-0001"))
                .andExpect(jsonPath("$[0].statut").value("DEPOSE"));
    }

    @Test
    @WithMockUser(roles = "LABORATOIRE")
    @DisplayName("POST /api/dossiers : crée un dossier avec succès")
    void creerDossier_devraitCreerAvecSucces() throws Exception {
        when(dossierService.creerDossier(any(DossierAMM.class))).thenReturn(dossierTest);
        when(camundaService.demarrerProcessus(any(), any(), any(), any())).thenReturn(123456L);
        doNothing().when(notificationService).notifierDepot(any());

        mockMvc.perform(post("/api/dossiers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dossierTest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("AMM-2024-0001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/dossiers/{id} : retourne 404 si introuvable")
    void getDossierById_devraitRetourner404() throws Exception {
        when(dossierService.getDossierById(99L))
                .thenThrow(new NoSuchElementException("Dossier introuvable avec l'id : 99"));

        mockMvc.perform(get("/api/dossiers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/dossiers/reference/{ref} : retourne dossier")
    void getDossierByReference_devraitRetournerDossier() throws Exception {
        when(dossierService.getDossierByReference("AMM-2024-0001")).thenReturn(dossierTest);

        mockMvc.perform(get("/api/dossiers/reference/AMM-2024-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomMedicament").value("Amoxicilline 500mg"));
    }

    @Test
    @DisplayName("GET /api/dossiers : retourne 403 sans authentification")
    void getDossiers_sansAuth_devraitRetourner403() throws Exception {
        mockMvc.perform(get("/api/dossiers"))
                .andExpect(status().isForbidden());
    }
}
