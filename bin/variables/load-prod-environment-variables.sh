#!/usr/bin/env bash

set -eu

echo 'export ENVIRONMENT=prod'

# urls
echo "export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-prod.service.core-compute-prod.internal"
echo "export CAMUNDA_BASE_URL=http://camunda-api-prod.service.core-compute-prod.internal"
