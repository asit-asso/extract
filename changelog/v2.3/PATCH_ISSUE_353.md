# PATCH_ISSUE_353 - Nouveau plugin de tâche : Extraction FME Flow V2

## Status: ✅ CONFORME

### Issue Description
Créer un nouveau plugin FME Flow qui utilise requêtes POST, authentification token API et paramètres GeoJSON.

### Conformity Analysis
**CONFORME** - L'issue #353 est entièrement implémentée.

#### ✅ Fonctionnalités implémentées:

1. **Plugin FME Flow V2 fonctionnel**
   - ✅ Code: "FMEFLOWV2"
   - ✅ Requêtes POST au lieu de GET
   - ✅ Authentification par token API

2. **Champs requis**
   - ✅ URL Service: paramètre "serviceURL" (requis)
   - ✅ Token API: paramètre "apiToken" (requis, type password)

3. **Paramètres GeoJSON et métadonnées**
   - ✅ Format GeoJSON avec Feature
   - ✅ ClientName, OrganismName, ProductLabel ajoutés
   - ✅ Support polygon, multipolygon, donuts

4. **Sécurité et téléchargement**
   - ✅ Validation URL (prévention SSRF)
   - ✅ Téléchargement ZIP via Data Download
   - ✅ Limite taille fichier (500MB)
   - ✅ Timeout et retry logic

### Code Locations
- extract-task-fmeserver-v2/src/main/java/ch/asit_asso/extract/plugins/fmeflowv2/FmeFlowV2Plugin.java

### Conclusion
Le plugin FME Flow V2 est entièrement conforme et inclut des améliorations de sécurité.
