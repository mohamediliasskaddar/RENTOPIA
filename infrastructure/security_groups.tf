# security_groups.tf - Security Groups pour l'application de location immobilière
# VERSION SSM - Pas d'IPs dynamiques, accès via SSM Session Manager

# ============================================
# sg Application Load Balancer
# ============================================
resource "aws_security_group" "tf_alb" {
  name        = "${var.project_name}-alb-sg"
  description = "Security group for Application Load Balancer"
  vpc_id      = aws_vpc.tf_main_vpc.id

  # HTTPS depuis Internet
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS from Internet"
  }

  # HTTP depuis Internet (redirection vers HTTPS)
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP from Internet"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "All outbound traffic"
  }

  tags = {
    Name    = "${var.project_name}-alb-sg"
    Project = var.project_name
  }
}

# ============================================
# sg Frontend (Angular)
# ============================================
resource "aws_security_group" "tf_frontend" {
  name        = "${var.project_name}-frontend-sg"
  description = "Security group for Angular frontend (served by Nginx/S3)"
  vpc_id      = aws_vpc.tf_main_vpc.id

  # HTTP depuis ALB uniquement
  ingress {
    from_port       = 80
    to_port         = 80
    protocol        = "tcp"
    security_groups = [aws_security_group.tf_alb.id]
    description     = "HTTP from ALB only"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "All outbound traffic"
  }

  tags = {
    Name    = "${var.project_name}-frontend-sg"
    Project = var.project_name
  }
}

# ============================================
# sg Backend (Spring Boot)
# ============================================
resource "aws_security_group" "tf_backend" {
  name        = "${var.project_name}-backend-sg"
  description = "Security group for Spring Boot microservices"
  vpc_id      = aws_vpc.tf_main_vpc.id

  # API REST depuis ALB (port principal)
  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.tf_alb.id]
    description     = "API from ALB"
  }

  # Communication entre microservices (même SG)
  ingress {
    from_port   = 8081
    to_port     = 8090
    protocol    = "tcp"
    self        = true
    description = "Inter-microservices communication"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "All outbound"
  }

  tags = {
    Name    = "${var.project_name}-backend-sg"
    Project = var.project_name
  }
}

# ============================================
# sg Jenkins - VERSION CORRIGÉE (SSM)
# ============================================
resource "aws_security_group" "tf_jenkins" {
  name        = "${var.project_name}-jenkins-sg"
  description = "Security group for Jenkins CI/CD server (SSM access only)"
  vpc_id      = aws_vpc.tf_main_vpc.id


   # ✅ AJOUTÉ : Port 8099 pour Jenkins (celui utilisé par votre collègue)
  ingress {
    from_port   = 8099
    to_port     = 8099
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  # Ou l'IP spécifique de votre collègue
    description = "Jenkins Web UI on port 8099"
  }

  # ✅ GARDÉ : GitHub Webhooks (nécessaire pour CI/CD)
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["192.30.252.0/22", "185.199.108.0/22", "140.82.112.0/20"]
    description = "GitHub Webhooks"
  }

  # ✅ AJOUTÉ : Jenkins Web UI depuis tout le VPC (accès interne)
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = [aws_vpc.tf_main_vpc.cidr_block]
    description = "Jenkins Web UI from VPC internal (SSM access)"
  }

  # 🔥 AJOUTÉ : SSM nécessite HTTPS sortant vers AWS
  egress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "SSM Agent to AWS endpoints"
  }

  # Autres sorties
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "All outbound traffic"
  }

  tags = {
    Name    = "${var.project_name}-jenkins-sg"
    Project = var.project_name
    Access  = "SSM-Only"
  }
}

# ============================================
# sg Kubernetes - BLOC RESTAURÉ SANS DOUBLON
# ============================================
resource "aws_security_group" "tf_kubernetes" {
  name        = "${var.project_name}-k8s-sg"
  description = "Security group for Kubernetes cluster nodes"
  vpc_id      = aws_vpc.tf_main_vpc.id

  # ❌ SUPPRIMÉ : Règle API K8s (existe en règle séparée ligne 388)
  
  # ✅ Kubelet : entre nodes
  ingress {
    from_port   = 10250
    to_port     = 10250
    protocol    = "tcp"
    self        = true
    description = "Kubelet API"
  }

  # ✅ NodePort : depuis ALB
  ingress {
    from_port       = 30000
    to_port         = 32767
    protocol        = "tcp"
    security_groups = [aws_security_group.tf_alb.id]
    description     = "NodePort from ALB"
  }

  # ✅ Communication inter-pods
  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    self        = true
    description = "Inter-node traffic"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "All outbound"
  }

  tags = {
    Name    = "${var.project_name}-k8s-sg"
    Project = var.project_name
  }
}

# ============================================
# sg Monitoring - VERSION CORRIGÉE (SSM)
# ============================================
resource "aws_security_group" "tf_monitoring" {
  name        = "${var.project_name}-monitoring-sg"
  description = "Security group for Prometheus and Grafana (VPC access only)"
  vpc_id      = aws_vpc.tf_main_vpc.id

  # ✅ AJOUTÉ : Grafana depuis VPC seulement (accès interne)
  ingress {
    from_port   = 3000
    to_port     = 3000
    protocol    = "tcp"
    cidr_blocks = [aws_vpc.tf_main_vpc.cidr_block]
    description = "Grafana UI from VPC internal (SSM access)"
  }

  # ✅ GARDÉ : AlertManager (interne VPC)
  ingress {
    from_port   = 9093
    to_port     = 9093
    protocol    = "tcp"
    cidr_blocks = [aws_vpc.tf_main_vpc.cidr_block]
    description = "AlertManager internal"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "All outbound"
  }

  tags = {
    Name    = "${var.project_name}-monitoring-sg"
    Project = var.project_name
    Access  = "VPC-Only"
  }
}

