# Third-party notices

Some licenses require the app to ship a notice and the license text with every copy. The desktop
app carries such a notice today, for [JLayer](http://www.javazoom.net/javalayer/javalayer.html)
(`javazoom:jlayer`, LGPL 2.1) to decode chapter listening MP3s. Android, iOS and web don't use it.

## Where they live

| What | Path |
| --- | --- |
| Source, in the repository | `desktopApp/licenses/` |
| macOS app (`.dmg` and Mac App Store `.pkg`) | `BiblePlanner.app/Contents/app/resources/licenses/` |
| Windows (`.msi`) | `<install folder>\app\resources\licenses\` |
| Linux (`.deb`, `.rpm`) | `/opt/bible-planner/lib/app/resources/licenses/` |

The folder holds `THIRD-PARTY-NOTICES.txt` (library, version, the copyright notices from its
sources, upstream and source links, and that it is used unmodified), the verbatim license text,
`LGPL-2.1.txt`, and `jlayer-<version>-sources.jar`. Shipping the source with the app is the form of
LGPL 2.1 section 6(a) that needs no written offer and no hosting of our own, and it holds for the Mac
App Store copy too.

The `collectAppResources` task in `desktopApp/build.gradle.kts` copies `desktopApp/licenses/` and
the jars of the `lgplLibrarySources` configuration into the `common` app resources, which Compose
Desktop puts in the app image of every target format. The `desktop-build` job of the release
workflow fails when a package comes out without them.
There is no in-app screen for it: the app shows no copyright notices of its own, so the files in the
bundle are what the license asks for.

## Adding or updating a library

When a desktop dependency's license requires a notice (LGPL, MPL, an Apache `NOTICE` file), or one
of those libraries changes version:

1. Add or update its entry in `desktopApp/licenses/THIRD-PARTY-NOTICES.txt`.
2. Add the license text to `desktopApp/licenses/` if it isn't there yet, copied verbatim.
3. For an LGPL library, add its sources jar to `lgplLibrarySources` in `desktopApp/build.gradle.kts`,
   and its file names to the release workflow's "Verify the installers carry the third-party
   notices" step.
4. Check the packaged macOS app has it. The Compose plugin refuses to package with a Homebrew JDK
   (an app packaged with one can't load its fonts outside Homebrew), so point
   `DESKTOP_PACKAGING_JAVA_HOME` at a self-contained one (Temurin, Corretto) when Gradle runs on
   Homebrew's. `--rerun` because the plugin doesn't track the contents of the app resources, so an
   app image built before the change stays up to date with the old notices:

   ```bash
   ./gradlew :desktopApp:createDistributable --rerun
   ls desktopApp/build/compose/binaries/main/app/*/Contents/app/resources/licenses
   ```

LGPL libraries must also stay replaceable: ship them unmodified as their own JAR, never shaded or
merged into another one.
