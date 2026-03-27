#!/bin/bash
set -e

# Switch to the installed custom JRE if present
if [ -d "/usr/lib/jvm/temurin-21-jdk" ]; then
  export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk
elif [ -d "/usr/lib/jvm/temurin-11-jdk" ]; then
  export JAVA_HOME=/usr/lib/jvm/temurin-11-jdk
fi

exec "$@"
