# 📚 GUIDE D'UTILISATION - Optimisation des trajets de covoiturage

## 🎯 Vue d'ensemble

Cette application permet de gérer et d'optimiser automatiquement les trajets de covoiturage en utilisant des algorithmes d'optimisation combinatoire et l'API Google Maps.

---

## 🚀 Installation et Configuration

### Prérequis

- **Java JDK 11+** : [Télécharger Java](https://www.oracle.com/java/technologies/downloads/)
- **Apache Tomcat 9+** : [Télécharger Tomcat](https://tomcat.apache.org/download-90.cgi)
- **XAMPP (MySQL)** : [Télécharger XAMPP](https://www.apachefriends.org/)
- **Maven 3.6+** : [Télécharger Maven](https://maven.apache.org/download.cgi)
- **Clé API Google Maps** : [Obtenir une clé](https://console.cloud.google.com/)

### Étapes d'installation

#### 1. Configuration de la base de données

```bash
# Démarrer XAMPP MySQL
# Ouvrir phpMyAdmin ou utiliser la ligne de commande

# Créer la base de données
mysql -u root -p
source database/schema.sql
```

#### 2. Configuration de l'API Google Maps

**💰 Coût : GRATUIT pour usage de test/développement**

Google offre **200$ de crédit mensuel gratuit** (environ 28 000 chargements de carte/mois).
Pour un projet universitaire, c'est **totalement gratuit** !

**⚠️ Important :** Une carte bancaire est requise pour l'activation, mais vous ne serez PAS facturé si vous restez dans les limites gratuites.

**🚫 Pas de carte bancaire ?** Voir [ALTERNATIVES_SANS_CARTE.md](ALTERNATIVES_SANS_CARTE.md) pour :
- ✅ Utiliser OpenStreetMap (100% gratuit, aucune carte requise)
- ✅ Utiliser Leaflet.js (open source)
- ✅ Mode démo avec coordonnées statiques

**Étapes (avec carte bancaire) :**

1. Aller sur [Google Cloud Console](https://console.cloud.google.com/)
2. Créer un nouveau projet (ex: "covoiturage-projet")
3. Activer la **facturation** (requis mais gratuit avec crédit de 200$/mois)
4. Activer les APIs suivantes :
   - ✅ **Maps JavaScript API** (pour afficher la carte)
   - ✅ **Geocoding API** (pour convertir adresses → coordonnées)
   - ⚠️ **Directions API** (optionnel - pour itinéraires)
   - ⚠️ **Distance Matrix API** (optionnel - pour calcul distances)
5. Créer une **clé API** :
   - Aller dans "Identifiants" → "Créer des identifiants" → "Clé API"
   - **Important :** Restreindre la clé (recommandé) :
     - Restrictions HTTP : Ajouter `http://localhost:8080/*`
     - Restrictions API : Sélectionner uniquement les APIs activées
6. Remplacer `YOUR_API_KEY` dans `index.html` :

```html
<script src="https://maps.googleapis.com/maps/api/js?key=VOTRE_CLE_ICI&libraries=places"></script>
```

#### 3. Configuration de la base de données

Éditer `src/main/resources/db.properties` :

```properties
db.url=jdbc:mysql://localhost:3306/covoiturage_db?useSSL=false&serverTimezone=UTC
db.username=root
db.password=VOTRE_MOT_DE_PASSE
```

#### 4. Compilation du projet

```bash
cd "d:/2IDSD/JAVA avan/Optimisation-des-trajets/Optimisation"
mvn clean install
```

#### 5. Déploiement sur Tomcat

1. Copier `target/covoiturage.war` dans le dossier `webapps/` de Tomcat
2. Démarrer Tomcat :
   ```bash
   # Windows
   catalina.bat start
   
   # Linux/Mac
   ./catalina.sh start
   ```
3. Accéder à l'application : `http://localhost:8080/covoiturage/`

---

## 📖 Guide d'utilisation

### 1. Gestion des utilisateurs

**Ajouter un utilisateur :**

1. Aller dans l'onglet "👥 Utilisateurs"
2. Remplir le formulaire :
   - **Nom** : Nom complet de l'utilisateur
   - **Adresse de départ** : Adresse complète (ex: 15 Rue de la Paix, Paris)
   - **Adresse d'arrivée** : Destination finale
   - **Heure de départ** : Heure souhaitée (optionnel)
   - **Heure d'arrivée** : Heure d'arrivée souhaitée (optionnel)
   - **Groupe** : Groupe d'appartenance (ex: Entreprise A)
3. Cliquer sur "Ajouter l'utilisateur"

**Supprimer un utilisateur :**
- Cliquer sur le bouton "🗑️ Supprimer" dans la liste

### 2. Gestion des véhicules

**Ajouter un véhicule :**

1. Aller dans l'onglet "🚙 Véhicules"
2. Remplir le formulaire :
   - **Conducteur** : Sélectionner un utilisateur existant
   - **Immatriculation** : Plaque d'immatriculation (ex: AB-123-CD)
   - **Capacité** : Nombre de places disponibles (hors conducteur)
   - **Disponible** : Cocher si le véhicule est disponible
3. Cliquer sur "Ajouter le véhicule"

### 3. Optimisation des trajets

**Créer un trajet optimisé :**

1. Aller dans l'onglet "🎯 Optimisation"
2. Sélectionner :
   - **Véhicule** : Le véhicule à utiliser
   - **Algorithme** : 
     - *Plus proche voisin* : Rapide, bonne solution
     - *Recuit simulé* : Plus lent, meilleure solution
3. Sélectionner les passagers à transporter
4. Cliquer sur "🎯 Optimiser le trajet"

**Résultats affichés :**
- Distance totale du trajet
- Temps total estimé
- Nombre de passagers
- Taux de remplissage du véhicule
- Ordre optimal de prise en charge
- Visualisation sur la carte Google Maps

**Comparer les algorithmes :**
- Cliquer sur "📊 Comparer les algorithmes"
- Voir les différences de performance entre les deux méthodes

### 4. Consulter les statistiques

1. Aller dans l'onglet "📊 Statistiques"
2. Voir les métriques globales :
   - Nombre total de trajets
   - Distance moyenne parcourue
   - Taux de remplissage moyen
   - Impact environnemental (km et CO₂ économisés)

---

## 🔧 API REST Documentation

### Endpoints Utilisateurs

#### GET `/api/utilisateurs`
Liste tous les utilisateurs

**Réponse :**
```json
[
  {
    "id": 1,
    "nom": "Alice Martin",
    "adresseDepart": "15 Rue de la Paix, Paris",
    "adresseArrivee": "50 Avenue des Champs-Élysées, Paris",
    "heureDepart": "08:00:00",
    "heureArrivee": "09:00:00",
    "groupe": "Entreprise A"
  }
]
```

#### POST `/api/utilisateurs`
Crée un nouvel utilisateur

**Corps de la requête :**
```json
{
  "nom": "Bob Dupont",
  "adresseDepart": "25 Boulevard Saint-Michel, Paris",
  "adresseArrivee": "100 Rue de Rivoli, Paris",
  "heureDepart": "08:15:00",
  "heureArrivee": "09:15:00",
  "groupe": "Entreprise A"
}
```

#### DELETE `/api/utilisateurs?id=1`
Supprime un utilisateur

### Endpoints Optimisation

#### POST `/api/optimiser`
Optimise un trajet

**Corps de la requête :**
```json
{
  "vehiculeId": 1,
  "utilisateurIds": [1, 2, 3, 4],
  "algorithme": "simulated_annealing"
}
```

**Réponse :**
```json
{
  "id": 1,
  "vehiculeId": 1,
  "distanceTotale": 12.5,
  "tempsTotalMinutes": 35,
  "utilisateurs": [...],
  "optimise": true
}
```

#### POST `/api/optimiser?action=comparer`
Compare les algorithmes

**Réponse :**
```json
{
  "nearestNeighbor": {
    "nom": "Nearest Neighbor",
    "distance": 13.2,
    "temps": 38,
    "executionMs": 5
  },
  "simulatedAnnealing": {
    "nom": "Simulated Annealing",
    "distance": 12.5,
    "temps": 35,
    "executionMs": 150
  },
  "meilleur": "Simulated Annealing",
  "amelioration": 5.3
}
```

### Endpoints Statistiques

#### GET `/api/stats`
Récupère toutes les statistiques

**Réponse :**
```json
{
  "totalTrajets": 10,
  "distanceTotale": 125.5,
  "distanceMoyenne": 12.55,
  "tempsMoyen": 36.2,
  "tauxRemplissageMoyen": 75.5,
  "kmEconomises": 250.3,
  "co2EconomiseKg": 30.04
}
```

---

## 🧪 Tests et Validation

### Jeux de données de test

La base de données contient déjà des données de test (voir `database/schema.sql`):
- 6 utilisateurs
- 3 véhicules
- 2 trajets d'exemple

### Scénarios de test recommandés

1. **Test de capacité** :
   - Créer un trajet avec plus de passagers que la capacité du véhicule
   - Vérifier que le système détecte le conflit

2. **Test d'optimisation** :
   - Créer un trajet avec 4-5 utilisateurs
   - Comparer les deux algorithmes
   - Vérifier que le recuit simulé donne une meilleure solution

3. **Test d'horaires** :
   - Ajouter des utilisateurs avec des horaires incompatibles
   - Vérifier la détection de conflits

---

## 🎓 Algorithmes d'optimisation

### Nearest Neighbor (Plus proche voisin)

**Principe :**
1. Partir du premier utilisateur
2. À chaque étape, choisir l'utilisateur non visité le plus proche
3. Répéter jusqu'à épuisement

**Caractéristiques :**
- ✅ Rapide : O(n²)
- ✅ Simple à implémenter
- ❌ Peut rester bloqué dans un minimum local
- 📊 Bon pour < 20 utilisateurs

### Simulated Annealing (Recuit simulé)

**Principe :**
1. Partir d'une solution aléatoire
2. Température initiale élevée
3. À chaque itération :
   - Générer une solution voisine (swap ou 2-opt)
   - Accepter si meilleure OU avec probabilité P = e^(-ΔE/T)
   - Réduire température : T = T × α
4. Répéter jusqu'à convergence

**Caractéristiques :**
- ✅ Évite les minima locaux
- ✅ Meilleure qualité de solution
- ❌ Plus lent : O(n × iterations)
- 📊 Recommandé pour > 10 utilisateurs

**Paramètres par défaut :**
- Température initiale : 1000.0
- Taux de refroidissement (α) : 0.95
- Iterations : 1000

---

## 🐛 Dépannage

### Erreur : "Impossible de se connecter à la base de données"

**Solution :**
1. Vérifier que MySQL est démarré dans XAMPP
2. Vérifier les identifiants dans `db.properties`
3. Vérifier que la base `covoiturage_db` existe

### Erreur : "La carte Google Maps ne s'affiche pas"

**Solution :**
1. Vérifier que la clé API est correcte
2. Vérifier que les APIs sont activées dans Google Cloud Console
3. Vérifier la console du navigateur pour les erreurs

### Erreur : "Servlet introuvable"

**Solution :**
1. Vérifier que le WAR est bien déployé dans Tomcat
2. Redémarrer Tomcat
3. Vérifier les logs dans `tomcat/logs/catalina.out`

---

## 📊 Performance et Optimisations

### Recommandations de performance

- **Nombre d'utilisateurs** : 
  - < 10 : Utiliser Nearest Neighbor
  - 10-50 : Utiliser Simulated Annealing
  - > 50 : Diviser en plusieurs trajets

- **Cache** :
  - Les distances entre utilisateurs peuvent être mises en cache
  - Utiliser Redis ou Memcached pour les résultats d'optimisation

- **Base de données** :
  - Index créés automatiquement
  - Connexions poolées (configurées dans db.properties)

---

## 📄 Licence

Projet universitaire - Optimisation combinatoire et Google Maps API

---

## 👨‍💻 Support

Pour toute question ou problème :
1. Consulter cette documentation
2. Vérifier les logs dans `tomcat/logs/`
3. Consulter le code source (commentaires détaillés)

---

**Bon covoiturage optimisé ! 🚗💨**
