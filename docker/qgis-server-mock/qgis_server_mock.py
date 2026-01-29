#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Mock QGIS Server for functional and integration tests.

This Flask application simulates QGIS Server WMS/WFS services for testing
the QGIS Print Atlas plugin.

Supported requests:
1. GetProjectSettings (WMS) - Returns XML with ComposerTemplate and atlasCoverageLayer
2. GetFeature (WFS) - Returns XML with feature IDs based on spatial filter
3. GetPrint (WMS) - Returns a PDF file

Test credentials:
    username=qgisuser, password=qgispass

Usage:
    python qgis_server_mock.py [--port 8889]
"""

import argparse
import base64
import io
import os
from datetime import datetime
from flask import Flask, request, Response, send_file

app = Flask(__name__)

# Configuration
VALID_USERNAME = "qgisuser"
VALID_PASSWORD = "qgispass"

# Sample project settings response
PROJECT_SETTINGS_TEMPLATE = '''<?xml version="1.0" encoding="UTF-8"?>
<WMS_Capabilities version="1.3.0">
  <Service>
    <Name>WMS</Name>
    <Title>QGIS Server Mock</Title>
  </Service>
  <Capability>
    <ComposerTemplates>
      <ComposerTemplate name="{template_name}" atlasCoverageLayer="{coverage_layer}" width="297" height="210">
        <ComposerMap name="map0" width="277" height="190"/>
      </ComposerTemplate>
      <ComposerTemplate name="Atlas" atlasCoverageLayer="parcels" width="297" height="210">
        <ComposerMap name="map0" width="277" height="190"/>
      </ComposerTemplate>
    </ComposerTemplates>
    <Layer queryable="1">
      <Name>{coverage_layer}</Name>
      <Title>Coverage Layer</Title>
    </Layer>
  </Capability>
</WMS_Capabilities>'''

# Sample GetFeature response
# The XPath used by plugin is: /FeatureCollection/featureMember/%s/@id
# So we need to use featureMember (not wfs:member) and @id attribute
GET_FEATURE_RESPONSE_TEMPLATE = '''<?xml version="1.0" encoding="UTF-8"?>
<FeatureCollection xmlns:gml="http://www.opengis.net/gml/3.2"
                   numberMatched="{num_features}" numberReturned="{num_features}">
{feature_members}
</FeatureCollection>'''

FEATURE_MEMBER_TEMPLATE = '''  <featureMember>
    <{layer_name} id="{layer_name}.{feature_id}">
      <gml:boundedBy>
        <gml:Envelope srsName="EPSG:2056">
          <gml:lowerCorner>{x1} {y1}</gml:lowerCorner>
          <gml:upperCorner>{x2} {y2}</gml:upperCorner>
        </gml:Envelope>
      </gml:boundedBy>
      <geometry>
        <gml:Polygon srsName="EPSG:2056">
          <gml:exterior>
            <gml:LinearRing>
              <gml:posList>{coords}</gml:posList>
            </gml:LinearRing>
          </gml:exterior>
        </gml:Polygon>
      </geometry>
    </{layer_name}>
  </featureMember>'''

# Service Exception template
SERVICE_EXCEPTION_TEMPLATE = '''<?xml version="1.0" encoding="UTF-8"?>
<ServiceExceptionReport version="1.3.0">
  <ServiceException code="{code}">{message}</ServiceException>
</ServiceExceptionReport>'''


def validate_basic_auth(auth_header):
    """Validate Basic Authentication header (optional for QGIS)."""
    if not auth_header:
        return True, None  # Auth is optional

    if not auth_header.startswith("Basic "):
        return True, None  # No auth provided is OK

    try:
        encoded = auth_header[6:]
        decoded = base64.b64decode(encoded).decode('utf-8')
        username, password = decoded.split(':', 1)

        if username == VALID_USERNAME and password == VALID_PASSWORD:
            return True, None
        else:
            return False, "Invalid credentials"
    except Exception as e:
        return False, f"Authentication error: {str(e)}"


def create_pdf():
    """Create a minimal PDF file for testing."""
    # Minimal PDF structure
    pdf_content = b"""%PDF-1.4
1 0 obj
<< /Type /Catalog /Pages 2 0 R >>
endobj
2 0 obj
<< /Type /Pages /Kids [3 0 R] /Count 1 >>
endobj
3 0 obj
<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << >> >>
endobj
4 0 obj
<< /Length 44 >>
stream
BT
/F1 12 Tf
100 700 Td
(QGIS Server Mock - Test PDF) Tj
ET
endstream
endobj
xref
0 5
0000000000 65535 f
0000000009 00000 n
0000000058 00000 n
0000000115 00000 n
0000000216 00000 n
trailer
<< /Size 5 /Root 1 0 R >>
startxref
311
%%EOF"""
    return io.BytesIO(pdf_content)


@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint."""
    return {"status": "healthy", "service": "QGIS Server Mock"}


