---
name: apple-signin-secret-rotation
description: Apple sign-in client secret (Supabase) expires 2026-12-08 and must be regenerated from the .p8 key
metadata: 
  node_type: memory
  type: project
  originSessionId: 79041d2d-6632-468f-9fc5-56a446b7e4b5
---

The Supabase Apple provider secret is an ES256 JWT that **expires 2026-12-08** (Apple caps validity at 6 months; last rotated 2026-06-10 by the automated workflow, validated end-to-end with HTTP 200).

**Why:** Apple sign-in (web OAuth flow on Android/desktop) breaks silently when the secret expires.

**How to apply:** Rotation is automated by `.github/workflows/rotate-apple-secret.yml` (runs 1st of Jan/Jun/Nov + manual dispatch; waits for Production environment approval). The required secrets are stored as GitHub Actions environment secrets. Manual fallback: run `supabase/generate_apple_client_secret.py` against the `.p8` key, then push the result with `supabase config push`. See `docs/setup_apple_signin.md` for the full step-by-step and the relevant Apple identifiers.
