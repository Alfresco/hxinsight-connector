#!/usr/bin/env bash

# check if format is correct and if it is not
if ! mvn spotless:check; then
  mvn spotless:apply > /dev/null
  exit 1
fi
