# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Extract is a Spring Boot-based Java web application for automated geodata extraction and delivery. It imports data requests from platforms, executes configured tasks to extract the requested data, and returns results to clients.

## Technology Stack

- **Backend**: Java 17, Spring Boot 2.7.18, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf templates, jQuery, Bootstrap 5, DataTables, OpenLayers
- **Database**: PostgreSQL 12+
- **Build**: Maven
- **Application Server**: Tomcat 9
- **JavaScript Dependencies**: Managed via Yarn

## Common Development Commands

### Build Commands
```bash
# Build the entire project (creates WAR file)
cd extract & ./mvnw clean package

# Build without tests
cd extract & ./mvnw clean package -DskipTests

# Install JavaScript dependencies (required before first build)
cd extract
yarn install
```

### Test Commands
```bash
# Run unit tests only
cd extract & ./mvnw -q test -Punit-tests --batch-mode --fail-at-end

# Run integration tests
cd extract & ./mvnw -q verify -Pintegration-tests --batch-mode

# Run functional tests (requires app running on localhost:8080)
cd extract & ./mvnw -q verify -Pfunctional-tests --batch-mode

# Run all tests
cd extract & ./mvnw clean verify
```

### Running the Application
```bash
# Run with Spring Boot (development)
cd extract
mvn spring-boot:run

# Deploy WAR to Tomcat
# Copy extract/target/extract##2.2.0.war to Tomcat's webapps directory
```

## Architecture Overview

### Multi-Module Structure

The project follows a multi-module Maven architecture:

- **extract-interface**: Common interfaces for connectors and task processors
- **extract**: Core application containing web UI and orchestrator
- **extract-connector-easysdiv4**: Connector for easySDI v4 platform integration
- **extract-task-***: Various task processor modules (email, FME, QGIS, archive, validation, etc.)

### Core Components

1. **Orchestrator** (`ch.asit_asso.extract.orchestrator.Orchestrator`): Background job scheduler that manages the extraction workflow
2. **Connectors** (`ch.asit_asso.extract.connectors`): Import requests from external platforms
3. **Task Processors** (`ch.asit_asso.extract.plugins`): Execute specific processing tasks on requests
4. **Request Matching** (`ch.asit_asso.extract.requestmatching`): Matches incoming requests to configured processes

### Plugin Architecture

Extract uses a plugin-based architecture for extensibility:

- **Connectors**: Implement `IConnector` interface to integrate with external platforms
- **Task Processors**: Implement `ITaskProcessor` interface to add processing capabilities
- Plugins are discovered via Java ServiceLoader mechanism (META-INF/services)
- Plugin JARs are placed in `src/main/resources/connectors/` or `src/main/resources/task_processors/`

### Security & Authentication

- Spring Security configuration with form-based authentication
- Support for LDAP/Active Directory integration
- Two-factor authentication (2FA) support with TOTP
- Remember-me functionality
- Password reset via email

### Key Domain Entities

- **User**: Application users with roles (ADMIN, USER)
- **Process**: Configured extraction workflows
- **Request**: Data extraction requests from external systems
- **Task**: Individual processing steps within a request
- **Connector**: External system integration points
- **Rule**: Request matching rules for automatic process assignment

### Database Schema

The application uses JPA/Hibernate with PostgreSQL. Key considerations:
- Auto-generates schema on first run (`spring.jpa.hibernate.ddl-auto=update`)
- Database migrations handled via `sql/update_db.sql`
- Encryption for sensitive data using configured secrets

### Configuration

Primary configuration files:
- `src/main/resources/application.properties`: Main application configuration
- `src/main/resources/logback-spring.xml`: Logging configuration
- Environment-specific overrides supported via Spring profiles

### Frontend Architecture

- Server-side rendering with Thymeleaf templates
- JavaScript modules in `src/main/resources/static/js/`
- Localization support (French by default) via property files
- Interactive maps using OpenLayers for geodata visualization

## Requirements
- All the code that you add must carefully analyse implications and not break the existing
- Always privilege reusability of code
- Always privilege code readability
- Always privilege code maintainability
- Always privilege code testability
- Always privilege code security
- Apply SRT principles
- Apply KISS principles
- N'ajoute jamais CLAUDE.md au repo git
- Je veux des commit oneliners