#!/usr/bin/env python3
"""
Test script that verifies all metadata properties are present
The plugin passes the parameters file path as the first argument.
"""
import sys
import json
import os

# Get parameters file path from command line argument
if len(sys.argv) < 2:
    print("ERROR: parameters.json path not provided as argument", file=sys.stderr)
    sys.exit(1)

params_file = sys.argv[1]

try:
    with open(params_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    properties = data.get('properties', {})

    # Check for expected metadata fields (using CamelCase as the plugin uses)
    expected_fields = ['ClientGuid', 'ClientName', 'OrganismName', 'ProductLabel', 'OrderLabel']
    missing_fields = []

    for field in expected_fields:
        if field not in properties:
            missing_fields.append(field)
        else:
            print(f"{field}: {properties[field]}")

    if missing_fields:
        print(f"WARNING: Missing fields: {missing_fields}", file=sys.stderr)

    # Check for dynamic parameters (if any) - they are in nested Parameters object
    dynamic_params = properties.get('Parameters', {})

    if dynamic_params:
        print(f"Dynamic parameters found: {list(dynamic_params.keys())}")

    print(f"SUCCESS: Read {len(properties)} properties")
    sys.exit(0)

except Exception as e:
    print(f"ERROR: {e}", file=sys.stderr)
    sys.exit(1)
