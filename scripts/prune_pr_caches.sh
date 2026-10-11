#!/bin/bash

# Deletes the Gradle cache entries of a pull request that its latest push no longer needs, or,
# with --all, every entry of a closed pull request:
#   ./scripts/prune_pr_caches.sh --dry-run refs/pull/585/merge 2026-10-11T12:57:50Z
#   ./scripts/prune_pr_caches.sh --all refs/pull/585/merge
# The cleanup-pr-caches workflow runs the first form once every writer workflow of a push has
# succeeded, with the time that push's runs started, and the second when a pull request closes.
#
# setup-gradle saves one small "home" entry per job, keyed by the job and the commit, and a bundle
# per directory of the Gradle user home, keyed by the bundle's content. A bundle whose content did
# not change is restored by the next push and not saved again, so the dependency bundles live on
# across pushes. The transforms and build cache bundles change on almost every push, and each push
# saves a new one per writer job, hundreds of MB each, while the old ones serve nothing: the next
# push restores the newest home entry of its job, and that one names the newest bundles. Left
# alone, three pushes of one pull request held 12.5 GB of the repository's 10 GB and pushed out
# the entries main had saved.
#
# Only the entries of setup-gradle and of the Kotlin/Native cache are considered, the ones the
# writer workflows save; a workflow that a path filter skipped on this push, like static-analysis
# with its ktlint entries, keeps what it saved earlier.
#
# The rule, which the workflow and docs/ci.md refer to rather than repeat:
#
# - An entry stays when the push created it, or restored it: its last access is then at or after
#   the push's runs started. GitHub sets that time on every restore, within seconds (every entry
#   a push restored showed the time of that push's first minutes when this was written), and the
#   pruning runs minutes after the last restore.
# - Anything older than the push that nothing restored goes.
# - A transforms, build cache or home entry that the push restored and then replaced goes too,
#   and a count tells which: the entries of one kind on one platform are one restored and one
#   saved per job, so when the push saved exactly as many as it restored, every job replaced its
#   own, and the restored ones are the previous push's. When it saved fewer, a job kept its bundle
#   (the iOS transforms rarely change) or failed to save; when it saved more, a job saved without
#   restoring from this scope (its entry was evicted, or a new shard); in both cases nothing says
#   which, so they all stay until a later push stops restoring them. The run that the push
#   cancelled still saves in its post steps, right after the new run started; those entries count
#   as this push's: a bundle this push needed has the same content and key, so this push restored
#   it or named it instead of saving its own, and one it did not need adds to the saved count,
#   which only keeps the restored ones one more push.
# - The count is of entries, and two jobs can restore one entry when their bundles have the same
#   content. Should one of them then save a new one and the other keep it, the count says
#   replaced and the kept one goes; that job rebuilds the bundle once on its next push. Transforms
#   with the same content need the same dependencies, which the jobs here do not share.
#
set -euo pipefail

usage() {
    echo "Usage: $0 [--dry-run] <ref> <run-start>" >&2
    echo "       $0 [--dry-run] --all <ref>" >&2
    echo "  ref        the cache scope, refs/pull/<number>/merge" >&2
    echo "  run-start  when the latest run of the ref started, ISO 8601 in UTC (2026-10-11T12:57:50Z)" >&2
    echo "  --all      every entry of the ref, for a closed pull request" >&2
    exit 1
}

DRY_RUN=false
ALL=false
while [ "$#" -gt 0 ]; do
    case "$1" in
        --dry-run) DRY_RUN=true ;;
        --all) ALL=true ;;
        --*) usage ;;
        *) break ;;
    esac
    shift
done
if [ "$ALL" = true ]; then
    [ "$#" -eq 1 ] || usage
    REF="$1"
    RUN_START=""
else
    [ "$#" -eq 2 ] || usage
    REF="$1"
    RUN_START="$2"
    case "$RUN_START" in [0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]T[0-9][0-9]:[0-9][0-9]:[0-9][0-9]Z) ;; *) usage ;; esac
fi

ENTRIES=$(gh cache list --ref "$REF" --limit 1000 --json id,key,version,sizeInBytes,createdAt,lastAccessedAt)

# Every entry with the seconds since the run started, negative when it predates the run, and its
# kind (the key without the content hash or the commit); then, per kind and platform, whether the
# run replaced what it restored, and from that whether the entry goes. With --all, every entry
# goes, the ones of other workflows included. The bundle keys carry no
# platform; the version field does, by way of the cached paths it hashes, which hold the runner's
# home directory (/home/runner on Linux, /Users/runner on macOS). Should those paths ever match
# across platforms, the two would share a group, and a bundle kept on one platform could cancel
# out an extra save on the other.
CLASSIFIED=$(jq --arg start "$RUN_START" --argjson all "$ALL" '
    def seconds: sub("\\.[0-9]+"; "") | fromdateiso8601;
    if $all then map(. + { delete: true }) else
    ($start | seconds) as $t
    | map(select(.key | test("^(gradle|konan)-")))
    | map(. + {
        created: ((.createdAt | seconds) - $t),
        accessed: ((.lastAccessedAt | seconds) - $t),
        kind: (.key | sub("\\|.*$"; "") | sub("-[0-9a-f]{32,}$"; "") | sub("-v[0-9]+$"; ""))
    })
    | group_by([.kind, .version])
    | map(
        (.[0].kind | IN("gradle-home", "gradle-transforms", "gradle-build-cache", "gradle-configuration-cache")) as $replacedEachPush
        | ([.[] | select(.created >= 0)] | length) as $saved
        | ([.[] | select(.created < 0 and .accessed >= 0)] | length) as $restored
        | map(. + { replaced: ($replacedEachPush and $saved == $restored) }))
    | add // []
    | map(. + { delete: (.created < 0 and (.accessed < 0 or .replaced)) })
    end' <<< "$ENTRIES")

TO_DELETE=$(jq -r '
    map(select(.delete))
    | sort_by(.createdAt)
    | .[] | [.id, (.sizeInBytes / 1000000 | round), .createdAt[0:19], .lastAccessedAt[0:19], .key] | @tsv
' <<< "$CLASSIFIED")

if [ -z "$TO_DELETE" ]; then
    echo "Nothing to delete on $REF"
    exit 0
fi

TOTAL_MB=$(awk -F'\t' '{ sum += $2 } END { print sum }' <<< "$TO_DELETE")
# What stays in the scope, the entries of other workflows included.
KEPT_MB=$(jq --argjson deleted "$TOTAL_MB" '([.[].sizeInBytes] | add // 0 | . / 1000000 | round) - $deleted' <<< "$ENTRIES")
echo "Deleting $(wc -l <<< "$TO_DELETE" | tr -d ' ') entries, $TOTAL_MB MB, of $REF; keeping $KEPT_MB MB"
printf '%s\n' "$TO_DELETE" | awk -F'\t' '{ printf "  %5d MB  created %s  accessed %s  %s\n", $2, $3, $4, $5 }'

if [ "$DRY_RUN" = true ]; then
    echo "Dry run: nothing deleted"
    exit 0
fi

# An entry evicted meanwhile is already gone; any other failure, a token without actions: write
# say, fails the script once every entry has been tried.
STATUS=0
while IFS= read -r ID; do
    if ! OUTPUT=$(gh cache delete "$ID" 2>&1); then
        case "$OUTPUT" in
            *"Could not find a cache"* | *"HTTP 404"*) echo "  $ID was already evicted" ;;
            *) echo "  $ID: $OUTPUT" >&2; STATUS=1 ;;
        esac
    fi
done <<< "$(cut -f1 <<< "$TO_DELETE")"
exit "$STATUS"
