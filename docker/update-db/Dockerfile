FROM postgres:12-alpine

#addition packages
RUN apk add --update \
    curl \
  && rm -rf /var/cache/apk/*

#unset entrypoint
ENTRYPOINT []

# Copy scripts
COPY wait-for-web.sh wait-for-web.sh
COPY update-db-when-ready.sh update-db-when-ready.sh
RUN chmod +x wait-for-web.sh update-db-when-ready.sh

CMD /update-db-when-ready.sh
