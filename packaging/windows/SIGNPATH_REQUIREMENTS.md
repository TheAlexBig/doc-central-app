# SignPath Foundation Readiness Requirements

This document defines the requirements Central Docs must satisfy before the
project applies for free code signing through SignPath Foundation. It is a
readiness specification, not confirmation that SignPath will accept the
project. Acceptance remains at SignPath Foundation's discretion.

## Objective

Produce a Windows MSI that:

- is built from the public Central Docs source repositories on a GitHub-hosted
  runner;
- is submitted to SignPath without exposing a signing private key;
- receives an Authenticode signature and trusted timestamp;
- can be traced to the exact backend and frontend revisions used to build it;
- continues to receive the existing GitHub artifact provenance attestation;
- displays a verified Windows publisher instead of `Unknown publisher`.

The expected certificate publisher is `SignPath Foundation`. Participation in
the free program does not provide a certificate issued in the name of Central
Docs or its maintainer.

## Project Scope

The signed product is assembled from two repositories:

| Component | Repository | Default branch |
| --- | --- | --- |
| Desktop host, API, templates, and installer workflow | `TheAlexBig/doc-central-app` | `master` |
| React user interface | `TheAlexBig/doc-central-forms` | `main` |

Both repositories are part of the product's source and policy scope. A release
is not eligible for signing unless the exact frontend revision is recorded by
the backend build and both revisions satisfy the approved source policy.

## Mandatory Eligibility Gates

### R1. Open-source licensing

- Both repositories MUST contain an OSI-approved license.
- All code and other material maintained as part of Central Docs MUST be
  compatible with that license.
- Legal document template text, images, fonts, icons, and other bundled assets
  MUST have documented ownership and redistribution permission.
- The project MUST NOT use commercial dual licensing while it participates in
  the free SignPath Foundation program.
- Third-party dependencies MUST have licenses compatible with distribution in
  the signed MSI.

Current status: **In progress**. Apache-2.0 has been selected and added. The
ownership and licensing of the legal template text must still be confirmed by
the maintainer before applying.

Decision required: choose the project license and determine whether the legal
template text is covered by it or needs separate licensing documentation.

### R2. Public and maintained source

- Both repositories MUST remain publicly accessible.
- The application MUST be actively maintained.
- The source MUST include the scripts and workflow used to produce the MSI.
- The project MUST sign only Central Docs releases built from its own source.

Current status: **Partially satisfied**. Both repositories are public and the
build scripts are versioned. Maintenance and release history should be made
clearer through published releases and project metadata.

### R3. Existing public release

- Central Docs MUST have at least one public release in the same MSI format
  that will later be signed.
- The release page MUST describe the product, installation, uninstallation,
  supported Windows versions, and known first-run security warning.
- The release MUST include a SHA-256 checksum.
- The release MUST identify both the backend and frontend source revisions.

Current status: **Blocked**. No published GitHub Release is currently visible
in the backend repository.

### R4. Privacy and end-user behavior

- A public privacy policy MUST describe every category of local data created by
  the application and its Windows path.
- The policy MUST state whether any information is sent to a networked system.
- Any future network transmission MUST be documented and explicitly initiated
  or consented to by the user.
- The installer MUST announce material system changes.
- The application MUST support normal Windows uninstallation.

The privacy inventory MUST cover at least:

- saved people and DUI values;
- saved agents;
- saved vehicle values;
- generated Word and PDF documents;
- document history and draft data;
- editable legal templates;
- application configuration;
- diagnostic and startup logs.

Current status: **Partially satisfied**. The app operates locally and the MSI
supports uninstallation, but a complete public privacy policy is missing. The
frontend README statement that it does not store submitted personal data must
be reconciled with the desktop application's local persistence.

### R5. Code signing policy and roles

The repository and every release/download page MUST link to a policy headed
`Code signing policy`. The policy MUST include:

> Free code signing provided by SignPath.io, certificate by SignPath Foundation.

The following roles MUST be named and kept current:

| Role | Responsibility | Assigned member(s) |
| --- | --- | --- |
| Committer/author | Maintains source and build scripts | `TheAlexBig` |
| Reviewer | Reviews contributions from non-committers | `TheAlexBig` |
| Signing approver | Manually approves release signing requests | `TheAlexBig` |

- All assigned members MUST use multi-factor authentication for GitHub and
  SignPath.
- Every signing request MUST require manual approval by an assigned approver.
- A signing approver SHOULD NOT approve an artifact they cannot trace to the
  intended source revisions and successful verification jobs.

