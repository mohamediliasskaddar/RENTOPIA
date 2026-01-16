#!/bin/bash

USERNAME=mohamediliasskaddar
IMAGES=(
  jee-project-ai-service
  jee-project-blockchain-service
  jee-project-payment-service
  jee-project-user-service
  jee-project-api-gateway
  jee-project-review-service
  jee-project-listing-service
  jee-project-booking-service
  jee-project-notification-service
  jee-project-messaging-service
  jee-project-mysql-db
  jee-project-eureka-server
  jee-project-front-service
)

for img in "${IMAGES[@]}"; do
  echo "Tagging $img..."
  docker tag $img $USERNAME/$img:latest
  echo "Pushing $img..."
  docker push $USERNAME/$img:latest
done
