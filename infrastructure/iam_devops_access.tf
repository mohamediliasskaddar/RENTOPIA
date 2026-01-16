# iam_devops_access.tf – VERSION FINALE AVEC LES BONS RÔLES

resource "aws_iam_user" "devops" {
  name = "devops-jenkins-dev"
  tags = { 
    Purpose = "DevOps-General-Access"
  }
}

resource "aws_iam_access_key" "devops" {
  user = aws_iam_user.devops.name
}

# 1. Policy MINIMALE mais COMPLÈTE pour EKS
resource "aws_iam_user_policy" "devops_eks_essential" {
  name = "DevOps-EKS-Essential"
  user = aws_iam_user.devops.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      # CRITIQUE: Pour aws eks get-token (authentification Kubernetes)
      {
        Sid    = "AllowEKSAuth"
        Effect = "Allow"
        Action = [
          "eks:DescribeCluster",
          "eks:ListClusters"
        ]
        Resource = "*"
      },
      # IMPORTANT: Pour pouvoir assumer le rôle du cluster (CORRIGÉ avec le vrai nom)
      {
        Sid    = "AllowIAMForEKSAuth"
        Effect = "Allow"
        Action = [
          "sts:AssumeRole",
          "iam:ListRoles",
          "iam:GetRole"
        ]
        Resource = [
          "arn:aws:iam::870613971200:role/real-estate-dapp-eks-cluster-role",      # ← CORRIGÉ
          "arn:aws:iam::870613971200:role/real-estate-dapp-eks-node-role",         # ← CORRIGÉ
          "arn:aws:iam::870613971200:role/aws-service-role/eks.amazonaws.com/AWSServiceRoleForAmazonEKS*"
        ]
      }
    ]
  })
}

# 2. Attachez CES 2 policies MANAGED (essentielles)
resource "aws_iam_user_policy_attachment" "devops_eks_cluster_policy" {
  user       = aws_iam_user.devops.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}

resource "aws_iam_user_policy_attachment" "devops_eks_worker_policy" {
  user       = aws_iam_user.devops.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
}

# 3. Optional mais recommandé pour le reste
resource "aws_iam_user_policy_attachment" "devops_ecr_power" {
  user       = aws_iam_user.devops.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryPowerUser"
}

# 4. AJOUTEZ S3 (important pour vos buckets)
resource "aws_iam_user_policy_attachment" "devops_s3_read" {
  user       = aws_iam_user.devops.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess"
}


# Outputs SENSIBLES
output "devops_access_key" {
  value     = aws_iam_access_key.devops.id
  sensitive = true
}

output "devops_secret_key" {
  value     = aws_iam_access_key.devops.secret
  sensitive = true
}

# Output NON-sensible (sans les credentials)
output "devops_instructions" {
  value = <<-EOT
  Instructions pour le DevOps :

  1. Configurez AWS CLI avec les credentials fournis
  2. Région : eu-west-3
  3. Testez votre accès : aws sts get-caller-identity
  4. Configurez kubectl : aws eks update-kubeconfig --name real-estate-dapp-eks-dev --region eu-west-3
  5. Testez : kubectl get pods -A

  Note: Les credentials sont disponibles dans les outputs Terraform (markés comme sensibles).
  EOT
}