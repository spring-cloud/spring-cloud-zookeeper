#!/bin/sh
set -e

REPOSRC=https://github.com/spring-cloud-samples/brewery.git
LOCALREPO=brewery

LOCALREPO_VC_DIR=$LOCALREPO/.git

if [ ! -d $LOCALREPO_VC_DIR ]
then
    git clone $REPOSRC $LOCALREPO
    cd $LOCALREPO
else
    cd $LOCALREPO
    git reset --hard
    git pull $REPOSRC master
fi

./gradlew clean build docker --parallel
docker-compose kill
docker-compose rm -f
docker-compose build
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
  curl $url:9991/health &&
  curl $url:9992/health &&
  curl $url:9993/health &&
  curl $url:9994/health && success=true && break
  n=$[$n+1]
  echo "Failed... will try again in [$waitTime] seconds"
  sleep $waitTime
done

if [ "$success" = true ] ; then
  echo "Successfully booted up all the apps. Proceeding with the acceptance tests"
  bash -e runAcceptanceTests.sh
else
  echo "Failed to boot the apps. Will now kill the containers and remove them"
fi

cd ..
