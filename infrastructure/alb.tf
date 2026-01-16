# alb.tf
resource "aws_lb" "main" {
  name               = "${var.project_name}-alb-${var.environment}"
  internal           = false  # Public (accessible depuis Internet)
  load_balancer_type = "application"
  
  # Security Group pour l'ALB (déjà créé dans security_groups.tf)
  security_groups    = [aws_security_group.tf_alb.id]
  
  # Subnets publics (où l'ALB sera placé)
  subnets            = aws_subnet.subnet_public[*].id

  # Désactiver la protection contre la suppression (important pour terraform destroy)
  enable_deletion_protection = false
  
  # Tags
  tags = {
    Name        = "${var.project_name}-alb"
    Environment = var.environment
    Project     = var.project_name
    Type        = "application"
  }
}
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"
  
  # ✅ Action par défaut : Rediriger vers le frontend
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.frontend.arn
  }
  
  tags = {
    Name = "${var.project_name}-http-listener"
  }
}

# Règle 1 : Router /api/* vers l'API Gateway
resource "aws_lb_listener_rule" "api_routing" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 100

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.api_gateway.arn
  }

  condition {
    path_pattern {
      values = ["/api/*"]
    }
  }

  tags = {
    Name = "${var.project_name}-api-routing-rule"
  }
}