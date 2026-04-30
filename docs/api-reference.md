# Référence API — AMM Backend

Base URL : `http://localhost:8080/api`

---

## Dossiers AMM

### POST /dossiers
Soumettre une nouvelle demande d'AMM.

**Body JSON :**
```json
{
  "nomMedicament": "Amoxicilline 500mg",
  "dci": "Amoxicilline",
  "nomLaboratoire": "Pharma Maroc",
  "emailLaboratoire": "contact@pharmamaroc.ma",
  "formePharmaceutique": "Comprimé",
  "dosage": "500mg"
}
```

**Réponse 201 :**
```json
{
  "id": 1,
  "reference": "AMM-2024-0001",
  "nomMedicament": "Amoxicilline 500mg",
  "statut": "DEPOSE",
  "processInstanceKey": 2251799813685248,
  "dateDepot": "2024-03-15T10:30:00"
}
```

---

### GET /dossiers
Lister tous les dossiers AMM.

**Réponse 200 :** `Array<DossierAMM>`

---

### GET /dossiers/{id}
Récupérer un dossier par son ID.

**Réponse 200 :** `DossierAMM`

**Réponse 404 :**
```json
{ "code": 404, "message": "Dossier introuvable avec l'id : 99" }
```

---

### GET /dossiers/reference/{reference}
Récupérer un dossier par sa référence.

**Exemple :** `GET /dossiers/reference/AMM-2024-0001`

**Réponse 200 :** `DossierAMM`

---

### GET /dossiers/statut/{statut}
Filtrer les dossiers par statut.

**Valeurs de statut :** `DEPOSE | EN_VERIFICATION | INCOMPLET | EN_EVALUATION | EN_VALIDATION | APPROUVE | REJETE | ANNULE`

**Exemple :** `GET /dossiers/statut/EN_VERIFICATION`

**Réponse 200 :** `Array<DossierAMM>`

---

### PUT /dossiers/{id}/statut
Mettre à jour manuellement le statut d'un dossier.

**Body JSON :**
```json
{
  "statut": "EN_VERIFICATION",
  "acteur": "ADMIN",
  "commentaire": "Dossier pris en charge"
}
```

**Réponse 200 :** `DossierAMM`

---

### GET /health
Vérifier l'état de l'API.

**Réponse 200 :**
```json
{
  "status": "UP",
  "service": "AMM Backend",
  "version": "1.0.0"
}
```

---

## Workflow (Actions sur le Processus Zeebe)

> **Note :** Le paramètre `jobKey` est la clé du job Zeebe actif.
> Obtenir les jobKeys via Camunda Operate : `http://localhost:8081`

---

### POST /dossiers/{id}/verification?jobKey={jobKey}
Compléter l'étape de vérification administrative.

**Body JSON :**
```json
{
  "dossierComplet": true,
  "commentaire": "Tous les documents sont présents"
}
```

**Réponse 200 :**
```json
{
  "message": "Vérification complétée",
  "dossierComplet": true,
  "nouveauStatut": "EN_EVALUATION"
}
```

Si `dossierComplet: false` → `nouveauStatut: "INCOMPLET"`

---

### POST /dossiers/{id}/evaluation?jobKey={jobKey}
Compléter l'évaluation technique.

**Body JSON :**
```json
{
  "commentaire": "Évaluation favorable, profil de sécurité acceptable"
}
```

**Réponse 200 :**
```json
{
  "message": "Évaluation complétée",
  "nouveauStatut": "EN_VALIDATION"
}
```

---

### POST /dossiers/{id}/decision?jobKey={jobKey}
Enregistrer la décision de la direction.

**Body JSON :**
```json
{
  "decision": "APPROUVE",
  "motif": "Dossier complet et évaluation favorable"
}
```

Ou :
```json
{
  "decision": "REJETE",
  "motif": "Données d'efficacité insuffisantes"
}
```

**Valeurs de décision :** `APPROUVE | REJETE`

**Réponse 200 :**
```json
{
  "message": "Décision enregistrée",
  "decision": "APPROUVE"
}
```

---

