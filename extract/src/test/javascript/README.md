# JavaScript Unit Tests for Extract

This directory contains JavaScript unit tests for the Extract application, specifically for the notification functionality in `requestsList.js`.

## Setup

To run these tests, you need to install the test dependencies:

```bash
# From the extract/extract directory
cp package-test.json package.json
yarn install
# or
npm install
```

## Running Tests

### Run all tests
```bash
npm test
# or
yarn test
```

### Run tests in watch mode (reruns on file changes)
```bash
npm run test:watch
# or
yarn test:watch
```

### Run tests with coverage report
```bash
npm run test:coverage
# or
yarn test:coverage
```

## Test Structure

- `requestsList.test.js` - Unit tests for notification functions and AJAX error handling
- `setup.js` - Test setup and configuration

## What's Being Tested

### Notification Functions (`_showAjaxErrorNotification`, `_clearAjaxErrorNotification`)
- Creating notifications with default English messages
- Creating notifications with French localization
- Preventing duplicate notifications
- Proper styling and DOM structure
- Auto-dismiss functionality after 10 seconds
- Clearing notifications

### Connectors State Refresh (`_refreshConnectorsState`)
- Handling authentication redirects (302 status)
- Handling invalid JSON responses
- Processing valid connector data
- Detecting HTML responses when expecting JSON

### Edge Cases
- Partially defined LANG_MESSAGES object
- Concurrent notification requests
- HTML content in AJAX responses

## Coverage Goals

The tests aim for at least 70% coverage of:
- Branches
- Functions
- Lines
- Statements

## Integration with CI/CD

To integrate these tests into your build pipeline:

### Maven Integration
Add to your `pom.xml`:

```xml
<plugin>
    <groupId>com.github.eirslett</groupId>
    <artifactId>frontend-maven-plugin</artifactId>
    <version>1.12.1</version>
    <executions>
        <execution>
            <id>install node and yarn</id>
            <goals>
                <goal>install-node-and-yarn</goal>
            </goals>
            <configuration>
                <nodeVersion>v18.16.0</nodeVersion>
                <yarnVersion>v1.22.19</yarnVersion>
            </configuration>
        </execution>
        <execution>
            <id>yarn install</id>
            <goals>
                <goal>yarn</goal>
            </goals>
        </execution>
        <execution>
            <id>javascript tests</id>
            <goals>
                <goal>yarn</goal>
            </goals>
            <configuration>
                <arguments>test</arguments>
            </configuration>
            <phase>test</phase>
        </execution>
    </executions>
</plugin>
```

### Docker Integration
Add to your Dockerfile:

```dockerfile
# Run JavaScript tests
RUN yarn install && yarn test
```

## Troubleshooting

### jQuery not found
Make sure jQuery is installed:
```bash
npm install jquery --save-dev
```

### Tests not finding functions
The functions in `requestsList.js` need to be exported for testing. You can either:
1. Export the functions (modify the source file)
2. Load the entire file in tests (using eval or require)
3. Use a test framework that can access private functions

### JSDOM issues
If you encounter JSDOM-related errors:
```bash
npm install jest-environment-jsdom --save-dev
```