Current status: **Satisfied in repository**. The public policy and current role
assignments are defined in `CODE_SIGNING_POLICY.md`. SignPath account roles
remain to be configured after acceptance.

### R6. Trusted build origin

- Release MSI files MUST be built on GitHub-hosted runners.
- Signing MUST use SignPath's GitHub trusted build-system integration.
- The SignPath GitHub App MUST be granted access to every source repository
  used by the release build.
- The unsigned MSI MUST first be uploaded as a GitHub Actions artifact.
- The signing request MUST refer to the artifact ID provided by GitHub, not to
  a separately uploaded or locally built file.
- Self-hosted runners MUST NOT be used in the signing path under the free OSS
  policy.

Current status: **Partially satisfied**. The MSI already builds on the
GitHub-hosted `windows-2022` runner. SignPath integration is not configured.

### R7. Release workflow controls

Before a signing request, the workflow MUST verify:

- backend tests pass;
- frontend lint, tests, and production build pass;
- installer version matches the intended release version;
- backend and frontend revisions are recorded in build metadata;
- the MSI filename, product name, manufacturer/author metadata, and version
  match the approved SignPath artifact restrictions;
- no unapproved file is introduced into the signed package.

After signing, the workflow MUST:

- verify the MSI Authenticode signature and timestamp;
- verify the expected certificate subject/publisher;
- fail before publication if verification fails;
- attest the final signed MSI with GitHub artifact attestations;
- upload or publish only the signed MSI as the release artifact;
- generate and publish the SHA-256 checksum of the signed MSI.

Current status: **Partially satisfied**. Tests and GitHub attestation exist, but
version consistency, source-revision metadata, SignPath submission, signature
verification, and signed checksum publication are not yet implemented.

### R8. Artifact configuration

SignPath MUST be configured to accept only the intended Central Docs MSI. At a
minimum, the configuration MUST restrict:

- filename pattern: `CentralDocs-<version>.msi` or the final agreed pattern;
- MSI product name: `Central Docs`;
- MSI author/manufacturer: the final approved value;
- product version: the workflow release version;
- permitted nested executable paths and counts.

The artifact configuration SHOULD deep-sign executable files produced by
Central Docs inside the MSI when technically supported. It MUST NOT apply the
project signature to third-party binaries as if Central Docs maintained them.
Third-party files may be included unsigned when allowed by SignPath policy, or
their existing trusted signatures may be verified.

Current status: **Blocked** pending a sample MSI analysis and SignPath project
configuration.

## Recommended Supporting Controls

These controls improve the application but are not all explicit prerequisites
for the initial application:

- generate a CycloneDX SBOM for every release;
- enable Dependabot or equivalent dependency monitoring;
- add `SECURITY.md` with a private vulnerability-reporting route;
- protect release branches and require successful checks;
- pin GitHub Actions to reviewed commit SHAs;
- record dependency licenses during the build;
- document release rollback and certificate-revocation response;
- test installation, upgrade, repair, and uninstallation on a clean Windows VM.

## Decisions Required From the Maintainer

The following decisions cannot be inferred from the source code:

1. Licensing/ownership of the Salvadoran legal template text.
2. Whether the publisher display name `SignPath Foundation` is acceptable.
3. Final MSI filename and manufacturer metadata.

## Application-Ready Definition

Central Docs is ready to submit its SignPath Foundation application only when:

- every mandatory gate R1-R8 is marked satisfied with a public evidence link;
- the two repositories use compatible OSI-approved licenses;
- privacy and code-signing policies are linked from both repositories;
- an existing MSI release and checksum are publicly downloadable;
- project roles and MFA requirements are documented;
- a clean GitHub-hosted build succeeds from tagged source;
- the sample MSI structure and metadata are suitable for a constrained
  SignPath artifact configuration.

## Implementation Sequence

1. Resolve licensing and ownership decisions.
2. Add licenses and public privacy, security, and code-signing policies.
3. Correct project metadata and privacy statements in both READMEs.
4. Publish a documented unsigned baseline release with checksums.
5. Apply to SignPath Foundation.
6. Configure the SignPath project, trusted GitHub build system, roles, and
   artifact restrictions after acceptance.
7. Add signing submission, manual approval, signature verification, final
   attestation, and publication to `windows-installer.yml`.
8. Validate the signed installer on clean Windows systems before general
   release.

## References

- SignPath Foundation conditions: <https://signpath.org/terms.html>
- SignPath Foundation application: <https://signpath.org/apply.html>
- SignPath GitHub integration:
  <https://docs.signpath.io/trusted-build-systems/github>
- SignPath artifact configuration:
  <https://docs.signpath.io/artifact-configuration/>
