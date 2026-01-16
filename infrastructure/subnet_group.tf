# subnet_group.tf - Subnets privés pour RDS
resource "aws_db_subnet_group" "tf_db_subnet_group" {
  name       = "${var.project_name}-db-subnet-group-${var.environment}"
  subnet_ids = [
    aws_subnet.subnet_private[0].id,
    aws_subnet.subnet_private[1].id
  ]

  tags = {
    Name        = "${var.project_name}-db-subnet-group"
    Environment = var.environment
    Project     = var.project_name
  }
  
  description = "Private subnets for RDS MySQL database"
}