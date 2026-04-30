# 🏥 Système de Gestion des AMM — AMMPS

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Camunda](https://img.shields.io/badge/Camunda%208-Zeebe-FC5D0D?style=for-the-badge&logo=camunda&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

**Plateforme de gestion des Autorisations de Mise sur le Marché pharmaceutique**

*Projet de Fin d'Études — 2026*

[![CI](https://github.com/Bachir2004/pfe-amm/actions/workflows/ci.yml/badge.svg)](https://github.com/Bachir2004/pfe-amm/actions/workflows/ci.yml)

</div>

---

## 📋 Description

Système web complet de gestion du processus d'**Autorisation de Mise sur le Marché (AMM)** pharmaceutique, développé pour l'**AMMPS** (Agence Marocaine du Médicament et des Produits de Santé) en partenariat avec **CDG Incept**.

Le système automatise le workflow BPMN complet : dépôt du dossier → vérification administrative → évaluation scientifique → décision de la direction → délivrance de l'AMM.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Frontend (SPA)                     │
│          Vanilla JS · HTML5 · CSS3                   │
└──────────────────────┬──────────────────────────────┘
                       │ REST API (JWT)
┌──────────────────────▼──────────────────────────────┐
│              Backend Spring Boot 3.1                 │
│     Spring Security · JPA · Validation · Actuator    │
└────┬─────────────────┬──────────────────────────────┘
     │                 │
┌────▼────┐    ┌───────▼──────┐    ┌─────────────────┐
│Postgres │    │  Camunda 8   │    │  Elasticsearch  │
│   15    │    │  Zeebe BPMN  │◄───│      8.9        │
└─────────┘    └──────────────┘    └─────────────────┘
                      │
              ┌───────▼──────┐
              │   Operate    │
              │  (Monitoring)│
              └──────────────┘
```

---

## 👥 Rôles & Accès

| Rôle | Responsabilité | Accès |
|------|---------------|-------|
| **LABORATOIRE** | Soumet les demandes AMM | Ses propres dossiers |
| **ADMIN** | Vérifie la complétude administrative | Tous les dossiers |
| **EVALUATEUR** | Évalue scientifiquement le dossier | Tous les dossiers |
| **DIRECTION** | Prend la décision finale (AMM/Rejet) | Tous les dossiers |

---

## 🔄 Workflow BPMN

```
[Dépôt] → [Vérification Admin] → ◆ Complet?
                                    ├── NON → [Demande Complément] → [Retour Labo] ↩
                                    └── OUI → [Évaluation Technique] → [Validation Direction]
                                                                              ├── APPROUVÉ → [AMM délivrée ✅]
                                                                              └── REJETÉ   → [Notification ❌]
```

---

## 🚀 Démarrage rapide

### Prérequis
- Docker Desktop
- Java 17+
- Maven 3.9+

### 1. Cloner le projet
```bash
git clone https://github.com/Bachir2004/pfe-amm.git
cd pfe-amm
```

### 2. Configurer l'environnement
```bash
cp .env.example .env
# Éditer .env avec vos valeurs
```

### 3. Lancer avec Docker Compose
```bash
docker-compose up -d postgres elasticsearch zeebe operate
```

### 4. Lancer le backend
```bash
cd backend
mvn spring-boot:run
```

### 5. Ouvrir le frontend
Ouvrir `frontend/index.html` dans le navigateur.

---

## 🔑 Comptes de test

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Admin | admin@amm.ma | admin123 |
| Évaluateur | evaluateur@amm.ma | eval123 |
| Direction | direction@amm.ma | dir123 |
| Laboratoire | labo@amm.ma | labo123 |

---

## 🌐 URLs des services

| Service | URL |
|---------|-----|
| Backend API | http://localhost:8080 |
| API Health | http://localhost:8080/api/dossiers/health |
| Actuator | http://localhost:8080/actuator |
| Camunda Operate | http://localhost:8081 |
| Elasticsearch | http://localhost:9200 |

---

## 🧪 Tests API (Newman)

```bash
npm install -g newman newman-reporter-htmlextra
newman run postman/AMM-Collection.json \
  -e postman/AMM-Environment.json \
  --reporters cli,htmlextra \
  --reporter-htmlextra-export postman/rapport-tests.html
```

**Résultats : 23 requêtes · 65 assertions · 100% de réussite**

---

## 📁 Structure du projet

```
pfe-amm/
├── backend/                          # Spring Boot API
│   └── src/main/java/ma/pfe/amm/
│       ├── controller/               # REST Controllers
│       ├── service/                  # Logique métier
│       ├── model/                    # Entités JPA
│       ├── repository/               # Spring Data JPA
│       ├── security/                 # JWT + Spring Security
│       ├── config/                   # Configuration
│       └── worker/                   # Camunda Job Workers
│   └── src/main/resources/
│       ├── application.yml
│       └── bpmn/processus-amm.bpmn  # Workflow BPMN
├── frontend/                         # SPA Vanilla JS
│   ├── index.html
│   ├── bpmn-viewer.html
│   └── assets/
├── postman/                          # Tests API Newman
├── monitoring/                       # Prometheus
├── docs/                             # Documentation
├── docker-compose.yml
└── .github/workflows/                # CI/CD
```

---

## ⚙️ Stack technique

| Couche | Technologie | Version |
|--------|------------|---------|
| Backend | Spring Boot | 3.1.5 |
| Langage | Java | 17 |
| Workflow | Camunda 8 / Zeebe | 8.3.0 |
| Base de données | PostgreSQL | 15 |
| Search | Elasticsearch | 8.9.0 |
| Sécurité | JWT + Spring Security | — |
| Conteneurisation | Docker Compose | — |
| Tests API | Newman (Postman CLI) | 6.2.2 |
| CI/CD | GitHub Actions | — |
| Monitoring | Prometheus + Grafana | — |

---

## 📊 API Endpoints principaux

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/register` | Inscription laboratoire |
| POST | `/api/auth/login` | Authentification JWT |
| GET | `/api/dossiers` | Liste des dossiers |
| POST | `/api/dossiers` | Soumettre une demande AMM |
| GET | `/api/dossiers/{id}/historique` | Historique des actions |
| POST | `/api/dossiers/{id}/verification` | Vérification administrative |
| POST | `/api/dossiers/{id}/evaluation` | Évaluation scientifique |
| POST | `/api/dossiers/{id}/decision` | Décision finale |
| GET | `/api/statistiques` | Statistiques globales |
| GET | `/api/dossiers/health` | Health check |

---

## 👨‍💻 Auteur

**EL BACHIR BABOUZID**
Projet de Fin d'Études — Génie Informatique — 2026

<div align="center">

Développé en partenariat avec **CDG Incept** pour l'**AMMPS**

*Agence Marocaine du Médicament et des Produits de Santé*

</div>
