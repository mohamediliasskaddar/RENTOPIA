// src/app/core/models/user.model.ts

/**
 * ============================
 * ENUM ProficiencyLevel
 * Correspond EXACTEMENT à l'enum backend UserLanguage.ProficiencyLevel
 * ============================
 */
export enum ProficiencyLevel {
  BASIC = 'BASIC',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED',
  NATIVE = 'NATIVE'
}

/**
 * ============================
 * Interface UserLanguageDTO
 * Correspond EXACTEMENT au UserLanguageDTO backend
 * Représente une langue parlée par l'utilisateur avec son niveau
 * ============================
 */
export interface UserLanguageDTO {
  languageId: number;          // ID de la langue dans la table languages
  languageCode: string;        // Code ISO (ex: "fr", "en", "ar")
  languageName: string;        // Nom de la langue (ex: "French")
  languageNativeName?: string; // Nom natif (ex: "Français")
  proficiencyLevel: ProficiencyLevel; // Niveau de maîtrise
}

/**
 * ============================
 * Interface User
 * Correspond à l'entité User backend
 * Utilisée pour la gestion interne côté frontend
 * ============================
 */
export interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  tel?: string;
  walletAdresse: string;
  photoUrl?: string;
  emailVerified: boolean;
  telephoneVerified: boolean;
  isGuest: boolean;
  isHost: boolean;
  createdAt: string; // LocalDateTime backend → string en JSON
  languages?: UserLanguageDTO[]; // Liste des langues parlées
}

/**
 * ============================
 * Interface UserResponseDTO
 * Correspond EXACTEMENT au UserResponseDTO backend
 * C'est ce que le backend renvoie dans les réponses API
 * ============================
 */
export interface UserResponseDTO {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  tel?: string;
  walletAdresse: string;
  photoUrl?: string;
  emailVerified: boolean;
  telephoneVerified: boolean;
  isGuest: boolean;
  isHost: boolean;
  createdAt: string;
  languages?: UserLanguageDTO[];
}

/**
 * ============================
 * Interface AddLanguageDTO
 * Correspond EXACTEMENT au AddLanguageDTO backend
 * Utilisée pour ajouter une langue au profil utilisateur
 * ============================
 */
export interface AddLanguageDTO {
  languageId: number;
  proficiencyLevel?: ProficiencyLevel; // Optionnel, par défaut INTERMEDIATE
}

/**
 * ============================
 * Interface UpdateUserDTO
 * Pour mettre à jour le profil utilisateur
 * Tous les champs sont optionnels
 * ============================
 */
export interface UpdateUserDTO {
  nom?: string;
  prenom?: string;
  tel?: string;
  photoUrl?: string;
  walletAdresse?: string;
}
