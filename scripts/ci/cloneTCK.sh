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

REPO_URL="https://github.com/HylandSoftware/ingestion-connector-tck"
REPO_DIR="ingestion-connector-tck"

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
