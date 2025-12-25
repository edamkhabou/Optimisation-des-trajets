# 🚀 Installation et Configuration d'Apache Tomcat

## 📋 Prérequis

- ✅ Java JDK 11+ installé
- ✅ Variable d'environnement `JAVA_HOME` configurée
- ✅ Projet compilé (`mvn clean package` réussi)

---

## 📥 Étape 1 : Télécharger Apache Tomcat

### Option A : Téléchargement officiel (RECOMMANDÉ)

1. Allez sur [https://tomcat.apache.org/download-90.cgi](https://tomcat.apache.org/download-90.cgi)

2. Sous **"Binary Distributions"** → **"Core"**, téléchargez :
   - **Windows** : `64-bit Windows zip` (apache-tomcat-9.x.xx-windows-x64.zip)
   - Exemple : `apache-tomcat-9.0.85.zip` (~12 MB)

3. Ou téléchargement direct :
   ```
   https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.85/bin/apache-tomcat-9.0.85-windows-x64.zip
   ```

### Option B : Via PowerShell (automatique)

```powershell
# Télécharger Tomcat 9
$url = "https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.85/bin/apache-tomcat-9.0.85-windows-x64.zip"
$output = "$env:USERPROFILE\Downloads\tomcat9.zip"
Invoke-WebRequest -Uri $url -OutFile $output

# Extraire dans C:\
Expand-Archive -Path $output -DestinationPath "C:\" -Force
Rename-Item "C:\apache-tomcat-9.0.85" "C:\Tomcat9"
```

---

## 📂 Étape 2 : Installer Tomcat

### Installation manuelle

1. **Extraire le ZIP** téléchargé
2. **Déplacer** le dossier extrait vers : `C:\Tomcat9`
3. Votre structure devrait ressembler à :
   ```
   C:\Tomcat9\
   ├── bin\         (scripts de démarrage)
   ├── conf\        (configuration)
   ├── lib\         (bibliothèques)
   ├── logs\        (fichiers de log)
   ├── temp\        (fichiers temporaires)
   ├── webapps\     (vos applications web)
   └── work\        (fichiers de travail)
   ```

---

## ⚙️ Étape 3 : Configurer les Variables d'Environnement

### Vérifier JAVA_HOME

```powershell
# Vérifier si JAVA_HOME existe
$env:JAVA_HOME

# Si vide, définir JAVA_HOME
# Exemple : C:\Program Files\Java\jdk-11.0.20
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-11", "Machine")
```

### Ajouter CATALINA_HOME (optionnel mais recommandé)

```powershell
# Définir CATALINA_HOME
[System.Environment]::SetEnvironmentVariable("CATALINA_HOME", "C:\Tomcat9", "Machine")

# Ajouter Tomcat au PATH
$path = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
[System.Environment]::SetEnvironmentVariable("Path", "$path;C:\Tomcat9\bin", "Machine")

# Recharger les variables (fermer/rouvrir PowerShell)
```

### Méthode graphique (si vous préférez)

1. **Clic droit sur "Ce PC"** → **Propriétés**
2. **Paramètres système avancés** → **Variables d'environnement**
3. **Nouvelles variables système** :
   - `JAVA_HOME` = `C:\Program Files\Java\jdk-11`
   - `CATALINA_HOME` = `C:\Tomcat9`
4. **Modifier Path** : Ajouter `C:\Tomcat9\bin`

---

## 🎯 Étape 4 : Configurer les Utilisateurs Tomcat (Optionnel)

Pour accéder au **Manager** et à l'**Host Manager** de Tomcat :

1. Ouvrez `C:\Tomcat9\conf\tomcat-users.xml`

2. Avant la balise `</tomcat-users>`, ajoutez :

```xml
<role rolename="manager-gui"/>
<role rolename="admin-gui"/>
<user username="admin" password="admin123" roles="manager-gui,admin-gui"/>
```

3. Sauvegardez le fichier

**⚠️ Attention :** Utilisez un mot de passe fort en production !

---

## 🔧 Étape 5 : Changer le Port de Tomcat (IMPORTANT)

**⚠️ XAMPP utilise déjà le port 8080, nous devons changer le port de Tomcat !**

1. Ouvrez le fichier `C:\Tomcat9\conf\server.xml` avec un éditeur de texte

2. Cherchez la ligne (environ ligne 69) :
   ```xml
   <Connector port="8080" protocol="HTTP/1.1"
   ```

3. Changez `8080` en `9090` :
   ```xml
   <Connector port="9090" protocol="HTTP/1.1"
   ```

4. Sauvegardez le fichier

**✅ Tomcat utilisera maintenant le port 9090 au lieu de 8080**

---

## ▶️ Étape 6 : Démarrer Tomcat

### Méthode 1 : Via les scripts batch

```powershell
# Démarrer Tomcat
cd C:\Tomcat9\bin
.\startup.bat

# Arrêter Tomcat
.\shutdown.bat
```

### Méthode 2 : Via la console (pour voir les logs)

```powershell
cd C:\Tomcat9\bin
.\catalina.bat run
```

**Sortie attendue :**
```
INFO: Starting ProtocolHandler ["http-nio-9090"]
INFO: Server startup in [2345] milliseconds
```

### Méthode 3 : Installer comme Service Windows (RECOMMANDÉ)

```powershell
# Installer le service
cd C:\Tomcat9\bin
.\service.bat install

# Démarrer le service
net start Tomcat9

# Arrêter le service
net stop Tomcat9

# Désinstaller le service
.\service.bat remove
```

---

## ✅ Étape 7 : Vérifier l'Installation

### Test 1 : Page d'accueil Tomcat

Ouvrez votre navigateur : **http://localhost:9090**

**Résultat attendu :** Page d'accueil Tomcat avec le logo du chat 🐱

### Test 2 : Manager Application

Allez sur : **http://localhost:9090/manager/html**
- **Username :** admin
- **Password :** admin123 (celui configuré à l'étape 4)

**Résultat attendu :** Interface de gestion des applications

---

## 📦 Étape 8 : Déployer Votre Application

### Méthode 1 : Copie manuelle (SIMPLE)

```powershell
# Copier le fichier WAR dans webapps
copy "D:\2IDSD\JAVA avan\Optimisation-des-trajets\Optimisation\target\covoiturage.war" "C:\Tomcat9\webapps\"

# Tomcat déploie automatiquement !
```

**Résultat :** Tomcat extrait automatiquement le WAR dans `webapps\covoiturage\`

### Méthode 2 : Via le Manager (INTERFACE)

1. Allez sur http://localhost:9090/manager/html
2. Section **"Fichier WAR à déployer"**
3. Cliquez sur **"Choisir un fichier"**
4. Sélectionnez `target\covoiturage.war`
5. Cliquez sur **"Déployer"**

### Méthode 3 : Déploiement automatique (SCRIPT)

Créez un script PowerShell `deploy.ps1` :

```powershell
# deploy.ps1
$projectPath = "D:\2IDSD\JAVA avan\Optimisation-des-trajets\Optimisation"
$tomcatPath = "C:\Tomcat9"

Write-Host "🔧 Compilation du projet..." -ForegroundColor Cyan
cd $projectPath
mvn clean package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation réussie!" -ForegroundColor Green
    
    Write-Host "📦 Déploiement sur Tomcat..." -ForegroundColor Cyan
    
    # Supprimer l'ancien déploiement
    if (Test-Path "$tomcatPath\webapps\covoiturage.war") {
        Remove-Item "$tomcatPath\webapps\covoiturage.war" -Force
    }
    if (Test-Path "$tomcatPath\webapps\covoiturage") {
        Remove-Item "$tomcatPath\webapps\covoiturage" -Recurse -Force
    }
    
    # Copier le nouveau WAR
    Copy-Item "$projectPath\target\covoiturage.war" "$tomcatPath\webapps\" -Force
    
    Write-Host "✅ Application déployée!" -ForegroundColor Green
    Write-Host "🌐 Accédez à : http://localhost:9090/covoiturage/" -ForegroundColor Yellow
} else {
    Write-Host "❌ Erreur de compilation!" -ForegroundColor Red
}
```

**Utilisation :**
```powershell
.\deploy.ps1
```

---

## 🌐 Étape 9 : Accéder à Votre Application

Ouvrez votre navigateur : **http://localhost:9090/covoiturage/**

**Résultat attendu :**
- ✅ Page d'accueil avec onglets (Utilisateurs, Véhicules, Optimisation, Statistiques)
- ✅ Carte OpenStreetMap visible
- ✅ Formulaires fonctionnels

---

## 🐛 Dépannage

### ❌ Erreur : "Port 9090 already in use"

**Cause :** Un autre service utilise le port 9090

**Solution :** Changer le port de Tomcat vers un autre port (ex: 8081, 8090, 9000)

1. Ouvrez `C:\Tomcat9\conf\server.xml`
2. Cherchez `<Connector port="9090"`
3. Changez en `<Connector port="8081"` (ou autre port libre)
4. Redémarrez Tomcat
5. Accédez à `http://localhost:8081/covoiturage/`

**Note :** Le port 8080 est utilisé par XAMPP Apache, c'est pourquoi nous utilisons 9090 pour Tomcat.

### ❌ Erreur : "JAVA_HOME is not defined"

```powershell
# Définir JAVA_HOME temporairement
$env:JAVA_HOME = "C:\Program Files\Java\jdk-11"

# Ou définir de façon permanente
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-11", "Machine")
```

### ❌ Erreur : "404 Not Found" sur /covoiturage

**Vérifications :**
1. Le WAR est-il dans `C:\Tomcat9\webapps\` ?
2. Le dossier `covoiturage\` a-t-il été créé ?
3. Consultez les logs : `C:\Tomcat9\logs\catalina.out`

```powershell
# Voir les dernières lignes du log
Get-Content "C:\Tomcat9\logs\catalina.2025-12-25.log" -Tail 50
```

### ❌ Erreur : "HTTP Status 500" dans l'application

**Causes fréquentes :**
1. Base de données non démarrée (XAMPP MySQL)
2. Mauvaise configuration `db.properties`
3. Erreur dans le code

**Vérifications :**
```powershell
# Vérifier que MySQL est démarré
mysql -u root -e "SHOW DATABASES;"

# Voir les logs Tomcat
Get-Content "C:\Tomcat9\logs\localhost.2025-12-25.log" -Tail 100
```

### ❌ Tomcat ne démarre pas

**Vérifications :**
1. Java est-il installé ? `java -version`
2. JAVA_HOME est-il défini ? `echo $env:JAVA_HOME`
3. Port 8080 est-il libre ? `netstat -ano | findstr :8080`
4. Consultez les logs : `C:\Tomcat9\logs\catalina.out`

---

## 📊 Logs Importants

| Fichier | Contenu |
|---------|---------|
| `catalina.out` | Logs généraux de Tomcat |
| `localhost.log` | Logs de l'application |
| `manager.log` | Logs du manager |
| `host-manager.log` | Logs du host manager |

**Voir les logs en temps réel :**
```powershell
Get-Content "C:\Tomcat9\logs\catalina.out" -Wait -Tail 50
```

---

## ⚡ Commandes Utiles

```powershell
# Démarrer Tomcat
C:\Tomcat9\bin\startup.bat

# Arrêter Tomcat
C:\Tomcat9\bin\shutdown.bat

# Démarrer en mode console (voir les logs)
C:\Tomcat9\bin\catalina.bat run

# Vérifier si Tomcat tourne
netstat -ano | findstr :9090

# Nettoyer les déploiements
Remove-Item "C:\Tomcat9\webapps\covoiturage*" -Recurse -Force
Remove-Item "C:\Tomcat9\work\Catalina\localhost\covoiturage" -Recurse -Force

# Redéployer rapidement
copy "target\covoiturage.war" "C:\Tomcat9\webapps\" -Force
```

---

## 🔧 Configuration Avancée

### Augmenter la mémoire (si nécessaire)

Créez `C:\Tomcat9\bin\setenv.bat` :

```batch
set CATALINA_OPTS=-Xms512M -Xmx1024M -XX:MaxPermSize=256M
```

### Activer le Hot Reload

Dans `C:\Tomcat9\conf\context.xml` :

```xml
<Context reloadable="true">
    <!-- ... -->
</Context>
```

### Configuration HTTPS (optionnel)

Voir la documentation officielle : [Tomcat SSL/TLS](https://tomcat.apache.org/tomcat-9.0-doc/ssl-howto.html)

---

## ✅ Checklist Complète

- [ ] Tomcat téléchargé et extrait dans `C:\Tomcat9`
- [ ] JAVA_HOME configuré
- [ ] CATALINA_HOME configuré (optionnel)
- [ ] Utilisateur admin configuré dans `tomcat-users.xml`
- [ ] Tomcat démarré avec succès
- [ ] Tomcat configuré pour utiliser le port 9090 (server.xml modifié)
- [ ] Page http://localhost:9090 accessible
- [ ] XAMPP MySQL démarré
- [ ] Base de données `covoiturage_db` créée
- [ ] Projet compilé (`mvn clean package`)
- [ ] WAR copié dans `C:\Tomcat9\webapps\`
- [ ] Application accessible sur http://localhost:9090/covoiturage/
- [ ] Carte OpenStreetMap s'affiche correctement

---

## 🎓 Pour la Soutenance

**Commandes à connaître :**

```powershell
# Tout en une commande
cd "D:\2IDSD\JAVA avan\Optimisation-des-trajets\Optimisation"
mvn clean package && copy target\covoiturage.war C:\Tomcat9\webapps\
```

**Démonstration :**
1. Démarrer XAMPP (MySQL + Apache sur port 8080)
2. Démarrer Tomcat (sur port 9090)
3. Accéder à http://localhost:9090/covoiturage/
4. Montrer les fonctionnalités

**Rappel des ports :**
- XAMPP Apache : http://localhost:8080 (phpMyAdmin)
- Tomcat : http://localhost:9090/covoiturage/ (votre application)

---

**Tomcat est maintenant configuré et prêt ! 🚀**

**Application accessible sur : http://localhost:9090/covoiturage/**

**Ports utilisés :**
- 🌐 XAMPP (Apache/phpMyAdmin) : http://localhost:8080
- 🚀 Tomcat (Application Java) : http://localhost:9090/covoiturage/
