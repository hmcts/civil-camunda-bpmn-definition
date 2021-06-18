#!/usr/bin/env bash

repoName=$1
assetName=$2

az login --identity
token=$(az keyvault secret show --vault-name infra-vault-nonprod --name hmcts-github-apikey \
 --query value -o tsv)

latestAssetId=$(curl -H "Authorization: token ${token}" \
  https://api.github.com/repos/hmcts/${repoName}/releases/latest \
  | docker run --rm --interactive stedolan/jq ".assets[] | select(.name==\"${assetName}\") | .id")

curl -L \
  -H "Accept: application/octet-stream" \
  -H "Authorization: token ${token}" \
  --output $assetName \
  https://api.github.com/repos/hmcts/${repoName}/releases/assets/${latestAssetId} \

unzip $assetName
rm $assetName
