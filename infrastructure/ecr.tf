# ecr.tf - Repositories ECR pour les 13 microservices applicatifs

data "aws_caller_identity" "current" {}

# Créer un repository ECR pour chaque microservice
resource "aws_ecr_repository" "tf_microservices" {
  for_each = toset(local.microservices)
  
  name                 = "${var.project_name}-${each.value}-${var.environment}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name        = "${each.value} Repository"
    Environment = var.environment
    Project     = var.project_name
    Service     = each.value
    Type = (
      contains(["api-gateway", "eureka-server"], each.value) ? "infrastructure" : "business"
    )
  }
}

# Politique de lifecycle
resource "aws_ecr_lifecycle_policy" "tf_microservices_policy" {
  for_each   = toset(local.microservices)
  repository = aws_ecr_repository.tf_microservices[each.key].name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Garder seulement les 10 dernières images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 10
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}
