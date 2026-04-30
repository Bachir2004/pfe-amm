.PHONY: start stop restart logs build clean ps backend-logs frontend-logs

## Démarrer tous les services
start:
	docker-compose up -d

## Arrêter tous les services
stop:
	docker-compose down

## Redémarrer tous les services
restart:
	docker-compose restart

## Voir les logs en temps réel
logs:
	docker-compose logs -f

## Logs backend seulement
backend-logs:
	docker-compose logs -f backend

## Logs frontend seulement
frontend-logs:
	docker-compose logs -f frontend

## Builder les images Docker
build:
	docker-compose build --no-cache

## Builder et démarrer
up: build start

## Statut des conteneurs
ps:
	docker-compose ps

## Tout arrêter et supprimer volumes (DANGER)
clean:
	docker-compose down -v --remove-orphans

## Lancer les tests Maven
test:
	cd backend && mvn test

## Builder le jar
package:
	cd backend && mvn package -DskipTests

## Analyse SonarQube
sonar:
	cd backend && mvn sonar:sonar

## Afficher l'aide
help:
	@echo "Commandes disponibles :"
	@echo "  make start    - Démarrer tous les services"
	@echo "  make stop     - Arrêter tous les services"
	@echo "  make restart  - Redémarrer les services"
	@echo "  make logs     - Voir tous les logs"
	@echo "  make build    - Builder les images Docker"
	@echo "  make clean    - Tout supprimer (volumes inclus)"
	@echo "  make test     - Lancer les tests"
	@echo "  make sonar    - Analyse SonarQube"
