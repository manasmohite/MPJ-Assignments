#!/bin/bash
# ═══════════════════════════════════════════════════════════
#  Campus Navigator — Build & Run Script (macOS)
#  Usage: ./run.sh [compile|seed|run|all]
#    all    = compile + seed + run  (first-time setup)
#    compile = compile only
#    seed   = run SeedDB to set passwords & sample data
#    run    = run the application (no recompile)
# ═══════════════════════════════════════════════════════════

set -e

# ── Configuration ──────────────────────────────────────────
APP_NAME="CampusNavigator"
SRC_DIR="src"
OUT_DIR="out"
LIB_DIR="lib"
MAIN_CLASS="Main"
SEED_CLASS="SeedDB"

# ── Colors ─────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; NC='\033[0m'; BOLD='\033[1m'

header() { echo -e "\n${CYAN}${BOLD}═══ $1 ═══${NC}"; }
ok()     { echo -e "${GREEN}✓${NC}  $1"; }
warn()   { echo -e "${YELLOW}⚠${NC}  $1"; }
err()    { echo -e "${RED}✗${NC}  $1"; }

# ── Find MySQL connector JAR ────────────────────────────────
find_jar() {
    JAR=$(find "$LIB_DIR" -name "mysql-connector-j-*.jar" 2>/dev/null | head -1)
    if [ -z "$JAR" ]; then
        JAR=$(find "$LIB_DIR" -name "mysql-connector*.jar" 2>/dev/null | head -1)
    fi
    if [ -z "$JAR" ]; then
        err "MySQL connector JAR not found in $LIB_DIR/"
        echo "    Download from: https://dev.mysql.com/downloads/connector/j/"
        echo "    Place the .jar file in the $LIB_DIR/ directory."
        exit 1
    fi
    echo "$JAR"
}

# ── Compile ─────────────────────────────────────────────────
do_compile() {
    header "Compiling"
    JAR=$(find_jar)
    ok "Found JAR: $JAR"

    mkdir -p "$OUT_DIR"

    # Find all .java files
    JAVA_FILES=$(find "$SRC_DIR" -name "*.java")
    COUNT=$(echo "$JAVA_FILES" | wc -l | tr -d ' ')
    echo "  Compiling $COUNT Java source files..."

    javac -cp ".:$JAR" \
          -sourcepath "$SRC_DIR" \
          -d "$OUT_DIR" \
          $JAVA_FILES

    # Copy db.properties to output
    cp "$SRC_DIR/db.properties" "$OUT_DIR/"

    ok "Compilation successful → $OUT_DIR/"
}

# ── Seed DB ─────────────────────────────────────────────────
do_seed() {
    header "Seeding Database"
    JAR=$(find_jar)
    java -cp "$OUT_DIR:$JAR" "$SEED_CLASS"
}

# ── Run ─────────────────────────────────────────────────────
do_run() {
    header "Launching $APP_NAME"
    JAR=$(find_jar)
    java -cp "$OUT_DIR:$JAR" "$MAIN_CLASS"
}

# ── Main ────────────────────────────────────────────────────
CMD="${1:-all}"

case "$CMD" in
    compile) do_compile ;;
    seed)    do_seed ;;
    run)     do_run ;;
    all)
        do_compile
        do_seed
        do_run
        ;;
    *)
        echo "Usage: $0 [compile|seed|run|all]"
        exit 1
        ;;
esac
