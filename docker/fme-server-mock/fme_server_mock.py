#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Mock FME Server for functional and integration tests.

This Flask application simulates FME Server Data Download service.
It supports both V1 (Basic Auth, GET) and V2 (API Token, POST) plugin protocols.

V1 Protocol (FME Server Plugin):
- HTTP GET with query parameters
- Basic Authentication (username/password)
- Response format: {"serviceResponse": {"statusInfo": {"status": "success"}, "url": "..."}}

V2 Protocol (FME Server V2 Plugin):
- HTTP POST with JSON body (GeoJSON Feature)
- API Token Authentication (Authorization: fmetoken token=XXX)
- Query params: opt_responseformat=json, opt_servicemode=sync
- Response format: {"serviceResponse": {"statusInfo": {"status": "success"}, "url": "..."}}

Usage:
    python fme_server_mock.py [--port 8888]

Test credentials:
    V1: username=testuser, password=testpass
    V2: API Token starting with "valid_" (e.g., "valid_token_123")
"""

import argparse
import base64
import io
import json
import os
import zipfile
from datetime import datetime
from flask import Flask, request, jsonify, send_file, Response

app = Flask(__name__)

# Configuration
VALID_V1_USERNAME = "testuser"
VALID_V1_PASSWORD = "testpass"
VALID_TOKEN_PREFIX = "valid_"

# Expected parameters for V1
V1_EXPECTED_PARAMS = ["Product", "Perimeter", "FolderOut", "Parameters", "OrderLabel", "Request", "Client", "Organism"]

# Expected properties for V2
V2_EXPECTED_PROPERTIES = ["FolderOut", "OrderGuid", "OrderLabel", "Client", "ClientName", "Organism", "OrganismName",
                          "Product", "ProductLabel", "Parameters", "id"]


def validate_basic_auth(auth_header):
    """Validate Basic Authentication header."""
    if not auth_header or not auth_header.startswith("Basic "):
        return False, "Missing or invalid Authorization header"

    try:
        encoded = auth_header[6:]  # Remove "Basic "
        decoded = base64.b64decode(encoded).decode('utf-8')
        username, password = decoded.split(':', 1)

        if username == VALID_V1_USERNAME and password == VALID_V1_PASSWORD:
            return True, None
        else:
            return False, "Invalid credentials"
    except Exception as e:
        return False, f"Authentication error: {str(e)}"


def validate_token_auth(auth_header):
    """Validate FME Token Authentication header."""
    if not auth_header:
        return False, "Missing Authorization header"

    # Expected format: "fmetoken token=XXX"
    if not auth_header.startswith("fmetoken token="):
        return False, "Invalid token format. Expected: fmetoken token=XXX"

    token = auth_header.replace("fmetoken token=", "")

    if token.startswith(VALID_TOKEN_PREFIX):
        return True, None
    else:
        return False, "Invalid API token"


def create_result_zip(content_text="FME Server Mock Result"):
    """Create an in-memory ZIP file with a result file."""
    memory_file = io.BytesIO()
    with zipfile.ZipFile(memory_file, 'w', zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("result.txt", content_text)
        zf.writestr("metadata.json", json.dumps({
            "timestamp": datetime.now().isoformat(),
            "mock": True,
            "status": "success"
        }))
    memory_file.seek(0)
    return memory_file


def validate_v1_parameters(args):
    """Validate V1 query parameters."""
    missing = []
    for param in ["Product", "FolderOut"]:  # Required params
        if param not in args or not args.get(param):
            missing.append(param)

    if missing:
        return False, f"Missing required parameters: {', '.join(missing)}"

    return True, None


def validate_v2_geojson(data):
    """Validate V2 GeoJSON Feature structure."""
    if not isinstance(data, dict):
        return False, "Request body is not a JSON object"

    if data.get("type") != "Feature":
        return False, "GeoJSON type must be 'Feature'"

    if "geometry" not in data:
        return False, "Missing 'geometry' field"

    if "properties" not in data:
        return False, "Missing 'properties' field"

    properties = data.get("properties", {})

    # Check required properties
    if "FolderOut" not in properties:
        return False, "Missing 'FolderOut' in properties"

    if "Parameters" not in properties:
        return False, "Missing 'Parameters' in properties"

    return True, None


@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint."""
    return jsonify({"status": "healthy", "service": "FME Server Mock"})


