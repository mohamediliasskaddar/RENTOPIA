// src/app/core/models/api-response.model.ts

/**
 * ============================
 * Interface ApiMessageResponse
 * Réponse générique de l'API avec un message simple
 * Utilisée pour les confirmations (ex: "Email envoyé", "Supprimé avec succès")
 * ============================
 */
export interface ApiMessageResponse {
  message: string;
}

/**
 * ============================
 * Interface ApiError
 * Format standard des erreurs retournées par l'API
 * ============================
 */
export interface ApiError {
  message: string;    // Message d'erreur
  status?: number;    // Code HTTP (400, 401, 500...)
  timestamp?: string; // Date/heure de l'erreur
  path?: string;      // Endpoint qui a généré l'erreur
}

/**
 * ============================
 * Interface PaginatedResponse<T>
 * Réponse paginée générique pour les listes
 * Correspond au format Spring Data Page
 * ============================
 */
export interface PaginatedResponse<T> {
  content: T[];        // Liste des éléments de la page
  totalElements: number; // Nombre total d'éléments
  totalPages: number;   // Nombre total de pages
  size: number;         // Taille de la page (nombre d'éléments par page)
  number: number;       // Numéro de la page actuelle (commence à 0)
  first: boolean;       // Est-ce la première page ?
  last: boolean;        // Est-ce la dernière page ?
}

/**
 * ============================
 * Interface DeleteResponse
 * Réponse pour les opérations de suppression
 * ============================
 */
export interface DeleteResponse {
  message: string;
  deleted?: boolean;
}

/**
 * ============================
 * Interface HealthCheckResponse
 * Réponse du endpoint /auth/health
 * ============================
 */
export interface HealthCheckResponse {
  status: string;  // "UP" ou "DOWN"
  service: string; // Nom du service (ex: "user-service")
}
