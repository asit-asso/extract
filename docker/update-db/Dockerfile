FROM postgres:12-alpine

#addition packages
RUN apk add --update \
    curl \
  && rm -rf /var/cache/apk/*

#unset entrypoint
ENTRYPOINT []

# Copy scripts
COPY update-db-when-ready.sh update-db-when-ready.sh
RUN chmod +x update-db-when-ready.sh

CMD /update-db-when-ready.sh
