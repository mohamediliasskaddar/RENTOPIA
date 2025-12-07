# 🚀  Media Service (LOCAL)

## 📋 ARCHITECTURE

### Mode Développement Local
```
┌─────────────────────────────────────┐
│  Media Service (localhost:8087)     │
│  ├─ Stockage : Filesystem local     │
│  ├─ Base : MySQL local              │
│  └─ Pas de credentials AWS          │
└─────────────────────────────────────┘
```

### Mode Production (EKS)
```
┌─────────────────────────────────────┐
│  Media Service (Pod Kubernetes)     │
│  ├─ Stockage : AWS S3               │
│  ├─ Base : RDS MySQL                │
│  └─ Auth : IAM Roles (automatique)  │
└─────────────────────────────────────┘
```

---

## 🛠️ ÉTAPE 1 : PRÉREQUIS

### Développement Local

✅ **Java 17+**
```bash
java -version
```

✅ **Maven 3.8+**
```bash
mvn -version
```

✅ **MySQL Local**
```bash
# Démarrer MySQL
mysql -u root -p

# Créer la base
CREATE DATABASE rental_db;
USE rental_db;

# Importer le schéma (votre fichier rental_db.sql)
SOURCE /chemin/vers/rental_db.sql;
```

✅ **Eureka Server** (port 8761)

---

## ⚙️ ÉTAPE 2 : CONFIGURATION

### 2.1 - Créer le fichier .env

À la racine de `media-service/` :

```bash
# DATABASE LOCAL
DB_HOST=localhost
DB_PORT=3306
DB_NAME=rental_db
DB_USER=root
DB_PASSWORD=root

# AWS (Pas de credentials nécessaires en local)
AWS_REGION=eu-west-3
S3_PROPERTIES_BUCKET=real-estate-dapp-properties-images-dev
S3_USERS_BUCKET=real-estate-dapp-users-photos-dev
CLOUDFRONT_URL=https://dynu4ij1ldniw.cloudfront.net

# EUREKA
EUREKA_SERVER_URL=http://localhost:8761/eureka

# PORT
SERVER_PORT=8087

# MODE (local pour développement)
APP_MODE=local
```

### 2.2 - Vérifier .gitignore

```
.env
*.env
local-uploads/
```

---

## 🚀 ÉTAPE 3 : COMPILATION

```bash
cd media-service
mvn clean install -DskipTests
```

**Résultat attendu :**
```
[INFO] BUILD SUCCESS
```

---

## ▶️ ÉTAPE 4 : DÉMARRAGE

### Option 1 : Maven
```bash
mvn spring-boot:run
```

### Option 2 : JAR
```bash
java -jar target/media-service-1.0.0.jar
```

### Option 3 : IDE
1. Ouvrir `MediaServiceApplication.java`
2. Clic droit → Run

---

## ✅ ÉTAPE 5 : VÉRIFICATION

### 5.1 - Logs de démarrage

Vous devriez voir :

```
✅ Variables .env chargées avec succès
💻 Mode LOCAL : Utilisation Mock S3 (filesystem)

╔════════════════════════════════════════╗
║   📸 MEDIA SERVICE DÉMARRÉ             ║
║   Port : 8087                          ║
║   Mode : LOCAL                         ║
║   Stockage : Filesystem                ║
╚════════════════════════════════════════╝
```

### 5.2 - Health Check

```bash
curl http://localhost:8087/api/media/health
```

**Réponse :**
```json
{
    "status": "UP",
    "service": "media-service",
    "mode": "local",
    "storage": "Filesystem local"
}
```

### 5.3 - Vérifier Eureka

http://localhost:8761

→ `MEDIA-SERVICE` doit apparaître

---

## 🧪 ÉTAPE 6 : PREMIER TEST

### Test 1 : Upload Image (Postman)

**Requête :**
```
POST http://localhost:8087/api/media/properties/upload
Content-Type: multipart/form-data
```

**Body (form-data) :**
| Key | Type | Value |
|-----|------|-------|
| file | File | [image.jpg] |
| entityId | Text | 1 |
| isCover | Text | true |
| displayOrder | Text | 1 |

**Réponse attendue :**
```json
{
    "photoId": 1,
    "s3Key": "properties/1/abc-123.jpg",
    "cdnUrl": "http://localhost:8087/api/media/files/properties/1/abc-123.jpg",
    "thumbnailUrl": "http://localhost:8087/api/media/files/properties/1/thumbnails/abc-123.jpg",
    "fileSize": 245678,
    "contentType": "image/jpeg",
    "width": 1920,
    "height": 1080,
    "isCover": true,
    "displayOrder": 1,
    "message": "Image uploadée avec succès"
}
```

### Test 2 : Vérifier le fichier local

```bash
ls -la local-uploads/properties/1/
# Vous devriez voir votre image
```

### Test 3 : Afficher l'image dans le navigateur

Copier l'URL `cdnUrl` et l'ouvrir :
```
http://localhost:8087/api/media/files/properties/1/abc-123.jpg
```

L'image doit s'afficher ! 🎉

---

## 🗄️ STRUCTURE DES FICHIERS

