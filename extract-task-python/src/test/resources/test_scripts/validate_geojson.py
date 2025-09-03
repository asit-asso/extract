#!/usr/bin/env python3
"""
Test script that validates the GeoJSON structure in detail
"""

import json
import sys
import os

def validate_geometry(geometry):
    """Validate GeoJSON geometry object"""
    if geometry is None:
        print("Geometry is null (valid)")
        return True
    
    if not isinstance(geometry, dict):
        print(f"Error: Geometry is not a dict: {type(geometry)}")
        return False
    
    if 'type' not in geometry:
        print("Error: Geometry missing 'type' field")
        return False
    
    geom_type = geometry['type']
    valid_types = ['Point', 'LineString', 'Polygon', 'MultiPoint', 
                   'MultiLineString', 'MultiPolygon', 'GeometryCollection']
    
    if geom_type not in valid_types:
        print(f"Error: Invalid geometry type: {geom_type}")
        return False
    
    if 'coordinates' not in geometry:
        print("Error: Geometry missing 'coordinates' field")
        return False
    
    print(f"Geometry type: {geom_type}")
    return True

def validate_properties(properties):
    """Validate required properties"""
    required_fields = [
        'RequestId', 'FolderOut', 'FolderIn',
        'OrderGuid', 'OrderLabel',
        'ClientGuid', 'ClientName',
        'OrganismGuid', 'OrganismName',
        'ProductGuid', 'ProductLabel',
        'Parameters'
    ]
    
    missing_fields = []
    for field in required_fields:
        if field not in properties:
            missing_fields.append(field)
    
    if missing_fields:
        print(f"Warning: Missing fields: {missing_fields}")
    
    # Check Parameters is an object
    if 'Parameters' in properties:
        params = properties['Parameters']
        if not isinstance(params, (dict, str)):
            print(f"Error: Parameters should be object or string, got {type(params)}")
            return False
        print(f"Parameters type: {type(params)}")
        if isinstance(params, dict):
            print(f"Parameters content: {json.dumps(params, indent=2)}")
    
    return len(missing_fields) == 0

def main():
    if len(sys.argv) < 2:
        print("Error: No parameters file provided")
        sys.exit(1)
    
    parameters_file = sys.argv[1]
    
    print(f"Reading parameters file: {parameters_file}")
    
    try:
        with open(parameters_file, 'r') as f:
            content = f.read()
            print(f"File size: {len(content)} bytes")
            feature = json.loads(content)
    except json.JSONDecodeError as e:
        print(f"Error: Invalid JSON: {e}")
        sys.exit(1)
    except FileNotFoundError:
        print(f"Error: File not found: {parameters_file}")
        sys.exit(1)
    
    # Validate it's a Feature
    if feature.get('type') != 'Feature':
        print(f"Error: Expected type 'Feature', got '{feature.get('type')}'")
        sys.exit(1)
    
    print("✓ Valid GeoJSON Feature")
    
    # Validate geometry
    if 'geometry' not in feature:
        print("Error: Missing 'geometry' field")
        sys.exit(1)
    
    if not validate_geometry(feature['geometry']):
        sys.exit(1)
    
    print("✓ Valid geometry")
    
    # Validate properties
    if 'properties' not in feature:
        print("Error: Missing 'properties' field")
        sys.exit(1)
    
    if not isinstance(feature['properties'], dict):
        print(f"Error: Properties is not a dict: {type(feature['properties'])}")
        sys.exit(1)
    
    if not validate_properties(feature['properties']):
        print("⚠ Some required properties are missing")
    else:
        print("✓ All required properties present")
    
    # Create validation report
    properties = feature['properties']
    output_dir = properties.get('FolderOut')
    if output_dir:
        report_file = os.path.join(output_dir, 'validation_report.json')
        report = {
            'valid': True,
            'feature_type': feature.get('type'),
            'geometry_type': feature['geometry'].get('type') if feature['geometry'] else None,
            'properties_count': len(properties),
            'has_parameters': 'Parameters' in properties,
            'parameters_type': type(properties.get('Parameters')).__name__ if 'Parameters' in properties else None,
            'request_id': properties.get('RequestId'),
            'validation_timestamp': __import__('datetime').datetime.now().isoformat()
        }
        
        with open(report_file, 'w') as f:
            json.dump(report, f, indent=2)
        print(f"\n✓ Validation report written to {report_file}")
    
    print("\n✓ GeoJSON validation completed successfully")
    sys.exit(0)

if __name__ == "__main__":
    main()