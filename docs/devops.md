# Documentation DevOps — Projet AMM

## Architecture Docker

```
┌─────────────────────────────────────────────────────────────┐
│                     amm-network (bridge)                    │
│                                                             │
│  ┌──────────┐    ┌──────────────┐    ┌─────────────────┐   │
│  │ frontend │    │   backend    │    │    postgres      │   │
│  │  :3000   │───>│   :8080      │───>│    :5432        │   │
│  │  nginx   │    │ Spring Boot  │    │  PostgreSQL 15   │   │
│  └──────────┘    └──────┬───────┘    └─────────────────┘   │
│                         │                                   │
│                  ┌──────▼───────┐    ┌─────────────────┐   │
│                  │    zeebe     │    │  elasticsearch  │   │
│                  │   :26500     │───>│    :9200        │   │
│                  │  Camunda 8   │    │     8.9.0       │   │
│                  └──────────────┘    └────────┬────────┘   │
│                                               │            │
│  ┌──────────┐    ┌──────────────┐    ┌────────▼────────┐   │
│  │ grafana  │    │  prometheus  │    │    operate      │   │
│  │  :3001   │<───│   :9090      │    │    :8081        │   │
│  └──────────┘    └──────────────┘    └─────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Pipeline CI/CD

```
Push to main/develop
        │
        ▼
┌───────────────────┐
│  CI: build-test   │  ← GitHub Actions (ci.yml)
│  • Java 17 setup  │
│  • mvn test       │
│  • JaCoCo report  │
│  • mvn package    │
└────────┬──────────┘
         │ success
         ▼
┌───────────────────┐
│  docker-build     │
│  • build backend  │
│  • build frontend │
└────────┬──────────┘
         │ (push to main only)
         ▼
┌───────────────────┐
│  CD: deploy       │  ← GitHub Actions (cd.yml)
│  • push to GHCR   │
│  • SSH to server  │
│  • docker-compose │
│    up -d          │
└───────────────────┘
```

## Monitoring Stack

| Service | URL | Credentials |
|---------|-----|-------------|
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3001 | admin / admin123 |
| Actuator Health | http://localhost:8080/actuator/health | — |
| Actuator Metrics | http://localhost:8080/actuator/metrics | — |
| Actuator Prometheus | http://localhost:8080/actuator/prometheus | — |

## Métriques Exposées

Le backend expose via Micrometer :
- `jvm.*` — métriques JVM (heap, GC, threads)
- `http.server.requests` — latence et taux d'erreur HTTP
- `hikaricp.*` — pool de connexions DB
- `process.*` — CPU et mémoire process

## Grafana — Tableaux de Bord Recommandés

Importer depuis Grafana.com :
- **JVM Micrometer** : ID 4701
- **Spring Boot Statistics** : ID 12900
- **PostgreSQL** : ID 9628

## Variables d'Environnement (.env)

| Variable | Description | Défaut |
|----------|-------------|--------|
| `DB_HOST` | Hôte PostgreSQL | `postgres` (Docker) / `localhost` (local) |
| `DB_NAME` | Nom base de données | `amm_db` |
| `DB_USER` | Utilisateur DB | `amm_user` |
| `DB_PASSWORD` | Mot de passe DB | `amm_pass` |
| `JWT_SECRET` | Clé secrète JWT (min 32 chars) | Voir `.env` |
| `JWT_EXPIRATION` | Durée token en ms | `86400000` (24h) |
| `ZEEBE_HOST` | Hôte Zeebe | `zeebe` (Docker) / `localhost` (local) |
| `GRAFANA_PASSWORD` | Mot de passe Grafana | `admin123` |

## Commandes Make

```bash
make start         # docker-compose up -d
make stop          # docker-compose down
make restart       # docker-compose restart
make build         # build images sans cache
make logs          # logs tous les services
make backend-logs  # logs backend seulement
make clean         # down + supprime volumes
make test          # mvn test
make sonar         # analyse SonarQube
```
