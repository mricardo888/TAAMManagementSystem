# Artifact seeding tool

One-off admin script that bulk-loads artifact records (with images) into the backend
used by the TAAM Artifact Management System Android app. **It does not run inside the
app and is not shipped with it** — keep it out of `app/src/`.

## Setup

```bash
pip install firebase-admin requests
```

## Run

```bash
python seed_artifacts.py \
  --data-dir seed_data \
  --service-account key.json \
  --database-url https://<project-id>-default-rtdb.<region>.firebasedatabase.app \
  --supabase-url https://<project-ref>.supabase.co \
  --supabase-key <anon key> \
  --supabase-bucket artifacts
```

Validate first without touching either service:

```bash
python seed_artifacts.py --data-dir seed_data --dry-run
```

| Flag | Where to get it |
|---|---|
| `--supabase-url` | `app/src/main/res/values/strings.xml` → `supabase_url` |
| `--supabase-key` | `strings.xml` → `supabase_anon_key` |
| `--supabase-bucket` | `strings.xml` → `supabase_image_bucket` |
| `--service-account` | Firebase Console → Project Settings → Service Accounts → Generate new private key |
| `--database-url` | `https://goldengeese-c4141-default-rtdb.firebaseio.com` — confirmed from the console (region: us-central1). Already pre-filled in `run_seed.sh`. |

## ⚠ The app needs configuration before it can read any of this

Seeding the data is not enough on its own — the Android app has two project-config
blockers that are unrelated to this script. See [`docs/FIREBASE_SETUP.md`](../../docs/FIREBASE_SETUP.md)
for both, and for what every teammate has to set up locally.

Every flag also reads an env var: `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_BUCKET`,
`GOOGLE_APPLICATION_CREDENTIALS`, `FIREBASE_DATABASE_URL`.

Other flags: `--overwrite` replaces records whose lot number already exists (default is
to skip them with a warning); `--dry-run` validates and reports without writing.

## ⚠ Never commit the service account key

The key grants full admin access to the database and bypasses all security rules.
The repo's `.gitignore` already blocks the usual filenames (`*firebase-adminsdk*.json`,
`serviceAccountKey.json`, `service-account*.json`) — keep the key **outside** the repo
anyway, and never relax those rules. If a key is ever committed, revoke it in the
Firebase Console rather than just deleting the file: it stays in the git history.

## What it writes

One JSON object per artifact at `/artifacts/{lotNum}` via a plain `.set()`. The lot
number *is* the uniqueness constraint — there is no separate id field. Exactly these
15 keys are written, matching the app's `Artifact` POJO:

`lotNum, name, description, category, material, dynasty, origin, dimensions,
conditionReport, location, acqMethod, provenance, accessionNum, notes, image`

`likes`, `comments`, `likedBy` and `savedBy` are deliberately **not** written — the
live app manages those. This matches `FirebaseArtifactRepository.addArtifact()`, which
also does a plain `setValue(artifact)` on a fresh lot number.

> **`--overwrite` is destructive in one specific way.** The app stores `likedBy` and
> `savedBy` as *children of* `/artifacts/{lotNum}`, so a `.set()` over an existing
> record erases those lists. (Comments live at `/comments/{lotNum}` and survive.) The
> app's own edit path uses `updateChildren()` precisely to avoid this. The script now
> warns before each overwrite. On an empty database this is moot.

Images upload to Supabase Storage at `artifacts/{lotNum}/{unix_millis}.{ext}` (the
same convention as `SupabaseImageUploader.java`), and the resulting public URL becomes
the `image` field. jpg/jpeg/png/gif/webp only, 10MB max — same limits as the app.

## Input format

```
seed_data/
├── artifacts.json     # JSON array, one object per artifact
└── images/            # files referenced by imageFile
```

Each object uses the field names above, except `image` is replaced by **`imageFile`** —
a filename relative to `seed_data/images/` (or `""` / omitted for no image). The final
`image` URL is generated at upload time, so don't put one in the manifest.

If your data lives in some other shape, `load_manifest()` is the only function that
reads the input format — swap it and nothing else changes.

## Error handling

Each artifact is independent: a bad record prints `FAILED [lot N] -- reason` and the
run continues. The script exits non-zero if anything failed. At the end it prints
total processed / created / skipped / failed.

Mandatory-field problems (missing `name`, non-positive `lotNum`, duplicate lot number
within the manifest, missing image file) are hard failures. A `category`, `material`
or `dynasty` value outside the app's fixed option lists is only a **warning** — it
writes fine, but won't appear pre-selected in the Edit Artifact dropdown.

## About the bundled `seed_data/`

Generated from `TAAM Chart.docx` — 31 artifacts with their images extracted from the
document. Notes on how it was derived:

- **Lot numbers 1–31**, in chart order. Neither the handout nor the requirements
  specify a numbering pattern beyond "mandatory, unique identifier".
- **Names are bilingual**, `English — 中文`, since the chart gives both and the
  requirements put no constraint on the name field.
- **Dynasty values were normalized** to the fixed dropdown options (the chart uses
  freer text like `Southern Song Dynasty 南宋` or `Qing Qianlong Period (1736-1795)`).
  The original catalogue wording is preserved verbatim in `notes`, so nothing is lost.
  Two rows named a span crossing two options — `Warring States to Early Han` →
  Eastern Zhou, and `Late Ming/Early Qing` → Ming; both took the **earlier** option and
  both say so in `notes`. Change lots 28 and 5 if you'd rather they sat in the later period.
- **Materials normalized** the same way: `Ceramics` → `Ceramic`,
  `Mixed Media (Textiles/Fabric)` → `Mixed Media`, original text kept in `notes`.
- **Categories were already exact matches** — no changes needed.
- `origin`, `dimensions`, `conditionReport`, `location`, `acqMethod`, `provenance` and
  `accessionNum` are at their defaults (`""`, `[0,0,0]`, `0`). The chart doesn't
  contain them and nothing was invented; admins can fill them in through the app.

`--dry-run` currently reports 31 valid records with zero option-list warnings.
