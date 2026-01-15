// src/app/core/services/api.service.ts

import { Injectable } from '@angular/core';
// Axios : permet d'envoyer des requêtes HTTP (GET, POST...)
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

// Observable : permet à Angular de recevoir les réponses asynchrones.
// Observable = permet de recevoir une réponse plus tard (asynchrone), L’application continue de fonctionner en attendant.Quand le serveur répond, l’Observable nous envoie les données.
// Used pour les appels API qui prennent du temps.
import { Observable, from, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

// Import les variables d'environnement
import { environment } from '../../../environments/environment';

//@Injectable : cette classe peut être “injectée” ailleurs
@Injectable({
  providedIn: 'root' // Angular va créer un seul service pour toute l’application
})

export class ApiService {
  private axiosInstance: AxiosInstance; // Instance Axiose

  constructor() {
    // Création d'une instance Axios avec configuration par défaut
    this.axiosInstance = axios.create({
      baseURL: environment.apiUrl, // URL de l'API backend (api gateway)
      timeout: 30000, // Temps max : 30 secondes => Si le serveur ne répond pas en 30 secondes, Axios renvoie une erreur.
      headers: {
        'Content-Type': 'application/json', // Format d'envoi par défaut : json
      }
    });

    // =============== INTERCEPTEUR REQUEST ===============
    // Avant chaque requête → ajouter automatiquement le token JWT
    this.axiosInstance.interceptors.request.use( // intercepter toutes les requêtes avant qu’elles partent vers le serveur.
      (config) => {
        const token = localStorage.getItem(environment.tokenKey); // récupérer le token jwt
        if (token && config.headers) {
          config.headers.Authorization = `Bearer ${token}`; // Ajouter "Authorization: Bearer ..." dans header pour permettre au backend de vérifier que l’utilisateur est connecté et autorisé
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // =============== INTERCEPTEUR RESPONSE  : Intercepte toutes les réponses du serveur avant qu’elles arrivent===============
    // Permet de gérer les erreurs globalement
    this.axiosInstance.interceptors.response.use(
      (response) => response, //Si la réponse du serveur est correcte (200 OK), on ne fait rien
      (error) => {
        if (error.response) {
          // si Le backend a renvoyé une erreur
          console.error('API Error:', error.response.status, error.response.data);

          // Si token invalidé (401) =>  déconnecter l'utilisateur
          if (error.response.status === 401) {
            localStorage.removeItem(environment.tokenKey);
            localStorage.removeItem(environment.userKey);
            window.location.href = '/login'; // redirection automatique
          }
        } else if (error.request) {
          // Aucune réponse du serveur (problème réseau ou backend OFF)
          console.error('Network Error:', error.request);
        } else {
          console.error('Error:', error.message);
        }
        return Promise.reject(error);
      }
    );
  }

  // ======================= MÉTHODES HTTP =======================

  /** GET request */
  get<T>(endpoint: string, params?: any): Observable<T> {
    const config: AxiosRequestConfig = { params }; // config = paramètres envoyés dans l’URL (ex: ?page=1)
    return from(this.axiosInstance.get<T>(endpoint, config)).pipe(
      map((response) => response.data), // Récupère juste les données
      catchError(this.handleError)
    );
  }

  /** POST request */
  // La méthode retourne un Observable contenant un objet du type T
  post<T>(endpoint: string, body?: any, config?: AxiosRequestConfig): Observable<T> { // config?: AxiosRequestConfig => configuration supplémentaire pour Axios
    return from(this.axiosInstance.post<T>(endpoint, body, config)).pipe(
      map((response) => response.data), // map → ne garde que response.data (les données utiles)
      catchError(this.handleError)
    );
  }

  /** PUT request */
  put<T>(endpoint: string, body?: any , config?: AxiosRequestConfig): Observable<T> {
    return from(this.axiosInstance.put<T>(endpoint, body)).pipe(
      map((response) => response.data),
      catchError(this.handleError)
    );
  }

  /** PATCH request */
  patch<T>(endpoint: string, body?: any , config?: AxiosRequestConfig): Observable<T> {
    return from(this.axiosInstance.patch<T>(endpoint, body)).pipe(
      map((response) => response.data),
      catchError(this.handleError)
    );
  }

  /** DELETE request */
  delete<T>(endpoint: string): Observable<T> {
    return from(this.axiosInstance.delete<T>(endpoint)).pipe(
      map((response) => response.data),
      catchError(this.handleError)
    );
  }

  /**
   * Upload de fichier (multipart/form-data)
   */
  upload<T>(url: string, formData: FormData): Observable<T> {
    return from(
      this.axiosInstance.post<T>(url, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }).then(response => response.data)
    );
  }

  // ======================= GESTION DES ERREURS =======================
//  handleError doit retourner un Observable, même en cas d'erreur.
//les méthodes HTTP retournent toujours des Observables.=> Donc si on retournait juste un message ou un objet, le composant CRASHERAIENT car il s'attend à recevoir un Observable<T>.
// throwError crée un Observable qui contient une erreur,=> ce qui permet à .subscribe() de la gérer proprement dans (error => ...)

  private handleError(error: any): Observable<never> { // la fonction reçoit un objet erreur venant d’Axios.
    let errorMessage = 'Une erreur est survenue';

    if (error.response) { // est-ce que le backend a répondu cad est ce que le backend a renvoyé une réponse avec un code d’erreur
      const status = error.response.status; // le code d’erreur HTTP  400, 401, 500, etc
      const data = error.response.data; // le corps de response pour conter message d'erreur

      // Si backend envoie message → on l’affiche
      if (data?.message) {
        errorMessage = data.message;
      } else {
        // Sinon message par défaut selon le code HTTP
        switch (status) {
          case 400: errorMessage = 'Requête invalide'; break;
          case 401: errorMessage = 'Non autorisé. Veuillez vous reconnecter.'; break;
          case 403: errorMessage = 'Accès interdit'; break;
          case 404: errorMessage = 'Ressource non trouvée'; break;
          case 500: errorMessage = 'Erreur serveur'; break;
          default: errorMessage = `Erreur ${status}`;
        }
      }
    } else if (error.request) { // le serveur  n'a pas repondu
      errorMessage = 'Impossible de contacter le serveur. Vérifiez votre connexion.';
    }
    // throwError crée un Observable qui envoie une erreur
    return throwError(() => ({
      message: errorMessage,
      status: error.response?.status,
      originalError: error
    }));
  }

  /** Permet d'utiliser Axios directement si besoin */
//Ici, this.axiosInstance agit comme un singleton :Angular crée ApiService une seule fois (grâce à providedIn: 'root'),
// getAxiosInstance() retourne toujours la même instance Axios.
// Toutes les requêtes utilisent donc la même configuration
  getAxiosInstance(): AxiosInstance {
    return this.axiosInstance;
  }
}
