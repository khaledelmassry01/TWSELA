#!/bin/bash

# Twsela Database Backup Script
# This script creates automated backups of the Twsela database

set -e

# Configuration
MYSQL_HOST=${MYSQL_HOST:-mysql}
MYSQL_USER=${MYSQL_USER:-root}
MYSQL_PASSWORD=${MYSQL_PASSWORD:-root}
MYSQL_DATABASE=${MYSQL_DATABASE:-twsela}
BACKUP_PATH=${BACKUP_PATH:-/backups}
RETENTION_DAYS=${RETENTION_DAYS:-30}

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_PATH"

# Generate backup filename with timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="twsela_backup_${TIMESTAMP}.sql"
BACKUP_PATH_FULL="$BACKUP_PATH/$BACKUP_FILE"

echo "Starting backup at $(date)"
echo "Backup file: $BACKUP_PATH_FULL"

# Create backup using mysqldump
mysqldump \
  -h "$MYSQL_HOST" \
  -u "$MYSQL_USER" \
  -p"$MYSQL_PASSWORD" \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  --add-drop-database \
  --databases "$MYSQL_DATABASE" \
  > "$BACKUP_PATH_FULL"

# Check if backup was successful
if [ $? -eq 0 ]; then
  echo "Backup created successfully: $BACKUP_PATH_FULL"
  
  # Compress backup file
  gzip "$BACKUP_PATH_FULL"
  echo "Backup compressed: $BACKUP_PATH_FULL.gz"
  
  # Clean old backups
  echo "Cleaning backups older than $RETENTION_DAYS days"
  find "$BACKUP_PATH" -name "twsela_backup_*.sql.gz" -mtime +$RETENTION_DAYS -delete
  
  echo "Backup completed successfully at $(date)"
else
  echo "Backup failed at $(date)"
  exit 1
fi
