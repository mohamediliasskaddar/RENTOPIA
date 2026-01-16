# gateways.tf - Gateway et NAT Gateway

# Internet Gateway (pour subnets publics)
resource "aws_internet_gateway" "tf_igw" {
  vpc_id = aws_vpc.tf_main_vpc.id
  tags = {
    Name    = "${var.project_name}-igw"
    Project = var.project_name
  }
}

# Elastic IP pour NAT Gateway (UN SEUL pour économiser)
resource "aws_eip" "tf_nat_eip" {
  domain = "vpc"
  tags = {
    Name    = "${var.project_name}-nat-eip"
    Project = var.project_name
  }
  depends_on = [aws_internet_gateway.tf_igw]
}

# NAT Gateway (UN SEUL dans le premier subnet public)
resource "aws_nat_gateway" "tf_nat_gw" {
  allocation_id = aws_eip.tf_nat_eip.id
  subnet_id     = aws_subnet.subnet_public[0].id  # Premier subnet public seulement
  tags = {
    Name    = "${var.project_name}-nat"
    Project = var.project_name
  }
  depends_on = [aws_internet_gateway.tf_igw]
}