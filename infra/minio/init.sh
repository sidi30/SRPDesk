#!/bin/sh
# Initialize MinIO bucket for evidences storage
set -e

mc alias set local http://localhost:9000 minioadmin minioadmin
mc mb local/evidences --ignore-existing
echo "Bucket 'evidences' created successfully"
