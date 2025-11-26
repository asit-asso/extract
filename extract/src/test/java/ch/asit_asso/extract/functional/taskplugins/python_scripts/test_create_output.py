#!/usr/bin/env python3
"""
Test script that creates an output file in FolderOut
"""
import sys
import os

folder_out = os.environ.get('FOLDER_OUT', '.')
output_file = os.path.join(folder_out, 'result.txt')

try:
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("Test output from Python script\n")
        f.write("Success!\n")

    print(f"Created output file: {output_file}")
    sys.exit(0)

except Exception as e:
    print(f"ERROR: Failed to create output file: {e}", file=sys.stderr)
    sys.exit(1)
