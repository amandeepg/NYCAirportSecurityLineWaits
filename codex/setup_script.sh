#!/bin/sh
set -eu

die() {
  echo "Error: $*" >&2
  exit 1
}

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "Missing required command: $1"
}

require_cmd curl
require_cmd unzip
require_cmd python3
require_cmd yes
SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/CodexAndroidHome}"
case "$SDK_ROOT" in
  "~") SDK_ROOT="$HOME" ;;
  "~/"*) SDK_ROOT="$HOME/${SDK_ROOT#~/}" ;;
esac

case "$(uname -s)" in
  Darwin) host_os="macosx" ;;
  Linux) host_os="linux" ;;
  *) die "Unsupported OS: $(uname -s). Only macOS and Linux are supported." ;;
esac

cmdline_url=$(python3 - "$host_os" <<'PY'
import sys
import urllib.request
import xml.etree.ElementTree as ET

host_os = sys.argv[1]
repo_url = "https://dl.google.com/android/repository/repository2-1.xml"
xml = urllib.request.urlopen(repo_url).read()
root = ET.fromstring(xml)

cmd_url = ""
for rp in root.findall("remotePackage"):
    if rp.attrib.get("path") == "cmdline-tools;latest":
        for arch in rp.find("archives").findall("archive"):
            if arch.findtext("host-os") == host_os:
                cmd_url = arch.find("complete").findtext("url")
                break
        break

if cmd_url and not cmd_url.startswith("http"):
    cmd_url = "https://dl.google.com/android/repository/" + cmd_url

print(cmd_url)
PY
)
test -n "$cmdline_url" || die "Could not resolve command-line tools download URL."

mkdir -p "$SDK_ROOT"

tmp_dir=$(mktemp -d 2>/dev/null || mktemp -d -t codex-android-sdk)
cleanup() { rm -rf "$tmp_dir"; }
trap cleanup EXIT

archive="$tmp_dir/cmdline-tools.zip"
curl -L -o "$archive" "$cmdline_url"
unzip -q "$archive" -d "$tmp_dir"

test -d "$tmp_dir/cmdline-tools" || die "Unexpected command-line tools archive layout."

rm -rf "$SDK_ROOT/cmdline-tools/latest"
mkdir -p "$SDK_ROOT/cmdline-tools"
mv "$tmp_dir/cmdline-tools" "$SDK_ROOT/cmdline-tools/latest"

SDKMANAGER="$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"
test -x "$SDKMANAGER" || die "sdkmanager not found at $SDKMANAGER"

yes | "$SDKMANAGER" --sdk_root="$SDK_ROOT" --licenses >/dev/null

echo "Installed Android SDK to $SDK_ROOT"