@app.route('/fmeserver/v1/datadownload', methods=['GET'])
@app.route('/fmedatadownload/Repositories/<path:repo_path>', methods=['GET'])
def fme_server_v1(repo_path=None):
    """
    FME Server V1 endpoint (GET with Basic Auth).

    Query parameters:
    - opt_responseformat=json
    - Product, Perimeter, FolderOut, Parameters, OrderLabel, Request, Client, Organism
    """
    print(f"\n=== FME Server V1 Request ===")
    print(f"Path: {request.path}")
    print(f"Args: {dict(request.args)}")

    # Validate authentication
    auth_header = request.headers.get('Authorization')
    is_valid, error = validate_basic_auth(auth_header)

    if not is_valid:
        print(f"Auth failed: {error}")
        response = jsonify({
            "serviceResponse": {
                "statusInfo": {
                    "status": "failure",
                    "message": error
                }
            }
        })
        response.headers['WWW-Authenticate'] = 'Basic realm="FME Server"'
        return response, 401

    # Validate parameters
    is_valid, error = validate_v1_parameters(request.args)
    if not is_valid:
        print(f"Param validation failed: {error}")
        return jsonify({
            "serviceResponse": {
                "statusInfo": {
                    "status": "failure",
                    "message": error
                }
            }
        }), 400

    # Log received parameters
    print("Received parameters:")
    for param in V1_EXPECTED_PARAMS:
        value = request.args.get(param, "N/A")
        # Truncate long values
        if len(str(value)) > 100:
            value = str(value)[:100] + "..."
        print(f"  {param}: {value}")

    # Generate download URL
    download_url = f"{request.host_url}download/v1/{datetime.now().strftime('%Y%m%d_%H%M%S')}.zip"

    print(f"Success! Download URL: {download_url}")

    return jsonify({
        "serviceResponse": {
            "statusInfo": {
                "status": "success"
            },
            "url": download_url
        }
    })


@app.route('/fmeserver/v2/datadownload', methods=['POST'])
@app.route('/fmedatadownload/v2/<path:service_path>', methods=['POST'])
def fme_server_v2(service_path=None):
    """
    FME Server V2 endpoint (POST with API Token).

    Query parameters:
    - opt_responseformat=json
    - opt_servicemode=sync

    Body: GeoJSON Feature with geometry and properties
    """
    print(f"\n=== FME Server V2 Request ===")
    print(f"Path: {request.path}")
    print(f"Query params: {dict(request.args)}")
    print(f"Content-Type: {request.content_type}")

    # Validate authentication
    auth_header = request.headers.get('Authorization')
    is_valid, error = validate_token_auth(auth_header)

    if not is_valid:
        print(f"Auth failed: {error}")
        return jsonify({
            "serviceResponse": {
                "statusInfo": {
                    "status": "failure",
                    "message": error
                }
            }
        }), 401

    # Check response format
    if request.args.get('opt_responseformat') != 'json':
        print("Warning: opt_responseformat is not 'json'")

    # Parse request body
    try:
        data = request.get_json(force=True)
    except Exception as e:
        print(f"JSON parse error: {e}")
        return jsonify({
            "serviceResponse": {
                "statusInfo": {
                    "status": "failure",
                    "message": f"Invalid JSON body: {str(e)}"
                }
            }
        }), 400

    # Validate GeoJSON
    is_valid, error = validate_v2_geojson(data)
    if not is_valid:
        print(f"GeoJSON validation failed: {error}")
        return jsonify({
            "serviceResponse": {
                "statusInfo": {
                    "status": "failure",
                    "message": error
                }
            }
        }), 400

    # Log received data
    print("Received GeoJSON Feature:")
    print(f"  Type: {data.get('type')}")

    geometry = data.get('geometry')
    if geometry:
        print(f"  Geometry type: {geometry.get('type')}")
    else:
        print("  Geometry: null")

    properties = data.get('properties', {})
    print("  Properties:")
    for prop in V2_EXPECTED_PROPERTIES:
        value = properties.get(prop, "N/A")
        if isinstance(value, dict):
            value = json.dumps(value)[:50] + "..."
        elif isinstance(value, str) and len(value) > 50:
            value = value[:50] + "..."
        print(f"    {prop}: {value}")

    # Generate download URL
    download_url = f"{request.host_url}download/v2/{datetime.now().strftime('%Y%m%d_%H%M%S')}.zip"

    print(f"Success! Download URL: {download_url}")

    return jsonify({
        "serviceResponse": {
            "statusInfo": {
                "status": "success"
            },
            "url": download_url
        }
    })


@app.route('/download/<version>/<filename>', methods=['GET'])
def download_file(version, filename):
    """Endpoint to download the generated result file."""
    print(f"\n=== Download Request ===")
    print(f"Version: {version}, Filename: {filename}")

    # Create ZIP content based on version
    content = f"FME Server Mock Result\nVersion: {version}\nFilename: {filename}\nTimestamp: {datetime.now().isoformat()}"
    zip_file = create_result_zip(content)

    return send_file(
        zip_file,
        mimetype='application/zip',
        as_attachment=True,
        download_name=filename
    )


@app.route('/fmeserver/error/test', methods=['GET', 'POST'])
def error_endpoint():
    """Endpoint that always returns an error for testing."""
    return jsonify({
        "serviceResponse": {
            "statusInfo": {
                "status": "failure",
                "message": "Simulated FME Server error"
            }
        }
    }), 500


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='FME Server Mock')
    parser.add_argument('--port', type=int, default=8888, help='Port to run on')
    parser.add_argument('--host', default='0.0.0.0', help='Host to bind to')
    args = parser.parse_args()

    print(f"Starting FME Server Mock on {args.host}:{args.port}")
    print(f"V1 credentials: {VALID_V1_USERNAME}/{VALID_V1_PASSWORD}")
    print(f"V2 token prefix: {VALID_TOKEN_PREFIX}")

    app.run(host=args.host, port=args.port, debug=True)
