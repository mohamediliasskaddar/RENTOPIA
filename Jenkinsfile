pipeline {
  agent any

  environment {
    DOCKERHUB_CREDS = credentials('docker-hub-creds')
    GITHUB_CREDS = credentials('github-credentials')
  }

  stages {

    stage('Checkout') {
      steps {
        git branch: 'main',
            url: 'https://github.com/mohamediliasskaddar/RENTOPIA.git',
            credentialsId: 'github-credentials'
      }
    }

    stage('DockerHub Login') {
      steps {
        sh 'echo $DOCKERHUB_CREDS_PSW | docker login -u $DOCKERHUB_CREDS_USR --password-stdin'
      }
    }

    stage('Build & Push Docker Images') {
      steps {
        sh '''
        services=(
          user-service listing-service booking-service payment-service
          messaging-service notification-service review-service
          media-service blockchain-service ai-service
          api-gateway eureka-server front-service
        )

        for service in "${services[@]}"; do
          echo "Building $service..."
          docker build -t $DOCKERHUB_CREDS_USR/$service:latest ./$service
          docker push $DOCKERHUB_CREDS_USR/$service:latest
        done
        '''
      }
    }

    // Optionnel : tu peux activer cette étape plus tard pour EKS
//     stage('Deploy to EKS') {
//       when {
//         expression { return false } // Désactivé pour tests locaux
//       }
//       steps {
//         sh '''
//         export KUBECONFIG=$KUBE_CONFIG
//         kubectl apply -f k8s/
//         '''
//       }
//     }
  }
}
