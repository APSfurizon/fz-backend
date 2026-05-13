#!/bin/bash

migrate -verbose -database="postgres://stranck@/fzbackend?host=/var/run/postgresql" -source="file://db/migrations/" up