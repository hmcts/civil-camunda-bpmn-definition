#!/usr/bin/env bash

set -eu

ccdRepoName="civil-ccd-definition"
branchName=${1:-master}

find_first_existing_dir() {
  for candidate in "$@"; do
    if [ -d "${candidate}" ]; then
      echo "${candidate}"
      return 0
    fi
  done
  return 1
}

# Checkout specific branch of CCD definitions.
git clone https://github.com/hmcts/${ccdRepoName}.git
cd "${ccdRepoName}"

echo "Switch to ${branchName} branch on ${ccdRepoName}"
git checkout "${branchName}"
cd ..

mergedDefinitionsDir=$(find_first_existing_dir \
  "./${ccdRepoName}/ccd-definition" \
  "./${ccdRepoName}/civil-ccd-definition/ccd-definition" || true)

if [ -z "${mergedDefinitionsDir:-}" ]; then
  echo "Unable to locate merged CCD definition directory in ${ccdRepoName}."
  exit 1
fi

rm -rf ./ccd-definition ./e2e ./playwright-e2e ./plugins \
  ./package.json ./yarn.lock ./.yarnrc.yml ./.yarn \
  ./playwright.config.ts ./codecept.conf.js

cp -r "${mergedDefinitionsDir}" ./ccd-definition
cp -r "./${ccdRepoName}/e2e" .
cp -r "./${ccdRepoName}/playwright-e2e" .
cp -r "./${ccdRepoName}/plugins" .
cp -r "./${ccdRepoName}/package.json" .
cp -r "./${ccdRepoName}/yarn.lock" .
cp -r "./${ccdRepoName}/.yarnrc.yml" .
cp -r "./${ccdRepoName}/.yarn" .
cp -r "./${ccdRepoName}/playwright.config.ts" .
cp -r "./${ccdRepoName}/codecept.conf.js" .

rm -rf "./${ccdRepoName}"
