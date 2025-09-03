#!/usr/bin/env python3
"""
Test script that succeeds - validates parameters and creates output
"""

import json
import sys
import os

def main():
    if len(sys.argv) < 2:
        print("Error: No parameters file provided")
        sys.exit(1)
    
    parameters_file = sys.argv[1]
    
    # Read and validate the GeoJSON Feature
    with open(parameters_file, 'r') as f:
        feature = json.load(f)
    
    # Validate structure
    assert feature.get('type') == 'Feature', "Not a valid GeoJSON Feature"
    assert 'properties' in feature, "Missing properties"
    assert 'geometry' in feature, "Missing geometry"
    
    properties = feature['properties']
    
    # Validate required properties
    assert 'RequestId' in properties, "Missing RequestId"
    assert 'FolderOut' in properties, "Missing FolderOut"
    assert 'Parameters' in properties, "Missing Parameters object"
    
    # Create output file
    output_dir = properties.get('FolderOut')
    if output_dir:
        output_file = os.path.join(output_dir, 'test_output.json')
        result = {
            'status': 'SUCCESS',
            'request_id': properties.get('RequestId'),
            'message': 'Test completed successfully'
        }
        with open(output_file, 'w') as f:
            json.dump(result, f, indent=2)
        print(f"Output written to {output_file}")
    
    print("Test script executed successfully")
    sys.exit(0)

if __name__ == "__main__":
    main()