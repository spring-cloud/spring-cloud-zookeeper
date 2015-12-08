#!/bin/sh

git clone https://github.com/spring-cloud-samples/brewery.git

cd brewery
./gradlew clean build docker --parallel
docker-compose up -d

url="http://127.0.0.1"
waitTime=5
retries=48
totalWaitingTime=240
n=0
success=false

echo "Waiting for the apps to boot for [$totalWaitingTime] seconds"
until [ $n -ge $retries ]
do
  echo "Pinging applications if they're alive..."
  curl $url:9091/health &&
  curl $url:9092/health &&
  curl $url:9093/health &&
  curl $url:9094/health && success=true && break
  n=$[$n+1]
  echo "Failed... will try again in [$waitTime] seconds"
  sleep $waitTime
done

if [ "$success" = true ] ; then
  echo "Successfully booted up all the apps. Proceeding with the acceptance tests"
  bash -e runAcceptanceTests.sh -Dspring.zipkin.enabled=false -Dspring.cloud.zookeeper.maxRetries=5 -Dpresenting.url=$url:9091
else
  echo "Failed to boot the apps. Will now kill the containers and remove them"
fi

docker-compose kill
docker-compose rm -f

cd ..
