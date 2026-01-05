# VPS Deployment Guide (Using Git Sparse-Checkout)

This guide shows how to deploy on your VPS using git sparse-checkout to only download the deployment files (no source code).

## Prerequisites

On your VPS, you need:
- Git (version 2.25+ for sparse-checkout)
- Docker
- Docker Compose

## Quick Deployment

### Step 1: Clone Only Deployment Files

On your VPS, run:

```bash
# Clone repository with sparse-checkout
git clone --filter=blob:none --sparse https://github.com/yourusername/onboarding-automation.git
cd onboarding-automation

# Configure sparse-checkout to only get deployment files
git sparse-checkout set docker-compose.prod.yml scripts/deploy-on-vps.sh notification-service/.env
```

**That's it!** Now you only have these files:
- `docker-compose.prod.yml`
- `scripts/deploy-on-vps.sh`
- `notification-service/.env` (if it exists in the repo)

No source code (.java files), no Dockerfiles, no pom.xml files - just what you need to deploy.

### Step 2: Set Environment Variables

```bash
export DOCKER_REGISTRY=docker.io
export DOCKER_USERNAME=yourusername
export IMAGE_TAG=v1.0.0
```

Replace:
- `yourusername` with your Docker Hub username
- `v1.0.0` with your desired image tag

### Step 3: Login to Docker Registry

```bash
docker login
```

### Step 4: Deploy

```bash
# Pull images and start services
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps
```

## Updating Deployment Files

If deployment files change in the repository:

```bash
cd onboarding-automation
git pull origin main
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

## Updating Services (New Images)

When you push new images to the registry:

```bash
cd onboarding-automation
export IMAGE_TAG=v1.0.1  # New tag
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

## Using Different Registry

For GitHub Container Registry:

```bash
export DOCKER_REGISTRY=ghcr.io
export DOCKER_USERNAME=yourusername
export IMAGE_TAG=v1.0.0

# Login to GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Deploy
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

## Verify What Files Were Cloned

To see what files you actually have:

```bash
cd onboarding-automation
find . -type f -not -path './.git/*'
```

You should only see:
- `docker-compose.prod.yml`
- `scripts/deploy-on-vps.sh`
- `notification-service/.env` (if exists)

No source code, no Dockerfiles, no pom.xml files!

## Troubleshooting

### Git version too old

If you get errors about sparse-checkout, make sure you have Git 2.25+:

```bash
git --version
```

### Files not showing up

If files don't appear after sparse-checkout:

```bash
git sparse-checkout list  # Check what's configured
git sparse-checkout set docker-compose.prod.yml scripts/deploy-on-vps.sh notification-service/.env
git read-tree -mu HEAD    # Refresh the working directory
```

### Docker login issues

Make sure you're logged in:

```bash
docker login
# Or for GitHub Container Registry:
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin
```

## Alternative: Manual File Copy

If you prefer not to use git on VPS:

1. On your local machine, copy files:
   ```bash
   scp docker-compose.prod.yml user@vps:/path/to/deployment/
   scp scripts/deploy-on-vps.sh user@vps:/path/to/deployment/
   scp -r notification-service/.env user@vps:/path/to/deployment/notification-service/
   ```

2. On VPS, deploy:
   ```bash
   cd /path/to/deployment
   chmod +x scripts/deploy-on-vps.sh
   ./scripts/deploy-on-vps.sh docker.io yourusername v1.0.0
   ```

But using git sparse-checkout is better because:
- Easy to update: just `git pull`
- Keeps files in sync with repository
- No manual file copying needed
