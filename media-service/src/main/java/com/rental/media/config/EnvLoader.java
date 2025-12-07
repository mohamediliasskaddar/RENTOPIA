package com.rental.media.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Classe statique pour charger .env au tout début
 * S'exécute AVANT tout le reste
 */
public class EnvLoader {

    static {
        try {
            System.out.println("🔄 Chargement du fichier .env...");

            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();

            // Injecter toutes les variables dans System Properties
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });

            System.out.println("✅ Fichier .env chargé avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement .env : " + e.getMessage());
        }
    }

    // Méthode pour forcer l'initialisation
    public static void init() {
        // Rien à faire, le bloc static s'exécute automatiquement
    }
}