# ============================================
# sg Blockchain (Hardhat node)
# ============================================
resource "aws_security_group" "tf_blockchain" {
  name        = "${var.project_name}-blockchain-sg"
  description = "Security group for Hardhat node"
  vpc_id      = aws_vpc.tf_main_vpc.id

  ingress {
    from_port       = 8545
    to_port         = 8545
    protocol        = "tcp"
    security_groups = [aws_security_group.tf_backend.id]
    description     = "Ethereum JSON-RPC from backend"
  }

  ingress {
    from_port       = 8546
    to_port         = 8546
    protocol        = "tcp"
    security_groups = [aws_security_group.tf_backend.id]
    description     = "Ethereum WebSocket from backend"
  }

  egress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS to Ethereum providers"
  }

  tags = {
    Name    = "${var.project_name}-blockchain-sg"
    Project = var.project_name
  }
}

resource "aws_security_group" "tf_db" {
  name        = "${var.project_name}-db-sg-${var.environment}"
  description = "Security group for MySQL RDS database"
  vpc_id      = aws_vpc.tf_main_vpc.id

  # ❌ PAS de ingress ici - tout est géré par aws_security_group_rule
  
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["10.0.0.0/16"]
    description = "Internal VPC communication"
  }

  tags = {
    Name    = "${var.project_name}-db-sg"
    Project = var.project_name
  }
}

# ============================================
# RÈGLES ADDITIONNELLES (évite les références circulaires)
# ============================================

# Backend peut recevoir depuis Jenkins (déploiement)
resource "aws_security_group_rule" "backend_from_jenkins" {
  type                     = "ingress"
  from_port                = 8080
  to_port                  = 8080
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.tf_jenkins.id
  security_group_id        = aws_security_group.tf_backend.id
  description              = "Deployment from Jenkins"
}

# Backend peut recevoir depuis Monitoring (métriques)
resource "aws_security_group_rule" "backend_from_monitoring" {
  type                     = "ingress"
  from_port                = 8080
  to_port                  = 8080
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.tf_monitoring.id
  security_group_id        = aws_security_group.tf_backend.id
  description              = "Metrics from Prometheus"
}

# Monitoring peut scraper Backend
resource "aws_security_group_rule" "monitoring_scrape_backend" {
  type                     = "ingress"
  from_port                = 9090
  to_port                  = 9090
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.tf_backend.id
  security_group_id        = aws_security_group.tf_monitoring.id
  description              = "Prometheus scraping from backend"
}

# Monitoring peut scraper Kubernetes
resource "aws_security_group_rule" "monitoring_scrape_k8s" {
  type                     = "ingress"
  from_port                = 9090
  to_port                  = 9090
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.tf_kubernetes.id
  security_group_id        = aws_security_group.tf_monitoring.id
  description              = "Prometheus scraping from k8s"
}


# Kubernetes vers Backend (communication inter-services)
resource "aws_security_group_rule" "k8s_to_backend" {
  type                     = "ingress"
  from_port                = 8080
  to_port                  = 8090
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.tf_kubernetes.id
  security_group_id        = aws_security_group.tf_backend.id
  description              = "K8s to Backend communication"
}

# Monitoring vers Kubelet metrics
resource "aws_security_group_rule" "monitoring_to_k8s_metrics" {
  type                     = "ingress"
  from_port                = 10250
  to_port                  = 10250
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.tf_monitoring.id
  security_group_id        = aws_security_group.tf_kubernetes.id
  description              = "Prometheus to Kubelet metrics"
}

# Backend vers Kubernetes API (si besoin)
resource "aws_security_group_rule" "k8s_api_from_backend" {
  type                     = "ingress"
  from_port                = 6443
  to_port                  = 6443
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.tf_backend.id
  security_group_id        = aws_security_group.tf_kubernetes.id
  description              = "Kubernetes API from Backend"
}

# Jenkins vers Kubernetes (pour les déploiements) - RÈGLE UNIQUE
resource "aws_security_group_rule" "jenkins_to_k8s" {
  type                     = "ingress"
  from_port                = 6443
  to_port                  = 6443
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.tf_jenkins.id
  security_group_id        = aws_security_group.tf_kubernetes.id
  description              = "Kubernetes API from Jenkins"
}

# ============================================
# OUTPUTS
# ============================================
output "sg_alb_id" {
  value       = aws_security_group.tf_alb.id
  description = "Security Group ID for ALB"
}

output "sg_frontend_id" {
  value       = aws_security_group.tf_frontend.id
  description = "Security Group ID for Frontend"
}

output "sg_backend_id" {
  value       = aws_security_group.tf_backend.id
  description = "Security Group ID for Backend"
}

output "sg_db_id" {
  value       = aws_security_group.tf_db.id
  description = "Security Group ID for Database"
}

output "sg_jenkins_id" {
  value       = aws_security_group.tf_jenkins.id
  description = "Security Group ID for Jenkins"
}

output "sg_kubernetes_id" {
  value       = aws_security_group.tf_kubernetes.id
  description = "Security Group ID for Kubernetes"
}

output "sg_monitoring_id" {
  value       = aws_security_group.tf_monitoring.id
  description = "Security Group ID for Monitoring"
}

output "sg_blockchain_id" {
  value       = aws_security_group.tf_blockchain.id
  description = "Security Group ID for Blockchain"
}