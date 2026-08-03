# Firebase / Supabase setup

Everything here is **per-developer local setup**. `google-services.json` is gitignored,
so cloning the repo is not enough to get a running app — each person has to do this once.

Firebase project: `goldengeese-c4141`

---

## 1. Get `google-services.json`

Firebase Console → Project Settings → General → the `com.golden.geese` Android app →
download `google-services.json` and save it to `app/google-services.json`.

**Then open it and confirm it contains a `firebase_url` key**, like this:

```json
"project_info": {
  "project_number": "1082058484586",
  "project_id": "goldengeese-c4141",
  "firebase_url": "https://goldengeese-c4141-default-rtdb.firebaseio.com",
  "storage_bucket": "goldengeese-c4141.firebasestorage.app"
}
```

### Why this matters

Both repositories resolve the database from the config alone:

```
FirebaseArtifactRepository.java:31      root = FirebaseDatabase.getInstance().getReference();
FirebaseUserProfileRepository.java:18   usersRef = FirebaseDatabase.getInstance().getReference("users");
```

With no `firebase_url`, `FirebaseDatabase.getInstance()` throws
*"Can't determine Firebase Database URL"* before any query runs, and nothing loads.

Older copies of this file are missing the key because the Realtime Database was created
*after* they were downloaded. Re-downloading is the fix; if you would rather patch by
hand, add the `firebase_url` line above to `project_info`.

The database lives in **us-central1**, so the URL is the `.firebaseio.com` form, not a
regional `.firebasedatabase.app` one. (A regional URL returns HTTP 404 with a
`correctUrl` pointing back to the address above.)

---

## 2. Enable Email/Password authentication

Firebase Console → Authentication → Sign-in method → enable **Email/Password**.

At the time of writing, Identity Toolkit returns `CONFIGURATION_NOT_FOUND` for this
project's API key — the key is valid (Firebase Installations accepts it), but no auth
config exists. Until this is enabled:

- login and sign-up fail, and
- **nothing in the app loads at all**, because the database rules require `auth != null`
  to read `/artifacts`.

---

## 3. Database rules

Reading `/artifacts` requires a signed-in user; writing requires an admin. You need a
regular account to browse, and an account flagged `isAdmin == true` to exercise the
add / edit / delete paths.

---

## 4. Supabase image storage

Already configured in `app/src/main/res/values/strings.xml` (`supabase_url`,
`supabase_anon_key`, `supabase_image_bucket`) — no local setup needed.

Bucket `artifacts` is public-read, capped at 10MB, restricted to image MIME types, with
SELECT and INSERT policies. Glide loads the public URLs with no auth header.

Two consequences worth knowing:

- There is **no UPDATE or DELETE policy**, so the "also delete the image from cloud
  storage" nice-to-have in the requirements will fail silently if implemented as-is.
  The requirements treat that as optional, so this is not a blocker.
- The anon key ships inside the APK, and the INSERT policy accepts it. Anyone who
  extracts the key can upload into the bucket. That is inherent to the storage approach
  suggested in the course handout's appendix; the 10MB cap and MIME restriction are the
  only limits on it.

---

## 5. Seeded data

The collection is loaded by [`tools/seed/`](../tools/seed/) — 31 artifacts at
`/artifacts/1` … `/artifacts/31` plus their images. See that README before re-running
it; `--overwrite` erases the `likedBy` / `savedBy` lists on existing records.
