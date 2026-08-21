# Appium Mobile Automation Testing — Study Guide

*Personal reference — written so I can set this up from scratch on a new PC without relying on the course again.*
*Course: Appium (Android + iOS), 26 sections, 157 lectures, 26h 5m total*

---

## 1. What is Appium

Appium is an **open-source mobile automation server**. It's not a library you import directly into your test code — it's a **separate background process** (built on Node.js) that your test code talks to over HTTP, the same way a browser automation tool talks to a browser driver.

**How the pieces connect:**

```
IntelliJ (IDE)
   → Maven (builds project, resolves dependencies)
      → TestNG (runs the test)
         → Your Java Test code
            → sends HTTP request to →  Appium Server (Node.js, port 4723)
                                          → hands off to →  UiAutomator2 Driver (Android)
                                                               → talks via →  adb (Android Debug Bridge)
                                                                                 → Emulator (AVD) or Real Device
```

**Key things to remember:**
- Appium server must be **running** before any test can connect to it.
- The server itself doesn't know how to automate Android or iOS on its own — it needs a **driver** installed for each platform (UiAutomator2 for Android, XCUITest for iOS). Appium 2.x ships with **zero drivers by default** — you install them separately.
- Everything below the Appium server (driver → adb → device) depends on the **Android SDK** being correctly installed and pathed — most early setup errors come from this layer, not from Java/Maven/TestNG.

**Full picture, laid out end to end:**

```
┌─────────────┐     compiles/runs      ┌──────────────┐
│  IntelliJ   │ ──────────────────────▶│    Maven      │
│  (IDE)      │                        │ (build tool)  │
└─────────────┘                        └───────┬───────┘
                                                │ runs
                                                ▼
                                        ┌──────────────┐
                                        │  TestNG       │
                                        │ (test runner) │
                                        └───────┬───────┘
                                                │ executes
                                                ▼
                                    ┌────────────────────┐
                                    │  Your Java Test     │
                                    │  (AppiumBasicsTest)  │
                                    └──────────┬──────────┘
                                               │ HTTP request (new session)
                                               ▼
                                    ┌────────────────────┐
                                    │  Appium Server       │
                                    │  (Node.js, port 4723)│
                                    └──────────┬──────────┘
                                               │ hands off to driver
                                               ▼
                                    ┌────────────────────┐
                                    │  UiAutomator2 Driver  │
                                    │  (needs Android SDK)  │
                                    └──────────┬──────────┘
                                               │ talks via adb
                                               ▼
                                    ┌────────────────────┐
                                    │  adb (Android Debug   │
                                    │  Bridge)               │
                                    └──────────┬──────────┘
                                               │
                                               ▼
                                    ┌────────────────────┐
                                    │  Emulator (AVD) or    │
                                    │  Real Device           │
                                    └────────────────────┘
```

Most errors happen somewhere in this chain, not in your test code itself — when debugging, ask "which layer is actually failing" before assuming it's a Java problem.

---

## 2. Tools Required

| Tool | What it's for |
|---|---|
| **JDK** | Compiles and runs the Java test code |
| **Maven** | Build tool — resolves dependencies, compiles, runs tests |
| **IntelliJ IDEA** | IDE — writing code, running TestNG tests, Maven integration |
| **Node.js** | Runtime that the Appium server itself is built on |
| **Appium Server** (npm package) | The automation server your tests talk to |
| **UiAutomator2 driver** | Appium plugin — lets Appium actually control Android |
| **Android Studio** | Provides the Android SDK, AVD Manager, and emulators |
| **Android SDK / platform-tools** | Contains `adb`, required by the UiAutomator2 driver |
| **TestNG** | Java test framework — annotations, assertions, test runner |

---

## 3. Installation (From Scratch, In Order)

1. **Install JDK** — download, install, confirm with `java -version`.
2. **Install Node.js** — required before Appium, since Appium is an npm package. Confirm with `node -v` and `npm -v`.
3. **Install Appium server globally via npm:**
   ```bash
   npm install -g appium
   ```
   Confirm with `appium -v`.
4. **Install the UiAutomator2 driver** (Appium installs with zero drivers by default):
   ```bash
   appium driver install uiautomator2
   appium driver list --installed
   ```
