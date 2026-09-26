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

## Module graph

The dependencies between Gradle modules are checked by
[modules-graph-assert](https://github.com/jraska/modules-graph-assert), configured in the
`moduleGraphAssert` block of the root `build.gradle.kts`. It only looks at production source sets
(test-only dependencies are ignored) and enforces the layering: `:core:navigation` and
`:core:provider:koin` are the composition root and the only core modules allowed to depend on
`:feature:*` or `:ui:*`. No feature, UI or other core module may depend on them, and `:ui:*` never
depends on `:feature:*`.

```bash
./gradlew assertModuleGraph                  # run every check (what CI runs)
```

The height of the graph (its longest dependency chain) is not enforced, since part of it is the
intentional composition root. Keep an eye on it instead, as a deeper graph compiles less in
parallel:

```bash
./gradlew generateModulesGraphStatistics     # module count, edge count, height, longest path
```

```bash
./gradlew generateModulesGraphvizText -Pmodules.graph.of.module=:feature:day
```

A new dependency that breaks a rule means the code is in the wrong module, not that the rule needs
an exception: move the shared piece down to a `:core:*` module instead.

The `module-graph` workflow runs `assertModuleGraph` on every pull request — and on every push to
`main` — that touches a Gradle build script, `build-logic` or the version catalog: the only files
that can change the module graph. It is a separate workflow so that Kotlin-only changes skip the
full Gradle configuration it needs.

## String resources

Two scripts guard the `strings.xml` files, and the `translations` workflow runs both on every pull
request — and on every push to `main`:

```bash
python3 scripts/check_translations.py    # every string exists in values-pt-rBR and values-es
python3 scripts/check_unused_strings.py  # every string is referenced by the code
```

`check_unused_strings.py` reports a string no code references anymore, so a leftover of a removed
feature doesn't keep being translated. A Compose Resources string counts as used when some Kotlin
file imports its accessor (`import bibleplanner.feature.day.generated.resources.day_title`); an
Android `res/` string when some code references `R.string.<name>` or some XML `@string/<name>`.
Android Lint's `UnusedResources` can't do this: it doesn't see Compose Resources accessors.

When it fails, delete the reported strings from `values/strings.xml` and from every translation next
to it.
