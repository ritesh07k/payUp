#!/bin/bash
set -e

for db in auth_db merchant_db payment_db refund_db webhook_db; do
  echo "Creating database: $db"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
    CREATE DATABASE $db;
EOSQL
done
