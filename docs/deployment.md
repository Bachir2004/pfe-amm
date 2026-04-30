# Guide de Déploiement — AMM

## Prérequis

| Outil | Version minimale |
|-------|-----------------|
| Docker Desktop | 24.x |
| Docker Compose | 2.x (intégré) |
| Java JDK | 17 |
| Maven | 3.9 |
| Git | 2.x |

---

## Déploiement Local (Développement)

### Étape 1 — Cloner le projet
```bash
git clone https://github.com/<user>/pfe-amm.git
cd pfe-amm
```

### Étape 2 — Configurer les variables d'environnement
```bash
# Le fichier .env est déjà présent avec les valeurs par défaut
# Modifier si nécessaire :
notepad .env   # Windows
nano .env      # Linux/Mac
```

### Étape 3 — Démarrer l'infrastructure Docker
```bash
docker-compose up -d postgres elasticsearch zeebe
# Attendre ~60 secondes que Zeebe soit prêt
docker-compose ps
```

### Étape 4 — Démarrer le backend
```bash
cd backend
mvn spring-boot:run
# API disponible sur http://localhost:8080/api
```

### Étape 5 — Ouvrir le frontend
```
Ouvrir frontend/index.html dans un navigateur
```

---

## Déploiement Full Docker

### Démarrer tous les services
```bash
docker-compose up -d
```

Ordre de démarrage automatique :
1. `postgres` + `elasticsearch` (en parallèle)
2. `zeebe` (attend elasticsearch)
3. `operate` (attend zeebe + elasticsearch)
4. `backend` (attend postgres + zeebe)
5. `frontend` (attend backend)
6. `prometheus` + `grafana` (indépendants)

### Vérifier l'état
```bash
docker-compose ps
# Tous les services doivent être "healthy" ou "running"
```

### Accéder aux services
| Service | URL | Login |
|---------|-----|-------|
| Frontend | http://localhost:3000 | Voir comptes test |
| Backend API | http://localhost:8080/api | Bearer JWT |
| Camunda Operate | http://localhost:8081 | — |
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3001 | admin / admin123 |

---

## Comptes de Test

| Email | Mot de passe | Rôle |
|-------|-------------|------|
| admin@amm.ma | admin123 | ADMIN |
| evaluateur@amm.ma | eval123 | EVALUATEUR |
| direction@amm.ma | dir123 | DIRECTION |
| labo@pharmamaroc.ma | labo123 | LABORATOIRE |

---

## Déploiement Production (Serveur Linux)

### Prérequis serveur
```bash
# Ubuntu 22.04+
sudo apt update
sudo apt install -y docker.io docker-compose-plugin git
sudo usermod -aG docker $USER
```

### Déploiement
```bash
git clone https://github.com/<user>/pfe-amm.git /opt/pfe-amm
cd /opt/pfe-amm

# Configurer les secrets
cp .env .env.prod
nano .env.prod   # Changer les mots de passe !

docker-compose --env-file .env.prod up -d
```

### Configuration CI/CD (GitHub Actions)

Ajouter ces secrets dans GitHub → Settings → Secrets :

| Secret | Description |
|--------|-------------|
| `DEPLOY_HOST` | IP du serveur |
| `DEPLOY_USER` | Utilisateur SSH |
| `DEPLOY_SSH_KEY` | Clé SSH privée |

---

## Vérification Post-Déploiement

```bash
# Santé de l'API
curl http://localhost:8080/actuator/health

# Test authentification
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@amm.ma","password":"admin123"}'

# Lister les dossiers (avec token)
curl http://localhost:8080/api/dossiers \
  -H "Authorization: Bearer <token>"
```

---

## Résolution de Problèmes

| Problème | Solution |
|----------|---------|
| Backend ne démarre pas | Vérifier que postgres et zeebe sont healthy : `docker-compose ps` |
| Erreur JWT | Vérifier que `JWT_SECRET` fait au moins 32 caractères |
| Zeebe ne se connecte pas | Attendre 60-90s après `docker-compose up`, Zeebe est lent à démarrer |
| Port 8080 occupé | Changer `server.port` dans application.yml |
| `docker-compose up` échoue | Vérifier que Docker Desktop est démarré |
