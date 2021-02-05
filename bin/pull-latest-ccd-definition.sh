#!/usr/bin/env bash

latestDefinitionAssetId=$(curl https://api.github.com/repos/hmcts/civil-damages-ccd-definition/releases/latest | docker run --rm --interactive stedolan/jq '.assets[] | select(.name=="civil-damages-ccd-definition.zip") | .id')

curl -L \
  -H "Accept: application/octet-stream" \
  --output ccd-definition.zip \
  https://api.github.com/repos/hmcts/civil-damages-ccd-definition/releases/assets/${latestDefinitionAssetId} \

unzip ccd-definition.zip
rm ccd-definition.zip

