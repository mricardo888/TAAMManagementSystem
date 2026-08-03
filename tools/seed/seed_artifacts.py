#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
seed_artifacts.py -- bulk-seed artifact records (with images) into the backend used
by the TAAM Artifact Management System Android app (CSCB07, com.golden.geese).

This is a ONE-OFF ADMIN / DATA-LOADING TOOL. It does not run inside the Android app
and is not shipped with it. It talks to the same two services the app talks to:

  * Firebase Realtime Database -- one JSON object per artifact at /artifacts/{lotNum}
  * Supabase Storage           -- image upload over plain HTTPS, same path convention
                                  as the app's SupabaseImageUploader.java

Usage
-----
    python seed_artifacts.py \
        --data-dir seed_data \
        --service-account key.json \
        --database-url https://<project-id>-default-rtdb.<region>.firebasedatabase.app \
        --supabase-url https://<project-ref>.supabase.co \
        --supabase-key <anon key> \
        --supabase-bucket artifacts

Add --overwrite to replace artifacts whose lot number already exists (default: skip).
Add --dry-run to validate the manifest and report what would happen without writing
anything to Supabase or Firebase.

Where the config values come from
---------------------------------
    --supabase-url / --supabase-key / --supabase-bucket
        Copy from the Android project's app/src/main/res/values/strings.xml
        (supabase_url, supabase_anon_key, supabase_image_bucket). They are inputs on
        purpose -- do not paste them into this file as literals.
        Env fallbacks: SUPABASE_URL, SUPABASE_ANON_KEY, SUPABASE_BUCKET

    --service-account
        Firebase Console -> Project Settings -> Service Accounts -> Generate new
        private key. Env fallback: GOOGLE_APPLICATION_CREDENTIALS

        !!  NEVER COMMIT THE SERVICE ACCOUNT KEY TO GIT.  !!
        It grants full admin access to the database and bypasses all security rules.
        Add the key file (and seed_data/, if it holds anything private) to .gitignore.

    --database-url
        Shown at the top of the Firebase Console's Realtime Database page. It is NOT
        in google-services.json for this project, so it has to be passed explicitly.

Dependencies
------------
    pip install firebase-admin requests
