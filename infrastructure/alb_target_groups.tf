# alb_target_groups.tf - Target Groups pour ALB

# Target Group 1 : API Gateway (point d'entrée des microservices)
resource "aws_lb_target_group" "api_gateway" {
  name        = "${var.project_name}-api-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.tf_main_vpc.id
  target_type = "ip"  # Important pour EKS

  health_check {
    enabled             = true
    path                = "/actuator/health"  # Spring Boot health endpoint
    port                = "traffic-port"
    protocol            = "HTTP"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    matcher             = "200"
  }

  deregistration_delay = 30  # Temps avant de retirer un pod du load balancing

  tags = {
    Name        = "${var.project_name}-api-gateway-tg"
    Environment = var.environment
    Project     = var.project_name
    Service     = "api-gateway"
  }
}

# Target Group 2 : Frontend Angular
resource "aws_lb_target_group" "frontend" {
  name        = "${var.project_name}-frontend-tg"
  port        = 80
  protocol    = "HTTP"
  vpc_id      = aws_vpc.tf_main_vpc.id
  target_type = "ip"

  health_check {
    enabled             = true
    path                = "/"  # Angular app root
    port                = "traffic-port"
    protocol            = "HTTP"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    matcher             = "200"
  }

  deregistration_delay = 30

  tags = {
    Name        = "${var.project_name}-frontend-tg"
    Environment = var.environment
    Project     = var.project_name
    Service     = "frontend"
  }
}