#!/usr/bin/env bash

set -eu

echo 'export ENVIRONMENT=perftest'

# urls
echo "export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-perftest.service.core-compute-perftest.internal"
echo "export CAMUNDA_BASE_URL=http://camunda-api-perftest.service.core-compute-perftest.internal"
