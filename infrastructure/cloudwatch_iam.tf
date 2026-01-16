# cloudwatch_iam.tf - Permissions pour CloudWatch Logs

# IAM Policy pour permettre aux pods EKS d'écrire dans CloudWatch
resource "aws_iam_policy" "eks_cloudwatch_logs" {
  name        = "${var.project_name}-eks-cloudwatch-logs-policy"
  description = "Permissions for EKS pods to write logs to CloudWatch"
  path        = "/"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams",
          "logs:CreateLogGroup"
        ]
        Resource = [
          # Pour vos microservices
          "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/${var.project_name}/${var.environment}/*:*",
          # Pour ALB
          "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/alb/${var.project_name}-${var.environment}:*"
        ]
      }
    ]
  })
}

# Attachez cette policy au rôle des nodes EKS
resource "aws_iam_role_policy_attachment" "eks_nodes_cloudwatch_logs" {
  role       = aws_iam_role.eks_nodes.name
  policy_arn = aws_iam_policy.eks_cloudwatch_logs.arn
}