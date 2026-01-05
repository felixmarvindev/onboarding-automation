#!/bin/bash

# Script to deploy on VPS by pulling pre-built images
# Usage: ./scripts/deploy-on-vps.sh [registry] [username] [tag]
# Example: ./scripts/deploy-on-vps.sh docker.io myusername v1.0.0

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

# Export environment variables for docker-compose
export DOCKER_REGISTRY=${DOCKER_REGISTRY}
export DOCKER_USERNAME=${DOCKER_USERNAME}
export IMAGE_TAG=${IMAGE_TAG}

echo "Pulling images from ${DOCKER_REGISTRY}/${DOCKER_USERNAME} with tag ${IMAGE_TAG}"

# Login to Docker registry if needed
echo "Make sure you're logged into the Docker registry:"
echo "  docker login ${DOCKER_REGISTRY}"
read -p "Press Enter to continue..."

# Pull all images
echo "Pulling images..."
docker-compose -f docker-compose.prod.yml pull

# Start services
echo "Starting services..."
docker-compose -f docker-compose.prod.yml up -d

# Show status
echo ""
echo "=== Deployment complete! ==="
docker-compose -f docker-compose.prod.yml ps

echo ""
echo "To view logs: docker-compose -f docker-compose.prod.yml logs -f"
echo "To stop services: docker-compose -f docker-compose.prod.yml down"
