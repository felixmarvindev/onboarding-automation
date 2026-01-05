#!/bin/bash

# Script to clone only deployment files from git repository
# Usage: ./scripts/clone-deployment-files.sh [repository-url] [branch]
# Example: ./scripts/clone-deployment-files.sh https://github.com/username/repo.git main

set -e

REPO_URL=${1:-""}
BRANCH=${2:-main}
DEPLOY_DIR="onboarding-deployment"

if [ -z "$REPO_URL" ]; then
    echo "Error: Please provide repository URL"
    echo "Usage: $0 [repository-url] [branch]"
    echo "Example: $0 https://github.com/username/repo.git main"
    exit 1
fi

echo "Cloning deployment files from ${REPO_URL} (branch: ${BRANCH})"

# Create deployment directory
mkdir -p ${DEPLOY_DIR}
cd ${DEPLOY_DIR}

# Initialize git repository
git init

# Add remote
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

echo ""
echo "=== Deployment files cloned successfully! ==="
echo ""
echo "Files cloned to: $(pwd)"
echo ""
echo "To deploy, run:"
echo "  cd ${DEPLOY_DIR}"
echo "  ./scripts/deploy-on-vps.sh [registry] [username] [tag]"
