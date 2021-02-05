#!/usr/bin/env bash

docker exec -it compose_ccd-shared-database_1 psql -U postgres -c "CREATE USER civil_scheduler WITH PASSWORD 'civil_scheduler'"
docker exec -it compose_ccd-shared-database_1 psql -U postgres -c "CREATE DATABASE civil_scheduler WITH OWNER civil_scheduler"
