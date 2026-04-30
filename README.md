# Système de Gestion des AMM Pharmaceutiques

![CI](https://github.com/user/pfe-amm/actions/workflows/ci.yml/badge.svg)
![Docker](https://img.shields.io/badge/Docker-ready-blue?logo=docker)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-green?logo=springboot)
![Camunda](https://img.shields.io/badge/Camunda-8.3.0-purple)

> **PFE Ingénieur DevOps** — *Conception et implémentation d'une solution digitale de gestion des agréments pharmaceutiques basée sur les workflows BPMN.*

---

## Architecture

```
┌──────────────┐     ┌──────────────────┐     ┌─────────────┐
│   Frontend   │────>│  Spring Boot API │────>│ PostgreSQL  │
│ nginx :3000  │     │     :8080        │     │   :5432     │
└──────────────┘     └────────┬─────────┘     └─────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
      ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
      │ Zeebe :26500 │ │ Prometheus   │ │   Grafana    │
      │ Camunda 8    │ │   :9090      │ │   :3001      │
      └──────────────┘ └──────────────┘ └──────────────┘
```

## Stack Technique

| Couche | Technologie |
|--------|-------------|
| Backend | Spring Boot 3.1.5, Java 17 |
| Sécurité | Spring Security + JWT (jjwt 0.11.5) |
| Workflow | Camunda 8 Zeebe 8.3.0 |
| Base de données | PostgreSQL 15 |
| Frontend | HTML5 / CSS3 / JavaScript Vanilla |
| Monitoring | Prometheus + Grafana |
| CI/CD | GitHub Actions |
| Conteneurisation | Docker + Docker Compose |

## Démarrage Rapide

```bash
# 1. Cloner
git clone https://github.com/user/pfe-amm.git && cd pfe-amm

# 2. Démarrer tout avec Docker
docker-compose up -d

# 3. Accéder à l'application
# Frontend : http://localhost:3000
# API :      http://localhost:8080/api
# Operate :  http://localhost:8081
# Grafana :  http://localhost:3001
```

### Ou en développement local

```bash
# Infrastructure seulement
docker-compose up -d postgres elasticsearch zeebe

# Backend
cd backend && mvn spring-boot:run

# Frontend : ouvrir frontend/index.html
```

## Commandes Make

```bash
make start        # Démarrer tous les services
make stop         # Arrêter
make build        # Builder les images
make logs         # Voir les logs
make test         # Tests unitaires
make clean        # Tout supprimer
```

## Workflow BPMN

```
DÉPÔT ──► Vérification Admin ──► [Complet?]
                                      │
                    OUI ──────────────►│
                     Évaluation        │
                     Validation Dir.   │
                     [Décision?]      │
                     APPROUVÉ ──► Notification ──► FIN ✓
                     REJETÉ   ──► Notification ──► FIN ✗
                                      │
                    NON ──────────────►│
                     Demande Complément
                     Retour Laboratoire
                     └──► (retour Vérification)
```

## Comptes de Test

| Email | Password | Rôle |
|-------|----------|------|
| admin@amm.ma | admin123 | ADMIN |
| evaluateur@amm.ma | eval123 | EVALUATEUR |
| direction@amm.ma | dir123 | DIRECTION |
| labo@pharmamaroc.ma | labo123 | LABORATOIRE |

## API Endpoints

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | `/api/auth/login` | Connexion | Non |
| POST | `/api/auth/register` | Inscription | Non |
| POST | `/api/dossiers` | Créer dossier | Oui |
| GET | `/api/dossiers` | Lister dossiers | Oui |
| GET | `/api/dossiers/reference/{ref}` | Chercher par référence | Oui |
| POST | `/api/dossiers/{id}/verification` | Vérification admin | Oui |
| POST | `/api/dossiers/{id}/evaluation` | Évaluation | Oui |
| POST | `/api/dossiers/{id}/decision` | Décision direction | Oui |
| GET | `/api/statistiques` | Statistiques | Oui |
| GET | `/api/dossiers/{id}/historique` | Historique | Oui |
| GET | `/actuator/health` | Santé API | Non |
| GET | `/actuator/prometheus` | Métriques | Non |

## Documentation

- [Architecture](docs/architecture.md)
- [API Reference](docs/api-reference.md)
- [Guide DevOps](docs/devops.md)
- [Guide Déploiement](docs/deployment.md)

## Structure du Projet

```
pfe-amm/
├── .env                          # Variables d'environnement
├── .github/workflows/            # CI/CD GitHub Actions
│   ├── ci.yml                    # Build & Test
│   └── cd.yml                    # Deploy
├── Makefile                      # Commandes DevOps
├── docker-compose.yml            # Tous les services
├── monitoring/
│   └── prometheus.yml            # Config Prometheus
├── backend/
│   ├── Dockerfile                # Multi-stage build
│   ├── pom.xml                   # Dépendances Maven
│   └── src/main/java/ma/pfe/amm/
│       ├── config/               # Security, DataInit, ExceptionHandler
│       ├── controller/           # REST Controllers + AuthController
│       ├── dto/                  # DTOs (Login, Auth, Verification...)
│       ├── model/                # Entités JPA (DossierAMM, Utilisateur...)
│       ├── repository/           # Spring Data JPA
│       ├── security/             # JwtUtil, JwtAuthFilter
│       ├── service/              # Business Logic + AuthService
│       └── worker/               # Zeebe Job Workers
├── frontend/
│   ├── Dockerfile                # Nginx
│   ├── nginx.conf                # Proxy vers backend
│   └── index.html                # SPA avec Login JWT
└── docs/
    ├── architecture.md
    ├── api-reference.md
    ├── devops.md
    └── deployment.md
```

---

*Projet de Fin d'Études — Ingénieur DevOps — 2024/2025*
