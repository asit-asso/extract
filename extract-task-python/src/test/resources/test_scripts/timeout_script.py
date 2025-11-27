#!/usr/bin/env python3
"""
Test script that runs indefinitely to test timeout
"""

import time
import sys

print("Starting long-running process...")
sys.stdout.flush()

# Sleep for 10 minutes (longer than the 5-minute timeout)
time.sleep(600)

print("This should never be printed due to timeout")
sys.exit(0)