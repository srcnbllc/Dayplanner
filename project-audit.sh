#!/usr/bin/env bash
# project-audit.sh
# Single-file project auditor for Android (Kotlin) projects.
# Usage: run from project root: ./project-audit.sh
set -euo pipefail
TZ=UTC

## Config
REPORT_DIR="./audit-report-$(date +%Y%m%dT%H%M%SZ)"
mkdir -p "$REPORT_DIR"
LOG="$REPORT_DIR/audit_report.txt"
ERRS="$REPORT_DIR/errors.txt"
touch "$LOG" "$ERRS"
PROJECT_ROOT="$(pwd)"
APP_MODULE="app"   # change if different
GRADLEW="./gradlew"

echo "PROJECT AUDIT - $(date -u)" | tee "$LOG"

# helper
log() { echo "$*" | tee -a "$LOG"; }
warn() { echo "WARN: $*" | tee -a "$LOG" "$ERRS"; }
err() { echo "ERROR: $*" | tee -a "$LOG" "$ERRS"; }

# 1) Environment checks
log "1) ENVIRONMENT CHECKS"
command -v git >/dev/null 2>&1 || warn "git not installed"
[ -x "$GRADLEW" ] || warn "gradlew wrapper not found at $GRADLEW"
command -v xmllint >/dev/null 2>&1 || warn "xmllint not found: XML syntax checks will be skipped"
command -v aapt >/dev/null 2>&1 || warn "aapt not found (optional)"
command -v jq >/dev/null 2>&1 || warn "jq missing (optional for JSON parsing)"

# 2) Basic project structure
log ""
log "2) PROJECT STRUCTURE"
find . -maxdepth 2 -type d | sed 's/^\.\///' | tee -a "$LOG"
log ""
if [ -f "settings.gradle" ] || [ -f "settings.gradle.kts" ]; then
  log "Found settings.gradle(.kts)"
else
  warn "No settings.gradle(.kts) found — multi-module detection may fail"
fi

# 3) File lists
log ""
log "3) Resource & source lists"
find app/src -type f -name "*.xml" > "$REPORT_DIR/xml_files.txt" 2>/dev/null || true
find app/src -type f -name "*.kt" > "$REPORT_DIR/kt_files.txt" 2>/dev/null || true
find app/src -type f -name "*.java" > "$REPORT_DIR/java_files.txt" 2>/dev/null || true
find app/src/main/res -type f > "$REPORT_DIR/res_files.txt" 2>/dev/null || true
log "XML count: $(wc -l < "$REPORT_DIR/xml_files.txt" 2>/dev/null || echo 0)"
log "Kotlin files: $(wc -l < "$REPORT_DIR/kt_files.txt" 2>/dev/null || echo 0)"
log "Resources: $(wc -l < "$REPORT_DIR/res_files.txt" 2>/dev/null || echo 0)"

# 4) XML syntax check (layouts, nav graphs, drawables)
log ""
log "4) XML SYNTAX CHECK (xmllint)"
if command -v xmllint >/dev/null 2>&1; then
  echo "xmllint checks:" | tee -a "$LOG"
  while IFS= read -r f; do
    if [[ -f "$f" ]]; then
      if ! xmllint --noout "$f" 2>>"$ERRS"; then
        err "Malformed XML: $f"
      fi
    fi
  done < <(grep -R --line-number --exclude-dir={build,.gradle,.idea} -E '\.xml$' -l app/src || true)
else
  warn "xmllint not installed; skipping XML well-formedness checks"
fi

# 5) Build & LINT (gradle)
log ""
log "5) GRADLE BUILD & LINT (assembleDebug, lint)"
if [ -x "$GRADLEW" ]; then
  # try to assemble debug (non-failing: capture failures)
  set +e
  "$GRADLEW" :$APP_MODULE:assembleDebug --no-daemon --console=plain >"$REPORT_DIR/gradle_assemble.log" 2>&1
  ASSEMBLE_EXIT=$?
  set -e
  if [ $ASSEMBLE_EXIT -ne 0 ]; then
    err "gradle assembleDebug FAILED. See $REPORT_DIR/gradle_assemble.log"
  else
    log "gradle assembleDebug succeeded"
  fi

  # Lint
  set +e
  "$GRADLEW" :$APP_MODULE:lintDebug --no-daemon --console=plain >"$REPORT_DIR/gradle_lint.log" 2>&1
  LINT_EXIT=$?
  set -e
  if [ $LINT_EXIT -ne 0 ]; then
    warn "gradle lintDebug reported issues. See $REPORT_DIR/gradle_lint.log"
  else
    log "gradle lintDebug finished. Inspect $REPORT_DIR/gradle_lint.log"
  fi

