#!/usr/bin/env python3
"""
Test script that reads and validates parameters.json file
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
if not os.path.exists(params_file):
    print(f"ERROR: parameters.json not found at {params_file}", file=sys.stderr)
    sys.exit(1)

# Read and parse JSON
try:
    with open(params_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    # Verify it's valid GeoJSON Feature
    if data.get('type') != 'Feature':
        print("ERROR: Not a valid GeoJSON Feature", file=sys.stderr)
        sys.exit(1)

    # Verify it has geometry
    if 'geometry' not in data:
        print("ERROR: Missing geometry in GeoJSON", file=sys.stderr)
        sys.exit(1)

    # Verify it has properties
    if 'properties' not in data:
        print("ERROR: Missing properties in GeoJSON", file=sys.stderr)
        sys.exit(1)

    print(f"SUCCESS: Read valid GeoJSON with geometry type: {data['geometry'].get('type', 'unknown')}")
    print(f"Properties count: {len(data['properties'])}")
    sys.exit(0)

except json.JSONDecodeError as e:
    print(f"ERROR: Invalid JSON: {e}", file=sys.stderr)
    sys.exit(1)
except Exception as e:
    print(f"ERROR: {e}", file=sys.stderr)
    sys.exit(1)
