#!/usr/bin/env python3
"""Turn a captured website API response into a sample for the Android integration tests.

The sample keeps the response's exact shape: every key, null, empty string, boolean, number,
timestamp and enum value, and the pagination block's keys and value types. Values that identify
people or content are replaced with deterministic stand-ins, so samples can be committed and
sanitizing the same capture again gives the same sample:

- prose (anything with spaces) becomes "Sample <key>"
- URLs point at example.com
- ids are renumbered from 1000, keeping ids that matched still matching
- other strings (usernames, slugs, tags, media keys, cursors) are scrambled character by
  character, keeping their length, case, digits and punctuation

Usage:
    python3 -I scripts/api-samples/sanitize.py <captured.json> <sample.json> [--items N]
"""

import argparse
import hashlib
import json
import re
import string

# Strings under these keys are enums the app branches on, not content.
ENUM_KEYS = {"resource", "type", "status", "color", "gender", "mime_type", "age_category"}
TIMESTAMP = re.compile(r"^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$")


class Sanitizer:
    def __init__(self):
        self.ids = {}

    def value(self, key, value):
        if isinstance(value, dict):
            return {k: self.value(k, v) for k, v in value.items()}
        if isinstance(value, list):
            return [self.value(key, item) for item in value]
        if isinstance(value, bool) or value is None:
            return value
        if isinstance(value, int) and (key == "id" or key.endswith("_id")):
            return self.ids.setdefault(value, 1000 + len(self.ids))
        if isinstance(value, str):
            return self.string(key, value)
        return value

    def string(self, key, value):
        if value == "" or value.isdigit() or key in ENUM_KEYS or TIMESTAMP.match(value):
            return value
        if value.startswith(("http://", "https://")):
            extension = re.search(r"\.(jpe?g|png|gif|webp|mp4)$", value)
            return f"https://example.com/{key}{extension.group(0) if extension else ''}"
        if any(character.isspace() for character in value):
            return f"Sample {key}"
        return scramble(value)


def scramble(value):
    digest = hashlib.sha256(value.encode()).digest()
    scrambled = []
    for index, character in enumerate(value):
        byte = digest[index % len(digest)] + index
        if character in string.ascii_lowercase:
            scrambled.append(string.ascii_lowercase[byte % 26])
        elif character in string.ascii_uppercase:
            scrambled.append(string.ascii_uppercase[byte % 26])
        elif character in string.digits:
            scrambled.append(string.digits[byte % 10])
        elif character.isalpha():
            scrambled.append(string.ascii_lowercase[byte % 26])
        else:
            scrambled.append(character)
    return "".join(scrambled)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("captured")
    parser.add_argument("sample")
    parser.add_argument("--items", type=int, default=1, help="how many items of `data` to keep (default 1)")
    arguments = parser.parse_args()

    with open(arguments.captured, encoding="utf-8") as file:
        response = json.load(file)
    if isinstance(response.get("data"), list):
        response["data"] = response["data"][: arguments.items]

    with open(arguments.sample, "w", encoding="utf-8") as file:
        json.dump(Sanitizer().value("", response), file, ensure_ascii=False, indent=2)
        file.write("\n")


if __name__ == "__main__":
    main()
