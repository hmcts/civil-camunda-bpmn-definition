#!/usr/bin/env bash

repoName=$1
assetName=$2

latestAssetId=$(curl https://api.github.com/repos/hmcts/${repoName}/releases/latest \
  | docker run --rm --interactive stedolan/jq ".assets[] | select(.name==\"${assetName}\") | .id")

curl -L \
  -H "Accept: application/octet-stream" \
  --output $assetName \
  https://api.github.com/repos/hmcts/${repoName}/releases/assets/${latestAssetId} \

unzip $assetName
rm $assetName
