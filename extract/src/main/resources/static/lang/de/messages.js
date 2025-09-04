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
            "title" : "Löschung eines Verbinders",
            "message" : "Sind Sie sicher, dass Sie den Verbinder \"{0}\" löschen möchten?"
        }
    },
    "processesList" : {
        "cloneConfirm" : {
            "title" : "Duplizierung einer Verarbeitung",
            "message" : "Sind Sie sicher, dass Sie die Verarbeitung \"{0}\" duplizieren möchten?"
        },
        "deleteConfirm" : {
            "title" : "Löschung einer Verarbeitung",
            "message" : "Sind Sie sicher, dass Sie die Verarbeitung \"{0}\" löschen möchten?"
        }
    },
    "processTask": {
        "deleteConfirm" : {
            "title" : "Löschung einer Aufgabe",
            "message" : "Sind Sie sicher, dass Sie diese Aufgabe löschen möchten?"
        }
    },
    "requestDetails" : {
        "deleteConfirm" : {
            "title" : "Löschung einer Anfrage",
            "message" : "Sind Sie sicher, dass Sie die Anfrage \"{0}\" löschen möchten?\n\nDiese Aktion ist unwiderruflich."
        },
        "deleteFileConfirm" : {
            "title" : "Löschung einer Ausgabedatei",
            "message" : "Sind Sie sicher, dass Sie die Datei \"{0}\" der Anfrage \"{1}\" löschen möchten?\n\nDiese Aktion ist unwiderruflich."
        },
        "rejectConfirm" : {
            "title" : "Abbruch einer Anfrage",
            "message" : "Sind Sie sicher, dass Sie die Verarbeitung der Anfrage \"{0}\" abbrechen möchten?"
        },
        "relaunchProcessConfirm" : {
            "title" : "Neustart der Verarbeitung",
            "message" : "Sind Sie sicher, dass Sie die Verarbeitung der Anfrage \"{0}\" von Anfang an neu starten möchten?"
        },
        "restartTaskConfirm" : {
            "title" : "Neustart der aktuellen Aufgabe",
            "message" : "Sind Sie sicher, dass Sie die aktuelle Aufgabe der Anfrage \"{0}\" neu starten möchten?"
        },
        "retryExportConfirm" : {
            "title" : "Neuer Exportversuch",
            "message" : "Sind Sie sicher, dass Sie den Export der Anfrage \"{0}\" neu starten möchten?"
        },
        "retryMatchingConfirm" : {
            "title" : "Neuer Versuch der Zuordnung zu einer Verarbeitung",
            "message" : "Sind Sie sicher, dass Sie erneut eine Verarbeitung für die Anfrage \"{0}\" suchen möchten?"
        },
        "skipTaskConfirm" : {
            "title" : "Verlassen der aktuellen Aufgabe",
            "message" : "Sind Sie sicher, dass Sie die Verarbeitung der Anfrage \"{0}\" fortsetzen möchten, indem Sie die aktuelle Aufgabe verlassen?"
        },
        "validateConfirm" : {
            "title" : "Validierung einer Anfrage",
            "message" : "Sind Sie sicher, dass Sie die Anfrage \"{0}\" validieren möchten?"
        },
        "exportToDxf" : {
            "label": "DXF",
            "tooltip": "Den Umfangspolygon der Bestellung im DXF-Format herunterladen"
        },
        "exportToKml" : {
            "label": "KML",
            "tooltip": "Den Umfangspolygon der Bestellung im KML-Format herunterladen"
        },
        "fullScreenControl" : {
          "tooltip": "Karte im Vollbild anzeigen"
        },
        "layerSwitcher" : {
            "tooltip": "Ebenenverwaltung"
        },
        "mapLayers" : {
            "polygon" : {
                "title": "Umfangspolygon"
            }
        }
    },
    "remarksList" : {
        "deleteConfirm" : {
            "title" : "Löschung einer Nachricht",
            "message" : "Sind Sie sicher, dass Sie die Nachricht \"{0}\" löschen möchten?"
        }
    },
    "rulesList" : {
        "deleteConfirm" : {
            "title" : "Löschung einer Regel",
            "message" : "Sind Sie sicher, dass Sie die Regel {0} löschen möchten?"
        }
    },
    "usersList" : {
        "deleteConfirm" : {
            "title" : "Löschung eines Benutzers",
            "message" : "Sind Sie sicher, dass Sie den Benutzer \"{0}\" löschen möchten?"
        }
    },
    "userDetails": {
        "migrateConfirm": {
            "title": "Migration eines Benutzers zu LDAP",
            "message": "Sind Sie sicher, dass Sie diesen Benutzer zu LDAP migrieren möchten?\n\nDer Benutzer muss sich zwingend mit seinen LDAP-Anmeldedaten anmelden.\n\nDiese Aktion ist nicht umkehrbar.\n\nDarüber hinaus gehen eventuelle andere Änderungen verloren.",
            "alertButtons": {
                "execute": "Benutzer migrieren"
            }
        },
        "disable2faConfirm": {
            "title": "Deaktivierung der Zweifaktor-Authentifizierung",
            "message": "Sind Sie sicher, dass Sie die Zweifaktor-Authentifizierung für diesen Benutzer deaktivieren möchten?\n\nEventuelle andere Änderungen gehen verloren.",
            "alertButtons": {
                "execute": "Deaktivieren"
            }
        },
        "enable2faConfirm": {
            "title": "Aktivierung der Zweifaktor-Authentifizierung",
            "message": "Sind Sie sicher, dass Sie die Zweifaktor-Authentifizierung für diesen Benutzer aktivieren möchten?\n\nEventuelle andere Änderungen gehen verloren.",
            "alertButtons": {
                "execute": "Aktivieren"
            }
        },
        "reset2faConfirm": {
            "title": "Zurücksetzung der Zweifaktor-Authentifizierung",
            "message": "Sind Sie sicher, dass Sie die Zweifaktor-Authentifizierung für diesen Benutzer zurücksetzen möchten?\n\nEventuelle andere Änderungen gehen verloren.",
            "alertButtons": {
                "execute": "Zurücksetzen"
            }
        }
    },
    "userGroupsList" : {
        "deleteConfirm" : {
            "title" : "Löschung einer Benutzergruppe",
            "message" : "Sind Sie sicher, dass Sie die Benutzergruppe \"{0}\" löschen möchten?"
        }
    },
    "generic" : {
        "alertButtons" : {
            "cancel" : "Abbrechen",
            "no" : "Nein",
            "ok": "OK",
            "yes": "Ja"
        },
        "notImplemented" : {
            "title" : "Noch nicht implementiert",
            "message" : "Entschuldigung, diese Funktion ist noch nicht verfügbar."
        }
    }
};


var RULE_HELP_CONTENT = 'static/lang/en/rulesHelp.html';

// Strings used only during development. What follows will be removed from production code
LANG_MESSAGES['development'] = {
    "notImplemented" : "Noch nicht entwickelt",
    "notImplementedLong" : "Diese Funktion wurde noch nicht entwickelt."
};
