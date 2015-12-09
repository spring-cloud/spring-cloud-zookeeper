#!/bin/sh
set -e

curl https://raw.githubusercontent.com/spring-cloud-samples/brewery/master/acceptance-tests/scripts/runDockerAcceptanceTests.sh --output runDockerAcceptanceTests.sh
sh runDockerAcceptanceTests.sh -DWHAT_TO_TEST=SERVICE_REGISTRY