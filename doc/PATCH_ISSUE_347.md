# PATCH_ISSUE_347 - Nouveau plugin de tâche : Extraction FME Form V2

## Status: ✅ CONFORME

### Issue Description
Créer un nouveau plugin FME qui contourne les limitations de ligne de commande via fichier GeoJSON et ajoute nom client, organisation, produit.

### Conformity Analysis
**CONFORME** - L'issue #347 est entièrement implémentée.

#### ✅ Fonctionnalités implémentées:

1. **Plugin FME V2 fonctionnel**
   - ✅ Nom: "Extraction FME V2" - code "FME2017V2"
   - ✅ Contourne limitations ligne de commande
   - ✅ Paramètres via fichier GeoJSON

2. **Champs requis**
   - ✅ Workspace FME: paramètre "workbench" (requis)
   - ✅ Exécutable FME: paramètre "application" (requis)
   - ✅ Instances max: paramètre "nbInstances" (1-8)

3. **Support géométries et métadonnées**
   - ✅ Polygon, MultiPolygon, donuts supportés
   - ✅ ClientName, OrganismName, ProductLabel ajoutés
   - ✅ Conversion WKT vers GeoJSON

4. **Gestion exécution**
   - ✅ Timeout 72 heures pour processus FME
   - ✅ Code de sortie 0 = succès
   - ✅ Fichiers output dans FolderOut

### Code Locations
- extract-task-fmedesktop-v2/src/main/java/ch/asit_asso/extract/plugins/fmedesktopv2/FmeDesktopV2Plugin.java

### Conclusion
Le plugin FME Desktop V2 est entièrement conforme aux spécifications.
