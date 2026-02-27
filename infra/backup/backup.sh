#!/bin/bash
# SRPDesk — Sauvegarde automatisee PostgreSQL
# Execute par le conteneur backup via cron Docker
set -euo pipefail

BACKUP_DIR="/backups"
RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-30}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
FILENAME="srpdesk_${TIMESTAMP}.dump"

echo "[$(date -Iseconds)] Debut sauvegarde PostgreSQL..."

# Dump compresse (format custom pg_restore)
PGPASSWORD="${POSTGRES_PASSWORD}" pg_dump \
    -h postgres \
    -U "${POSTGRES_USER}" \
    -d "${POSTGRES_DB}" \
    -Fc \
    --no-owner \
    --no-privileges \
    > "${BACKUP_DIR}/${FILENAME}"

SIZE=$(du -h "${BACKUP_DIR}/${FILENAME}" | cut -f1)
echo "[$(date -Iseconds)] Sauvegarde terminee : ${FILENAME} (${SIZE})"

# Supprimer les backups de plus de N jours
DELETED=$(find "${BACKUP_DIR}" -name "srpdesk_*.dump" -mtime +${RETENTION_DAYS} -delete -print | wc -l)
if [ "$DELETED" -gt 0 ]; then
    echo "[$(date -Iseconds)] ${DELETED} ancien(s) backup(s) supprime(s) (retention: ${RETENTION_DAYS}j)"
fi

echo "[$(date -Iseconds)] Sauvegarde OK"
