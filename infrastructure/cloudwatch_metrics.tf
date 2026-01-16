# cloudwatch_metrics.tf - Métriques et Dashboard

# Dashboard principal pour monitoring
resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${var.project_name}-${var.environment}-dashboard"
  
  dashboard_body = jsonencode({
    widgets = [
      # Widget 1 : EKS Cluster
      {
        type = "metric"
        x = 0
        y = 0
        width = 12
        height = 6
        properties = {
          metrics = [
            ["ContainerInsights", "node_cpu_utilization", "ClusterName", aws_eks_cluster.main.name, { "label": "CPU %" }],
            ["ContainerInsights", "node_memory_utilization", "ClusterName", aws_eks_cluster.main.name, { "label": "Memory %" }]
          ]
          view = "timeSeries"
          stacked = false
          region = var.aws_region
          title = "EKS Cluster - CPU & Memory"
          period = 300
          stat = "Average"
        }
      },
      
      # Widget 2 : RDS Database
      {
        type = "metric"
        x = 0
        y = 6
        width = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/RDS", "CPUUtilization", "DBInstanceIdentifier", aws_db_instance.tf_real_estate_db.identifier, { "label": "CPU %" }],
            ["AWS/RDS", "DatabaseConnections", "DBInstanceIdentifier", aws_db_instance.tf_real_estate_db.identifier, { "label": "Connections" }],
            ["AWS/RDS", "FreeStorageSpace", "DBInstanceIdentifier", aws_db_instance.tf_real_estate_db.identifier, { "label": "Free Storage (MB)", "yAxis": "right" }]
          ]
          view = "timeSeries"
          stacked = false
          region = var.aws_region
          title = "RDS MySQL Database"
          period = 300
          stat = "Average"
        }
      },
      
      # Widget 3 : ALB Metrics
      {
        type = "metric"
        x = 12
        y = 0
        width = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/ApplicationELB", "HTTPCode_Target_2XX_Count", "LoadBalancer", aws_lb.main.arn_suffix, { "label": "2XX Success" }],
            ["AWS/ApplicationELB", "HTTPCode_Target_4XX_Count", "LoadBalancer", aws_lb.main.arn_suffix, { "label": "4XX Client Errors" }],
            ["AWS/ApplicationELB", "HTTPCode_Target_5XX_Count", "LoadBalancer", aws_lb.main.arn_suffix, { "label": "5XX Server Errors" }],
            ["AWS/ApplicationELB", "TargetResponseTime", "LoadBalancer", aws_lb.main.arn_suffix, { "label": "Response Time (ms)", "yAxis": "right" }]
          ]
          view = "timeSeries"
          stacked = false
          region = var.aws_region
          title = "ALB - HTTP Status & Latency"
          period = 300
          stat = "Sum"
        }
      }
    ]
  })
}

# Alerte pour erreurs 5XX sur ALB
resource "aws_cloudwatch_metric_alarm" "alb_5xx_errors" {
  alarm_name          = "${var.project_name}-alb-5xx-errors-${var.environment}"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "1"
  metric_name         = "HTTPCode_Target_5XX_Count"
  namespace           = "AWS/ApplicationELB"
  period              = "300"
  statistic           = "Sum"
  threshold           = "10"
  alarm_description   = "ALB returning too many 5XX errors"
  treat_missing_data  = "notBreaching"
  
  dimensions = {
    LoadBalancer = aws_lb.main.arn_suffix
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
    Service     = "alb"
  }
}

# Alerte pour RDS CPU élevé
resource "aws_cloudwatch_metric_alarm" "rds_high_cpu" {
  alarm_name          = "${var.project_name}-rds-high-cpu-${var.environment}"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "RDS CPU utilization is high"
  
  dimensions = {
    DBInstanceIdentifier = aws_db_instance.tf_real_estate_db.identifier
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
    Service     = "rds"
  }
}