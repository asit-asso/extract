# ISSUE_440 - Les paramètres booléens des tâches sont écrasés (null) lors de leur déplacement (drag-drop)

## Status: ✅ CONFORME

### Issue Description
Sur la page d'édition d'un traitement, un paramètre booléen de tâche (par exemple « Écraser la
remarque existante ? » du plugin Remarque fixe, rendu sous forme de deux boutons radio Oui/Non)
perd sa sélection dès qu'on déplace la tâche par glisser-déposer : après le déplacement, ni Oui
ni Non n'est sélectionné. À l'enregistrement, la valeur `null` est illégale pour ce paramètre et
le traitement échoue avec une erreur 500 sans être sauvé.

### Root Cause
Le tri des tâches (`processDetails.js`, plugin jQuery UI Sortable) utilise `helper: 'clone'` :
pendant le glisser, un clone complet de la carte de tâche suit le pointeur, y compris ses champs
radio Oui/Non — avec le **même attribut `name`** que les champs réels du formulaire. Tant que le
clone est présent dans le DOM, le navigateur applique l'exclusivité mutuelle du groupe radio
(« un seul bouton coché par `name` ») entre les deux copies et décoche l'original : le paramètre
n'a alors plus aucune valeur sélectionnée.

Reproduit avec un vrai glisser-déposer (Chromium piloté par CDP) : même l'auto-glissement d'une
tâche unique (la ramasser puis la reposer au même endroit) suffit à déclencher la perte ; avec
deux tâches ou plus, le mécanisme interne de jQuery UI ne l'a pas reproduite dans nos essais,
mais le mécanisme sous-jacent (nom dupliqué pendant le drag) reste identique quel que soit le
nombre de tâches.

### Implementation Completed
1. `static/js/processDetails.js` : `helper` devient une fonction qui clone la carte puis retire
   l'attribut `name` (et désactive les champs) sur le clone visuel, afin qu'il ne puisse jamais
   entrer en concurrence avec le formulaire réel pour le groupe radio. Changement purement
   cosmétique côté aperçu de glisser-déposer ; la carte réelle, soumise au serveur, n'est pas
   modifiée.

### Tests
- Vérification manuelle en navigateur réel (reproduction du bug puis confirmation du correctif) :
  état du bouton radio avant/après glisser-déposer, puis après enregistrement complet — valeur
  persistée en base (`task_params`) : `{"overwrite":"false"}`, jamais `null`.
- `ProcessTaskReorderFunctionalTest` (Selenium, nouveau) : seed d'un traitement avec une tâche
  Remarque fixe, glisser-déposer réel, puis enregistrement ; vérifie que la sélection du
  paramètre booléen survit au déplacement et à la sauvegarde. Exécuté et passé par la CI du
  projet (étape « Execute functional tests »).

### Conclusion
Le paramètre booléen conserve sa valeur après un déplacement par glisser-déposer, avant comme
après l'enregistrement.
