# iam_devops_access.tf – Accès SSM complet pour le DevOps
resource "aws_iam_user" "devops" {
  name = "devops-jenkins-${var.environment}"
  tags = { Purpose = "Jenkins-SSM-Access" }
}

resource "aws_iam_access_key" "devops" {
  user = aws_iam_user.devops.name
}

# ✅ CORRIGÉ : Permissions SSM complètes
resource "aws_iam_user_policy" "devops_ssm_full" {
  name = "Jenkins-SSM-Full-Access"
  user = aws_iam_user.devops.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowSSMSessionActions"
        Effect = "Allow"
        Action = [
          "ssm:StartSession",
          "ssm:TerminateSession",
          "ssm:ResumeSession",
          "ssm:DescribeSessions",
          "ssm:GetConnectionStatus"
        ]
        Resource = [
          "arn:aws:ec2:*:*:instance/${aws_instance.jenkins.id}",
          "arn:aws:ssm:*:*:document/SSM-SessionManagerRunShell"  # ✅ AJOUTÉ ICI
        ]
      },
      {
        Sid    = "AllowSSMDescribeForAllInstances"
        Effect = "Allow"
        Action = [
          "ssm:DescribeInstanceInformation",
          "ssm:DescribeSessions"
        ]
        Resource = "*"
      },
      {
        Sid    = "AllowEC2DescribeForTaggedInstances"
        Effect = "Allow"
        Action = [
          "ec2:DescribeInstances",
          "ec2:DescribeInstanceStatus"
        ]
        Resource = "*"
        Condition = {
          StringEquals = {
            "ec2:ResourceTag/Project" = var.project_name
          }
        }
      }
    ]
  })
}

# ✅ AJOUTEZ CETTE POLICY ESSENTIELLE
resource "aws_iam_user_policy_attachment" "devops_ssm_core" {
  user       = aws_iam_user.devops.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Output sensible
output "devops_access_key" {
  value     = aws_iam_access_key.devops.id
  sensitive = true
}

output "devops_secret_key" {
  value     = aws_iam_access_key.devops.secret
  sensitive = true
}