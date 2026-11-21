#!/usr/bin/env bash
# Placeholder API keys and Firebase config so CI can build androidApp without secrets.
set -euo pipefail

cat <<'EOF' > apikeys.properties
WYKOP_KEY=ci-placeholder-key
WYKOP_SECRET=ci-placeholder-secret
EOF

mkdir -p androidApp/src/debug androidApp/src/release

cat <<'EOF' > androidApp/src/debug/google-services.json
{
  "project_info": {
    "project_number": "1234567890",
    "project_id": "podkop-ci-debug",
    "storage_bucket": "podkop-ci-debug.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:1234567890:android:debugplaceholder",
        "android_client_info": {
          "package_name": "pl.masslany.podkop.debug"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "ci-debug-placeholder-key"
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
EOF

cat <<'EOF' > androidApp/src/release/google-services.json
{
  "project_info": {
    "project_number": "1234567890",
    "project_id": "podkop-ci-release",
    "storage_bucket": "podkop-ci-release.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:1234567890:android:releaseplaceholder",
        "android_client_info": {
          "package_name": "pl.masslany.podkop"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "ci-release-placeholder-key"
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
EOF
