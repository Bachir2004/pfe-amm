package ma.pfe.amm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.pfe.amm.dto.AuthResponse;
import ma.pfe.amm.dto.LoginRequest;
import ma.pfe.amm.model.Role;
import ma.pfe.amm.model.TypeLaboratoire;
import ma.pfe.amm.model.Utilisateur;
import ma.pfe.amm.repository.UtilisateurRepository;
import ma.pfe.amm.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));
    }

    public AuthResponse login(LoginRequest request, AuthenticationManager authManager) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        Utilisateur u = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        String token = jwtUtil.generateToken(u,
                Map.of("role", u.getRole().name(), "nom", u.getNom(), "id", u.getId()));

        log.info("Connexion réussie : {} ({})", u.getEmail(), u.getRole());
        return buildResponse(token, u);
    }

    public AuthResponse register(String nom, String email, String password, Role role,
                                  String nomLaboratoire, String adresse, String telephone,
                                  String ice, TypeLaboratoire typeLaboratoire) {
        if (utilisateurRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email déjà utilisé : " + email);
        }
        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        u.setRole(role);
        u.setNomLaboratoire(nomLaboratoire);
        u.setAdresse(adresse);
        u.setTelephone(telephone);
        u.setIce(ice);
        u.setTypeLaboratoire(typeLaboratoire);
        utilisateurRepository.save(u);

        String token = jwtUtil.generateToken(u,
                Map.of("role", role.name(), "nom", nom, "id", u.getId()));

        log.info("Inscription réussie : {} ({})", email, role);
        return buildResponse(token, u);
    }

    private AuthResponse buildResponse(String token, Utilisateur u) {
        return new AuthResponse(
                token,
                u.getRole().name(),
                u.getNom(),
                u.getEmail(),
                u.getId(),
                u.getNomLaboratoire(),
                u.getTypeLaboratoire() != null ? u.getTypeLaboratoire().name() : null
        );
    }
}
