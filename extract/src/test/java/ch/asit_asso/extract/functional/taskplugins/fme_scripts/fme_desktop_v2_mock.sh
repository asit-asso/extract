#!/bin/bash
# Mock FME Desktop V2 executable wrapper script
# This script calls the Python mock and passes all arguments

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$SCRIPT_DIR/FmeDesktopV2Test.py"

# Find Python interpreter
if command -v python3 &> /dev/null; then
    PYTHON=python3
elif command -v python &> /dev/null; then
    PYTHON=python
else
    echo "Python not found" >&2
    exit 1
fi

# Execute the Python script with all passed arguments
exec "$PYTHON" "$PYTHON_SCRIPT" "$@"
