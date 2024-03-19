# Extract - Initialiser un nouveau plugin de tâche 

## Introduction

Ce module java est un exemple de plugin de tâche qui peut être utilisé pour initialiser un nouveau 
plugin pour le projet Extract. 
Le code source est documenté et permet d'avoir les indications nécessaires pour développer un nouveau plugin,
Il peut être importé dans un nouveau environnement Java, des adaptations sont cependant
nécessaires selon le fonctionnement attendu.

## Pré-requis pour l'utilisation
* OS 64 bits
* Java 17
* Tomcat 9

## Pré-requis pour le développement et la compilation
* Java 17
* [Yarn][Yarn_Site]
* Projet extract-interface (Interface commune pour l'utilisation des plugins connecteurs et tâches)

## Initialisation du nouveau plugin de tâche.
Le projet doit être un module Java. \
Le projet du nouveau plugin doit définir une dépendance vers le projet extract-interface.\
Les dépendances requises sont définies dans le fichier pom.xml.

Pour initialiser un nouveau plugin, suivre les étapes dans l'ordre :
1. Copier le cde du plugin extract-task-sample vers un workspace java. Le système propose demande de définir un 
nouveau nom.  Si ce n'est pas le cas, utiliser le menu contextuel (clic droit) `Refactor > Rename`


2. Editer le fichier **pom.xml** du module, remplacer les occurences de `extrat-task-sample` par le nouveau nom du plugin.
Après un clic droit sur le fichier, choisir l'option `Add as Maven Project`


3. Après un clic droit sur l'espace de nom `ch.asit_asso.extract.plugins.sample`, choisir le menu `Refactor > Rename`.
Saisir le nom de la nouvelle classe permettant d'identifier le plugin. Si l'interface le demande, cliquer sur le bouton 
`Add in current Module` afin d'appliquer les changements sur le mode uniquement.
Cela aura pour effet de modifier automatiquement le nom du package dans tous les fichiers du module.


4. Après un clic droit sur le fichier **SamplePlugin.java**, choisir le menu `Refactor > Rename` puis saisir le nom 
de la nouvelle classe principale du plugin. Cela aura pour effet de renommer le fichier et de modifer 
toutes les références à cette classe partout où elle est utilisée. 


5. Refaire l'opération 4 pour les fichiers **SampleRequest.java** et **SampleResult.java**


6. Editer le fichier **LocalizedMessages.java** : ajuster la valeur du paramètre LOCALIZED_FILE_PATH_FORMAT qui 
correspond au chemin vers le répertoire `lang`


7. Vérifier le fichier **module-info.java** en particulier la référence à `SamplePlugin`. Vérifier  également la ligne 4
de ce fichier (référence à l'espace de nom `ch.asit_asso.extract.plugins.sample`)


8. Vérifier le fichier **resources\META-INF\services\ch.asit_asso.extract.plugins.common.ITaskProcessor** : en particulier 
la cohérence de  la classe `ch.asit_asso.extract.plugins.sample.SamplePlugin` 


9. Après un clic droit sur le dossier **resources\plugins\sample**, choisir le menu `Refactor > Rename`. Saisir 
le nouveau nom


10. Editer le fichier **resources\plugins\<plugin>\lang\fr\messages.properties**. Modifier ou
ajouter les libellés qui seront utilisés par le code source du plugin. Cette étape peut se faire 
de manière progressive pendant le développement


11. Editer le fichier **resources\plugins\<plugin>\lang\fr\help.html** afin d'y décrire le focntionnement
du plugin. *Le contenu de fichier est au format HTML*


12. Editer le fichier **resources\plugins\<plugin>\properties\config.properties**. Ce ficher contient les
paramètres de configuration du plugn utilisés par le code source. Cette étape peut se faire
de manière progressive pendant le développement


## Points important à prendre en compte pendant le développement 

Le code source est suffisamment commenté afin d'aider le développeur à développer le 
nouveau plugin. Les commentaires en **MAJUSCULE** permettent d'identifer les parties de code ou les fonctions 
importantes à mettre à jour.

Il est notamment recommandé d'apporter les modifications suivantes dans la class Plugin:
* Ajuster si besoin la variable `CONFIG_FILE_PATH`
* Modifier la valeur du paramètre `code` par une valeur permettant d'identifier le plugin (e.g `remark` ou `fmeserver`)
* Changer la valeur du paramètre pictoClass par un nom de classe CSS approprié (image du logo du plugin). Chercher 
une icône sur le site [Font Awesome][Fontawesome_Site]


Ensuite, les fonctions à adapter sont celles qui surchargent les fonctions de l'interface 
ITaskProcessor :
* `getParams` pour définir les paramètres du connecteur. Cette méthode retourne les paramètres 
du plugin sous forme de tableau au format JSON. Si le plugin n’accepte pas de paramètres, renvoyer un tableau vide
* `execute` qui permet de gérer l'exécution du plugin

## Installation ou mise à jour du plugin dans EXTRACT 

Avant de compiler le plugin, supprimer le dossier **target**.\
Dès que le plugin est compilé et que le fichier jar est généré, il suffit de placer le JAR
dans le répertoire **WEB-INF/classes/task_processors** de l’application 
(contenant tous les plugins de tâches).\
En cas de mise à jour, il convient de supprimer le WAR de l’ancienne version afin d’éviter des conflits.

```
Le redémarrage de l’application Tomcat EXTRACT est ensuite requis afin que la 
modification des plugins soit prise en compte.
``` 


[Yarn_Site]: https://yarnpkg.com/ "Site du gestionnaire de package Yarn"
[Fontawesome_Site]: https://fontawesome.com/icons "Site web FontAwesome"