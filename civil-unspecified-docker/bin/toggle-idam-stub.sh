#!/usr/bin/env bash

set -eu

uncommentLine() {
  matchingText=$1
  filename=$2
  sed  -i '' -e "/${matchingText}/s/^#//g" ${filename}
}

commentLine() {
  matchingText=$1
  filename=$2
  sed  -i '' -e "/${matchingText}/s/^/#/g" ${filename}
}

./ccd enable backend frontend dm-store sidam sidam-local sidam-local-ccd xui unspec docmosis camunda

if [ ${IDAM_STUB_ENABLED:-false} == "true" ]; then
  ./ccd disable sidam sidam-local sidam-local-ccd

  uncommentLine "IDAM_STUB_SERVICE_NAME" .env
  uncommentLine "IDAM_STUB_LOCALHOST" .env

  uncommentLine "#export IDAM_STUB_LOCALHOST" bin/utils/ccd-import-definition.sh

  sed -i '' -e 's/      idam-api:/      ccd-test-stubs-service:/g' compose/backend.yml

  uncommentLine "#    volumes: #comment" compose/backend.yml
  uncommentLine "get_details.json" compose/backend.yml
  uncommentLine "get_userinfo.json" compose/backend.yml
  uncommentLine "post_userinfo.json" compose/backend.yml
else
  commentLine "IDAM_STUB_SERVICE_NAME" .env
  commentLine "IDAM_STUB_LOCALHOST" .env

  commentLine "export IDAM_STUB_LOCALHOST" bin/utils/ccd-import-definition.sh

  sed -i '' -e 's/      ccd-test-stubs-service:/      idam-api:/g' compose/backend.yml

  commentLine "    volumes: #comment" compose/backend.yml
  commentLine "get_details.json" compose/backend.yml
  commentLine "get_userinfo.json" compose/backend.yml
  commentLine "post_userinfo.json" compose/backend.yml
fi

./ccd compose up -d
