#!/usr/bin/env bash

java --version

if ! mvn spotless:check; then
  mvn spotless:apply > /dev/null
  exit 1
fi
