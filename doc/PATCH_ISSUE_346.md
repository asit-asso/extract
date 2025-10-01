# PATCH_ISSUE_346 - Nouveau plugin de tâche : Extraction Python

## Status: ✅ CONFORME

### Issue Description
Créer un plugin Python générique qui exécute des scripts avec paramètres passés via fichier JSON, contournant les limitations de ligne de commande.

### Conformity Analysis
**CONFORME** - L'issue #346 est entièrement implémentée dans le code actuel.

#### ✅ Fonctionnalités implémentées:

1. **Champs de plugin requis**
   - ✅ Chemin interpréteur Python: paramètre "pythonInterpreter" (requis)
   - ✅ Chemin script Python: paramètre "pythonScript" (requis)
   - Code: PythonPlugin.java:236-252

2. **Passage paramètres via fichier GeoJSON**
   - ✅ Fichier parameters.json créé automatiquement: ligne 376
   - ✅ Format GeoJSON avec Feature et properties: createParametersFile():637-716
   - ✅ Geometry encodée comme feature avec propriétés: lignes 647-668

3. **Support géométries**
   - ✅ Polygon, MultiPolygon: convertWKTToGeoJSON():725-769
   - ✅ Support "donuts" (interior rings): polygonToCoordinates():779-805
   - ✅ Point, LineString également supportés

4. **Gestion exécution et codes de sortie**
   - ✅ Exécution avec timeout (5 minutes): ligne 528
   - ✅ Code de sortie 0 = succès: condition ligne 544
   - ✅ Gestion détaillée des erreurs Python: lignes 549-603

### Code Locations
- extract-task-python/src/main/java/ch/asit_asso/extract/plugins/python/PythonPlugin.java

### Conclusion
Le plugin Python est entièrement conforme aux spécifications de l'issue #346.
