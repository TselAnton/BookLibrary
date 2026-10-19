# Release and update protocol

The published baseline is [4.1.0](https://github.com/TselAnton/BookLibrary/releases/tag/v4.1.0), built from main without an updater. Development continues as `5.0.0-SNAPSHOT`. Stable 5.0.0 must wait until development is complete. Installing 5.0.0 over the legacy baseline requires a one-time manual JAR replacement; subsequent update-capable versions can use the in-app flow.

## Release job

`.github/workflows/release.yml` is manually dispatched from the selected branch. `build/release.ps1` reads the Maven version, checks the exact commit, runs `clean verify` on Windows x64 / Java 17, and prepares an unsigned fat JAR. Main publishes stable versions; other branches publish unique `-preview.<run>.<commit>` versions and never become latest. A main SNAPSHOT is rejected before any release is created. The new POM uses Maven's revision property. The legacy baseline also works with its fixed 4.1.0 POM.

Publication is staged: push a new version tag, create a draft, upload every asset, then publish. Existing tags and releases are never overwritten. Failed uploads leave an unpublished draft; retry requires explicit cleanup of the draft/tag, or a new version. The job uses GITHUB_TOKEN with contents:write; no token is bundled in the application. The workflow must exist in main before it can be dispatched from GitHub's UI.

Assets:

- `BookLibraryApp-windows-x86_64.jar`: application, dependencies and embedded independent updater JAR.
- `BookLibraryApp-windows-x86_64.jar.sha256`: checksum for manual verification.
- `update-windows-x86_64.properties`: protocol, exact version, platform, minimum Java, asset name and SHA-256. Legacy builds have protocol 0 and require manual installation; update-capable builds have protocol 1.

The helper contains only update.shared and update.helper and depends only on JDK classes. It does not open the installed application's JAR. Maven creates and embeds it during process-classes, so tests and packaged applications use the same helper.

## User flow

After the main window is shown, a daemon task checks the same stable-release endpoint. A newer compatible release opens the update window with the already checked metadata; nothing is downloaded or installed automatically. No-new-release and offline results open no dialogs. Closing the main window cancels the check. An existing manual update window prevents a duplicate startup offer. Manual checks continue to display actionable network errors.

Справка → Проверить обновления opens a 560 × 400 sage modal. It shows the current version, progress, release notes, download and installation actions. Only published stable releases from TselAnton/BookLibrary are offered. Comparison is numeric; 5.0.0 is newer than 5.0.0-SNAPSHOT. Network failures and missing/incompatible assets are distinguished from an up-to-date result. Network work stays off the JavaFX thread. Closing a check/download cancels it. Once helper launch begins, wait for success or an error instead of starting another launch.

Downloads go to a unique OS temporary directory. Initial URLs must reference this repository's GitHub release assets; only HTTPS is permitted. Download size and duration are bounded. The exact size and SHA-256, JAR contents, application entry point, version, platform and update protocol are checked. SHA-256 protects integrity, not authenticity against a compromised release account; unsigned releases are an explicit product choice.

After confirmation, child windows close through their normal handlers. Canceling an unsaved form prevents installation. The trusted embedded helper is extracted alongside the download. A plan records canonical paths, source/original hashes, PID plus process start time, and a unique backup folder. The helper uses the installed Java runtime, not PATH. Unicode/spaced paths are passed as arguments. A Windows write probe selects normal launch or PowerShell RunAs. Canceling UAC keeps the application open. The elevated helper never launches the application as administrator.

Before requesting elevation, the application starts and verifies a separate restart process using its own privileges. That process uses the same JDK-only helper JAR in `--restart` mode. It first acknowledges readiness, then waits for the exact owner PID/start time to exit. A canceled or unsuccessful helper launch forcibly stops only this newly created restart process. The application closes only after both the restart process and installer have acknowledged readiness.

The helper validates again and writes pending markers plus a ready acknowledgment. Only then does the app exit through Platform.exit. The helper never kills it: it waits up to 120 seconds for that specific process, then acquires the data-directory OS lock. Timeout or a busy lock preserves the old JAR.

## Installation and backup

Normal startup holds `.booklibrary.lock` in its data directory until JVM termination, including remaining non-daemon exports. A second cooperating instance cannot open the same library. Pending markers are checked before JSON initialization and again after taking the lock. An installation-directory marker also protects launches with a different working directory.

After shutdown, while holding the lock, the helper creates:

```text
<installed-JAR-directory>/backup/updates/update-to-<version>-<UUID>/
    application.jar
    data/repository/...       (all regular files, including nested files)
    data/.properties         (if present)
    data-location.properties (previous mapping, if present)
    files.properties         (individual SHA-256 values)
    complete.properties      (completion marker and index hash)
```

Every copy is checked against the source hash, including another source hash after copying. Symlinks and paths escaping the backup directory are refused. The backup is verified before replacement. It is independent of ordinary JSON-backup retention, including maxArchivedBackups=0.

Only the JAR is replaced. JSON and settings are not overwritten by release content. External cover paths remain unchanged; external covers are not included in the backup. The helper writes `.booklibrary-data.properties` beside the JAR to retain the original absolute library directory across later launches. `-Dbooklibrary.dataDirectory=...` overrides this mapping. Installations without a mapping keep their historical working-directory behavior until the first managed update.

The download is copied to a sibling temporary JAR, flushed and checked. Replacement uses ATOMIC_MOVE + REPLACE_EXISTING on the same filesystem. There is no delete-old-then-copy gap or non-atomic fallback. An unsupported atomic move, read-only target, sharing violation or insufficient space aborts installation. A damaged/missing target observed after failure is restored from the verified application.jar backup when possible. Failed recovery retains pending markers and diagnostics rather than opening data in an uncertain state.

## Interrupted updates and recovery

After replacement, the helper verifies the installed hash, writes `.booklibrary-update-result.properties` and removes pending markers. The restart process requires a success result matching both the unique backup path and version in its own plan, absence of pending markers, release of the data-directory lock, and another validation of the installed JAR/hash. It launches the JAR with the original data directory as both working directory and an explicit `booklibrary.dataDirectory` property. It inherits the original application's privileges, independently of the elevated installer.

When a restart process is ready, the installer omits its success popup and the updated main window opens automatically. Standalone helper invocations without a restart process retain the completion popup and manual launch. Failed installations do not start any JAR. Diagnostics are written to `restart-error.log`; launch output and child PID/version are retained in `restart-application.log` and `restarted.properties`. Restart preparation is bounded to 10 seconds, owner-exit waiting to 5 minutes and installation-result waiting to 30 minutes. A restart failure reports how to launch manually after a successful installation.

Interruption before replacement leaves the old JAR intact. Interruption immediately after replacement can be completed using the pending plan and verified backup, even if the downloaded candidate has disappeared. A stale marker prompts to resume before data initialization. Recovery changes the recorded owner PID to the current bootstrap, so the helper also waits for this process to release the JAR. Prompts are serialized by the data lock. A running helper causes a wait-and-relaunch message instead of another installer.

If bootstrap itself cannot run, recover manually:

1. Close all BookLibrary/updater processes. Inspect update-error.log and helper-start.log beside the plan in the temporary directory.
2. Keep the entire backup. Verify application.jar against originalSha256 in complete.properties and the backup index before restoring it to the original JAR filename.
3. This updater never modifies JSON, so repository restoration is normally unnecessary. Future schema changes require a separate migration/rollback design.
4. Once the restored JAR is verified, remove only `.booklibrary-update.pending` beside that JAR and in the recorded data directory. Keep the data-location mapping or explicitly pass the original data path.
5. Start normally. Deleting `.booklibrary.lock` does not bypass a running process: the OS lock, not file presence, controls access.

There is no claim of 100% operation under hardware failure, filesystem corruption, external modification, antivirus interference or legacy applications ignoring locks. Backups and explicit failure/recovery states are the protection. Interactive UAC acceptance and cancellation depend on the Windows elevation policy; automated tests do not approve elevation dialogs.

## Verification

Tests cover numeric/prerelease comparison, invalid metadata/foreign URLs, corrupted downloads, standalone packaging, real process locks and shutdown handoff, backup contents/hashes, failed replacement, damaged-target restoration, interruption after replacement, missing download on resume and corrupt backup rejection. JavaFX tests check asynchronous UI and network-error reporting. Fixtures use temporary directories, including Cyrillic/spaced paths, without reading the user's repository.

Startup-check tests cover an asynchronous new-release offer without a second network check, silent offline/no-new-version results, cancellation when the main window closes and suppression of a duplicate manual window.

Restart process tests use an independently compiled executable fixture JAR. They verify that neither a live owner nor a failed installation triggers launch, that a successful installation executes the new version with the original working/data directory, and that the previous JAR remains in the verified backup.

### Protected Windows installation, 2026-10-20

An isolated packaged application was installed in `C:\Program Files\BookLibrary Update Test 83c254e0`. The test process had a medium-integrity token with the Administrators group disabled for allow checks. Creating a temporary file beside the JAR failed with AccessDeniedException, confirming that the production launcher's elevation branch was exercised. Test JSON (including a nested Cyrillic filename) and settings used an independent directory under `target/protected-update-test`, outside the user's library.

The production launcher extracted its embedded helper, invoked PowerShell RunAs, and the elevated helper replaced the test JAR and created its backup inside Program Files. A synthetic local candidate marked 6.0.0 was used only for this test; no GitHub release was created. Verification confirmed the candidate SHA-256 at the installed path, the original JAR SHA-256 in the backup, matching hashes for every original and backed-up JSON/settings file, and absence of pending markers. Reports and the generated probe are retained in `target/protected-update-test`.

The original JAR SHA-256 was `67014cb9447c59ecbf79a05339705c2be926924b8df84d536be60dba5bc569fc`; the installed local candidate SHA-256 was `77d1a9eceb0ec6dd669b1daf0f0592aacc91731eec66477a360ca69ba70c226c`.

Initially EnableLUA was 1 but ConsentPromptBehaviorAdmin was 0: Windows elevated without showing a consent dialog. After the user enabled notifications, the policy became 5. The user then rejected a real Java UAC prompt: the original process remained alive, all JAR/JSON/settings hashes stayed unchanged, and no ready/pending markers were created. The same request was then accepted; a local 7.0.0 candidate was installed successfully and a new JVM loaded its version and original data mapping. Candidate 7.0.0 SHA-256: `96ed677333bfd41f6b105212af96bdad9c08a43a570219dd20f0e4bdb4036735`. Interactive acceptance and cancellation were both confirmed by the user.

A launch from the restricted tool environment had previously failed with 0xc0000142 before a helper started; the original JAR remained intact. Running the same ordinary-token probe outside that restriction succeeded.

### Protected Windows automatic restart, 2026-10-20

The application with the new restart mechanism was copied into `C:\Program Files\BookLibrary Restart Test 93c254e0`, with independent data under `target/protected-restart-test`. A local 8.0.0 candidate contained the actual application and dependencies. On a real UAC refusal, the probe verified unchanged JAR/JSON/settings hashes, no pending markers, a living original process and a stopped restart process. On acceptance, the installer backed up and replaced the JAR, and the real JavaFX main window opened automatically. The user confirmed both actions and the opened window.

A read-only Windows TokenElevation query on the restarted javaw process returned 0, proving that the elevated installer did not pass administrator privileges to the application. A new JVM verified version 8.0.0 and the original data mapping; every original JSON/settings file and its backup retained its original SHA-256. The original JAR hash was `76f9ead7ab3b5e0b5473e3f43325b1a4064adb78422ca03ca727e7791ee8376f`; the installed candidate hash was `276968acaeeaed789771ed88713dfc37a17bebb42ade00cc8cd9966ab071c24c`. Reports, generated probes and token verification are retained under `target/protected-restart-test` (removed by Maven clean). Versions 6.0.0, 7.0.0 and 8.0.0 were synthetic local fixtures; none was published. Development remains 5.0.0-SNAPSHOT.

Before stable 5.0.0: finish feature validation, change revision to 5.0.0, run the job from main, and perform a Windows acceptance update between released builds, including UAC cancellation, a locked target and launch during replacement.
