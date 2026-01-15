// src/app/core/services/user.service.ts

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { environment } from '../../../environments/environment';
import {
  User,
  UserResponseDTO,
  UpdateUserDTO,
  UserLanguageDTO,
  AddLanguageDTO
} from '../models/user.model';
import { ProficiencyLevel } from '../models/user.model';

/**
 * ============================
 * SERVICE USER
 * Gère les opérations CRUD sur les utilisateurs
 * Toutes les routes passent par l'API Gateway → User Service
 * ============================
 */
@Injectable({
  providedIn: 'root'
})
export class UserService {
  // Prefix pour toutes les routes user
  private readonly baseUrl = environment.services.user;

  constructor(private apiService: ApiService) {}

  /**
   * ============================
   * RÉCUPÉRER UN UTILISATEUR PAR ID
   * GET /users/users/{id}
   * Route complète : http://localhost:8080/api/users/users/123
   * ============================
   */
  getUserById(id: number): Observable<UserResponseDTO> {
    return this.apiService.get<UserResponseDTO>(`${this.baseUrl}/users/${id}`);
  }

  /**
   * ============================
   * RÉCUPÉRER TOUS LES UTILISATEURS
   * GET /users/users
   * Peut filtrer par langue avec ?language=fr
   * Route complète : http://localhost:8080/api/users/users
   * ============================
   */
  getAllUsers(languageFilter?: string): Observable<UserResponseDTO[]> {
    const params = languageFilter ? { language: languageFilter } : undefined;
    return this.apiService.get<UserResponseDTO[]>(`${this.baseUrl}/users`, params);
  }

  /**
   * ============================
   * METTRE À JOUR LE PROFIL UTILISATEUR
   * PUT /users/users/{id}
   * Route complète : http://localhost:8080/api/users/users/123
   * ============================
   */
  updateUser(id: number, updateData: UpdateUserDTO): Observable<UserResponseDTO> {
    return this.apiService.put<UserResponseDTO>(`${this.baseUrl}/users/${id}`, updateData);
  }

  /**
   * ============================
   * SUPPRIMER UN UTILISATEUR
   * DELETE /users/users/{id}
   * Route complète : http://localhost:8080/api/users/users/123
   * ============================
   */
  deleteUser(id: number): Observable<{ message: string }> {
    return this.apiService.delete<{ message: string }>(`${this.baseUrl}/users/${id}`);
  }

  /**
   * ============================
   * ACTIVER/DÉSACTIVER LE RÔLE HOST (PROPRIÉTAIRE)
   * PATCH /users/users/{id}/toggle-host
   * Route complète : http://localhost:8080/api/users/users/123/toggle-host
   * ============================
   */
  toggleHostRole(id: number): Observable<UserResponseDTO> {
    return this.apiService.patch<UserResponseDTO>(`${this.baseUrl}/users/${id}/toggle-host`);
  }

  // ========================================
  // GESTION DES LANGUES DE L'UTILISATEUR
  // ========================================

  /**
   * ============================
   * RÉCUPÉRER LES LANGUES D'UN UTILISATEUR
   * GET /users/users/{id}/languages
   * Route complète : http://localhost:8080/api/users/users/123/languages
   * ============================
   */
  getUserLanguages(userId: number): Observable<UserLanguageDTO[]> {
    return this.apiService.get<UserLanguageDTO[]>(`${this.baseUrl}/users/${userId}/languages`);
  }

  /**
   * ============================
   * AJOUTER UNE LANGUE À UN UTILISATEUR
   * POST /users/users/{id}/languages
   * Route complète : http://localhost:8080/api/users/users/123/languages
   * Body : { languageId: 1, proficiencyLevel: "INTERMEDIATE" }
   * ============================
   */
  addLanguageToUser(userId: number, languageData: AddLanguageDTO): Observable<UserLanguageDTO> {
    return this.apiService.post<UserLanguageDTO>(
      `${this.baseUrl}/users/${userId}/languages`,
      languageData
    );
  }

  /**
   * ============================
   * SUPPRIMER UNE LANGUE D'UN UTILISATEUR
   * DELETE /users/users/{id}/languages/{languageId}
   * Route complète : http://localhost:8080/api/users/users/123/languages/5
   * ============================
   */
  removeLanguageFromUser(userId: number, languageId: number): Observable<{ message: string }> {
    return this.apiService.delete<{ message: string }>(
      `${this.baseUrl}/users/${userId}/languages/${languageId}`
    );
  }

  /**
   * ============================
   * MODIFIER LE NIVEAU D'UNE LANGUE
   * PATCH /users/users/{id}/languages/{languageId}/proficiency?proficiencyLevel=ADVANCED
   * Route complète : http://localhost:8080/api/users/users/123/languages/5/proficiency
   * ============================
   */
  updateLanguageProficiency(
    userId: number,
    languageId: number,
    proficiencyLevel: ProficiencyLevel
  ): Observable<UserLanguageDTO> {
    return this.apiService.patch<UserLanguageDTO>(
      `${this.baseUrl}/users/${userId}/languages/${languageId}/proficiency`,
      null,
      { params: { proficiencyLevel } }
    );
  }

  uploadProfilePhoto(userId: number, file: File): Observable<{ photoUrl: string; message: string }> {
    const formData = new FormData();
    formData.append('file', file);

    return this.apiService.post<{ photoUrl: string; message: string }>(
      `${this.baseUrl}/users/${userId}/upload-photo`,
      formData
    );
  }
}
