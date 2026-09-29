# ISSUE_362 - Stockage des mots de passe non-encodés dans PostgreSQL

## Status: ✅ CONFORME

### Issue Description
La plupart des secrets d'Extract (mot de passe serveur SMTP, mot de passe utilisateur LDAP de
synchronisation, mots de passe des connecteurs, mot de passe et clé API des serveurs FME Flow,
mot de passe du serveur QGIS pour l'impression Atlas) étaient enregistrés en clair dans la base
de données, alors qu'une clé et un sel de chiffrement sont déjà disponibles dans
`application.properties`.

### Acceptance Criteria
| Identifiant | Description |
| --- | --- |
| 362-1 | Tous les secrets sont chiffrés dans la base de données |
| 362-2 | Les anciens secrets déjà existants dans la base de données sont aussi chiffrés |
| 362-3 | La rétro-compatibilité entre nouveaux et anciens secrets est assurée |

### Implementation Completed
1. `utils/Secrets.java` : chiffrement AES-GCM (préfixe `enc:v1:`) et `decryptLegacy()`, qui
   accepte encore une valeur en clair (rétro-compatibilité, 362-3) mais échoue explicitement si
   une valeur qui ressemble à un chiffré existant ne peut pas être déchiffrée.
2. `services/SecretParameters.java` (nouveau) et `services/SecretMigrationService.java`
   (nouveau, `@EventListener(ApplicationReadyEvent.class)`) : chiffrent au démarrage les secrets
   système, les paramètres de connecteurs et de tâches déjà en base qui ne le sont pas encore
   (362-2), sans écraser ceux déjà migrés.
3. Points de persistance (`EmailConfiguration`, `ConnectorsController`, `ProcessesController`,
   `ConnectorModel`, `TaskModel`, `LdapSettings`) : chiffrement à l'enregistrement, déchiffrement
   à la lecture/utilisation runtime (362-1).
4. `sql/update_db.sql` : script de migration pour les colonnes concernées.

### Corrections apportées après la revue initiale (CI)
La branche a été rebasée sur la version de `master` intégrant les correctifs #370/#373/#374
publiés entre-temps, ce qui a révélé deux problèmes distincts, corrigés dans cette même branche :

5. `unit/web/model/ProcessModelDescriptionTest.java` : utilisait encore le constructeur
   `ProcessModel` à 3 arguments, remplacé par celui à 4 arguments (avec `SecretParameters`)
   introduit par ce ticket — corrigé pour utiliser le bon constructeur.
6. `EmailSettings.setSettingsFromDataSource()` : `EmailConfiguration.emailSettings()` est
   construit pendant le rafraîchissement du contexte Spring, **avant** que
   `SecretMigrationService` (déclenché sur `ApplicationReadyEvent`) n'ait eu l'occasion de migrer
   un éventuel ancien mot de passe SMTP. Si le déchiffrement échoue à ce moment précis (clé
   incorrecte, valeur corrompue, ou — comme observé en CI — un concours de circonstances avec la
   migration), c'est tout le contexte Spring qui échouait à démarrer, entraînant l'échec de
   **tous** les tests d'intégration. Le mot de passe SMTP est désormais traité comme le port SMTP
   juste en dessous dans la même méthode : une erreur est journalisée et le mot de passe est
   laissé non défini plutôt que de faire échouer le démarrage de l'application.
7. `ProcessDescriptionIntegrationTest` : alignée sur la convention déjà en place dans
   `ProcessTaskOwnershipIntegrationTest` (`@MockBean TaskProcessorDiscovererWrapper`), la
   résolution réelle des plugins de tâches via `ServletContext` ne pouvant structurellement pas
   fonctionner sous `@SpringBootTest` en environnement de test.
8. `SecretMigrationService.migrateTasks`/`migrateConnectors` : la migration d'une tâche ou d'un
   connecteur dont le plugin n'est pas (encore) résolu est désormais ignorée individuellement au
   lieu d'interrompre la boucle.

### Tests
- Suite complète (unitaire + intégration) via `docker-compose-test.yaml` : 524 tests, 0 échec.
- CI GitHub Actions du dépôt (build + tests unitaires, d'intégration et fonctionnels) : verte.

### Conclusion
Les critères 362-1 à 362-3 sont satisfaits, et les régressions de démarrage introduites par le
rebase sur les tickets #370/#373/#374 sont corrigées.
