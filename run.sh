#!/bin/sh

docker run --env-file=/home/webint/fz-backend/.env -v /home/webint/fz-backend/data:/app/data fz-backend