else
  warn "gradlew not executable; skip build/lint"
fi

# 6) Find unused resources heuristics
log ""
log "6) UNUSED RESOURCES (heuristic)"
# 6a) R file references from sources
grep -R --line-number --exclude-dir={build,.gradle,.idea} -E "R\.drawable|R\.layout|R\.id|@drawable|@layout" app/src > "$REPORT_DIR/r_references.txt" || true
# 6b) list drawables and layouts
find app/src/main/res/drawable* -maxdepth 1 -type f -printf "%f\n" > "$REPORT_DIR/drawable_list.txt" 2>/dev/null || true
find app/src/main/res/layout -maxdepth 1 -type f -printf "%f\n" > "$REPORT_DIR/layout_list.txt" 2>/dev/null || true
# Attempt find drawables not referenced in code or xml
comm -23 <(sort "$REPORT_DIR/drawable_list.txt" 2>/dev/null || true) <(grep -oE '[a-z0-9_]+(?=\.(png|jpg|xml|webp))' "$REPORT_DIR/r_references.txt" 2>/dev/null | sort -u) > "$REPORT_DIR/possibly_unused_drawables.txt" || true
log "Possible unused drawables saved to $REPORT_DIR/possibly_unused_drawables.txt (heuristic). Review before removal."

# 7) Layout visibility & icon issues
log ""
log "7) LAYOUT VISIBILITY & ICON TINT CHECKS"
grep -R --line-number --exclude-dir={build,.gradle,.idea} 'android:visibility' app/src > "$REPORT_DIR/visibility_occurrences.txt" || true
grep -R --line-number --exclude-dir={build,.gradle,.idea} 'app:iconTint' app/src > "$REPORT_DIR/icon_tint_occurrences.txt" || true
grep -R --line-number --exclude-dir={build,.gradle,.idea} -E 'app:icon=|app:iconTint=|android:src=' app/src > "$REPORT_DIR/icon_usage.txt" || true
log "Visibility occurrences: $(wc -l < $REPORT_DIR/visibility_occurrences.txt 2>/dev/null || echo 0)"
log "Icon usages: $(wc -l < $REPORT_DIR/icon_usage.txt 2>/dev/null || echo 0)"
warn "If icon usages exist without iconTint or iconSize, icons may be invisible in some themes."

# 8) IDs referenced in code but missing in layouts (or vice versa)
log ""
log "8) ID CROSS-CHECK (code vs layouts)"
# collect ids from XML
grep -R --line-number --exclude-dir={build,.gradle,.idea} -oP 'android:id="@\+id/\K[^"]+' app/src | sort -u > "$REPORT_DIR/ids_in_layouts.txt" || true
# collect ids referenced in Kotlin/Java as R.id.xyz or viewBinding.id
grep -R --line-number --exclude-dir={build,.gradle,.idea} -oP 'R\.id\.\K[A-Za-z0-9_]+' app/src | sort -u > "$REPORT_DIR/ids_in_code.txt" || true
# Or viewBinding usage: binding.xyz
grep -R --line-number --exclude-dir={build,.gradle,.idea} -oP 'binding\.\K[A-Za-z0-9_]+' app/src | sort -u >> "$REPORT_DIR/ids_in_code.txt" || true
sort -u "$REPORT_DIR/ids_in_code.txt" -o "$REPORT_DIR/ids_in_code.txt" || true
comm -23 "$REPORT_DIR/ids_in_code.txt" "$REPORT_DIR/ids_in_layouts.txt" > "$REPORT_DIR/ids_ref_but_missing_in_layouts.txt" || true
comm -23 "$REPORT_DIR/ids_in_layouts.txt" "$REPORT_DIR/ids_in_code.txt" > "$REPORT_DIR/ids_in_layouts_but_not_referenced.txt" || true
log "IDs referenced in code but missing in layouts: $(wc -l < $REPORT_DIR/ids_ref_but_missing_in_layouts.txt 2>/dev/null || echo 0)"
log "IDs in layouts but not referenced in code: $(wc -l < $REPORT_DIR/ids_in_layouts_but_not_referenced.txt 2>/dev/null || echo 0)"

