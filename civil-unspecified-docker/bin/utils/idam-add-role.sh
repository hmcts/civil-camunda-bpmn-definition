#!/usr/bin/env bash

set -eu

if [ "${ENVIRONMENT:-local}" != "local" ]; then
  exit 0;
fi

dir=$(dirname ${0})

ID=${1}

echo -e "\nCreating IDAM role: ${ID}"

if [ "$#" -ne 2  ]; then
    ASSINABLE_ROLES="[]"
else
    ASSINABLE_ROLES='['$2']'
    echo -e '\nASSIGNING role: '${ASSINABLE_ROLES}''
fi

apiToken=$(${dir}/idam-authenticate.sh "${IDAM_ADMIN_USER}" "${IDAM_ADMIN_PASSWORD}")


STATUS=$(curl --silent --output /dev/null --write-out '%{http_code}' -H 'Content-Type: application/json' -H "Authorization: AdminApiAuthToken ${apiToken}" \
  ${IDAM_API_BASE_URL:-http://localhost:5000}/roles -d '{
  "id": "'${ID}'",
  "name": "'${ID}'",
  "description": "'${ID}'",
  "assignableRoles": '${ASSINABLE_ROLES}',
  "conflictingRoles": [ ]
}')

if [ $STATUS -eq 201 ]; then
  echo "Role created sucessfully"
elif [ $STATUS -eq 409 ]; then
  echo "Role already exists!"
else
  echo "ERROR: HTTPCODE = $STATUS"
  exit 1
fi
