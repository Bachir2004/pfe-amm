package ma.pfe.amm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.pfe.amm.dto.AuthResponse;
import ma.pfe.amm.dto.LoginRequest;
import ma.pfe.amm.model.Role;
import ma.pfe.amm.model.TypeLaboratoire;
import ma.pfe.amm.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request, authenticationManager));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody Map<String, String> body) {
        String nom            = body.get("nom");
        String email          = body.get("email");
        String password       = body.get("password");
        String nomLaboratoire = body.get("nomLaboratoire");
        String adresse        = body.get("adresse");
        String telephone      = body.get("telephone");
        String ice            = body.get("ice");

        TypeLaboratoire typeLaboratoire = null;
        if (body.get("typeLaboratoire") != null) {
            try { typeLaboratoire = TypeLaboratoire.valueOf(body.get("typeLaboratoire")); }
            catch (IllegalArgumentException ignored) {}
        }

        // L'inscription libre est toujours LABORATOIRE
        Role role = Role.LABORATOIRE;

        AuthResponse response = authService.register(
                nom, email, password, role,
                nomLaboratoire, adresse, telephone, ice, typeLaboratoire
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
