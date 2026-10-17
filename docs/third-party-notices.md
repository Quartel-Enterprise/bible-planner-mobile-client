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
sources, upstream and source links, and that it is used unmodified) and the verbatim license text, `LGPL-2.1.txt`.

The `collectAppResources` task in `desktopApp/build.gradle.kts` copies `desktopApp/licenses/` into
the `common` app resources, which Compose Desktop puts in the app image of every target format.
There is no in-app screen for it: the app shows no copyright notices of its own, so the files in the
bundle are what the license asks for.

## Adding or updating a library

When a desktop dependency's license requires a notice (LGPL, MPL, an Apache `NOTICE` file), or one
of those libraries changes version:

1. Add or update its entry in `desktopApp/licenses/THIRD-PARTY-NOTICES.txt`.
2. Add the license text to `desktopApp/licenses/` if it isn't there yet, copied verbatim.
3. Check the packaged macOS app has it. Packaging refuses a Homebrew JDK, so point
   `DESKTOP_PACKAGING_JAVA_HOME` at a self-contained one (Temurin, Corretto) when Gradle runs on
   Homebrew's:

   ```bash
   ./gradlew :desktopApp:createDistributable
   ls desktopApp/build/compose/binaries/main/app/*/Contents/app/resources/licenses
   ```

LGPL libraries must also stay replaceable: ship them unmodified as their own JAR, never shaded or
merged into another one.
