#!/bin/sh
set -e #crashes the script on errors

echo "pulling from git"
#git pull origin master
echo "stopping service"
service fz-backend stop
echo "building docker"
docker build --no-cache -t fz-backend .
echo "Starting service"
service fz-backend start
service fz-backend status
