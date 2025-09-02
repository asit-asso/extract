# Guide de débogage pour le plugin Email

## Problème : "L'envoi de la notification par e-mail a échoué pour une raison inconnue. (-1)"

### Améliorations apportées au logging

Les fichiers suivants ont été modifiés pour ajouter des logs détaillés :

1. **EmailPlugin.java** - Ajout de logs détaillés pour :
   - Vérification si les notifications sont activées
   - Parsing des adresses email
   - Remplacement des variables dans le sujet et le corps
   - Statut de l'envoi

2. **Email.java** - Ajout de logs pour :
   - Configuration SMTP (serveur, port, authentification, SSL)
   - Création de la session SMTP
   - Processus d'envoi via Transport
   - Détails des erreurs

### Points de vérification

1. **Configuration SMTP**
   - Vérifier que le serveur SMTP est correctement configuré dans l'application
   - Vérifier le port (25 pour SMTP, 465 pour SMTPS, 587 pour SMTP avec STARTTLS)
   - Vérifier si l'authentification est requise et configurée
   - Vérifier si SSL/TLS est requis et configuré

2. **Format des adresses email**
   - Les adresses doivent être séparées par des virgules ou points-virgules
   - Vérifier qu'elles sont valides (format correct)
   - Le plugin vérifie maintenant chaque adresse et log les adresses invalides

3. **Contenu du message**
   - Vérifier que le sujet et le corps ne sont pas null ou vides
   - Vérifier que le template HTML est trouvé (emailTemplate.html)
   - Les placeholders disponibles : {client}, {clientGuid}, {organism}, {organismGuid}, {remark}, {perimeter}, {orderLabel}, {productLabel}, {startDate}, {endDate}, {status}

### Niveaux de log à activer

Pour voir tous les détails du débogage, configurez le niveau de log à DEBUG pour :
```
ch.asit_asso.extract.plugins.email=DEBUG
```

### Messages de log à surveiller

1. **Succès** :
   - "Email sent successfully via SMTP"
   - "Email sent successfully to: [adresse]"

2. **Échecs courants** :
   - "Email notifications are disabled in settings" - Les notifications sont désactivées
   - "No valid email addresses found" - Aucune adresse valide trouvée
   - "Invalid email address rejected" - Adresse email invalide
   - "The SMTP configuration is not valid" - Configuration SMTP invalide
   - "Failed to create SMTP session" - Impossible de créer la session SMTP
   - "Could not send the e-mail because an error occurred with the SMTP transport" - Erreur d'envoi SMTP

### Exemple de configuration de test

Pour tester avec un serveur SMTP local (comme MailHog) :
- Serveur SMTP : localhost
- Port : 1025
- Authentification : Non
- SSL : Non

Pour Gmail :
- Serveur SMTP : smtp.gmail.com
- Port : 587 (ou 465 pour SSL)
- Authentification : Oui
- SSL/TLS : Oui
- Utiliser un mot de passe d'application, pas le mot de passe normal

### Commande pour voir les logs en temps réel

```bash
tail -f [chemin_vers_logs]/extract.log | grep -E "(EmailPlugin|Email|email)"
```

### Test simple

1. Créer une tâche email avec :
   - To : votre-email@example.com
   - Subject : Test {orderLabel}
   - Body : Test du plugin email pour {productLabel}

2. Exécuter la tâche et vérifier les logs

Les logs détaillés devraient maintenant indiquer exactement où le processus échoue.