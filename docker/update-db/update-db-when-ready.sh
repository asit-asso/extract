#!/bin/bash

echo "Updating database schema..."
psql --host=$PGHOST --username=$PGUSER --dbname=$PGDB < /update_db.sql

if [ -f /create_test_data.sql ]; then
  echo "Creating test data..."
  psql --host=$PGHOST --username=$PGUSER --dbname=$PGDB < /create_test_data.sql
fi

echo "Done"
