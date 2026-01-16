# outputs.tf - Outputs complets

output "public_subnet_ids" {
  description = "IDs des sous-réseaux publics"
  value       = aws_subnet.subnet_public[*].id
}

output "private_subnet_ids" {
  description = "IDs des sous-réseaux privés"
  value       = aws_subnet.subnet_private[*].id
}

output "availability_zones" {
  description = "Zones de disponibilité utilisées"
  value       = data.aws_availability_zones.available.names
}

output "cloudfront_domain_name" {
  description = "URL du CDN pour les images"
  value       = aws_cloudfront_distribution.media_cdn.domain_name
}

output "cloudfront_id" {
  description = "ID de la distribution CloudFront"
  value       = aws_cloudfront_distribution.media_cdn.id
}

# 1. kubeconfig EKS (ACTIVÉ)
output "eks_cluster_name" {
  value       = aws_eks_cluster.main.name
  description = "Nom du cluster EKS"
}

output "eks_cluster_endpoint" {
  value       = aws_eks_cluster.main.endpoint
  description = "Endpoint API du cluster EKS"
}

output "eks_cluster_certificate" {
  value       = aws_eks_cluster.main.certificate_authority[0].data
  description = "Certificat CA du cluster"
  sensitive   = true
}

output "kubeconfig_command" {
  value       = "aws eks update-kubeconfig --name ${aws_eks_cluster.main.name} --region ${var.aws_region}"
  description = "Commande pour générer le kubeconfig"
}

# 2. ECR (ACTIVÉ)
output "ecr_registry_url" {
  value       = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
  description = "URL du registre ECR"
}

# 4. Réseau
output "vpc_id" {
  value       = aws_vpc.tf_main_vpc.id
  description = "ID du VPC principal"
}

output "public_subnets" {
  value       = aws_subnet.subnet_public[*].id
  description = "IDs des subnets publics (ALB)"
}

output "private_subnets" {
  value       = aws_subnet.subnet_private[*].id
  description = "IDs des subnets privés (pods EKS)"
}

output "alb_security_group_id" {
  value       = aws_security_group.tf_alb.id
  description = "Security Group du ALB"
}

# 6. Application URLs - ✅ MODIFIÉ ICI !
output "app_url" {
  value       = "http://${aws_lb.main.dns_name}"  # ← CHANGÉ : http au lieu de https
  description = "URL publique de l'application (HTTP pour développement)"
}

output "api_url" {
  value       = "http://${aws_lb.main.dns_name}/api"  # ← CHANGÉ : http au lieu de https
  description = "URL de l'API backend (HTTP pour développement)"
}

# 7. RDS
output "rds_endpoint" {
  value       = aws_db_instance.tf_real_estate_db.endpoint
  description = "Endpoint RDS MySQL"
  sensitive   = false
}

# 8. S3
output "s3_properties_bucket" {
  value       = aws_s3_bucket.tf_properties_images.bucket
  description = "Nom du bucket S3 pour les propriétés"
}

output "s3_users_bucket" {
  value       = aws_s3_bucket.tf_users_photos.bucket
  description = "Nom du bucket S3 pour les utilisateurs"
}

# ✅ AJOUTEZ : ALB DNS Name (utile pour tests)
output "alb_dns_name" {
  value       = aws_lb.main.dns_name
  description = "Nom DNS de l'ALB"
}

# ✅ AJOUTEZ : Message de statut
output "deployment_status" {
  value = <<-EOT
    ✅ DÉPLOIEMENT RÉUSSI !
    
    Application: http://${aws_lb.main.dns_name}
    API Backend: http://${aws_lb.main.dns_name}/api
    Database: ${aws_db_instance.tf_real_estate_db.endpoint}
  
    
    Note: Jenkins a été supprimé. Déploiement via Docker Hub + kubectl.
  EOT
}

# 9. DevOps Access - ✅ CORRIGÉ (problème local.microservices)

# ========================================
# OUTPUTS pour le DevOps Engineer
# ========================================

output "ecr_repositories_count" {
  value       = length(local.microservices)
  description = "Nombre total de repositories ECR"
}

output "ecr_login_command" {
  value       = "aws ecr get-login-password --region ${var.aws_region} | docker login --username AWS --password-stdin ${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
  description = "Commande pour se connecter à ECR"
}

output "ecr_repositories_urls" {
  value = {
    for service in local.microservices :
    service => aws_ecr_repository.tf_microservices[service].repository_url
  }
  description = "URLs complètes de tous les repositories ECR"
}

output "ecr_repositories_names" {
  value = {
    for service in local.microservices :
    service => aws_ecr_repository.tf_microservices[service].name
  }
  description = "Noms des repositories ECR"
}

# Output formaté pour copier-coller facilement
output "ecr_repositories_list" {
  value = [
    for service in local.microservices :
    "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/${var.project_name}-${service}-${var.environment}"
  ]
  description = "Liste simple de toutes les URLs ECR"
}

# ✅ AJOUTEZ : Guide rapide pour DevOps
output "ecr_quick_guide" {
  value = <<-EOT
    📦 ECR - Container Registry
    
    Total: ${length(local.microservices)} repositories
    Login: aws ecr get-login-password --region ${var.aws_region} | docker login...
    
    Liste complète:
    ${join("\n    ", [for s in local.microservices : "- ${var.project_name}-${s}-${var.environment}"])}
  EOT
}

output "alb_arn" {
  value       = aws_lb.main.arn
  description = "ARN de l'ALB"
}

output "alb_zone_id" {
  value       = aws_lb.main.zone_id
  description = "Zone ID de l'ALB"
}


# CloudWatch outputs
output "cloudwatch_dashboard_url" {
  value = "https://${var.aws_region}.console.aws.amazon.com/cloudwatch/home?region=${var.aws_region}#dashboards:name=${aws_cloudwatch_dashboard.main.dashboard_name}"
  description = "URL du dashboard CloudWatch"
}

output "cloudwatch_alarms" {
  value = [
    aws_cloudwatch_metric_alarm.alb_5xx_errors.alarm_name,
    aws_cloudwatch_metric_alarm.rds_high_cpu.alarm_name
  ]
  description = "Liste des alarmes CloudWatch créées"
}

output "cloudwatch_log_groups_created" {
  value = {
    alb = aws_cloudwatch_log_group.alb.name
    microservices = [for name, lg in aws_cloudwatch_log_group.microservices : lg.name]
  }
  description = "Log Groups CloudWatch créés"
}