# 📘 DOCUMENTATION TECHNIQUE - Optimisation des trajets de covoiturage

## 📑 Table des matières

1. [Architecture du système](#architecture)
2. [Modèle de données](#modèle-de-données)
3. [Algorithmes d'optimisation](#algorithmes)
4. [API REST](#api-rest)
5. [Frontend](#frontend)
6. [Bonnes pratiques](#bonnes-pratiques)
7. [Exemples de code](#exemples)

---

## 🏗️ Architecture du système <a name="architecture"></a>

### Architecture en couches

```
┌─────────────────────────────────────────┐
│         PRÉSENTATION (Frontend)         │
│   HTML5, CSS3, JavaScript, Google Maps  │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│          API REST (Servlets)            │
│  UtilisateurServlet, VehiculeServlet,   │
│  TrajetServlet, OptimisationServlet     │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│       LOGIQUE MÉTIER (Services)         │
│  OptimisationService, ConflitService,   │
│        StatistiqueService               │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│     ALGORITHMES D'OPTIMISATION          │
│   NearestNeighbor, SimulatedAnnealing   │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│      ACCÈS DONNÉES (DAO Pattern)        │
│  UtilisateurDAO, VehiculeDAO, TrajetDAO │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│        BASE DE DONNÉES (MySQL)          │
│  utilisateurs, vehicules, trajets,      │
│  trajet_utilisateurs, conflits          │
└─────────────────────────────────────────┘
```

### Technologies utilisées

| Couche | Technologie | Version |
|--------|-------------|---------|
| Frontend | HTML5, CSS3, JavaScript | ES6+ |
| API Maps | Google Maps JavaScript API | v3 |
| Serveur | Apache Tomcat | 9+ |
| Backend | Java | 11+ |
| API REST | Servlets | 4.0 |
| Build | Maven | 3.6+ |
| Base de données | MySQL | 8.0+ |
| JSON | Gson | 2.10.1 |

---

## 🗄️ Modèle de données <a name="modèle-de-données"></a>

### Diagramme ER

```
┌─────────────────┐
│  UTILISATEURS   │
├─────────────────┤
│ id (PK)         │
│ nom             │
│ adresse_depart  │
│ adresse_arrivee │
│ heure_depart    │
│ heure_arrivee   │
│ preferences     │
│ groupe          │
│ latitude        │
│ longitude       │
└─────────────────┘
         │ 1
         │
         │ conducteur
         ├────────────────┐
         │                │
         │ *              │ 1
┌────────▼───────┐  ┌────▼──────────┐
│   VEHICULES    │  │ TRAJET_UTIL.  │
├────────────────┤  ├───────────────┤
│ id (PK)        │  │ trajet_id (FK)│
│ conducteur_id  │  │ util_id (FK)  │
│ immatriculation│  │ ordre         │
│ capacite       │  └───────────────┘
│ disponible     │         │ *
└────────────────┘         │
         │ 1               │
         │                 │
         │ *               │
    ┌────▼─────────────────▼─┐
    │      TRAJETS           │
    ├────────────────────────┤
    │ id (PK)                │
    │ vehicule_id (FK)       │
    │ distance_totale        │
    │ temps_total_minutes    │
    │ route_polyline         │
    │ optimise               │
    └────────────────────────┘
```

### Tables principales

#### utilisateurs
```sql
CREATE TABLE utilisateurs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    adresse_depart VARCHAR(255) NOT NULL,
    adresse_arrivee VARCHAR(255) NOT NULL,
    heure_depart TIME,
    heure_arrivee TIME,
    preferences TEXT,
    groupe VARCHAR(50),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8)
);
```

#### vehicules
```sql
CREATE TABLE vehicules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conducteur_id BIGINT NOT NULL,
    immatriculation VARCHAR(20) NOT NULL UNIQUE,
    capacite INT NOT NULL CHECK (capacite > 0),
    heure_debut_disponibilite TIME,
    heure_fin_disponibilite TIME,
    disponible BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (conducteur_id) REFERENCES utilisateurs(id)
);
```

#### trajets
```sql
CREATE TABLE trajets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vehicule_id BIGINT NOT NULL,
    distance_totale DECIMAL(10, 2) DEFAULT 0,
    temps_total_minutes DECIMAL(10, 2) DEFAULT 0,
    route_polyline TEXT,
    optimise BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (vehicule_id) REFERENCES vehicules(id)
);
```

---

## 🧮 Algorithmes d'optimisation <a name="algorithmes"></a>

### 1. Nearest Neighbor (Plus proche voisin)

#### Pseudocode

```
ALGORITHME NearestNeighbor(utilisateurs, vehicule)
ENTRÉES:
    utilisateurs : Liste d'utilisateurs à transporter
    vehicule : Véhicule utilisé
SORTIE:
    Solution optimisée

DÉBUT
    ordre_optimise ← []
    visites ← ensemble vide
    courant ← utilisateurs[0]
    
    AJOUTER courant À ordre_optimise
    AJOUTER courant À visites
    
    TANT QUE taille(visites) < taille(utilisateurs) FAIRE
        plus_proche ← NULL
        distance_min ← INFINI
        
        POUR CHAQUE utilisateur DANS utilisateurs FAIRE
            SI utilisateur NON DANS visites ALORS
                distance ← calculerDistance(courant, utilisateur)
                
                SI distance < distance_min ALORS
                    distance_min ← distance
                    plus_proche ← utilisateur
                FIN SI
            FIN SI
        FIN POUR
        
        AJOUTER plus_proche À ordre_optimise
        AJOUTER plus_proche À visites
        courant ← plus_proche
    FIN TANT QUE
    
    solution ← créerSolution(ordre_optimise)
    calculerMetriques(solution)
    
    RETOURNER solution
FIN
```

#### Complexité

- **Temporelle** : O(n²) où n = nombre d'utilisateurs
- **Spatiale** : O(n)

#### Avantages et inconvénients

✅ **Avantages:**
- Rapide et efficace
- Solution garantie en temps raisonnable
- Facile à comprendre et implémenter
- Bon pour petits ensembles (< 20 utilisateurs)

❌ **Inconvénients:**
- Peut rester bloqué dans un minimum local
- Dépend fortement du point de départ
- Pas toujours la solution optimale globale

### 2. Simulated Annealing (Recuit simulé)

#### Pseudocode

```
ALGORITHME SimulatedAnnealing(utilisateurs, vehicule)
ENTRÉES:
    utilisateurs : Liste d'utilisateurs
    vehicule : Véhicule utilisé
SORTIE:
    Solution optimisée

PARAMÈTRES:
    T0 = 1000.0          // Température initiale
    α = 0.95             // Taux de refroidissement
    iterations = 1000    // Nombre d'itérations
    Tmin = 1.0          // Température minimale

DÉBUT
    solution_courante ← genererSolutionAleatoire(utilisateurs)
    meilleure_solution ← copier(solution_courante)
    T ← T0
    
    POUR i DE 1 À iterations ET T > Tmin FAIRE
        // Générer une solution voisine
        solution_voisine ← genererVoisin(solution_courante)
        
        // Calculer la différence d'énergie (coût)
        ΔE ← solution_voisine.cout - solution_courante.cout
        
        SI ΔE < 0 ALORS
            // Meilleure solution → accepter
            solution_courante ← solution_voisine
            
            SI solution_courante.cout < meilleure_solution.cout ALORS
                meilleure_solution ← copier(solution_courante)
            FIN SI
        SINON
            // Solution moins bonne → accepter avec probabilité
            P ← exp(-ΔE / T)
            
            SI random() < P ALORS
                solution_courante ← solution_voisine
            FIN SI
        FIN SI
        
        // Refroidir la température
        T ← T * α
    FIN POUR
    
    RETOURNER meilleure_solution
FIN

FONCTION genererVoisin(solution)
    SI random() < 0.5 ALORS
        // Opérateur Swap : échanger 2 utilisateurs
        i ← randomInt(0, n-1)
        j ← randomInt(0, n-1)
        échanger(solution[i], solution[j])
    SINON
        // Opérateur 2-opt : inverser un segment
        i ← randomInt(0, n-1)
        j ← randomInt(0, n-1)
        inverser(solution, min(i,j), max(i,j))
    FIN SI
    
    RETOURNER solution
FIN
```

#### Complexité

- **Temporelle** : O(n × iterations)
- **Spatiale** : O(n)

#### Paramètres clés

| Paramètre | Valeur par défaut | Impact |
|-----------|-------------------|--------|
| Température initiale (T₀) | 1000.0 | ↑ Plus d'exploration |
| Taux refroidissement (α) | 0.95 | ↓ Convergence plus lente |
| Nombre d'itérations | 1000 | ↑ Meilleure solution |

#### Avantages et inconvénients

✅ **Avantages:**
- Évite les minima locaux
- Meilleure qualité de solution
- Flexible et adaptable
- Bon pour ensembles moyens/grands (> 10)

❌ **Inconvénients:**
- Plus lent que les heuristiques simples
- Nécessite réglage des paramètres
- Résultat non déterministe

### Comparaison des algorithmes

| Critère | Nearest Neighbor | Simulated Annealing |
|---------|------------------|---------------------|
| Vitesse | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| Qualité | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Complexité | Simple | Moyenne |
| Déterministe | Oui | Non |
| Recommandé pour | < 20 utilisateurs | > 10 utilisateurs |

---

## 🌐 API REST <a name="api-rest"></a>

### Architecture REST

L'API suit les principes RESTful :
- URLs significatives
- Méthodes HTTP appropriées (GET, POST, PUT, DELETE)
- Codes de statut HTTP standards
- Format JSON pour les échanges

### Endpoints complets

#### Utilisateurs

```
GET    /api/utilisateurs           → Liste tous les utilisateurs
GET    /api/utilisateurs?id=1      → Récupère l'utilisateur ID 1
GET    /api/utilisateurs?groupe=A  → Utilisateurs du groupe A
POST   /api/utilisateurs           → Crée un utilisateur
PUT    /api/utilisateurs           → Met à jour un utilisateur
DELETE /api/utilisateurs?id=1      → Supprime l'utilisateur ID 1
```

#### Véhicules

```
GET    /api/vehicules                  → Liste tous les véhicules
GET    /api/vehicules?id=1             → Récupère le véhicule ID 1
GET    /api/vehicules?disponible=true  → Véhicules disponibles
POST   /api/vehicules                  → Crée un véhicule
PUT    /api/vehicules                  → Met à jour un véhicule
DELETE /api/vehicules?id=1             → Supprime le véhicule ID 1
```

#### Trajets

```
GET    /api/trajets              → Liste tous les trajets
GET    /api/trajets?id=1         → Récupère le trajet ID 1
GET    /api/trajets?vehiculeId=1 → Trajets du véhicule ID 1
DELETE /api/trajets?id=1         → Supprime le trajet ID 1
```

#### Optimisation

```
POST /api/optimiser                  → Optimise un trajet
POST /api/optimiser?action=comparer  → Compare les algorithmes
```

#### Statistiques

```
GET /api/stats → Récupère toutes les statistiques
```

### Format des réponses

#### Succès (200 OK)
```json
{
  "id": 1,
  "nom": "Alice Martin",
  "adresseDepart": "15 Rue de la Paix, Paris",
  "adresseArrivee": "50 Avenue des Champs-Élysées, Paris"
}
```

#### Erreur (400, 404, 500)
```json
{
  "error": "Description de l'erreur"
}
```

---

## 🎨 Frontend <a name="frontend"></a>

### Architecture JavaScript

```
app.js          → Logique principale, gestion CRUD
maps.js         → Intégration Google Maps
```

### Fonctions principales

#### Gestion des utilisateurs
```javascript
async function chargerUtilisateurs()
async function ajouterUtilisateur(event)
async function supprimerUtilisateur(id)
```

#### Optimisation
```javascript
async function optimiserTrajet(event)
async function comparerAlgorithmes()
function afficherResultatsOptimisation(trajet)
```

#### Google Maps
```javascript
function initMap()
function afficherTrajetSurCarte(trajet)
function afficherMarqueurs(utilisateurs)
function geocoderAdresse(adresse, index, user)
```

---

## ✨ Bonnes pratiques <a name="bonnes-pratiques"></a>

### Code Java

1. **Pattern DAO** : Séparation claire accès données / logique métier
2. **Gestion des exceptions** : Try-catch systématique avec logging
3. **Injection de dépendances** : Services injectés dans servlets
4. **Commentaires** : Javadoc sur toutes les classes et méthodes publiques
5. **Constantes** : Valeurs en dur extraites en constantes

### Base de données

1. **Index** : Sur toutes les clés étrangères et champs de recherche
2. **Contraintes** : Intégrité référentielle avec CASCADE
3. **Transactions** : Pour opérations complexes (trajet + utilisateurs)
4. **Vues** : Pour requêtes complexes répétitives
5. **Procédures stockées** : Pour logique complexe côté BD

### Frontend

1. **Async/Await** : Pour toutes les requêtes API
2. **Gestion d'erreurs** : Try-catch + messages utilisateur
3. **Validation** : Côté client ET serveur
4. **Responsive** : Media queries pour mobile
5. **Performance** : Minimiser les appels API

---

## 💡 Exemples de code <a name="exemples"></a>

### Exemple 1: Créer et optimiser un trajet

```java
// Backend - OptimisationService.java
public Trajet optimiserTrajet(Long vehiculeId, List<Long> utilisateurIds, 
                              String typeAlgorithme) throws SQLException {
    
    // 1. Charger le véhicule
    Vehicule vehicule = vehiculeDAO.findById(vehiculeId)
        .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable"));
    
    // 2. Charger les utilisateurs
    List<Utilisateur> utilisateurs = new ArrayList<>();
    for (Long userId : utilisateurIds) {
        utilisateurs.add(utilisateurDAO.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable")));
    }
    
    // 3. Sélectionner l'algorithme
    OptimisationAlgorithme algorithme = selectionnerAlgorithme(typeAlgorithme);
    
    // 4. Optimiser
    Solution solution = algorithme.optimiser(utilisateurs, vehicule);
    
    // 5. Créer le trajet
    Trajet trajet = new Trajet();
    trajet.setVehicule(vehicule);
    trajet.setUtilisateurs(solution.getOrdreUtilisateurs());
    trajet.setDistanceTotale(solution.getDistanceTotale());
    trajet.setOptimise(true);
    
    // 6. Sauvegarder
    return trajetDAO.create(trajet);
}
```

```javascript
// Frontend - app.js
async function optimiserTrajet(event) {
    event.preventDefault();
    
    const data = {
        vehiculeId: parseInt(document.getElementById('vehiculeId').value),
        utilisateurIds: getSelectedPassagers(),
        algorithme: document.getElementById('algorithme').value
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/optimiser`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        const trajet = await response.json();
        afficherResultatsOptimisation(trajet);
        afficherTrajetSurCarte(trajet);
        
    } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur lors de l\'optimisation');
    }
}
```

### Exemple 2: Détection de conflits

```java
public List<Conflit> detecterConflits(Trajet trajet) {
    List<Conflit> conflits = new ArrayList<>();
    
    // Vérifier la capacité
    if (trajet.getUtilisateurs().size() > trajet.getVehicule().getCapacite()) {
        Conflit conflit = new Conflit();
        conflit.setType(Conflit.TypeConflit.CAPACITE);
        conflit.setMessage(String.format(
            "Capacité dépassée: %d passagers pour %d places",
            trajet.getUtilisateurs().size(),
            trajet.getVehicule().getCapacite()
        ));
        conflits.add(conflit);
    }
    
    // Vérifier les horaires
    for (int i = 0; i < utilisateurs.size(); i++) {
        for (int j = i + 1; j < utilisateurs.size(); j++) {
            if (!utilisateurs.get(i).horairesCompatibles(utilisateurs.get(j))) {
                Conflit conflit = new Conflit();
                conflit.setType(Conflit.TypeConflit.HORAIRE);
                conflits.add(conflit);
            }
        }
    }
    
    return conflits;
}
```

---

**Fin de la documentation technique**

Pour plus d'informations, consulter :
- [README.md](README.md) - Vue d'ensemble
- [GUIDE_UTILISATION.md](GUIDE_UTILISATION.md) - Guide utilisateur
- Code source (commentaires Javadoc)
