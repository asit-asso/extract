#!/bin/bash

echo "Waiting for PostgreSQL to be ready..."
until psql --host=$PGHOST --username=$PGUSER --dbname=$PGDB -c '\q' 2>/dev/null; do
  echo "PostgreSQL is unavailable - sleeping"
  sleep 1
done

echo "Waiting for Hibernate to create database tables..."
until psql --host=$PGHOST --username=$PGUSER --dbname=$PGDB -c "SELECT 1 FROM users LIMIT 1" 2>/dev/null; do
  echo "Tables not yet created by Hibernate - waiting..."
  sleep 2
done

echo "Tables created - updating database schema..."
psql --host=$PGHOST --username=$PGUSER --dbname=$PGDB < /update_db.sql

if [ -f /create_test_data.sql ]; then
  echo "Creating test data..."
  psql --host=$PGHOST --username=$PGUSER --dbname=$PGDB < /create_test_data.sql
fi

echo "Database initialization completed successfully"
