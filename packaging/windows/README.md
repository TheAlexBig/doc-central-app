# Windows Installer

Run the installer build on Windows with a Java 21 JDK containing `jpackage`,
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
