#!/usr/bin/env bash

set -eu
workspace=${1}
echo $workspace

latestDefinitionAssetId=$(curl https://api.github.com/repos/hmcts/civil-damages-camunda-bpmn-definition/releases/latest | docker run --rm --interactive stedolan/jq '.assets[] | .id')

curl -L \
  -H "Accept: application/octet-stream" \
  --output "$(realpath $workspace)/civil-damages-camunda-bpmn-definition.zip" \
  https://api.github.com/repos/hmcts/civil-damages-camunda-bpmn-definition/releases/assets/${latestDefinitionAssetId} \

filepath="$(realpath $workspace)/camunda"
echo $filepath

unzip "$(realpath $workspace)/civil-damages-camunda-bpmn-definition.zip" -d $filepath
rm "$(realpath $workspace)/civil-damages-camunda-bpmn-definition.zip"

serviceToken=$($(realpath $workspace)/civil-unspecified-docker/bin/utils/idam-lease-service-token.sh unspec_service $(docker run --rm toolbelt/oathtool --totp -b ${S2S_SECRET:-AABBCCDDEEFFGGHH}))

for file in $(find ${filepath} -name '*.bpmn')
do
  uploadResponse=$(curl --insecure -v --silent -w "\n%{http_code}" --show-error -X POST \
    ${CAMUNDA_BASE_URL:-http://localhost:9404}/engine-rest/deployment/create \
    -H "Accept: application/json" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -F "deployment-name=$(date +"%Y%m%d-%H%M%S")-$(basename ${file})" \
    -F "file=@${filepath}/$(basename ${file})")

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

if [[ "${upload_http_code}" == '200' ]]; then
  echo "$(basename ${file}) diagram uploaded successfully (${upload_response_content})"
  continue;
fi

echo "$(basename ${file}) upload failed with http code ${upload_http_code} and response (${upload_response_content})"
continue;

done
exit 0;
