# cloudfront.tf - CDN pour les images (biens + users)

# Identité d'accès pour que CloudFront accède aux buckets S3 privés
resource "aws_cloudfront_origin_access_identity" "oai_media" {
  comment = "OAI for rental media buckets"
}

# Distribution CloudFront principale (un seul pour les 2 buckets)
resource "aws_cloudfront_distribution" "media_cdn" {
  enabled             = true
  is_ipv6_enabled     = true
  default_root_object = ""  # Pas d'index par défaut

  # Origine 1 : Bucket des photos de propriétés
  origin {
    domain_name = aws_s3_bucket.tf_properties_images.bucket_regional_domain_name
    origin_id   = "S3-properties-origin"

    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.oai_media.cloudfront_access_identity_path
    }
  }

  # Origine 2 : Bucket des photos users
  origin {
    domain_name = aws_s3_bucket.tf_users_photos.bucket_regional_domain_name
    origin_id   = "S3-users-origin"

    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.oai_media.cloudfront_access_identity_path
    }
  }

  # Comportement par défaut : cache pour les GET/HEAD
  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD", "OPTIONS"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "S3-properties-origin"  # Par défaut, utilise properties (tu peux router par path si besoin)

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"  # Force HTTPS
    min_ttl                = 0
    default_ttl            = 3600  # Cache 1h par défaut
    max_ttl                = 86400  # Max 24h
    compress               = true  # Compresse les images auto
  }

  # Pas de restrictions géo (dispo mondial)
  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  # Certificat HTTPS gratuit par défaut
  viewer_certificate {
    cloudfront_default_certificate = true
  }

  # Tags pour matcher tes buckets
  tags = {
    Name        = "Rental Media CDN"
    Environment = var.environment
    Project     = var.project_name
  }
}

# Policy S3 pour autoriser UNIQUEMENT CloudFront VIA OAI
resource "aws_s3_bucket_policy" "allow_cloudfront_properties" {
  bucket = aws_s3_bucket.tf_properties_images.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowCloudFrontProperties"
        Effect = "Allow"
        Principal = {
          AWS = aws_cloudfront_origin_access_identity.oai_media.iam_arn  # ✅ Utilise l'ARN IAM de l'OAI
        }
        Action   = "s3:GetObject"
        Resource = "${aws_s3_bucket.tf_properties_images.arn}/*"
        # ❌ SUPPRIME la Condition (pas nécessaire avec OAI)
      }
    ]
  })
}

# Même chose pour le bucket users
resource "aws_s3_bucket_policy" "allow_cloudfront_users" {
  bucket = aws_s3_bucket.tf_users_photos.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowCloudFrontUsers"
        Effect = "Allow"
        Principal = {
          AWS = aws_cloudfront_origin_access_identity.oai_media.iam_arn  # ✅ Même OAI
        }
        Action   = "s3:GetObject"
        Resource = "${aws_s3_bucket.tf_users_photos.arn}/*"
      }
    ]
  })
}