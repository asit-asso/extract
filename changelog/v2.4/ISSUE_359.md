# ISSUE_359 - Création d'un rôle de consultation (observateurs des traitements)

## Status: ✅ CONFORME

### Issue Description
Certains utilisateurs (chefs de projet, par exemple) doivent pouvoir consulter les demandes
d'un traitement sans pouvoir agir dessus. La solution retenue consiste à ajouter des
« Observateurs » aux traitements : utilisateurs et/ou groupes d'utilisateurs disposant d'un
droit de lecture seule sur les demandes issues du traitement observé, et notifiés par e-mail
à chaque nouvelle demande.

### Acceptance Criteria
| Identifiant | Description |
| --- | --- |
| 359-1 | Un nouveau champ est implémenté dans la vue d'édition des traitements selon la maquette |
| 359-2 | L'administrateur peut ajouter n'importe quel utilisateur et/ou groupe d'utilisateurs dans le champ via une liste drop-down |
| 359-3 | Une nouvelle relation est créée entre les traitements et les utilisateurs observateurs |
| 359-4 | Les observateurs peuvent voir les requêtes issues du traitement observé et télécharger leurs données (folderOut) mais n'effectuer aucune opération, pour autant qu'ils ne soient pas également opérateurs attitrés du traitement |
| 359-5 | Les observateurs sont notifiés par e-mail à chaque nouvelle requête issue d'un traitement observé, pour autant que leurs notifications soient activées |

### Implementation Completed

#### Domaine et persistance
1. `domain/Process.java` : deux nouvelles relations `@ManyToMany` — `watchersCollection`
   (table `processes_watchers`) et `watcherGroupsCollection` (table `processes_watchergroups`) —
   calquées sur les collections d'opérateurs, plus `getDistinctWatchers()` (observateurs directs
   et issus des groupes, sans doublon, tolérant aux collections nulles). `createCopy()` recopie
   les observateurs lors de la duplication d'un traitement.
2. `persistence/ProcessesRepository.java` : `getProcessWatchers(processId)` (observateurs actifs
   avec notifications activées, directs ou via groupe) et `findWatchedProcessesByUser(userId)`.
3. `sql/update_db.sql` : création idempotente des deux tables de jointure, avec clés primaires
   composites, clés étrangères `ON DELETE CASCADE` et index.

#### Vue d'édition d'un traitement
4. `web/model/ProcessModel.java` : champs et accesseurs `watcherUsersIds` / `watcherUserGroupsIds`,
   chargement depuis le domaine et persistance via `copyWatchers` / `copyWatcherGroups`.
5. `templates/pages/processes/details.html` : champ « Observateurs » (optionnel) avec le même
   composant select2 que « Opérateurs attitrés », et affichage en lecture seule.
6. `static/js/processDetails.js` : initialisation du select des observateurs et sérialisation des
   identifiants préfixés `user-` / `group-`. Correction au passage du filtrage des entrées vides
   (`''.split(',')` renvoyait `['']`), qui touchait aussi le select des opérateurs.

#### Droits d'accès
7. `web/controllers/RequestsController.java` : nouvelle méthode `canCurrentUserActOnRequest()`
   qui reprend l'ancienne logique d'accès (administrateur, opérateur attitré du traitement,
   propriétaire de la requête). Toutes les actions (validation, annulation, relance, réexécution
   ou saut de tâche, nouvelle tentative d'export, rematching, ajout/suppression de fichiers,
   assignation de propriétaires) s'appuient désormais sur cette méthode.
   `canCurrentUserViewRequestDetails()` accorde en plus la lecture aux observateurs, ce qui
   préserve la consultation du détail et le téléchargement des fichiers produits.
   La vue de détail reçoit l'attribut `watcherOnly`.
8. `domain/User.java` : les deux requêtes nommées listant les requêtes associées à un utilisateur
   incluent les traitements observés (directement ou via un groupe).
9. `web/controllers/IndexController.java` : les demandes traitées incluent celles des traitements
   observés.
10. `templates/pages/requests/details.html` : pour un observateur pur, les blocs d'action, la zone
    de validation en attente, le panneau de propriétaires additionnels et les boutons
    d'ajout/suppression de fichiers sont masqués ; le téléchargement reste disponible.

#### Notification e-mail
11. Nouvelle classe `email/NewRequestWatcherEmail.java` et template
    `templates/email/html/newRequestWatcher.html`, calqués sur `TaskStandbyEmail`.
12. `batch/processor/RequestMatchingProcessor.java` : après rattachement d'une requête à son
    traitement, envoi d'un e-mail à chaque observateur dans sa langue. Toute erreur d'envoi est
    journalisée sans interrompre le traitement de la requête. Le constructeur reçoit désormais
    le `ProcessesRepository` (`RequestMatcherJobRunner` adapté).
13. Nouvelles clés i18n `processDetails.fields.watchers.*` et `email.newRequestWatcher.*` en
    français, anglais et allemand.

### Tests
- `ProcessWatchersIntegrationTest` (11 tests) : accès en lecture d'un observateur direct et d'un
  observateur via groupe, refus pour un utilisateur non concerné, refus serveur d'une action
  demandée par un observateur pur, action autorisée pour un opérateur, conservation des droits
  pour un observateur également opérateur, filtrage de `getProcessWatchers` (actif +
  notifications) et résultats de `findWatchedProcessesByUser`.
- Suites complètes : 1415 tests unitaires et 528 tests d'intégration au vert.

### Vérification manuelle
- Ajout d'un observateur depuis la vue d'édition d'un traitement, persistance vérifiée en base
  (`processes_watchers`), opérateurs attitrés inchangés.
- Observateur connecté : demandes en cours et traitées du traitement observé visibles, détail
  d'une demande en attente de validation affiché sans aucun bouton d'action ; le même écran
  affiche les blocs Valider / Annuler / Relancer pour l'opérateur attitré.
- Import d'une demande rattachée au traitement : l'observateur reçoit l'e-mail « Nouvelle commande
  pour le traitement … » (vérifié via Mailpit), l'opérateur reçoit son e-mail de validation.

### Conclusion
Les critères 359-1 à 359-5 sont satisfaits.
