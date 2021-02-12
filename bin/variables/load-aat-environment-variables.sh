#!/usr/bin/env bash

set -eu

echo 'export ENVIRONMENT=aat'

# urls
echo "export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
echo "export CAMUNDA_BASE_URL=http://camunda-api-aat.service.core-compute-aat.internal"
