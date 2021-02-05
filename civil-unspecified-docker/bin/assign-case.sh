#!/usr/bin/env bash

set -eu

if [ "${ENVIRONMENT:-local}" != "local" ]; then
  exit 0;
fi

dir=$(dirname ${0})

DEFENDANT_EMAIL=${1}
CASE_ID=${2}

echo -e "\nAssigning case to defendant: ${DEFENDANT_EMAIL}"

userToken=$(${dir}/utils/idam-access-token.sh "${DEFENDANT_EMAIL}" "Password12!")

STATUS=$(curl --silent --output /dev/null --write-out '%{http_code}' -H 'Content-Type: application/json' -H "Authorization: Bearer ${userToken}" -X POST\
  http://localhost:4000/testing-support/assign-case/${CASE_ID} )

echo "status ${STATUS}"

if [ $STATUS -eq 200 ]; then
  echo "Role created sucessfully"
elif [ $STATUS -eq 409 ]; then
  echo "Role already exists!"
else
  echo "ERROR: HTTPCODE = $STATUS"
  exit 1
fi
