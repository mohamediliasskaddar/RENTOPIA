export interface Review {
  id: number;
  reservationId: number;
  userId: number;
  propertyId: number;
  reviewText: string;
  ratingValue: number;
  isVisible: boolean;
  createdAt: string;
  updatedAt: string;
  guestName?: string;
  propertyTitle?: string;

}

export interface ReviewRequest {
  reservationId: number;
  userId: number;
  propertyId: number;
  reviewText: string;
  ratingValue: number;
}

export interface ReviewUpdateRequest {
  reviewText?: string;
  isVisible?: boolean;
}



export interface ReviewWithProperty {
  // Review info
  id: number;
  reservationId: number;
  userId: number;
  propertyId: number;
  reviewText: string;
  ratingValue: number;
  isVisible: boolean;
  createdAt: string;
  updatedAt: string;

  // ✅ Property info
  propertyTitle: string;
  propertyCity: string;
  propertyCountry: string;
  propertyMainPhoto: string| null;
  propertyType: string;

  // ✅ Host info (optionnel)
  hostId?: number;

}

// ✅ Les interfaces existantes restent inchangées
export interface Review {
  id: number;
  reservationId: number;
  userId: number;
  propertyId: number;
  reviewText: string;
  ratingValue: number;
  isVisible: boolean;
  createdAt: string;
  updatedAt: string;
  guestName?: string;
  propertyTitle?: string;

}

export interface ReviewRequest {
  reservationId: number;
  userId: number;
  propertyId: number;
  reviewText: string;
  ratingValue: number;
}

export interface ReviewUpdateRequest {
  reviewText?: string;
  isVisible?: boolean;
}
