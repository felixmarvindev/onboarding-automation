# Deployment Guide

This guide explains how to build Docker images locally and deploy them on a VPS without transferring the source code.

## Overview

Instead of copying the entire codebase to your VPS, you can:
1. Build Docker images locally (or in CI/CD)
2. Push images to a Docker registry (Docker Hub, GitHub Container Registry, etc.)
3. Pull and run images on your VPS

This approach transfers only the built images (typically 100-300MB each) instead of the entire source code.

## Prerequisites

- Docker installed locally and on VPS
- Docker Compose installed on VPS
- Account on a Docker registry (Docker Hub, GitHub Container Registry, etc.)

## Quick Start

### Option 1: Using Docker Hub (Recommended for beginners)

1. **Build and push images locally:**
   ```bash
   # Make script executable
   chmod +x scripts/build-and-push.sh
   
   # Build and push (replace 'yourusername' with your Docker Hub username)
   ./scripts/build-and-push.sh docker.io yourusername v1.0.0
   ```

2. **On your VPS, deploy:**
   ```bash
   # Make script executable
   chmod +x scripts/deploy-on-vps.sh
   
   # Pull and deploy (replace 'yourusername' with your Docker Hub username)
   ./scripts/deploy-on-vps.sh docker.io yourusername v1.0.0
   ```

### Option 2: Manual Steps

#### Step 1: Build and Push Images Locally

1. **Login to Docker Hub:**
   ```bash
   docker login
   ```

2. **Build and tag each service:**
   ```bash
   # Replace 'yourusername' with your Docker Hub username
   DOCKER_USERNAME=yourusername
   IMAGE_TAG=v1.0.0
   
   # Build each service
   docker build -t ${DOCKER_USERNAME}/onboarding-api:${IMAGE_TAG} -t ${DOCKER_USERNAME}/onboarding-api:latest -f onboarding-api/Dockerfile .
   docker build -t ${DOCKER_USERNAME}/kyc-service:${IMAGE_TAG} -t ${DOCKER_USERNAME}/kyc-service:latest -f kyc-service/Dockerfile .
   docker build -t ${DOCKER_USERNAME}/identity-service:${IMAGE_TAG} -t ${DOCKER_USERNAME}/identity-service:latest -f identity-service/Dockerfile .
   docker build -t ${DOCKER_USERNAME}/provisioning-service:${IMAGE_TAG} -t ${DOCKER_USERNAME}/provisioning-service:latest -f provisioning-service/Dockerfile .
   docker build -t ${DOCKER_USERNAME}/notification-service:${IMAGE_TAG} -t ${DOCKER_USERNAME}/notification-service:latest -f notification-service/Dockerfile .
   docker build -t ${DOCKER_USERNAME}/completion-service:${IMAGE_TAG} -t ${DOCKER_USERNAME}/completion-service:latest -f completion-service/Dockerfile .
   docker build -t ${DOCKER_USERNAME}/failure-handler:${IMAGE_TAG} -t ${DOCKER_USERNAME}/failure-handler:latest -f failure-handler/Dockerfile .
   ```

3. **Push images to Docker Hub:**
   ```bash
   docker push ${DOCKER_USERNAME}/onboarding-api:${IMAGE_TAG}
   docker push ${DOCKER_USERNAME}/onboarding-api:latest
   docker push ${DOCKER_USERNAME}/kyc-service:${IMAGE_TAG}
   docker push ${DOCKER_USERNAME}/kyc-service:latest
   docker push ${DOCKER_USERNAME}/identity-service:${IMAGE_TAG}
   docker push ${DOCKER_USERNAME}/identity-service:latest
   docker push ${DOCKER_USERNAME}/provisioning-service:${IMAGE_TAG}
   docker push ${DOCKER_USERNAME}/provisioning-service:latest
   docker push ${DOCKER_USERNAME}/notification-service:${IMAGE_TAG}
   docker push ${DOCKER_USERNAME}/notification-service:latest
   docker push ${DOCKER_USERNAME}/completion-service:${IMAGE_TAG}
   docker push ${DOCKER_USERNAME}/completion-service:latest
   docker push ${DOCKER_USERNAME}/failure-handler:${IMAGE_TAG}
   docker push ${DOCKER_USERNAME}/failure-handler:latest
   ```

