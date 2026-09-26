#!/bin/bash

# Writes a google-services.json with placeholder values, so the Android app builds where the real
# one is not available: the build-and-test workflow runs on every pull request, and the real file
# lives only in the Production environment, which needs a manual approval. The app it produces
# compiles and runs through R8 like the real one, but cannot reach Firebase.

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cat > "$PROJECT_ROOT/androidApp/google-services.json" <<'JSON'
{
  "project_info": {
    "project_number": "000000000000",
    "project_id": "bible-planner-placeholder",
    "storage_bucket": "bible-planner-placeholder.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:000000000000:android:0000000000000000",
        "android_client_info": {
          "package_name": "com.quare.bibleplanner"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "placeholder-api-key"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
JSON
