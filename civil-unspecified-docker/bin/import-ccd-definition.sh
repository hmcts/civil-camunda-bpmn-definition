#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

if [[ ($# -ne 3) && ($# -ne 5) ]]; then
  echo "Usage: ./import-ccd-definition [-e 'UserProfile.json,*-nonprod.json'] - the -e argument is optional, "\
  "if provided the files matching the provided patterns will be excluded from processing."
  exit 1
fi

definition_input_dir=${1}
definition_output_file=${2}
additionalParameters="${@:3}"

echo "Definition directory: ${definition_input_dir}"
echo "Definition spreadsheet ${definition_output_file}"
echo "Additional parameters: ${additionalParameters}"

mkdir -p $(dirname ${definition_output_file})

${dir}/utils/process-definition.sh ${definition_input_dir} ${definition_output_file} "${additionalParameters}"
${dir}/utils/ccd-import-definition.sh ${definition_output_file}
