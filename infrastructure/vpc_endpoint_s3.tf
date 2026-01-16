# vpc_endpoint_s3.tf - Accès S3 sans Internet (sécurité + économie)
#VPC Endpoint crée un passage secret sécurisé directement vers S3, sans passer par Internet.

resource "aws_vpc_endpoint" "tf_s3_endpoint" {
  vpc_id            = aws_vpc.tf_main_vpc.id
  service_name      = "com.amazonaws.${var.aws_region}.s3"
  vpc_endpoint_type = "Gateway"

  route_table_ids = [
    aws_route_table.tf_table_private[0].id,
    aws_route_table.tf_table_private[1].id
  ]

  tags = {
    Name        = "${var.project_name}-s3-endpoint"
    Environment = var.environment
    Project     = var.project_name
  }
}