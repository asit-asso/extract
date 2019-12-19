EXTRACT
======

Extract est une application qui facilite l’extraction et la livraison de vos géodonnées

L'application EXTRACT importe les commandes de données déposées sur une plateforme easySDI (comme le portail ASIT VD), puis exécute une série de tâches préconfigurées afin d'extraire la donnée demandée , puis renvoie le résultat vers le client : avec ou sans intervention humaine, c'est vous qui le définissez !

En automatisant le processus d'extraction et de livraison de vos géodonnées, vous :

* diminuez les temps de traitement des commandes,
* augmentez la qualité des données livrées,
* augmentez la satisfaction client. 

#### Prérequis:
* Windows or Linux
* Java 7 OR Java 8 (Java 9+ not supported), Oracle OR OpenJDK
* Tomcat 7 to 9
* PostgreSQL >= 9.4

#### Pour installer

Suivez les guicdes d'installation et d'exploitation ici : https://projets.asitvd.ch/projects/extracteur/wiki

#### Juste pour essayer (avec docker et maven):

Lancez les commandes suivantes 
```bash
mvn clean install
docker-compose up
```
Puis ouvrez `http://localhost:8080/extract/login` dans votre navigateur,\
Utilisateur par défaut : `admin`, mot de passe : `motdepasse21`.

Il ne reste qu'à lire la documentation 😊

#### Doc:

About page (FR) : https://www.asitvd.ch/partager/automatiser-la-diffusion-des-geodonnees-avec-extract.html

Help & doc page (FR) : https://projets.asitvd.ch/projects/extracteur/wiki

Forked from easySDI (www.easysdi.org) : https://svn.easysdi.org/svn/easysdi/branches/4.5.x/java
