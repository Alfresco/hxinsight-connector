#!/usr/bin/env bash
set -e

usage() {
    echo "Clone and update Ingest Connector Technology Compatibility Kit." 1>&2;
    echo "Usage: $0 [-h] [-t TOKEN]" 1>&2;
    echo "  -h: Display this help" 1>&2;
    echo "  -t: GitHub token for authentication" 1>&2;
    exit 1;
}

GITHUB_TOKEN=""

while getopts ":ht:" arg; do
  case $arg in
    t)
      GITHUB_TOKEN=$OPTARG
      ;;
    h | *) # Display help.
      usage
      ;;
  esac
done

if [ -z "$GITHUB_TOKEN" ]; then
    echo "GitHub token is required" 1>&2
    usage
fi

REPO_URL="https://x-access-token:${GITHUB_TOKEN}@github.com/Hyland-Software/ingestion-connector-tck.git"
REPO_DIR="ingestion-connector-tck"

# Clone the repository
if [ ! -d "$REPO_DIR" ]; then
  git clone "$REPO_URL" "$REPO_DIR" --recursive
else
  echo "Repository already cloned. Pulling latest changes."
  cd "$REPO_DIR"
  git pull
  git submodule update --init --recursive
  cd -
fi

cd "$REPO_DIR"

# Update the repository to the latest version
git pull
git submodule update --remote

cd -
