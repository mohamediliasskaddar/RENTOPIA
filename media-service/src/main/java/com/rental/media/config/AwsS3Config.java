package com.rental.media.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Configuration AWS S3 Client
 *
 * MODE LOCAL : Mock S3 (filesystem local)
 * MODE PRODUCTION : IAM Roles automatiques (pas de credentials)
 */
@Configuration
public class AwsS3Config {

    @Value("${aws.region}")
    private String region;

    @Value("${app.mode:local}")
    private String appMode;

    /**
     * Bean S3Client pour PRODUCTION (EKS)
     * Utilise automatiquement les IAM Roles du pod Kubernetes
     */
    @Bean
    @Profile("!local")
    public S3Client s3ClientProduction() {
        System.out.println("🚀 Mode PRODUCTION : Utilisation IAM Roles");

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Bean S3Client pour DÉVELOPPEMENT LOCAL
     * Mock S3 - Les fichiers sont stockés localement
     */
    @Bean
    @Profile("local")
    public S3Client s3ClientLocal() {
        System.out.println("💻 Mode LOCAL : Utilisation Mock S3 (filesystem)");

        // Mock S3 local (endpoint fictif)
        return S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create("http://localhost:9090")) // Mock S3 endpoint
                .forcePathStyle(true) // Nécessaire pour mock S3
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}