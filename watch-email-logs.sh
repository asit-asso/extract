#!/bin/bash

# Script pour surveiller les logs du plugin email en temps réel

echo "==================================="
echo "Surveillance des logs Email Extract"
echo "==================================="
echo ""
echo "Logs surveillés :"
echo "- /var/log/extract/email.*.log (logs email)"
echo "- /var/log/extract/task-plugins.*.log (logs des plugins de tâches)"
echo "- /var/log/extract/extract.*.log (logs généraux)"
echo ""
echo "Appuyez sur Ctrl+C pour arrêter"
echo "==================================="
echo ""

# Vérifier si les fichiers de log existent
if [ ! -d "/var/log/extract" ]; then
    echo "ATTENTION: Le répertoire /var/log/extract n'existe pas"
    echo "Les logs pourraient être dans un autre emplacement"
    echo ""
    echo "Recherche des logs dans le répertoire actuel..."
    find . -name "*.log" -type f 2>/dev/null | head -10
    echo ""
fi

# Surveiller plusieurs fichiers de log en même temps
tail -f /var/log/extract/email.*.log \
        /var/log/extract/task-plugins.*.log \
        /var/log/extract/extract.*.log 2>/dev/null | \
    grep -E "(EmailPlugin|Email|email|SMTP|smtp|mail|Mail)" --color=auto