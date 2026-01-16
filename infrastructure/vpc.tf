# vpc.tf - Création du VPC
resource "aws_vpc" "tf_main_vpc" {
  cidr_block           = var.vpc_cidr #olage des adresses IP du VPC
  enable_dns_hostnames = true # Active la résolution DNS pour les noms d'hôtes
  enable_dns_support   = true # Active le support DNS dans le VPC
  instance_tenancy     = "default" # permet aux instances d'utiliser une location partagée

  tags = {
    Name        = "${var.project_name}-vpc"
    Environment = var.environment
    Project     = var.project_name
  }
}
