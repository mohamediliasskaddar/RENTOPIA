# iam_jenkins_role.tf - Role IAM pour Jenkins + SSM

resource "aws_iam_role" "tf_jenkins_role" {
  name = "${var.project_name}-jenkins-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
    }]
  })

  tags = {
    Name        = "Jenkins-CI-CD-Role"
    Environment = var.environment
    Project     = var.project_name
    Purpose     = "Jenkins-EC2-SSM"
  }
}

resource "aws_iam_policy" "tf_jenkins_policy" {
  name = "${var.project_name}-jenkins-policy-${var.environment}"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload"
        ]
        Resource = "*"
      },
      {
        Effect   = "Allow"
        Action   = [
          "eks:DescribeCluster",
          "eks:ListClusters",
          "eks:DescribeNodegroup",
          "eks:ListNodegroups"
        ]
        Resource = "*"
      },
      {
        Effect   = "Allow"
        Action   = ["s3:GetObject", "s3:PutObject", "s3:ListBucket"]
        Resource = [
          "arn:aws:s3:::${var.project_name}-jenkins-artifacts-${var.environment}",
          "arn:aws:s3:::${var.project_name}-jenkins-artifacts-${var.environment}/*"
        ]
      }
    ]
  })
}

# SSM Session Manager (obligatoire)
resource "aws_iam_role_policy_attachment" "ssm_core" {
  role       = aws_iam_role.tf_jenkins_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Policy custom CI/CD
resource "aws_iam_role_policy_attachment" "custom_policy" {
  role       = aws_iam_role.tf_jenkins_role.name
  policy_arn = aws_iam_policy.tf_jenkins_policy.arn
}

# CloudWatch (logs Jenkins + système)
resource "aws_iam_role_policy_attachment" "cloudwatch" {
  role       = aws_iam_role.tf_jenkins_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

resource "aws_iam_instance_profile" "tf_jenkins_profile" {
  name = "${var.project_name}-jenkins-profile-${var.environment}"
  role = aws_iam_role.tf_jenkins_role.name
}