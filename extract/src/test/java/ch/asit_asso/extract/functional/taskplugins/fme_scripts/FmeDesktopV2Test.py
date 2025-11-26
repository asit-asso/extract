#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Mock FME Desktop V2 executable for functional tests.

This script validates the parameters.json file format (GeoJSON Feature) used by FME Desktop V2 plugin (Issue #347).
It replaces command-line parameter passing with JSON file reading.

Usage:
    python FmeDesktopV2Test.py workspace.fmw --parametersFile /path/to/parameters.json

Exit codes:
    0   - Success
    10  - No workspace provided
    15  - Invalid workspace path
    20  - No --parametersFile argument
    25  - Parameters file not found
    30  - Invalid JSON in parameters file
    35  - Invalid GeoJSON structure
    40  - Missing required property
    45  - Invalid geometry
    100 - Simulated FME error (workspace ends with _fails)
"""

import json
import os
import sys
from pathlib import Path


def validate_geojson_geometry(geometry):
    """Validates GeoJSON geometry structure."""
    if geometry is None:
        return True  # Null geometry is valid

    if not isinstance(geometry, dict):
        return False

    geom_type = geometry.get("type")
    coordinates = geometry.get("coordinates")

    valid_types = ["Point", "LineString", "Polygon", "MultiPoint", "MultiLineString", "MultiPolygon"]

    if geom_type not in valid_types:
        return False

    if coordinates is None:
        return False

    return True


def validate_properties(properties):
    """Validates required properties exist."""
    if not isinstance(properties, dict):
        return False, "Properties is not an object"

    # Check for some expected properties (may vary based on config)
    # At minimum, we should have the Parameters nested object
    if "Parameters" not in properties:
        return False, "Missing 'Parameters' in properties"

    return True, ""


def validate_geojson_feature(data):
    """Validates GeoJSON Feature structure."""
    if not isinstance(data, dict):
        return False, "Root is not an object"

    # Check type
    if data.get("type") != "Feature":
        return False, "Type is not 'Feature'"

    # Check geometry exists (can be null)
    if "geometry" not in data:
        return False, "Missing 'geometry' field"

    # Validate geometry if not null
    geometry = data.get("geometry")
    if geometry is not None and not validate_geojson_geometry(geometry):
        return False, "Invalid geometry structure"

    # Check properties
    if "properties" not in data:
        return False, "Missing 'properties' field"

    is_valid, error = validate_properties(data.get("properties"))
    if not is_valid:
        return False, error

    return True, ""


def main():
    print("FME Desktop V2 Mock - Reading {} arguments: {}".format(len(sys.argv), sys.argv))

    if len(sys.argv) < 2:
        sys.stderr.write("No FME workspace provided\n")
        sys.exit(10)

    workspace_path = sys.argv[1]

    if not workspace_path:
        sys.stderr.write("The FME workspace path is invalid\n")
        sys.exit(15)

    # Parse remaining arguments looking for --parametersFile
    parameters_file_path = None
    i = 2
    while i < len(sys.argv):
        arg = sys.argv[i]
        if arg == "--parametersFile" and i + 1 < len(sys.argv):
            parameters_file_path = sys.argv[i + 1]
            i += 2
        else:
            i += 1

    if parameters_file_path is None:
        sys.stderr.write("No --parametersFile argument provided\n")
        sys.exit(20)

    print("Parameters file: {}".format(parameters_file_path))

    # Check if parameters file exists
    if not os.path.isfile(parameters_file_path):
        sys.stderr.write("Parameters file not found: {}\n".format(parameters_file_path))
        sys.exit(25)

    # Read and parse JSON
    try:
        with open(parameters_file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        print("Successfully parsed parameters.json")
    except json.JSONDecodeError as e:
        sys.stderr.write("Invalid JSON in parameters file: {}\n".format(str(e)))
        sys.exit(30)

    # Validate GeoJSON Feature structure
    is_valid, error_message = validate_geojson_feature(data)
    if not is_valid:
        sys.stderr.write("Invalid GeoJSON Feature structure: {}\n".format(error_message))
        sys.exit(35)

    print("GeoJSON Feature structure is valid")

    # Extract FolderOut from properties
    properties = data.get("properties", {})
    folder_out = properties.get("FolderOut")

    if not folder_out:
        sys.stderr.write("Missing FolderOut in properties\n")
        sys.exit(40)

    print("FolderOut: {}".format(folder_out))

    # Print geometry info
    geometry = data.get("geometry")
    if geometry:
        print("Geometry type: {}".format(geometry.get("type")))
    else:
        print("Geometry: null")

    # Print parameters info
    params = properties.get("Parameters", {})
    print("Parameters count: {}".format(len(params)))

    # Simulate FME error if workspace ends with _fails
    workspace_stem = Path(workspace_path).stem
    if workspace_stem.endswith("_fails"):
        sys.stderr.write("The FME workspace resulted in an error (simulated)\n")
        sys.exit(100)

    # Create output file unless workspace ends with _nofiles
    if not workspace_stem.endswith("_nofiles"):
        if os.path.isdir(folder_out):
            output_file_path = os.path.join(folder_out, "result_v2.txt")
            with open(output_file_path, 'w', encoding='utf-8') as f:
                f.write("FME Desktop V2 Mock - Execution successful\n")
                f.write("Workspace: {}\n".format(workspace_path))
                f.write("Parameters file: {}\n".format(parameters_file_path))
                if geometry:
                    f.write("Geometry type: {}\n".format(geometry.get("type")))
            print("Created output file: {}".format(output_file_path))
        else:
            print("Warning: FolderOut does not exist: {}".format(folder_out))

    print("FME Desktop V2 Mock - Execution completed successfully")
    sys.exit(0)


if __name__ == "__main__":
    main()
