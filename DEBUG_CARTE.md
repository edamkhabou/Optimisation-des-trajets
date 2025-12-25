# 🗺️ Guide de Dépannage - Visualisation du Trajet

## ❌ Problème : La carte ne s'affiche pas

### Vérifications à faire :

1. **Ouvrir la Console JavaScript (F12)**
   - Appuyez sur `F12` dans votre navigateur
   - Allez dans l'onglet "Console"
   - Cherchez les erreurs en rouge

### Causes fréquentes :

#### 1. Leaflet pas chargé

**Symptôme :** Erreur `L is not defined`

**Solution :** Vérifiez que Leaflet est bien inclus dans `index.html` :
```html
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
```

#### 2. Div "map" manquante

**Symptôme :** Erreur `Map container not found`

**Solution :** Vérifiez que le div existe dans `index.html` :
```html
<div id="map" style="height: 500px;"></div>
```

#### 3. Coordonnées GPS manquantes

**Symptôme :** La carte s'affiche mais pas les marqueurs

**Solution :** Vérifiez que les utilisateurs ont des coordonnées dans la base de données :

```sql
-- Voir les utilisateurs
SELECT id, nom, latitude, longitude FROM utilisateurs;

-- Si les coordonnées sont nulles, ajoutez-les manuellement (Paris par exemple)
UPDATE utilisateurs SET latitude = 48.8566, longitude = 2.3522 WHERE id = 1;
UPDATE utilisateurs SET latitude = 48.8600, longitude = 2.3400 WHERE id = 2;
```

---

## 🔧 Solution Rapide : Mettre à jour les coordonnées

### Étape 1 : Se connecter à MySQL

```powershell
# XAMPP MySQL
C:\xampp\mysql\bin\mysql.exe -u root

# Puis dans MySQL :
USE covoiturage_db;
```

### Étape 2 : Vérifier les données

```sql
SELECT id, nom, adresse_depart, latitude, longitude FROM utilisateurs;
```

### Étape 3 : Ajouter des coordonnées de test (Paris)

```sql
-- Coordonnées autour de Paris
UPDATE utilisateurs SET 
    latitude = 48.8698, 
    longitude = 2.3322 
WHERE id = 1; -- Alice Martin

UPDATE utilisateurs SET 
    latitude = 48.8534, 
    longitude = 2.3438 
WHERE id = 2; -- Bob Dupont

UPDATE utilisateurs SET 
    latitude = 48.8510, 
    longitude = 2.3736 
WHERE id = 3; -- Charlie Rousseau

UPDATE utilisateurs SET 
    latitude = 48.8720, 
    longitude = 2.3825 
WHERE id = 4; -- Diana Laurent

UPDATE utilisateurs SET 
    latitude = 48.8656, 
    longitude = 2.3422 
WHERE id = 5; -- Etienne Bernard

UPDATE utilisateurs SET 
    latitude = 48.8634, 
    longitude = 2.3516 
WHERE id = 6; -- Fanny Petit
```

---

## 🧪 Tester la Visualisation

### Test 1 : Console du navigateur

Ouvrez **F12** → **Console**, vous devriez voir :
```
📍 Initialisation de la carte...
✅ Carte OpenStreetMap (Leaflet) initialisée
```

### Test 2 : Après optimisation

Après avoir cliqué sur "Optimiser le trajet", vous devriez voir :
```
🗺️ Affichage du trajet sur la carte: {utilisateurs: Array(4), ...}
📌 Affichage de 4 utilisateurs
🔍 Traitement des utilisateurs pour affichage...
   Utilisateur 1: Alice Martin Lat: 48.8698, Lng: 2.3322
   ✅ Marqueur 1 créé
   ...
🛣️ Création de la route avec 4 points
✅ Route tracée
🎯 Ajustement de la vue sur 4 points
✅ Trajet affiché avec succès !
```

### Test 3 : Marqueurs visibles

Sur la carte, vous devriez voir :
- ✅ Des marqueurs bleus numérotés (1, 2, 3...)
- ✅ Une ligne bleue reliant les marqueurs
- ✅ Popups au clic sur les marqueurs

---

## 🚀 Redéploiement

Si vous avez modifié le code, redéployez :

```powershell
# 1. Compiler
cd "D:\2IDSD\JAVA avan\Optimisation-des-trajets\Optimisation"
mvn clean package

# 2. Arrêter Tomcat
C:\Tomcat9\bin\shutdown.bat

# 3. Nettoyer l'ancien déploiement
Remove-Item "C:\Tomcat9\webapps\covoiturage.war" -ErrorAction SilentlyContinue
Remove-Item "C:\Tomcat9\webapps\covoiturage" -Recurse -ErrorAction SilentlyContinue

# 4. Copier le nouveau WAR
copy target\covoiturage.war C:\Tomcat9\webapps\

# 5. Redémarrer Tomcat
C:\Tomcat9\bin\startup.bat

# 6. Attendre 10 secondes puis accéder à :
# http://localhost:9090/covoiturage/
```

---

## 📋 Checklist de Dépannage

- [ ] **XAMPP MySQL démarré**
- [ ] **Base de données `covoiturage_db` existe**
- [ ] **Utilisateurs ont des coordonnées (latitude/longitude)**
- [ ] **Leaflet inclus dans index.html**
- [ ] **Div "map" présent dans index.html**
- [ ] **Projet compilé (`mvn clean package`)**
- [ ] **WAR déployé dans Tomcat**
- [ ] **Tomcat démarré sur port 9090**
- [ ] **Application accessible** (http://localhost:9090/covoiturage/)
- [ ] **Console navigateur sans erreurs (F12)**
- [ ] **Message "Carte initialisée" dans console**

---

## 🐛 Erreurs Fréquentes

### Erreur : "Cannot read property 'map' of undefined"

**Cause :** Le div "map" n'existe pas

**Solution :** Ajoutez-le dans `index.html` dans l'onglet Optimisation

### Erreur : "L is not defined"

**Cause :** Leaflet n'est pas chargé

**Solution :** Vérifiez les scripts CDN dans `<head>`

### Carte grise / vide

**Cause :** Tuiles OpenStreetMap non chargées

**Solution :** Vérifiez votre connexion internet

### Marqueurs ne s'affichent pas

**Cause :** Coordonnées nulles ou invalides

**Solution :** Mettez à jour la base de données (voir ci-dessus)

---

## 💡 Astuce pour la Soutenance

Si la visualisation ne fonctionne toujours pas :

1. **Utilisez les données de test** avec coordonnées pré-configurées
2. **Montrez les logs de la console** (F12) pour prouver que le code fonctionne
3. **Expliquez le processus** : géocodage → marqueurs → route

**Phrase clé :**
> "Le système récupère les coordonnées GPS depuis la base de données, crée des marqueurs numérotés sur OpenStreetMap, et trace automatiquement la route optimisée entre les points."

---

**La visualisation devrait maintenant fonctionner ! 🗺️✨**
