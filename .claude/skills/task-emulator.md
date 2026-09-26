# Task emulator

Shared by `start-task` and `finish-task`: each task gets an emulator of its own, so its device work
never lands on an emulator someone else is using.

Several sessions run at once, and every emulator on the machine is visible to all of them. A
`connectedAndroidDeviceTest` installs on every device it sees. A test then runs against an APK that
another session just replaced, and on a screen whose rotation someone else changed. The user's own
emulators (`Medium_Phone`, `Pixel_Tablet`, …) are theirs to look at, so tests never run on them.

## Name

`BiblePlanner_<short_description>`: the task's short description from `start-task`, with `-` turned
into `_`. For `feature/reading-streak`, that is `BiblePlanner_reading_streak`. The name follows from
the branch, so `finish-task` finds the same emulator without anything written down.

## Create

`start-task` does this right after it creates the branch. It only writes two small config files. No
disk image is copied, and the emulator only takes space once it boots for the first time.

The copy is taken from `Pixel_9`: an API 36 phone, above the API 30 the device tests need (see
[docs/testing/compose-ui-tests.md](../../docs/testing/compose-ui-tests.md#on-an-android-device)),
whose landscape is wide enough for the two-pane layouts. If `emulator -list-avds` doesn't list
`Pixel_9`, ask the user which AVD to copy.

```bash
AVD=BiblePlanner_<short_description>
SOURCE=Pixel_9
AVD_HOME=~/.android/avd
ls -d "$AVD_HOME/$AVD.avd" "$AVD_HOME/$AVD.ini" 2>/dev/null
```

If either path already exists, stop and tell the user, the way `start-task` does for a branch that
already exists. Otherwise:

```bash
mkdir "$AVD_HOME/$AVD.avd"
cp "$AVD_HOME/$SOURCE.avd/config.ini" "$AVD_HOME/$AVD.avd/config.ini"
sed -i '' "s/^AvdId=.*/AvdId=$AVD/; s/^avd.ini.displayname=.*/avd.ini.displayname=$AVD/" \
  "$AVD_HOME/$AVD.avd/config.ini"
printf 'avd.ini.encoding=UTF-8\npath=%s\npath.rel=avd/%s.avd\ntarget=%s\n' \
  "$AVD_HOME/$AVD.avd" "$AVD" "$(sed -n 's/^target=//p' "$AVD_HOME/$SOURCE.ini")" \
  > "$AVD_HOME/$AVD.ini"
```

Only `config.ini` is copied. Copying the source's `userdata` or `snapshots` would carry over its
apps, its data and its rotation.

## Boot

Boot the emulator only when the task needs a device. Give it a console port outside the range adb
scans on its own (5554–5585), and an adb server of its own:

```bash
CONSOLE_PORT=<an even port from 5700 to 5798 that `lsof -iTCP:<port>` shows free>
export ANDROID_ADB_SERVER_PORT=$((CONSOLE_PORT + 1000))
nohup ~/Library/Android/sdk/emulator/emulator -avd "$AVD" -port "$CONSOLE_PORT" \
  -no-window -no-snapshot -no-audio > /dev/null 2>&1 &
until [ "$(adb -s "emulator-$CONSOLE_PORT" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; do sleep 5; done
```

The loop is what waits for the boot. `adb -s <serial> wait-for-device` doesn't: it fails at once
with "device not found" while the emulator hasn't registered with the adb server yet.

The default adb server, the one Gradle and Android Studio use, never sees this emulator, so another
session's `connected*` task can't land on it either.

Always pass `-s emulator-<port>`. Even a private adb server finds the user's emulators on the usual
ports, and without `-s` an `adb install` fails on "more than one device", or lands on the wrong one.
A shell variable doesn't survive from one Bash call to the next, so repeat the `export` (and the
variables) in every call that runs `adb`. Drop `-no-window` when the user wants to watch.

## Run tests on it

Gradle's `connected*` tasks go through the default adb server and install on every device, so don't
use them here. Build the module's test APK, install it on this emulator, and run `am instrument`.
A module's device tests are one self-instrumenting APK: its package is the module's `namespace` plus
`.test`, and it holds the module under test, so there is no app APK to install next to it.

```bash
./gradlew :feature:books:assembleAndroidDeviceTest
adb -s "emulator-$CONSOLE_PORT" install -r feature/books/build/outputs/apk/androidTest/books-androidTest.apk
adb -s "emulator-$CONSOLE_PORT" shell pm clear com.quare.bibleplanner.feature.books.test
adb -s "emulator-$CONSOLE_PORT" shell am instrument -w \
  -e class com.quare.bibleplanner.feature.books.presentation.BooksUiTest \
  com.quare.bibleplanner.feature.books.test/androidx.test.runner.AndroidJUnitRunner
```

- Build through a task that ends in `AndroidDeviceTest`: only such a build raises the module's
  `minSdk` to 30, and the test APK doesn't dex without it.
- In worktree mode, point Gradle and the APK path at the worktree (`./gradlew -p <worktree>`), or
  you install the main checkout's build.
- Leave out `-e class` to run the module's whole `commonTest`, which is what CI runs on the device.
- `am instrument` exits 0 even when a test fails, and a `-e class` that matches no test prints
  `OK (0 tests)`. Read the output for `FAILURES!!!` and for the test count instead of trusting the
  exit code.
- A landscape run only stays in landscape with the rotation locked. `settings put system
  user_rotation` is undone as soon as a UiAutomation client disconnects:

  ```bash
  adb -s "emulator-$CONSOLE_PORT" shell cmd window user-rotation lock 1   # 0 for portrait
  adb -s "emulator-$CONSOLE_PORT" shell cmd window fixed-to-user-rotation enabled
  adb -s "emulator-$CONSOLE_PORT" shell dumpsys window | grep -m1 -o 'ROTATION_[0-9]*'
  ```

## Run the app on it

Run configurations and Gradle's `installDebug` pick a device on their own, so install the APK
directly:

```bash
./gradlew :androidApp:assembleDebug
adb -s "emulator-$CONSOLE_PORT" install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb -s "emulator-$CONSOLE_PORT" shell am start -n com.quare.bibleplanner/.MainActivity
adb -s "emulator-$CONSOLE_PORT" exec-out screencap -p > <scratchpad>/screen.png
```

## Delete

`finish-task` does this once the branch's merge is verified. Shut the emulator down if it is
running, wait for it to exit, then remove its two paths:

```bash
AVD=BiblePlanner_<short_description>
pkill -f -- "-avd $AVD( |$)"
while pgrep -f -- "-avd $AVD( |$)" > /dev/null; do sleep 1; done
rm -rf ~/.android/avd/"$AVD".avd ~/.android/avd/"$AVD".ini
```

- Never delete an AVD that isn't named after the task being finished. `Medium_Phone`, `Pixel_9`,
  `Pixel_Tablet` and every `TuneScout_*` are shared or belong to another project.
- If the task never created one (started before this step existed, or the user skipped it), there
  is nothing to delete, so move on.
