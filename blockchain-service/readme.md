Démarrer le Blockchain Service:
cd blockchain-service

# 1. Générer le wrapper (si pas encore fait)
mvn clean compile

# 2. Définir la clé privée admin
export ADMIN_PRIVATE_KEY=ta_clef_privee_admin

# 3. Démarrer le service
mvn spring-boot:run


new