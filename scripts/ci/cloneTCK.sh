#!/usr/bin/env bash
set -e

usage() {
    echo "Clone, update, build, and start ingestion-connector-tck using Docker Compose." 1>&2;
    echo "Usage: $0 [-h]" 1>&2;
    echo "  -h: Display this help" 1>&2;
    exit 1;
}

while getopts ":h" arg; do
  case $arg in
    h | *) # Display help.
      usage
      ;;
  esac
done

# Use GITHUB_TOKEN if available, otherwise fallback to HTTPS
if [ -n "$GITHUB_TOKEN" ]; then
    REPO_URL="https://x-access-token:${GITHUB_TOKEN}@github.com/HylandSoftware/ingestion-connector-tck"
    # Configure git to use token for submodules
    git config --global url."https://x-access-token:${GITHUB_TOKEN}@github.com/".insteadOf "https://github.com/"
else
    REPO_URL="https://github.com/HylandSoftware/ingestion-connector-tck"
fi

REPO_DIR="ingestion-connector-tck"

# Clone or update repository
if [ ! -d "$REPO_DIR" ]; then
    echo "Cloning repository..."
    git clone "$REPO_URL"
    cd "$REPO_DIR"
    git submodule update --init --recursive
else
    echo "Repository exists, updating..."
    cd "$REPO_DIR"
    git pull
    git submodule update --init --recursive
fi

# Ensure we're up to date
git pull
git submodule update --remote
