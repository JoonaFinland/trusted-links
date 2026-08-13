# Changelog

All notable changes to Trusted Links will be documented here. The project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.0.0] - 2026-08-13

### Added

- Client-side trusted-hostname allowlist for Minecraft Java Edition 26.1.2.
- Secure exact-hostname and subdomain-boundary matching for HTTP and HTTPS URLs.
- **Yes & Trust Hostname** action on Minecraft's external-link confirmation screen.
- Automatic `config/trusted-links.json` creation and atomic configuration updates.
- Safe fallback behavior for missing or malformed configuration.
- Unit tests for trusted, unrelated, malicious-boundary, malformed, and unsupported-scheme URLs.
