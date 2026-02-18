# 🏦 OpenVault : Système de Gestion Bancaire Full-Stack

[![Java CI with Maven](https://github.com/votre-username/openvault/actions/workflows/maven.yml/badge.svg)](https://github.com/votre-username/openvault/actions)
![Java Version](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)
![Angular](https://img.shields.io/badge/Angular-17-red)

## 📌 Présentation
**OpenVault** est une application web de gestion bancaire moderne et sécurisée. Ce projet a été conçu pour démontrer une maîtrise complète du développement d'entreprise en utilisant l'écosystème **Java/Spring Boot** pour le backend et **Angular** pour le frontend.

L'objectif principal est de garantir la **fiabilité absolue des transactions** et une **expérience utilisateur fluide**, en respectant les standards de l'industrie Fintech.

## 🚀 Fonctionnalités
- **Dashboard Client** : Visualisation en temps réel du solde et des dernières activités.
- **Virements Sécurisés** : Transferts atomiques entre comptes avec gestion transactionnelle (`@Transactional`).
- **Authentification JWT** : Sécurisation des routes et gestion des sessions via JSON Web Tokens.
- **Historique Détaillé** : Filtrage et consultation des transactions passées.
- **Gestion de Profil** : Génération automatique d'IBAN et sécurisation des données personnelles.

## 🛠 Stack Technique
- **Backend** : Java 21, Spring Boot 3, Spring Security, Spring Data JPA (Hibernate).
- **Frontend** : Angular 17, TypeScript, Tailwind CSS, RxJS.
- **Base de données** : PostgreSQL.
- **Documentation API** : Swagger UI / OpenAPI 3.
- **Qualité & Tests** : JUnit 5, Mockito, Jacoco (couverture de code).
- **Infrastructure** : Docker, Docker-compose.

## 🏗 Points Clés de l'Architecture
### Backend (Rigueur & Robustesse)
- **Architecture en Couches** : Séparation stricte entre Contrôleurs, Services et Dépôts.
- **Précision Monétaire** : Utilisation exclusive de `BigDecimal` pour éviter les erreurs d'arrondi sur les montants.
- **Gestion des Conflits** : Implémentation du *Optimistic Locking* pour prévenir les problèmes lors de virements simultanés.

### Frontend (Modernité & Typage)
- **Standalone Components** : Utilisation des dernières fonctionnalités d'Angular pour une application plus légère.
- **Intercepteurs HTTP** : Injection automatique du token JWT dans les headers de chaque requête.
- **Guards & Resolvers** : Protection des routes sensibles et pré-chargement des données bancaires.

## 🔒 Focus sur la Sécurité
- Mots de passe hashés avec **BCrypt**.
- Protection contre les failles CSRF et injection SQL.
- Validation stricte des données d'entrée (Bean Validation).

## 📦 Installation
Le projet est entièrement conteneurisé avec Docker pour un déploiement simplifié.

### Prérequis
- Docker et Docker Compose installés sur votre machine

### Démarrage rapide

```bash
# Cloner le dépôt
git clone https://github.com/votre-username/openvault.git
cd openvault

# Lancer l'intégralité de la stack (PostgreSQL, API Spring Boot, Frontend Angular)
docker compose up --build
```

**L'application sera accessible sur :**
- **Application Web** : http://localhost:4200
- **API Swagger** : http://localhost:8080/api/swagger-ui.html
- **API Base URL** : http://localhost:8080/api

### Commandes utiles

```bash
# Démarrer en arrière-plan
docker compose up -d

# Voir les logs
docker compose logs -f

# Arrêter les services
docker compose down

# Arrêter et supprimer les volumes (⚠️ supprime les données)
docker compose down -v

# Reconstruire les images
docker compose build --no-cache
```



## 🧪 Tests
```bash
# Exécuter les tests unitaires et d'intégration
mvn test
```