#!/usr/bin/env python3
"""
Test script that fails (exit code 1)
"""
import sys

print("Script failed intentionally", file=sys.stderr)
sys.exit(1)