"""

import argparse
import json
import mimetypes
import os
import sys
import time

import requests

import firebase_admin
from firebase_admin import credentials, db


# ---------------------------------------------------------------------------
# Fixed option lists -- must match the Android app's dropdowns exactly. A value
# outside these lists still writes fine, but the app won't pre-select it in the
# Edit Artifact spinner, so we warn.
# ---------------------------------------------------------------------------

CATEGORY_OPTIONS = [
    "Painting and Calligraphy", "Ceramics", "Bronze Ware", "Lacquerware",
    "Jade Ware", "Enamel Ware", "Glassware", "Furniture",
    "Embroidery and Textiles", "Documents and Archives", "Gold and Silverware",
    "Clocks and Watches", "Religious Artifacts", "Daily-use Items", "Weaponry",
    "Miscellaneous Artifacts",
]

MATERIAL_OPTIONS = [
    "Bronze", "Stone", "Wood", "Jade", "Ceramic", "Lacquerware", "Ivory",
    "Gold", "Silver", "Iron", "Mixed Media",
]

DYNASTY_OPTIONS = [
    "Shang Dynasty (c. 1600-1046 BCE)", "Western Zhou Dynasty (c. 1046-771 BCE)",
    "Eastern Zhou Dynasty (770-256 BCE)", "Qin Dynasty (221-206 BCE)",
    "Han Dynasty (206 BCE-220 CE)", "Three Kingdoms Period (220-280 CE)",
    "Jin Dynasty (266-420 CE)", "Southern and Northern Dynasties (420-589 CE)",
    "Sui Dynasty (581-618 CE)", "Tang Dynasty (618-907 CE)",
    "Five Dynasties and Ten Kingdoms (907-960 CE)", "Song Dynasty (960-1279 CE)",
    "Liao Dynasty (907-1125 CE)", "Jin Dynasty (1115-1234 CE)",
    "Yuan Dynasty (1271-1368 CE)", "Ming Dynasty (1368-1644 CE)",
    "Qing Dynasty (1644-1912 CE)", "Republic of China Period (1912-1949 CE)",
]

ALLOWED_IMAGE_EXTS = {"jpg", "jpeg", "png", "gif", "webp"}
MAX_IMAGE_BYTES = 10 * 1024 * 1024  # 10MB, same limit the app enforces


class ArtifactError(Exception):
    """One bad record. Caught per-artifact so a single failure never aborts the run."""


# ---------------------------------------------------------------------------
# Manifest parsing -- the ONLY place that knows the on-disk input format.
# Swap this one function to read a CSV, a spreadsheet export, or anything else;
# the validation / upload / write logic below never touches the raw file.
# ---------------------------------------------------------------------------

def load_manifest(data_dir):
    """Read seed_data/artifacts.json and return a list of plain dicts.

    Each dict uses the app's field names plus `imageFile`, a filename relative to
    <data_dir>/images/ (or "" / absent when the artifact has no image).
    """
    path = os.path.join(data_dir, "artifacts.json")
    if not os.path.isfile(path):
        sys.exit("ERROR: manifest not found: %s" % path)

    with open(path, "r", encoding="utf-8") as fh:
        try:
            records = json.load(fh)
        except json.JSONDecodeError as exc:
            sys.exit("ERROR: %s is not valid JSON: %s" % (path, exc))

    if not isinstance(records, list):
        sys.exit("ERROR: %s must contain a JSON array of artifact objects." % path)
    return records


# ---------------------------------------------------------------------------
# Validation
# ---------------------------------------------------------------------------

def _clean_str(value):
    return value.strip() if isinstance(value, str) else ""


def _clean_dimensions(value):
    """Coerce to exactly three numbers; anything unusable becomes [0, 0, 0]."""
    if not isinstance(value, (list, tuple)) or len(value) != 3:
        return [0, 0, 0]
    out = []
    for item in value:
        if isinstance(item, bool) or not isinstance(item, (int, float)):
            return [0, 0, 0]
        out.append(item)
    return out


def build_record(raw):
    """Validate a manifest entry and return (lot_num, record_dict, warnings).

    Raises ArtifactError on anything that makes the record unusable.
    The returned dict holds exactly the keys the Artifact POJO expects -- no
    likes/comments/likedBy/savedBy, which the live app manages on its own.
    """
    warnings = []

    # lotNum: positive integer, and the uniqueness key for /artifacts
    lot_raw = raw.get("lotNum")
    if isinstance(lot_raw, bool) or lot_raw is None:
        raise ArtifactError("lotNum is missing")
    try:
        lot_num = int(lot_raw)
    except (TypeError, ValueError):
        raise ArtifactError("lotNum %r is not an integer" % (lot_raw,))
    if isinstance(lot_raw, float) and lot_raw != lot_num:
        raise ArtifactError("lotNum %r is not a whole number" % (lot_raw,))
    if lot_num <= 0:
        raise ArtifactError("lotNum must be a positive integer (got %d)" % lot_num)

    # remaining mandatory fields
    name = _clean_str(raw.get("name"))
    description = _clean_str(raw.get("description"))
    category = _clean_str(raw.get("category"))
    material = _clean_str(raw.get("material"))
    dynasty = _clean_str(raw.get("dynasty"))

    missing = [
        field for field, value in (
            ("name", name), ("description", description), ("category", category),
            ("material", material), ("dynasty", dynasty),
        ) if not value
    ]
    if missing:
        raise ArtifactError("missing mandatory field(s): %s" % ", ".join(missing))

    # soft checks -- a mismatch writes fine but won't pre-select in the app's dropdown
    for field, value, options in (
        ("category", category, CATEGORY_OPTIONS),
        ("material", material, MATERIAL_OPTIONS),
        ("dynasty", dynasty, DYNASTY_OPTIONS),
    ):
        if value not in options:
            warnings.append(
                "%s %r is not one of the app's fixed options; it will not appear "
                "pre-selected in the Edit Artifact dropdown" % (field, value)
            )

    accession = raw.get("accessionNum", 0)
    if isinstance(accession, bool) or not isinstance(accession, (int, float)):
        accession = 0

    record = {
        "lotNum": lot_num,
        "name": name,
        "description": description,
        "category": category,
        "material": material,
        "dynasty": dynasty,
        "origin": _clean_str(raw.get("origin")),
        "dimensions": _clean_dimensions(raw.get("dimensions")),
        "conditionReport": _clean_str(raw.get("conditionReport")),
        "location": _clean_str(raw.get("location")),
        "acqMethod": _clean_str(raw.get("acqMethod")),
        "provenance": _clean_str(raw.get("provenance")),
        "accessionNum": accession,
        "notes": _clean_str(raw.get("notes")),
        "image": "",  # filled in after a successful Supabase upload
    }
    return lot_num, record, warnings


# ---------------------------------------------------------------------------
# Supabase Storage upload -- mirrors SupabaseImageUploader.java so the resulting
# public URLs are the same shape the app already produces.
# ---------------------------------------------------------------------------

def upload_image(local_path, lot_num, supabase_url, supabase_key, bucket):
    """Upload one image and return its public URL. Raises ArtifactError on failure."""
    ext = os.path.splitext(local_path)[1].lstrip(".").lower()
    if ext not in ALLOWED_IMAGE_EXTS:
        raise ArtifactError(
            "image %s has unsupported extension %r (allowed: %s)"
            % (os.path.basename(local_path), ext, ", ".join(sorted(ALLOWED_IMAGE_EXTS)))
        )

    size = os.path.getsize(local_path)
    if size > MAX_IMAGE_BYTES:
        raise ArtifactError(
            "image %s is %.1fMB, over the 10MB limit"
            % (os.path.basename(local_path), size / 1024 / 1024)
        )
    if size == 0:
        raise ArtifactError("image %s is empty" % os.path.basename(local_path))

    with open(local_path, "rb") as fh:
        data = fh.read()

    content_type = mimetypes.guess_type(local_path)[0] or "application/octet-stream"

    # timestamped path keeps re-uploads unique -- same convention as the app
    file_path = "artifacts/%d/%d.%s" % (lot_num, int(time.time() * 1000), ext)
    endpoint = "%s/storage/v1/object/%s/%s" % (supabase_url.rstrip("/"), bucket, file_path)

    try:
        response = requests.post(
            endpoint,
            headers={
                "apikey": supabase_key,
                "Authorization": "Bearer %s" % supabase_key,
                "Content-Type": content_type,
            },
            data=data,
            timeout=60,
        )
    except requests.RequestException as exc:
        raise ArtifactError("image upload failed: %s" % exc)

    if response.status_code not in (200, 201):
        raise ArtifactError(
            "image upload failed (HTTP %d): %s"
            % (response.status_code, response.text[:200].strip())
        )

    return "%s/storage/v1/object/public/%s/%s" % (supabase_url.rstrip("/"), bucket, file_path)


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

def parse_args():
    parser = argparse.ArgumentParser(
        description="Bulk-seed TAAM artifact records into Firebase + Supabase.",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    parser.add_argument("--data-dir", default="seed_data",
                        help="directory holding artifacts.json and images/")
    parser.add_argument("--service-account",
                        default=os.environ.get("GOOGLE_APPLICATION_CREDENTIALS"),
                        help="Firebase service account key JSON "
                             "(env: GOOGLE_APPLICATION_CREDENTIALS)")
    parser.add_argument("--database-url",
                        default=os.environ.get("FIREBASE_DATABASE_URL"),
                        help="Realtime Database URL (env: FIREBASE_DATABASE_URL)")
    parser.add_argument("--supabase-url", default=os.environ.get("SUPABASE_URL"),
                        help="Supabase project URL (env: SUPABASE_URL)")
    parser.add_argument("--supabase-key", default=os.environ.get("SUPABASE_ANON_KEY"),
                        help="Supabase anon key (env: SUPABASE_ANON_KEY)")
    parser.add_argument("--supabase-bucket", default=os.environ.get("SUPABASE_BUCKET"),
                        help="Supabase storage bucket (env: SUPABASE_BUCKET)")
    parser.add_argument("--overwrite", action="store_true",
                        help="replace records whose lot number already exists "
                             "(default: skip them)")
    parser.add_argument("--dry-run", action="store_true",
                        help="validate the manifest and report what would happen, "
                             "without uploading or writing anything")
    args = parser.parse_args()

    if not args.dry_run:
        missing = [flag for flag, value in (
            ("--service-account", args.service_account),
            ("--database-url", args.database_url),
            ("--supabase-url", args.supabase_url),
            ("--supabase-key", args.supabase_key),
            ("--supabase-bucket", args.supabase_bucket),
        ) if not value]
        if missing:
            parser.error("missing required argument(s): %s" % ", ".join(missing))
        if not os.path.isfile(args.service_account):
            parser.error("service account key not found: %s" % args.service_account)

    return args


def main():
    args = parse_args()

    records = load_manifest(args.data_dir)
    images_dir = os.path.join(args.data_dir, "images")

    if args.dry_run:
        print("DRY RUN -- nothing will be uploaded or written.\n")
    else:
        firebase_admin.initialize_app(
            credentials.Certificate(args.service_account),
            {"databaseURL": args.database_url},
        )

    print("Processing %d artifact(s) from %s\n"
          % (len(records), os.path.join(args.data_dir, "artifacts.json")))

    created = skipped = failed = 0
    seen_lots = set()

    for index, raw in enumerate(records, start=1):
        label = "entry #%d" % index

        try:
            if not isinstance(raw, dict):
                raise ArtifactError("manifest entry is not a JSON object")

            lot_num, record, warnings = build_record(raw)
            label = "lot %d" % lot_num

            for warning in warnings:
                print("  WARNING [%s]: %s" % (label, warning))

            # duplicate inside the manifest itself -- the later one would silently
            # clobber the earlier one, so refuse it
            if lot_num in seen_lots:
                raise ArtifactError("lot number %d appears more than once in the "
                                    "manifest" % lot_num)
            seen_lots.add(lot_num)

            ref = None if args.dry_run else db.reference("/artifacts/%d" % lot_num)
            exists = bool(ref.get(shallow=True)) if ref is not None else False

            if exists and not args.overwrite:
                print("SKIPPED [%s] -- already exists at /artifacts/%d "
                      "(use --overwrite to replace)" % (label, lot_num))
                skipped += 1
                continue

            if exists and args.overwrite:
                # .set() replaces the whole node. The app stores likedBy/savedBy as
                # children of /artifacts/{lotNum}, so those lists are destroyed here.
                # (Comments live at /comments/{lotNum} and are unaffected.)
                print("  WARNING [%s]: overwriting -- existing likedBy/savedBy under "
                      "/artifacts/%d will be erased" % (label, lot_num))

            # image (optional)
            image_file = _clean_str(raw.get("imageFile"))
            if image_file:
                local_path = os.path.join(images_dir, image_file)
                if not os.path.isfile(local_path):
                    raise ArtifactError("imageFile %r not found in %s"
                                        % (image_file, images_dir))
                if args.dry_run:
                    ext = os.path.splitext(local_path)[1].lstrip(".").lower()
                    if ext not in ALLOWED_IMAGE_EXTS:
                        raise ArtifactError("image %s has unsupported extension %r"
                                            % (image_file, ext))
                    size = os.path.getsize(local_path)
                    if size > MAX_IMAGE_BYTES:
                        raise ArtifactError("image %s is %.1fMB, over the 10MB limit"
                                            % (image_file, size / 1024 / 1024))
                    record["image"] = "<would upload %s>" % image_file
                else:
                    record["image"] = upload_image(
                        local_path, lot_num, args.supabase_url,
                        args.supabase_key, args.supabase_bucket,
                    )

            if args.dry_run:
                print("OK      [%s] -- %s%s"
                      % (label, record["name"][:60],
                         "" if image_file else "  (no image)"))
            else:
                ref.set(record)
                print("CREATED [%s] -- %s%s"
                      % (label, record["name"][:60],
                         "" if record["image"] else "  (no image)"))
            created += 1

        except ArtifactError as exc:
            print("FAILED  [%s] -- %s" % (label, exc))
            failed += 1
        except Exception as exc:  # noqa: BLE001 -- one bad record must not kill the run
            print("FAILED  [%s] -- unexpected error: %s: %s"
                  % (label, type(exc).__name__, exc))
            failed += 1

    verb = "would create" if args.dry_run else "created"
    print("\n%s\nTotal processed: %d\n%s: %d\nSkipped: %d\nFailed: %d"
          % ("-" * 46, len(records), verb.capitalize(), created, skipped, failed))

    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