### Mode Local
```
media-service/
├── local-uploads/           ← Fichiers stockés ici
│   ├── properties/
│   │   ├── 1/
│   │   │   ├── abc-123.jpg
│   │   │   └── thumbnails/
│   │   │       └── abc-123.jpg
│   │   └── 2/
│   └── users/
│       └── 1/
│           └── profile.jpg
```

### Mode Production (EKS)
```
AWS S3 Bucket : real-estate-dapp-properties-images-dev
├── properties/
│   ├── 1/
│   │   ├── abc-123.jpg
│   │   └── thumbnails/
│   │       └── abc-123.jpg
│   └── 2/
└── users/
```

---

## 🔄 PASSAGE EN PRODUCTION

Quand vous déploierez sur EKS, **rien à changer dans le code** :

### 1. Modifier .env (ou variables d'environnement Kubernetes)
```bash
APP_MODE=production
DB_HOST=real-estate-dapp-db-dev.cnwseskwiq1u.eu-west-3.rds.amazonaws.com
```

### 2. Le service utilisera automatiquement :
- ✅ IAM Roles du pod Kubernetes
- ✅ RDS MySQL
- ✅ S3 réel
- ✅ CloudFront CDN

**Aucun credential AWS dans le code !** 🔒

---

## 🧪 TESTS POSTMAN COMPLETS

### Collection JSON

```json
{
  "info": {
    "name": "Media Service - Local",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "url": "http://localhost:8087/api/media/health"
      }
    },
    {
      "name": "Upload Property Image",
      "request": {
        "method": "POST",
        "url": "http://localhost:8087/api/media/properties/upload",
        "body": {
          "mode": "formdata",
          "formdata": [
            {"key": "file", "type": "file"},
            {"key": "entityId", "value": "1", "type": "text"},
            {"key": "isCover", "value": "true", "type": "text"},
            {"key": "displayOrder", "value": "1", "type": "text"}
          ]
        }
      }
    },
    {
      "name": "Upload User Photo",
      "request": {
        "method": "POST",
        "url": "http://localhost:8087/api/media/users/upload",
        "body": {
          "mode": "formdata",
          "formdata": [
            {"key": "file", "type": "file"},
            {"key": "userId", "value": "1", "type": "text"}
          ]
        }
      }
    },
    {
      "name": "Delete Property Photo",
      "request": {
        "method": "DELETE",
        "url": "http://localhost:8087/api/media/properties/1"
      }
    },
    {
      "name": "View Image (Local)",
      "request": {
        "method": "GET",
        "url": "http://localhost:8087/api/media/files/properties/1/abc-123.jpg"
      }
    }
  ]
}
```

---

## 🐛 DÉPANNAGE

### Problème 1 : "Cannot create directory local-uploads"

**Solution :**
```bash
mkdir -p local-uploads/properties
mkdir -p local-uploads/users
chmod 755 local-uploads
```

### Problème 2 : "Table property_photos doesn't exist"

**Solution :**
```sql
USE rental_db;
SOURCE rental_db.sql;
```

### Problème 3 : Port 8087 déjà utilisé

**Solution :**
```bash
# Linux/Mac
lsof -i :8087
kill -9 <PID>

# OU changer le port dans .env
SERVER_PORT=8088
```

### Problème 4 : Images ne s'affichent pas

**Vérifier :**
```bash
# Le dossier existe ?
ls -la local-uploads/properties/1/

# Les permissions ?
chmod -R 755 local-uploads/

# Les logs ?
tail -f logs/media-service.log
```

---

## 📊 ORDRE DE DÉMARRAGE

```
1. MySQL (3306)           ← Base locale
2. Eureka Server (8761)   ← Service discovery
3. Media Service (8087)   ← Votre service ✨
4. API Gateway (8089)     ← Optionnel pour tests
```

---

## ✅ CHECKLIST FINALE

- [ ] Java 17+ installé
- [ ] Maven installé
- [ ] MySQL local démarré avec base `rental_db`
- [ ] Eureka Server démarré
- [ ] Fichier `.env` créé avec `APP_MODE=local`
- [ ] `.env` dans `.gitignore`
- [ ] Compilation réussie (`mvn clean install`)
- [ ] Service démarré
- [ ] Health check répond
- [ ] Upload test réussi
- [ ] Image visible dans `local-uploads/`
- [ ] Image affichée dans le navigateur

---

## 🎯 AVANTAGES DE CETTE APPROCHE

✅ **Développement Local**
- Pas besoin de credentials AWS
- Pas de coûts AWS pendant le dev
- Fichiers visibles localement
- Tests rapides

✅ **Production (EKS)**
- IAM Roles automatiques (sécurisé)
- Pas de credentials hardcodés
- Scalable avec S3
- CDN CloudFront pour performances

✅ **Code Propre**
- Même code pour dev et prod
- Changement de mode via variable
- Respecte les best practices AWS

---

## 🚀 C'EST PRÊT !

service fonctionne en mode local avec stockage filesystem.

Quand le Cloud Engineer déploiera sur EKS, il changera juste `APP_MODE=production` et le service utilisera automatiquement S3 avec IAM Roles.

**Aucune modification de code nécessaire !** 🎉