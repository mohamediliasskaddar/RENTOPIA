variable "aws_region" {
  description = "Région AWS où déployer l'infra"
  type    = string
  default = "eu-west-3"  # Paris
}

variable "project_name" {
  type    = string
  default = "real-estate-dapp"  # Nom du projet
}
variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "dev"  # Valeur par défaut
}
variable "vpc_cidr" {
  description = "Plage CIDR du VPC"
  type        = string
  default     = "10.0.0.0/16"
}
variable "db_password" {
  description = "Mot de passe de la base de données RDS"
  type        = string
  sensitive   = true  # ← Cache dans les logs
}
variable "db_username" {
  description = "Nom d'utilisateur RDS"
  type        = string
  default     = "admin"
}
# Dans variables.tf - AJOUTE 3 LIGNES SEULEMENT :
variable "blockchain_rpc_url" {
  description = "URL RPC externe (ex: https://rpc.sepolia.org)"
  type        = string
  default     = "https://rpc.sepolia.org"
}

locals {
  microservices = [
    # Services métier (10)
    "user-service",
    "listing-service",
    "booking-service", 
    "payment-service",
    "messaging-service",
    "notification-service",
    "review-service",
    "media-service",
    "blockchain-service",  # Service d'intégration Web3
    "ai-service",
    
    # Infrastructure Spring Cloud (2)
    "api-gateway",
    "eureka-server",

    # frontend géré séparément
    "frontend"
  ]
}