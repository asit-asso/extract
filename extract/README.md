# Extract Core

## Introduction

Ce projet repr�sente le c&oelig;ur du module Extract permettant l'extraction de donn�es � partir de commandes. Il
contient � la fois l'application web d'administration et le moteur d'execution des extractions en arri�re-plan,

## Pr�-requis pour l'utilisation
* OS 64 bits
* Java 7
* Tomcat 7

## Pr�-requis pour le d�veloppement et la compilation
* Java 7
* [Yarn][Yarn_Site]
* Projet extract-interface (Interface commune pour l'utilisation des plugins connecteurs et t�ches)

## Installation des librairies JavaScript

Les librairies JavaScript utilis�es par l'application Web et leurs d�pendances sont g�r�es au moyen du gestionnaire de
packages [Yarn][Yarn_Site]. Si vous r�cup�rez le code depuis le repository, il faudra installer ces packages avant de pouvoir
ex�cuter l'application.

Pour ce faire, ouvrez une ligne de commande dans le r�pertoire racine du projet et ex�cutez la commande suivante&nbsp;:
```
yarn install
```

Les librairies JavaScript n�cessaires devraient maintenant se trouver dans le r�pertoire `src/main/resources/static/lib`.

[Yarn_Site]: https://yarnpkg.com/ "Site du gestionnaire de package Yarn"