#### Step 2: Deploy on VPS

1. **Copy only necessary files to VPS:**
   ```bash
   # On your local machine, copy only deployment files
   scp docker-compose.prod.yml user@your-vps:/path/to/deployment/
   scp scripts/deploy-on-vps.sh user@your-vps:/path/to/deployment/
   scp -r notification-service/.env user@your-vps:/path/to/deployment/notification-service/  # if exists
   ```

2. **On VPS, set environment variables:**
   ```bash
   export DOCKER_REGISTRY=docker.io
   export DOCKER_USERNAME=yourusername
   export IMAGE_TAG=v1.0.0
   ```

3. **Login to Docker Hub on VPS:**
   ```bash
   docker login
   ```

4. **Pull and start services:**
   ```bash
   docker-compose -f docker-compose.prod.yml pull
   docker-compose -f docker-compose.prod.yml up -d
   ```

5. **Check status:**
   ```bash
   docker-compose -f docker-compose.prod.yml ps
   docker-compose -f docker-compose.prod.yml logs -f
   ```

## Using GitHub Container Registry (ghcr.io)

If you prefer using GitHub Container Registry:

1. **Build and push:**
   ```bash
   # Login to GitHub Container Registry
   echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin
   
   # Build and push
   ./scripts/build-and-push.sh ghcr.io yourusername v1.0.0
   ```

2. **On VPS:**
   ```bash
   # Login
   echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin
   
   # Deploy
   ./scripts/deploy-on-vps.sh ghcr.io yourusername v1.0.0
   ```

## Using Private Registry

For a private registry (e.g., AWS ECR, Azure Container Registry):

1. **Login to your registry:**
   ```bash
   docker login your-registry.example.com
   ```

2. **Build and push:**
   ```bash
   ./scripts/build-and-push.sh your-registry.example.com yourusername v1.0.0
   ```

3. **On VPS:**
   ```bash
   docker login your-registry.example.com
   ./scripts/deploy-on-vps.sh your-registry.example.com yourusername v1.0.0
   ```

## Updating Services

To update services on VPS:

```bash
# Pull latest images
docker-compose -f docker-compose.prod.yml pull

# Restart services with new images
docker-compose -f docker-compose.prod.yml up -d

# Or for a specific service
docker-compose -f docker-compose.prod.yml up -d --no-deps onboarding-api
```

## Files Needed on VPS

You only need these files on your VPS:
- `docker-compose.prod.yml` - Production docker-compose configuration
- `scripts/deploy-on-vps.sh` - Deployment script (optional)
- `notification-service/.env` - Environment variables for notification service (if exists)

**You do NOT need:**
- Source code (.java files)
- Dockerfiles
- pom.xml files
- Any other source files

## Image Size Optimization

The built images contain:
- JRE (Java Runtime Environment)
- Your compiled JAR files
- Application dependencies

Typical image sizes: 150-300MB per service.

## CI/CD Integration

You can integrate this into your CI/CD pipeline (Jenkins, GitHub Actions, etc.):

1. Build images in CI/CD
2. Push to registry
3. On VPS, pull and deploy

See `Jenkinsfile` for an example of building images in CI/CD (add push steps).

## Troubleshooting

### Images not found
- Verify you're logged into the registry
- Check image names and tags match
- Ensure images were pushed successfully

### Permission denied
- Make sure scripts are executable: `chmod +x scripts/*.sh`
- Check Docker permissions on VPS

### Pull fails
- Check network connectivity
- Verify registry credentials
- Check if registry allows anonymous pulls (if applicable)
