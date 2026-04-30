# Architecture du Système AMM

## Vue d'ensemble

Le système de gestion des AMM pharmaceutiques est une application web full-stack utilisant une architecture orientée services avec orchestration de processus métier via Camunda 8 Zeebe.

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND                                 │
│           HTML/CSS/JS  →  http://localhost:8080/api             │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTP/REST
┌──────────────────────────────▼──────────────────────────────────┐
│                      BACKEND (Spring Boot 3.2)                  │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │  Controllers │  │   Services   │  │   Job Workers        │  │
│  │              │  │              │  │                      │  │
│  │ DossierAMM   │  │ DossierAMM   │  │ NotifApprobation     │  │
│  │ Workflow     │  │ Camunda      │  │ NotifRejet           │  │
│  │ Statistiques │  │ Notification │  │                      │  │
│  │ Historique   │  │ Statistiques │  │                      │  │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘  │
│         │                 │                      │              │
│  ┌──────▼─────────────────▼──────────────────────▼───────────┐  │
│  │              Repository Layer (Spring Data JPA)            │  │
│  └──────────────────────────┬───────────────────────────────┘  │
└─────────────────────────────┼───────────────────────────────────┘
                              │
         ┌────────────────────┼──────────────────────┐
         │                   │                       │
┌────────▼───────┐  ┌────────▼────────┐  ┌──────────▼──────────┐
│  PostgreSQL 15 │  │  Zeebe (Camunda)│  │   Elasticsearch     │
│  port 5432     │  │  port 26500     │  │   port 9200         │
│                │  │                 │  │                     │
│  amm_db        │  │  Broker BPMN    │  │  Stockage Opérate   │
└────────────────┘  └────────┬────────┘  └─────────────────────┘
                             │
                    ┌────────▼────────┐
                    │ Camunda Operate │
                    │ port 8081       │
                    │                 │
                    │ Monitoring BPMN │
                    └─────────────────┘
```

## Flux du Processus BPMN

```
START
  │
  ▼
[UserTask] Vérification Administrative (Admin)
  │
  ▼
<Gateway> Dossier Complet ?
  │
  ├── OUI ──► [UserTask] Évaluation Technique (Évaluateur)
  │                │
  │                ▼
  │           [UserTask] Validation Direction (Directeur)
  │                │
  │                ▼
  │           <Gateway> Décision ?
  │                │
  │                ├── APPROUVÉ ──► [ServiceTask] Notification Approbation
  │                │                     │
  │                │                     ▼
  │                │                   [END] AMM Approuvée ✓
  │                │
  │                └── REJETÉ ───► [ServiceTask] Notification Rejet
  │                                      │
  │                                      ▼
  │                                    [END] AMM Rejetée ✗
  │
  └── NON ──► [UserTask] Demande de Complément (Admin)
                   │
                   ▼
              [UserTask] Retour Laboratoire
                   │
                   └──► (retour à Vérification Administrative)
```

## Structure des Packages

```
ma.pfe.amm/
├── AmmApplication.java           # Point d'entrée Spring Boot
├── config/
│   ├── CamundaDeploymentConfig   # Déploiement BPMN au démarrage
│   └── GlobalExceptionHandler    # Gestion globale des erreurs REST
├── controller/
│   ├── DossierAMMController      # CRUD dossiers (POST, GET, PUT)
│   ├── WorkflowController        # Actions workflow (vérif, eval, décision)
│   ├── StatistiquesController    # Tableau de bord statistiques
│   └── HistoriqueController      # Historique des actions par dossier
├── dto/
│   ├── VerificationRequest       # Payload vérification admin
│   ├── DecisionRequest           # Payload décision direction
│   └── StatistiquesDto           # Réponse statistiques
├── model/
│   ├── DossierAMM                # Entité principale (JPA)
│   ├── HistoriqueAction          # Audit trail (JPA)
│   └── StatutDossier             # Enum des statuts
├── repository/
│   ├── DossierAMMRepository      # Spring Data JPA
│   └── HistoriqueActionRepository
├── service/
│   ├── DossierAMMService         # Logique métier dossiers
│   ├── CamundaService            # Intégration Zeebe client
│   ├── NotificationService       # Envoi notifications (log simulé)
│   └── StatistiquesService       # Calcul indicateurs
└── worker/
    ├── NotificationApprobationWorker  # @JobWorker: notification-approbation
    └── NotificationRejetWorker        # @JobWorker: notification-rejet
```

## Modèle de Données

### Table `dossiers_amm`

| Colonne                  | Type        | Description                    |
|--------------------------|-------------|--------------------------------|
| id                       | BIGINT PK   | Identifiant auto-incrémenté    |
| reference                | VARCHAR UNIQUE | Format AMM-YYYY-XXXX        |
| nom_medicament           | VARCHAR     | Nom commercial du médicament   |
| nom_laboratoire          | VARCHAR     | Nom du laboratoire demandeur   |
| email_laboratoire        | VARCHAR     | Contact email laboratoire      |
| forme_pharmaceutique     | VARCHAR     | Comprimé, Sirop, Injectable... |
| dci                      | VARCHAR     | Dénomination commune internationale |
| dosage                   | VARCHAR     | Ex: 500mg, 10mg/5ml           |
| statut                   | ENUM        | StatutDossier                  |
| process_instance_key     | BIGINT      | Clé instance Zeebe             |
| commentaire_admin        | TEXT        | Commentaire administrateur     |
| commentaire_evaluateur   | TEXT        | Commentaire évaluateur         |
| date_depot               | TIMESTAMP   | Date de soumission             |
| date_derniere_modification | TIMESTAMP | Dernière mise à jour          |

### Table `historique_actions`

| Colonne        | Type      | Description                          |
|----------------|-----------|--------------------------------------|
| id             | BIGINT PK | Identifiant                          |
| dossier_id     | BIGINT FK | Référence vers dossiers_amm          |
| acteur         | VARCHAR   | Utilisateur ayant effectué l'action  |
| action         | VARCHAR   | Code de l'action                     |
| ancien_statut  | ENUM      | Statut avant l'action                |
| nouveau_statut | ENUM      | Statut après l'action                |
| commentaire    | TEXT      | Commentaire libre                    |
| date_action    | TIMESTAMP | Horodatage de l'action               |

## Décisions Architecturales

### 1. Camunda 8 vs Camunda 7
Camunda 8 avec Zeebe a été choisi pour sa scalabilité horizontale et son modèle cloud-native. Zeebe utilise une architecture event-sourced avec Elasticsearch pour la persistance des états de processus.

### 2. Job Workers vs Managed Tasks
Les service tasks utilisent le pattern Job Worker pour les notifications automatiques. Les user tasks sont complétées via l'API REST avec fourniture du `jobKey` (obtenu depuis Camunda Operate ou Tasklist).

### 3. Historique Audit
Chaque changement de statut est tracé dans `historique_actions` pour garantir une traçabilité complète du processus réglementaire.

### 4. Génération de Référence
Format `AMM-YYYY-XXXX` généré côté serveur à la soumission, garantissant l'unicité dans la base de données.
