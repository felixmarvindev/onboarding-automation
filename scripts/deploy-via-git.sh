#!/bin/bash

# All-in-one script: Clone deployment files and deploy
# Usage: ./scripts/deploy-via-git.sh [repo-url] [branch] [registry] [username] [tag]
# Example: ./scripts/deploy-via-git.sh https://github.com/username/repo.git main docker.io myusername v1.0.0

set -e

REPO_URL=${1:-""}
BRANCH=${2:-main}
DOCKER_REGISTRY=${3:-docker.io}
DOCKER_USERNAME=${4:-yourusername}
IMAGE_TAG=${5:-latest}

if [ -z "$REPO_URL" ] || [ "$DOCKER_USERNAME" = "yourusername" ]; then
    echo "Error: Please provide required parameters"
    echo "Usage: $0 [repo-url] [branch] [registry] [username] [tag]"
    echo "Example: $0 https://github.com/username/repo.git main docker.io myusername v1.0.0"
    exit 1
fi

DEPLOY_DIR="onboarding-deployment"

echo "=== Cloning deployment files ==="
mkdir -p ${DEPLOY_DIR}
cd ${DEPLOY_DIR}

# Check if .git exists (already cloned)
if [ -d ".git" ]; then
    echo "Repository already cloned. Updating..."
    git sparse-checkout set \
        docker-compose.prod.yml \
        scripts/deploy-on-vps.sh \
        notification-service/.env
    git pull origin ${BRANCH}
else
    # Initialize git repository
    git init
    git remote add origin ${REPO_URL}
    
    # Enable sparse-checkout
    git sparse-checkout init --cone
    
    # Configure sparse-checkout to include only deployment files
    git sparse-checkout set \
        docker-compose.prod.yml \
        scripts/deploy-on-vps.sh \
        notification-service/.env
    
    # Pull only the specified files
    git pull origin ${BRANCH}
fi

echo ""
echo "=== Deploying services ==="

# Export environment variables for docker-compose
export DOCKER_REGISTRY=${DOCKER_REGISTRY}
export DOCKER_USERNAME=${DOCKER_USERNAME}
export IMAGE_TAG=${IMAGE_TAG}

# Make deploy script executable
chmod +x scripts/deploy-on-vps.sh

# Run deployment
./scripts/deploy-on-vps.sh ${DOCKER_REGISTRY} ${DOCKER_USERNAME} ${IMAGE_TAG}

echo ""
echo "=== Deployment complete! ==="
