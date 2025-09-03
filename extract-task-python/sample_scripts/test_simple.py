#!/usr/bin/env python3
"""
Script Python de test simple pour le plugin Extract Python.
Ce script lit le fichier GeoJSON des paramètres et génère un fichier de sortie.
"""

import json
import sys
import os


def main():
    # Vérifier qu'un fichier de paramètres est fourni
    if len(sys.argv) < 2:
        print("Erreur: Aucun fichier de paramètres fourni")
        sys.exit(1)
    
    parameters_file = sys.argv[1]
    
    try:
        # Lire le fichier GeoJSON
        with open(parameters_file, 'r') as f:
            feature = json.load(f)
        
        # Vérifier que c'est bien un Feature GeoJSON
        if feature.get('type') != 'Feature':
            print("Erreur: Le fichier n'est pas un GeoJSON Feature valide")
            sys.exit(1)
        
        # Extraire les properties
        properties = feature.get('properties', {})
        
        print(f"Traitement de la requête {properties.get('RequestId')}")
        
        # Créer un fichier de sortie
        output_dir = properties.get('FolderOut')
        if output_dir:
            output_file = os.path.join(output_dir, 'resultat.txt')
            with open(output_file, 'w') as f:
                f.write(f"Requête {properties.get('RequestId')} traitée avec succès\n")
                f.write(f"Client: {properties.get('ClientName')}\n")
                f.write(f"Organisme: {properties.get('OrganismName')}\n")
                f.write(f"Produit: {properties.get('ProductLabel')}\n")
                
                # Traiter la géométrie
                geometry = feature.get('geometry')
                if geometry:
                    geom_type = geometry.get('type')
                    f.write(f"Type de géométrie: {geom_type}\n")
                
                # Traiter les paramètres custom
                parameters = properties.get('Parameters', {})
                if parameters:
                    f.write("\nParamètres custom:\n")
                    for key, value in parameters.items():
                        f.write(f"  - {key}: {value}\n")
            
            print(f"Fichier créé: {output_file}")
        
        print("Traitement terminé avec succès")
        sys.exit(0)
        
    except Exception as e:
        print(f"Erreur: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()