# 9) Navigation graph checks (XML destinations exist)
log ""
log "9) NAVIGATION GRAPH vs Fragment/Activities"
NAV_FILES=$(find app/src -type f -path '*res/navigation/*.xml' 2>/dev/null || true)
if [ -n "$NAV_FILES" ]; then
  echo "$NAV_FILES" | tee -a "$LOG"
  for nav in $NAV_FILES; do
    log "Inspecting $nav"
    # list fragment destinations
    grep -oP 'destination="@id/\K[^"]+' "$nav" | sort -u > "$REPORT_DIR/dest_ids_in_nav.txt" || true
    # check corresponding fragment classes existence
    grep -oP 'class="[^"]+"' "$nav" | sed -E 's/class="//;s/"$//' > "$REPORT_DIR/dest_classes_in_nav.txt" || true
    # verify classes exist in source tree
    while IFS= read -r cls; do
      if ! grep -R --line-number --exclude-dir={build,.gradle,.idea} "$(basename $cls)" app/src >/dev/null 2>&1; then
        warn "Nav destination class missing or rename detected: $cls"
      fi
    done < "$REPORT_DIR/dest_classes_in_nav.txt" || true
  done
else
  warn "No navigation graphs found under res/navigation"
fi

# 10) Room / Converters / Flow-LiveData heuristics
log ""
log "10) ROOM / Flow / LiveData HEURISTICS"
grep -R --line-number --exclude-dir={build,.gradle,.idea} '@Entity' app/src > "$REPORT_DIR/entities.txt" || true
grep -R --line-number --exclude-dir={build,.gradle,.idea} '@TypeConverter' app/src > "$REPORT_DIR/type_converters.txt" || true
grep -R --line-number --exclude-dir={build,.gradle,.idea} '\.asLiveData\(|Flow<' app/src > "$REPORT_DIR/flow_live_usage.txt" || true
log "Entities found: $(wc -l < $REPORT_DIR/entities.txt 2>/dev/null || echo 0)"
if [ ! -s "$REPORT_DIR/type_converters.txt" ]; then
  warn "No @TypeConverter found (if using enums in Room, add converters)."
fi

# 11) Search for suspicious infinite loops & blocking calls
log ""
log "11) INFINITE LOOPS / BLOCKING CALLS HEURISTIC"
grep -R --line-number --exclude-dir={build,.gradle,.idea} -E 'while\s*\(\s*true\s*\)|for\s*\(\s*;;\s*\)' app/src > "$REPORT_DIR/infinite_loops.txt" || true
grep -R --line-number --exclude-dir={build,.gradle,.idea} -E 'Thread\.sleep\(|System\.exit\(|Runtime\.getRuntime' app/src > "$REPORT_DIR/blocking_calls.txt" || true
grep -R --line-number --exclude-dir={build,.gradle,.idea} 'Toast\.makeText' app/src > "$REPORT_DIR/toast_occurrences.txt" || true
log "Possible infinite loops: $(wc -l < $REPORT_DIR/infinite_loops.txt 2>/dev/null || echo 0)"
log "Blocking calls: $(wc -l < $REPORT_DIR/blocking_calls.txt 2>/dev/null || echo 0)"
log "Toast occurrences: $(wc -l < $REPORT_DIR/toast_occurrences.txt 2>/dev/null || echo 0)"
warn "Check Toast usage inside loops or background threads."

# 12) Manifest checks
log ""
log "12) MANIFEST CHECKS"
if [ -f app/src/main/AndroidManifest.xml ]; then
  grep -n --line-number '<uses-permission' app/src/main/AndroidManifest.xml >"$REPORT_DIR/manifest_permissions.txt" || true
  grep -n --line-number 'android:exported' app/src/main/AndroidManifest.xml > "$REPORT_DIR/manifest_exported.txt" || true
  log "Permissions listed: $(wc -l < $REPORT_DIR/manifest_permissions.txt 2>/dev/null || echo 0)"
