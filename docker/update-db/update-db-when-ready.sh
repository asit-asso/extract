#!/bin/bash

# The web application creates the database, wait for the app to be up, so DB is ready
echo "Waiting for EXTRACT to be up..."
/wait-for-web.sh http://tomcat:8080/extract/login 100
echo "Updating database schema..."
psql --host=$PGHOST --username=$PGUSER --dbname=$PGDB < /update_db.sql
psql --host=$PGHOST --username=$PGUSER --dbname=$PGDB < /create_test_data.sql
echo "Done"
/wait-for-web.sh http://tomcat:8080/titi/toto -1