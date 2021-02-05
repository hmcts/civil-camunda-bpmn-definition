#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

${dir}/utils/idam-create-service.sh "ccd_gateway" "ccd_gateway" "ccd_gateway_secret" "http://localhost:3451/oauth2redirect"

${dir}/utils/idam-create-service.sh "unspec" "unspec" "OOOOOOOOOOOOOOOO" "https://localhost:9000/oauth2/callback"

${dir}/utils/idam-create-service.sh "xui_webapp" "xui_webapp" "OOOOOOOOOOOOOOOO" "http://localhost:3333/oauth2/callback" "false" "profile openid roles manage-user create-user"

${dir}/utils/idam-create-service.sh "xui_mo_webapp" "xui_mo_webapp" "OOOOOOOOOOOOOOOO" "http://localhost:3000/oauth2/callback" "false" "profile openid roles manage-user create-user manage-roles"

${dir}/utils/idam-create-service.sh "xuiaowebapp" "xuiaowebapp" "OOOOOOOOOOOOOOOO" "http://localhost:3000/oauth2/callback" "false" "profile openid roles manage-user create-user"

${dir}/utils/idam-create-service.sh "aac_manage_case_assignment" "aac_manage_case_assignment" "OOOOOOOOOOOOOOOO" "https://manage-case-assignment/oauth2redirect" "false" "openid profile roles manage-user"

${dir}/utils/idam-create-service.sh "rd_professional_api" "rd_professional_api" "OOOOOOOOOOOOOOOO" "https://rd_professional_api/oauth2redirect" "false" "openid profile roles create-user manage-user search-user"

${dir}/utils/idam-create-service.sh "rd_user_profile_api" "rd_user_profile_api" "OOOOOOOOOOOOOOOO" "https://rd_user_profile_api/oauth2redirect" "false" "openid profile roles create-user manage-user search-user"
