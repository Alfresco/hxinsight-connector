#!/usr/bin/env bash

export DOCKER_COMPOSE_PATH="${1}"

if [ -z "${DOCKER_COMPOSE_PATH}" ]; then
  echo "Please provide a docker-compose.yml filepath"
  exit 1
fi

echo "Starting ACS stack with ${DOCKER_COMPOSE_PATH}"

cd "$(dirname "${DOCKER_COMPOSE_PATH}")"

docker-compose -f "$(basename "${DOCKER_COMPOSE_PATH}")" up -d

if [ $? -eq 0 ]; then
  echo "Docker Compose started ok"
else
  echo "Docker Compose failed to start" >&2
  exit 1
fi

WAIT_INTERVAL=1
COUNTER=0
TIMEOUT=300
t0=$(date +%s)

echo "Waiting for alfresco to start"
until curl --output /dev/null --silent --head --fail http://localhost:8080/alfresco || [ "$COUNTER" -eq "$TIMEOUT" ]; do
   printf '.'
   sleep ${WAIT_INTERVAL}
   COUNTER=$((COUNTER + WAIT_INTERVAL))
done

if (("$COUNTER" < "$TIMEOUT")) ; then
   t1=$(date +%s)
   delta=$(((t1 - t0)/60))
   echo "Alfresco Started in $delta minutes"
else
   echo "Waited $COUNTER seconds"
   echo "Alfresco Could not start in time."
   echo "START of Alfresco service logs for investigation"
   docker-compose logs --tail="all" alfresco
   echo "END of Alfresco service logs for investigation"
   exit 1
fi
