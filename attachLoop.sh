#!/bin/bash

while true; do docker attach fz-backend.service; sleep 1; echo "WAITING 5 SECS"; sleep 5; service fz-backend start; sleep 2; done
