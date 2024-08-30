#!/usr/bin/env bash

set -eu

usage() {
    echo "Create development docker images." 1>&2;
    echo "Usage: $0 [-p] [-t <tag>] [-h]" 1>&2;
    echo "  -p: Build multiarch images and push them (requires GitHub Action variables to be set)"
    echo "  -t <tag>: Override the default docker tag" 1>&2;
    echo "  -h: Display this help" 1>&2;
    exit 1;
}

PUSH_IMAGE="false"
DOCKER_TAG=""
while getopts ":hpt:" arg; do
  case $arg in
    p)
      PUSH_IMAGE="true"
      ;;
    t)
      DOCKER_TAG="${OPTARG}"
      ;;
    h | *) # Display help.
      usage
      exit 0
      ;;
  esac
done

PROJECT_VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)

for DOCKER_BUILD_DIR in $(find . -name Dockerfile -exec dirname {} \;)
do
  if [[ "${DOCKER_BUILD_DIR}" != *"/classes/"* ]]
  then
    continue
  fi

  cd "${DOCKER_BUILD_DIR}"
  echo "Build docker image in: ${DOCKER_BUILD_DIR}"
  source ./build.properties
  DOCKER_REGISTRY="quay.io"

  if [[ "${PUSH_IMAGE}" == "true" ]]
  then
    if [[ "${GITHUB_EVENT_NAME}" != "pull_request" || "${DOCKER_IMAGE_SKIP_PUSH}" == "false" ]]
    then
      # The docker tag can be at most 128 characters long. Leave enough characters for the build number and a separator and replace any invalid characters.
      MAX_LENGTH=$((127-${#GITHUB_RUN_NUMBER}))
      DOCKER_TAG_BASE=`echo ${GITHUB_REF_NAME} | sed "s|[^a-zA-Z0-9.\-]|_|g" | cut -c "1-${MAX_LENGTH}"`
      if [[ "${DOCKER_TAG_BASE}" == "master" ]]
      then
        if [[ "${DOCKER_TAG}" == "" ]]
        then
          DOCKER_TAG=latest
        fi
        docker buildx build --push --file Dockerfile --provenance=false --label "GIT_COMMIT=${COMMIT_MESSAGE}" --label "GIT_BRANCH=${GITHUB_REF_NAME}" --tag "${DOCKER_REGISTRY}/${DOCKER_IMAGE_REPOSITORY}:${DOCKER_TAG}" --platform linux/amd64,linux/arm64 .
      else
        if [[ "${DOCKER_TAG}" == "" ]]
        then
          DOCKER_TAG=${DOCKER_TAG_BASE}-${GITHUB_RUN_NUMBER}
        fi
        # Build and push image with 7-day expiration date
        docker buildx build --push --file Dockerfile --provenance=false --label quay.expires-after=7d --label "GIT_COMMIT=${COMMIT_MESSAGE}" --label "GIT_BRANCH=${GITHUB_REF_NAME}" --tag "${DOCKER_REGISTRY}/${DOCKER_IMAGE_REPOSITORY}:${DOCKER_TAG}" --platform linux/amd64,linux/arm64 .
      fi
    else
      echo "Skip pushing docker image on pull request"
    fi
  else
    # Build the image locally.
    if [[ "${DOCKER_TAG}" == "" ]]
    then
      DOCKER_TAG=${PROJECT_VERSION}
    fi
    docker build --label "GIT_COMMIT=${COMMIT_MESSAGE}" --label "GIT_BRANCH=${GITHUB_REF_NAME}" --tag "${DOCKER_REGISTRY}/${DOCKER_IMAGE_REPOSITORY}:${DOCKER_TAG}" --build-arg="JAVA_VERSION=${JAVA_VERSION}" .
  fi

  cd -
done
