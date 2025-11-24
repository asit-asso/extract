#!/bin/bash

echo "Waiting for PostgreSQL to be ready..."
until psql --host=$PGHOST --username=$PGUSER --dbname=$PGDB -c '\q' 2>/dev/null; do
  echo "PostgreSQL is unavailable - sleeping"
  sleep 1
done

echo "PostgreSQL is up - updating database schema..."
psql --host=$PGHOST --username=$PGUSER --dbname=$PGDB < /update_db.sql

if [ -f /create_test_data.sql ]; then
  echo "Creating test data..."
  psql --host=$PGHOST --username=$PGUSER --dbname=$PGDB < /create_test_data.sql
fi

echo "Database initialization completed successfully"
