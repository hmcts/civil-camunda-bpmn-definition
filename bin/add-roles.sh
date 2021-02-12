#!/usr/bin/env bash

# User used during the CCD import and ccd-role creation
./bin/utils/ccd-add-role.sh "caseworker-civil"
./bin/utils/ccd-add-role.sh "caseworker-caa"
./bin/utils/ccd-add-role.sh "caseworker-approver"

roles=("solicitor" "systemupdate")
for role in "${roles[@]}"
do
  ./bin/utils/ccd-add-role.sh "caseworker-civil-${role}"
done
