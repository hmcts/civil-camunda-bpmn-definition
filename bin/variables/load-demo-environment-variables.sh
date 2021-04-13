#!/usr/bin/env bash

set -eu

echo 'export ENVIRONMENT=demo'

# urls
echo "export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-demo.service.core-compute-demo.internal"
echo "export CAMUNDA_BASE_URL=http://camunda-api-demo.service.core-compute-demo.internal"
