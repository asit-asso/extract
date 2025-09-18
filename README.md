Extract
======

## Extract est une application qui facilite l’extraction et la livraison de vos géodonnées

L'application Extract **importe les commandes** de données déposées sur une plateforme ou magasin de données (comme les portails ASIT viageo.ch et plans-reseaux.ch), puis exécute une série de tâches préconfigurées afin d'**extraire la donnée demandée** , puis **renvoie le résultat** vers le client : avec ou sans intervention humaine, c'est vous qui le définissez !

En automatisant le processus d'extraction et de livraison de vos géodonnées, vous :

* diminuez les **temps de traitement** des commandes,
* augmentez la **qualité des données** livrées,
* augmentez la **satisfaction client**. 

![Extract Robot](https://raw.githubusercontent.com/wiki/asit-asso/extract/images/extract_robot.png)

## Extract est une application open source, qui s'installe chez vous 

![Extract Robot](https://raw.githubusercontent.com/wiki/asit-asso/extract/images/shema_global_transparent2.png)

Extract est modulable et extensible, à installer dans l’environnement informatique de chaque fournisseur de données, et accessible via un navigateur.

## 📋Documentation

Toute la documentation pour installer, paramétrer et utiliser Extract est en ligne (en anglais) : https://benoitregamey.github.io/doc/

## Groupe utilisateurs

L'ASIT, des administrations et gestionnaires de réseaux forment un groupe utilisateur qui pilote et finance le projet

![Membres du groupe utilisateur](https://raw.githubusercontent.com/wiki/asit-asso/extract/images/extract_sponsors.png)

## En production 

Actuellement, 37 fournisseurs diffusent tout ou partie de leurs géodonnées avec Extract grâce aux 11 instances installées chez :

* Ville de Nyon & TRN SA
* Ville de Pully & Belmont-sur-Lausanne
* Romande Energie SA & SIE SA
* Ville de Lausanne
* Bureau Jaquier Pointet SA qui gère la diffusion de 17 communes en délégation
* Cartoriviera qui gère la diffusion de 9 communes et associations intercommunales (SIGE)
* Ville de Morges
* Viteos SA
* SITN
* Holdigaz Prestations SA
* ASIT - Association pour le système d'information du territoire

## Développement

### Packaging

Pour générer un WAR de l'application, il faut lancer la commande suivante
```bash
mvn package
```

Outre le fichier WAR, l'archive de livraison d'une nouvelle version contient :

* La dernière version du script de mise à jour de la base de données (`sql/update_db.sql`)
* La documentation : 
  * guide d'installation (`doc/MAN_ASIT_Extract_ManuelInstallation.pdf`)
  * guide d'exploitation (`doc/MAN_ASIT_Extract_ManuelExploitation.pdf`) 
  * guide d'utilisation (`doc/MAN_ASIT_Extract_ManuelUtilisation.pdf`)
  * documentation de création d'un connecteur (`doc/extract-connector-sample/`)
  * documentation de création d'un plugin de tâche (`doc/extract-task-sample/`)
* Le script d'exemple FME (`fme/`)

### Tests

Les tests unitaires peuvent se lancer indépendamment du packaging par la commande
```bash
mvn -q test -Punit-tests --batch-mode --fail-at-end
```

Pour exécuter les tests d'intégration
```bash
mvn -q verify -Pintegration-tests --batch-mode
```

Pour les tests fonctionnels (nécessite que l'application tourne sur le port 8080 du localhost)
```bash
mvn -q verify -Pfunctional-tests --batch-mode
```

## Liens:

Présentations sur le projet aux Rencontres ASIT : https://asit-asso.ch/toutes-les-rencontres#2018

L'ASIT, Association pour le Système d'Information du Territoire, à l'origine du projet : https://asit-asso.ch

Forked from easySDI (www.easysdi.org) : https://svn.easysdi.org/svn/easysdi/branches/4.5.x/java

## Screenshots:

![easysdi Extract Home screenshot](https://raw.githubusercontent.com/wiki/asit-asso/extract/images/screenshots/extract_home_logo2_2x.png)
