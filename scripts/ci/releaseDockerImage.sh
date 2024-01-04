#!/usr/bin/env bash
set -x

scriptDir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

scriptName=`basename "$0"`

usage="Usage: ${scriptName} [options]

    -h , --help           show this help text
    -v <releaseVersion>   the version number for the release (mandatory)"

while getopts ':hv:' option; do
  case "${option}" in
    h) echo -e "Build and publish the docker images for a release.\n\n${usage}"
       exit
       ;;
    v) releaseVersion=${OPTARG}
       ;;
    :) echo -e "Missing argument for -${OPTARG}\n\n${usage}" >&2
       exit 1
       ;;
   \?) echo -e "Illegal option: -${OPTARG}\n\n${usage}" >&2
       exit 1
       ;;
  esac
done
shift $((OPTIND - 1))

if [ "#${releaseVersion}" == "#" ]; then
    echo -e "Please supply a release version with the -v option.\n\n${usage}" >&2
    exit 1
fi

for dockerBuildDir in $(find -name Dockerfile -exec dirname {} \;)
do
    if [[ "${dockerBuildDir}" != *"/classes"* ]]
    then
        continue
    fi

    cd ${dockerBuildDir}
    echo "Build docker image in:: ${dockerBuildDir}"
    # Load properties related to this docker image.
    source ./build.properties

    # Build the image and push.
    docker buildx build --push --provenance=false --label "GIT_COMMIT=$COMMIT_MESSAGE" --label "GIT_BRANCH=$GITHUB_REF_NAME" --tag "${DOCKER_IMAGE_REPOSITORY}:${releaseVersion}" --platform linux/amd64,linux/arm64 .

    cd -
done
