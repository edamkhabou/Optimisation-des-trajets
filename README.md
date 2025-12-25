# 🚗 Optimisation des trajets de covoiturage avec Google Maps

## 📋 Description
Application web de gestion et optimisation de trajets de covoiturage intégrant Google Maps API et des algorithmes d'optimisation combinatoire.

## 🎯 Fonctionnalités

### 1. Gestion des entités
- ✅ CRUD complet pour Utilisateurs, Véhicules et Trajets
- ✅ Relations entre entités avec contraintes d'intégrité
- ✅ Validation des capacités et horaires

### 2. Optimisation des trajets
- ✅ Algorithme Nearest Neighbor (plus proche voisin)
- ✅ Algorithme d'insertion
- ✅ Recuit simulé (Simulated Annealing)
- ✅ Minimisation distance + temps total

### 3. Détection de conflits
- ✅ Dépassement de capacité véhicules
- ✅ Chevauchement d'horaires
- ✅ Incompatibilité de préférences

### 4. Intégration Google Maps
- ✅ Affichage markers départ/arrivée
- ✅ Tracé des routes optimisées
- ✅ Mise à jour dynamique

### 5. Statistiques
- ✅ Distance totale parcourue
- ✅ Temps moyen de trajet
- ✅ Taux de remplissage véhicules
- ✅ Nombre de conflits

## 🛠️ Stack Technique

- **Frontend**: HTML5, CSS3, JavaScript ES6+
- **Carte**: Google Maps JavaScript API
- **Backend**: Java 11, Servlets
- **Base de données**: MySQL via XAMPP
- **Build**: Maven
- **JSON**: Gson

## 📁 Structure du projet

```
Optimisation/
├── src/main/java/com/covoiturage/
│   ├── models/          # Entités métier
│   ├── dao/             # Data Access Objects
│   ├── services/        # Logique métier
│   ├── optimization/    # Algorithmes d'optimisation
│   ├── servlets/        # API REST
│   └── utils/           # Utilitaires
├── src/main/webapp/
│   ├── index.html       # Interface principale
│   ├── css/             # Styles
│   └── js/              # Scripts frontend
├── database/
│   └── schema.sql       # Schéma de base de données
└── pom.xml              # Configuration Maven
```

## 🚀 Installation

### Prérequis
- Java JDK 11+
- Apache Tomcat 9+
- XAMPP (MySQL)
- Maven 3.6+
- Clé API Google Maps

### Étapes

1. **Cloner le projet**
```bash
cd "d:/2IDSD/JAVA avan/Optimisation-des-trajets/Optimisation"
```

2. **Configurer MySQL**
```bash
# Démarrer XAMPP MySQL
# Créer la base de données
mysql -u root -p < database/schema.sql
```

3. **Configurer l'API Google Maps**
- Obtenir une clé API sur [Google Cloud Console](https://console.cloud.google.com/)
- Activer les APIs: Maps JavaScript API, Directions API, Distance Matrix API
- Remplacer `YOUR_API_KEY` dans `index.html`

4. **Configurer la base de données**
Éditer `src/main/resources/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/covoiturage_db
db.username=root
db.password=
```

5. **Compiler le projet**
```bash
mvn clean install
```

6. **Déployer sur Tomcat**
- Copier `target/covoiturage.war` dans le dossier `webapps/` de Tomcat
- Démarrer Tomcat
- Accéder à: `http://localhost:8080/covoiturage/`

## 📊 Utilisation

### 1. Ajouter des utilisateurs
- Cliquer sur "Ajouter Utilisateur"
- Renseigner nom, adresses, horaires, préférences

### 2. Ajouter des véhicules
- Cliquer sur "Ajouter Véhicule"
- Renseigner conducteur, capacité, disponibilité

### 3. Optimiser les trajets
- Cliquer sur "Optimiser"
- Sélectionner l'algorithme (Nearest Neighbor / Recuit Simulé)
- Les trajets optimisés s'affichent sur la carte

### 4. Consulter les statistiques
- Voir le tableau de bord avec distance, temps, taux de remplissage

## 🧪 Tests

Des jeux de données de test sont disponibles dans les fichiers de test.

```bash
mvn test
```

## 🔧 API REST Endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/utilisateurs` | Liste tous les utilisateurs |
| POST | `/api/utilisateurs` | Créer un utilisateur |
| PUT | `/api/utilisateurs/{id}` | Modifier un utilisateur |
| DELETE | `/api/utilisateurs/{id}` | Supprimer un utilisateur |
| GET | `/api/vehicules` | Liste tous les véhicules |
| POST | `/api/vehicules` | Créer un véhicule |
| GET | `/api/trajets` | Liste tous les trajets |
| POST | `/api/trajets/optimize` | Optimiser les trajets |
| GET | `/api/stats` | Obtenir les statistiques |

## 🎓 Algorithmes d'optimisation

### Nearest Neighbor (Plus proche voisin)
- Complexité: O(n²)
- Rapide, solution acceptable
- Bon pour petits ensembles

### Simulated Annealing (Recuit simulé)
- Complexité: O(n × iterations)
- Meilleure qualité de solution
- Évite les minima locaux

## 📝 Auteur

Projet universitaire - Optimisation combinatoire et Google Maps API

## 📄 Licence

MIT License - Projet éducatif
