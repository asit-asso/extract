# Patch: Correction des problèmes de filtrage dans la liste des utilisateurs

## Problèmes identifiés

1. **Filtrage automatique non désiré** : Lorsqu'un élément était sélectionné dans les listes déroulantes (select2), le filtre était automatiquement appliqué, ce qui n'était pas le comportement souhaité.

2. **Filtre 2FA non fonctionnel** : Le filtrage par statut 2FA ne fonctionnait pas car les valeurs recherchées étaient incorrectes ("Activé"/"Désactivé" au lieu de "Actif"/"Inactif").

3. **Largeurs des filtres trop courtes** : Les sélecteurs de filtres étaient trop étroits pour afficher correctement leur contenu.

## Solutions apportées

### 1. Désactivation du filtrage automatique
- Ajout de `stopImmediatePropagation()` sur les événements select2 pour empêcher le déclenchement automatique
- Les filtres ne s'appliquent maintenant que sur clic du bouton de recherche ou touche Entrée

### 2. Correction du filtre 2FA
- Identification des valeurs correctes via les fichiers de propriétés : "Actif", "Inactif", "En attente"
- Mise à jour des comparaisons pour correspondre exactement aux valeurs affichées
- Ajout de comparaisons insensibles à la casse avec conversion en minuscules

### 3. Ajustement des largeurs
- Ajout de `min-width` dans le CSS pour les conteneurs select2
- Largeurs spécifiques : 150px par défaut, 170px pour le rôle, 140px pour notifications

## Fichiers modifiés

- `extract/src/main/resources/static/js/usersList.js` : Logique de filtrage
- `extract/src/main/resources/templates/pages/users/list.html` : Labels des options 2FA
- `extract/src/main/resources/static/css/extract.css` : Styles des filtres

## Tests recommandés

1. Vérifier que la sélection d'un filtre ne déclenche pas automatiquement la recherche
2. Tester le filtre 2FA avec les trois valeurs : Actif, Inactif, En attente
3. Confirmer que les filtres État et Notifications fonctionnent correctement
4. Vérifier que les largeurs des sélecteurs sont suffisantes pour afficher le texte

## Impact

Ces corrections améliorent l'expérience utilisateur en rendant le filtrage plus prévisible et fonctionnel, sans impact sur les autres parties de l'application.