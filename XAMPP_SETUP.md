# 🔧 Configuration XAMPP pour le Projet Covoiturage

## 📋 Prérequis

- XAMPP installé sur votre machine
- Java 11+ installé
- Maven installé
- Apache Tomcat 9+ installé

---

## 🚀 Étapes d'Installation avec XAMPP

### 1️⃣ Démarrer XAMPP

1. Ouvrez le **XAMPP Control Panel**
2. Démarrez **Apache** (pour phpMyAdmin - optionnel)
3. Démarrez **MySQL** (obligatoire)

```
[✓] Apache - Port 80, 443
[✓] MySQL  - Port 3306
```

### 2️⃣ Créer la Base de Données

#### Option A : Via phpMyAdmin (Interface graphique)

1. Ouvrez votre navigateur : `http://localhost/phpmyadmin`
2. Cliquez sur **"Nouvelle base de données"**
3. Nom : `covoiturage_db`
4. Interclassement : `utf8mb4_unicode_ci`
5. Cliquez sur **"Créer"**

6. Sélectionnez la base `covoiturage_db`
7. Cliquez sur l'onglet **"SQL"**
8. Copiez tout le contenu du fichier `database/schema.sql`
9. Collez dans la zone de texte
10. Cliquez sur **"Exécuter"**

#### Option B : Via ligne de commande

```powershell
# Naviguez vers le répertoire du projet
cd "D:\2IDSD\JAVA avan\Optimisation-des-trajets\Optimisation"

# Exécutez le script SQL
# Chemin XAMPP par défaut : C:\xampp\mysql\bin\mysql.exe
C:\xampp\mysql\bin\mysql.exe -u root < database\schema.sql
```

**Note :** Par défaut, XAMPP n'a **pas de mot de passe** pour l'utilisateur `root`.

### 3️⃣ Vérifier la Configuration

Le fichier `src/main/resources/db.properties` est déjà configuré pour XAMPP :

```properties
db.url=jdbc:mysql://localhost:3306/covoiturage_db?useSSL=false&serverTimezone=UTC
db.username=root
db.password=
```

✅ **Aucune modification nécessaire** si vous utilisez la configuration XAMPP par défaut !

### 4️⃣ Vérifier la Base de Données

#### Via phpMyAdmin :
1. Allez sur `http://localhost/phpmyadmin`
2. Sélectionnez `covoiturage_db`
3. Vérifiez que les tables suivantes existent :
   - ✅ `utilisateurs`
   - ✅ `vehicules`
   - ✅ `trajets`
   - ✅ `trajet_utilisateurs`
   - ✅ `conflits`

#### Via MySQL Command Line :
```powershell
C:\xampp\mysql\bin\mysql.exe -u root

# Dans le prompt MySQL :
USE covoiturage_db;
SHOW TABLES;
SELECT COUNT(*) FROM utilisateurs;  # Devrait retourner 6
SELECT COUNT(*) FROM vehicules;     # Devrait retourner 3
SELECT COUNT(*) FROM trajets;       # Devrait retourner 2
EXIT;
```

---

## 🔒 Sécurité (Optionnel pour Production)

### Définir un Mot de Passe pour MySQL

```powershell
C:\xampp\mysql\bin\mysql.exe -u root

# Dans MySQL :
ALTER USER 'root'@'localhost' IDENTIFIED BY 'votre_mot_de_passe';
FLUSH PRIVILEGES;
EXIT;
```

**Important :** Si vous définissez un mot de passe, mettez à jour `db.properties` :
```properties
db.password=votre_mot_de_passe
```

---

## 🏗️ Compiler et Déployer l'Application

### 1. Compiler avec Maven

```powershell
cd "D:\2IDSD\JAVA avan\Optimisation-des-trajets\Optimisation"
mvn clean install
```

Résultat attendu :
```
[INFO] BUILD SUCCESS
[INFO] covoiturage.war créé dans target/
```

### 2. Déployer sur Tomcat

```powershell
# Copiez le fichier WAR vers Tomcat
copy target\covoiturage.war "C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\"
```

### 3. Démarrer Tomcat

