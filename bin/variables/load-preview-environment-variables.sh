#!/usr/bin/env bash

set -eu

pr=${1}

echo 'export ENVIRONMENT=preview'

# urls
echo "export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
echo "export IDAM_API_BASE_URL=https://idam-api.aat.platform.hmcts.net"
echo "export CCD_IDAM_REDIRECT_URL=https://ccd-case-management-web-aat.service.core-compute-aat.internal/oauth2redirect"
echo "export CCD_DEFINITION_STORE_API_BASE_URL=https://ccd-definition-store-civil-camunda-pr-${pr}.service.core-compute-preview.internal"
echo "export CAMUNDA_BASE_URL=https://camunda-civil-camunda-pr-${pr}.service.core-compute-preview.internal"
echo "export SERVICE_REQUEST_UPDATE=https://civil-camunda-pr-${pr}.service.core-compute-preview.internal/service-request-update"
echo "export SERVICE_REQUEST_UPDATE_CLAIM_ISSUED=https://civil-camunda-pr-${pr}.service.core-compute-preview.internal/service-request-update-claim-issued"

# definition placeholders
echo "export CCD_DEF_CASE_SERVICE_BASE_URL=http://civil-camunda-pr-${pr}-java"
