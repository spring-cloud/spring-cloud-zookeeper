#!/bin/bash

set -o errexit

SCRIPT_URL="https://raw.githubusercontent.com/spring-cloud-samples/brewery/2021.0.x/runAcceptanceTests.sh"
AT_WHAT_TO_TEST="ZOOKEEPER"

curl "${SCRIPT_URL}" --output runAcceptanceTests.sh

chmod +x runAcceptanceTests.sh

./runAcceptanceTests.sh --whattotest "${AT_WHAT_TO_TEST}" --killattheend
