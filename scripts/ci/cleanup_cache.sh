#!/usr/bin/env bash
set -ev

find "${HOME}/.m2/repository/" -type d -name "*-SNAPSHOT" -print0 | xargs -0 -r -l rm -rf