```powershell
# Démarrez Tomcat
cd "C:\Program Files\Apache Software Foundation\Tomcat 9.0\bin"
.\startup.bat
```

### 4. Accéder à l'Application

Ouvrez votre navigateur : **http://localhost:8080/covoiturage/**

---

## 🛠️ Dépannage XAMPP

### ❌ Problème : "Port 3306 already in use"

**Cause :** Un autre service MySQL est déjà en cours d'exécution

**Solution :**
```powershell
# Arrêter tous les services MySQL
net stop MySQL
net stop MySQL80  # Si vous avez MySQL 8 installé

# Redémarrer XAMPP MySQL
```

### ❌ Problème : "Access denied for user 'root'@'localhost'"

**Solution 1 :** Vérifiez le mot de passe dans `db.properties`

**Solution 2 :** Réinitialisez le mot de passe MySQL
```powershell
C:\xampp\mysql\bin\mysql.exe -u root

# Dans MySQL :
ALTER USER 'root'@'localhost' IDENTIFIED BY '';
FLUSH PRIVILEGES;
```

### ❌ Problème : "Unknown database 'covoiturage_db'"

**Cause :** La base de données n'a pas été créée

**Solution :** Exécutez à nouveau le script SQL (voir Étape 2)

### ❌ Problème : XAMPP MySQL ne démarre pas

**Vérifications :**
1. Port 3306 libre : `netstat -ano | findstr :3306`
2. Logs XAMPP : `C:\xampp\mysql\data\mysql_error.log`
3. Redémarrer le PC si nécessaire

---

## 📊 Données de Test Préchargées

Après l'exécution du script SQL, vous disposez de :

### Utilisateurs (6)
| ID | Nom | Groupe |
|----|-----|--------|
| 1 | Alice Martin | Entreprise A |
| 2 | Bob Dupont | Entreprise A |
| 3 | Charlie Rousseau | Entreprise B |
| 4 | Diana Laurent | Entreprise A |
| 5 | Etienne Bernard | Entreprise C |
| 6 | Fanny Petit | Entreprise B |

### Véhicules (3)
| ID | Conducteur | Immatriculation | Capacité |
|----|------------|-----------------|----------|
| 1 | Alice Martin | AB-123-CD | 4 |
| 2 | Charlie Rousseau | EF-456-GH | 3 |
| 3 | Etienne Bernard | IJ-789-KL | 5 |

### Trajets (2)
| ID | Véhicule | Distance | Temps |
|----|----------|----------|-------|
| 1 | AB-123-CD | 12.5 km | 35 min |
| 2 | EF-456-GH | 15.3 km | 42 min |

---

## ✅ Checklist Complète

- [ ] XAMPP installé et démarré
- [ ] MySQL en cours d'exécution (port 3306)
- [ ] Base de données `covoiturage_db` créée
- [ ] Script SQL `schema.sql` exécuté
- [ ] Vérification des 5 tables créées
- [ ] Vérification des données de test (6 utilisateurs, 3 véhicules)
- [ ] `db.properties` configuré (username: root, password: vide)
- [ ] Google Maps API Key ajoutée dans `index.html`
- [ ] Projet compilé avec Maven (`mvn clean install`)
- [ ] WAR déployé sur Tomcat
- [ ] Application accessible sur http://localhost:8080/covoiturage/

---

## 🎓 Pour la Soutenance

**Points à mentionner :**
- ✅ Base de données normalisée (3NF)
- ✅ Utilisation de XAMPP (stack LAMP/WAMP)
- ✅ Contraintes d'intégrité référentielle
- ✅ Triggers pour validation automatique
- ✅ Vues pour requêtes complexes
- ✅ Procédures stockées pour logique métier
- ✅ Index pour optimisation des performances
- ✅ Données de test représentatives

**Commandes à connaître :**
```sql
-- Voir les trajets optimisés
SELECT * FROM v_trajets_complets;

-- Statistiques globales
SELECT * FROM v_statistiques;

-- Utilisateurs compatibles pour un véhicule
CALL sp_utilisateurs_compatibles(1);

-- Statistiques d'un trajet
CALL sp_statistiques_trajet(1);
```

---

**Projet prêt pour démonstration avec XAMPP !** 🚀
