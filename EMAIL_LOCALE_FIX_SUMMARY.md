# Résumé de la correction des locales pour les emails système

## Problème identifié

Les emails système étaient toujours envoyés en français même si `extract.i18n.language=de,en` à cause de :
1. Spring MessageSource charge automatiquement les fichiers `messages_fr.properties` s'ils existent
2. Les emails utilisaient directement les locales des utilisateurs sans validation

## Solutions implémentées

### 1. Validation des locales utilisateur pour les emails (déjà fait)
- **Classe modifiée** : `LocaleUtils.java` (nouvelle)
- **Fonction** : Valide les locales utilisateur contre les langues configurées
- **Impact** : Chaque opérateur reçoit les emails dans sa langue SI elle est disponible, sinon dans la première langue configurée

### 2. Validation dans EmailSettings (nouveau)
- **Classe modifiée** : `EmailSettings.java`
- **Ajouts** :
  - Nouveau paramètre `languageConfig` dans le constructeur
  - Méthode `validateLocale()` qui valide les locales avant utilisation
  - Modification de `getMessageString()` pour valider les locales
- **Impact** : TOUTES les locales sont validées avant d'être passées à MessageSource

### 3. Configuration mise à jour
- **Classe modifiée** : `EmailConfiguration.java`
- **Ajout** : Injection de `extract.i18n.language` et passage à EmailSettings

## Comment le système fonctionne maintenant

1. **Quand un email est envoyé** :
   - La locale de l'utilisateur est récupérée
   - `LocaleUtils` valide cette locale contre `extract.i18n.language`
   - Si invalide → utilise la première langue configurée (ex: "de")

2. **Quand un message est résolu** :
   - `EmailSettings.getMessageString()` reçoit la locale
   - La locale est RE-VALIDÉE dans EmailSettings
   - Spring MessageSource reçoit toujours une locale valide

3. **Double validation** :
   - Niveau 1 : Dans RequestTaskRunner/StandbyRequestsReminderProcessor
   - Niveau 2 : Dans EmailSettings avant MessageSource

## Tests à effectuer

### Configuration de test
```properties
extract.i18n.language=de,en
```

### Scénarios de test

1. **Utilisateur avec locale "fr"** :
   - Devrait recevoir les emails en "de" (première langue disponible)
   - Logs devraient montrer : "Locale fr is not available, falling back to: de"

2. **Utilisateur avec locale "en"** :
   - Devrait recevoir les emails en "en"
   - Logs : "Locale en is available (exact match)"

3. **Utilisateur sans locale définie** :
   - Devrait recevoir les emails en "de" (fallback)

### Commandes pour vérifier les logs

```bash
# Voir les validations de locale dans les emails
grep "Email message locale changed" extract.log

# Voir les fallbacks
grep "falling back to" extract.log

# Voir les messages résolus
grep "Message .* resolved to" extract.log
```

## Limitation connue

**Les fichiers `messages_fr.properties` existent toujours** dans le classpath. Spring peut encore les charger dans certains cas. Pour une isolation complète :

### Option 1 : Renommer les fichiers non supportés
```bash
mv extract/src/main/resources/messages_fr.properties extract/src/main/resources/messages_fr.properties.disabled
```

### Option 2 : Créer un MessageSource personnalisé
Implémenter un `FilteredMessageSource` qui refuse de charger les locales non configurées (solution plus complexe mais plus propre).

## Résultat attendu

Avec `extract.i18n.language=de,en` :
- ✅ Aucun email en français
- ✅ Tous les emails en allemand ou anglais uniquement
- ✅ Validation automatique et transparente
- ✅ Logs détaillés pour débogage