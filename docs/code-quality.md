# Code quality

The project uses ktlint for formatting and style, extended with the custom rules in
[`tools/ktlint-custom-rules`](../tools/ktlint-custom-rules). What those rules enforce, and the
conventions behind them, is documented in [Code style](architecture/code-style.md).

## Running it

CI runs the ktlint CLI directly, and `scripts/ktlint.sh` runs exactly the same check locally:

```bash
./scripts/ktlint.sh            # check (what CI runs)
```

```bash
./scripts/ktlint.sh --format   # autocorrect what can be autocorrected
```

The script downloads the ktlint CLI once (cached in `~/.cache/ktlint`) and rebuilds the custom
ruleset jar only when its sources change.

`./gradlew ktlintCheck` still works, but it is much slower: it drags KSP and Kotlin/Native
compilation into the task graph to check files that are already on disk.

## In CI

The `static-analysis` workflow runs the same script on every pull request — and on every push to
`main` — that touches a `.kt` or `.kts` file, the version catalog, the ktlint script or
`.editorconfig`.
