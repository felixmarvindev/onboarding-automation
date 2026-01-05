#!/bin/bash

# Script to build and push Docker images to a registry
# Usage: ./scripts/build-and-push.sh [registry] [username] [tag]
# Example: ./scripts/build-and-push.sh docker.io myusername v1.0.0

set -e

# Default values
DOCKER_REGISTRY=${1:-docker.io}
DOCKER_USERNAME=${2:-yourusername}
IMAGE_TAG=${3:-latest}

if [ "$DOCKER_USERNAME" = "yourusername" ]; then
    echo "Error: Please provide your Docker username"
    echo "Usage: $0 [registry] [username] [tag]"
    echo "Example: $0 docker.io myusername v1.0.0"
    exit 1
fi

echo "Building and pushing images to ${DOCKER_REGISTRY}/${DOCKER_USERNAME} with tag ${IMAGE_TAG}"

# Services to build
SERVICES=(
    "onboarding-api"
    "kyc-service"
    "identity-service"
    "provisioning-service"
    "notification-service"
    "completion-service"
    "failure-handler"
)

# Login to Docker registry (if not docker.io)
if [ "$DOCKER_REGISTRY" != "docker.io" ]; then
    echo "Logging into ${DOCKER_REGISTRY}..."
    docker login ${DOCKER_REGISTRY}
else
    echo "Using Docker Hub (docker.io). Make sure you're logged in:"
    echo "Run: docker login"
    read -p "Press Enter to continue..."
fi

# Build and push each service
for SERVICE in "${SERVICES[@]}"; do
    echo ""
    echo "=== Building ${SERVICE} ==="
    
    # Build the image
    IMAGE_NAME="${DOCKER_REGISTRY}/${DOCKER_USERNAME}/${SERVICE}:${IMAGE_TAG}"
    
    docker build \
        -t ${IMAGE_NAME} \
        -t ${DOCKER_REGISTRY}/${DOCKER_USERNAME}/${SERVICE}:latest \
        -f ${SERVICE}/Dockerfile \
        .
    
    echo "Pushing ${IMAGE_NAME}..."
    docker push ${IMAGE_NAME}
    docker push ${DOCKER_REGISTRY}/${DOCKER_USERNAME}/${SERVICE}:latest
    
    echo "✓ ${SERVICE} pushed successfully"
done

echo ""
echo "=== All images built and pushed successfully! ==="
echo ""
echo "To deploy on VPS, use:"
echo "  export DOCKER_REGISTRY=${DOCKER_REGISTRY}"
echo "  export DOCKER_USERNAME=${DOCKER_USERNAME}"
echo "  export IMAGE_TAG=${IMAGE_TAG}"
echo "  docker-compose -f docker-compose.prod.yml pull"
echo "  docker-compose -f docker-compose.prod.yml up -d"
