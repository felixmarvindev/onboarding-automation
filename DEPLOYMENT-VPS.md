# VPS Deployment Guide (Using Git Sparse-Checkout)

This guide shows how to deploy on your VPS using git sparse-checkout to only download the deployment files (no source code).

## Prerequisites

On your VPS, you need:
- Git (version 2.25+ for sparse-checkout)
- Docker
- Docker Compose
- Existing PostgreSQL database (or set up one)

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

### Step 2: Configure Database Connection

The production docker-compose file uses an existing PostgreSQL database. Configure how to connect:

**Option A: Postgres on host machine (default)**
```bash
export POSTGRES_HOST=host.docker.internal
export POSTGRES_PORT=5432
export POSTGRES_USERNAME=onboarding_user
export POSTGRES_PASSWORD=onboarding_pass
```

**Option B: Postgres in existing container**
```bash
# Find your existing postgres container name
docker ps | grep postgres

# Use that container name
export POSTGRES_HOST=your-existing-postgres-container-name
export POSTGRES_PORT=5432
export POSTGRES_USERNAME=onboarding_user
export POSTGRES_PASSWORD=onboarding_pass
```

**Option C: Remote Postgres**
```bash
export POSTGRES_HOST=your-postgres-host-or-ip
export POSTGRES_PORT=5432
export POSTGRES_USERNAME=onboarding_user
export POSTGRES_PASSWORD=onboarding_pass
```

**Note:** If postgres is in a different Docker network, you may need to:
1. Connect postgres container to `onboarding-network`, or
2. Use host networking mode, or
3. Use the host's IP address instead of container name

### Step 3: Set Environment Variables

```bash
export DOCKER_REGISTRY=docker.io
export DOCKER_USERNAME=yourusername
export IMAGE_TAG=v1.0.0
```

Replace:
- `yourusername` with your Docker Hub username
- `v1.0.0` with your desired image tag

### Step 4: Login to Docker Registry

```bash
docker login
```

### Step 5: Deploy

```bash
# Pull images and start services
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps
```

## Container Names

All containers use unique names with the `onboarding-prod-` prefix:
- `onboarding-prod-api`
- `onboarding-prod-kyc`
- `onboarding-prod-identity`
- `onboarding-prod-provisioning`
- `onboarding-prod-notification`
- `onboarding-prod-completion`
- `onboarding-prod-failure-handler`
- `onboarding-prod-rabbitmq`

This prevents conflicts with other deployments.

## Connecting to Existing Postgres Container

If your postgres is in a different Docker network, connect it:

```bash
# Find your postgres container
docker ps | grep postgres

# Connect it to the onboarding network
docker network connect onboarding-automation_onboarding-network your-postgres-container-name

# Then use the container name as POSTGRES_HOST
export POSTGRES_HOST=your-postgres-container-name
```

Alternatively, if both are on the same host, use `host.docker.internal` (Linux) or the host's IP address.

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

### Postgres connection issues

**Problem: Cannot connect to postgres**

Check:
1. Postgres is running: `docker ps | grep postgres` or `systemctl status postgresql`
2. Postgres is accessible from host: `psql -h localhost -U onboarding_user -d onboarding_db`
3. Network connectivity:
   - For host postgres: Use `host.docker.internal` (add `--add-host=host.docker.internal:host-gateway` to docker-compose if needed on Linux)
   - For container postgres: Ensure it's on the same network or connected to `onboarding-network`
   - For remote postgres: Verify firewall rules and network access

**Linux: host.docker.internal not available**

On Linux, add to docker-compose services that need postgres:
```yaml
extra_hosts:
  - "host.docker.internal:host-gateway"
```

Or use the host's IP address instead:
```bash
export POSTGRES_HOST=$(hostname -I | awk '{print $1}')
```

### Port conflicts

If ports are already in use, you can:
1. Stop conflicting containers
2. Change ports in docker-compose.prod.yml (e.g., `8080:8080` → `8081:8080`)

### Container name conflicts

All containers now use unique `onboarding-prod-` prefix. If you still have conflicts:
1. Stop and remove old containers: `docker-compose -f docker-compose.prod.yml down`
2. Remove containers manually: `docker rm container-name`

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
