EXTRACT
======

## Extract est une application qui facilite l’extraction et la livraison de vos géodonnées

L'application EXTRACT **importe les commandes** de données déposées sur une plateforme ou magasin de données (comme le portail ASIT VD, avec easySDI), puis exécute une série de tâches préconfigurées afin d'**extraire la donnée demandée** , puis **renvoie le résultat** vers le client : avec ou sans intervention humaine, c'est vous qui le définissez !

En automatisant le processus d'extraction et de livraison de vos géodonnées, vous :

* diminuez les **temps de traitement** des commandes,
* augmentez la **qualité des données** livrées,
* augmentez la **satisfaction client**. 

![EXTRACT Robot](https://raw.githubusercontent.com/wiki/asit-asso/extract/images/extract_robot.png)

## Extract est une application open source, qui s'installe chez vous 

![EXTRACT Robot](https://raw.githubusercontent.com/wiki/asit-asso/extract/images/shema_global_transparent2.png)

Extract est modulable et extensible, à installer dans l’environnement informatique de chaque fournisseur de données, et accessible via un navigateur.

## Groupe utilisateurs

L'ASIT, des administrations et gestionnaires de réseaux forment un groupe utilisateur qui pilote et finance le projet

![Membres du groupe utilisateur](https://raw.githubusercontent.com/wiki/asit-asso/extract/images/extract_sponsors.png)

## En production 

Actuellement, 33 fournisseurs diffusent tout ou partie de leurs géodonnées avec EXTRACT grâce aux 7 instances installées chez :

* Ville de Nyon & TRN SA
* Ville de Pully
* Romande Energie
* Ville de Lausanne
* Bureau Jaquier Pointet SA qui gère la diffusion de 17 communes en délégation
* Cartoriviera qui gère la diffusion de 10 communes et associations intercommunales (SIGE)
* Ville de Morges

## Pour installer

Suivez les guides d'installation et d'exploitation ici : https://github.com/asit-asso/extract/wiki

### Prérequis:
* Windows or Linux, 64bit
* Java 7 ou Java 8 (Java 9+ non supporté), Oracle ou OpenJDK, en 64bit
* Tomcat 7 to 9, 64bit
* PostgreSQL >= 9.4

### Juste pour essayer (avec docker et maven):

Lancez les commandes suivantes 
```bash
mvn clean install
docker-compose up
```
Puis ouvrez `http://localhost:8080/extract/login` dans votre navigateur,\
Utilisateur par défaut : `admin`, mot de passe : `motdepasse21`.

Il ne reste qu'à lire la documentation 😊

## Documentation et liens:

Aide et documentation : https://github.com/asit-asso/extract/wiki

Présentations sur le projet aux Rencontres ASIT : https://asit-asso.ch/toutes-les-rencontres#2018

L'ASIT, Association pour le Système d'Information du Territoire, à l'origine du projet : https://asit-asso.ch

Forked from easySDI (www.easysdi.org) : https://svn.easysdi.org/svn/easysdi/branches/4.5.x/java

## Screenshots:

![easysdi EXTRACT Home screenshot](https://raw.githubusercontent.com/wiki/asit-asso/extract/images/screenshots/extract_home_logo2_2x.png)
