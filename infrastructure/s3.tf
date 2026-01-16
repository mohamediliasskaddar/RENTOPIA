# s3.tf - Stockage des images (biens + utilisateurs)

# Bucket 1 : Images des propriétés
resource "aws_s3_bucket" "tf_properties_images" {
    bucket = "${var.project_name}-properties-images-${var.environment}"  

    tags = {
    Name        = "Properties Images"
    Environment = var.environment
    Project     = var.project_name
  }
}

# Bucket 2 : Photos de profil utilisateurs
resource "aws_s3_bucket" "tf_users_photos" {
    bucket = "${var.project_name}-users-photos-${var.environment}"

    tags = {
    Name        = "Users Photos"
    Environment = var.environment
    Project     = var.project_name
  }
}

# SÉCURITÉ : BLOQUER TOUT ACCÈS PUBLIC
resource "aws_s3_bucket_public_access_block" "tf_block_public" {
  for_each = {
    properties = aws_s3_bucket.tf_properties_images.id
    users      = aws_s3_bucket.tf_users_photos.id
  }

  bucket                  = each.value
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# VERSIONING : activer pour recuperer une image supprimee par erreur
resource "aws_s3_bucket_versioning" "tf_versioning" {
  for_each = {
    properties = aws_s3_bucket.tf_properties_images.id
    #users      = aws_s3_bucket.tf_users_photos.id
  }

  bucket = each.value
  versioning_configuration {
    status = "Enabled"
  }
}

# chiffrement automatique des objects dans les buckets
resource "aws_s3_bucket_server_side_encryption_configuration" "tf_encryption" {
  for_each = {
    properties = aws_s3_bucket.tf_properties_images.id
    users      = aws_s3_bucket.tf_users_photos.id
  }

  bucket = each.value

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# cycle de vie : supprimer les photos des biens après 90 jrs de stockage 
resource "aws_s3_bucket_lifecycle_configuration" "tf_lifecycle_properties" {
  bucket = aws_s3_bucket.tf_properties_images.id

  rule {
    id     = "delete-properties-after-90-days"
    status = "Enabled"

    # ✅ AJOUTEZ CE FILTER (OBLIGATOIRE)
    filter {
      prefix = "properties/"  # S'applique au dossier properties/
    }

    expiration {
      days = 90
    }
  }
}