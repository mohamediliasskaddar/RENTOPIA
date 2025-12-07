package com.rental.media.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Initializer pour charger les variables .env AVANT le démarrage de Spring
 * Cette classe est appelée très tôt dans le cycle de vie de l'application
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // Charger le fichier .env depuis la racine du projet
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")  // Racine du projet
                    .ignoreIfMissing()
                    .load();

            // Créer une Map avec toutes les variables
            Map<String, Object> envMap = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                envMap.put(entry.getKey(), entry.getValue());
                // Aussi les ajouter aux propriétés système (double sécurité)
                System.setProperty(entry.getKey(), entry.getValue());
            });

            // Ajouter au PropertySources de Spring (priorité haute)
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            environment.getPropertySources().addFirst(
                    new MapPropertySource("dotenvProperties", envMap)
            );

            System.out.println("✅ Variables .env chargées avec succès");
            System.out.println("   - DB_HOST: " + dotenv.get("DB_HOST", "non défini"));
            System.out.println("   - DB_NAME: " + dotenv.get("DB_NAME", "non défini"));
            System.out.println("   - APP_MODE: " + dotenv.get("APP_MODE", "non défini"));

        } catch (Exception e) {
            System.err.println("⚠️ ATTENTION: Impossible de charger le fichier .env");
            System.err.println("   Erreur: " + e.getMessage());
            System.err.println("   Le service va démarrer avec les variables d'environnement système");
        }
    }
}