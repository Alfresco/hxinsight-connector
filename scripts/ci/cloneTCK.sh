#!/usr/bin/env bash
set -e

usage() {
    echo "Clone and update Ingest Connector Technology Compatibility Kit." 1>&2;
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
