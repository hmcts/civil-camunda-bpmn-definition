#!/usr/bin/env bash

set -eu

if [ ! -d "ccd-definition/civil" ]; then
  echo "Unable to locate civil CCD definition directory at ccd-definition/civil."
  exit 1
fi

definition_input_dir=$(realpath 'ccd-definition/civil')
definition_output_file="$(realpath ".")/build/ccd-development-config/ccd-civil-dev.xlsx"
params="$@"

./bin/utils/import-ccd-definition.sh "${definition_input_dir}" "${definition_output_file}" "${params}"
