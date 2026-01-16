# vpc_endpoints_ecr.tf - Accès ECR sans NAT Gateway (économie ~64€/mois)

# Endpoint ECR API
resource "aws_vpc_endpoint" "ecr_api" {
  vpc_id              = aws_vpc.tf_main_vpc.id
  service_name        = "com.amazonaws.${var.aws_region}.ecr.api"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = aws_subnet.subnet_private[*].id
  security_group_ids  = [aws_security_group.tf_vpc_endpoints.id]
  private_dns_enabled = true

  tags = {
    Name        = "${var.project_name}-ecr-api-endpoint"
    Environment = var.environment
    Project     = var.project_name
  }
}

# Endpoint ECR Docker
resource "aws_vpc_endpoint" "ecr_dkr" {
  vpc_id              = aws_vpc.tf_main_vpc.id
  service_name        = "com.amazonaws.${var.aws_region}.ecr.dkr"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = aws_subnet.subnet_private[*].id
  security_group_ids  = [aws_security_group.tf_vpc_endpoints.id]
  private_dns_enabled = true

  tags = {
    Name        = "${var.project_name}-ecr-dkr-endpoint"
    Environment = var.environment
    Project     = var.project_name
  }
}

# Endpoint EKS
resource "aws_vpc_endpoint" "eks" {
  vpc_id              = aws_vpc.tf_main_vpc.id
  service_name        = "com.amazonaws.${var.aws_region}.eks"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = aws_subnet.subnet_private[*].id
  security_group_ids  = [aws_security_group.tf_vpc_endpoints.id]
  private_dns_enabled = true

  tags = {
    Name        = "${var.project_name}-eks-endpoint"
    Environment = var.environment
    Project     = var.project_name
  }
}

# Endpoint EC2 (pour EKS nodes)
resource "aws_vpc_endpoint" "ec2" {
  vpc_id              = aws_vpc.tf_main_vpc.id
  service_name        = "com.amazonaws.${var.aws_region}.ec2"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = aws_subnet.subnet_private[*].id
  security_group_ids  = [aws_security_group.tf_vpc_endpoints.id]
  private_dns_enabled = true

  tags = {
    Name        = "${var.project_name}-ec2-endpoint"
    Environment = var.environment
    Project     = var.project_name
  }
}

# Security Group pour VPC Endpoints
resource "aws_security_group" "tf_vpc_endpoints" {
  name        = "${var.project_name}-vpc-endpoints-sg"
  description = "Security group for VPC Endpoints (ECR, EKS, EC2)"
  vpc_id      = aws_vpc.tf_main_vpc.id

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
    description = "HTTPS from VPC"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "All outbound"
  }

  tags = {
    Name    = "${var.project_name}-vpc-endpoints-sg"
    Project = var.project_name
  }
}