else
  warn "app/src/main/AndroidManifest.xml not found"
fi

# 13) Styles & Theme potential issues
log ""
log "13) THEME/STYLE CHECKS"
find app/src/main/res/values* -type f -name "themes*.xml" -o -name "styles*.xml" > "$REPORT_DIR/theme_files.txt" 2>/dev/null || true
if [ -s "$REPORT_DIR/theme_files.txt" ]; then
  log "Theme files:"
  sed -n '1,200p' "$REPORT_DIR/theme_files.txt" | tee -a "$LOG"
else
  warn "No theme/style files found in res/values*"
fi

# 14) Find hardcoded strings (for i18n)
log ""
log "14) HARDCODED STRINGS"
grep -R --line-number --exclude-dir={build,.gradle,.idea} -E 'android:text="[^@]' app/src > "$REPORT_DIR/hardcoded_strings.txt" || true
log "Hardcoded strings occurrences: $(wc -l < $REPORT_DIR/hardcoded_strings.txt 2>/dev/null || echo 0)"

# 15) Report summary
log ""
log "15) SUMMARY & NEXT STEPS"
echo "FILES GENERATED: $(ls -1 $REPORT_DIR | wc -l)" | tee -a "$LOG"
echo "Inspect $REPORT_DIR for detailed files. Critical errors written to $ERRS" | tee -a "$LOG"
if [ -s "$ERRS" ]; then
  err "Errors/warnings were recorded. Open $ERRS and $LOG"
else
  log "No immediate parse errors found."
fi

# 16) Safety: suggestions for automated cleanup (commented out)
cat > "$REPORT_DIR/cleanup_instructions.sh" <<'CLEAN'
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
CLEAN

log "Cleanup instructions created: $REPORT_DIR/cleanup_instructions.sh (disabled by default for safety)"

# 17) Final note file with manual checks / Android Studio tasks
cat > "$REPORT_DIR/manual_checks.md" <<'MD'
# Manual checks to run in Android Studio / device
1. Run: Build -> Clean Project, Build -> Rebuild Project.
2. Open Logcat, filter by Errors. Run the app and interact with finance screen.
3. Use Layout Inspector (View -> Tool Windows -> Layout Inspector) while the finance screen is visible.
4. Use "Analyze -> Inspect Code" in Android Studio and examine the "Android" and "Lint" profiles.
5. Manually verify:
   - Every nav_graph destination has a matching Fragment/Activity class.
   - Every MaterialButton with app:icon has app:iconTint and app:iconSize.
   - DropDown (AutoCompleteTextView) has dropDownBackground set (for dark/light themes).
6. For visuals: check values-night and values/themes.xml for color conflicts (icon text same as background).
7. If using vector drawables: ensure vectorDrawables.useSupportLibrary = true in module build.gradle.
MD

log "Manual checks guide saved: $REPORT_DIR/manual_checks.md"

# 18) Cursor/AI limitations and mitigation
cat > "$REPORT_DIR/ai_limitations.md" <<'MD'
# Cursor/AI Limitations & Mitigation (auto audit)
- Static analysis cannot guarantee runtime state: visibility toggled at runtime (View.setVisibility) may hide views even if XML says VISIBLE.
  * Mitigation: use Layout Inspector + runtime logs.
- Heuristics may list false positives for unused resources (some drawables loaded dynamically by name).
  * Mitigation: backup then remove; prefer grep for dynamic references (e.g. getDrawable(context.resources.getIdentifier(...))).
- Complex Kotlin usage (reflection, generated code, databinding, kapt-generated ids) may confuse simple regex search.
  * Mitigation: run Android Studio Inspections and use the IDE's "Find Usages".
- Detection of logic bugs (race conditions, coroutine misuse) requires manual test scenarios and code review.
  * Mitigation: add unit & instrumentation tests; use strict lint/detekt rules.
MD

log "AI limitations doc saved: $REPORT_DIR/ai_limitations.md"

echo "AUDIT COMPLETE. Review files in $REPORT_DIR. Important: do NOT run cleanup move/delete commands without review." | tee -a "$LOG"
