#!/bin/bash

# Deletes the Gradle cache entries of a pull request that its latest push no longer needs, for
# example:
#   ./scripts/prune_pr_caches.sh --dry-run refs/pull/585/merge 2026-10-11T12:57:50Z
# The cleanup-pr-caches workflow runs it once every writer workflow of a push has succeeded, with
# the time that push's runs started.
#
# setup-gradle saves one small "home" entry per job, keyed by the job and the commit, and a bundle
# per directory of the Gradle user home, keyed by the bundle's content. A bundle whose content did
# not change is restored by the next run and not saved again, so the dependency bundles live on
# across pushes. The transforms and build cache bundles change on almost every push, and each push
# saves a new one per writer job, hundreds of MB each, while the old ones serve nothing: the next
# run restores the newest home entry of its job, and that one names the newest bundles. Left
# alone, three pushes of one pull request held 12.5 GB of the repository's 10 GB and pushed out
# the entries main had saved.
#
# Only the entries of setup-gradle and of the Kotlin/Native cache are considered, the ones the
# writer workflows save; a workflow that a path filter skipped on this push, like static-analysis
# with its ktlint entries, keeps what it saved earlier.
#
# An entry stays when the latest run created it, or restored it: its last access is then at or
# after the run started. Anything older than the run that nothing restored goes. A transforms,
# build cache or home entry that the run restored and then replaced goes too, and the count tells
# which: the entries of one kind on one platform (the version field, a hash of the paths and the
# platform, tells Linux from macOS) are one restored and one saved per job, so when the run saved
# exactly as many as it restored, every job replaced its own, and the restored ones are the
# previous push's. When it saved fewer, a job kept its bundle (the iOS transforms rarely change);
# when it saved more, a job saved without restoring from this scope (its entry was evicted, or a
# cancelled run of the previous push saved late); in both cases nothing says which, so they all
# stay until a later run stops restoring them. The pruning is skipped when a job restored its home
# entry in the latest run but saved none, because that job still needs its previous bundles. A
# job that was removed or renamed (a matrix change renames the android shards) restores nothing,
# so it does not hold the pruning up and its entries go with the rest once nothing restores them.

set -euo pipefail

usage() {
    echo "Usage: $0 [--dry-run] <ref> <run-start>" >&2
    echo "  ref        the cache scope, refs/pull/<number>/merge" >&2
    echo "  run-start  when the latest run of the ref started, ISO 8601 in UTC (2026-10-11T12:57:50Z)" >&2
    exit 1
}

DRY_RUN=false
if [ "${1:-}" = "--dry-run" ]; then
    DRY_RUN=true
    shift
fi
[ "$#" -eq 2 ] || usage
REF="$1"
RUN_START="$2"
case "$RUN_START" in [0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]T[0-9][0-9]:[0-9][0-9]:[0-9][0-9]Z) ;; *) usage ;; esac

ENTRIES=$(gh cache list --ref "$REF" --limit 1000 --json id,key,version,sizeInBytes,createdAt,lastAccessedAt \
    | jq 'map(select(.key | test("^(gradle|konan)-")))')

# Every entry with the seconds since the run started, negative when it predates the run; its kind
# (the key without the content hash or the commit) and platform; and, for a home entry, its job.
CLASSIFIED=$(jq --arg start "$RUN_START" '
    def seconds: sub("\\.[0-9]+"; "") | fromdateiso8601;
    ($start | seconds) as $t
    | map(. + {
        created: ((.createdAt | seconds) - $t),
        accessed: ((.lastAccessedAt | seconds) - $t),
        kind: (.key | sub("\\|.*$"; "") | sub("-[0-9a-f]{32,}$"; "") | sub("-v[0-9]+$"; "")),
        job: (if .key | startswith("gradle-home-") then .key | sub("-[0-9a-f]{40}$"; "") else null end)
    })
    | map(. + { replacedEachPush: (.kind | IN("gradle-home", "gradle-transforms", "gradle-build-cache")) })
    | group_by([.kind, .version])
    | map(
        ([.[] | select(.created >= 0)] | length) as $saved
        | ([.[] | select(.created < 0 and .accessed >= 0)] | length) as $restored
        | map(. + { replaced: (.replacedEachPush and $saved == $restored) }))
    | add // []' <<< "$ENTRIES")

NOT_SAVED=$(jq -r '
    (map(select(.job != null)) | group_by(.job))
    | map(select(all(.created < 0) and any(.accessed >= 0))) | .[][0].job
' <<< "$CLASSIFIED")
if [ -n "$NOT_SAVED" ]; then
    echo "Keeping every entry of $REF: these jobs restored their cache in the run that started at $RUN_START but saved none" >&2
    echo "$NOT_SAVED" >&2
    exit 0
fi

TO_DELETE=$(jq -r '
    map(select(.created < 0 and (.accessed < 0 or .replaced)))
    | sort_by(.createdAt)
    | .[] | [.id, (.sizeInBytes / 1000000 | round), .createdAt[0:19], .lastAccessedAt[0:19], .key] | @tsv
' <<< "$CLASSIFIED")

if [ -z "$TO_DELETE" ]; then
    echo "Nothing to prune on $REF"
    exit 0
fi

TOTAL_MB=$(awk -F'\t' '{ sum += $2 } END { print sum }' <<< "$TO_DELETE")
KEPT_MB=$(jq '[.[] | select(.created >= 0 or (.accessed >= 0 and (.replaced | not))) | .sizeInBytes] | add // 0 | . / 1000000 | round' <<< "$CLASSIFIED")
echo "Pruning $(wc -l <<< "$TO_DELETE" | tr -d ' ') entries, $TOTAL_MB MB, of $REF; keeping $KEPT_MB MB"
printf '%s\n' "$TO_DELETE" | awk -F'\t' '{ printf "  %5d MB  created %s  accessed %s  %s\n", $2, $3, $4, $5 }'

if [ "$DRY_RUN" = true ]; then
    echo "Dry run: nothing deleted"
    exit 0
fi

# A delete that fails (the entry was evicted meanwhile) is no reason to keep the others.
cut -f1 <<< "$TO_DELETE" | xargs -r -n 1 gh cache delete || true
