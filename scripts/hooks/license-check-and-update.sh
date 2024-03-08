#!/usr/bin/env bash

if [[ -e ${GITHUB_MODIFIED_FILES} ]]
then
  modified_files=${GITHUB_MODIFIED_FILES}
else
  modified_files=$(git diff --cached --name-only --diff-filter=ACMR)
fi

mvn validate -DlicenseUpdateHeaders=true

all_old_headers=$(git diff --name-only --diff-filter=ACMR)

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
