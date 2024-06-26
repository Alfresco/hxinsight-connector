#!/usr/bin/env bash

export DOCKER_COMPOSE_PATH=$1

if [ -z "$DOCKER_COMPOSE_PATH" ]
then
  echo "Please provide directory path of docker-compose.yml: \"${0##*/} /path/to/docker-compose.yml\""
  exit 1
fi

echo "Killing ACS stack in ${DOCKER_COMPOSE_PATH}"

cd "$(dirname "${DOCKER_COMPOSE_PATH}")"

docker-compose ps
# logs for debug
docker-compose logs --no-color -t alfresco live-ingester transform-router transform-core-aio
docker-compose -f "$(basename "${DOCKER_COMPOSE_PATH}")" kill
docker-compose rm -fv
