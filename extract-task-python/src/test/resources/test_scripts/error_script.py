#!/usr/bin/env python3
"""
Test script that fails with various error types
"""

import sys

# Choose error type based on first argument after parameters file
error_type = sys.argv[2] if len(sys.argv) > 2 else "general"

if error_type == "syntax":
    # This will cause a SyntaxError
    eval("print('unclosed string)")
elif error_type == "import":
    # This will cause an ImportError
    import non_existent_module
elif error_type == "name":
    # This will cause a NameError
    print(undefined_variable)
elif error_type == "file":
    # This will cause a FileNotFoundError
    with open('/non/existent/file.txt', 'r') as f:
        content = f.read()
elif error_type == "permission":
    # This will cause a PermissionError
    import os
    os.chmod('/etc/passwd', 0o777)
elif error_type == "indent":
    # This will cause an IndentationError
    exec("""
def test():
print("bad indent")
""")
elif error_type == "zero":
    # This will cause a ZeroDivisionError
    result = 10 / 0
else:
    # General error with exit code 1
    print("Error: Test script failed intentionally")
    sys.exit(1)