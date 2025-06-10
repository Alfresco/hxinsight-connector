#!/usr/bin/env bash
set -e

# This script generates JaCoCo code coverage reports for all modules in the workspace.
EXEC_FILES=$(find ${WORKSPACE}/jacoco-data -name "*.exec" -type f | tr '\n' ',')
EXEC_FILES=${EXEC_FILES%,}

echo "Using exec files:"
echo "$EXEC_FILES"

MODULES=$(find ${WORKSPACE}/jacoco-data -mindepth 1 -maxdepth 1 -type d | xargs -n1 basename | sed 's/jacoco-report-//' | tr '\n' ',')
MODULES=${MODULES%,}

echo "Processing modules: $MODULES"

echo "Building modules for coverage report generation"
mvn clean compile -DskipTests

for module in $(echo $MODULES | tr ',' ' '); do
    echo "Processing module: $module"
    mkdir -p ${WORKSPACE}/$module/target/

    echo "Looking for exec file for module $module"
    find ${WORKSPACE}/jacoco-data -name "*$module*.exec" -type f

    if [ -f "${WORKSPACE}/jacoco-data/jacoco-report-$module/target/jacoco-$module.exec" ]; then
        cp -v "${WORKSPACE}/jacoco-data/jacoco-report-$module/target/jacoco-$module.exec" "${WORKSPACE}/$module/target/" || true
        echo "Copied exec file from target directory"
    elif [ -f "${WORKSPACE}/jacoco-data/jacoco-report-$module/jacoco-$module.exec" ]; then
        cp -v "${WORKSPACE}/jacoco-data/jacoco-report-$module/jacoco-$module.exec" "${WORKSPACE}/$module/target/" || true
        echo "Copied exec file from root artifact directory"
    else
        echo "No exec file found for module $module"
        find ${WORKSPACE}/jacoco-data -name "*.exec" -type f | grep -i $module || echo "No exec files with $module in name found"
    fi
done

echo "Checking if exec files were copied:"
find ${WORKSPACE} -name "jacoco-*.exec" -type f

for module in $(echo $MODULES | tr ',' ' '); do
    echo "Generating report for module: $module"
    if [ -f "${WORKSPACE}/$module/target/jacoco-$module.exec" ]; then
        echo "Found exec file for $module, generating report"
        mvn -f ${WORKSPACE}/$module/pom.xml jacoco:report -Djacoco.dataFile=${WORKSPACE}/$module/target/jacoco-$module.exec || true
    else
        echo "No exec file found for module $module"
    fi
done

echo "Checking for generated reports:"
find ${WORKSPACE} -name "jacoco.xml" -type f
