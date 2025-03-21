#!/usr/bin/env bash
set -e

# Build JSON service
echo "Building JSON service image..."
docker buildx build \
  --target builder \
  --load \
  --cache-from type=gha \
  --cache-to type=gha,mode=max \
  -t json-service:latest \
  ./ingestion-connector-tck/app

# Build API service
echo "Building API service image..."
docker buildx build \
  --target builder \
  --load \
  --cache-from type=gha \
  --cache-to type=gha,mode=max \
  -t api-service:latest \
  -f ./ingestion-connector-tck/DockerfilePrism \
  ./ingestion-connector-tck

# Start services
echo "Starting services..."
docker compose -f ./ingestion-connector-tck/compose.yaml up -d

# Wait for Prism
echo "Waiting for Prism..."
timeout=30
until curl -s http://localhost:4010/health > /dev/null || [ $timeout -eq 0 ]; do
  echo "Waiting for Prism to start..."
  sleep 1
  ((timeout--))
done

if [ $timeout -eq 0 ]; then
  echo "Prism failed to start"
  docker compose -f ./ingestion-connector-tck/compose.yaml logs
  exit 1
fi
