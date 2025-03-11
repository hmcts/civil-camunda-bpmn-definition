#!/usr/bin/env bash

set -eu

branchName=$1

#Checkout specific branch pf  civil camunda bpmn definition
git clone https://github.com/hmcts/civil-wa-task-configuration.git
cd civil-wa-task-configuration

echo "Switch to ${branchName} branch on civil-wa-task-configuration"
git checkout ${branchName}
cd ..

#Copy camunda folder to civil-ccd-def which contians dmn files
cp -r ./civil-wa-task-configuration/src/main/resources .
rm -rf ./civil-wa-task-configuration

./bin/import-dmn-diagram.sh . civil civil preview
