# media-service-iam.tf
# Rôle IAM pour le media-service

# 1. Rôle IAM que le media-service peut assumer
resource "aws_iam_role" "media_service" {
  name = "real-estate-dapp-media-service-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = aws_iam_openid_connect_provider.eks_oidc.arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "${replace(aws_iam_openid_connect_provider.eks_oidc.url, "https://", "")}:sub" = "system:serviceaccount:dev:media-service"
          }
        }
      }
    ]
  })
}

# 2. Permissions S3 (écriture + lecture)
resource "aws_iam_role_policy" "media_service_s3" {
  name = "media-service-s3-policy"
  role = aws_iam_role.media_service.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:GetObject", 
          "s3:DeleteObject",
          "s3:ListBucket"
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