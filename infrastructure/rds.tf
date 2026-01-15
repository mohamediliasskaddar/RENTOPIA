# rds.tf - Base de données MySQL

resource "aws_db_instance" "tf_real_estate_db" {
  identifier              = "${var.project_name}-db-${var.environment}"
  engine                  = "mysql"
  engine_version          = "8.0.43"  # ← VERSION STABLE
  instance_class          = "db.t4g.micro"
  allocated_storage       = 10  # ← 10GB au lieu de 20 (suffisant pour dev)
  max_allocated_storage   = 0   # ← 0 pour désactiver l'auto-scaling payant
  storage_type            = "gp2"
  storage_encrypted       = false  # ← false pour éviter les coûts KMS
  # kms_key_id            = aws_kms_key.tf_db_key.arn  # ← SUPPRIMER cette ligne
  publicly_accessible = false # ← false pour ne pas exposer la BDD sur Internet

  db_name                 = "realestatedb"
  username                = var.db_username
  password                = var.db_password
  parameter_group_name    = aws_db_parameter_group.tf_mysql_params.name

  vpc_security_group_ids  = [aws_security_group.tf_db.id]
  db_subnet_group_name    = aws_db_subnet_group.tf_db_subnet_group.name

  # ⚠️ MODIFICATIONS CRITIQUES POUR ÉVITER LES COÛTS :
  multi_az                = false  # ← FALSE au lieu de true (économise ~$15/mois)
  backup_retention_period = 1      # ← 1 jour au lieu de 7 (économise ~$5/mois)
  backup_window           = "03:00-04:00"
  maintenance_window      = "sun:04:00-sun:05:00"

  skip_final_snapshot     = true
  deletion_protection     = false

  tags = {
    Name        = "Real Estate DB"
    Environment = var.environment
    Project     = var.project_name
  }
}