/**
 * @file Creates an object containing the localized messages using by scripts.
 * @author Yves Grasset
 */

/**
 * Localized messages to be used by scripts.
 * 
 * @type Object
 */
var LANG_MESSAGES = {
    "connectorsList" : {
        "deleteConfirm" : {
            "title" : "Suppression d'un connecteur",
            "message" : "Êtes-vous sûr de vouloir supprimer le connecteur \"{0}\" ?"
        }
    },
    "processesList" : {
        "cloneConfirm" : {
            "title" : "Duplication d'un traitement",
            "message" : "Êtes-vous sûr de vouloir dupliquer le traitement \"{0}\" ?"
        },
        "deleteConfirm" : {
            "title" : "Suppression d'un traitement",
            "message" : "Êtes-vous sûr de vouloir supprimer le traitement \"{0}\" ?"
        }
    },
    "processTask": {
        "deleteConfirm" : {
            "title" : "Suppression d'une tâche",
            "message" : "Êtes-vous sûr de vouloir supprimer cette tâche ?"
        }
    },
    "requestDetails" : {
        "deleteConfirm" : {
            "title" : "Suppression d'une demande",
            "message" : "Êtes-vous sûr de vouloir supprimer la demande \"{0}\" ?\n\nCette action est irréversible."
        },
        "deleteFileConfirm" : {
            "title" : "Suppression d'un fichier de sortie",
            "message" : "Êtes-vous sûr de vouloir supprimer le fichier \"{0}\" de la demande \"{1}\" ?\n\nCette action est irréversible."
        },
        "rejectConfirm" : {
            "title" : "Annulation d'une demande",
            "message" : "Êtes-vous sûr de vouloir annuler le traitement de la demande \"{0}\" ?"
        },
        "relaunchProcessConfirm" : {
            "title" : "Redémarrage du traitement",
            "message" : "Êtes-vous sûr de vouloir redémarrer le traitement de la demande \"{0}\" depuis le début ?"
        },
        "restartTaskConfirm" : {
            "title" : "Redémarrage de la tâche courante",
            "message" : "Êtes-vous sûr de vouloir redémarrer la tâche courante de la demande \"{0}\" ?"
        },
        "retryExportConfirm" : {
            "title" : "Nouvelle tentative d'export",
            "message" : "Êtes-vous sûr de vouloir relancer l'export de la demande \"{0}\" ?"
        },
        "retryMatchingConfirm" : {
            "title" : "Nouvelle tentative d'association à un traitement",
            "message" : "Êtes-vous sûr de vouloir chercher à nouveau un traitement pour la demande \"{0}\" ?"
        },
        "skipTaskConfirm" : {
            "title" : "Abandon de la tâche courante",
            "message" : "Êtes-vous sûr de vouloir poursuivre le traitement de la demande \"{0}\" en abandonnant la tâche courante ?"
        },
        "validateConfirm" : {
            "title" : "Validation d'une demande",
            "message" : "Êtes-vous sûr de vouloir valider la demande \"{0}\" ?"
        },
        "exportToDxf" : {
            "label": "DXF",
            "tooltip": "Télécharger le polygone d'emprise de la commande au format DXF"
        },
        "exportToKml" : {
            "label": "KML",
            "tooltip": "Télécharger le polygone d'emprise de la commande au format KML"
        },
        "fullScreenControl" : {
          "tooltip": "Afficher la carte en plein écran"
        },
        "layerSwitcher" : {
            "tooltip": "Gestion des couches"
        },
        "mapLayers" : {
            "polygon" : {
                "title": "Polygone d'emprise"
            }
        }
    },
    "remarksList" : {
        "deleteConfirm" : {
            "title" : "Suppression d'un message",
            "message" : "Êtes-vous sûr de vouloir supprimer le message \"{0}\" ?"
        }
    },
    "rulesList" : {
        "deleteConfirm" : {
            "title" : "Suppression d'une règle",
            "message" : "Êtes-vous sûr de vouloir supprimer la règle {0} ?"
        }
    },
    "usersList" : {
        "deleteConfirm" : {
            "title" : "Suppression d'un utilisateur",
            "message" : "Êtes-vous sûr de vouloir supprimer l'utilisateur \"{0}\" ?"
        }
    },
    "userGroupsList" : {
        "deleteConfirm" : {
            "title" : "Suppression d'un groupe d'utilisateurs",
            "message" : "Êtes-vous sûr de vouloir supprimer le groupe d'utilisateurs \"{0}\" ?"
        }
    },
    "generic" : {
        "alertButtons" : {
            "cancel" : "Annuler",
            "no" : "Non",
            "ok": "OK",
            "yes": "Oui"
        },
        "notImplemented" : {
            "title" : "Pas encore implémenté",
            "message" : "Désolé, cette fonction n'est pas encore disponible."
        }
    }
};


var RULE_HELP_CONTENT = 'static/lang/fr/rulesHelp.html';

// Strings used only during development. What follows will be removed from production code
LANG_MESSAGES['development'] = {
    "notImplemented" : "Pas encore développé",
    "notImplementedLong" : "Cette fonction n'a pas encore été développée."
};
