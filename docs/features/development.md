---
title: Development
---

## Packaging

To generate a WAR of the application, run the following command:

``` bash
mvn package
```

In addition to the WAR file, the delivery archive of a new version
contains:

-   The latest version of the database update script
    (`sql/update_db.sql`)
-   The FME sample script (`fme/`)

## Tests

Unit tests can be run independently of packaging with the command:

``` bash
mvn -q test -Punit-tests --batch-mode --fail-at-end
```

To run integration tests:

``` bash
mvn -q verify -Pintegration-tests --batch-mode
```

For functional tests (requires the application to be running on
localhost port 8080):

``` bash
mvn -q verify -Pfunctional-tests --batch-mode
```

<br>
<br>
<br>
<br>
<br>