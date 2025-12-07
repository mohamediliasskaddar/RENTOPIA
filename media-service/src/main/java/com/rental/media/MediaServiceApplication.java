package com.rental.media;

import com.rental.media.config.DotenvConfig;
import com.rental.media.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Classe principale du microservice Media
 *
 * Fonctionnalités :
 * - Upload d'images vers AWS S3 (production) ou filesystem (local)
 * - Compression automatique des images
 * - Génération de miniatures
 * - Intégration avec CloudFront CDN
 * - Sauvegarde des métadonnées en MySQL
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MediaServiceApplication {

	public static void main(String[] args) {
		// 1. Charger .env EN PREMIER (avant Spring Boot)
		EnvLoader.init();

		// 2. Créer l'application Spring Boot
		SpringApplication app = new SpringApplication(MediaServiceApplication.class);

		// 3. Ajouter l'initializer pour .env
		app.addInitializers(new DotenvConfig());

		// 4. Démarrer l'application
		app.run(args);

		System.out.println("""
            
            ╔════════════════════════════════════════╗
            ║   📸 MEDIA SERVICE DÉMARRÉ             ║
            ║   Port : 8087                          ║
            ║   Eureka : Activé                      ║
            ║   Stockage : Local/S3                  ║
            ╚════════════════════════════════════════╝
            
        """);
	}
}