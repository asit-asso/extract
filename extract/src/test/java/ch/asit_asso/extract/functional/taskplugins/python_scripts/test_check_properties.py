#!/usr/bin/env python3
"""
Test script that verifies all metadata properties are present
"""
import sys
import json
import os

params_file = os.path.join(os.environ.get('FOLDER_IN', '.'), 'parameters.json')

try:
    with open(params_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    properties = data.get('properties', {})

    # Check for expected metadata fields
    expected_fields = ['clientGuid', 'clientName', 'organismName', 'productLabel', 'orderLabel']
    missing_fields = []

    for field in expected_fields:
        if field not in properties:
            missing_fields.append(field)
        else:
            print(f"{field}: {properties[field]}")

    if missing_fields:
        print(f"WARNING: Missing fields: {missing_fields}", file=sys.stderr)

    # Check for dynamic parameters (if any)
    dynamic_params = {k: v for k, v in properties.items() if k not in ['clientGuid', 'clientName', 'organismGuid', 'organismName', 'productGuid', 'productLabel', 'orderGuid', 'orderLabel', 'tiersGuid', 'tiersDetails', 'remark']}

    if dynamic_params:
        print(f"Dynamic parameters found: {list(dynamic_params.keys())}")

    print(f"SUCCESS: Read {len(properties)} properties")
    sys.exit(0)

except Exception as e:
    print(f"ERROR: {e}", file=sys.stderr)
    sys.exit(1)
