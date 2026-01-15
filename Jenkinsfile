pipeline {
    agent any

    environment {
        AWS_REGION = "eu-west-3"
        AWS_ACCOUNT_ID = "870613971200"
        ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        IMAGE_TAG = "latest"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: 'main']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/mohamediliasskaddar/RENTOPIA.git',
                        credentialsId: 'github-token',
                        name: 'origin'  // on renomme ops en origin ici
                    ]]
                ])
            }
        }


        stage('Login to ECR') {
            steps {
                sh '''
                aws ecr get-login-password --region $AWS_REGION \
                | docker login --username AWS --password-stdin $ECR_REGISTRY
                '''
            }
        }

        stage('Build & Push Services') {
            steps {
                script {

                    def services = [
                        "eureka-server",
                        "user-service",
                        "listing-service",
                        "booking-service",
                        "payment-service",
                        "messaging-service",
                        "notification-service",
                        "review-service",
                        "media-service",
                        "blockchain-service",
                        "ai-service",
                        "api-gateway",
                        "front-service"
                    ]

                    for (svc in services) {
                        sh """
                        echo "===== Building ${svc} ====="
                        docker build -t real-estate-dapp-${svc}-dev ./${svc}

                        docker tag real-estate-dapp-${svc}-dev:$IMAGE_TAG \
                        $ECR_REGISTRY/real-estate-dapp-${svc}-dev:$IMAGE_TAG

                        docker push $ECR_REGISTRY/real-estate-dapp-${svc}-dev:$IMAGE_TAG
                        """
                    }
                }
            }
        }

        // ===== OPTIONAL : Deploy to Kubernetes =====
//         stage('Deploy to EKS') {
//             steps {
//                 sh '''
//                 aws eks update-kubeconfig --name real-estate-dapp-eks-dev --region eu-west-3
//
//                 kubectl apply -f k8s/
//                 '''
//             }
//         }
    }

    post {
        success {
            echo "✅ Build & Push terminé avec succès !"
        }
        failure {
            echo "❌ Pipeline échouée"
        }
    }
}
