#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

${dir}/utils/idam-add-role.sh "ccd-import"
${dir}/utils/idam-add-role.sh "caseworker"
${dir}/utils/idam-add-role.sh "caseworker-civil"
${dir}/utils/idam-add-role.sh "caseworker-caa"
${dir}/utils/idam-add-role.sh "caseworker-approver"

# User used during the CCD import and ccd-role creation
${dir}/utils/idam-create-caseworker.sh "ccd.docker.default@hmcts.net" "ccd-import"

${dir}/utils/ccd-add-role.sh "caseworker-civil"
${dir}/utils/ccd-add-role.sh "caseworker-caa"
${dir}/utils/ccd-add-role.sh "caseworker-approver"

roles=("solicitor" "systemupdate")
for role in "${roles[@]}"
do
  ${dir}/utils/idam-add-role.sh "caseworker-civil-${role}"
  ${dir}/utils/ccd-add-role.sh "caseworker-civil-${role}"
done

roles=("caa" "case-manager" "finance-manager" "organisation-manager" "user-manager")
for role in "${roles[@]}"
do
  ${dir}/utils/idam-add-role.sh "pui-${role}"
done

${dir}/utils/idam-add-role.sh "caseworker-probate"
${dir}/utils/idam-add-role.sh "caseworker-probate-solicitor"

${dir}/utils/idam-add-role.sh "caseworker-ia"
${dir}/utils/idam-add-role.sh "caseworker-ia-legalrep-solicitor"

${dir}/utils/idam-add-role.sh "caseworker-publiclaw"
${dir}/utils/idam-add-role.sh "caseworker-publiclaw-solicitor"

${dir}/utils/idam-add-role.sh "xui-approver-userdata"

${dir}/utils/idam-add-role.sh "caseworker-divorce"
${dir}/utils/idam-add-role.sh "caseworker-divorce-solicitor"
${dir}/utils/idam-add-role.sh "caseworker-divorce-financialremedy"
${dir}/utils/idam-add-role.sh "caseworker-divorce-financialremedy-solicitor"

prdRoles=('"caseworker"','"caseworker-caa"','"caseworker-divorce"','"caseworker-divorce-solicitor"','"caseworker-divorce-financialremedy"','"caseworker-divorce-financialremedy-solicitor"','"caseworker-probate"','"caseworker-ia"','"caseworker-probate-solicitor"','"caseworker-publiclaw"','"caseworker-ia-legalrep-solicitor"','"caseworker-publiclaw-solicitor"','"caseworker-civil"','"caseworker-civil-solicitor"','"xui-approver-userdata"','"pui-caa"','"prd-admin"','"pui-case-manager"','"pui-finance-manager"','"pui-organisation-manager"','"pui-user-manager"')
${dir}/utils/idam-add-role.sh "prd-admin" "${prdRoles[@]}"
