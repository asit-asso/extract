#!/usr/bin/env python3
"""
Sample Python script for Extract task processing.
This script demonstrates how to read and process the GeoJSON Feature file
passed by the Extract Python plugin.
"""

import json
import sys
import os
from datetime import datetime


def main():
    """Main processing function."""
    
    # Check if parameters file was provided
    if len(sys.argv) < 2:
        print("Error: No parameters file provided")
        sys.exit(1)
    
    parameters_file = sys.argv[1]
    
    # Check if file exists
    if not os.path.exists(parameters_file):
        print(f"Error: Parameters file not found: {parameters_file}")
        sys.exit(1)
    
    try:
        # Read the GeoJSON Feature
        with open(parameters_file, 'r', encoding='utf-8') as f:
            feature = json.load(f)
        
        # Validate it's a Feature
        if feature.get('type') != 'Feature':
            print("Error: File is not a valid GeoJSON Feature")
            sys.exit(1)
        
        # Extract properties
        data = feature.get('properties', {})
        
        print("=" * 60)
        print("EXTRACT PYTHON TASK PROCESSOR")
        print("=" * 60)
        print(f"Processing started at: {datetime.now().isoformat()}")
        print()
        
        # Extract basic information
        request_id = data.get('RequestId')
        folder_out = data.get('FolderOut')
        folder_in = data.get('FolderIn')
        
        print(f"Request ID: {request_id}")
        print(f"Input folder: {folder_in}")
        print(f"Output folder: {folder_out}")
        print()
        
        # Extract order information
        print("Order Information:")
        print(f"  - GUID: {data.get('OrderGuid')}")
        print(f"  - Label: {data.get('OrderLabel')}")
        print()
        
        # Extract client information
        print("Client Information:")
        print(f"  - GUID: {data.get('ClientGuid')}")
        print(f"  - Name: {data.get('ClientName')}")
        print()
        
        # Extract organism information
        print("Organism Information:")
        print(f"  - GUID: {data.get('OrganismGuid')}")
        print(f"  - Name: {data.get('OrganismName')}")
        print()
        
        # Extract product information
        print("Product Information:")
        print(f"  - GUID: {data.get('ProductGuid')}")
        print(f"  - Label: {data.get('ProductLabel')}")
        print()
        
        # Extract custom parameters
        parameters = data.get('Parameters', {})
        if parameters:
            print("Custom Parameters:")
            if isinstance(parameters, dict):
                for key, value in parameters.items():
                    print(f"  - {key}: {value}")
            else:
                print(f"  {parameters}")
            print()
        
        # Extract geometry from the Feature
        geometry = feature.get('geometry')
        if geometry:
            print("Geometry (GeoJSON):")
                geom_type = geometry.get('type', 'Unknown')
                print(f"  - Type: {geom_type}")
                
                # For polygon, calculate some basic statistics
                if geom_type in ['Polygon', 'MultiPolygon'] and 'coordinates' in geometry:
                    coords = geometry['coordinates']
                    if geom_type == 'Polygon' and coords and len(coords) > 0:
                        num_points = len(coords[0]) if coords[0] else 0
                        print(f"  - Number of points: {num_points}")
                        
                        # Calculate bounding box
                        if num_points > 0:
                            all_x = [p[0] for p in coords[0] if len(p) >= 2]
                            all_y = [p[1] for p in coords[0] if len(p) >= 2]
                            if all_x and all_y:
                                print(f"  - Bounding box:")
                                print(f"    - Min X: {min(all_x):.6f}")
                                print(f"    - Max X: {max(all_x):.6f}")
                                print(f"    - Min Y: {min(all_y):.6f}")
                                print(f"    - Max Y: {max(all_y):.6f}")
            
            print()
        
        # Create output files
        print("Creating output files...")
        
        # Create a summary JSON file
        summary_file = os.path.join(folder_out, 'processing_summary.json')
        summary = {
            'processing_date': datetime.now().isoformat(),
            'request_id': request_id,
            'status': 'SUCCESS',
            'message': 'Data processed successfully',
            'statistics': {
                'input_folder': folder_in,
                'output_folder': folder_out,
                'parameters_count': len(parameters) if isinstance(parameters, dict) else 0
            }
        }
        
        with open(summary_file, 'w', encoding='utf-8') as f:
            json.dump(summary, f, indent=2)
        print(f"  - Created: {summary_file}")
        
        # Create a sample output text file
        output_file = os.path.join(folder_out, 'processing_log.txt')
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(f"Processing log for request {request_id}\n")
            f.write(f"Processed at: {datetime.now().isoformat()}\n")
            f.write(f"Client: {data.get('ClientName', 'Unknown')}\n")
            f.write(f"Product: {data.get('ProductLabel', 'Unknown')}\n")
            f.write("\nProcessing completed successfully.\n")
        print(f"  - Created: {output_file}")
        
        print()
        print("=" * 60)
        print(f"Processing completed successfully at: {datetime.now().isoformat()}")
        print("=" * 60)
        
        # Exit with success
        sys.exit(0)
        
    except json.JSONDecodeError as e:
        print(f"Error: Failed to parse JSON file: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"Error: Unexpected error during processing: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()