5. **Install Android Studio** — this bundles the Android SDK and AVD Manager. Note the SDK path shown in **Settings → Languages & Frameworks → Android SDK** — you'll need it for env vars.
6. **Create a virtual device (AVD)** via Android Studio's Device Manager (e.g. Pixel 7).
7. **Set environment variables** (see Section 7 below — this is the step most likely to break on a fresh PC).

8. **Install Maven standalone.**
   IntelliJ bundles its own internal copy of Maven, but that copy is **not** on the system `Path` — a plain terminal (PowerShell, cmd) has no idea `mvn` exists unless it's installed separately at the OS level.

    - Download from the official site: **https://maven.apache.org/download.cgi** — grab the **Binary zip archive** (`apache-maven-x.x.x-bin.zip`).
    - Extract it somewhere permanent, e.g. `C:\tools\apache-maven-3.9.16`.
    - Add `C:\tools\apache-maven-3.9.16\bin` to your **system `Path`** environment variable (this is what makes `mvn` work in *any* terminal, not just IntelliJ's).
    - Open a **new** terminal window and confirm:
      ```bash
      mvn -version
      ```
    - This should print the Maven version, the Maven home path, and the JDK it's using — confirming it's installed globally, not per-project.

   **Add dependencies to `pom.xml`** — this is how Maven knows to download and use a library like Appium's Java client. Example (from Maven Central, the official public repository):
   ```xml
   <dependency>
       <groupId>io.appium</groupId>
       <artifactId>java-client</artifactId>
       <version>10.1.1</version>
       <scope>compile</scope>
   </dependency>
   ```
   You can search for the exact `groupId`/`artifactId`/latest `version` of any library at **https://mvnrepository.com** or **https://search.maven.org** — copy the dependency block shown there directly into your `pom.xml`'s `<dependencies>` section. Maven then downloads the jar automatically the next time you build.

9. **Install IntelliJ IDEA**, open/create the Maven project, add `java-client` (Appium) and `testng` dependencies to `pom.xml` (see step 8 above for the exact block).

   **IntelliJ setup checklist — things easy to miss on a fresh install:**
    - **File → Settings → Plugins** — confirm **Maven** and **TestNG** plugins are enabled (usually bundled by default, but worth checking after a clean install).
    - **Install the Claude Code plugin** (GUI) via Settings → Plugins → Marketplace → search "Claude Code" — lets you use Claude directly inside IntelliJ for coding help, not just chat in a browser.
    - **JetBrains MCP server** — if using AI tooling that connects via MCP (Model Context Protocol), this can be enabled under Settings → Tools (or via the AI Assistant/Claude Code plugin settings, depending on IDE version) so external AI tools can interact with the IDE directly.
    - **Build tool support** — confirm both **Maven** and **Gradle** integration plugins are present under Settings → Plugins → Build Tools, even if this project only uses Maven — some Appium/Java course material or sample projects may use Gradle instead.
    - **JDK setup** — File → Project Structure → SDKs, confirm the correct JDK is registered and selected for the project.

10. **Start the emulator.**
    Two ways to do this — prefer the terminal method (see Section 6 below for why):

    - **Terminal (preferred):**
      ```bash
      cd <SDK>\emulator
      emulator -list-avds
      emulator -avd "Pixel_7_API_37" -verbose
      ```
      Replace `"Pixel_7_API_37"` with whatever name `-list-avds` actually prints. `-verbose` streams real boot logs to the terminal — you see exactly what's happening, including errors, in real time.
    - **Android Studio GUI (fallback):** Device Manager → click the ▶ Play icon next to your AVD. Works, but the status indicator ("Starting up") can hang/lie — see Section 6.

11. **Verify the whole chain works:**
    - Start the Appium server (`appium` in a terminal)
    - Start the emulator (see step 10)
    - Run `adb devices` — should show the emulator as `device` (not `offline`, not missing)
    - Run a basic TestNG test that opens the app

---

## 4. Reusable Test Setup Pattern (Programmatic Server Start)

Once this pattern works, it's the same skeleton for basically every future test — this is the "do it once, reuse forever" boilerplate.

```java
package com.pat.appium;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.testng.annotations.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class AppiumBasicsTest {
    @Test
    public void Test() throws MalformedURLException, URISyntaxException {

        // Setup
        AppiumDriverLocalService service = new AppiumServiceBuilder()
                .withAppiumJS(new File("C:\\Users\\gonza\\AppData\\Roaming\\npm\\node_modules\\appium\\build\\lib\\main.js"))
                .withIPAddress("127.0.0.1")
                .usingPort(4723)
                .build();
        service.start();

        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName("Pixel_7");
        options.setApp("C:\\Users\\gonza\\IdeaProjects\\AppiumMobileTests\\src\\main\\java\\resources\\ApiDemos-debug.apk");

        // Android Driver
        AndroidDriver driver = new AndroidDriver(new URI("http://127.0.0.1:4723").toURL(), options);

        // Actual Automation
        // ... test steps go here ...

        driver.quit();
        service.stop();
    }
}
```

### Why each piece exists (per the java-client docs)

**`AppiumDriverLocalService` + `AppiumServiceBuilder`**
These two classes let you **start and stop the Appium server from inside your Java code**, instead of manually opening a separate terminal and typing `appium` every time before running a test. `AppiumServiceBuilder` is a builder — every method call (`.withAppiumJS(...)`, `.withIPAddress(...)`, `.usingPort(...)`) configures one setting and returns the same builder object (`self-reference`, per the javadoc), which is why they can be chained fluently into one statement. `.build()` at the end produces the actual `AppiumDriverLocalService` object; `.start()` is what physically launches the Node.js process running Appium.
**Why this matters for real work:** this is exactly the pattern used for **CI/CD pipelines** (Jenkins, etc.) — a fully automated pipeline can't have a human sitting there manually typing `appium` in a terminal before every run. Programmatic start/stop means the whole test suite is self-contained and can run unattended.

**`.withAppiumJS(File)`**
Tells the builder exactly which `main.js` file to execute as the server entry point (per docs: *"an executable appium.js (1.4.x and lower) or main.js (1.5.x and higher)"*). This is the same `main.js` path noted in Section 7 — normally Appium finds this automatically when you just type `appium` in a terminal, but when starting it programmatically, you have to point directly at the file.

**`.withIPAddress(String)`**
Sets the address the server should bind to. **Expects a bare host/IP only** — `"127.0.0.1"`, not `"http://127.0.0.1"`. This is exactly the bug documented in Section 6 above (`UnknownHostException: http`) — passing a full URL here breaks both Appium's own CLI argument validation and the Java client's server-availability ping.

**`.usingPort(int)`**
Sets which port the server listens on (`4723` is Appium's conventional default, though any free port works). Must match the port used later when constructing the driver's connection URL — if these two don't agree, the driver will try to connect to a server that isn't listening on that port.

**`service.start()` / `service.stop()`**
Actually launches / shuts down the Node.js Appium process. `service.stop()` should always be called when the test finishes (ideally in a `finally` block or `@AfterMethod`) — otherwise the Appium server process keeps running in the background even after your test ends, which can cause port conflicts on the next run.

**`UiAutomator2Options`**
A capabilities builder specific to Android automation via the UiAutomator2 driver. Instead of manually building a raw key-value capabilities map (the old, error-prone way), this class provides typed setter methods — `setDeviceName()`, `setApp()`, `setUdid()`, etc. — for every capability the UiAutomator2 driver understands, with autocomplete and compile-time checking instead of typo-prone string keys. Full method list: `https://www.javadoc.io/doc/io.appium/java-client/latest/io/appium/java_client/android/options/UiAutomator2Options.html`

**`new AndroidDriver(URL, options)`**
This is the actual moment the test connects to the running Appium server and requests a new automation session. It sends an HTTP `POST /session` request to the server's URL, with `options` serialized as the desired capabilities — the server then hands that request off to the UiAutomator2 driver, which uses `adb` to install/launch the app on the target device (see the architecture diagram in Section 1). This is why the URL and port here (`http://127.0.0.1:4723`) must exactly match what was configured on the `AppiumServiceBuilder` above — same server, two different places it's referenced.

**Why `new URI(...).toURL()` instead of `new URL(...)` directly**
Constructing a `URL` object directly from a string is considered legacy/unsafe in modern Java, since it performs zero validation of the string's structure at construction time — malformed URLs can silently produce broken `URL` objects. Building a `URI` first validates the string against proper URI syntax rules, and only then converting it to a `URL` (`.toURL()`) catches syntax errors early and explicitly, via a checked `URISyntaxException` — which is exactly why the method signature above declares `throws URISyntaxException`.

---

## 5. Commands Used

### ADB / Emulator

```bash
adb devices
```
Shows connected/emulated devices and their state (`device`, `offline`, or missing entirely).

```bash
adb kill-server
adb start-server
```
Forces a clean restart of the adb server — fixes a lot of "offline" or duplicate-server issues.

```bash
adb reconnect offline
```
Attempts to force-reconnect a device stuck in `offline` state without a full server restart.

```bash
tasklist | findstr adb
```
(Windows) Lists all running `adb.exe` processes — useful for spotting duplicate adb servers fighting over the same port.

```bash
taskkill /F /IM adb.exe
```
(Windows) Force-kills all adb processes, so you can restart clean.

```bash
emulator -list-avds
```
Shows the list of available AVDs (Android Virtual Devices) by name.

```bash
emulator -avd "emulator name" -verbose
```
Launches a specific AVD by name with live logs streamed to the terminal. **This is the method that actually worked reliably** — see Section 6, "adb devices shows offline," for the real-world case where this solved a stuck/lying GUI status.

### Appium

```bash
appium
```
Starts the Appium server (defaults to `127.0.0.1:4723`). Must be running before any test tries to connect.

```bash
appium driver install uiautomator2
appium driver list --installed
```
Installs and verifies the Android driver.

### Maven

```bash
mvn -version
```
**How this gets installed / what "global" means here:** Maven is installed **once, at the OS level** — not per-project, and not something `npm`/`pip`-style tools install locally into a project folder. You download the binary zip from the Apache site, extract it to a permanent folder, and add its `bin` folder to the system `Path` environment variable (see Section 3, step 8). Once that's done, **any terminal window on the machine** — regardless of which project folder you're in — can run `mvn`, because `Path` is a system-wide setting, not tied to a specific project or IDE. This command confirms it worked and shows which Maven version + JDK it's using.

```bash
mvn clean
```
Deletes the `target/` build folder — wipes stale/half-broken build artifacts.

```bash
mvn clean install
```
Full rebuild: compiles `src/main`, compiles + runs `src/test` (with test-scope dependencies like TestNG visible), packages, installs to local `.m2` repo.

**When to actually use this (real scenario hit during setup):** IntelliJ was showing red "cannot resolve dependency" errors on `testng:7.12.0` and `java-client:10.1.1` — both of which *did* exist on Maven Central and were correctly declared in `pom.xml`. Running `mvn clean install -U` from the terminal forced Maven to bypass IntelliJ's stale internal cache/index and re-fetch real dependency metadata directly from Central, which resolved the phantom errors. **General rule of thumb — reach for `mvn clean install` whenever:**
- The IDE shows dependency/compile errors that don't match what's actually in `pom.xml` (stale index)
- You just added a new dependency and want to confirm it actually resolves and compiles
- Before committing code, to confirm a truly clean build works (not just "works in my currently-open IDE session")
- After pulling changes from git that touched `pom.xml`

```bash
mvn clean install -U
```
Same as above, but forces Maven to re-check for updated dependency versions instead of trusting the local cache — useful when a very recently published dependency version isn't resolving.

---

## 6. Debugging Problems & Solutions

Real errors hit during setup, kept in the order encountered — cause, fix, and the underlying lesson for each.

### "IntelliJ shows red errors on dependencies that definitely exist on Maven Central"
- **Cause:** IntelliJ's local dependency index was cached before a very recently published version (e.g. `testng:7.12.0`) existed, so it never re-checked against Central.
- **Fix:** Maven tool window → **Reload Maven Project** (refresh icon).
- **Lesson:** IDE red squiggles ≠ the dependency is actually broken. Verify the artifact exists on Central first.

### `mvn : The term 'mvn' is not recognized...`
- **Cause:** IntelliJ bundles its own internal Maven, but that copy isn't on the system `Path` — a plain terminal has no idea `mvn` exists.
- **Fix:** Installed Maven standalone, added its `bin` folder to system `Path`, opened a **new** terminal window.

### `package org.testng.annotations does not exist`
- **Cause:** Test class was in `src/main/java` instead of `src/test/java`. Maven only exposes `test`-scope dependencies (like TestNG) to code under `src/test/java`.
- **Fix:** Moved the class to `src/test/java/...`.

### TestNG naming warning: `Test class name doesn't match regex ...`
- **Cause:** Surefire only auto-discovers classes matching patterns like `*Test`, `*Tests`, `Test*`. My class was named `AppiumBasics`.
- **Fix:** Renamed class + file to `AppiumBasicsTest`.

### `SessionNotCreatedException`: *Neither ANDROID_HOME nor ANDROID_SDK_ROOT environment variable was exported*
- **Cause:** UiAutomator2 driver couldn't locate the Android SDK — `ANDROID_HOME` wasn't set at the OS level.
- **Fix:** Set `ANDROID_HOME` in Windows environment variables, then **restarted the Appium server terminal** (the specific process that reads that variable — not everything needs restarting).
- **Lesson:** HTTP 500 = server-side failure, not a code bug. Read the `Message:` line first; Appium error messages are usually very literal about what's actually missing.

### `'adb' is not recognized as an internal or external command`
- **Cause:** `ANDROID_HOME` tells tools *where* the SDK is, but `Path` didn't include the `platform-tools` subfolder where `adb.exe` actually lives.
- **Fix:** Added `<SDK>\platform-tools` to system `Path`. New terminal to pick up the change.

### Emulator stuck on "Starting up" in Android Studio's Device Manager
- **Cause:** The GUI spawns the emulator as a child process and polls its status — if the process hangs or crashes, the GUI often has no real "failed" state and just shows a stale spinner forever.
- **Fix:** Launched directly from terminal instead: `emulator -avd <name> -verbose`. Real logs, real errors, no fake loading state.

### `adb devices` shows device as `offline` (not missing — offline)
- **Cause:** Emulator's graphics/UI can fully boot (visible home screen) while `adbd` (the internal daemon that talks back to your PC) hasn't completed its handshake — often worsened by **duplicate adb servers** (Android Studio's bundled one + a manually started one) fighting over the same port.
- **What actually fixed it:** Closing the emulator entirely and launching it **directly from the terminal** with `emulator -avd "name" -verbose` instead of starting it through Android Studio's Device Manager GUI. The GUI-launched emulator was the one getting stuck in `offline` and taking a very long time with no useful feedback. Calling it directly from cmd bypassed whatever was wedging the GUI path and connected cleanly and fast.
- **Other things tried before landing on the real fix (kept for reference, in escalation order):**
    1. `adb kill-server` + `adb start-server`
    2. `adb reconnect offline`
    3. `tasklist | findstr adb` → kill duplicates with `taskkill /F /IM adb.exe` → `adb start-server`
    4. Cold Boot the emulator (Device Manager → ⋮ → **Cold Boot Now**)
- **Lesson:** "Offline" ≠ "no device found" — different failure mode, different fix. And more broadly: **when the GUI path is unreliable, launch the same tool directly from the terminal** — same emulator binary, but without Android Studio's extra polling/status layer in the way.

### `Could not find a driver for automationName 'UIAutomator2' and platformName 'ANDROID'`
- **Cause:** The UiAutomator2 driver plugin itself was never installed into the Appium server. Appium 2.x separated the server from its drivers — installing Appium doesn't install any driver by default.
- **Fix:** `appium driver install uiautomator2`, confirmed with `appium driver list --installed`.

### `AppiumServerHasNotBeenStartedLocallyException` + `UnknownHostException: http` (starting Appium server programmatically via `AppiumServiceBuilder`)
- **Symptom:** Server fails to start with a CLI argument validation error (`main.js server: error: argument --address/-a: FORMAT must match format "hostname"`), then times out with `Appium HTTP server is not listening at http://http://127.0.0.1:4723/` — note the doubled `http://`.
- **Cause:** `.withIPAddress("http://127.0.0.1")` was passed a full URL (with scheme) instead of a bare hostname/IP. `withIPAddress()` expects just `127.0.0.1`, not `http://127.0.0.1`. Appium's own CLI arg validator rejects the malformed hostname, and separately, the Java client's availability-checker prepends its own `http://` on top when pinging the server, producing the doubled-scheme URL and the resulting `UnknownHostException: http`.
- **Fix:** Drop the scheme in `withIPAddress()` — pass only `"127.0.0.1"`:
  ```java
  AppiumDriverLocalService service = new AppiumServiceBuilder()
          .withAppiumJS(new File("C:\\Users\\gonza\\AppData\\Roaming\\npm\\node_modules\\appium\\build\\lib\\main.js"))
          .withIPAddress("127.0.0.1")   // no "http://" here
          .usingPort(4723)
          .build();
  service.start();
  ```
  The scheme is still correct (and needed) in the separate `new URI("http://127.0.0.1:4723")` call used for the actual driver connection — it's specifically `withIPAddress()` on `AppiumServiceBuilder` that wants a bare host, not a URL.
- **Lesson:** Different Appium/Selenium APIs have different expected input formats for "the same thing" (address vs. URL) — always check whether a method wants a bare host/IP or a full URL with scheme; mixing them up produces confusing double-prefixed errors downstream rather than a clear "wrong format" message at the call site.

---

## 7. Technical Notes for Reference

### Environment Variables — what each one does, who reads it

| Variable | Who reads it | What breaks without it |
|---|---|---|
| `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) | Appium's UiAutomator2 driver | `SessionNotCreatedException`: "Neither ANDROID_HOME nor ANDROID_SDK_ROOT..." |
| `Path` → `...\Sdk\platform-tools` | Any terminal trying to run `adb` | `'adb' is not recognized...` |
| `Path` → `...\Sdk\emulator` | Terminal running `emulator` command | `'emulator' is not recognized...` |
| `JAVA_HOME` | Maven, IntelliJ (usually auto-detected) | Build tool can't find JDK |

**Golden rule:** environment variables are read once, at process startup, and cached in memory.
- A process already running **before** a variable is set/changed will **not** see the new value.
- Only a **freshly opened** process (new terminal, restarted IDE, restarted Appium server) picks up the new value.
- Only restart the specific process that actually *reads* the changed variable — trace the parent/child chain (e.g. is Appium running standalone, or spawned from inside IntelliJ's terminal?) rather than restarting everything defensively.

### Where Appium itself actually lives (npm global install)
Main Appium server entry file:
```
C:\Users\gonza\AppData\Roaming\npm\node_modules\appium\build\lib\main.js
```
This is the actual file Node.js runs when you type `appium` in a terminal — useful to know if you ever need to check the installed version's source, debug a weird global-install issue, or point another tool directly at it.

### Appium 2.x vs 1.x
Appium 2.x separated the **server** from its **drivers**. Installing/starting the Appium server does not give you Android or iOS automation — you must separately install `uiautomator2` (Android) and/or `xcuitest` (iOS) drivers.

### Where to find available methods/capabilities
`UiAutomator2Options` javadoc — the master list of every method callable on the options object (`setDeviceName`, `setApp`, `setUdid`, etc.):
```
https://www.javadoc.io/doc/io.appium/java-client/latest/io/appium/java_client/android/options/UiAutomator2Options.html
```
Same pattern applies for iOS under `XCUITestOptions`.

### General triage habits
1. **Read the `Message:` line of any exception first** — it's usually extremely literal about what's actually wrong. Don't just pattern-match on the exception class name (e.g. `SessionNotCreatedException` can mean several different root causes).
2. **HTTP status code tells you who's at fault before reading anything else:** `4xx` = client-side (your request was malformed), `5xx` = server-side (your request was fine, the server failed processing it).
3. **When a GUI hangs with no useful feedback, drop to the CLI equivalent** — fewer layers of abstraction between you and the real process/logs, so failures surface immediately instead of being silently swallowed by a status-polling UI.
4. **"Not found" and "found but broken" are different failure modes** (e.g. `adb devices` returning nothing vs. returning `offline`) — don't treat them the same; they have different fixes.
5. Maven strictly separates `src/main` and `src/test` scopes — a `test`-scope dependency (like TestNG) is invisible to code sitting in `src/main`.

---

## 8. Notes / Progress Log

*Add dated entries here as sections are completed.*

**Aug 20, 2026** — Finished full environment setup: JDK, Maven, IntelliJ, Node/Appium server, Android Studio + AVD, UiAutomator2 driver. Hit and resolved 10 separate setup errors (see Section 6 above) — env vars, PATH, adb offline states, GUI emulator hang, missing driver, and a programmatic-server-start address format bug. First test (`AppiumBasicsTest`) launches the app successfully on the emulator, including full server start/stop lifecycle managed from code. Next: locators (ID, XPath, Accessibility ID) and basic element interaction.

### Still To Learn / Revisit
- [ ] iOS setup (Xcode, XCUITest driver, iOS Simulator)
- [ ] Real device testing (UDID, provisioning)
- [ ] Page Object Model framework structure
- [ ] CI/CD integration (Jenkins) for automated runs
- [ ] BrowserStack cloud device execution

---

## 9. Course Curriculum Reference (Full Outline)

### Core Fundamentals
- Appium features & internal architecture
- Java basics for automation: variables, data types, arrays, ArrayList, loops, strings, methods, inheritance
- UIAutomator object & capabilities
- Starting/stopping Appium server programmatically (AppiumServiceBuilder)
- Appium Inspector — identifying elements

### Locators & Element Handling
- ID, XPath, Accessibility ID locators
- Handling mobile popups, extracting text, entering info

### Mobile Gestures
- Long press, scroll, swipe, drag & drop
- Miscellaneous gestures: orientation, key events

### End-to-End Practice Project (Shopping App)
- Filling form details
- Verifying toast messages
- Scrolling through product lists
- Dynamically selecting products by text
- Validating total amount functionality
- Code optimization with user-defined functions
- Validating mobile gestures (tap, long press)

### Hybrid App / Web View
- Context switching with ChromeDriver
- Desired capabilities for mobile browser testing
- Scrolling and assertions on browser tests

### iOS Automation
- Xcode + iPhone Simulator setup
- XCUITest capabilities
- IOSClassChain locator
- IOSPredicateString
- Long press, scrolling, picker components, sliding
- Real device testing (UDID, XcodeOrgId, Bundle ID generation)

### Framework Design
- Page Object Factory pattern
- Action methods in page object files
- Inheriting Android/iOS Actions classes for reusable methods
- Test strategy & setup/teardown methods
- Running all tests via single TestNG.xml
- Data-driven testing (DataProvider, JSON-driven parameterization)
- Global properties file

### Reporting & CI/CD
- Extent Reports setup (with automatic screenshots on failure)
- Jenkins setup & integration
- BrowserStack cloud integration for parallel execution

### TestNG Deep Dive
- Installation & setup in Eclipse
- Annotations (Part 1 & 2)
- Prioritizing test cases
- Include/exclude mechanisms
- Groups functionality
- Parameterization (XML file + DataProvider)
- Listeners interface
- Parallel execution & reporting

### Git Fundamentals
- Local repositories, staging, commits
- Branching & merging
- Remote repositories, push/pull
- Resolving merge conflicts

### Java Basics (Appendix)
- Classes, objects, interfaces, polymorphism, inheritance
- Arrays (single & multidimensional)
- Common interview questions (arrays, string reversal)
- Date/Calendar classes
- Access modifiers, constructors

---

## 10. Sample Resume Section — This Course, Harvard Format

Written **as if the full 157-lecture curriculum is completed** — Android + iOS, framework design, CI/CD, and TestNG — as if the framework built in this course were real work delivered. Action-verb bullets, reverse-chronological style, ready to drop into a full resume under a "Projects" heading.

---

### PROJECTS

**Cross-Platform Mobile Test Automation Framework — Appium, Java, TestNG, Maven**
*Personal Project*

- Installed and configured a complete Appium mobile automation environment for both Android and iOS (Node.js, Appium server, UiAutomator2 driver, XCUITest driver, Android SDK, AVD emulator, Xcode, iOS Simulator) across Windows and macOS.
- Designed and built a cross-platform **Page Object Factory** framework with a shared Appium Utils superclass, enabling reusable action methods across both Android and iOS test suites.
- Developed end-to-end Android test cases covering element location (ID, XPath, Accessibility ID), mobile gestures (tap, long-press, scroll, swipe, drag-and-drop), popup handling, and hybrid app WebView context switching via ChromeDriver.
- Developed end-to-end iOS test cases using XCUITest capabilities, iOS Class Chain and iOS Predicate String locators, covering gestures, picker components, sliding, and native app automation (e.g. Photos app).
- Automated a full shopping/e-commerce test flow: form submission, toast message validation, dynamic product selection by scanning scrollable lists, and cart total amount verification, on both platforms.
- Configured and executed tests on **real physical devices** (Android and iOS), including UDID registration, XcodeOrgId and Bundle ID generation for iOS provisioning.
- Implemented **data-driven testing** using TestNG DataProvider, sourcing parameterized test data from external JSON files parsed into HashMaps.
- Built a centralized global properties file for environment/configuration values shared across the framework.
- Configured **TestNG** at a framework level: prioritized test execution, include/exclude filtering, package-level execution with regex, custom groups, parameterization via `testng.xml`, custom Listeners for reporting hooks, and parallel test execution.
- Configured automated **Extent Reports** with HTML output and automatic screenshot capture attached to failed test cases via TestNG Listeners.
- Set up and integrated **Jenkins** for CI/CD, scheduling and parameterizing automated framework runs from a local CI server.
- Integrated **BrowserStack** cloud infrastructure to execute the framework's test suite in parallel across multiple real devices in the cloud.
- Triggered and parameterized framework test runs via Maven command-line options, passing runtime parameters directly into test execution.
- Debugged and resolved real environment-level automation failures — Android SDK/environment variable configuration, ADB device-connection state issues, Appium driver installation gaps, and cross-OS (Windows/Mac) environment variable setup — documenting root cause and resolution for each.
- Version-controlled the full framework using Git, including branching strategy and resolving merge conflicts, with all code pushed to a remote GitHub repository.

---

### TECHNICAL SKILLS *(relevant to this project)*

- **Languages:** Java

- **Mobile Automation — Android:** Appium, Appium Server, UiAutomator2 Driver, Android Studio, Android SDK, AVD Emulator, ADB (Android Debug Bridge), ChromeDriver (hybrid/WebView context switching)

- **Mobile Automation — iOS:** XCUITest Driver, Xcode, iOS Simulator, iOS Class Chain locator, iOS Predicate String locator, real device provisioning (UDID, XcodeOrgId, Bundle ID)

- **Framework Design:** Page Object Factory pattern, reusable Appium Utils superclass (Android/iOS shared architecture), data-driven testing (TestNG DataProvider, JSON parameterization), global properties/config management

- **Test Framework (TestNG):** Annotations, test prioritization, include/exclude filtering, regex package-level execution, groups, `testng.xml` parameterization, Listeners, parallel execution

- **Build & Dependency Management:** Maven (build lifecycle, command-line test triggering, runtime parameterization)

- **CI/CD & Cloud:** Jenkins (local CI/CD setup, job scheduling and parameterization), BrowserStack (cloud device execution, parallel test runs)

- **Reporting:** Extent Reports (HTML reports, automatic failure screenshots via TestNG Listeners)

- **Version Control:** Git (branching, merge conflict resolution, staging/commit workflow), GitHub (remote repository management)

- **Environment & Tooling:** Node.js, npm, JDK, Windows & macOS environment variable / PATH configuration, command-line (PowerShell/Terminal) troubleshooting

- **IDE:** IntelliJ IDEA (also compatible with Eclipse-based TestNG setup)

- **Platform Coverage:** Android (Emulator & Real Device), iOS (Simulator & Real Device)

---

*Note: this section assumes full course completion. Update specifics (app tested, exact bug counts, framework depth) to match what was genuinely built, and be ready to speak to the architecture/code directly if asked in an interview — especially the Page Object Factory design and the Jenkins/BrowserStack integration, since those are the strongest signals of framework-level (not just script-level) skill.*