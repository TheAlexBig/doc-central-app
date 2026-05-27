#!/usr/bin/env bash
set -euo pipefail

version="${1:-1.0.0}"
frontend_ref="${2:-main}"
backend_ref="${3:-master}"
repository="${DOC_CENTRAL_APP_REPOSITORY:-TheAlexBig/doc-central-app}"
output_directory="${DOC_CENTRAL_WINDOWS_OUTPUT:-target/windows-installer}"
workflow="windows-installer.yml"
request_id="linux-$(date -u +%Y%m%dT%H%M%SZ)-$$"

if ! command -v gh >/dev/null 2>&1; then
  printf 'GitHub CLI is required. Install gh and authenticate before continuing.\n' >&2
  exit 1
fi

if ! gh auth status --hostname github.com >/dev/null 2>&1; then
  printf 'Authenticate GitHub CLI first: gh auth login --hostname github.com\n' >&2
  exit 1
fi

printf 'Dispatching Windows installer build for backend %s and frontend %s...\n' \
  "$backend_ref" "$frontend_ref"
gh workflow run "$workflow" \
  --repo "$repository" \
  --ref "$backend_ref" \
  --field "version=$version" \
  --field "frontend_ref=$frontend_ref" \
  --field "request_id=$request_id"

run_id=""
for _attempt in {1..20}; do
  run_id="$(gh run list \
    --repo "$repository" \
    --workflow "$workflow" \
    --branch "$backend_ref" \
    --event workflow_dispatch \
    --limit 20 \
    --json databaseId,displayTitle \
    --jq ".[] | select(.displayTitle | contains(\"$request_id\")) | .databaseId" |
    head -n 1)"
  if [[ -n "$run_id" ]]; then
    break
  fi
  sleep 2
done

if [[ -z "$run_id" || "$run_id" == "null" ]]; then
  printf 'The workflow was dispatched, but its run could not be located.\n' >&2
  exit 1
fi

printf 'Waiting for workflow run %s...\n' "$run_id"
gh run watch "$run_id" --repo "$repository" --exit-status

mkdir -p "$output_directory"
gh run download "$run_id" \
  --repo "$repository" \
  --name "central-docs-windows-msi-$version" \
  --dir "$output_directory"

printf 'Downloaded the Windows MSI artifact into %s\n' "$output_directory"
