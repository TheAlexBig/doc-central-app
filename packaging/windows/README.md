# Windows Installer

The requirements and readiness gates for applying to the SignPath Foundation
open-source signing program are tracked in
[`SIGNPATH_REQUIREMENTS.md`](SIGNPATH_REQUIREMENTS.md).
Suggested answers for the public application fields are maintained in
[`SIGNPATH_APPLICATION.md`](SIGNPATH_APPLICATION.md).

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

Arguments are `version`, `frontend ref`, `backend ref`, and an optional
`publish release` boolean that defaults to `true`. Pass `false` as the fourth
argument for a verification build that must not create a release. The script
uses the GitHub CLI to dispatch the Windows build, wait for it, download the
MSI and checksum to `target/windows-installer/<workflow-run-id>/`, verify the
SHA-256 checksum, and verify its GitHub artifact attestation when the installed
GitHub CLI supports `gh attestation`.
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

## Automated Releases

By default, a successful workflow run creates the immutable tag
`v<version>` and publishes a GitHub Release containing:

- `Central.Docs-<version>.msi`;
- `SHA256SUMS.txt` generated from that exact MSI;
- release notes containing the backend revision, frontend revision, and
  GitHub Actions run ID.

The workflow refuses to replace an existing release. Increment the version in
the backend `pom.xml`, frontend `package.json` and `package-lock.json` before
publishing. All three values must match the workflow input or the build fails
before packaging. The final MSI receives its GitHub provenance attestation
before it is published.

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
UUID so later installer versions can replace earlier ones cleanly. During an
upgrade, the MSI asks the running `Central Docs.exe` process to close, waits up
to ten seconds, and terminates it only if it does not exit. User data beneath
`%LOCALAPPDATA%\Central Docs` is not removed.
