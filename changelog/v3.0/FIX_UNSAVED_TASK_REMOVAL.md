# FIX - Erreur 500 à la suppression d'une tâche non enregistrée

## Status: ✅ CONFORME

### Issue Description
Sur un traitement déjà enregistré, ajouter un plugin puis le supprimer aussitôt, sans enregistrer,
provoque une erreur HTTP 500 au lieu de réafficher les tâches enregistrées.

### Root Cause
L'ajout d'une tâche force la position de défilement `htmlScrollY` à `99999` pour que la page
s'affiche tout en bas. Chrome rapporte alors une position fractionnaire (par exemple `3583.75`,
mise en page sub-pixel). `processDetails.js` l'envoie telle quelle à la suppression, mais
`ProcessModel.htmlScrollY` est un `int` : la liaison échoue (`NumberFormatException`), la
`BindException` n'est pas gérée par `deleteTask` (pas de `BindingResult`) et aboutit à l'erreur 500.

### Implementation Completed
1. `static/js/processDetails.js` : la position de défilement est arrondie
   (`Math.round($(document).scrollTop())`) avant d'être envoyée ; suppression d'une ligne
   dupliquée inutile.
2. `web/editors/ScrollPositionEditor` (nouveau) : éditeur de propriété tolérant, enregistré dans
   `ProcessesController.initBinder` pour le seul champ `htmlScrollY`. Valeur vide, non numérique,
   `NaN` ou infinie : 0 ; sinon arrondi à l'entier le plus proche, borné à `[0, Integer.MAX_VALUE]`.

### Tests
- `ScrollPositionEditorTest` (unitaire, nouveau) : valeurs fractionnaires, entières, vides, négatives,
  invalides et très grandes.
- `UnsavedTaskRemovalIntegrationTest` (intégration, nouveau) : ajout puis suppression d'une tâche non
  enregistrée avec une position de défilement `3583.75` ; échouait en 500 avant le correctif.

### Conclusion
Une tâche ajoutée puis supprimée avant l'enregistrement n'entraîne plus d'erreur : les tâches
enregistrées restent affichées, même avec une position de défilement fractionnaire.
