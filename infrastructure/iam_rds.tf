# iam_rds_role.tf (si tu as ce fichier)

resource "aws_iam_role" "tf_backend_rds_role" {
  name = "${var.project_name}-backend-rds-role-${var.environment}"

  # ✅ VERSION SIMPLIFIÉE
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"  # ← Au lieu de EKS
      }
    }]
  })
}

resource "aws_iam_policy" "tf_rds_access_policy" {
  name = "${var.project_name}-rds-access-policy"
  
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "rds-db:connect"
        ]
        Resource = aws_db_instance.tf_real_estate_db.arn
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "tf_backend_rds_policy" {
  policy_arn = aws_iam_policy.tf_rds_access_policy.arn
  role       = aws_iam_role.tf_backend_rds_role.name
}