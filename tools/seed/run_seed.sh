#!/usr/bin/env bash
# Runner for seed_artifacts.py -- Supabase values are pre-filled from the Android
# project's app/src/main/res/values/strings.xml (read at build time, not guessed).
#
#   ./run_seed.sh                 -> dry run only (safe, writes nothing)
#   ./run_seed.sh --go            -> dry run, then prompt, then write for real
#   ./run_seed.sh --go --overwrite-> same, but replaces existing lot numbers
#
# Needs: the Firebase service account key JSON in this folder, and DATABASE_URL below.

set -euo pipefail
cd "$(dirname "$0")"

# --- config from strings.xml (project: goldengeese-c4141) --------------------
SUPABASE_URL="https://uvnrdrmwhkhlfxkgxzmk.supabase.co"
SUPABASE_KEY="sb_publishable_4vNG3L02W_ce0H5ZTQmTew_-EinHYN1"
SUPABASE_BUCKET="artifacts"

# --- Realtime Database URL ---------------------------------------------------
# Read off the Firebase Console (database location: United States / us-central1).
# Note it is NOT in google-services.json -- see SEEDING_README.md, this also means
# the Android app itself currently can't resolve the database URL at runtime.
DATABASE_URL="${DATABASE_URL:-https://goldengeese-c4141-default-rtdb.firebaseio.com}"

# --- service account key -----------------------------------------------------
# Firebase Console -> Project Settings -> Service Accounts -> Generate new private key.
# Drop the downloaded .json in this folder; it is found automatically.
KEY="${SERVICE_ACCOUNT:-}"
if [ -z "$KEY" ]; then
  KEY=$(ls -1 goldengeese-c4141-firebase-adminsdk-*.json *firebase-adminsdk*.json 2>/dev/null | head -1 || true)
fi

# --- preflight ---------------------------------------------------------------
PY=$(command -v python3 || true)
[ -z "$PY" ] && { echo "ERROR: python3 not found. Install it, then re-run."; exit 1; }

echo "Python:   $($PY --version)"
$PY -c "import firebase_admin, requests" 2>/dev/null || {
  echo "Installing firebase-admin and requests..."
  $PY -m pip install --quiet --user firebase-admin requests || \
  $PY -m pip install --quiet --break-system-packages firebase-admin requests
}

if [ ! -f seed_data/artifacts.json ]; then
  echo "ERROR: seed_data/artifacts.json not found next to this script."; exit 1
fi

# --- always dry-run first ----------------------------------------------------
echo
echo "=============== DRY RUN (nothing is written) ==============="
$PY seed_artifacts.py --data-dir seed_data --dry-run

if [ "${1:-}" != "--go" ]; then
  echo
  echo "Dry run only. Re-run with:  ./run_seed.sh --go   to write for real."
  exit 0
fi
shift

# --- checks that only matter for a real run ----------------------------------
if [ -z "$DATABASE_URL" ]; then
  echo
  echo "ERROR: DATABASE_URL is not set."
  echo "  Edit this file and fill in DATABASE_URL, or run:"
  echo "  DATABASE_URL='https://...' ./run_seed.sh --go"
  exit 1
fi
if [ -z "$KEY" ] || [ ! -f "$KEY" ]; then
  echo
  echo "ERROR: no Firebase service account key found in $(pwd)."
  echo "  Firebase Console -> Project Settings -> Service Accounts ->"
  echo "  Generate new private key, then save the .json in this folder."
  exit 1
fi

echo
echo "Service account: $KEY"
echo "Database:        $DATABASE_URL"
echo "Supabase bucket: $SUPABASE_BUCKET"
echo "About to upload 31 images and write 31 records to /artifacts/{1..31}."
[ "${1:-}" = "--overwrite" ] && echo "!! --overwrite: existing lot numbers WILL be replaced."
printf "Type 'yes' to continue: "
read -r CONFIRM
[ "$CONFIRM" = "yes" ] || { echo "Aborted."; exit 1; }

echo
echo "=============== LIVE RUN ==============="
$PY seed_artifacts.py \
  --data-dir seed_data \
  --service-account "$KEY" \
  --database-url "$DATABASE_URL" \
  --supabase-url "$SUPABASE_URL" \
  --supabase-key "$SUPABASE_KEY" \
  --supabase-bucket "$SUPABASE_BUCKET" \
  "$@"
