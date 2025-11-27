#!/usr/bin/env python3
"""
Test script that creates an output file in FolderOut
The plugin passes the parameters file path as the first argument.
FolderOut is read from the properties in parameters.json.
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
    # Read parameters to get FolderOut
    with open(params_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    properties = data.get('properties', {})
    folder_out = properties.get('FolderOut')

    if not folder_out:
        print("ERROR: FolderOut not found in parameters", file=sys.stderr)
        sys.exit(1)

    output_file = os.path.join(folder_out, 'result.txt')

    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("Test output from Python script\n")
        f.write("Success!\n")

    print(f"Created output file: {output_file}")
    sys.exit(0)

except Exception as e:
    print(f"ERROR: Failed to create output file: {e}", file=sys.stderr)
    sys.exit(1)
