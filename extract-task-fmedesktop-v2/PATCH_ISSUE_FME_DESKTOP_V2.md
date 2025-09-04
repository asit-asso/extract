# PATCH ISSUE: FME Desktop V2 Task Processor

## Issue
Implementation of a new FME Desktop task processor (v2) that overcomes command line length limitations by passing parameters through a GeoJSON file instead of command line arguments.

## Problem Solved
The original FME Desktop plugin passes all parameters as command line arguments, which can exceed system limits when dealing with complex geometries or large parameter sets. This v2 implementation solves this by:
- Creating a parameters.json file in GeoJSON format
- Passing only the file path to FME via --PARAM_FILE argument
- Automatically converting WKT geometries to GeoJSON format

## Implementation Details

### Key Features
1. **GeoJSON Parameter File**: All parameters are written to a GeoJSON Feature file containing:
   - `geometry`: The perimeter converted from WKT to GeoJSON
   - `properties`: All request parameters as key-value pairs

2. **Parameter Structure**:
   ```json
   {
     "type": "Feature",
     "geometry": { /* GeoJSON geometry */ },
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

3. **FME Integration**: 
   - FME workspace receives the file path via `$(PARAM_FILE)` parameter
   - Can read the file with GeoJSON Reader or FeatureReader
   - All attributes are directly accessible
   - Geometry is automatically imported

### Files Modified/Created

#### New Module Structure
- `extract-task-fmedesktop-v2/`
  - `pom.xml` - Maven configuration
  - `src/main/java/ch/asit_asso/extract/plugins/fmedesktopv2/`
    - `FmeDesktopV2Plugin.java` - Main plugin implementation with GeoJSON creation
    - `FmeDesktopV2Result.java` - Result handler
    - `LocalizedMessages.java` - Internationalization support
    - `PluginConfiguration.java` - Configuration loader
    - `InstancePool.java` - FME instance management
  - `src/main/resources/`
    - `META-INF/services/ch.asit_asso.extract.plugins.common.ITaskProcessor` - Service loader configuration
    - `plugins/fmedesktopv2/lang/fr/`
      - `messages.properties` - French messages
      - `fmeDesktopV2Help.html` - User documentation explaining GeoJSON usage
    - `plugins/fmedesktopv2/properties/`
      - `configFME.properties` - Plugin configuration

#### Main Project Updates
- Updated root `pom.xml` to include the new module in reactor build

### Testing Recommendations
1. Test with large/complex WKT geometries that would exceed command line limits
2. Verify GeoJSON file creation and format
3. Test FME workspace integration with parameter file reading
4. Validate all parameter types are correctly passed
5. Test error handling for malformed geometries

### Benefits
- No command line length limitations
- Cleaner parameter passing
- Better geometry handling with native GeoJSON
- Easier debugging with readable parameter file
- Compatible with modern FME workflows

## Author
Extract Team

## Date
2025-09-04