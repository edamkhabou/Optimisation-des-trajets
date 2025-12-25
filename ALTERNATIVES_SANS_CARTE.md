# 🗺️ Alternatives Sans Carte Bancaire

## 🎯 Problème : Pas de carte bancaire pour Google Maps API

**Pas de panique !** Voici plusieurs solutions **100% gratuites** qui ne nécessitent **AUCUNE carte bancaire**.

---

## ✅ Solution 1 : OpenStreetMap + Leaflet.js (RECOMMANDÉ)

### Avantages
- ✅ **100% gratuit** - Aucune limite, aucun compte requis
- ✅ **Aucune carte bancaire** nécessaire
- ✅ **Open source** et communautaire
- ✅ **Très similaire** à Google Maps visuellement
- ✅ **Facile à intégrer** - Juste inclure un script

### Installation

#### Étape 1 : Modifier `index.html`

Remplacez la section Google Maps par Leaflet :

```html
<!-- SUPPRIMER CETTE LIGNE -->
<!-- <script src="https://maps.googleapis.com/maps/api/js?key=YOUR_API_KEY&libraries=places"></script> -->

<!-- AJOUTER CES LIGNES À LA PLACE -->
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
```

#### Étape 2 : Modifier `js/maps.js`

Remplacez le contenu par ce code adapté pour Leaflet :

```javascript
// ============================================
// CONFIGURATION LEAFLET (OpenStreetMap)
// ============================================

let map;
let markers = [];

/**
 * Initialise la carte Leaflet
 */
function initMap() {
    // Centre sur Paris par défaut
    map = L.map('map').setView([48.8566, 2.3522], 12);
    
    // Ajouter les tuiles OpenStreetMap (gratuites)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19
    }).addTo(map);
    
    console.log('Carte Leaflet initialisée');
}

/**
 * Affiche un trajet sur la carte
 */
function afficherTrajetSurCarte(trajet) {
    if (!map) {
        console.error('Carte non initialisée');
        return;
    }
    
    // Effacer les marqueurs existants
    markers.forEach(marker => map.removeLayer(marker));
    markers = [];
    
    if (!trajet.utilisateurs || trajet.utilisateurs.length === 0) {
        return;
    }
    
    const bounds = [];
    
    // Ajouter un marqueur pour chaque utilisateur
    trajet.utilisateurs.forEach((user, index) => {
        if (user.latitude && user.longitude) {
            const lat = parseFloat(user.latitude);
            const lng = parseFloat(user.longitude);
            
            // Créer le marqueur
            const marker = L.marker([lat, lng]).addTo(map);
            
            // Popup avec infos
            const popupContent = `
                <b>Ordre ${index + 1}: ${user.nom}</b><br>
                📍 ${user.adresseDepart}
            `;
            marker.bindPopup(popupContent);
            
            markers.push(marker);
            bounds.push([lat, lng]);
        }
    });
    
    // Ajuster la vue pour montrer tous les marqueurs
    if (bounds.length > 0) {
        map.fitBounds(bounds, { padding: [50, 50] });
    }
    
    // Tracer une ligne entre les points (ordre du trajet)
    if (bounds.length > 1) {
        const polyline = L.polyline(bounds, {
            color: '#4285f4',
            weight: 3,
            opacity: 0.7
        }).addTo(map);
        markers.push(polyline);
    }
}

/**
 * Géocodage simple avec Nominatim (OpenStreetMap)
 */
async function geocoderAdresse(adresse, index, user) {
    try {
        const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(adresse)}`;
        
        const response = await fetch(url, {
            headers: {
                'User-Agent': 'Covoiturage-App/1.0'
            }
        });
        
        const data = await response.json();
        
        if (data && data.length > 0) {
            user.latitude = parseFloat(data[0].lat);
            user.longitude = parseFloat(data[0].lon);
            console.log(`Géocodé: ${adresse} → ${user.latitude}, ${user.longitude}`);
        } else {
            console.warn(`Adresse non trouvée: ${adresse}`);
        }
    } catch (error) {
        console.error('Erreur géocodage:', error);
    }
}

/**
 * Affiche des marqueurs pour une liste d'utilisateurs
 */
function afficherMarqueurs(utilisateurs) {
    if (!map) return;
    
    // Effacer les anciens marqueurs
    markers.forEach(marker => map.removeLayer(marker));
    markers = [];
    
    const bounds = [];
    
    utilisateurs.forEach(user => {
        if (user.latitude && user.longitude) {
            const lat = parseFloat(user.latitude);
            const lng = parseFloat(user.longitude);
            
            const marker = L.marker([lat, lng]).addTo(map);
            marker.bindPopup(`<b>${user.nom}</b><br>${user.adresseDepart}`);
            
            markers.push(marker);
            bounds.push([lat, lng]);
        }
    });
    
    if (bounds.length > 0) {
        map.fitBounds(bounds, { padding: [50, 50] });
    }
}

// Initialiser la carte au chargement
document.addEventListener('DOMContentLoaded', () => {
    initMap();
});
```

#### Étape 3 : Vérifier que ça fonctionne

1. Démarrez Tomcat
2. Accédez à `http://localhost:8080/covoiturage/`
3. Vous devriez voir une carte OpenStreetMap !

### ✅ Résultat

