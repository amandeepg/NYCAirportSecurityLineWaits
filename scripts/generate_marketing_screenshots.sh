#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${script_dir}/.." && pwd)"

cd "$repo_root"

usage() {
  cat <<'EOF'
Usage: generate_marketing_screenshots [--webp]

Runs screenshot tests, clears previous marketing outputs, and generates marketing
assets with the marketing_tools pipeline.

Options:
  --webp      Convert generated PNGs in images/frames/output to WebP
  -h, --help  Show this help text
EOF
}

enable_webp=false
while [[ $# -gt 0 ]]; do
  case "$1" in
    --webp)
      enable_webp=true
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
  shift
done

time (
  rm -rf app/src/screenshotTestDebug/reference/ca/amandeep/nycairportsecuritylinewaits/ui/main
  ./gradlew cleanUpdateDebugScreenshotTest updateDebugScreenshotTest
  rm -rf images/frames/output/*.png || true
  rm -rf images/frames/output/*.webp || true
  uv run --project "${HOME}/Downloads/marketing_tools" app-store-screenshots --config images/frames/marketing_jobs.yaml
  if [[ "$enable_webp" == true ]]; then
    "${HOME}/Downloads/marketing_tools/scripts/convert_png_to_webp.sh" images/frames/output/
  fi
)