@app.route('/qgis', methods=['GET', 'POST'])
@app.route('/qgis/<path:project_path>', methods=['GET', 'POST'])
def qgis_server(project_path=None):
    """Main QGIS Server endpoint handling WMS and WFS requests."""
    print(f"\n=== QGIS Server Request ===")
    print(f"Method: {request.method}")
    print(f"Path: {request.path}")
    print(f"Args: {dict(request.args)}")

    # Validate authentication (optional)
    auth_header = request.headers.get('Authorization')
    is_valid, error = validate_basic_auth(auth_header)
    if not is_valid:
        return Response(
            SERVICE_EXCEPTION_TEMPLATE.format(code="AuthorizationError", message=error),
            status=401,
            mimetype='application/xml'
        )

    # Get service type
    service = request.args.get('SERVICE', '').upper()
    req_type = request.args.get('REQUEST', '').upper()
    map_path = request.args.get('MAP', project_path or '/data/test_project.qgs')

    print(f"Service: {service}, Request: {req_type}, MAP: {map_path}")

    if service == 'WMS':
        if req_type == 'GETPROJECTSETTINGS':
            return handle_get_project_settings(request.args)
        elif req_type == 'GETPRINT':
            return handle_get_print(request.args)
        else:
            return Response(
                SERVICE_EXCEPTION_TEMPLATE.format(code="OperationNotSupported",
                                                   message=f"Request type '{req_type}' not supported"),
                status=400,
                mimetype='application/xml'
            )

    elif service == 'WFS':
        if req_type == 'GETFEATURE':
            return handle_get_feature(request)
        else:
            return Response(
                SERVICE_EXCEPTION_TEMPLATE.format(code="OperationNotSupported",
                                                   message=f"Request type '{req_type}' not supported"),
                status=400,
                mimetype='application/xml'
            )

    else:
        return Response(
            SERVICE_EXCEPTION_TEMPLATE.format(code="MissingParameterValue",
                                               message="SERVICE parameter is required"),
            status=400,
            mimetype='application/xml'
        )


def handle_get_project_settings(args):
    """Handle WMS GetProjectSettings request."""
    print("Handling GetProjectSettings")

    # Default template and coverage layer
    template_name = "Atlas"
    coverage_layer = "parcels"

    response_xml = PROJECT_SETTINGS_TEMPLATE.format(
        template_name=template_name,
        coverage_layer=coverage_layer
    )

    return Response(response_xml, mimetype='application/xml')


def handle_get_feature(req):
    """Handle WFS GetFeature request."""
    print("Handling GetFeature")

    typename = req.args.get('TYPENAME', 'parcels')
    print(f"TYPENAME: {typename}")

    # Check if there's a body (spatial filter)
    body = req.data.decode('utf-8') if req.data else ""
    if body:
        print(f"Body (first 500 chars): {body[:500]}")

    # Generate sample feature IDs based on spatial query
    # In a real scenario, this would query based on geometry
    feature_ids = [1, 2, 3, 5, 8]  # Simulated feature IDs

    # Build feature members
    feature_members = []
    for i, fid in enumerate(feature_ids):
        x1 = 2500000 + i * 100
        y1 = 1200000 + i * 100
        x2 = x1 + 100
        y2 = y1 + 100
        coords = f"{x1} {y1} {x2} {y1} {x2} {y2} {x1} {y2} {x1} {y1}"

        member = FEATURE_MEMBER_TEMPLATE.format(
            layer_name=typename,
            feature_id=fid,
            x1=x1, y1=y1, x2=x2, y2=y2,
            coords=coords
        )
        feature_members.append(member)

    response_xml = GET_FEATURE_RESPONSE_TEMPLATE.format(
        num_features=len(feature_ids),
        feature_members='\n'.join(feature_members)
    )

    print(f"Returning {len(feature_ids)} features")
    return Response(response_xml, mimetype='application/xml')


def handle_get_print(args):
    """Handle WMS GetPrint request."""
    print("Handling GetPrint")

    template = args.get('TEMPLATE', 'Atlas')
    atlas_pk = args.get('ATLAS_PK', '')
    crs = args.get('CRS', 'EPSG:2056')
    layers = args.get('LAYERS', '')
    format_type = args.get('FORMAT', 'pdf').lower()

    print(f"Template: {template}")
    print(f"ATLAS_PK: {atlas_pk}")
    print(f"CRS: {crs}")
    print(f"Layers: {layers}")
    print(f"Format: {format_type}")

    if not atlas_pk:
        return Response(
            SERVICE_EXCEPTION_TEMPLATE.format(
                code="MissingParameterValue",
                message="ATLAS_PK parameter is required for Atlas printing"
            ),
            status=400,
            mimetype='application/xml'
        )

    # Generate PDF
    pdf_file = create_pdf()

    return send_file(
        pdf_file,
        mimetype='application/pdf',
        as_attachment=True,
        download_name=f'{template}_{datetime.now().strftime("%Y%m%d_%H%M%S")}.pdf'
    )


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='QGIS Server Mock')
    parser.add_argument('--port', type=int, default=8889, help='Port to run on')
    parser.add_argument('--host', default='0.0.0.0', help='Host to bind to')
    args = parser.parse_args()

    print(f"Starting QGIS Server Mock on {args.host}:{args.port}")
    print(f"Credentials: {VALID_USERNAME}/{VALID_PASSWORD}")

    app.run(host=args.host, port=args.port, debug=True)
