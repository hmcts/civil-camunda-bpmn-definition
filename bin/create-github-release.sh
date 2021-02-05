#!/usr/bin/env bash

zip -r -j civil-damages-camunda-bpmn-definition.zip src/main/resources/camunda

currentVersion=$(curl --silent "https://api.github.com/repos/hmcts/civil-damages-camunda-bpmn-definition/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')
IFS='.' read -a versionParts <<< "$currentVersion"
patchVersion=$((versionParts[2] + 1))
nextVersion="${versionParts[0]}.${versionParts[1]}.${patchVersion}"

gh release create ${nextVersion} \
  --title civil-damages-camunda-bpmn-definition-v${nextVersion} \
  --notes TO-BE-UPDATED-WITH-BUILD-LINK \
  civil-damages-camunda-bpmn-definition.zip

rm civil-damages-camunda-bpmn-definition.zip
