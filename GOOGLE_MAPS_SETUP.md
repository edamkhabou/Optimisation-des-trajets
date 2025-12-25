# 🗺️ Configuration Google Maps API - Guide Détaillé

## 💰 Coût et Limites Gratuites

### ✅ C'EST GRATUIT pour ce projet !

Google Maps Platform offre :
- **200 $ de crédit mensuel GRATUIT** (renouvelé chaque mois)
- **Pas de facturation automatique** (vous devez activer manuellement la facturation au-delà)

### 📊 Limites gratuites mensuelles (avec 200$)

| API | Utilisation gratuite | Suffisant pour |
|-----|---------------------|----------------|
| Maps JavaScript API | ~28 000 chargements | ✅ 900 par jour |
| Geocoding API | ~40 000 requêtes | ✅ 1 300 par jour |
| Directions API | ~40 000 requêtes | ✅ 1 300 par jour |
| Distance Matrix API | ~40 000 éléments | ✅ 1 300 par jour |

**Pour un projet universitaire de démonstration : Largement suffisant !** 🎓

---

## 🚀 Installation Étape par Étape

### Étape 1 : Créer un compte Google Cloud

1. Allez sur [Google Cloud Console](https://console.cloud.google.com/)
2. Connectez-vous avec votre compte Google (Gmail)
3. Acceptez les conditions d'utilisation

### Étape 2 : Créer un projet

1. Cliquez sur le **sélecteur de projet** (en haut à gauche)
2. Cliquez sur **"NOUVEAU PROJET"**
3. Nom du projet : `covoiturage-optimisation`
4. Cliquez sur **"CRÉER"**
5. Attendez quelques secondes, puis sélectionnez le projet créé

### Étape 3 : Activer la facturation (REQUIS mais GRATUIT)

⚠️ **Une carte bancaire est requise, mais vous ne serez PAS débité automatiquement**

1. Dans le menu (☰), allez dans **"Facturation"**
2. Cliquez sur **"Associer un compte de facturation"**
3. Sélectionnez **"Créer un compte de facturation"**
4. Remplissez vos informations :
   - Type de compte : **Particulier**
   - Pays : Sélectionnez votre pays
   - Carte bancaire : Entrez les détails (non débitée si < 200$/mois)
5. Cochez **"J'accepte les conditions d'utilisation"**
6. Cliquez sur **"DÉMARRER MON ESSAI GRATUIT"**

**🎁 Bonus :** Nouveaux comptes reçoivent parfois 300$ de crédit supplémentaire pour 90 jours !

### Étape 4 : Activer les APIs Google Maps

1. Dans le menu (☰), allez dans **"APIs et services"** → **"Bibliothèque"**

2. **Maps JavaScript API** (OBLIGATOIRE) :
   - Recherchez "Maps JavaScript API"
   - Cliquez dessus
   - Cliquez sur **"ACTIVER"**

3. **Geocoding API** (OBLIGATOIRE pour ce projet) :
   - Recherchez "Geocoding API"
   - Cliquez dessus
   - Cliquez sur **"ACTIVER"**

4. **Directions API** (OPTIONNEL - pour itinéraires) :
   - Recherchez "Directions API"
   - Cliquez dessus
   - Cliquez sur **"ACTIVER"**

5. **Distance Matrix API** (OPTIONNEL - pour calculs de distance) :
   - Recherchez "Distance Matrix API"
   - Cliquez dessus
   - Cliquez sur **"ACTIVER"**

### Étape 5 : Créer une clé API

1. Allez dans **"APIs et services"** → **"Identifiants"**
2. Cliquez sur **"+ CRÉER DES IDENTIFIANTS"**
3. Sélectionnez **"Clé API"**
4. Une clé sera générée (ex: `AIzaSyB1234567890abcdefghijklmnopqr`)

### Étape 6 : Sécuriser la clé API (RECOMMANDÉ)

⚠️ **Important :** Ne partagez jamais votre clé API publiquement !

1. Cliquez sur **"RESTREINDRE LA CLÉ"** dans la popup
2. Ou allez dans "Identifiants" → Cliquez sur votre clé

**Restrictions d'application :**
- Sélectionnez **"Référents HTTP (sites web)"**
- Ajoutez :
  ```
  http://localhost:8080/*
  http://127.0.0.1:8080/*
  ```
  
**Restrictions d'API :**
- Sélectionnez **"Restreindre la clé"**
- Cochez :
  - ✅ Maps JavaScript API
  - ✅ Geocoding API
  - ✅ Directions API (si activée)
  - ✅ Distance Matrix API (si activée)

3. Cliquez sur **"ENREGISTRER"**

### Étape 7 : Intégrer la clé dans le projet

1. Copiez votre clé API (ex: `AIzaSyB1234567890abcdefghijklmnopqr`)

2. Ouvrez `src/main/webapp/index.html`

3. Remplacez à la ligne ~15 :
   ```html
   <!-- AVANT -->
   <script src="https://maps.googleapis.com/maps/api/js?key=YOUR_API_KEY&libraries=places"></script>
   
   <!-- APRÈS -->
   <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyB1234567890abcdefghijklmnopqr&libraries=places"></script>
   ```

4. Sauvegardez le fichier

---

## ✅ Vérification de l'installation

### Test 1 : Vérifier l'activation des APIs

```
https://console.cloud.google.com/apis/dashboard
```
Vous devriez voir vos APIs activées avec des graphiques.

### Test 2 : Tester la clé API

Ouvrez cette URL dans votre navigateur (remplacez YOUR_KEY) :
```
https://maps.googleapis.com/maps/api/geocode/json?address=Paris&key=YOUR_KEY
```

**Réponse attendue :** JSON avec coordonnées de Paris
**Erreur :** Vérifiez la clé et les restrictions

### Test 3 : Tester dans l'application

1. Démarrez Tomcat
2. Accédez à `http://localhost:8080/covoiturage/`
3. La carte devrait s'afficher sans erreur
4. Ouvrez la **Console du navigateur** (F12)
5. Vérifiez qu'il n'y a pas d'erreur Google Maps

---

## 🔍 Surveillance de l'utilisation

### Voir votre consommation

1. Allez sur [APIs Dashboard](https://console.cloud.google.com/apis/dashboard)
2. Cliquez sur une API (ex: Maps JavaScript API)
3. Onglet **"Métriques"** : Voir les requêtes par jour
4. Onglet **"Quotas"** : Voir les limites

### Voir les coûts

1. Menu (☰) → **"Facturation"** → **"Rapports"**
2. Vous verrez :
   - Crédit gratuit restant (ex: 198,50$ sur 200$)
   - Utilisation par API
   - Prévision mensuelle

**Astuce :** Configurez une alerte à 10$ pour être notifié si vous approchez de la limite gratuite.

---

## 🛡️ Bonnes Pratiques de Sécurité

### ✅ À FAIRE

1. **Toujours restreindre la clé API** (HTTP referrers + APIs spécifiques)
2. **Ne jamais committer la clé dans Git** :
   ```bash
   # Ajouter dans .gitignore
   **/index.html  # Si la clé est en dur
   ```
3. **Utiliser des variables d'environnement en production**
4. **Créer des clés différentes pour dev/test/prod**
5. **Surveiller l'utilisation régulièrement**

### ❌ À ÉVITER

1. ❌ Partager la clé publiquement (GitHub, forums)
2. ❌ Utiliser la même clé pour plusieurs projets
3. ❌ Laisser la clé sans restrictions
4. ❌ Oublier de désactiver les APIs non utilisées

---

## 🐛 Dépannage

### Erreur : "This API key is not authorized to use this service or API"

**Cause :** Restrictions trop strictes ou API non activée

**Solution :**
1. Vérifiez que l'API est activée dans la bibliothèque
2. Vérifiez les restrictions de la clé (HTTP referrers + APIs)
3. Attendez 5 minutes (propagation des changements)

### Erreur : "RefererNotAllowedMapError"

**Cause :** L'URL du site n'est pas dans les référents autorisés

**Solution :**
Ajoutez dans les restrictions HTTP :
```
http://localhost:8080/*
http://127.0.0.1:8080/*
```

### Erreur : "The provided API key is expired"

**Cause :** Clé supprimée ou régénérée

**Solution :**
Créez une nouvelle clé API et mettez à jour `index.html`

### Carte grise avec message "For development purposes only"

**Cause :** Compte de facturation non activé

**Solution :**
Activez la facturation (Étape 3 ci-dessus) même si c'est gratuit

### Erreur : "You have exceeded your daily request quota"

**Cause :** Limite gratuite dépassée (rare pour ce projet)

**Solution :**
1. Attendez le lendemain (quotas réinitialisés à minuit PST)
2. Vérifiez qu'il n'y a pas de boucle infinie dans le code
3. Activez la facturation au-delà si nécessaire

---

## 💡 Astuces pour Économiser les Requêtes

### 1. Cacher les résultats de géocodage

```javascript
// Stocker dans localStorage
const cachedGeocode = localStorage.getItem(address);
if (cachedGeocode) {
    return JSON.parse(cachedGeocode);
}
```

### 2. Utiliser des coordonnées directement

Si vous connaissez les coordonnées GPS, utilisez-les directement au lieu de géocoder.

### 3. Limiter les chargements de carte

N'initialisez la carte qu'une seule fois, pas à chaque interaction.

### 4. Regrouper les requêtes

Utilisez Distance Matrix API pour plusieurs distances en une requête plutôt que plusieurs requêtes individuelles.

---

## 📚 Ressources Utiles

- [Documentation Google Maps Platform](https://developers.google.com/maps/documentation)
- [Calculateur de prix](https://mapsplatformtransition.withgoogle.com/calculator)
- [Exemples de code](https://developers.google.com/maps/documentation/javascript/examples)
- [Support Google Maps](https://developers.google.com/maps/support)

---

## 🎓 Pour la Soutenance

**Points à mentionner :**
- ✅ Utilisation de Google Maps Platform (standard industrie)
- ✅ Coût : 0€ grâce au crédit gratuit de 200$/mois
- ✅ APIs utilisées : Maps JavaScript, Geocoding, Directions
- ✅ Sécurité : Clé API restreinte par domaine
- ✅ Performance : Mise en cache des résultats

**Démo :**
1. Montrer la carte interactive
2. Montrer le géocodage des adresses
3. Montrer l'affichage du trajet optimisé
4. Montrer le dashboard Google Cloud (utilisation)

---

**Votre projet est maintenant configuré avec Google Maps ! 🗺️✨**

**Coût total : 0€** (dans les limites du crédit gratuit)
