// src/app/core/models/language.model.ts

/**
 * ============================
 * Interface Language
 * Correspond à l'entité Language backend
 * Représente une langue disponible dans le système
 * ============================
 */
export interface Language {
  id: number;
  code: string;        // Code ISO (ex: "fr", "en", "ar")
  name: string;        // Nom en anglais (ex: "French", "English")
  nativeName?: string; // Nom natif (ex: "Français", "العربية")
  isActive: boolean;   // Langue active ou désactivée
}

/**
 * ============================
 * Interface LanguageDTO
 * Correspond EXACTEMENT au LanguageDTO backend
 * Version simplifiée retournée par l'API
 * ============================
 */
export interface LanguageDTO {
  id: number;
  code: string;
  name: string;
  nativeName?: string;
}
