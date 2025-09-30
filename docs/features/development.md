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

## Documentation (edit and build)

If you would like to set up a local development environment to edit the current documentation, perform the following steps. You just need an installation of python 3 on your machine.

1. Create and activate a python virtual environment (not mandatory but recommended)
    ```powershell
    python -m venv .venv

    Set-ExecutionPolicy Unrestricted -Scope Process  # only for windows
    .venv\Scripts\Activate.ps1 
    ```

2. Install the necessary packages
    ```powershell
    pip install mkdocs-material mkdocs-exclude-search
    ```

3. Build and serve the documentation on localhost
    ```
    mkdocs serve
    ```
4. Visit http://localhost:8000

You can then edit the markdown files under ``./docs`` following the syntax from [mkdocs-material](https://squidfunk.github.io/mkdocs-material/reference/){target="_blank"}. The browser will automatically reload when changes are made to the ``./docs`` folder.

To publish your modification, send them (only the markdown files) through a pull request on GitHub. When your pull request will be merged to the main branch, the documentation will automatically be build and publish to the documentation website.

<br>
<br>
<br>
<br>
<br>