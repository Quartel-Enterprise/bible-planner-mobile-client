---
name: project-app-store-screenshot-set-race
description: "Screenshot Set Already Exists!" in ios-screenshots-upload is an Apple deletion race; #510 retries it in the lane, #488 skips submission if locales end up empty.
metadata:
  type: project
  modified: 2026-10-03T03:03:52.434Z
---

deliver (overwrite_screenshots) deletes all App Store screenshots, then App Store Connect can refuse to recreate a set with "Screenshot Set Already Exists!" because the delete hasn't settled. Hit 2.9.1 and 2.10.0 (run 37086810803, 2026-10-03): es-ES/es-MX/en-US left empty, ios-submit ran with SUBMIT_FOR_REVIEW=false (#488 safeguard), whole run still green.

**Why:** the run looks successful while the iOS build is silently left unsubmitted.

**How to apply:** if it recurs despite #510's retry (3 attempts, 30s apart), the fix is `gh run rerun <run> --job <ios-screenshots-upload job id>`. It re-runs ios-submit, which then submits, and finalize, which skips because the release is already published. Do this within the 1-day ios-ipa artifact window. Related: [[project-release-workflow-split]].
