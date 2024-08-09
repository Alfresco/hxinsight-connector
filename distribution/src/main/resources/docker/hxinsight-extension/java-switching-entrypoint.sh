#!/bin/bash
set -e

# Switch to Java 11 if it has been installed
[ -d "/usr/lib/jvm/temurin-11-jdk" ] && export JAVA_HOME=/usr/lib/jvm/temurin-11-jdk

exec "$@"
