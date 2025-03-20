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
    echo "Error: GitHub token is required" 1>&2
    usage
fi

REPO_URL="https://x-access-token:${GITHUB_TOKEN}@github.com/HylandSoftware/ingestion-connector-tck.git"
REPO_DIR="ingestion-connector-tck"

# Configure git for submodules
git config --global url."https://x-access-token:${GITHUB_TOKEN}@github.com/".insteadOf "https://github.com/"

# Clone or update repository
if [ ! -d "$REPO_DIR" ]; then
    echo "Cloning repository..."
    git clone "$REPO_URL" || exit 1
    cd "$REPO_DIR" || exit 1
    git submodule update --init --recursive || exit 1
else
    echo "Repository exists, updating..."
    cd "$REPO_DIR" || exit 1
    git pull
    git submodule update --init --recursive
fi

# Ensure we're up to date
git pull
git submodule update --remote
