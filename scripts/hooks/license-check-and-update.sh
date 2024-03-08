#!/usr/bin/env bash

modified_files=$(git diff --cached --name-only --diff-filter=AM)

mvn validate -DlicenseUpdateHeaders=true > /dev/null

all_old_headers=$(git diff --name-only --diff-filter=AM)

for file in ${all_old_headers}
do
  revert=1
  for modified_file in ${modified_files}
  do
    if [[ "${modified_file}" == "${file}" ]]
    then
      revert=0
      break
    fi
  done
  if [[ ${revert} == 1 ]]
  then
    git checkout -- "${file}"
  fi
done
