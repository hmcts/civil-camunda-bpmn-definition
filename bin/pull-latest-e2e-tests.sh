#!/usr/bin/env bash

latestDefinitionAssetId=$(curl https://api.github.com/repos/hmcts/civil-damages-ccd-definition/releases/latest | docker run --rm --interactive stedolan/jq '.assets[] | select(.name=="civil-damages-e2e.zip") | .id')

curl -L \
  -H "Accept: application/octet-stream" \
  --output civil-damages-e2e.zip \
  https://api.github.com/repos/hmcts/civil-damages-ccd-definition/releases/assets/${latestDefinitionAssetId} \

unzip civil-damages-e2e.zip
rm civil-damages-e2e.zip
