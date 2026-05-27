# Windows Installer

`jpackage` does not cross-package native installers: Oracle's Java 21
documentation requires each package format to be built on the operating system
where it runs. A Windows MSI therefore cannot be emitted directly by Linux
`jpackage`.

## Build From Linux

The repository includes a GitHub Actions workflow that runs `jpackage` on a
Windows runner. After the backend and frontend changes have been pushed to
GitHub branches, run this on Linux:

```bash
./packaging/windows/build-from-linux.sh 1.0.0 main master
```

Arguments are `version`, `frontend ref`, and `backend ref`. The script uses the
GitHub CLI to dispatch the Windows build, wait for it, and download the MSI to
`target/windows-installer/`. Authenticate once before using it:

```bash
gh auth login --hostname github.com
```

The default remote repositories are `TheAlexBig/doc-central-app` and
`TheAlexBig/doc-central-forms`; both are checked out by the Windows workflow.

## Build On Windows

Run directly on Windows with a Java 21 JDK containing `jpackage`,
Node.js/npm, Maven Wrapper prerequisites, and WiX Toolset available on `PATH`.
The frontend repository is expected beside this backend repository:

```powershell
.\packaging\windows\build-installer.ps1 -Version 1.0.0
```

Increment `-Version` for an upgrade build and update the Maven project version
when publishing a corresponding application release.

The build performs these steps:

1. Builds the React production application from `doc-central-forms`.
2. Packages that `dist` output inside the Spring Boot jar with the `desktop`
   Maven profile.
3. Generates the Central Docs Windows icon and builds an MSI installer through
   `jpackage`.

The resulting MSI is written beneath `target\installer`. It includes its Java
runtime, creates Start Menu and desktop shortcuts, and uses a stable upgrade
UUID so later installer versions can replace earlier ones cleanly.
