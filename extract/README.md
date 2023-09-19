# Extract Core

## Introduction

Ce projet représente le c&oelig;ur du module Extract permettant l'extraction de données à partir de commandes. Il
contient à la fois l'application web d'administration et le moteur d'execution des extractions en arrière-plan,

## Pré-requis pour l'utilisation
* OS 64 bits
* Java 17
* Tomcat 9

## Pré-requis pour le développement et la compilation
* Java 17
* [Yarn][Yarn_Site]
* Projet extract-interface (Interface commune pour l'utilisation des plugins connecteurs et tâches)

## Installation des librairies JavaScript

Les librairies JavaScript utilisées par l'application Web et leurs dépendances sont gérées au moyen du gestionnaire de
packages [Yarn][Yarn_Site]. Si vous récupérez le code depuis le repository, il faudra installer ces packages avant de pouvoir
exécuter l'application.

Pour ce faire, ouvrez une ligne de commande dans le répertoire racine du projet et exécutez la commande suivante&nbsp;:
```
yarn install
```

Les librairies JavaScript nécessaires devraient maintenant se trouver dans le répertoire `src/main/resources/static/lib`.

[Yarn_Site]: https://yarnpkg.com/ "Site du gestionnaire de package Yarn"
