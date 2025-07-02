#!/bin/bash
set -ex

echo "Running Functional tests on ${ENVIRONMENT} env"

if [ "$FORCE_GREEN_BUILD" == "true" ]; then
  echo "Manually forced green build, no functional tests will be run."
  exit 0
fi


# Check if RUN_FAILED_AND_PREV_NOT_EXECUTED_TEST_FILES is set to "true"
if [ "$RUN_PREV_FAILED_AND_NOT_EXECUTED_TEST_FILES" != "true" ]; then
  yarn playwright install
  yarn test:api-nightly-prod
else 
  # Define path to failedTestFiles.json and prevTestFilesReport.json
  TEST_FILES_REPORT="test-results/functional/testFilesReport.json"
  PREV_TEST_FILES_REPORT="test-results/functional/prevTestFilesReport.json"

  # Check if testFilesReport.json exists and is non-empty
  if [ ! -f "$TEST_FILES_REPORT" ] || [ ! -s "$TEST_FILES_REPORT" ]; then
    echo "testFilesReport.json not found or is empty."
    exit 1

  # Check if the JSON array inside testFilesReport.json is empty
  elif [ "$(jq '.failedTestFiles | length' "$TEST_FILES_REPORT")" -eq 0 ]; then
    echo "failedTestFiles in testFilesReport.json contains an empty array."
    exit 1

  else
    echo "Running failed and not executed functional test files on ${ENVIRONMENT} env"
    # Move testFilesReport.json to prevTestFilesReport.json
    mv "$TEST_FILES_REPORT" "$PREV_TEST_FILES_REPORT"

    # Collect array elements into a comma-separated string
    PREV_FAILED_TEST_FILES=$(jq -r '.failedTestFiles[]' "$PREV_TEST_FILES_REPORT" | paste -sd "," -)

    # Collect array elements into a comma-separated string
    PREV_NOT_EXECUTED_TEST_FILES=$(jq -r '.notExecutedTestFiles[]' "$PREV_TEST_FILES_REPORT" | paste -sd "," -)

    # Export as environment variable
    export PREV_FAILED_TEST_FILES="$PREV_FAILED_TEST_FILES"
    export PREV_NOT_EXECUTED_TEST_FILES="$PREV_NOT_EXECUTED_TEST_FILES"
      
    yarn playwright install
    yarn test:api-nightly-prod
  fi
fi