- ✅ Carte interactive fonctionnelle
- ✅ Marqueurs pour chaque utilisateur
- ✅ Lignes pour visualiser le trajet
- ✅ Géocodage gratuit avec Nominatim
- ✅ **Aucune clé API requise**
- ✅ **Aucune carte bancaire**

---

## ✅ Solution 2 : Mapbox (Gratuit jusqu'à 50 000 requêtes/mois)

### Avantages
- ✅ Interface moderne et élégante
- ✅ 50 000 requêtes gratuites par mois
- ✅ **Pas de carte bancaire** pour le plan gratuit
- ✅ Directions API gratuite

### Installation

1. Créer un compte gratuit sur [Mapbox](https://www.mapbox.com/)
2. Récupérer votre **token d'accès** (aucune carte bancaire requise)
3. Utiliser dans `index.html` :

```html
<link href='https://api.mapbox.com/mapbox-gl-js/v2.15.0/mapbox-gl.css' rel='stylesheet' />
<script src='https://api.mapbox.com/mapbox-gl-js/v2.15.0/mapbox-gl.js'></script>

<script>
mapboxgl.accessToken = 'VOTRE_TOKEN_MAPBOX';
const map = new mapboxgl.Map({
    container: 'map',
    style: 'mapbox://styles/mapbox/streets-v12',
    center: [2.3522, 48.8566],
    zoom: 12
});
</script>
```

---

## ✅ Solution 3 : Mode Démo avec Coordonnées Statiques

Si vous voulez juste une **démonstration pour la soutenance**, utilisez des coordonnées pré-définies.

### Modifier `schema.sql`

Les données de test incluent déjà des coordonnées GPS pour Paris :

```sql
-- Déjà dans le fichier schema.sql
INSERT INTO utilisateurs (nom, adresse_depart, adresse_arrivee, heure_depart, heure_arrivee, groupe, latitude, longitude) VALUES
('Alice Martin', '15 Rue de la Paix, Paris', '50 Avenue des Champs-Élysées, Paris', '08:00:00', '09:00:00', 'Entreprise A', 48.8698, 2.3322),
('Bob Dupont', '25 Boulevard Saint-Michel, Paris', '100 Rue de Rivoli, Paris', '08:15:00', '09:15:00', 'Entreprise A', 48.8534, 2.3438);
```

### Avantages
- ✅ **Aucune API externe** nécessaire
- ✅ Fonctionne **hors ligne**
- ✅ Parfait pour **soutenance/démo**
- ✅ Utiliser avec Leaflet (Solution 1)

---

## ✅ Solution 4 : Demander l'aide d'un proche

### Options
1. **Parent/Ami** : Demander à quelqu'un de confiance d'utiliser sa carte
2. **Carte virtuelle** : Utiliser Revolut, N26, ou carte prépayée
3. **Professeur/Université** : Demander si l'université a un compte Google Cloud

---

## 📊 Comparaison des Solutions

| Solution | Gratuit | Carte bancaire | Qualité | Facilité |
|----------|---------|----------------|---------|----------|
| **Leaflet + OSM** | ✅ Illimité | ❌ Non | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Mapbox** | ✅ 50k/mois | ❌ Non | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Google Maps** | ✅ 200$/mois | ✅ Oui | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Coordonnées statiques** | ✅ Illimité | ❌ Non | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## 🎓 Pour la Soutenance

### Que dire au jury ?

**Option 1 : Vous utilisez Leaflet/OpenStreetMap**
> "Pour des raisons de simplicité et d'accessibilité, j'ai utilisé OpenStreetMap via Leaflet.js, une solution 100% gratuite et open-source qui ne nécessite aucune clé API. C'est une alternative professionnelle utilisée par de nombreuses entreprises comme Facebook et Apple."

**Option 2 : Vous utilisez des coordonnées statiques**
> "Pour cette démonstration, j'ai pré-configuré les coordonnées GPS des utilisateurs. Dans un environnement de production, on intégrerait une API de géocodage comme Nominatim (gratuit) ou Google Maps."

**Points positifs à mentionner :**
- ✅ Conscience des coûts et alternatives
- ✅ Solution open-source et éthique
- ✅ Architecture flexible (facile de changer de provider)
- ✅ Respect de la vie privée (pas de tracking Google)

---

## 🔧 Installation Rapide (Leaflet)

```bash
# Aucune installation nécessaire !
# Juste remplacer le script dans index.html
```

**Fichiers à modifier :**
1. `index.html` - Changer le script Google Maps → Leaflet
2. `js/maps.js` - Adapter le code (voir code ci-dessus)

**Temps requis :** 10 minutes ⏱️

---

## 📚 Ressources

- [Leaflet Documentation](https://leafletjs.com/)
- [OpenStreetMap](https://www.openstreetmap.org/)
- [Nominatim API (Géocodage)](https://nominatim.org/release-docs/latest/api/Overview/)
- [Mapbox](https://www.mapbox.com/)

---

## ✅ Recommandation Finale

**Pour votre projet universitaire, utilisez la Solution 1 : Leaflet + OpenStreetMap**

**Pourquoi ?**
- ✅ 100% gratuit à vie
- ✅ Aucune carte bancaire
- ✅ Aucune limite d'utilisation
- ✅ Open source et éthique
- ✅ Qualité professionnelle
- ✅ Installation en 10 minutes

---

**Votre projet fonctionnera parfaitement sans Google Maps ni carte bancaire ! 🗺️✨**
