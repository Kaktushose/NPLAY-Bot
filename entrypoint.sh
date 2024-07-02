#!/bin/sh

set -e

/wait-for-it.sh -t 30 postgres:5432

/flyway/flyway migrate -url="$POSTGRES_URL" -user="$POSTGRES_USER" -password="$POSTGRES_PASSWORD" -locations=filesystem:/db/migration -X

exec "$@"
