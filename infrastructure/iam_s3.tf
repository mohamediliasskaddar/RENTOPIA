# iam_backend_roles.tf - Rôles pour les microservices backend

# Rôle S3 pour les pods Spring Boot
resource "aws_iam_role" "tf_backend_s3_role" {
  name = "${var.project_name}-backend-s3-role-${var.environment}"

  # ✅ VERSION SIMPLIFIÉE (sans EKS pour l'instant)
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"  # ← Simplifié
        }
      }
    ]
  })

  tags = {
    Name        = "Backend S3 Role"
    Environment = var.environment
    Project     = var.project_name
  }
}

# Politique S3
resource "aws_iam_policy" "tf_backend_s3_policy" {
  name = "${var.project_name}-backend-s3-policy-${var.environment}"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:GetObject", 
          "s3:DeleteObject",
          "s3:ListBucket",
          "s3:GetObjectVersion"
        ]
        Resource = [
          aws_s3_bucket.tf_properties_images.arn,
          "${aws_s3_bucket.tf_properties_images.arn}/*",
          aws_s3_bucket.tf_users_photos.arn,
          "${aws_s3_bucket.tf_users_photos.arn}/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "tf_backend_s3_attach" {
  role       = aws_iam_role.tf_backend_s3_role.name
  policy_arn = aws_iam_policy.tf_backend_s3_policy.arn
}

# ✅ INSTANCE PROFILE (pour attacher le rôle aux instances EC2/pods)
resource "aws_iam_instance_profile" "tf_backend_profile" {
  name = "${var.project_name}-backend-profile-${var.environment}"
  role = aws_iam_role.tf_backend_s3_role.name
}