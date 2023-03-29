#!/usr/bin/env bash

branchName=$1

#Checkout specific branch pf  civil camunda bpmn definition
git clone https://github.com/hmcts/civil-ccd-definition.git
cd civil-ccd-definition

echo "Switch to ${branchName} branch on civil-ccd-definition"
git checkout ${branchName}
cd ..

#Copy ccd definition files  to civil-ccd-def which contians ccd def files
cp -r ./civil-ccd-definition/ccd-definition .
cp -r ./civil-ccd-definition/e2e .
cp -r ./civil-ccd-definition/package.json .
cp -r ./civil-ccd-definition/yarn.lock .
cp -r ./civil-ccd-definition/codecept.conf.js .
echo *
rm -rf ./civil-ccd-definition
