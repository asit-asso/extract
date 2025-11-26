#!/usr/bin/env python3
"""
Test script that verifies GeoJSON format and geometry conversion
"""
import sys
import json
import os

params_file = os.path.join(os.environ.get('FOLDER_IN', '.'), 'parameters.json')

try:
    with open(params_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    geometry = data.get('geometry', {})
    geom_type = geometry.get('type')
    coordinates = geometry.get('coordinates')

    print(f"Geometry type: {geom_type}")
    print(f"Has coordinates: {coordinates is not None}")

    # Verify coordinates structure based on type
    if geom_type == 'Polygon':
        if not isinstance(coordinates, list) or len(coordinates) == 0:
            print("ERROR: Invalid Polygon coordinates", file=sys.stderr)
            sys.exit(1)
        print(f"Polygon rings: {len(coordinates)}")

    elif geom_type == 'MultiPolygon':
        if not isinstance(coordinates, list) or len(coordinates) == 0:
            print("ERROR: Invalid MultiPolygon coordinates", file=sys.stderr)
            sys.exit(1)
        print(f"MultiPolygon polygons: {len(coordinates)}")

    elif geom_type == 'Point':
        if not isinstance(coordinates, list) or len(coordinates) != 2:
            print("ERROR: Invalid Point coordinates", file=sys.stderr)
            sys.exit(1)
        print(f"Point: {coordinates}")

    elif geom_type == 'LineString':
        if not isinstance(coordinates, list) or len(coordinates) < 2:
            print("ERROR: Invalid LineString coordinates", file=sys.stderr)
            sys.exit(1)
        print(f"LineString points: {len(coordinates)}")

    print("SUCCESS: Valid GeoJSON geometry")
    sys.exit(0)

except Exception as e:
    print(f"ERROR: {e}", file=sys.stderr)
    sys.exit(1)
