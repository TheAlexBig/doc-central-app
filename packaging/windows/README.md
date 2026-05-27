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

Arguments are `version`, `frontend ref`, and `backend ref`. The script uses
the GitHub CLI to dispatch the Windows build, wait for it, download the MSI to
`target/windows-installer/<workflow-run-id>/`, and verify its GitHub artifact
attestation when the installed GitHub CLI supports `gh attestation`.
Authenticate once before using it:

```bash
gh auth login --hostname github.com
```

The default remote repositories are `TheAlexBig/doc-central-app` and
`TheAlexBig/doc-central-forms`; both are checked out by the Windows workflow.

## Build Provenance

The GitHub Windows workflow generates a signed artifact attestation for each
MSI using `actions/attest`. Because this repository is public, GitHub uses
Sigstore's public-good service, allowing users to verify that an installer was
built by this repository's workflow and was not modified afterward.

The Linux build helper verifies the downloaded installer automatically when a
recent GitHub CLI with the `attestation` command is installed; otherwise it
prints the verification command after downloading the MSI. An installer can
also be verified directly:

```bash
gh attestation verify path/to/CentralDocs-1.0.0.msi \
  --repo TheAlexBig/doc-central-app
```

Artifact attestations establish build provenance; they do not Authenticode
sign the MSI or set a Windows publisher identity. Windows can therefore still
display `Unknown publisher` when an attested MSI is launched.

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
