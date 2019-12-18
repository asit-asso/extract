#!/bin/bash

if [ "$#" -eq 0 ]; then
    echo "Usage : wait-for-web.sh full-url [number of attempts (1 per second)]"
    echo "Example : wait-for-web.sh http://example.com 5"
    exit 255
fi

attempt_counter=0
max_attempts=${2-10}

until $(curl --output /dev/null --silent --head --fail "$1"); do
    if [ ${attempt_counter} -eq ${max_attempts} ];then
      echo "Max attempts reached"
      exit 1
    fi

    printf '.'
    attempt_counter=$(($attempt_counter+1))
    sleep 1
done
