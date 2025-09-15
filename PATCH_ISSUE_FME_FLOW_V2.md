# PATCH ISSUE: FME Flow V2 Task Processor

## Issue
Implementation of a new FME Flow (formerly FME Server) task processor (v2) with enhanced security, GeoJSON parameter support, and improved error handling for modern FME Flow installations.

## Problem Solved
The original FME Server plugin had several limitations:
- Limited parameter passing capabilities
- Basic authentication only
- No structured geometry support
- Limited error reporting
- Outdated API usage patterns

This v2 implementation addresses these issues by:
- Supporting both token-based and basic authentication
- Passing parameters via GeoJSON structure for complex geometries
- Enhanced SSL/TLS handling with custom certificate support
- Comprehensive error reporting and logging
- Modern REST API integration

## Implementation Details

### Key Features

1. **Enhanced Authentication**:
   - Token-based authentication support
   - Basic authentication fallback
   - Secure credential handling
   - SSL certificate validation options

2. **GeoJSON Parameter Support**:
   - Parameters passed as structured GeoJSON Feature
   - Automatic WKT to GeoJSON geometry conversion
   - All request parameters embedded in properties
   - Compatible with FME Flow's modern parameter handling

3. **Parameter Structure**:
   ```json
   {
     "type": "Feature",
     "geometry": { /* GeoJSON geometry from WKT */ },
     "properties": {
       "RequestId": 365,
       "FolderOut": "/path/to/output/",
       "FolderIn": "/path/to/input/",
       "OrderGuid": "uuid",
       "OrderLabel": "label",
       "ClientGuid": "uuid",
       "ClientName": "name",
       "OrganismGuid": "uuid",
       "OrganismName": "name",
       "ProductGuid": "uuid",
       "ProductLabel": "label",
       "Parameters": { /* custom parameters object */ }
     }
   }
   ```

4. **Enhanced Security**:
   - SSL context configuration
   - Custom trust manager support
   - Secure random token generation
   - HTTPS enforcement options

5. **Improved Error Handling**:
   - Detailed HTTP response analysis
   - Structured error messages
   - Job status monitoring
   - Timeout handling with configurable intervals

### Files Created

#### New Module Structure
- `extract-task-fmeserver-v2/`
  - `pom.xml` - Maven configuration with updated dependencies
  - `src/main/java/ch/asit_asso/extract/plugins/fmeflowv2/`
    - `FmeFlowV2Plugin.java` - Main plugin implementation with enhanced security and GeoJSON support
    - `FmeFlowV2Result.java` - Enhanced result handler with detailed status reporting
    - `LocalizedMessages.java` - Comprehensive internationalization support
  - `src/main/resources/`
    - `META-INF/services/ch.asit_asso.extract.plugins.common.ITaskProcessor` - Service registration
    - `plugins/fmeflowv2/lang/fr/`
      - `messages.properties` - French localization
      - `fmeFlowV2Help.html` - Comprehensive user documentation
    - `plugins/fmeserver/properties/`
      - `config.properties` - Plugin configuration with security options
  - `src/test/java/ch/asit_asso/extract/plugins/fmeflowv2/`
    - `FmeFlowV2PluginTest.java` - Unit tests for the new plugin

#### Main Project Updates
- Updated root `pom.xml` to include the new FME Flow v2 module

### Configuration Options

The plugin supports various configuration parameters:
- `fmeflow.server.url` - FME Flow server URL
- `fmeflow.auth.type` - Authentication type (token/basic)
- `fmeflow.auth.token` - API token for authentication
- `fmeflow.ssl.verify` - SSL certificate verification (true/false)
- `fmeflow.timeout.job` - Job execution timeout in seconds
- `fmeflow.timeout.poll` - Polling interval for job status

### Testing Recommendations

1. **Authentication Testing**:
   - Test token-based authentication
   - Verify basic authentication fallback
   - Test with invalid credentials

2. **Parameter Passing**:
   - Test with complex WKT geometries
   - Verify GeoJSON conversion accuracy
   - Test parameter inheritance and custom parameters

3. **Security Testing**:
   - Test HTTPS connections
   - Verify SSL certificate handling
   - Test with self-signed certificates

4. **Error Handling**:
   - Test network failures
   - Test invalid workspace/repository combinations
   - Test job timeout scenarios

5. **Performance Testing**:
   - Test with large parameter sets
   - Monitor job execution times
   - Test concurrent job submissions

### Benefits

- **Enhanced Security**: Modern authentication methods and SSL handling
- **Better Parameter Handling**: GeoJSON structure for complex data
- **Improved Reliability**: Enhanced error detection and reporting
- **Future-Proof**: Compatible with latest FME Flow versions
- **Better Monitoring**: Detailed job status tracking and logging

### Migration from V1

Users can migrate from the original FME Server plugin by:
1. Installing the new FME Flow V2 plugin
2. Updating configuration to use new parameter names
3. Testing workspace compatibility with GeoJSON parameters
4. Updating authentication credentials if using token-based auth

## Author
Extract Team

## Date
2025-09-15