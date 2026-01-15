# jenkins.tf - CORRIGÉ

resource "aws_instance" "jenkins" {
  ami           = "ami-0ef9bcd5dfb57b968"
  instance_type = "t3.micro"
  
  subnet_id              = aws_subnet.subnet_public[0].id
  vpc_security_group_ids = [aws_security_group.tf_jenkins.id]  # ← CHANGÉ !
  
  key_name = "jenkins-key"
  iam_instance_profile = aws_iam_instance_profile.tf_jenkins_profile.name
  
  root_block_device {
    volume_size = 8
    volume_type = "gp3"
    delete_on_termination = true
  }
  
  user_data = <<-EOF
              #!/bin/bash
              sudo snap install amazon-ssm-agent --classic
              sudo systemctl start snap.amazon-ssm-agent.amazon-ssm-agent
              sudo systemctl enable snap.amazon-ssm-agent.amazon-ssm-agent
              
              sudo apt update -y
              sudo apt upgrade -y
              
              sudo useradd -m -s /bin/bash devops
              echo "devops:ChangeThisPassword123!" | sudo chpasswd
              sudo usermod -aG sudo devops
              
              sudo mkdir -p /opt/jenkins
              sudo chown -R ubuntu:ubuntu /opt/jenkins
              EOF
  
  tags = {
    Name        = "${var.project_name}-jenkins-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "terraform"
    CostCenter  = "free-tier"
  }
}

# SUPPRIMER le Security Group jenkins_sg de ce fichier !
# Il est déjà défini dans security_group.tf