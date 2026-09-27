#!/usr/bin/env python3
"""Validate the built app's distribution metadata without printing credentials."""
import argparse
import plistlib
from pathlib import Path


def verify(app: Path, configuration: str) -> None:
    def require(condition: bool, message: str) -> None:
        if not condition:
            raise SystemExit(message)

    with (app / "Info.plist").open("rb") as source:
        info = plistlib.load(source)
    debug = configuration == "Debug"
    require(info.get("CFBundleIdentifier") == "pl.masslany.podkop.Podkop" + (".debug" if debug else ""),
            "Unexpected bundle identifier")
    require(info.get("CFBundleDisplayName") == ("Podkop Debug" if debug else "Podkop"),
            "Unexpected app display name")
    primary_icon = info.get("CFBundleIcons", {}).get("CFBundlePrimaryIcon", {})
    require(primary_icon.get("CFBundleIconName") == ("AppIconDebug" if debug else "AppIcon"),
            "Unexpected primary app icon")
    ipad_orientations = {
        "UIInterfaceOrientationPortrait", "UIInterfaceOrientationPortraitUpsideDown",
        "UIInterfaceOrientationLandscapeLeft", "UIInterfaceOrientationLandscapeRight",
    }
    require(set(info.get("UISupportedInterfaceOrientations~ipad", [])) == ipad_orientations,
            "iPad multitasking requires all four supported orientations")
    require(bool(info.get("UISupportedInterfaceOrientations")), "Missing iPhone orientations")
    require(bool(info.get("NSPhotoLibraryAddUsageDescription")), "Missing Photos save usage description")
    require(info.get("UILaunchStoryboardName") == "LaunchScreen", "Missing launch storyboard declaration")
    require((app / "Base.lproj" / "LaunchScreen.storyboardc").exists()
            or (app / "LaunchScreen.storyboardc").exists(), "Missing compiled launch storyboard")
    for key in ("WYKOP_KEY", "WYKOP_SECRET"):
        value = info.get(key)
        require(isinstance(value, str) and bool(value) and "$(" not in value,
                "Missing or unresolved API configuration")
    manifest_path = app / "PrivacyInfo.xcprivacy"
    require(manifest_path.is_file(), "Privacy manifest is not bundled")
    with manifest_path.open("rb") as source:
        manifest = plistlib.load(source)
    require(manifest.get("NSPrivacyTracking") is False, "Unexpected tracking declaration")
    require(not manifest.get("NSPrivacyTrackingDomains"), "Unexpected tracking domains")
    reasons = {item["NSPrivacyAccessedAPIType"]: item["NSPrivacyAccessedAPITypeReasons"]
               for item in manifest.get("NSPrivacyAccessedAPITypes", [])}
    require("CA92.1" in reasons.get("NSPrivacyAccessedAPICategoryUserDefaults", []),
            "Missing app-only UserDefaults privacy reason")
    require("C617.1" in reasons.get("NSPrivacyAccessedAPICategoryFileTimestamp", []),
            "Missing app-container file metadata privacy reason")
    require(bool(manifest.get("NSPrivacyCollectedDataTypes")), "Missing user-content privacy disclosures")
    print(f"Verified {configuration} app identity, icons, orientations, launch screen and privacy metadata")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("app", type=Path)
    parser.add_argument("--configuration", required=True, choices=("Debug", "Release"))
    arguments = parser.parse_args()
    verify(arguments.app, arguments.configuration)
