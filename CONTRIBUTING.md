# Contributing to Trusted Links

Thanks for helping improve Trusted Links.

## Reporting bugs

Before opening an issue, check that the problem occurs on Minecraft 26.1.2 with the current Fabric Loader and Fabric API versions. Include:

- Trusted Links version
- Fabric Loader and Fabric API versions
- Steps to reproduce
- Expected and actual behavior
- Relevant lines from `logs/latest.log`
- Other installed mods that may affect chat, screens, or clickable text

Do not post malicious test URLs or security vulnerabilities in a public issue; follow `SECURITY.md`.

## Development

Trusted Links requires Java 25. Build and test with:

```bash
./gradlew build
```

Keep changes client-side and narrowly scoped. New URL or hostname behavior should include unit tests. In particular, changes must preserve these properties:

- Unknown and malformed URLs use vanilla behavior.
- Only HTTP and HTTPS URLs can bypass confirmation.
- Matching uses parsed hostnames and label boundaries.
- A trusted hostname never implies trust for its parent or sibling hostnames.
- Servers never need the mod installed.

## Pull requests

Keep each pull request focused, explain the user-visible behavior, and describe how it was tested. The GitHub Actions build must pass before merge.