### POST /dossiers/{id}/complement?jobKey={jobKey}
Soumettre le complément de dossier (par le laboratoire).

**Body JSON :**
```json
{
  "commentaire": "Rapport d'études cliniques phases II-III ajouté"
}
```

**Réponse 200 :**
```json
{
  "message": "Complément soumis, retour en vérification",
  "nouveauStatut": "EN_VERIFICATION"
}
```

---

## Statistiques

### GET /statistiques
Obtenir les statistiques globales du système.

**Réponse 200 :**
```json
{
  "totalDossiers": 42,
  "deposes": 5,
  "enVerification": 8,
  "incomplets": 3,
  "enEvaluation": 7,
  "enValidation": 4,
  "approuves": 12,
  "rejetes": 3,
  "annules": 0,
  "parLaboratoire": {
    "Pharma Maroc": 15,
    "MedLab Casablanca": 10,
    "BioSanté": 17
  }
}
```

---

## Historique

### GET /dossiers/{id}/historique
Récupérer l'historique complet des actions sur un dossier, trié du plus récent au plus ancien.

**Réponse 200 :**
```json
[
  {
    "id": 5,
    "acteur": "ADMIN",
    "action": "VERIFICATION_ADMINISTRATIVE",
    "ancienStatut": "DEPOSE",
    "nouveauStatut": "EN_EVALUATION",
    "commentaire": "Dossier complet",
    "dateAction": "2024-03-15T14:22:00"
  },
  {
    "id": 1,
    "acteur": "SYSTEME",
    "action": "DEPOT_DOSSIER",
    "ancienStatut": null,
    "nouveauStatut": "DEPOSE",
    "commentaire": "Dossier AMM soumis par Pharma Maroc",
    "dateAction": "2024-03-15T10:30:00"
  }
]
```

---

## Codes d'Erreur

| Code | Description |
|------|-------------|
| 200  | Succès |
| 201  | Créé avec succès |
| 400  | Données invalides ou argument incorrect |
| 404  | Ressource introuvable |
| 500  | Erreur interne du serveur |

**Format d'erreur standard :**
```json
{
  "code": 404,
  "message": "Dossier introuvable avec la référence : AMM-2024-9999",
  "timestamp": "2024-03-15T14:30:00"
}
```

**Format d'erreur de validation (400) :**
```json
{
  "code": 400,
  "message": "Données invalides",
  "timestamp": "2024-03-15T14:30:00",
  "erreurs": {
    "nomMedicament": "Le nom du médicament est obligatoire",
    "emailLaboratoire": "Email invalide"
  }
}
```

---

## Exemple de Scénario Complet

```bash
# 1. Soumettre un dossier
curl -X POST http://localhost:8080/api/dossiers \
  -H "Content-Type: application/json" \
  -d '{"nomMedicament":"Paracétamol 1g","dci":"Paracétamol","nomLaboratoire":"MedLab","emailLaboratoire":"info@medlab.ma","formePharmaceutique":"Comprimé","dosage":"1g"}'

# → Réponse: {"reference":"AMM-2024-0001", "processInstanceKey": 123456, ...}

# 2. Récupérer l'ID du job de vérification depuis Operate (port 8081)
# jobKey = 987654

# 3. Compléter la vérification (dossier complet)
curl -X POST "http://localhost:8080/api/dossiers/1/verification?jobKey=987654" \
  -H "Content-Type: application/json" \
  -d '{"dossierComplet":true,"commentaire":"Tout est en ordre"}'

# 4. Compléter l'évaluation
curl -X POST "http://localhost:8080/api/dossiers/1/evaluation?jobKey=999111" \
  -H "Content-Type: application/json" \
  -d '{"commentaire":"Profil bénéfice/risque favorable"}'

# 5. Décision direction : APPROUVE
curl -X POST "http://localhost:8080/api/dossiers/1/decision?jobKey=888222" \
  -H "Content-Type: application/json" \
  -d '{"decision":"APPROUVE","motif":"Dossier excellent"}'

# → Le worker notification-approbation met automatiquement le statut à APPROUVE
```
