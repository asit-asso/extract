#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Mock FME Desktop V1 executable for functional tests.

This script validates command-line parameters used by FME Desktop V1 plugin.
It accepts the same parameters as the real FME Desktop and validates them.

Usage:
    python FmeDesktopV1Test.py workspace.fmw --Perimeter "WKT" --Product "GUID" --FolderOut "/path" ...

Exit codes:
    0   - Success
    10  - No workspace provided
    15  - Invalid workspace path
    20  - Missing --FolderOut parameter
    25  - Missing --Perimeter parameter (warning only, continues)
    30  - Missing --Product parameter (warning only, continues)
    35  - FolderOut does not exist
    100 - Simulated FME error (workspace ends with _fails)
"""

import json
import os
import sys
from pathlib import Path


def parse_arguments(args):
    """Parse command-line arguments in FME style (--key value)."""
    params = {}
    i = 0
    while i < len(args):
        arg = args[i]
        if arg.startswith("--") and i + 1 < len(args):
            key = arg[2:]  # Remove --
            value = args[i + 1]
            params[key] = value
            i += 2
        else:
            i += 1
    return params


def main():
    print("FME Desktop V1 Mock - Reading {} arguments".format(len(sys.argv)))
    print("Arguments: {}".format(sys.argv))

    if len(sys.argv) < 2:
        sys.stderr.write("No FME workspace provided\n")
        sys.exit(10)

    workspace_path = sys.argv[1]

    if not workspace_path or workspace_path.startswith("--"):
        sys.stderr.write("The FME workspace path is invalid\n")
        sys.exit(15)

    print("Workspace: {}".format(workspace_path))

    # Parse remaining arguments
    params = parse_arguments(sys.argv[2:])

    print("Parsed parameters:")
    for key, value in params.items():
        # Truncate long values for display
        display_value = value[:100] + "..." if len(value) > 100 else value
        print("  {}: {}".format(key, display_value))

    # Validate required parameters
    folder_out = params.get("FolderOut")
    if not folder_out:
        sys.stderr.write("Missing --FolderOut parameter\n")
        sys.exit(20)

    perimeter = params.get("Perimeter")
    if not perimeter:
        print("Warning: No --Perimeter parameter provided (may be intentional)")

    product = params.get("Product")
    if not product:
        print("Warning: No --Product parameter provided")

    # Validate FolderOut exists
    if not os.path.isdir(folder_out):
        sys.stderr.write("FolderOut does not exist: {}\n".format(folder_out))
        sys.exit(35)

    # Check for other expected parameters
    order_label = params.get("OrderLabel", "unknown")
    client_guid = params.get("Client", "unknown")
    organism_guid = params.get("Organism", "unknown")
    request_id = params.get("Request", "unknown")
    parameters_json = params.get("Parameters", "{}")

    print("Order Label: {}".format(order_label))
    print("Client GUID: {}".format(client_guid))
    print("Organism GUID: {}".format(organism_guid))
    print("Request ID: {}".format(request_id))

    # Validate JSON parameters if provided
    if parameters_json and parameters_json != "{}":
        try:
            json_params = json.loads(parameters_json)
            print("Custom parameters count: {}".format(len(json_params)))
        except json.JSONDecodeError as e:
            print("Warning: Parameters is not valid JSON: {}".format(str(e)))

    # Simulate FME error if workspace ends with _fails
    workspace_stem = Path(workspace_path).stem
    if workspace_stem.endswith("_fails"):
        sys.stderr.write("The FME workspace resulted in an error (simulated)\n")
        sys.exit(100)

    # Create output file unless workspace ends with _nofiles
    if not workspace_stem.endswith("_nofiles"):
        output_file_path = os.path.join(folder_out, "result_v1.txt")
        with open(output_file_path, 'w', encoding='utf-8') as f:
            f.write("FME Desktop V1 Mock - Execution successful\n")
            f.write("Workspace: {}\n".format(workspace_path))
            f.write("FolderOut: {}\n".format(folder_out))
            f.write("Product: {}\n".format(product or "null"))
            f.write("OrderLabel: {}\n".format(order_label))
            f.write("ClientGuid: {}\n".format(client_guid))
            f.write("OrganismGuid: {}\n".format(organism_guid))
            f.write("RequestId: {}\n".format(request_id))
            if perimeter:
                # Truncate perimeter for file
                f.write("Perimeter: {}\n".format(perimeter[:200] + "..." if len(perimeter) > 200 else perimeter))
        print("Created output file: {}".format(output_file_path))
    else:
        print("Workspace configured to produce no files")

    print("FME Desktop V1 Mock - Execution completed successfully")
    sys.exit(0)


if __name__ == "__main__":
    main()
