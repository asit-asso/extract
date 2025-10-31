# Guide de débogage des locales dans Extract

## Configuration actuelle
- **Langues configurées** : `extract.i18n.language=de,en`
- **Langues supportées** : Allemand (de) et Anglais (en)
- **Français (fr) NON supporté** mais les fichiers messages_fr.properties existent

## Comment tester le système de validation des locales

### 1. Configurer le niveau de log

Ajouter dans `application.properties` :
```properties
logging.level.ch.asit_asso.extract.configuration.UserLocaleResolver=INFO
```

Ou lancer l'application avec :
```bash
cd extract
./mvnw spring-boot:run -Dlogging.level.ch.asit_asso.extract.configuration.UserLocaleResolver=INFO
```

### 2. Scénarios de test

#### Test 1 : Utilisateur avec locale "fr" en base de données

1. Connectez-vous avec un utilisateur ayant "fr" comme locale
2. Observez les logs, vous devriez voir :

```
============ LOCALE RESOLUTION DEBUG START ============
Available locales configured: [de, en]
User: admin, Authenticated: true
--- LOCALE SOURCES ---
Session locale: null (explicitly selected: null)
Database locale for user admin: fr
Browser locale: fr_FR (Accept-Language: fr-FR,fr;q=0.9)
--- RESOLUTION PROCESS ---
Step 2: Checking database preference for authenticated user
User admin has locale fr in database
User locale fr is not available, falling back to de and updating DB
=== DECISION: de (Reason: Database locale for user admin) ===
============ LOCALE RESOLUTION DEBUG END ============
```

#### Test 2 : Changement de langue via le sélecteur

1. Essayez de changer la langue vers "fr" via l'interface
2. Les logs devraient montrer :

```
============ SET LOCALE DEBUG START ============
Request to set locale: fr
Available locales: [de, en]
Is fr available? false
Requested locale fr is NOT available, using fallback: de
Updated session with locale: de
Updated database locale for user admin from fr to de
============ SET LOCALE DEBUG END ============
```

#### Test 3 : Utilisateur non authentifié avec navigateur en français

1. Déconnectez-vous
2. Configurez votre navigateur pour envoyer "fr" comme langue préférée
3. Accédez à la page de login
4. Les logs montreront :

```
============ LOCALE RESOLUTION DEBUG START ============
Available locales configured: [de, en]
User: anonymous, Authenticated: false
--- LOCALE SOURCES ---
Session locale: null (explicitly selected: null)
Browser locale: fr_FR (Accept-Language: fr-FR,fr;q=0.9)
--- RESOLUTION PROCESS ---
Step 3: Checking browser locale
  -> Browser locale fr_FR does not match any available locale
Step 4: Using fallback locale
=== DECISION: de (Reason: Fallback to first available locale) ===
============ LOCALE RESOLUTION DEBUG END ============
```

### 3. Vérifier les logs

Recherchez dans les logs :
```bash
grep "LOCALE RESOLUTION DEBUG" extract.log | tail -50
grep "SET LOCALE DEBUG" extract.log | tail -20
grep "DECISION:" extract.log | tail -10
```

### 4. Points à vérifier

✅ **Succès** si :
- Toute demande de locale "fr" est convertie en "de" (première locale disponible)
- La base de données est mise à jour avec la locale validée
- L'interface s'affiche en allemand ou anglais uniquement

❌ **Problème** si :
- L'interface s'affiche encore en français
- Les logs montrent que "fr" est utilisé comme décision finale
- La validation échoue silencieusement

### 5. Si l'interface s'affiche encore en français

Cela peut être dû à :

1. **Cache du navigateur** : Videz le cache et les cookies
2. **Session persistante** : Déconnectez-vous et reconnectez-vous
3. **Fichiers de messages** : Spring charge automatiquement messages_fr.properties s'il existe

Pour tester sans les fichiers français :
```bash
# Renommer temporairement les fichiers français
mv extract/src/main/resources/messages_fr.properties extract/src/main/resources/messages_fr.properties.disabled
mv extract/src/main/resources/static/lang/fr extract/src/main/resources/static/lang/fr.disabled
```

### 6. Commandes utiles pour le débogage

```bash
# Voir toutes les décisions de locale
grep "=== DECISION:" extract.log

# Voir les fallbacks
grep "falling back to" extract.log

# Voir les mises à jour de BD
grep "Updated database locale" extract.log

# Voir les validations de locale
grep "Is .* available?" extract.log
```

## Résultat attendu

Avec `extract.i18n.language=de,en`, le système devrait :
1. ✅ Rejeter toute tentative d'utiliser "fr"
2. ✅ Toujours utiliser "de" ou "en"
3. ✅ Mettre à jour automatiquement les préférences utilisateur invalides
4. ✅ Logger clairement chaque décision prise