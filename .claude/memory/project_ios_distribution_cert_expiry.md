---
name: project_ios_distribution_cert_expiry
description: "CI iOS distribution cert (match, created by API Key) expires 2027-05-21; renewal is manual since CI match is readonly; other portal certs expiring are unused."
metadata:
  node_type: memory
  type: project
  originSessionId: 603ba527-6d30-440e-83b1-33cfa5b66443
  modified: 2026-10-03T04:24:07.800Z
---

The certificate the iOS release CI signs with is the "Distribution" cert whose Created By is **API Key** in the developer portal — fastlane match created it; it expires **2027-05-21**. The macOS match certs (Mac App Distribution + Mac Installer Distribution, lane `mac_certs`) expire **2027-07-28**.

**Why:** CI runs `match(readonly: true)`, so nothing renews these automatically; an expired cert breaks the `fastlane ios build` job. The rotate-apple-secret workflow only rotates the Sign in with Apple JWT ([[project_apple_signin_secret_rotation]]), not signing certs.

**How to apply:** renew in ~April 2027 by running match locally with `readonly: false` (new cert pushed to Quartel-Enterprise/bible-planner-certs). Apple "certificate expires in 30 days" emails for other Distribution/Development/Managed certs (e.g. 86SL657373, expired 2026-11-01, created manually by the user) are unused by the repo and can be ignored — verified 2026-10-03 against the portal list and the certs repo IDs.
