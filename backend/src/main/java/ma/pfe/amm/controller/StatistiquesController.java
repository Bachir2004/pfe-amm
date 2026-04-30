package ma.pfe.amm.controller;

import lombok.RequiredArgsConstructor;
import ma.pfe.amm.dto.StatistiquesDto;
import ma.pfe.amm.service.StatistiquesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistiques")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class StatistiquesController {

    private final StatistiquesService statistiquesService;

    @GetMapping
    public ResponseEntity<StatistiquesDto> getStatistiques() {
        return ResponseEntity.ok(statistiquesService.calculerStatistiques());
    }
}
