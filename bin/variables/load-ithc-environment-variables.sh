#!/usr/bin/env bash

set -eu

echo 'export ENVIRONMENT=ithc'

# urls
echo "export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-ithc.service.core-compute-ithc.internal"
echo "export CAMUNDA_BASE_URL=http://camunda-api-ithc.service.core-compute-ithc.internal"
