# subnets.tf - Subnets publics et privés

# Availability Zones 
data "aws_availability_zones" "available" {
  state = "available"
}

# subnets.tf - CORRIGÉ (sans cycle)

# Subnets Publics (2 AZ) 
resource "aws_subnet" "subnet_public" {
  count = 2

  vpc_id                  = aws_vpc.tf_main_vpc.id
  cidr_block              = "10.0.${count.index + 1}.0/24"
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name        = "${var.project_name}-public-subnet-${count.index + 1}"
    Type        = "Public"
    Project     = var.project_name
    # ✅ MODIFIÉ : Utiliser le nom du projet au lieu du cluster
    "kubernetes.io/role/elb" = "1"
  }
}

# Subnets Privés (2 AZ) 
resource "aws_subnet" "subnet_private" {
  count = 2

  vpc_id                  = aws_vpc.tf_main_vpc.id
  cidr_block              = "10.0.${count.index + 10}.0/24"
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = false

  tags = {
    Name        = "${var.project_name}-private-subnet-${count.index + 1}"
    Type        = "Private"
    Project     = var.project_name
    # ✅ MODIFIÉ : Tag fixe sans référence au cluster
    "kubernetes.io/role/internal-elb" = "1"
  }
}