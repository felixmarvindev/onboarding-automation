# Quick VPS Setup Guide

## Problem Solved

This setup addresses:
1. ✅ Reuses existing PostgreSQL database (no new postgres container)
2. ✅ Unique container names (`onboarding-prod-*`) to avoid conflicts
3. ✅ Configurable database connection

## Quick Start

### 1. Stop existing conflicting containers (if any)

```bash
# Check what's running
docker ps

# Stop conflicting containers if needed
docker stop notification-service  # or other conflicting containers
docker rm notification-service    # remove if you want to reuse the name
```

### 2. Configure Database Connection

**For postgres on host machine:**
```bash
export POSTGRES_HOST=host.docker.internal
export POSTGRES_PORT=5432
export POSTGRES_USERNAME=onboarding_user
export POSTGRES_PASSWORD=onboarding_pass
```

**For postgres in existing container:**
```bash
# First, find your postgres container
docker ps | grep postgres

# Connect it to the network (if not already)
docker network create onboarding-automation_onboarding-network 2>/dev/null || true
docker network connect onboarding-automation_onboarding-network your-postgres-container-name

# Use container name
export POSTGRES_HOST=your-postgres-container-name
export POSTGRES_PORT=5432
export POSTGRES_USERNAME=onboarding_user
export POSTGRES_PASSWORD=onboarding_pass
```

### 3. Set Other Environment Variables

```bash
export DOCKER_REGISTRY=docker.io
export DOCKER_USERNAME=yourusername
export IMAGE_TAG=v1.0.0
```

### 4. Deploy

```bash
docker login
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

## Container Names

All containers now use unique names:
- `onboarding-prod-api`
- `onboarding-prod-kyc`
- `onboarding-prod-identity`
- `onboarding-prod-provisioning`
- `onboarding-prod-notification`
- `onboarding-prod-completion`
- `onboarding-prod-failure-handler`
- `onboarding-prod-rabbitmq`

No more conflicts!

## Troubleshooting

### Port 5432 already in use

That's expected! We're using an existing postgres, so port 5432 is already taken. The docker-compose file no longer tries to create a postgres container.

### Container name conflicts

All containers now have unique names. If you still see conflicts:
```bash
docker-compose -f docker-compose.prod.yml down
docker ps -a | grep onboarding
# Remove old containers if needed
docker rm old-container-name
```

### Cannot connect to postgres

**Check 1: Is postgres running?**
```bash
docker ps | grep postgres
# or
systemctl status postgresql
```

**Check 2: Can you connect from host?**
```bash
psql -h localhost -U onboarding_user -d onboarding_db
```

**Check 3: If postgres is in a container, connect it to the network:**
```bash
# Find the network name
docker network ls | grep onboarding

# Connect your postgres container
docker network connect onboarding-automation_onboarding-network your-postgres-container-name
```

**Check 4: Use host IP instead of host.docker.internal (Linux)**
```bash
# Get host IP
hostname -I | awk '{print $1}'

# Use it
export POSTGRES_HOST=172.17.0.1  # or your host IP
```

### Services can't find postgres

Make sure:
1. POSTGRES_HOST is set correctly
2. If using container name, that container is on the same network
3. If using host postgres, use `host.docker.internal` (or host IP on Linux)

The docker-compose file includes `extra_hosts: - "host.docker.internal:host-gateway"` for services that need postgres, which should work on Linux.
