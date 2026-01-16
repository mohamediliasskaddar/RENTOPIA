# route_tables.tf - Tables de routage

# Route Table Publique
resource "aws_route_table" "tf_table_public" {
  vpc_id = aws_vpc.tf_main_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.tf_igw.id
  }

  tags = {
    Name    = "${var.project_name}-public-rt"
    Project = var.project_name
  }
}

# Association des subnets publics
resource "aws_route_table_association" "tf_association_public" {
  count = 2

  subnet_id      = aws_subnet.subnet_public[count.index].id
  route_table_id = aws_route_table.tf_table_public.id
}

# Route Table Privée (une par AZ) - AVEC accès Internet via NAT
resource "aws_route_table" "tf_table_private" {
  count = 2

  vpc_id = aws_vpc.tf_main_vpc.id

  # ✅ AJOUTÉ : Route vers NAT Gateway pour Internet
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.tf_nat_gw.id  # Même NAT pour les 2 AZ
  }

  tags = {
    Name    = "${var.project_name}-private-rt-${count.index + 1}"
    Project = var.project_name
  }
}


# Association des subnets privés
resource "aws_route_table_association" "tf_association_private" {
  count = 2

  subnet_id      = aws_subnet.subnet_private[count.index].id
  route_table_id = aws_route_table.tf_table_private[count.index].id
}