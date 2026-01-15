# cloudwatch_logs.tf - Logs CloudWatch pour vos applications (SANS EKS logs)

# Log Group pour les microservices Spring Boot
resource "aws_cloudwatch_log_group" "microservices" {
  for_each = toset([
    "user-service",
    "api-gateway",      # Seulement les services critiques
    "payment-service",
    "frontend"
  ])

  name              = "/${var.project_name}/${var.environment}/${each.value}"
  retention_in_days = 3  # Garde 3 jours

  tags = {
    Service     = each.value
    Environment = var.environment
    Project     = var.project_name
    Type        = contains(["api-gateway", "eureka-server", "config-server"], each.value) ? "infrastructure" : "business"
  }
}

# Log Group pour ALB (access logs)
resource "aws_cloudwatch_log_group" "alb" {
  name              = "/aws/alb/${var.project_name}-${var.environment}"
  retention_in_days = 7  # Logs d'accès HTTP

  tags = {
    Name        = "${var.project_name}-alb-logs"
    Environment = var.environment
    Project     = var.project_name
  }
}