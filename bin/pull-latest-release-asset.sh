#!/usr/bin/env bash

repoName=$1
assetName=$2

for retries in {1..5}
do
  echo "Try ${retries}"

  latestAssetId=$(curl https://api.github.com/repos/hmcts/${repoName}/releases/latest \
   | docker run --rm --interactive stedolan/jq ".assets[] | select(.name==\"${assetName}\") | .id")

  curl -L \
    -H "Accept: application/octet-stream" \
    --output $assetName \
    https://api.github.com/repos/hmcts/${repoName}/releases/assets/${latestAssetId}

  unzip -o $assetName
  unzipExitStatus=$?
  rm $assetName

  if [ "$unzipExitStatus" -eq 0 ]
  then
    exit 0
  fi
done

echo "Unable to get latest release from GitHub API"
exit 1
