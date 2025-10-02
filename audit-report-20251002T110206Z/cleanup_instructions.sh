#!/usr/bin/env bash
# REVIEW BEFORE RUNNING: This script will *move* possibly-unused drawables to a backup folder.
# Edit variables below then run with: bash cleanup_instructions.sh

PROJECT_ROOT="."           # change if not root
BACKUP_DIR="./deleted_unused_backup_$(date +%Y%m%dT%H%M%SZ)"
mkdir -p "$BACKUP_DIR"

# Example move command (UNCOMMENT to execute)
# while read -r f; do
#   mv "$PROJECT_ROOT/app/src/main/res/drawable/$f" "$BACKUP_DIR/"
# done < "./audit-report-*/possibly_unused_drawables.txt"

echo "Dry-run ready. Files listed in possibly_unused_drawables.txt will be moved to $BACKUP_DIR when you uncomment the move loop."
